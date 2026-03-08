//
//  SettingsView.swift
//  Letzebuerg FM
//
//  Settings sheet: language selection and app info
//

import SwiftUI

struct SettingsView: View {
    @EnvironmentObject var languageManager: LanguageManager
    @Environment(\.dismiss) var dismiss

    private var appVersion: String {
        Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "—"
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

                        // MARK: Language section
                        sectionHeader(languageManager.language)

                        VStack(spacing: 8) {
                            ForEach(LanguageManager.Language.allCases, id: \.rawValue) { lang in
                                Button(action: {
                                    languageManager.currentLanguage = lang
                                }) {
                                    HStack(spacing: 16) {
                                        Text(lang.flag)
                                            .font(.system(size: 28))
                                        Text(lang.displayName)
                                            .font(.system(size: 17, weight: .medium))
                                            .foregroundColor(.white)
                                        Spacer()
                                        if lang == languageManager.currentLanguage {
                                            Image(systemName: "checkmark")
                                                .foregroundColor(.blue)
                                                .font(.system(size: 15, weight: .semibold))
                                        }
                                    }
                                    .padding(.vertical, 13)
                                    .padding(.horizontal, 16)
                                    .background(
                                        RoundedRectangle(cornerRadius: 12)
                                            .fill(lang == languageManager.currentLanguage
                                                  ? Color.blue.opacity(0.2)
                                                  : Color.white.opacity(0.06))
                                    )
                                }
                            }
                        }

                        // MARK: About section
                        sectionHeader(languageManager.about)

                        HStack {
                            Text(languageManager.version)
                                .font(.system(size: 17, weight: .medium))
                                .foregroundColor(.white)
                            Spacer()
                            Text(appVersion)
                                .font(.system(size: 17))
                                .foregroundColor(.white.opacity(0.5))
                        }
                        .padding(.vertical, 13)
                        .padding(.horizontal, 16)
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
        }
        .presentationDetents([.large])
        .presentationDragIndicator(.visible)
    }

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
