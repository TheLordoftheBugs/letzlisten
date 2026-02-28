//
//  RadioStation.swift
//  Radio Lëtzebuerg
//
//  Model for radio stations - loads dynamically from JSON
//

import Foundation

struct RadioStation: Identifiable, Codable {
    let id: String
    let name: String
    let streamURL: String
    let logoImageName: String
    let websiteURL: String?
    let isEnabled: Bool?  // NEW: Si false, la radio est désactivée
    
    // Helper computed property
    var enabled: Bool {
        return isEnabled ?? true  // Par défaut: activée
    }
    
    // Computed property to get stations from loader
    static var stations: [RadioStation] {
        return RadioStationLoader.shared.stations
    }
}
