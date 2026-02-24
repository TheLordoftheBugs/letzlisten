//
//  FavoritesView.swift
//  Radio
//
//  View to display and manage favorite songs
//

import SwiftUI

struct FavoritesView: View {
    @EnvironmentObject var favoritesManager: FavoritesManager
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
                        
                        Text("No Favorites Yet")
                            .font(.title2)
                            .fontWeight(.semibold)
                            .foregroundColor(.white)
                        
                        Text("Tap the heart icon while listening to save your favorite songs")
                            .font(.body)
                            .foregroundColor(.white.opacity(0.7))
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 40)
                    }
                } else {
                    // List of favorites
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
            .navigationTitle("Favorites")
            .navigationBarTitleDisplayMode(.large)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Done") {
                        dismiss()
                    }
                    .foregroundColor(.blue)
                }
                
                if !favoritesManager.favorites.isEmpty {
                    ToolbarItem(placement: .navigationBarTrailing) {
                        Button(role: .destructive) {
                            favoritesManager.clearAll()
                        } label: {
                            Text("Clear All")
                                .foregroundColor(.red)
                        }
                    }
                }
            }
            .toolbarBackground(.visible, for: .navigationBar)
            .toolbarBackground(Color(red: 0.08, green: 0.08, blue: 0.12), for: .navigationBar)
        }
    }
}

struct FavoriteRowView: View {
    let favorite: Favorite
    
    var body: some View {
        Button(action: {
            searchOnWeb(artist: favorite.artist, title: favorite.title)
        }) {
            VStack(alignment: .leading, spacing: 12) {
                // Title and Artist (clickable)
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
            .padding(.vertical, 8)
        }
        .buttonStyle(PlainButtonStyle())
    }
    
    // MARK: - Actions
    
    private func searchOnWeb(artist: String, title: String) {
        let query = "\(artist) \(title)".addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        
        // Open Google search for the song
        if let searchURL = URL(string: "https://www.google.com/search?q=\(query)") {
            UIApplication.shared.open(searchURL)
        }
    }
}

#Preview {
    FavoritesView()
        .environmentObject(FavoritesManager())
}
