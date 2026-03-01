package com.florentin.letzlisten.data

import kotlinx.serialization.Serializable

@Serializable
data class Favorite(
    val id: String,
    val title: String,
    val artist: String,
    val stationId: String,
    val stationName: String,
    val timestamp: Long
)
