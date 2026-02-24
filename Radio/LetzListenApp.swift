//
//  LetzListenApp.swift
//  Lëtz Listen
//
//  Main app entry point
//

import SwiftUI

@main
struct LetzListenApp: App {
    // Force RadioStationLoader to initialize first
    private let stationLoader = RadioStationLoader.shared
    
    @StateObject private var audioPlayer = RadioPlayer()
    @StateObject private var favoritesManager = FavoritesManager()
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(audioPlayer)
                .environmentObject(favoritesManager)
                .onAppear {
                    RadioStationLoader.shared.loadStations()
                }
        }
    }
}
