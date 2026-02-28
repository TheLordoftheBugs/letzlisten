//
//  StationSelectorView.swift
//  Radio Lëtzebuerg
//
//  Popup for selecting radio stations
//

import SwiftUI

struct StationSelectorView: View {
    @EnvironmentObject var audioPlayer: RadioPlayer
    @EnvironmentObject var languageManager: LanguageManager
    @Environment(\.dismiss) var dismiss
    
    // Sorted stations: only enabled stations, sorted alphabetically
    private var sortedStations: [RadioStation] {
        RadioStation.stations
            .filter { $0.enabled }  // Only show enabled stations
            .sorted { $0.name.localizedCaseInsensitiveCompare($1.name) == .orderedAscending }
    }
    
    var body: some View {
        NavigationView {
            ZStack {
                // Background gradient
                LinearGradient(
                    gradient: Gradient(colors: [
                        Color(red: 0.1, green: 0.1, blue: 0.2),
                        Color(red: 0.05, green: 0.05, blue: 0.15)
                    ]),
                    startPoint: .top,
                    endPoint: .bottom
                )
                .ignoresSafeArea()
                
                VStack(spacing: 0) {
                    // Header
                    Text(languageManager.chooseYourRadio)
                        .font(.system(size: 24, weight: .bold))
                        .foregroundColor(.white)
                        .padding(.top, 20)
                        .padding(.bottom, 10)
                    
                    // Scrollable station list
                    ScrollView {
                        LazyVStack(spacing: 12) {
                            ForEach(sortedStations) { station in
                                StationButton(
                                    station: station,
                                    isSelected: station.id == audioPlayer.currentStation.id
                                ) {
                                    audioPlayer.switchStation(station)
                                    dismiss()
                                }
                            }
                        }
                        .padding(.horizontal)
                        .padding(.bottom, 20)
                    }
                }
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(languageManager.back) {
                        dismiss()
                    }
                    .foregroundColor(.blue)
                }
            }
            .toolbarBackground(.visible, for: .navigationBar)
            .toolbarBackground(Color(red: 0.08, green: 0.08, blue: 0.12), for: .navigationBar)
        }
        .presentationDetents([.large])
        .presentationDragIndicator(.visible)
    }
}

struct StationButton: View {
    let station: RadioStation
    let isSelected: Bool
    let action: () -> Void
    @State private var logo: UIImage?
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: 16) {
                // Station logo with loading
                if let logo = logo {
                    Image(uiImage: logo)
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(width: 50, height: 50)
                        .cornerRadius(8)
                        .opacity(station.enabled ? 1.0 : 0.3)  // Grayed if disabled
                } else {
                    RoundedRectangle(cornerRadius: 8)
                        .fill(Color.white.opacity(station.enabled ? 0.1 : 0.05))
                        .frame(width: 50, height: 50)
                        .overlay(
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: .white.opacity(0.5)))
                                .scaleEffect(0.7)
                        )
                }
                
                // Station name
                Text(station.name)
                    .font(.system(size: 17, weight: .medium))
                    .foregroundColor(station.enabled ? .white : .white.opacity(0.4))  // Grayed if disabled
                
                Spacer()
                
                // Disabled icon or checkmark
                if !station.enabled {
                    Image(systemName: "lock.fill")
                        .foregroundColor(.white.opacity(0.3))
                        .font(.system(size: 16))
                } else if isSelected {
                    Image(systemName: "checkmark")
                        .foregroundColor(.blue)
                        .font(.system(size: 16, weight: .semibold))
                }
            }
            .padding(.vertical, 12)
            .padding(.horizontal, 16)
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .fill(isSelected && station.enabled ? Color.blue.opacity(0.2) : Color.white.opacity(0.05))
            )
        }
        .onAppear {
            loadStationLogo()
        }
    }
    
    private func loadStationLogo() {
        FaviconFetcher.shared.loadLogo(for: station) { image in
            logo = image
        }
    }
}

#Preview {
    StationSelectorView()
        .environmentObject(RadioPlayer())
        .environmentObject(LanguageManager.shared)
}
