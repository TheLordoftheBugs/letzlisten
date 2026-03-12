//
//  LetzListenApp.swift
//  Letzebuerg FM
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

    @AppStorage("AppearanceMode") private var appearanceMode: String = "system"

    private var preferredColorScheme: ColorScheme? {
        switch appearanceMode {
        case "light": return .light
        case "dark":  return .dark
        default:      return nil
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(audioPlayer)
                .environmentObject(favoritesManager)
                .environmentObject(languageManager)
                .preferredColorScheme(preferredColorScheme)
        }
    }
}
