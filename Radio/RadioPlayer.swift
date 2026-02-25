//
//  RadioPlayer.swift
//  Radio Lëtzebuerg
//
//  Multi-station audio player with background support
//

import Foundation
import AVFoundation
import MediaPlayer
import Combine

class RadioPlayer: NSObject, ObservableObject {
    @Published var isPlaying = false
    @Published var isLoading = false
    @Published var currentTrack = TrackInfo(title: "Title", artist: "Artist")
    @Published var currentStation: RadioStation
    @Published var currentArtwork: UIImage?  // Album artwork (nil = use station logo)
    
    private var player: AVPlayer?
    private var timeObserver: Any?
    private var cachedStationLogo: UIImage?
    private var cancellables = Set<AnyCancellable>()
    
    // UserDefaults key for last station
    private let lastStationKey = "LastPlayedStationID"
    
    override init() {
        // Load last played station or fallback to default
        let lastStationID = UserDefaults.standard.string(forKey: lastStationKey)
        let stations = RadioStation.stations

        // Restore last station only if it's still enabled
        if let stationID = lastStationID,
           let lastStation = stations.first(where: { $0.id == stationID && $0.enabled }) {
            self.currentStation = lastStation
        } else {
            // Fallback to RGL if available and enabled, otherwise first enabled station
            self.currentStation = stations.first(where: { $0.id == "rgl" && $0.enabled })
                ?? stations.first(where: { $0.enabled })
                ?? stations.first!
        }

        super.init()

        // Always persist so the station is saved even on first launch
        UserDefaults.standard.set(currentStation.id, forKey: lastStationKey)

        // Quiet log — remote will confirm below
        print("🎵 Using station: \(currentStation.name)")

        setupPlayer()
        setupRemoteControls()
        setupNotifications()

        // Once remote stations are loaded, restore the saved station from the authoritative
        // remote list. This also re-syncs the station object if the URL/name changed.
        RadioStationLoader.shared.$remoteLoadedAt
            .compactMap { $0 }
            .first()
            .receive(on: DispatchQueue.main)
            .sink { [weak self] _ in
                guard let self = self else { return }
                let remoteStations = RadioStationLoader.shared.stations
                let savedID = UserDefaults.standard.string(forKey: self.lastStationKey)

                if let savedID, let station = remoteStations.first(where: { $0.id == savedID && $0.enabled }) {
                    // Station still valid in remote — re-sync with latest data
                    print("🔄 Restored last station: \(station.name)")
                    if station.id != self.currentStation.id || station.streamURL != self.currentStation.streamURL {
                        self.currentStation = station
                        self.loadStation(station)
                    } else {
                        self.currentStation = station
                    }
                    UserDefaults.standard.set(station.id, forKey: self.lastStationKey)
                } else {
                    // Saved station removed or disabled in remote — fall back to default
                    print("⚠️ Station '\(savedID ?? "unknown")' no longer available, switching to default")
                    let fallback = remoteStations.first(where: { $0.id == "rgl" && $0.enabled })
                        ?? remoteStations.first(where: { $0.enabled })
                    guard let fallback else { return }
                    self.currentStation = fallback
                    self.loadStation(fallback)
                    UserDefaults.standard.set(fallback.id, forKey: self.lastStationKey)
                }
            }
            .store(in: &cancellables)
    }
    
    private func setupPlayer() {
        loadStation(currentStation)
    }
    
    func switchStation(_ station: RadioStation) {
        let wasPlaying = isPlaying
        
        // Stop current playback
        if isPlaying {
            stop()
        }
        
        // Switch station
        currentStation = station
        
        // Save last played station
        UserDefaults.standard.set(station.id, forKey: lastStationKey)
        print("💾 Saved last station: \(station.name)")
        
        // Reload player with new station
        loadStation(station)
        
        // Resume playback if it was playing
        if wasPlaying {
            play()
        }
    }
    
    private func loadStation(_ station: RadioStation) {
        // Remove observer from old player
        if let player = player, let currentItem = player.currentItem {
            player.removeObserver(self, forKeyPath: "timeControlStatus")
            currentItem.removeObserver(self, forKeyPath: "timedMetadata")
        }
        
        // Create new player with station URL
        guard let url = URL(string: station.streamURL) else { return }
        let playerItem = AVPlayerItem(url: url)
        player = AVPlayer(playerItem: playerItem)
        
        // Observe new player status
        player?.addObserver(self, forKeyPath: "timeControlStatus", options: [.new, .old], context: nil)
        
        // Observe metadata changes
        playerItem.addObserver(self, forKeyPath: "timedMetadata", options: [.new], context: nil)
        
        // Reset track info when switching stations
        currentTrack = TrackInfo(title: "Title", artist: "Artist")
        currentArtwork = nil
        cachedStationLogo = nil

        // Preload station logo for lock screen (non-blocking)
        FaviconFetcher.shared.loadLogo(for: station) { [weak self] image in
            self?.cachedStationLogo = image
            self?.updateNowPlayingInfo()
        }

        updateNowPlayingInfo()
    }
    
    override func observeValue(forKeyPath keyPath: String?, of object: Any?, change: [NSKeyValueChangeKey : Any]?, context: UnsafeMutableRawPointer?) {
        if keyPath == "timeControlStatus" {
            DispatchQueue.main.async {
                if let player = self.player {
                    switch player.timeControlStatus {
                    case .playing:
                        self.isPlaying = true
                        self.isLoading = false
                    case .paused:
                        self.isPlaying = false
                        self.isLoading = false
                    case .waitingToPlayAtSpecifiedRate:
                        self.isLoading = true
                    @unknown default:
                        break
                    }
                }
            }
        } else if keyPath == "timedMetadata" {
            // Handle metadata updates
            if let playerItem = object as? AVPlayerItem {
                DispatchQueue.main.async {
                    self.parseMetadata(from: playerItem)
                }
            }
        }
    }
    
    private func parseMetadata(from playerItem: AVPlayerItem) {
        // Get metadata from timed metadata
        guard let metadata = playerItem.timedMetadata else { return }
        
        var newTitle: String?
        var newArtist: String?
        
        for item in metadata {
            guard let commonKey = item.commonKey?.rawValue else { continue }
            
            if let value = item.value as? String {
                switch commonKey {
                case "title":
                    newTitle = value
                case "artist":
                    newArtist = value
                default:
                    break
                }
            }
        }
        
        // If we got at least one value from standard keys, use them.
        // Otherwise fall back to ICY metadata parsing.
        if newTitle != nil || newArtist != nil {
            updateTrackInfo(title: newTitle, artist: newArtist)
        } else {
            parseICYMetadata(from: metadata)
        }
    }
    
    private func parseICYMetadata(from metadata: [AVMetadataItem]) {
        for item in metadata {
            if let key = item.commonKey?.rawValue, key == "title" || key == "name" {
                if let value = item.value as? String {
                    // ICY metadata often comes as "Artist - Title"
                    let components = value.split(separator: "-", maxSplits: 1).map { $0.trimmingCharacters(in: .whitespaces) }
                    
                    if components.count == 2 {
                        updateTrackInfo(title: components[1], artist: components[0])
                    } else if !value.isEmpty {
                        updateTrackInfo(title: value, artist: nil)
                    }
                    return
                }
            }
        }
    }
    
    private func updateTrackInfo(title: String?, artist: String?) {
        // Filter out useless metadata
        let filteredTitle = filterMetadata(title)
        let filteredArtist = filterMetadata(artist)
        
        let newTitle = filteredTitle ?? "Title"
        let newArtist = filteredArtist ?? "Artist"
        
        // Only update if changed
        if currentTrack.title != newTitle || currentTrack.artist != newArtist {
            currentTrack = TrackInfo(title: newTitle, artist: newArtist)
            
            // Fetch album artwork if we have real metadata
            if newTitle != "Title" && newArtist != "Artist" {
                // First update with no artwork
                currentArtwork = nil
                updateNowPlayingInfo()
                
                // Then fetch and update again when artwork arrives
                AlbumArtFetcher.shared.fetchArtwork(artist: newArtist, title: newTitle) { [weak self] image in
                    self?.currentArtwork = image
                    // Force update of lock screen with new artwork
                    self?.updateNowPlayingInfo()
                }
            } else {
                // Reset to station logo
                currentArtwork = nil
                updateNowPlayingInfo()
            }
        }
    }
    
    private func filterMetadata(_ value: String?) -> String? {
        guard let value = value else { return nil }
        
        let trimmed = value.trimmingCharacters(in: .whitespacesAndNewlines)
        
        // Empty or very short
        if trimmed.isEmpty || trimmed.count < 2 {
            return nil
        }
        
        // Convert to lowercase for comparison
        let lower = trimmed.lowercased()
        
        // Filter out common useless values
        let uselessValues = [
            "unknown",
            "unknow",
            "n/a",
            "na",
            "-",
            "..."
        ]
        
        if uselessValues.contains(lower) {
            return nil
        }
        
        // Filter out station announcements
        if lower.contains("on air") ||
           (lower.contains("fm") && lower.contains("96.6")) ||
           (lower.contains("fm") && lower.contains("100.7")) ||
           (lower.contains("fm") && lower.contains("105")) ||
           (lower.contains("rgl") && lower.contains("fm")) ||
           (lower.contains("eldoradio") && lower.contains("fm")) ||
           (lower.contains("radio") && lower.contains("fm")) {
            return nil
        }
        
        return trimmed
    }
    
    func togglePlayback() {
        if isPlaying {
            stop()
        } else {
            play()
        }
    }
    
    func play() {
        // Configure audio session when actually needed
        AudioSessionManager.shared.configureAudioSession()
        
        isLoading = true
        isPlaying = true
        player?.play()
        updateNowPlayingInfo()
    }
    
    func stop() {
        player?.pause()
        isPlaying = false
        isLoading = false
        updateNowPlayingInfo()
    }
    
    private func setupRemoteControls() {
        let commandCenter = MPRemoteCommandCenter.shared()
        
        // Play command
        commandCenter.playCommand.isEnabled = true
        commandCenter.playCommand.addTarget { [weak self] _ in
            self?.play()
            return .success
        }
        
        // Pause command
        commandCenter.pauseCommand.isEnabled = true
        commandCenter.pauseCommand.addTarget { [weak self] _ in
            self?.stop()
            return .success
        }
        
        // Stop command
        commandCenter.stopCommand.isEnabled = true
        commandCenter.stopCommand.addTarget { [weak self] _ in
            self?.stop()
            return .success
        }
        
        // Disable other commands not relevant for radio
        commandCenter.nextTrackCommand.isEnabled = false
        commandCenter.previousTrackCommand.isEnabled = false
        commandCenter.changePlaybackPositionCommand.isEnabled = false
    }
    
    private func updateNowPlayingInfo() {
        var nowPlayingInfo = [String: Any]()
        
        nowPlayingInfo[MPMediaItemPropertyTitle] = currentTrack.title
        nowPlayingInfo[MPMediaItemPropertyArtist] = currentTrack.artist
        nowPlayingInfo[MPMediaItemPropertyAlbumTitle] = currentStation.name
        
        // Set custom artwork
        if let artwork = createArtwork() {
            nowPlayingInfo[MPMediaItemPropertyArtwork] = artwork
        }
        
        // For live streams, set playback rate
        nowPlayingInfo[MPNowPlayingInfoPropertyPlaybackRate] = isPlaying ? 1.0 : 0.0
        nowPlayingInfo[MPNowPlayingInfoPropertyIsLiveStream] = true
        
        MPNowPlayingInfoCenter.default().nowPlayingInfo = nowPlayingInfo
    }
    
    private func createArtwork() -> MPMediaItemArtwork? {
        // Use fetched album artwork if available
        if let albumArt = currentArtwork {
            let size = albumArt.size
            return MPMediaItemArtwork(boundsSize: size) { _ in albumArt }
        }

        // Use cached station logo (preloaded in loadStation)
        if let logo = cachedStationLogo {
            let size = logo.size
            return MPMediaItemArtwork(boundsSize: size) { _ in logo }
        }

        // Fallback: Create simple artwork with Luxembourg flag colors
        let size = CGSize(width: 600, height: 600)
        let renderer = UIGraphicsImageRenderer(size: size)
        
        let image = renderer.image { context in
            // Luxembourg flag colors: Red, White, Blue
            let red = UIColor(red: 0.93, green: 0.16, blue: 0.22, alpha: 1.0)
            let white = UIColor.white
            let blue = UIColor(red: 0.0, green: 0.62, blue: 0.89, alpha: 1.0)
            
            // Draw horizontal stripes
            red.setFill()
            context.fill(CGRect(x: 0, y: 0, width: 600, height: 200))
            
            white.setFill()
            context.fill(CGRect(x: 0, y: 200, width: 600, height: 200))
            
            blue.setFill()
            context.fill(CGRect(x: 0, y: 400, width: 600, height: 200))
        }
        
        return MPMediaItemArtwork(boundsSize: size) { _ in image }
    }
    
    private func setupNotifications() {
        // Handle audio session interruptions (phone calls, etc.)
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleInterruption),
            name: AVAudioSession.interruptionNotification,
            object: AVAudioSession.sharedInstance()
        )
        
        // Handle route changes (headphones unplugged, etc.)
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleRouteChange),
            name: AVAudioSession.routeChangeNotification,
            object: AVAudioSession.sharedInstance()
        )
    }
    
    @objc private func handleInterruption(notification: Notification) {
        guard let userInfo = notification.userInfo,
              let typeValue = userInfo[AVAudioSessionInterruptionTypeKey] as? UInt,
              let type = AVAudioSession.InterruptionType(rawValue: typeValue) else {
            return
        }
        
        switch type {
        case .began:
            stop()
        case .ended:
            guard let optionsValue = userInfo[AVAudioSessionInterruptionOptionKey] as? UInt else { return }
            let options = AVAudioSession.InterruptionOptions(rawValue: optionsValue)
            if options.contains(.shouldResume) {
                play()
            }
        @unknown default:
            break
        }
    }
    
    @objc private func handleRouteChange(notification: Notification) {
        guard let userInfo = notification.userInfo,
              let reasonValue = userInfo[AVAudioSessionRouteChangeReasonKey] as? UInt,
              let reason = AVAudioSession.RouteChangeReason(rawValue: reasonValue) else {
            return
        }
        
        switch reason {
        case .oldDeviceUnavailable:
            stop()
        default:
            break
        }
    }
    
    deinit {
        if let player = player, let currentItem = player.currentItem {
            player.removeObserver(self, forKeyPath: "timeControlStatus")
            currentItem.removeObserver(self, forKeyPath: "timedMetadata")
        }
        NotificationCenter.default.removeObserver(self)
    }
}

struct TrackInfo {
    var title: String
    var artist: String
}
