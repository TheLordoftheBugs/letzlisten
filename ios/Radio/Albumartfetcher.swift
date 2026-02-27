//
//  AlbumArtFetcher.swift
//  Radio Lëtzebuerg
//
//  Fetches album artwork from iTunes API
//

import Foundation
import UIKit

class AlbumArtFetcher {
    static let shared = AlbumArtFetcher()
    
    private init() {}
    
    // Cache for artwork to avoid repeated API calls
    private var cache: [String: UIImage] = [:]
    
    func fetchArtwork(artist: String, title: String, completion: @escaping (UIImage?) -> Void) {
        // Check if values are defaults - don't search
        if artist == "Artist" || title == "Title" {
            completion(nil)
            return
        }
        
        // Create cache key
        let cacheKey = "\(artist)-\(title)".lowercased()
        
        // Check cache first
        if let cachedImage = cache[cacheKey] {
            completion(cachedImage)
            return
        }
        
        // Build search query
        let searchTerm = "\(artist) \(title)"
            .addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        
        let urlString = "https://itunes.apple.com/search?term=\(searchTerm)&media=music&entity=song&limit=1"
        
        guard let url = URL(string: urlString) else {
            completion(nil)
            return
        }
        
        // Fetch from iTunes API
        URLSession.shared.dataTask(with: url) { [weak self] data, response, error in
            guard let data = data, error == nil else {
                DispatchQueue.main.async {
                    completion(nil)
                }
                return
            }
            
            do {
                let result = try JSONDecoder().decode(iTunesResponse.self, from: data)
                
                // Get artwork URL from first result
                if let firstResult = result.results.first,
                   let artworkUrlString = firstResult.artworkUrl100,
                   // Upgrade to higher resolution (600x600)
                   let highResUrl = artworkUrlString.replacingOccurrences(of: "100x100", with: "600x600") as String?,
                   let artworkUrl = URL(string: highResUrl) {
                    
                    // Download the image
                    self?.downloadImage(from: artworkUrl, cacheKey: cacheKey, completion: completion)
                } else {
                    DispatchQueue.main.async {
                        completion(nil)
                    }
                }
            } catch {
                DispatchQueue.main.async {
                    completion(nil)
                }
            }
        }.resume()
    }
    
    private func downloadImage(from url: URL, cacheKey: String, completion: @escaping (UIImage?) -> Void) {
        URLSession.shared.dataTask(with: url) { [weak self] data, response, error in
            guard let data = data,
                  let image = UIImage(data: data),
                  error == nil else {
                DispatchQueue.main.async {
                    completion(nil)
                }
                return
            }
            
            // Cache the image
            self?.cache[cacheKey] = image
            
            DispatchQueue.main.async {
                completion(image)
            }
        }.resume()
    }
    
    func clearCache() {
        cache.removeAll()
    }
}

// MARK: - iTunes API Response Models

struct iTunesResponse: Codable {
    let results: [iTunesTrack]
}

struct iTunesTrack: Codable {
    let artistName: String?
    let trackName: String?
    let artworkUrl100: String?
    let artworkUrl60: String?
}
