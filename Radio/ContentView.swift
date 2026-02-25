//
//  ContentView.swift
//  Lëtz Listen
//
//  Main UI with landscape support
//

import SwiftUI

struct ContentView: View {
    @EnvironmentObject var audioPlayer: RadioPlayer
    @EnvironmentObject var favoritesManager: FavoritesManager
    @EnvironmentObject var languageManager: LanguageManager
    @ObservedObject var stationLoader = RadioStationLoader.shared
    @State private var showFavorites = false
    @State private var showingShareSheet = false
    @State private var showStationSelector = false
    @State private var showLanguagePicker = false
    @State private var stationLogo: UIImage?
    
    // Detect orientation
    @Environment(\.verticalSizeClass) var verticalSizeClass
    @Environment(\.horizontalSizeClass) var horizontalSizeClass
    
    var isLandscape: Bool {
        verticalSizeClass == .compact || horizontalSizeClass == .regular
    }
    
    var body: some View {
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
            
            if isLandscape {
                // Landscape layout
                LandscapeLayout(
                    audioPlayer: audioPlayer,
                    favoritesManager: favoritesManager,
                    stationLogo: stationLogo,
                    showStationSelector: $showStationSelector
                )
            } else {
                // Portrait layout (current)
                PortraitLayout(
                    audioPlayer: audioPlayer,
                    favoritesManager: favoritesManager,
                    stationLogo: stationLogo,
                    showStationSelector: $showStationSelector
                )
            }
        }
        // Favorites button - Top LEFT
        .overlay(alignment: .topLeading) {
            Button(action: {
                showFavorites = true
            }) {
                Image(systemName: "heart.circle")
                    .font(.system(size: 20, weight: .semibold))
                    .foregroundColor(.white.opacity(0.9))
                    .padding(12)
                    .background(
                        Circle()
                            .fill(Color.white.opacity(0.15))
                    )
            }
            .padding(.top, 16)
            .padding(.leading, 20)
        }
        // Language picker button - Top CENTER
        .overlay(alignment: .top) {
            Button(action: {
                showLanguagePicker = true
            }) {
                Text(languageManager.currentLanguage.flag)
                    .font(.system(size: 20))
                    .padding(.horizontal, 12)
                    .padding(.vertical, 6)
                    .background(
                        Capsule()
                            .fill(Color.white.opacity(0.15))
                    )
            }
            .padding(.top, 20)
        }
        // Share button - Top RIGHT
        .overlay(alignment: .topTrailing) {
            Button(action: {
                showingShareSheet = true
            }) {
                Image(systemName: "square.and.arrow.up")
                    .font(.system(size: 20, weight: .semibold))
                    .foregroundColor(.white.opacity(audioPlayer.isPlaying ? 0.9 : 0.4))
                    .padding(12)
                    .background(
                        Circle()
                            .fill(Color.white.opacity(audioPlayer.isPlaying ? 0.15 : 0.05))
                    )
            }
            .disabled(!audioPlayer.isPlaying)
            .padding(.top, 16)
            .padding(.trailing, 20)
        }
        .sheet(isPresented: $showFavorites) {
            FavoritesView()
                .environmentObject(favoritesManager)
                .environmentObject(languageManager)
        }
        .sheet(isPresented: $showingShareSheet) {
            ShareSheet(items: generateShareItems())
        }
        .sheet(isPresented: $showStationSelector) {
            StationSelectorView()
                .environmentObject(audioPlayer)
                .environmentObject(languageManager)
        }
        .sheet(isPresented: $showLanguagePicker) {
            LanguagePickerView()
                .environmentObject(languageManager)
        }
        .onAppear {
            loadStationLogo()
        }
        .onChange(of: audioPlayer.currentStation.id) { _ in
            loadStationLogo()
        }
    }
    
    private func loadStationLogo() {
        FaviconFetcher.shared.loadLogo(for: audioPlayer.currentStation) { image in
            stationLogo = image
        }
    }
    
    private func generateShareItems() -> [Any] {
        let title = audioPlayer.currentTrack.title
        let artist = audioPlayer.currentTrack.artist
        let station = audioPlayer.currentStation.name
        let message = languageManager.shareMessage(
            artist: artist,
            title: title,
            station: station,
            url: audioPlayer.currentStation.websiteURL
        )
        return [message]
    }
}

// MARK: - Portrait Layout
struct PortraitLayout: View {
    @ObservedObject var audioPlayer: RadioPlayer
    @ObservedObject var favoritesManager: FavoritesManager
    let stationLogo: UIImage?
    @Binding var showStationSelector: Bool
    
    var body: some View {
        VStack(spacing: 0) {
            Spacer()
            
            // Station artwork and info
            VStack(spacing: 24) {
                // Artwork (tappable = link to station website)
                Group {
                    if let urlString = audioPlayer.currentStation.websiteURL,
                       let url = URL(string: urlString) {
                        Link(destination: url) {
                            ArtworkView(
                                artwork: audioPlayer.currentArtwork,
                                stationLogo: stationLogo,
                                size: 180
                            )
                        }
                    } else {
                        ArtworkView(
                            artwork: audioPlayer.currentArtwork,
                            stationLogo: stationLogo,
                            size: 180
                        )
                    }
                }

                // Station name
                Text(audioPlayer.currentStation.name)
                    .font(.system(size: 32, weight: .bold))
                    .foregroundColor(.white)
                    .multilineTextAlignment(.center)
                    .frame(maxWidth: .infinity)

                // Track info + favorite button (only when metadata is known)
                if !audioPlayer.currentTrack.isUnknown {
                    TrackInfoView(track: audioPlayer.currentTrack)

                    FavoriteButton(
                        audioPlayer: audioPlayer,
                        favoritesManager: favoritesManager
                    )
                }
            }
            .padding(.horizontal, 40)

            Spacer()

            // Bottom control bar
            BottomControlBar(showStationSelector: $showStationSelector)
                .environmentObject(audioPlayer)
                .environmentObject(favoritesManager)
        }
    }
}

// MARK: - Landscape Layout
struct LandscapeLayout: View {
    @ObservedObject var audioPlayer: RadioPlayer
    @ObservedObject var favoritesManager: FavoritesManager
    let stationLogo: UIImage?
    @Binding var showStationSelector: Bool
    
    var body: some View {
        VStack(spacing: 0) {
            Spacer()
            
            HStack(spacing: 40) {
                // Left: Artwork (tappable = link to station website)
                Group {
                    if let urlString = audioPlayer.currentStation.websiteURL,
                       let url = URL(string: urlString) {
                        Link(destination: url) {
                            ArtworkView(
                                artwork: audioPlayer.currentArtwork,
                                stationLogo: stationLogo,
                                size: 160
                            )
                        }
                    } else {
                        ArtworkView(
                            artwork: audioPlayer.currentArtwork,
                            stationLogo: stationLogo,
                            size: 160
                        )
                    }
                }
                .padding(.leading, 40)

                // Right: Info and controls
                VStack(spacing: 20) {
                    // Station name
                    Text(audioPlayer.currentStation.name)
                        .font(.system(size: 28, weight: .bold))
                        .foregroundColor(.white)
                        .multilineTextAlignment(.center)
                        .frame(maxWidth: .infinity)

                    // Track info + favorite button (only when metadata is known)
                    if !audioPlayer.currentTrack.isUnknown {
                        TrackInfoView(track: audioPlayer.currentTrack)

                        FavoriteButton(
                            audioPlayer: audioPlayer,
                            favoritesManager: favoritesManager
                        )
                    }

                    Spacer().frame(height: 20)
                    
                    // Play controls inline
                    HStack(spacing: 30) {
                        // AirPlay
                        AirPlayButton()
                            .frame(width: 50, height: 50)
                        
                        // Play/Stop
                        Button(action: {
                            audioPlayer.togglePlayback()
                        }) {
                            ZStack {
                                Circle()
                                    .fill(audioPlayer.isPlaying ? Color.red : Color.blue)
                                    .frame(width: 64, height: 64)
                                
                                Image(systemName: audioPlayer.isPlaying ? "stop.fill" : "play.fill")
                                    .font(.system(size: 28))
                                    .foregroundColor(.white)
                            }
                        }
                        .disabled(audioPlayer.isLoading)
                        
                        // Station selector
                        Button(action: {
                            showStationSelector = true
                        }) {
                            Image(systemName: "antenna.radiowaves.left.and.right")
                                .font(.system(size: 24))
                                .foregroundColor(.white)
                                .frame(width: 50, height: 50)
                                .background(
                                    RoundedRectangle(cornerRadius: 12)
                                        .fill(Color.white.opacity(0.15))
                                )
                        }
                    }
                }
                .padding(.trailing, 40)
            }
            
            Spacer()
        }
    }
}

// MARK: - Shared Components

struct ArtworkView: View {
    let artwork: UIImage?
    let stationLogo: UIImage?
    let size: CGFloat
    
    var body: some View {
        Group {
            if let artwork = artwork {
                Image(uiImage: artwork)
                    .resizable()
                    .aspectRatio(contentMode: .fill)
                    .frame(width: size, height: size)
                    .cornerRadius(20)
                    .shadow(color: .black.opacity(0.3), radius: 20, x: 0, y: 10)
            } else if let logo = stationLogo {
                Image(uiImage: logo)
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .frame(width: size, height: size)
                    .cornerRadius(20)
                    .shadow(color: .black.opacity(0.3), radius: 20, x: 0, y: 10)
            } else {
                RoundedRectangle(cornerRadius: 20)
                    .fill(Color.white.opacity(0.1))
                    .frame(width: size, height: size)
                    .overlay(
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                    )
                    .shadow(color: .black.opacity(0.3), radius: 20, x: 0, y: 10)
            }
        }
    }
}

struct TrackInfoView: View {
    let track: TrackInfo
    @EnvironmentObject var languageManager: LanguageManager

    var body: some View {
        VStack(spacing: 8) {
            Text(track.title == "Title" ? languageManager.defaultTitle : track.title)
                .font(.system(size: 20, weight: .semibold))
                .foregroundColor(.white)
                .lineLimit(1)
                .minimumScaleFactor(0.8)

            Text(track.artist == "Artist" ? languageManager.defaultArtist : track.artist)
                .font(.system(size: 16, weight: .regular))
                .foregroundColor(.white.opacity(0.8))
                .lineLimit(1)
                .minimumScaleFactor(0.8)
        }
    }
}

struct FavoriteButton: View {
    @ObservedObject var audioPlayer: RadioPlayer
    @ObservedObject var favoritesManager: FavoritesManager
    
    var body: some View {
        Button(action: {
            let track = audioPlayer.currentTrack
            let station = audioPlayer.currentStation
            
            if favoritesManager.isFavorited(title: track.title, artist: track.artist) {
                favoritesManager.removeFavorite(title: track.title, artist: track.artist)
            } else {
                favoritesManager.addFavorite(
                    title: track.title,
                    artist: track.artist,
                    stationId: station.id,
                    stationName: station.name
                )
            }
        }) {
            let isFavorited = favoritesManager.isFavorited(
                title: audioPlayer.currentTrack.title,
                artist: audioPlayer.currentTrack.artist
            )
            
            Image(systemName: isFavorited ? "heart.fill" : "heart")
                .font(.system(size: 28))
                .foregroundColor(isFavorited ? .red : .white.opacity(0.7))
        }
        .padding(.top, 8)
    }
}

// MARK: - Bottom Control Bar
struct BottomControlBar: View {
    @EnvironmentObject var audioPlayer: RadioPlayer
    @EnvironmentObject var favoritesManager: FavoritesManager
    @Binding var showStationSelector: Bool
    
    var body: some View {
        VStack(spacing: 0) {
            Divider()
                .background(Color.white.opacity(0.1))
            
            HStack(spacing: 20) {
                // AirPlay button
                AirPlayButton()
                    .frame(width: 64, height: 64)
                
                Spacer()
                
                // Play/Stop button
                Button(action: {
                    audioPlayer.togglePlayback()
                }) {
                    ZStack {
                        Circle()
                            .fill(audioPlayer.isPlaying ? Color.red : Color.blue)
                            .frame(width: 64, height: 64)
                            .shadow(color: (audioPlayer.isPlaying ? Color.red : Color.blue).opacity(0.4), radius: 8, x: 0, y: 4)
                        
                        Image(systemName: audioPlayer.isPlaying ? "stop.fill" : "play.fill")
                            .font(.system(size: 28))
                            .foregroundColor(.white)
                    }
                }
                .disabled(audioPlayer.isLoading)
                
                Spacer()
                
                // Station Selector Button
                Button(action: {
                    showStationSelector = true
                }) {
                    Image(systemName: "antenna.radiowaves.left.and.right")
                        .font(.system(size: 28))
                        .foregroundColor(.white)
                        .frame(width: 64, height: 64)
                        .background(
                            RoundedRectangle(cornerRadius: 12)
                                .fill(Color.white.opacity(0.15))
                        )
                }
            }
            .padding(.horizontal, 24)
            .padding(.vertical, 16)
            .background(
                Color(red: 0.08, green: 0.08, blue: 0.12)
                    .opacity(0.95)
            )
        }
    }
}

// Share Sheet for sharing current track
struct ShareSheet: UIViewControllerRepresentable {
    let items: [Any]
    
    func makeUIViewController(context: Context) -> UIActivityViewController {
        let controller = UIActivityViewController(activityItems: items, applicationActivities: nil)
        return controller
    }
    
    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {
        // No updates needed
    }
}

// MARK: - Language Picker

struct LanguagePickerView: View {
    @EnvironmentObject var languageManager: LanguageManager
    @Environment(\.dismiss) var dismiss

    var body: some View {
        NavigationView {
            ZStack {
                LinearGradient(
                    gradient: Gradient(colors: [
                        Color(red: 0.1, green: 0.1, blue: 0.2),
                        Color(red: 0.05, green: 0.05, blue: 0.15)
                    ]),
                    startPoint: .top,
                    endPoint: .bottom
                )
                .ignoresSafeArea()

                VStack(spacing: 16) {
                    ForEach(LanguageManager.Language.allCases, id: \.rawValue) { language in
                        Button(action: {
                            languageManager.currentLanguage = language
                            dismiss()
                        }) {
                            HStack(spacing: 16) {
                                Text(language.flag)
                                    .font(.system(size: 32))
                                Text(language.displayName)
                                    .font(.system(size: 18, weight: .medium))
                                    .foregroundColor(.white)
                                Spacer()
                                if language == languageManager.currentLanguage {
                                    Image(systemName: "checkmark")
                                        .foregroundColor(.blue)
                                        .font(.system(size: 16, weight: .semibold))
                                }
                            }
                            .padding(.vertical, 14)
                            .padding(.horizontal, 20)
                            .background(
                                RoundedRectangle(cornerRadius: 12)
                                    .fill(language == languageManager.currentLanguage
                                          ? Color.blue.opacity(0.2)
                                          : Color.white.opacity(0.05))
                            )
                        }
                    }
                }
                .padding(.horizontal, 20)
                .padding(.top, 20)
            }
            .navigationTitle(languageManager.selectLanguage)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(languageManager.done) {
                        dismiss()
                    }
                    .foregroundColor(.blue)
                }
            }
            .toolbarBackground(.visible, for: .navigationBar)
            .toolbarBackground(Color(red: 0.08, green: 0.08, blue: 0.12), for: .navigationBar)
        }
        .presentationDetents([.medium])
        .presentationDragIndicator(.visible)
    }
}

#Preview {
    ContentView()
        .environmentObject(RadioPlayer())
        .environmentObject(FavoritesManager())
        .environmentObject(LanguageManager.shared)
}
