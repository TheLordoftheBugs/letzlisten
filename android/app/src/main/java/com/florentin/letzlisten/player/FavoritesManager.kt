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

    fun exportData(): ByteArray? {
        return try { json.encodeToString(_favorites.value).toByteArray(Charsets.UTF_8) } catch (_: Exception) { null }
    }

    // Returns count of newly imported favorites, or -1 on parse error.
    fun importFavorites(data: ByteArray): Int {
        val imported = try {
            json.decodeFromString<List<Favorite>>(String(data, Charsets.UTF_8))
        } catch (_: Exception) { return -1 }
        var count = 0
        val current = _favorites.value.toMutableList()
        for (fav in imported) {
            if (current.none { it.title == fav.title && it.artist == fav.artist }) {
                current.add(fav)
                count++
            }
        }
        if (count > 0) {
            val sorted = current.sortedByDescending { it.timestamp }
            _favorites.value = sorted
            persist(sorted)
        }
        return count
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
