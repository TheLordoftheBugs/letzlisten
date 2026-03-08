//
//  RadioPlayer.swift
//  Radio Lëtzebuerg
//
//  Multi-station audio player with background support
//

import Foundation
import AVFoundation
import MediaPlayer
import MediaToolbox
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

    // ShazamKit — used as fallback when no ICY metadata is available.
    // Stored as Any? to avoid @available constraints on the property itself.
    private var shazamMatcher: Any?
    private var shazamDebounceTimer: Timer?
    
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

        // Register defaults — continuousPlayback is ON by default
        UserDefaults.standard.register(defaults: ["continuousPlayback": true])

        // Always persist so the station is saved even on first launch
        UserDefaults.standard.set(currentStation.id, forKey: lastStationKey)

        // Quiet log — remote will confirm below
        print("🎵 Using station: \(currentStation.name)")

        setupPlayer()
        setupRemoteControls()
        setupNotifications()
    }
    
    private func setupPlayer() {
        loadStation(currentStation)
    }
    
    func switchStation(_ station: RadioStation) {
        let wasPlaying = isPlaying
        stop()
        currentStation = station
        UserDefaults.standard.set(station.id, forKey: lastStationKey)
        loadStation(station)
        if wasPlaying && UserDefaults.standard.bool(forKey: "continuousPlayback") {
            play()
        }
    }
    
    private func loadStation(_ station: RadioStation) {
        // Stop any pending Shazam recognition from the previous station
        stopShazam()
        shazamMatcher = nil

        // Remove observer from old player
        if let player = player, let currentItem = player.currentItem {
            player.removeObserver(self, forKeyPath: "timeControlStatus")
            currentItem.removeObserver(self, forKeyPath: "timedMetadata")
        }

        // Create new player with station URL
        // Send Icy-MetaData: 1 so Shoutcast/Icecast servers include ICY stream metadata.
        // This also prevents some servers from returning a bare "ICY 200 OK" response
        // (which iOS rejects) by signalling that the client speaks the ICY protocol.
        guard let url = URL(string: station.streamURL) else { return }
        let asset = AVURLAsset(url: url, options: [
            "AVURLAssetHTTPHeaderFieldsKey": ["Icy-MetaData": "1"]
        ])
        let playerItem = AVPlayerItem(asset: asset)
        player = AVPlayer(playerItem: playerItem)

        // Observe new player status
        player?.addObserver(self, forKeyPath: "timeControlStatus", options: [.new, .old], context: nil)

        // Observe metadata changes
        playerItem.addObserver(self, forKeyPath: "timedMetadata", options: [.new], context: nil)

        // Install audio tap for ShazamKit (iOS 15+)
        if #available(iOS 15, *) {
            setupShazamTap(for: playerItem)
        }

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

    // MARK: - ShazamKit tap setup

    @available(iOS 15.0, *)
    private func setupShazamTap(for playerItem: AVPlayerItem) {
        let matcher = ShazamMatcher()
        matcher.onMatch = { [weak self] title, artist in
            self?.updateTrackInfo(title: title, artist: artist)
        }
        shazamMatcher = matcher

        // Load audio tracks asynchronously (they may not be ready yet at setup time)
        playerItem.asset.loadValuesAsynchronously(forKeys: ["tracks"]) { [weak self, weak playerItem] in
            guard self != nil, let playerItem = playerItem,
                  let audioTrack = playerItem.asset.tracks(withMediaType: .audio).first else { return }

            let tapContext = ShazamTapContext(matcher: matcher)
            let retainedCtx = Unmanaged.passRetained(tapContext)

            // C-compatible callbacks — no captures allowed; context is passed via clientInfo.
            let tapInit: MTAudioProcessingTapInitCallback = { tap, clientInfo, tapStorageOut in
                tapStorageOut.pointee = clientInfo
            }
            let tapFinalize: MTAudioProcessingTapFinalizeCallback = { tap in
                let storage = MTAudioProcessingTapGetStorage(tap)
                Unmanaged<ShazamTapContext>.fromOpaque(storage).release()
            }
            let tapPrepare: MTAudioProcessingTapPrepareCallback = { tap, _, processingFormat in
                let storage = MTAudioProcessingTapGetStorage(tap)
                let ctx = Unmanaged<ShazamTapContext>.fromOpaque(storage).takeUnretainedValue()
                ctx.format = AVAudioFormat(streamDescription: processingFormat)
            }
            let tapProcess: MTAudioProcessingTapProcessCallback = { tap, numberFrames, _, bufferListInOut, numberFramesOut, flagsOut in
                MTAudioProcessingTapGetSourceAudio(tap, numberFrames, bufferListInOut, flagsOut, nil, numberFramesOut)
                let storage = MTAudioProcessingTapGetStorage(tap)
                let ctx = Unmanaged<ShazamTapContext>.fromOpaque(storage).takeUnretainedValue()
                guard let format = ctx.format,
                      let matcher = ctx.matcher,
                      matcher.isRunning else { return }
                let frameCount = AVAudioFrameCount(numberFramesOut.pointee)
                guard frameCount > 0,
                      let pcmBuffer = AVAudioPCMBuffer(pcmFormat: format, frameCapacity: frameCount) else { return }
                pcmBuffer.frameLength = frameCount
                let srcPtr = UnsafeMutableAudioBufferListPointer(bufferListInOut)
                let dstPtr = UnsafeMutableAudioBufferListPointer(pcmBuffer.mutableAudioBufferList)
                for i in 0..<min(srcPtr.count, dstPtr.count) {
                    if let src = srcPtr[i].mData, let dst = dstPtr[i].mData {
                        memcpy(dst, src, Int(srcPtr[i].mDataByteSize))
                    }
                }
                matcher.match(buffer: pcmBuffer)
            }
            var callbacks = MTAudioProcessingTapCallbacks(
                version: kMTAudioProcessingTapCallbacksVersion_0,
                clientInfo: retainedCtx.toOpaque(),
                init: tapInit,
                finalize: tapFinalize,
                prepare: tapPrepare,
                unprepare: nil,
                process: tapProcess
            )

            var tap: MTAudioProcessingTap?
            let status = MTAudioProcessingTapCreate(
                kCFAllocatorDefault, &callbacks,
                kMTAudioProcessingTapCreationFlag_PreEffects, &tap
            )
            guard status == noErr, let tap = tap else { return }

            let inputParams = AVMutableAudioMixInputParameters(track: audioTrack)
            inputParams.audioTapProcessor = tap

            let audioMix = AVMutableAudioMix()
            audioMix.inputParameters = [inputParams]

            DispatchQueue.main.async {
                playerItem.audioMix = audioMix
            }
        }
    }

    // MARK: - Shazam start / stop helpers

    /// Starts ShazamKit recognition after a short debounce, only when needed.
    private func startShazamIfNeeded() {
        guard #available(iOS 15, *) else { return }
        guard isPlaying, currentTrack.isUnknown else { return }
        guard (shazamMatcher as? ShazamMatcher)?.isRunning != true else { return }

        shazamDebounceTimer?.invalidate()
        // Wait 5 s before starting: short gaps between tracks shouldn't trigger Shazam.
        shazamDebounceTimer = Timer.scheduledTimer(withTimeInterval: 5.0, repeats: false) { [weak self] _ in
            guard let self = self, self.isPlaying, self.currentTrack.isUnknown else { return }
            (self.shazamMatcher as? ShazamMatcher)?.start()
        }
    }

    /// Cancels any pending or active Shazam recognition.
    private func stopShazam() {
        shazamDebounceTimer?.invalidate()
        shazamDebounceTimer = nil
        if #available(iOS 15, *) {
            (shazamMatcher as? ShazamMatcher)?.stop()
        }
    }
    
    override func observeValue(forKeyPath keyPath: String?, of object: Any?, change: [NSKeyValueChangeKey : Any]?, context: UnsafeMutableRawPointer?) {
        if keyPath == "timeControlStatus" {
            DispatchQueue.main.async {
                if let player = self.player {
                    switch player.timeControlStatus {
                    case .playing:
                        self.isPlaying = true
                        self.isLoading = false
                        // Player is now actually streaming — start Shazam if needed.
                        self.startShazamIfNeeded()
                    case .paused:
                        self.isPlaying = false
                        self.isLoading = false
                        self.stopShazam()
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
        
        if let artist = newArtist {
            // Stream provides an explicit separate artist field — use directly.
            updateTrackInfo(title: newTitle, artist: artist)
        } else if let rawTitle = newTitle {
            // Title only: many ICY streams pack "Artist - Title" into the title key.
            // Split on " - " (with spaces) to avoid false splits on hyphenated words.
            let parts = rawTitle.components(separatedBy: " - ")
            if parts.count >= 2 {
                let artist = parts[0].trimmingCharacters(in: .whitespaces)
                let title  = parts[1...].joined(separator: " - ").trimmingCharacters(in: .whitespaces)
                updateTrackInfo(title: title, artist: artist)
            } else {
                updateTrackInfo(title: rawTitle, artist: nil)
            }
        } else {
            // No standard keys — fall back to raw ICY metadata parsing.
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

            if newTitle != "Title" || newArtist != "Artist" {
                // Valid metadata (from stream or Shazam) — stop any Shazam recognition.
                stopShazam()

                // First update with no artwork
                currentArtwork = nil
                updateNowPlayingInfo()

                // Then fetch and update again when artwork arrives
                AlbumArtFetcher.shared.fetchArtwork(artist: newArtist, title: newTitle) { [weak self] image in
                    self?.currentArtwork = image
                    self?.updateNowPlayingInfo()
                }
            } else {
                // No metadata — reset artwork and try Shazam if playing.
                currentArtwork = nil
                updateNowPlayingInfo()
                startShazamIfNeeded()
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

        // Resume Shazam if we still have no metadata
        startShazamIfNeeded()
    }

    func stop() {
        player?.pause()
        isPlaying = false
        isLoading = false
        stopShazam()
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

    var isUnknown: Bool { title == "Title" && artist == "Artist" }
}
