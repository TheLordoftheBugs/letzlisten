package com.florentin.letzlisten.data

import android.content.Context
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

// Remote URL for live station updates (same source as iOS)
private const val REMOTE_STATIONS_URL =
    "https://raw.githubusercontent.com/TheLordoftheBugs/letzlisten/main/stations.json"

object StationsRepository {

    /**
     * Loads stations from the bundled assets/stations.json.
     * Falls back gracefully if the file is missing.
     */
    fun loadFromAssets(context: Context): List<RadioStation> {
        return try {
            val raw = context.assets.open("stations.json").bufferedReader().readText()
            json.decodeFromString<StationsResponse>(raw).stations
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Fetches the latest stations from the remote GitHub source.
     * Call this from a coroutine / background thread.
     */
    suspend fun fetchRemote(): List<RadioStation> {
        return try {
            val raw = java.net.URL(REMOTE_STATIONS_URL).readText()
            json.decodeFromString<StationsResponse>(raw).stations
        } catch (e: Exception) {
            emptyList()
        }
    }
}
