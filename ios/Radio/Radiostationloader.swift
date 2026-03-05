//
//  RadioStationLoader.swift
//  Radio Lëtzebuerg
//
//  Loads radio stations from the local bundled JSON file
//

import Foundation

class RadioStationLoader: ObservableObject {
    static let shared = RadioStationLoader()

    @Published var stations: [RadioStation] = []

    private init() {
        loadFromBundle()
    }

    private func loadFromBundle() {
        guard let url = Bundle.main.url(forResource: "stations", withExtension: "json"),
              let data = try? Data(contentsOf: url),
              let config = parseJSON(data) else {
            return
        }
        stations = config.stations.sorted { $0.name < $1.name }
    }

    private func parseJSON(_ data: Data) -> StationConfig? {
        return try? JSONDecoder().decode(StationConfig.self, from: data)
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
