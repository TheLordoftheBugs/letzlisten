package com.florentin.letzlisten.player

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLanguage(val code: String, val flag: String, val displayName: String) {
    LB("lb", "🇱🇺", "Lëtzebuergesch"),
    FR("fr", "🇫🇷", "Français"),
    DE("de", "🇩🇪", "Deutsch"),
    EN("en", "🇬🇧", "English"),
    PT("pt", "🇵🇹", "Português")
}

class LanguageManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("letzlisten_prefs", Context.MODE_PRIVATE)

    private val _currentLanguage = MutableStateFlow(
        AppLanguage.values().find { it.code == prefs.getString("language", "lb") } ?: AppLanguage.LB
    )
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        prefs.edit().putString("language", language.code).apply()
    }

    val favorites: String get() = when (_currentLanguage.value) {
        AppLanguage.LB -> "Favoritten"
        AppLanguage.FR -> "Favoris"
        AppLanguage.DE -> "Favoriten"
        AppLanguage.EN -> "Favourites"
        AppLanguage.PT -> "Favoritos"
    }

    val noFavoritesYet: String get() = when (_currentLanguage.value) {
        AppLanguage.LB -> "Nach keng Favoritten"
        AppLanguage.FR -> "Pas encore de favoris"
        AppLanguage.DE -> "Noch keine Favoriten"
        AppLanguage.EN -> "No Favourites Yet"
        AppLanguage.PT -> "Ainda sem favoritos"
    }

    val noFavoritesHint: String get() = when (_currentLanguage.value) {
        AppLanguage.LB -> "Tippt op d'Häerz-Ikon fir Är Liblingslidder ze späicheren"
        AppLanguage.FR -> "Appuyez sur l'icône cœur pour sauvegarder vos chansons préférées"
        AppLanguage.DE -> "Tippen Sie auf das Herz-Symbol, um Ihre Lieblingslieder zu speichern"
        AppLanguage.EN -> "Tap the heart icon to save your favourite songs"
        AppLanguage.PT -> "Toque no ícone de coração para guardar as suas músicas favoritas"
    }

    val clearAll: String get() = when (_currentLanguage.value) {
        AppLanguage.LB -> "Alles läschen"
        AppLanguage.FR -> "Tout effacer"
        AppLanguage.DE -> "Alles löschen"
        AppLanguage.EN -> "Clear All"
        AppLanguage.PT -> "Limpar tudo"
    }

    val chooseYourRadio: String get() = when (_currentLanguage.value) {
        AppLanguage.LB -> "Wielt Är Radio"
        AppLanguage.FR -> "Choisissez votre radio"
        AppLanguage.DE -> "Wählen Sie Ihr Radio"
        AppLanguage.EN -> "Choose Your Radio"
        AppLanguage.PT -> "Escolha a sua rádio"
    }

    val selectLanguage: String get() = when (_currentLanguage.value) {
        AppLanguage.LB -> "Sprooch wielen"
        AppLanguage.FR -> "Choisir la langue"
        AppLanguage.DE -> "Sprache wählen"
        AppLanguage.EN -> "Select Language"
        AppLanguage.PT -> "Selecionar idioma"
    }

    val cancel: String get() = when (_currentLanguage.value) {
        AppLanguage.LB -> "Ofbriechen"
        AppLanguage.FR -> "Annuler"
        AppLanguage.DE -> "Abbrechen"
        AppLanguage.EN -> "Cancel"
        AppLanguage.PT -> "Cancelar"
    }

    val confirmClearAll: String get() = when (_currentLanguage.value) {
        AppLanguage.LB -> "All Favoritten läschen?"
        AppLanguage.FR -> "Supprimer tous les favoris ?"
        AppLanguage.DE -> "Alle Favoriten löschen?"
        AppLanguage.EN -> "Delete all favourites?"
        AppLanguage.PT -> "Eliminar todos os favoritos?"
    }

    val done: String get() = when (_currentLanguage.value) {
        AppLanguage.LB -> "Fäerdeg"
        AppLanguage.FR -> "Terminé"
        AppLanguage.DE -> "Fertig"
        AppLanguage.EN -> "Done"
        AppLanguage.PT -> "Concluído"
    }

    val settings: String get() = when (_currentLanguage.value) {
        AppLanguage.LB -> "Astellungen"
        AppLanguage.FR -> "Paramètres"
        AppLanguage.DE -> "Einstellungen"
        AppLanguage.EN -> "Settings"
        AppLanguage.PT -> "Definições"
    }

    val language: String get() = when (_currentLanguage.value) {
        AppLanguage.LB -> "Sprooch"
        AppLanguage.FR -> "Langue"
        AppLanguage.DE -> "Sprache"
        AppLanguage.EN -> "Language"
        AppLanguage.PT -> "Idioma"
    }

    val about: String get() = when (_currentLanguage.value) {
        AppLanguage.LB -> "Iwwert"
        AppLanguage.FR -> "À propos"
        AppLanguage.DE -> "Über"
        AppLanguage.EN -> "About"
        AppLanguage.PT -> "Sobre"
    }

    val version: String get() = when (_currentLanguage.value) {
        AppLanguage.LB -> "Versioun"
        AppLanguage.FR -> "Version"
        AppLanguage.DE -> "Version"
        AppLanguage.EN -> "Version"
        AppLanguage.PT -> "Versão"
    }

    val continuousPlayback: String get() = when (_currentLanguage.value) {
        AppLanguage.LB -> "Duerchgehend nolauschteren"
        AppLanguage.FR -> "Lecture continue"
        AppLanguage.DE -> "Durchgehende Wiedergabe"
        AppLanguage.EN -> "Continuous Playback"
        AppLanguage.PT -> "Reprodução contínua"
    }

    val continuousPlaybackHint: String get() = when (_currentLanguage.value) {
        AppLanguage.LB -> "Lues weider wann d'Radio gewiesselt gëtt"
        AppLanguage.FR -> "Continuer la lecture lors du changement de station"
        AppLanguage.DE -> "Wiedergabe beim Stationswechsel fortsetzen"
        AppLanguage.EN -> "Keep playing when switching stations"
        AppLanguage.PT -> "Manter reprodução ao trocar de estação"
    }

    val exportFavorites: String get() = when (_currentLanguage.value) {
        AppLanguage.LB -> "Exportéieren"
        AppLanguage.FR -> "Exporter"
        AppLanguage.DE -> "Exportieren"
        AppLanguage.EN -> "Export"
        AppLanguage.PT -> "Exportar"
    }

    val importFavorites: String get() = when (_currentLanguage.value) {
        AppLanguage.LB -> "Importéieren"
        AppLanguage.FR -> "Importer"
        AppLanguage.DE -> "Importieren"
        AppLanguage.EN -> "Import"
        AppLanguage.PT -> "Importar"
    }

    val importFailed: String get() = when (_currentLanguage.value) {
        AppLanguage.LB -> "⚠ Fichier net valabel"
        AppLanguage.FR -> "⚠ Fichier invalide"
        AppLanguage.DE -> "⚠ Ungültige Datei"
        AppLanguage.EN -> "⚠ Invalid file"
        AppLanguage.PT -> "⚠ Arquivo inválido"
    }

    fun importSuccess(count: Int): String = when (_currentLanguage.value) {
        AppLanguage.LB -> "✓ $count Favorit${if (count == 1) "" else "en"} importéiert"
        AppLanguage.FR -> "✓ $count favori${if (count == 1) "" else "s"} importé${if (count == 1) "" else "s"}"
        AppLanguage.DE -> "✓ $count Favorit${if (count == 1) "" else "en"} importiert"
        AppLanguage.EN -> "✓ $count favourite${if (count == 1) "" else "s"} imported"
        AppLanguage.PT -> "✓ $count favorito${if (count == 1) "" else "s"} importado${if (count == 1) "" else "s"}"
    }

    fun exportSuccess(count: Int): String = when (_currentLanguage.value) {
        AppLanguage.LB -> "✓ $count Favorit${if (count == 1) "" else "en"} exportéiert"
        AppLanguage.FR -> "✓ $count favori${if (count == 1) "" else "s"} exporté${if (count == 1) "" else "s"}"
        AppLanguage.DE -> "✓ $count Favorit${if (count == 1) "" else "en"} exportiert"
        AppLanguage.EN -> "✓ $count favourite${if (count == 1) "" else "s"} exported"
        AppLanguage.PT -> "✓ $count favorito${if (count == 1) "" else "s"} exportado${if (count == 1) "" else "s"}"
    }

    fun shareMessage(artist: String, title: String, station: String, url: String?): String {
        val base = when (_currentLanguage.value) {
            AppLanguage.LB -> "Moien, ech lauschteren elo op $artist - $title op $station."
            AppLanguage.FR -> "Salut, j'écoute $artist - $title sur $station."
            AppLanguage.DE -> "Hallo, ich höre gerade $artist - $title auf $station."
            AppLanguage.EN -> "Hey, I'm listening to $artist - $title on $station."
            AppLanguage.PT -> "Olá, estou a ouvir $artist - $title em $station."
        }
        return if (url != null) "$base\n$url" else base
    }
}
