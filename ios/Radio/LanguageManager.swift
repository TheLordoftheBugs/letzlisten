//
//  LanguageManager.swift
//  Letzebuerg FM
//
//  Manages app language: Lëtzebuergesch (default), Deutsch, English, Français, Português
//

import Foundation
import Combine

class LanguageManager: ObservableObject {
    static let shared = LanguageManager()

    enum Language: String, CaseIterable {
        case luxembourgish = "lb"
        case german = "de"
        case english = "en"
        case french = "fr"
        case portuguese = "pt"

        var displayName: String {
            switch self {
            case .luxembourgish: return "Lëtzebuergesch"
            case .german:        return "Deutsch"
            case .english:       return "English"
            case .french:        return "Français"
            case .portuguese:    return "Português"
            }
        }

        var flag: String {
            switch self {
            case .luxembourgish: return "🇱🇺"
            case .german:        return "🇩🇪"
            case .english:       return "🇬🇧"
            case .french:        return "🇫🇷"
            case .portuguese:    return "🇵🇹"
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
        case .german:        return "Wählen Sie Ihr Radio"
        case .english:       return "Choose Your Radio"
        case .french:        return "Choisissez votre radio"
        case .portuguese:    return "Escolha a sua rádio"
        }
    }

    var back: String {
        switch currentLanguage {
        case .luxembourgish: return "Zréck"
        case .german:        return "Zurück"
        case .english:       return "Back"
        case .french:        return "Retour"
        case .portuguese:    return "Voltar"
        }
    }

    var favorites: String {
        switch currentLanguage {
        case .luxembourgish: return "Favoritten"
        case .german:        return "Favoriten"
        case .english:       return "Favourites"
        case .french:        return "Favoris"
        case .portuguese:    return "Favoritos"
        }
    }

    var noFavoritesYet: String {
        switch currentLanguage {
        case .luxembourgish: return "Nach keng Favoritten"
        case .german:        return "Noch keine Favoriten"
        case .english:       return "No Favourites Yet"
        case .french:        return "Pas encore de favoris"
        case .portuguese:    return "Ainda sem favoritos"
        }
    }

    var noFavoritesHint: String {
        switch currentLanguage {
        case .luxembourgish: return "Tippt op d'Häerz-Ikon fir Är Liblingslidder ze späicheren"
        case .german:        return "Tippen Sie auf das Herz-Symbol, um Ihre Lieblingslieder zu speichern"
        case .english:       return "Tap the heart icon to save your favourite songs"
        case .french:        return "Appuyez sur l'icône cœur pour sauvegarder vos chansons préférées"
        case .portuguese:    return "Toque no ícone de coração para guardar as suas músicas favoritas"
        }
    }

    var done: String {
        switch currentLanguage {
        case .luxembourgish: return "Fäerdeg"
        case .german:        return "Fertig"
        case .english:       return "Done"
        case .french:        return "Terminé"
        case .portuguese:    return "Concluído"
        }
    }

    var clearAll: String {
        switch currentLanguage {
        case .luxembourgish: return "Alles läschen"
        case .german:        return "Alles löschen"
        case .english:       return "Clear All"
        case .french:        return "Tout effacer"
        case .portuguese:    return "Apagar tudo"
        }
    }

    var cancel: String {
        switch currentLanguage {
        case .luxembourgish: return "Ofbriechen"
        case .german:        return "Abbrechen"
        case .english:       return "Cancel"
        case .french:        return "Annuler"
        case .portuguese:    return "Cancelar"
        }
    }

    var confirmClearAll: String {
        switch currentLanguage {
        case .luxembourgish: return "All Favoritten läschen?"
        case .german:        return "Alle Favoriten löschen?"
        case .english:       return "Delete all favourites?"
        case .french:        return "Supprimer tous les favoris ?"
        case .portuguese:    return "Eliminar todos os favoritos?"
        }
    }

    var selectLanguage: String {
        switch currentLanguage {
        case .luxembourgish: return "Sprooch wielen"
        case .german:        return "Sprache wählen"
        case .english:       return "Select Language"
        case .french:        return "Choisir la langue"
        case .portuguese:    return "Selecionar idioma"
        }
    }

    var defaultTitle: String {
        switch currentLanguage {
        case .luxembourgish: return "Titel"
        case .german:        return "Titel"
        case .english:       return "Title"
        case .french:        return "Titre"
        case .portuguese:    return "Título"
        }
    }

    var defaultArtist: String {
        switch currentLanguage {
        case .luxembourgish: return "Kënschtler"
        case .german:        return "Künstler"
        case .english:       return "Artist"
        case .french:        return "Artiste"
        case .portuguese:    return "Artista"
        }
    }

    var settings: String {
        switch currentLanguage {
        case .luxembourgish: return "Astellungen"
        case .german:        return "Einstellungen"
        case .english:       return "Settings"
        case .french:        return "Paramètres"
        case .portuguese:    return "Definições"
        }
    }

    var language: String {
        switch currentLanguage {
        case .luxembourgish: return "Sprooch"
        case .german:        return "Sprache"
        case .english:       return "Language"
        case .french:        return "Langue"
        case .portuguese:    return "Idioma"
        }
    }

    var about: String {
        switch currentLanguage {
        case .luxembourgish: return "Iwwert"
        case .german:        return "Über"
        case .english:       return "About"
        case .french:        return "À propos"
        case .portuguese:    return "Sobre"
        }
    }

    var version: String {
        switch currentLanguage {
        case .luxembourgish: return "Versioun"
        case .german:        return "Version"
        case .english:       return "Version"
        case .french:        return "Version"
        case .portuguese:    return "Versão"
        }
    }

    var playback: String {
        switch currentLanguage {
        case .luxembourgish: return "Nolauschteren"
        case .german:        return "Wiedergabe"
        case .english:       return "Playback"
        case .french:        return "Lecture"
        case .portuguese:    return "Reprodução"
        }
    }

    var continuousPlayback: String {
        switch currentLanguage {
        case .luxembourgish: return "Duerchgehend nolauschteren"
        case .german:        return "Durchgehende Wiedergabe"
        case .english:       return "Continuous Playback"
        case .french:        return "Lecture continue"
        case .portuguese:    return "Reprodução contínua"
        }
    }

    var continuousPlaybackHint: String {
        switch currentLanguage {
        case .luxembourgish: return "Lues weider wann d'Radio gewiesselt gëtt"
        case .german:        return "Wiedergabe beim Stationswechsel fortsetzen"
        case .english:       return "Keep playing when switching stations"
        case .french:        return "Continuer la lecture lors du changement de station"
        case .portuguese:    return "Manter reprodução ao trocar de estação"
        }
    }

    var stationsListVersion: String {
        switch currentLanguage {
        case .luxembourgish: return "Radiolëscht"
        case .german:        return "Senderliste"
        case .english:       return "Stations list"
        case .french:        return "Liste des stations"
        case .portuguese:    return "Lista de estações"
        }
    }

    var stationsUpdated: String {
        switch currentLanguage {
        case .luxembourgish: return "✓ Statiounen aktualiséiert"
        case .german:        return "✓ Sender aktualisiert"
        case .english:       return "✓ Stations updated"
        case .french:        return "✓ Stations mises à jour"
        case .portuguese:    return "✓ Estações atualizadas"
        }
    }

    var stationsUpdateFailed: String {
        switch currentLanguage {
        case .luxembourgish: return "⚠ Aktualiséierung fehlgeschloen"
        case .german:        return "⚠ Aktualisierung fehlgeschlagen"
        case .english:       return "⚠ Update failed"
        case .french:        return "⚠ Échec de la mise à jour"
        case .portuguese:    return "⚠ Falha na atualização"
        }
    }

    var sleepTimer: String {
        switch currentLanguage {
        case .luxembourgish: return "Schlof-Timer"
        case .german:        return "Schlaf-Timer"
        case .english:       return "Sleep Timer"
        case .french:        return "Minuterie"
        case .portuguese:    return "Temporizador"
        }
    }

    var sleepTimerCancel: String {
        switch currentLanguage {
        case .luxembourgish: return "Timer ofbriechen"
        case .german:        return "Timer abbrechen"
        case .english:       return "Cancel Timer"
        case .french:        return "Annuler la minuterie"
        case .portuguese:    return "Cancelar temporizador"
        }
    }

    func shareMessage(artist: String, title: String, station: String, url: String?) -> String {
        let base: String
        switch currentLanguage {
        case .luxembourgish:
            base = "Moien, ech lauschteren elo op \(artist) - \(title) op \(station)."
        case .german:
            base = "Hallo, ich höre gerade \(artist) - \(title) auf \(station)."
        case .english:
            base = "Hey, I'm listening to \(artist) - \(title) on \(station)."
        case .french:
            base = "Salut, j'écoute \(artist) - \(title) sur \(station)."
        case .portuguese:
            base = "Olá, estou a ouvir \(artist) - \(title) na \(station)."
        }
        if let url = url { return "\(base)\n\(url)" }
        return base
    }

    func shareStationMessage(station: String, url: String?) -> String {
        let base: String
        switch currentLanguage {
        case .luxembourgish:
            base = "Moien, ech lauschteren elo Radio: \(station)."
        case .german:
            base = "Hallo, ich höre gerade Radio: \(station)."
        case .english:
            base = "Hey, I'm listening to the radio: \(station)."
        case .french:
            base = "Salut, j'écoute la radio : \(station)."
        case .portuguese:
            base = "Olá, estou a ouvir a rádio: \(station)."
        }
        if let url = url { return "\(base)\n\(url)" }
        return base
    }
}
