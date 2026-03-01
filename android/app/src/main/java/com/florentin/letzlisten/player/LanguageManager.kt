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

    val defaultTitle: String get() = when (_currentLanguage.value) {
        AppLanguage.LB -> "Titel"
        AppLanguage.FR -> "Titre"
        AppLanguage.DE -> "Titel"
        AppLanguage.EN -> "Title"
        AppLanguage.PT -> "Título"
    }

    val defaultArtist: String get() = when (_currentLanguage.value) {
        AppLanguage.LB -> "Kënschtler"
        AppLanguage.FR -> "Artiste"
        AppLanguage.DE -> "Künstler"
        AppLanguage.EN -> "Artist"
        AppLanguage.PT -> "Artista"
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

    fun shareMessage(artist: String, title: String, station: String, url: String?): String {
        val suffix = url?.let { "\n$it" } ?: ""
        return when (_currentLanguage.value) {
            AppLanguage.LB -> "🎵 $title - $artist\n▶ Op $station$suffix"
            AppLanguage.FR -> "🎵 $title - $artist\n▶ Sur $station$suffix"
            AppLanguage.DE -> "🎵 $title - $artist\n▶ Auf $station$suffix"
            AppLanguage.EN -> "🎵 $title - $artist\n▶ On $station$suffix"
            AppLanguage.PT -> "🎵 $title - $artist\n▶ Em $station$suffix"
        }
    }
}
