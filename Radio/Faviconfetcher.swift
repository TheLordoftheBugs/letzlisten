//
//  FaviconFetcher.swift
//  Radio Lëtzebuerg
//
//  Downloads and caches favicons from website URLs
//

import Foundation
import UIKit

class FaviconFetcher {
    static let shared = FaviconFetcher()
    
    private let fileManager = FileManager.default
    private var cacheDirectory: URL {
        fileManager.urls(for: .cachesDirectory, in: .userDomainMask)[0].appendingPathComponent("Favicons")
    }
    
    private init() {
        createCacheDirectoryIfNeeded()
    }
    
    // MARK: - Main Loading Function
    
    /// Load logo: try local Assets first, then favicon from website (cached)
    func loadLogo(for station: RadioStation, completion: @escaping (UIImage?) -> Void) {
        // Priority 1: Try local Assets
        if let localImage = UIImage(named: station.logoImageName) {
            completion(localImage)
            return
        }
        
        // Priority 2: Try disk cache
        if let cachedImage = loadFromCache(stationId: station.id) {
            completion(cachedImage)
            return
        }
        
        // Priority 3: Download favicon from website
        guard let websiteURL = station.websiteURL else {
            // No website URL - use placeholder
            completion(createPlaceholder(for: station))
            return
        }
        
        downloadFavicon(from: websiteURL, stationId: station.id) { [weak self] image in
            if let image = image {
                // Save to cache for future use
                self?.saveToCache(image: image, stationId: station.id)
                completion(image)
            } else {
                // Favicon download failed - use placeholder
                completion(self?.createPlaceholder(for: station))
            }
        }
    }
    
    // MARK: - Favicon Download
    
    private func downloadFavicon(from websiteURLString: String, stationId: String, completion: @escaping (UIImage?) -> Void) {
        // Check if it's a Facebook page
        if websiteURLString.contains("facebook.com") {
            downloadFacebookProfilePicture(from: websiteURLString, completion: completion)
            return
        }
        
        // Try multiple favicon URLs in order of preference
        let faviconURLs = generateFaviconURLs(from: websiteURLString)
        
        tryDownloadFavicons(urls: faviconURLs, completion: completion)
    }
    
    // MARK: - Facebook Profile Picture
    
    private func downloadFacebookProfilePicture(from facebookURL: String, completion: @escaping (UIImage?) -> Void) {
        // Extract Facebook page ID or username from URL
        // Example: https://www.facebook.com/RadioName or https://facebook.com/profile.php?id=123456
        
        var pageIdentifier: String?
        
        if let url = URL(string: facebookURL) {
            let pathComponents = url.pathComponents.filter { $0 != "/" }
            
            // Check for username format: facebook.com/RadioName
            if let firstComponent = pathComponents.first, !firstComponent.isEmpty {
                pageIdentifier = firstComponent
            }
            
            // Check for ID format: facebook.com/profile.php?id=123456
            if let query = url.query, query.contains("id=") {
                let components = query.components(separatedBy: "&")
                for component in components {
                    if component.hasPrefix("id=") {
                        pageIdentifier = component.replacingOccurrences(of: "id=", with: "")
                        break
                    }
                }
            }
        }
        
        guard let identifier = pageIdentifier else {
            // Fallback to regular favicon if we can't extract identifier
            let faviconURLs = generateFaviconURLs(from: facebookURL)
            tryDownloadFavicons(urls: faviconURLs, completion: completion)
            return
        }
        
        // Facebook Graph API for profile picture (no auth needed for public pages)
        // Try multiple sizes for best quality
        let fbImageURLs = [
            "https://graph.facebook.com/\(identifier)/picture?type=large&width=500&height=500",  // Large (500x500)
            "https://graph.facebook.com/\(identifier)/picture?type=normal&width=200&height=200", // Normal (200x200)
            "https://graph.facebook.com/\(identifier)/picture?type=square"  // Square (fallback)
        ]
        
        tryDownloadFavicons(urls: fbImageURLs, completion: completion)
    }
    
    private func generateFaviconURLs(from websiteURL: String) -> [String] {
        guard let url = URL(string: websiteURL),
              let host = url.host else {
            return []
        }
        
        let scheme = url.scheme ?? "https"
        let baseURL = "\(scheme)://\(host)"
        
        // Try multiple favicon locations - BEST QUALITY FIRST
        return [
            // "\(baseURL)/apple-touch-icon.png",  // 1. Apple touch icon (usually 180x180 or higher - BEST QUALITY)
            "https://www.google.com/s2/favicons?domain=\(host)&sz=256"  // 2. Google favicon service high-res (256x256)
            // "\(baseURL)/favicon.png",           // 3. PNG favicon (usually better than .ico)
            // "\(baseURL)/favicon-192x192.png",   // 4. Android chrome icon (192x192)
            // "\(baseURL)/favicon-96x96.png",     // 5. High-res favicon (96x96)
            // "\(baseURL)/favicon.ico"            // 6. Standard favicon (often low quality, last resort)
        ]
    }
    
    private func tryDownloadFavicons(urls: [String], currentIndex: Int = 0, completion: @escaping (UIImage?) -> Void) {
        guard currentIndex < urls.count else {
            // All attempts failed
            completion(nil)
            return
        }
        
        let urlString = urls[currentIndex]
        guard let url = URL(string: urlString) else {
            // Try next URL
            tryDownloadFavicons(urls: urls, currentIndex: currentIndex + 1, completion: completion)
            return
        }
        
        URLSession.shared.dataTask(with: url) { [weak self] data, response, error in
            if let data = data,
               let image = UIImage(data: data),
               error == nil,
               image.size.width > 10 { // Ensure it's not a tiny error image
                DispatchQueue.main.async {
                    completion(image)
                }
            } else {
                // This URL failed, try next
                self?.tryDownloadFavicons(urls: urls, currentIndex: currentIndex + 1, completion: completion)
            }
        }.resume()
    }
    
    // MARK: - Cache Management
    
    private func createCacheDirectoryIfNeeded() {
        if !fileManager.fileExists(atPath: cacheDirectory.path) {
            try? fileManager.createDirectory(at: cacheDirectory, withIntermediateDirectories: true)
        }
    }
    
    private func loadFromCache(stationId: String) -> UIImage? {
        let fileURL = cacheDirectory.appendingPathComponent("\(stationId).png")
        guard let data = try? Data(contentsOf: fileURL),
              let image = UIImage(data: data) else {
            return nil
        }
        return image
    }
    
    private func saveToCache(image: UIImage, stationId: String) {
        guard let data = image.pngData() else { return }
        let fileURL = cacheDirectory.appendingPathComponent("\(stationId).png")
        try? data.write(to: fileURL)
    }
    
    // MARK: - Placeholder Generation
    
    private func createPlaceholder(for station: RadioStation) -> UIImage? {
        // Try Luxembourg flag first
        if let flagImage = UIImage(named: "LuxembourgFlag") {
            return flagImage
        }
        
        // Generate text-based placeholder with station initials
        let size = CGSize(width: 512, height: 512)
        let renderer = UIGraphicsImageRenderer(size: size)
        
        return renderer.image { context in
            // Background gradient (Luxembourg colors)
            let gradient = CGGradient(
                colorsSpace: CGColorSpaceCreateDeviceRGB(),
                colors: [
                    UIColor(red: 0.0, green: 0.62, blue: 0.89, alpha: 1.0).cgColor,  // Blue
                    UIColor(red: 0.93, green: 0.16, blue: 0.22, alpha: 1.0).cgColor   // Red
                ] as CFArray,
                locations: [0.0, 1.0]
            )
            
            if let gradient = gradient {
                context.cgContext.drawLinearGradient(
                    gradient,
                    start: CGPoint(x: 0, y: 0),
                    end: CGPoint(x: size.width, y: size.height),
                    options: []
                )
            }
            
            // Get station initials
            let initials = getInitials(from: station.name)
            
            // Draw initials
            let paragraphStyle = NSMutableParagraphStyle()
            paragraphStyle.alignment = .center
            
            let attributes: [NSAttributedString.Key: Any] = [
                .font: UIFont.systemFont(ofSize: 180, weight: .bold),
                .foregroundColor: UIColor.white,
                .paragraphStyle: paragraphStyle
            ]
            
            let textRect = CGRect(
                x: 0,
                y: (size.height - 200) / 2,
                width: size.width,
                height: 200
            )
            
            initials.draw(in: textRect, withAttributes: attributes)
        }
    }
    
    private func getInitials(from name: String) -> String {
        let words = name.components(separatedBy: " ").filter { !$0.isEmpty }
        
        if words.count >= 2 {
            // Take first letter of first two words
            let first = String(words[0].prefix(1))
            let second = String(words[1].prefix(1))
            return (first + second).uppercased()
        } else if let firstWord = words.first {
            // Take first two letters of single word
            return String(firstWord.prefix(2)).uppercased()
        }
        
        return "📻"
    }
    
    func clearCache() {
        try? fileManager.removeItem(at: cacheDirectory)
        createCacheDirectoryIfNeeded()
    }
}
