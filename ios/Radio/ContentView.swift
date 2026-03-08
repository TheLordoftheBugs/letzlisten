//
//  ContentView.swift
//  Letzebuerg FM
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
    @State private var showSettings = false
    @State private var showFavoritesPanel = false   // iPad: left panel
    @State private var showStationPanel = false     // iPad: right panel
    @State private var stationLogo: UIImage?
    
    // Detect orientation
    @Environment(\.verticalSizeClass) var verticalSizeClass
    @Environment(\.horizontalSizeClass) var horizontalSizeClass
    
    var isIPad: Bool {
        UIDevice.current.userInterfaceIdiom == .pad
    }

    var isLandscape: Bool {
        verticalSizeClass == .compact
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
            
            if isIPad {
                // iPad layout: split panels (favorites left, station right)
                iPadSplitLayout(
                    audioPlayer: audioPlayer,
                    favoritesManager: favoritesManager,
                    stationLogo: stationLogo,
                    showFavoritesPanel: $showFavoritesPanel,
                    showStationPanel: $showStationPanel
                )
            } else if isLandscape {
                // iPhone landscape layout
                LandscapeLayout(
                    audioPlayer: audioPlayer,
                    favoritesManager: favoritesManager,
                    stationLogo: stationLogo
                )
            } else {
                // iPhone portrait layout
                PortraitLayout(
                    audioPlayer: audioPlayer,
                    favoritesManager: favoritesManager,
                    stationLogo: stationLogo
                )
            }
        }
        // Top buttons — fixed circle size so all 3 are identical regardless of icon shape
        .overlay(alignment: .top) {
            let circleSize: CGFloat = isLandscape ? 53 : 60
            let iconSize: CGFloat = isLandscape ? 24 : 26

            HStack(spacing: 0) {
                // Favorites - LEFT
                Button(action: {
                    if isIPad {
                        withAnimation(.easeInOut(duration: 0.3)) { showFavoritesPanel.toggle() }
                    } else {
                        showFavorites = true
                    }
                }) {
                    ZStack {
                        Circle()
                            .fill(Color.white.opacity(isIPad && showFavoritesPanel ? 0.25 : 0.15))
                        Image(systemName: isIPad && showFavoritesPanel ? "heart.fill" : "heart")
                            .font(.system(size: iconSize, weight: .semibold))
                            .foregroundColor(.white.opacity(0.9))
                    }
                    .frame(width: circleSize, height: circleSize)
                }
                .padding(.leading, 20)

                Spacer()

                // Station selector - CENTER
                Button(action: {
                    if isIPad {
                        withAnimation(.easeInOut(duration: 0.3)) { showStationPanel.toggle() }
                    } else {
                        showStationSelector = true
                    }
                }) {
                    ZStack {
                        Circle()
                            .fill(Color.white.opacity(isIPad && showStationPanel ? 0.25 : 0.15))
                        Image(systemName: "antenna.radiowaves.left.and.right")
                            .font(.system(size: iconSize, weight: .semibold))
                            .foregroundColor(.white.opacity(0.9))
                    }
                    .frame(width: circleSize, height: circleSize)
                }

                Spacer()

                // Settings - RIGHT
                Button(action: {
                    showSettings = true
                }) {
                    ZStack {
                        Circle()
                            .fill(Color.white.opacity(0.15))
                        Image(systemName: "gearshape")
                            .font(.system(size: iconSize, weight: .semibold))
                            .foregroundColor(.white.opacity(0.9))
                    }
                    .frame(width: circleSize, height: circleSize)
                }
                .padding(.trailing, 20)
            }
            .padding(.top, 16)
        }
        // Bottom buttons (AirPlay / Play / Share) — single HStack for perfect alignment
        .overlay(alignment: .bottom) {
            let btnSize: CGFloat = isLandscape ? 53 : 60
            let playSize: CGFloat = isLandscape ? 62 : 77
            let canShare = audioPlayer.isPlaying

            HStack(spacing: 0) {
                // AirPlay - LEFT
                ZStack {
                    Circle()
                        .fill(Color.white.opacity(0.15))
                    AirPlayButton()
                }
                .frame(width: btnSize, height: btnSize)
                .padding(.leading, 20)

                Spacer()

                // Play/Stop - CENTER
                Button(action: {
                    audioPlayer.togglePlayback()
                }) {
                    ZStack {
                        Circle()
                            .fill(audioPlayer.isPlaying ? Color.red : Color.blue)
                            .shadow(color: (audioPlayer.isPlaying ? Color.red : Color.blue).opacity(0.4), radius: isLandscape ? 6 : 8, x: 0, y: isLandscape ? 3 : 4)
                        Image(systemName: audioPlayer.isPlaying ? "stop.fill" : "play.fill")
                            .font(.system(size: isLandscape ? 26 : 34))
                            .foregroundColor(.white)
                    }
                    .frame(width: playSize, height: playSize)
                }
                .disabled(audioPlayer.isLoading)

                Spacer()

                // Share - RIGHT
                Button(action: {
                    showingShareSheet = true
                }) {
                    ZStack {
                        Circle()
                            .fill(Color.white.opacity(canShare ? 0.15 : 0.05))
                        Image(systemName: "square.and.arrow.up")
                            .font(.system(size: isLandscape ? 24 : 26, weight: .semibold))
                            .foregroundColor(.white.opacity(canShare ? 0.9 : 0.4))
                    }
                    .frame(width: btnSize, height: btnSize)
                }
                .disabled(!canShare)
                .padding(.trailing, 20)
            }
            .padding(.bottom, -8)
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
        .sheet(isPresented: $showSettings) {
            SettingsView()
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
        let station = audioPlayer.currentStation.name
        let url = audioPlayer.currentStation.websiteURL
        let message: String
        if audioPlayer.currentTrack.isUnknown {
            message = languageManager.shareStationMessage(station: station, url: url)
        } else {
            message = languageManager.shareMessage(
                artist: audioPlayer.currentTrack.artist,
                title: audioPlayer.currentTrack.title,
                station: station,
                url: url
            )
        }
        return [message]
    }
}

// MARK: - Portrait Layout
struct PortraitLayout: View {
    @ObservedObject var audioPlayer: RadioPlayer
    @ObservedObject var favoritesManager: FavoritesManager
    let stationLogo: UIImage?

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

                // Station name (tappable = link to station website)
                Group {
                    if let urlString = audioPlayer.currentStation.websiteURL,
                       let url = URL(string: urlString) {
                        Link(destination: url) {
                            Text(audioPlayer.currentStation.name)
                                .font(.system(size: 32, weight: .bold))
                                .foregroundColor(.white)
                                .multilineTextAlignment(.center)
                                .frame(maxWidth: .infinity)
                        }
                    } else {
                        Text(audioPlayer.currentStation.name)
                            .font(.system(size: 32, weight: .bold))
                            .foregroundColor(.white)
                            .multilineTextAlignment(.center)
                            .frame(maxWidth: .infinity)
                    }
                }

                // Track info + favorite button (always reserve space, hidden when no metadata)
                TrackInfoView(track: audioPlayer.currentTrack)
                    .opacity(audioPlayer.currentTrack.isUnknown ? 0 : 1)

                FavoriteButton(
                    audioPlayer: audioPlayer,
                    favoritesManager: favoritesManager
                )
                .opacity(audioPlayer.currentTrack.isUnknown ? 0 : 1)
            }
            .padding(.horizontal, 40)

            Spacer()
        }
    }
}

// MARK: - Landscape Layout
struct LandscapeLayout: View {
    @ObservedObject var audioPlayer: RadioPlayer
    @ObservedObject var favoritesManager: FavoritesManager
    let stationLogo: UIImage?

    var body: some View {
        VStack(spacing: 0) {
            Spacer()
            // Main content — paddings clear header (~60pt) and footer (~60pt) overlays
            HStack(spacing: 32) {
                // Left: Artwork (tappable = link to station website)
                Group {
                    if let urlString = audioPlayer.currentStation.websiteURL,
                       let url = URL(string: urlString) {
                        Link(destination: url) {
                            ArtworkView(
                                artwork: audioPlayer.currentArtwork,
                                stationLogo: stationLogo,
                                size: 120
                            )
                        }
                    } else {
                        ArtworkView(
                            artwork: audioPlayer.currentArtwork,
                            stationLogo: stationLogo,
                            size: 120
                        )
                    }
                }
                .padding(.leading, 72)

                // Right: Station name + track info + favourite button
                VStack(spacing: 10) {
                    // Station name (tappable = link to station website)
                    Group {
                        if let urlString = audioPlayer.currentStation.websiteURL,
                           let url = URL(string: urlString) {
                            Link(destination: url) {
                                Text(audioPlayer.currentStation.name)
                                    .font(.system(size: 18, weight: .bold))
                                    .foregroundColor(.white)
                                    .multilineTextAlignment(.center)
                                    .frame(maxWidth: .infinity)
                                    .lineLimit(1)
                                    .minimumScaleFactor(0.7)
                            }
                        } else {
                            Text(audioPlayer.currentStation.name)
                                .font(.system(size: 18, weight: .bold))
                                .foregroundColor(.white)
                                .multilineTextAlignment(.center)
                                .frame(maxWidth: .infinity)
                                .lineLimit(1)
                                .minimumScaleFactor(0.7)
                        }
                    }

                    // Track info + favourite button (only when metadata is known)
                    if !audioPlayer.currentTrack.isUnknown {
                        VStack(spacing: 4) {
                            Text(audioPlayer.currentTrack.title)
                                .font(.system(size: 15, weight: .semibold))
                                .foregroundColor(.white)
                                .lineLimit(1)
                                .minimumScaleFactor(0.8)
                            Text(audioPlayer.currentTrack.artist)
                                .font(.system(size: 13, weight: .regular))
                                .foregroundColor(.white.opacity(0.8))
                                .lineLimit(1)
                                .minimumScaleFactor(0.8)
                        }

                        FavoriteButton(
                            audioPlayer: audioPlayer,
                            favoritesManager: favoritesManager
                        )
                    }
                }
                .padding(.trailing, 72)
            }
            Spacer()
        }
        .padding(.top, 60)
        .padding(.bottom, 60)
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

                ScrollView {
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

// MARK: - iPad Layout

struct iPadSplitLayout: View {
    @ObservedObject var audioPlayer: RadioPlayer
    @ObservedObject var favoritesManager: FavoritesManager
    let stationLogo: UIImage?
    @Binding var showFavoritesPanel: Bool
    @Binding var showStationPanel: Bool

    var body: some View {
        HStack(spacing: 0) {
            // Left panel: Favorites (slides in from left)
            if showFavoritesPanel {
                iPadFavoritesPanel()
                    .frame(width: 320)
                    .transition(.move(edge: .leading))

                Rectangle()
                    .fill(Color.white.opacity(0.1))
                    .frame(width: 1)
            }

            // Center: iPhone-style portrait layout
            PortraitLayout(
                audioPlayer: audioPlayer,
                favoritesManager: favoritesManager,
                stationLogo: stationLogo
            )
            .frame(maxWidth: .infinity)

            // Right panel: Station selector (slides in from right)
            if showStationPanel {
                Rectangle()
                    .fill(Color.white.opacity(0.1))
                    .frame(width: 1)

                iPadStationSidebar(audioPlayer: audioPlayer)
                    .frame(width: 320)
                    .transition(.move(edge: .trailing))
            }
        }
        .animation(.easeInOut(duration: 0.3), value: showFavoritesPanel)
        .animation(.easeInOut(duration: 0.3), value: showStationPanel)
        .onChange(of: showStationPanel) { newValue in
            if newValue { withAnimation(.easeInOut(duration: 0.3)) { showFavoritesPanel = false } }
        }
        .onChange(of: showFavoritesPanel) { newValue in
            if newValue { withAnimation(.easeInOut(duration: 0.3)) { showStationPanel = false } }
        }
    }
}

struct iPadFavoritesPanel: View {
    @EnvironmentObject var favoritesManager: FavoritesManager
    @EnvironmentObject var languageManager: LanguageManager
    @State private var showConfirmClearAll = false

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Spacer()

                if !favoritesManager.favorites.isEmpty {
                    Button(role: .destructive) {
                        showConfirmClearAll = true
                    } label: {
                        Text(languageManager.clearAll)
                            .font(.system(size: 14))
                            .foregroundColor(.red)
                    }
                    .alert(languageManager.confirmClearAll, isPresented: $showConfirmClearAll) {
                        Button(languageManager.clearAll, role: .destructive) {
                            favoritesManager.clearAll()
                        }
                        Button(languageManager.cancel, role: .cancel) {}
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 16)
            .padding(.bottom, 48)

            Rectangle()
                .fill(Color.white.opacity(0.1))
                .frame(height: 1)

            if favoritesManager.favorites.isEmpty {
                Spacer()
                VStack(spacing: 16) {
                    Image(systemName: "heart.slash")
                        .font(.system(size: 48))
                        .foregroundColor(.white.opacity(0.4))

                    Text(languageManager.noFavoritesYet)
                        .font(.title3)
                        .fontWeight(.semibold)
                        .foregroundColor(.white)

                    Text(languageManager.noFavoritesHint)
                        .font(.body)
                        .foregroundColor(.white.opacity(0.7))
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 20)
                }
                Spacer()
            } else {
                List {
                    ForEach(favoritesManager.favorites) { favorite in
                        FavoriteRowView(favorite: favorite)
                            .listRowBackground(Color.clear)
                            .listRowSeparator(.hidden)
                    }
                    .onDelete(perform: favoritesManager.removeFavorite)
                }
                .listStyle(.plain)
                .scrollContentBackground(.hidden)
            }
        }
        .background(Color(red: 0.08, green: 0.08, blue: 0.12).opacity(0.95))
    }
}

struct iPadStationSidebar: View {
    @ObservedObject var audioPlayer: RadioPlayer
    @EnvironmentObject var languageManager: LanguageManager
    @ObservedObject private var stationLoader = RadioStationLoader.shared

    private var sortedStations: [RadioStation] {
        RadioStation.stations
            .sorted { $0.name.localizedCaseInsensitiveCompare($1.name) == .orderedAscending }
    }

    var body: some View {
        VStack(spacing: 0) {
            Text(languageManager.chooseYourRadio)
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(.white)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal, 16)
                .padding(.top, 16)
                .padding(.bottom, 48)

            Rectangle()
                .fill(Color.white.opacity(0.1))
                .frame(height: 1)

            ScrollView {
                LazyVStack(spacing: 8) {
                    ForEach(sortedStations) { station in
                        iPadStationRow(
                            station: station,
                            isSelected: station.id == audioPlayer.currentStation.id
                        ) {
                            audioPlayer.switchStation(station)
                        }
                    }
                }
                .padding(.vertical, 12)
                .padding(.horizontal, 8)
            }
        }
        .background(Color(red: 0.08, green: 0.08, blue: 0.12).opacity(0.95))
    }
}

struct iPadStationRow: View {
    let station: RadioStation
    let isSelected: Bool
    let onTap: () -> Void
    @State private var logo: UIImage?

    var body: some View {
        Button(action: station.enabled ? onTap : {}) {
            HStack(spacing: 12) {
                Group {
                    if let logo = logo {
                        Image(uiImage: logo)
                            .resizable()
                            .aspectRatio(contentMode: .fit)
                    } else {
                        RoundedRectangle(cornerRadius: 8)
                            .fill(Color.white.opacity(station.enabled ? 0.1 : 0.05))
                            .overlay(
                                ProgressView()
                                    .progressViewStyle(CircularProgressViewStyle(tint: .white.opacity(0.5)))
                                    .scaleEffect(0.7)
                            )
                    }
                }
                .frame(width: 44, height: 44)
                .cornerRadius(8)
                .opacity(station.enabled ? 1.0 : 0.3)

                Text(station.name)
                    .font(.system(size: 16, weight: isSelected && station.enabled ? .semibold : .regular))
                    .foregroundColor(.white.opacity(station.enabled ? 1.0 : 0.4))
                    .lineLimit(1)

                Spacer()

                if !station.enabled {
                    Image(systemName: "lock.fill")
                        .font(.system(size: 14))
                        .foregroundColor(.white.opacity(0.3))
                } else if isSelected {
                    Image(systemName: "checkmark")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(.blue)
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 10)
            .background(
                RoundedRectangle(cornerRadius: 10)
                    .fill(isSelected && station.enabled ? Color.blue.opacity(0.2) : Color.clear)
            )
        }
        .onAppear {
            FaviconFetcher.shared.loadLogo(for: station) { image in
                logo = image
            }
        }
    }
}


#Preview {
    ContentView()
        .environmentObject(RadioPlayer())
        .environmentObject(FavoritesManager())
        .environmentObject(LanguageManager.shared)
}
