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
    EN("en", "🇬🇧", "English")
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

    fun shareMessage(artist: String, title: String, station: String, url: String?): String {
        val suffix = url?.let { "\n$it" } ?: ""
        return when (_currentLanguage.value) {
            AppLanguage.LB -> "🎵 $title - $artist\n▶ Op $station$suffix"
            AppLanguage.FR -> "🎵 $title - $artist\n▶ Sur $station$suffix"
            AppLanguage.DE -> "🎵 $title - $artist\n▶ Auf $station$suffix"
            AppLanguage.EN -> "🎵 $title - $artist\n▶ On $station$suffix"
        }
    }
}
