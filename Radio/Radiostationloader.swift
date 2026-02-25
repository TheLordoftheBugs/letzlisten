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
    /// Set to the fetch date when remote stations have been loaded successfully.
    @Published var remoteLoadedAt: Date? = nil
    
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

        // Load from bundle as base fallback (may be outdated)
        loadFromBundle()

        // Override with local cache if available (saved from previous remote fetch)
        // This ensures we have up-to-date stations on launch before network completes
        loadFromCache()

        print("🎯 Ready with \(stations.count) stations (v\(currentVersion))")
    }

    // MARK: - Main Loading Function

    func loadStations() {
        loadFromRemote { success in
            if !success {
                print("⚠️ Remote unavailable, keeping current stations")
            }
        }
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

                guard remoteVersion != currentVersion else {
                    // Same version — no need to replace stations in memory.
                    // Seed the cache if it doesn't exist yet (e.g. first install).
                    let cacheExists = FileManager.default.fileExists(
                        atPath: self?.cacheFileURL.path ?? "")
                    if !cacheExists { self?.saveToCache(data) }
                    print("✅ Remote v\(remoteVersion) matches current, nothing to update")
                    self?.remoteLoadedAt = Date()
                    completion(true)
                    return
                }

                // New version — update stations
                print("🔄 Updating to v\(remoteVersion) (was v\(currentVersion))...")
                self?.stations = config.stations.sorted { $0.name < $1.name }
                self?.currentVersion = remoteVersion
                self?.lastUpdateDate = Date()
                self?.remoteLoadedAt = Date()
                self?.saveToCache(data)
                print("✅ Updated to v\(remoteVersion): \(config.stations.count) stations")
                print("📅 Last updated: \(config.lastUpdated)")
                completion(true)
            }
        }.resume()
    }
    
    // MARK: - Cache Loading
    
    private func loadFromCache() {
        let cachedVersion = UserDefaults.standard.string(forKey: versionKey) ?? "0.0"
        guard cachedVersion != currentVersion else {
            print("⚡️ Cache v\(cachedVersion) matches bundle, skipping")
            return
        }
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
