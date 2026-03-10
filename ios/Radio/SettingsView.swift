//
//  SettingsView.swift
//  Letzebuerg FM
//
//  Settings sheet: language (dropdown), playback options, app info, favorites export/import
//

import SwiftUI
import UniformTypeIdentifiers

struct SettingsView: View {
    @EnvironmentObject var languageManager: LanguageManager
    @EnvironmentObject var favoritesManager: FavoritesManager
    @Environment(\.dismiss) var dismiss

    @AppStorage("continuousPlayback") private var continuousPlayback = true

    @State private var showFileImporter = false
    @State private var importFeedback: String? = nil
    @State private var showConfirmClearAll = false

    private var appVersion: String {
        Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "—"
    }
    private var buildNumber: String {
        Bundle.main.infoDictionary?["CFBundleVersion"] as? String ?? "—"
    }

    var body: some View {
        NavigationStack {
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
                    VStack(alignment: .leading, spacing: 24) {

                        // MARK: Paramètres — langue + lecture dans une seule carte
                        sectionHeader(languageManager.settings)

                        VStack(spacing: 0) {
                            // Langue
                            HStack {
                                Text(languageManager.language)
                                    .font(.system(size: 17, weight: .medium))
                                    .foregroundColor(.white)
                                    .lineLimit(1)
                                Spacer(minLength: 8)
                                Picker("", selection: $languageManager.currentLanguage) {
                                    ForEach(LanguageManager.Language.allCases, id: \.rawValue) { lang in
                                        Text("\(lang.flag)  \(lang.displayName)").tag(lang)
                                    }
                                }
                                .pickerStyle(.menu)
                                .accentColor(.blue)
                                .fixedSize()
                            }
                            .padding(.vertical, 13)
                            .padding(.horizontal, 16)

                            Divider()
                                .background(Color.white.opacity(0.1))
                                .padding(.horizontal, 16)

                            // Lecture continue
                            Toggle(isOn: $continuousPlayback) {
                                VStack(alignment: .leading, spacing: 3) {
                                    Text(languageManager.continuousPlayback)
                                        .font(.system(size: 17, weight: .medium))
                                        .foregroundColor(.white)
                                    Text(languageManager.continuousPlaybackHint)
                                        .font(.system(size: 13))
                                        .foregroundColor(.white.opacity(0.5))
                                        .fixedSize(horizontal: false, vertical: true)
                                }
                            }
                            .tint(.blue)
                            .padding(.vertical, 13)
                            .padding(.horizontal, 16)
                        }
                        .background(
                            RoundedRectangle(cornerRadius: 12)
                                .fill(Color.white.opacity(0.06))
                        )

                        // MARK: Favoris — export / import
                        sectionHeader(languageManager.favorites)

                        VStack(spacing: 0) {
                            // Export
                            if let url = makeExportFile(), !favoritesManager.favorites.isEmpty {
                                ShareLink(item: url) {
                                    HStack {
                                        Text(languageManager.exportFavorites)
                                            .font(.system(size: 17, weight: .medium))
                                            .foregroundColor(.white)
                                        Spacer()
                                        Image(systemName: "square.and.arrow.up")
                                            .foregroundColor(.blue)
                                    }
                                    .padding(.vertical, 13)
                                    .padding(.horizontal, 16)
                                }
                            } else {
                                HStack {
                                    Text(languageManager.exportFavorites)
                                        .font(.system(size: 17, weight: .medium))
                                        .foregroundColor(.white.opacity(0.3))
                                    Spacer()
                                    Image(systemName: "square.and.arrow.up")
                                        .foregroundColor(.blue.opacity(0.3))
                                }
                                .padding(.vertical, 13)
                                .padding(.horizontal, 16)
                            }

                            Divider()
                                .background(Color.white.opacity(0.1))
                                .padding(.horizontal, 16)

                            // Import
                            Button(action: { showFileImporter = true }) {
                                HStack {
                                    Text(languageManager.importFavorites)
                                        .font(.system(size: 17, weight: .medium))
                                        .foregroundColor(.white)
                                    Spacer()
                                    Image(systemName: "square.and.arrow.down")
                                        .foregroundColor(.blue)
                                }
                                .padding(.vertical, 13)
                                .padding(.horizontal, 16)
                            }

                            Divider()
                                .background(Color.white.opacity(0.1))
                                .padding(.horizontal, 16)

                            // Tout supprimer
                            Button(action: { showConfirmClearAll = true }) {
                                HStack {
                                    Text(languageManager.clearAll)
                                        .font(.system(size: 17, weight: .medium))
                                        .foregroundColor(favoritesManager.favorites.isEmpty ? .red.opacity(0.4) : .red)
                                    Spacer()
                                }
                                .padding(.vertical, 13)
                                .padding(.horizontal, 16)
                            }
                            .disabled(favoritesManager.favorites.isEmpty)

                            // Feedback import
                            if let feedback = importFeedback {
                                Divider()
                                    .background(Color.white.opacity(0.1))
                                    .padding(.horizontal, 16)

                                Text(feedback)
                                    .font(.system(size: 14))
                                    .foregroundColor(.white.opacity(0.7))
                                    .padding(.vertical, 10)
                                    .padding(.horizontal, 16)
                                    .transition(.opacity)
                            }
                        }
                        .background(
                            RoundedRectangle(cornerRadius: 12)
                                .fill(Color.white.opacity(0.06))
                        )
                        .animation(.easeInOut(duration: 0.2), value: importFeedback)

                        // MARK: About — version + build
                        sectionHeader(languageManager.about)

                        VStack(spacing: 0) {
                            HStack {
                                Text(languageManager.version)
                                    .font(.system(size: 17, weight: .medium))
                                    .foregroundColor(.white)
                                Spacer()
                                Text("\(appVersion) (Build \(buildNumber))")
                                    .font(.system(size: 17))
                                    .foregroundColor(.white.opacity(0.5))
                            }
                            .padding(.vertical, 13)
                            .padding(.horizontal, 16)
                        }
                        .background(
                            RoundedRectangle(cornerRadius: 12)
                                .fill(Color.white.opacity(0.06))
                        )
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, 20)
                    .padding(.bottom, 40)
                }
            }
            .navigationTitle(languageManager.settings)
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
            .alert(languageManager.confirmClearAll, isPresented: $showConfirmClearAll) {
                Button(languageManager.clearAll, role: .destructive) {
                    favoritesManager.clearAll()
                }
                Button(languageManager.cancel, role: .cancel) {}
            }
            .fileImporter(
                isPresented: $showFileImporter,
                allowedContentTypes: [UTType.json],
                allowsMultipleSelection: false
            ) { result in
                handleImport(result: result)
            }
        }
        .presentationDetents([.large])
        .presentationDragIndicator(.visible)
    }

    // MARK: - Actions

    private func makeExportFile() -> URL? {
        guard let data = favoritesManager.exportData() else { return nil }
        let url = FileManager.default.temporaryDirectory
            .appendingPathComponent("favoris-letzlisten.json")
        try? data.write(to: url)
        return url
    }

    private func handleImport(result: Result<[URL], Error>) {
        guard case .success(let urls) = result, let url = urls.first else { return }
        let accessing = url.startAccessingSecurityScopedResource()
        defer { if accessing { url.stopAccessingSecurityScopedResource() } }
        guard let data = try? Data(contentsOf: url) else {
            showImportFeedback(languageManager.importFailed)
            return
        }
        let count = favoritesManager.importFavorites(from: data)
        if count < 0 {
            showImportFeedback(languageManager.importFailed)
        } else {
            showImportFeedback(languageManager.importSuccess(count: count))
        }
    }

    private func showImportFeedback(_ message: String) {
        withAnimation { importFeedback = message }
        DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
            withAnimation { importFeedback = nil }
        }
    }

    // MARK: - Helpers

    @ViewBuilder
    private func sectionHeader(_ title: String) -> some View {
        Text(title.uppercased())
            .font(.system(size: 12, weight: .semibold))
            .foregroundColor(.white.opacity(0.45))
            .padding(.leading, 4)
            .padding(.bottom, -8)
    }
}


#Preview {
    SettingsView()
        .environmentObject(LanguageManager.shared)
        .environmentObject(FavoritesManager())
}
