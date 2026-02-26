//
//  LanguageManager.swift
//  Lëtz Listen
//
//  Manages app language: Lëtzebuergesch (default), Français, Deutsch, English
//

import Foundation
import Combine

class LanguageManager: ObservableObject {
    static let shared = LanguageManager()

    enum Language: String, CaseIterable {
        case german = "de"
        case english = "en"
        case french = "fr"
        case luxembourgish = "lb"

        var displayName: String {
            switch self {
            case .luxembourgish: return "Lëtzebuergesch"
            case .french:        return "Français"
            case .german:        return "Deutsch"
            case .english:       return "English"
            }
        }

        var flag: String {
            switch self {
            case .luxembourgish: return "🇱🇺"
            case .french:        return "🇫🇷"
            case .german:        return "🇩🇪"
            case .english:       return "🇬🇧"
            }
        }
    }

    @Published var currentLanguage: Language {
        didSet {
            UserDefaults.standard.set(currentLanguage.rawValue, forKey: "AppLanguage")
        }
    }

    private init() {
        let saved = UserDefaults.standard.string(forKey: "AppLanguage") ?? "lb"
        currentLanguage = Language(rawValue: saved) ?? .luxembourgish
    }

    // MARK: - Translations

    var chooseYourRadio: String {
        switch currentLanguage {
        case .luxembourgish: return "Wielt Är Radio"
        case .french:        return "Choisissez votre radio"
        case .german:        return "Wählen Sie Ihr Radio"
        case .english:       return "Choose Your Radio"
        }
    }

    var back: String {
        switch currentLanguage {
        case .luxembourgish: return "Zréck"
        case .french:        return "Retour"
        case .german:        return "Zurück"
        case .english:       return "Back"
        }
    }

    var favorites: String {
        switch currentLanguage {
        case .luxembourgish: return "Favoritten"
        case .french:        return "Favoris"
        case .german:        return "Favoriten"
        case .english:       return "Favourites"
        }
    }

    var noFavoritesYet: String {
        switch currentLanguage {
        case .luxembourgish: return "Nach keng Favoritten"
        case .french:        return "Pas encore de favoris"
        case .german:        return "Noch keine Favoriten"
        case .english:       return "No Favourites Yet"
        }
    }

    var noFavoritesHint: String {
        switch currentLanguage {
        case .luxembourgish: return "Tippt op d'Häerz-Ikon fir Är Liblingslidder ze späicheren"
        case .french:        return "Appuyez sur l'icône cœur pour sauvegarder vos chansons préférées"
        case .german:        return "Tippen Sie auf das Herz-Symbol, um Ihre Lieblingslieder zu speichern"
        case .english:       return "Tap the heart icon to save your favourite songs"
        }
    }

    var done: String {
        switch currentLanguage {
        case .luxembourgish: return "Fäerdeg"
        case .french:        return "Terminé"
        case .german:        return "Fertig"
        case .english:       return "Done"
        }
    }

    var clearAll: String {
        switch currentLanguage {
        case .luxembourgish: return "Alles läschen"
        case .french:        return "Tout effacer"
        case .german:        return "Alles löschen"
        case .english:       return "Clear All"
        }
    }

    var selectLanguage: String {
        switch currentLanguage {
        case .luxembourgish: return "Sprooch wielen"
        case .french:        return "Choisir la langue"
        case .german:        return "Sprache wählen"
        case .english:       return "Select Language"
        }
    }

    var defaultTitle: String {
        switch currentLanguage {
        case .luxembourgish: return "Titel"
        case .french:        return "Titre"
        case .german:        return "Titel"
        case .english:       return "Title"
        }
    }

    var defaultArtist: String {
        switch currentLanguage {
        case .luxembourgish: return "Kënschtler"
        case .french:        return "Artiste"
        case .german:        return "Künstler"
        case .english:       return "Artist"
        }
    }

    func shareMessage(artist: String, title: String, station: String, url: String?) -> String {
        let base: String
        switch currentLanguage {
        case .luxembourgish:
            base = "Moien, ech lauschteren elo op \(artist) - \(title) op \(station)."
        case .french:
            base = "Salut, j'écoute \(artist) - \(title) sur \(station)."
        case .german:
            base = "Hallo, ich höre gerade \(artist) - \(title) auf \(station)."
        case .english:
            base = "Hey, I'm listening to \(artist) - \(title) on \(station)."
        }
        if let url = url { return "\(base)\n\(url)" }
        return base
    }
}
