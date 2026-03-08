//
//  SettingsView.swift
//  Letzebuerg FM
//
//  Settings sheet: language (dropdown), playback options, app info
//  Secret: tap version 7 times to force-refresh stations from remote
//

import SwiftUI

struct SettingsView: View {
    @EnvironmentObject var languageManager: LanguageManager
    @Environment(\.dismiss) var dismiss

    @AppStorage("continuousPlayback") private var continuousPlayback = true
    @ObservedObject private var stationLoader = RadioStationLoader.shared

    @State private var versionTapCount = 0
    @State private var secretFeedback: String? = nil
    @State private var isRefreshing = false

    private var appVersion: String {
        Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "—"
    }
    private var buildNumber: String {
        Bundle.main.infoDictionary?["CFBundleVersion"] as? String ?? "—"
    }

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
                    VStack(alignment: .leading, spacing: 24) {

                        // MARK: Paramètres — langue + lecture dans une seule carte
                        sectionHeader(languageManager.settings)

                        VStack(spacing: 0) {
                            // Langue
                            HStack {
                                Text(languageManager.language)
                                    .font(.system(size: 17, weight: .medium))
                                    .foregroundColor(.white)
                                Spacer()
                                Picker("", selection: $languageManager.currentLanguage) {
                                    ForEach(LanguageManager.Language.allCases, id: \.rawValue) { lang in
                                        Text("\(lang.flag)  \(lang.displayName)").tag(lang)
                                    }
                                }
                                .pickerStyle(.menu)
                                .accentColor(.blue)
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

                        // MARK: About — version + build, 7-tap secret
                        sectionHeader(languageManager.about)

                        VStack(spacing: 0) {
                            // App version — tappable (secret mode)
                            HStack {
                                Text(languageManager.version)
                                    .font(.system(size: 17, weight: .medium))
                                    .foregroundColor(.white)
                                Spacer()
                                if isRefreshing {
                                    ProgressView()
                                        .progressViewStyle(CircularProgressViewStyle(tint: .white.opacity(0.5)))
                                        .scaleEffect(0.8)
                                } else if let feedback = secretFeedback {
                                    Text(feedback)
                                        .font(.system(size: 14))
                                        .foregroundColor(.white.opacity(0.7))
                                        .transition(.opacity)
                                } else {
                                    Text("\(appVersion) (Build \(buildNumber))")
                                        .font(.system(size: 17))
                                        .foregroundColor(.white.opacity(0.5))
                                }
                            }
                            .padding(.vertical, 13)
                            .padding(.horizontal, 16)
                            .contentShape(Rectangle())
                            .onTapGesture { handleVersionTap() }

                            Divider()
                                .background(Color.white.opacity(0.1))
                                .padding(.horizontal, 16)

                            // Stations list version — updates on bundle load and remote fetch
                            HStack {
                                Text(languageManager.stationsListVersion)
                                    .font(.system(size: 17, weight: .medium))
                                    .foregroundColor(.white)
                                Spacer()
                                Text(stationLoader.stationsVersion.isEmpty ? "—" : stationLoader.stationsVersion)
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

                        // Secret tap progress dots (visible after first tap)
                        if versionTapCount > 0 {
                            HStack(spacing: 6) {
                                Spacer()
                                ForEach(0..<7, id: \.self) { i in
                                    Circle()
                                        .fill(i < versionTapCount ? Color.blue : Color.white.opacity(0.2))
                                        .frame(width: 6, height: 6)
                                }
                                Spacer()
                            }
                            .transition(.opacity)
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, 20)
                    .padding(.bottom, 40)
                    .animation(.easeInOut(duration: 0.2), value: versionTapCount)
                    .animation(.easeInOut(duration: 0.3), value: secretFeedback)
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
        }
        .presentationDetents([.large])
        .presentationDragIndicator(.visible)
    }

    // MARK: - Secret mode

    private func handleVersionTap() {
        guard secretFeedback == nil, !isRefreshing else { return }
        versionTapCount += 1
        if versionTapCount >= 7 {
            versionTapCount = 0
            triggerRemoteRefresh()
        }
    }

    private func triggerRemoteRefresh() {
        isRefreshing = true
        RadioStationLoader.shared.fetchFromRemote { success in
            isRefreshing = false
            withAnimation {
                secretFeedback = success
                    ? languageManager.stationsUpdated
                    : languageManager.stationsUpdateFailed
            }
            DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                withAnimation { secretFeedback = nil }
            }
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
}
