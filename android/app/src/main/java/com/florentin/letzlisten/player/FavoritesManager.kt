package com.florentin.letzlisten.player

import android.content.Context
import com.florentin.letzlisten.data.Favorite
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class FavoritesManager(context: Context) {

    private val prefs = context.getSharedPreferences("letzlisten_favorites", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    private val _favorites = MutableStateFlow(load())
    val favorites: StateFlow<List<Favorite>> = _favorites.asStateFlow()

    fun add(title: String, artist: String, stationId: String, stationName: String) {
        if (_favorites.value.any { it.title == title && it.artist == artist }) return
        val fav = Favorite(
            id = UUID.randomUUID().toString(),
            title = title,
            artist = artist,
            stationId = stationId,
            stationName = stationName,
            timestamp = System.currentTimeMillis()
        )
        val updated = listOf(fav) + _favorites.value
        _favorites.value = updated
        persist(updated)
    }

    fun remove(id: String) {
        val updated = _favorites.value.filterNot { it.id == id }
        _favorites.value = updated
        persist(updated)
    }

    fun clearAll() {
        _favorites.value = emptyList()
        prefs.edit().remove("data").apply()
    }

    fun isFavorited(title: String, artist: String) =
        _favorites.value.any { it.title == title && it.artist == artist }

    private fun load(): List<Favorite> {
        val raw = prefs.getString("data", null) ?: return emptyList()
        return try { json.decodeFromString(raw) } catch (e: Exception) { emptyList() }
    }

    private fun persist(list: List<Favorite>) {
        prefs.edit().putString("data", json.encodeToString(list)).apply()
    }
}
