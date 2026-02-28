package com.florentin.letzlisten.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RadioStation(
    val id: String,
    val name: String,
    @SerialName("streamURL") val streamUrl: String,
    @SerialName("logoImageName") val logoImageName: String? = null,
    @SerialName("websiteURL") val websiteUrl: String? = null,
    @SerialName("isEnabled") val isEnabled: Boolean = true
)

@Serializable
data class StationsResponse(
    val version: String,
    val stations: List<RadioStation>
)
