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
