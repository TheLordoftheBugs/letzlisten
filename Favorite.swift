//
//  Favorite.swift
//  Radio
//
//  Model for storing favorite songs and moments
//

import Foundation

struct Favorite: Identifiable, Codable {
    let id: UUID
    let timestamp: Date
    let title: String
    let artist: String
    let stationId: String      // ID de la radio
    let stationName: String    // Nom de la radio (pour affichage)
    
    init(id: UUID = UUID(), timestamp: Date = Date(), title: String, artist: String, stationId: String, stationName: String) {
        self.id = id
        self.timestamp = timestamp
        self.title = title
        self.artist = artist
        self.stationId = stationId
        self.stationName = stationName
    }
    
    var formattedDate: String {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        return formatter.string(from: timestamp)
    }
}
