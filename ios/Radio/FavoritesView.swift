//
//  FavoritesView.swift
//  Radio
//
//  View to display and manage favorite songs
//

import SwiftUI

struct FavoritesView: View {
    @EnvironmentObject var favoritesManager: FavoritesManager
    @EnvironmentObject var languageManager: LanguageManager
    @Environment(\.dismiss) var dismiss

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

                if favoritesManager.favorites.isEmpty {
                    // Empty state
                    VStack(spacing: 16) {
                        Image(systemName: "heart.slash")
                            .font(.system(size: 60))
                            .foregroundColor(.white.opacity(0.4))

                        Text(languageManager.noFavoritesYet)
                            .font(.title2)
                            .fontWeight(.semibold)
                            .foregroundColor(.white)

                        Text(languageManager.noFavoritesHint)
                            .font(.body)
                            .foregroundColor(.white.opacity(0.7))
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 40)
                    }
                } else {
                    ScrollView {
                        LazyVStack(spacing: 8) {
                            ForEach(favoritesManager.favorites) { favorite in
                                FavoriteRowView(favorite: favorite) {
                                    favoritesManager.removeFavorite(favorite)
                                }
                            }
                        }
                        .padding(.horizontal, 20)
                        .padding(.top, 12)
                        .padding(.bottom, 20)
                    }
                }
            }
            .navigationTitle(languageManager.favorites)
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
    }
}

struct FavoriteRowView: View {
    let favorite: Favorite
    let onDelete: () -> Void

    var body: some View {
        HStack(spacing: 0) {
            Button(action: {
                searchOnWeb(artist: favorite.artist, title: favorite.title)
            }) {
                VStack(alignment: .leading, spacing: 12) {
                    // Title and Artist
                    VStack(alignment: .leading, spacing: 4) {
                        Text(favorite.title)
                            .font(.system(size: 18, weight: .semibold))
                            .foregroundColor(.white)

                        Text(favorite.artist)
                            .font(.system(size: 15))
                            .foregroundColor(.white.opacity(0.7))
                    }

                    // Station info
                    HStack(spacing: 6) {
                        Image(systemName: "antenna.radiowaves.left.and.right")
                            .font(.system(size: 12))
                        Text(favorite.stationName)
                            .font(.system(size: 13, weight: .medium))
                    }
                    .foregroundColor(.blue.opacity(0.8))

                    // Timestamp
                    HStack {
                        Image(systemName: "clock")
                            .font(.system(size: 12))
                        Text(favorite.formattedDate)
                            .font(.system(size: 13))
                    }
                    .foregroundColor(.white.opacity(0.5))
                }
                .padding(.vertical, 12)
                .padding(.leading, 16)
                .frame(maxWidth: .infinity, alignment: .leading)
            }
            .buttonStyle(PlainButtonStyle())

            Button(action: onDelete) {
                Image(systemName: "trash")
                    .font(.system(size: 16))
                    .foregroundColor(.red.opacity(0.7))
                    .padding(.horizontal, 16)
                    .frame(maxHeight: .infinity)
            }
        }
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(Color.white.opacity(0.06))
        )
    }

    // MARK: - Actions

    private func searchOnWeb(artist: String, title: String) {
        let query = "\(artist) \(title)".addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        if let searchURL = URL(string: "https://www.google.com/search?q=\(query)") {
            UIApplication.shared.open(searchURL)
        }
    }
}

#Preview {
    FavoritesView()
        .environmentObject(FavoritesManager())
        .environmentObject(LanguageManager.shared)
}
