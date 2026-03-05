package com.florentin.letzlisten.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.florentin.letzlisten.data.StationsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class ItunesResponse(val results: List<ItunesTrack> = emptyList())

@Serializable
private data class ItunesTrack(val artworkUrl100: String? = null)

private val itunesJson = Json { ignoreUnknownKeys = true }

data class TrackInfo(
    val title: String = "",
    val artist: String = ""
) {
    val isUnknown: Boolean get() = title.isBlank() && artist.isBlank()
}

class RadioViewModel(application: Application) : AndroidViewModel(application) {

    private val exoPlayer = ExoPlayer.Builder(application).build()
    private val favoritesManager = FavoritesManager(application)

    private val _stations = MutableStateFlow<List<com.florentin.letzlisten.data.RadioStation>>(emptyList())
    val stations: StateFlow<List<com.florentin.letzlisten.data.RadioStation>> = _stations.asStateFlow()

    private val _currentStation = MutableStateFlow<com.florentin.letzlisten.data.RadioStation?>(null)
    val currentStation: StateFlow<com.florentin.letzlisten.data.RadioStation?> = _currentStation.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // True once playback has started at least once for the current station; resets on station change.
    private val _hasStartedPlaying = MutableStateFlow(false)
    val hasStartedPlaying: StateFlow<Boolean> = _hasStartedPlaying.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // True while the remote stations.json is being fetched from GitHub (mirrors iOS RadioStationLoader.isLoading).
    private val _isStationsLoading = MutableStateFlow(false)
    val isStationsLoading: StateFlow<Boolean> = _isStationsLoading.asStateFlow()

    private val _currentTrack = MutableStateFlow(TrackInfo())
    val currentTrack: StateFlow<TrackInfo> = _currentTrack.asStateFlow()

    private val _albumArtUrl = MutableStateFlow<String?>(null)
    val albumArtUrl: StateFlow<String?> = _albumArtUrl.asStateFlow()

    private val itunesCache = mutableMapOf<String, String?>()

    val favorites = favoritesManager.favorites

    val isFavorited: StateFlow<Boolean> = combine(favoritesManager.favorites, _currentTrack) { favs, track ->
        if (track.isUnknown) false
        else favs.any { it.title == track.title && it.artist == track.artist }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    init {
        loadStations()
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) _hasStartedPlaying.value = true
            }
            override fun onPlaybackStateChanged(state: Int) {
                _isLoading.value = state == Player.STATE_BUFFERING
            }
            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                val raw = mediaMetadata.title?.toString() ?: return
                val parts = raw.split(" - ", limit = 2)
                val artist = if (parts.size == 2) parts[0].trim() else ""
                val title = if (parts.size == 2) parts[1].trim() else raw.trim()

                val filteredTitle = filterMetadata(title) ?: return
                val filteredArtist = filterMetadata(artist.ifBlank { null }) ?: ""

                val newTrack = TrackInfo(title = filteredTitle, artist = filteredArtist)
                if (_currentTrack.value == newTrack) return

                _currentTrack.value = newTrack
                _albumArtUrl.value = null
                fetchAlbumArt(filteredArtist, filteredTitle)
            }
        })
    }

    private fun filterMetadata(value: String?): String? {
        val trimmed = value?.trim() ?: return null
        if (trimmed.length < 2) return null
        val lower = trimmed.lowercase()
        val junk = listOf("unknown", "unknow", "n/a", "na", "-", "...")
        if (lower in junk) return null
        if (lower.contains("on air")) return null
        if (lower.contains("fm") && (lower.contains("96.6") || lower.contains("100.7") || lower.contains("105"))) return null
        if (lower.contains("rgl") && lower.contains("fm")) return null
        if (lower.contains("eldoradio") && lower.contains("fm")) return null
        if (lower.contains("radio") && lower.contains("fm")) return null
        return trimmed
    }

    private fun fetchAlbumArt(artist: String, title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val key = "$artist-$title".lowercase()
            if (itunesCache.containsKey(key)) {
                _albumArtUrl.value = itunesCache[key]
                return@launch
            }
            try {
                val query = java.net.URLEncoder.encode("$artist $title", "UTF-8")
                val raw = java.net.URL(
                    "https://itunes.apple.com/search?term=$query&media=music&entity=song&limit=1"
                ).readText()
                val url = itunesJson.decodeFromString<ItunesResponse>(raw)
                    .results.firstOrNull()?.artworkUrl100
                    ?.replace("100x100bb", "600x600bb")
                itunesCache[key] = url
                _albumArtUrl.value = url
            } catch (_: Exception) {
                itunesCache[key] = null
            }
        }
    }

    private fun loadStations() {
        viewModelScope.launch {
            val local = StationsRepository.loadFromAssets(getApplication())
            _stations.value = local.filter { it.isEnabled }.sortedBy { it.name }
            _stations.value.firstOrNull()?.let { selectStation(it) }
        }
    }

    private fun selectStation(station: com.florentin.letzlisten.data.RadioStation) {
        _currentStation.value = station
        _currentTrack.value = TrackInfo()
        _albumArtUrl.value = null
        _hasStartedPlaying.value = false
        exoPlayer.setMediaItem(MediaItem.fromUri(station.streamUrl))
        exoPlayer.prepare()
    }

    fun switchStation(station: com.florentin.letzlisten.data.RadioStation) {
        _currentStation.value = station
        _currentTrack.value = TrackInfo()
        _albumArtUrl.value = null
        _hasStartedPlaying.value = false
        _isLoading.value = true
        exoPlayer.setMediaItem(MediaItem.fromUri(station.streamUrl))
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun togglePlayback() {
        if (exoPlayer.isPlaying) {
            _isPlaying.value = false
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun toggleFavorite() {
        val track = _currentTrack.value
        val station = _currentStation.value
        if (track.isUnknown || station == null) return
        if (favoritesManager.isFavorited(track.title, track.artist)) {
            favoritesManager.favorites.value
                .find { it.title == track.title && it.artist == track.artist }
                ?.let { favoritesManager.remove(it.id) }
        } else {
            favoritesManager.add(track.title, track.artist, station.id, station.name)
        }
    }

    fun removeFavorite(id: String) = favoritesManager.remove(id)

    fun clearAllFavorites() = favoritesManager.clearAll()

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }
}
