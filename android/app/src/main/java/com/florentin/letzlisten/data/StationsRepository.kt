package com.florentin.letzlisten.data

import android.content.Context
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

object StationsRepository {

    fun loadFromAssets(context: Context): List<RadioStation> {
        return try {
            val raw = context.assets.open("stations.json").bufferedReader().readText()
            json.decodeFromString<StationsResponse>(raw).stations
        } catch (e: Exception) {
            emptyList()
        }
    }
}
