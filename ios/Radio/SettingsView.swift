//
//  SettingsView.swift
//  Letzebuerg FM
//
//  Settings sheet: language (dropdown), playback options, app info
//

import SwiftUI

struct SettingsView: View {
    @EnvironmentObject var languageManager: LanguageManager
    @Environment(\.dismiss) var dismiss

    @AppStorage("continuousPlayback") private var continuousPlayback = true

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
                ToolbarItem(placement: .navigationBarLeading) {
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
