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
    @StateObject private var languageManager = LanguageManager.shared

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(audioPlayer)
                .environmentObject(favoritesManager)
                .environmentObject(languageManager)
                .onAppear {
                    RadioStationLoader.shared.loadStations()
                }
        }
    }
}
