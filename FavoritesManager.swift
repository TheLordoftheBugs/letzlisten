//
//  FavoritesManager.swift
//  Radio
//
//  Manages saving and loading favorite songs
//

import Foundation
import Combine

class FavoritesManager: ObservableObject {
    @Published var favorites: [Favorite] = []
    
    private let favoritesKey = "RadioGuttLaunFavorites"
    
    init() {
        loadFavorites()
    }
    
    // Add a new favorite (prevent duplicates: same song regardless of station)
    func addFavorite(title: String, artist: String, stationId: String, stationName: String) {
        // Check if already exists (same title + artist, ignore station)
        let alreadyExists = favorites.contains { 
            $0.title == title && $0.artist == artist
        }
        
        if !alreadyExists {
            let favorite = Favorite(title: title, artist: artist, stationId: stationId, stationName: stationName)
            favorites.insert(favorite, at: 0) // Add to beginning
            saveFavorites()
        }
    }
    
    // Remove ALL entries of this song (regardless of station)
    func removeFavorite(title: String, artist: String) {
        favorites.removeAll { $0.title == title && $0.artist == artist }
        saveFavorites()
    }
    
    // Remove a favorite by ID
    func removeFavorite(_ favorite: Favorite) {
        favorites.removeAll { $0.id == favorite.id }
        saveFavorites()
    }
    
    // Remove favorite at indices
    func removeFavorite(at indexSet: IndexSet) {
        for index in indexSet.sorted(by: >) {
            if favorites.indices.contains(index) {
                favorites.remove(at: index)
            }
        }
        saveFavorites()
    }
    
    // Check if track is favorited (ignore station)
    func isFavorited(title: String, artist: String) -> Bool {
        favorites.contains { 
            $0.title == title && $0.artist == artist
        }
    }
    
    // Save favorites to UserDefaults
    private func saveFavorites() {
        if let encoded = try? JSONEncoder().encode(favorites) {
            UserDefaults.standard.set(encoded, forKey: favoritesKey)
        }
    }
    
    // Load favorites from UserDefaults
    private func loadFavorites() {
        if let data = UserDefaults.standard.data(forKey: favoritesKey),
           let decoded = try? JSONDecoder().decode([Favorite].self, from: data) {
            favorites = decoded
        }
    }
    
    // Clear all favorites
    func clearAll() {
        favorites.removeAll()
        saveFavorites()
    }
}
