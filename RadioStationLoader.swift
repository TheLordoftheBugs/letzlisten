//
//  RadioStationLoader.swift
//  Radio Lëtzebuerg
//
//  Loads radio stations from JSON (local file + remote URL)
//

import Foundation
import Combine

class RadioStationLoader: ObservableObject {
    static let shared = RadioStationLoader()
    
    @Published var stations: [RadioStation] = []
    @Published var isLoading = false
    @Published var lastUpdateDate: Date?
    @Published var currentVersion: String = "0.0"
    
    // URL du fichier JSON distant (GitHub)
    private let remoteURL = "https://raw.githubusercontent.com/arnoflorentin/letzlisten/main/stations.json"
    
    // Cache local
    private let cacheFileName = "stations_cache.json"
    private let versionKey = "stations_version"
    private var cacheFileURL: URL {
        FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
            .appendingPathComponent(cacheFileName)
    }
    
    private init() {
        print("🚀 Initializing RadioStationLoader...")
        
        // ALWAYS try GitHub first - no cache, no bundle unless GitHub fails
        if loadFromRemoteSync() {
            print("🎯 Ready with \(stations.count) stations (v\(currentVersion)) from GitHub ✅")
        } else {
            // Only use local if GitHub completely failed
            print("⚠️ GitHub unavailable, using local fallback...")
            loadFromBundle()
            print("🎯 Ready with \(stations.count) stations (v\(currentVersion)) from bundle")
        }
    }
    
    // MARK: - Main Loading Function
    
    func loadStations() {
        // Manual refresh if needed
        loadFromRemote { _ in }
    }
    
    // MARK: - Synchronous Remote Loading (for init only)
    
    @discardableResult
    private func loadFromRemoteSync() -> Bool {
        // Add timestamp to bypass GitHub cache
        let timestamp = Int(Date().timeIntervalSince1970)
        let urlWithCacheBuster = "\(remoteURL)?t=\(timestamp)"
        
        guard let url = URL(string: urlWithCacheBuster) else {
            print("❌ Invalid remote URL")
            return false
        }
        
        print("🌐 Loading from GitHub: \(remoteURL)")
        
        var request = URLRequest(url: url)
        request.cachePolicy = .reloadIgnoringLocalAndRemoteCacheData
        request.timeoutInterval = 10
        
        // Synchronous request
        var data: Data?
        var response: URLResponse?
        var error: Error?
        
        let semaphore = DispatchSemaphore(value: 0)
        
        URLSession.shared.dataTask(with: request) { d, r, e in
            data = d
            response = r
            error = e
            semaphore.signal()
        }.resume()
        
        semaphore.wait()
        
        // Check for errors
        guard let data = data, error == nil else {
            print("❌ GitHub load failed: \(error?.localizedDescription ?? "Unknown")")
            return false
        }
        
        // Parse JSON
        guard let config = parseJSON(data) else {
            print("❌ Failed to parse JSON from GitHub")
            return false
        }
        
        // Filter only enabled stations
        let enabledStations = config.stations.filter { $0.enabled }
        
        if enabledStations.isEmpty {
            print("⚠️ No enabled stations in GitHub JSON")
            return false
        }
        
        // Update stations with only enabled ones
        self.stations = enabledStations.sorted { $0.name < $1.name }
        self.currentVersion = config.version
        self.lastUpdateDate = Date()
        
        print("✅ GitHub v\(config.version): \(stations.count) enabled stations loaded")
        
        return true
    }
    
    // MARK: - Remote Loading
    
    func loadFromRemote(completion: @escaping (Bool) -> Void) {
        // Add timestamp to bypass GitHub cache
        let timestamp = Int(Date().timeIntervalSince1970)
        let urlWithCacheBuster = "\(remoteURL)?t=\(timestamp)"
        
        guard let url = URL(string: urlWithCacheBuster) else {
            print("❌ Invalid remote URL: \(remoteURL)")
            DispatchQueue.main.async {
                completion(false)
            }
            return
        }
        
        print("🌐 Fetching stations from: \(remoteURL)")
        isLoading = true
        
        var request = URLRequest(url: url)
        request.cachePolicy = .reloadIgnoringLocalAndRemoteCacheData  // Force fresh download
        request.timeoutInterval = 10  // 10 second timeout
        
        URLSession.shared.dataTask(with: request) { [weak self] data, response, error in
            DispatchQueue.main.async {
                self?.isLoading = false
                
                guard let data = data, error == nil else {
                    print("❌ Failed to load remote stations: \(error?.localizedDescription ?? "Unknown error")")
                    completion(false)
                    return
                }
                
                // Parse JSON
                guard let config = self?.parseJSON(data) else {
                    print("❌ Failed to parse JSON from remote")
                    completion(false)
                    return
                }
                
                let remoteVersion = config.version
                let currentVersion = self?.currentVersion ?? "0.0"
                
                print("📊 Version comparison: Current=\(currentVersion), Remote=\(remoteVersion)")
                
                // ALWAYS update from remote (even if same version, content might differ)
                print("🔄 Loading remote version \(remoteVersion)...")
                
                self?.stations = config.stations.sorted { $0.name < $1.name }
                self?.currentVersion = remoteVersion
                self?.lastUpdateDate = Date()
                
                // Save to cache for offline use
                self?.saveToCache(data)
                
                if remoteVersion != currentVersion {
                    print("✅ Updated to v\(remoteVersion): \(config.stations.count) stations (previously had v\(currentVersion))")
                } else {
                    print("✅ Loaded v\(remoteVersion): \(config.stations.count) stations")
                }
                print("📅 Last updated: \(config.lastUpdated)")
                
                completion(true)
            }
        }.resume()
    }
    
    // MARK: - Cache Loading
    
    private func loadFromCache() {
        guard let data = try? Data(contentsOf: cacheFileURL),
              let config = parseJSON(data) else {
            print("⚠️ No cache available")
            return
        }
        
        stations = config.stations.sorted { $0.name < $1.name }
        currentVersion = config.version
        print("✅ Loaded \(stations.count) stations from cache (v\(currentVersion))")
    }
    
    private func saveToCache(_ data: Data) {
        try? data.write(to: cacheFileURL)
        
        // Also save version to UserDefaults for quick comparison
        if let config = parseJSON(data) {
            UserDefaults.standard.set(config.version, forKey: versionKey)
        }
        
        print("💾 Saved stations to cache")
    }
    
    // MARK: - Bundle Loading (Fallback)
    
    private func loadFromBundle() {
        guard let url = Bundle.main.url(forResource: "stations", withExtension: "json"),
              let data = try? Data(contentsOf: url),
              let config = parseJSON(data) else {
            print("❌ Failed to load bundled stations.json")
            return
        }
        
        stations = config.stations.sorted { $0.name < $1.name }
        currentVersion = config.version
        print("✅ Loaded \(stations.count) stations from bundle (v\(currentVersion))")
    }
    
    // MARK: - JSON Parsing
    
    private func parseJSON(_ data: Data) -> StationConfig? {
        let decoder = JSONDecoder()
        return try? decoder.decode(StationConfig.self, from: data)
    }
    
    // MARK: - Manual Refresh
    
    func refresh() {
        loadFromRemote { _ in }
    }
    
    // MARK: - Clear Cache
    
    func clearCache() {
        try? FileManager.default.removeItem(at: cacheFileURL)
        print("🗑️ Cache cleared")
    }
}

// MARK: - Models

struct StationConfig: Codable {
    let version: String
    let lastUpdated: String
    let stations: [RadioStation]
    
    enum CodingKeys: String, CodingKey {
        case version
        case lastUpdated = "last_updated"
        case stations
    }
}
