//
//  RadioStationLoader.swift
//  Radio Lëtzebuerg
//
//  Loads radio stations from the local bundled JSON file
//

import Foundation
import Combine

class RadioStationLoader: ObservableObject {
    static let shared = RadioStationLoader()

    @Published var stations: [RadioStation] = []
    @Published var stationsVersion: String = ""

    // Remote URL for secret station refresh (7-tap on version in Settings)
    private let remoteStationsURL = "https://raw.githubusercontent.com/TheLordoftheBugs/letzlisten/main/stations.json"

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
        stationsVersion = config.version
    }

    func fetchFromRemote(completion: @escaping (Bool) -> Void) {
        guard let url = URL(string: remoteStationsURL) else {
            DispatchQueue.main.async { completion(false) }
            return
        }
        URLSession.shared.dataTask(with: url) { [weak self] data, _, error in
            guard let self = self,
                  let data = data,
                  error == nil,
                  let config = self.parseJSON(data) else {
                DispatchQueue.main.async { completion(false) }
                return
            }
            DispatchQueue.main.async {
                self.stations = config.stations.sorted { $0.name < $1.name }
                self.stationsVersion = config.version
                completion(true)
            }
        }.resume()
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
