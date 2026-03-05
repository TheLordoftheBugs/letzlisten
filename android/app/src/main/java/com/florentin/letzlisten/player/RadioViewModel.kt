package com.florentin.letzlisten.player

import android.app.Application
import android.content.ComponentName
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.florentin.letzlisten.RadioService
import com.florentin.letzlisten.data.RadioStation
import com.florentin.letzlisten.data.StationsRepository
import com.florentin.letzlisten.ui.bundledLogoRes
import com.florentin.letzlisten.ui.stationLogoUrls
import com.google.common.util.concurrent.ListenableFuture
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

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val favoritesManager = FavoritesManager(application)

    private val _stations = MutableStateFlow<List<RadioStation>>(emptyList())
    val stations: StateFlow<List<RadioStation>> = _stations.asStateFlow()

    private val _currentStation = MutableStateFlow<RadioStation?>(null)
    val currentStation: StateFlow<RadioStation?> = _currentStation.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // True once playback has started at least once for the current station; resets on station change.
    private val _hasStartedPlaying = MutableStateFlow(false)
    val hasStartedPlaying: StateFlow<Boolean> = _hasStartedPlaying.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentTrack = MutableStateFlow(TrackInfo())
    val currentTrack: StateFlow<TrackInfo> = _currentTrack.asStateFlow()

    private val _albumArtUrl = MutableStateFlow<String?>(null)
    val albumArtUrl: StateFlow<String?> = _albumArtUrl.asStateFlow()

    private val itunesCache = mutableMapOf<String, String?>()
    private val prefs = application.getSharedPreferences("radio_prefs", android.content.Context.MODE_PRIVATE)

    val favorites = favoritesManager.favorites

    val isFavorited: StateFlow<Boolean> = combine(favoritesManager.favorites, _currentTrack) { favs, track ->
        if (track.isUnknown) false
        else favs.any { it.title == track.title && it.artist == track.artist }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    // Handles playback-state events only. ICY metadata comes from RadioService.icyTitle.
    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
            if (isPlaying) _hasStartedPlaying.value = true
        }

        override fun onPlaybackStateChanged(state: Int) {
            _isLoading.value = state == Player.STATE_BUFFERING
        }
    }

    init {
        // Load station list immediately so UI can display it before the controller connects.
        val local = StationsRepository.loadFromAssets(application)
        _stations.value = local.filter { it.isEnabled }.sortedBy { it.name }
        val savedId = prefs.getString("last_station_id", null)
        val initialStation = _stations.value.firstOrNull { it.id == savedId }
            ?: _stations.value.firstOrNull { it.id == "rgl" }
            ?: _stations.value.firstOrNull()

        // Collect raw ICY StreamTitle pushed directly by the ExoPlayer listener in RadioService.
        // onMetadata(IcyInfo) is the authoritative source for live-stream track changes.
        viewModelScope.launch {
            RadioService.icyTitle.collect { raw ->
                handleIcyTitle(raw ?: return@collect)
            }
        }

        // Connect to RadioService asynchronously via MediaController.
        val sessionToken = SessionToken(application, ComponentName(application, RadioService::class.java))
        val future = MediaController.Builder(application, sessionToken).buildAsync()
        controllerFuture = future
        future.addListener({
            val controller = future.get()
            mediaController = controller
            controller.addListener(playerListener)
            initialStation?.let { selectStation(it) }
        }, ContextCompat.getMainExecutor(application))
    }

    private fun handleIcyTitle(raw: String) {
        // Skip if it's just the station name broadcast by the stream itself.
        if (raw.trim() == _currentStation.value?.name?.trim()) return

        val parts = raw.split(" - ", limit = 2)
        val artist = if (parts.size == 2) parts[0].trim() else ""
        val title = if (parts.size == 2) parts[1].trim() else raw.trim()

        val filteredTitle = filterMetadata(title) ?: return
        val filteredArtist = filterMetadata(artist.ifBlank { null }) ?: ""

        val newTrack = TrackInfo(title = filteredTitle, artist = filteredArtist)
        if (_currentTrack.value == newTrack) return

        _currentTrack.value = newTrack
        _albumArtUrl.value = null
        // Reset notification artwork to station logo while album art is being fetched.
        RadioService.icyArtworkUri.value = _currentStation.value?.let { stationArtworkUri(it) }
        fetchAlbumArt(filteredArtist, filteredTitle)
    }

    private fun stationArtworkUri(station: RadioStation): Uri? {
        val app: Application = getApplication()
        val resId = bundledLogoRes(station.logoImageName)
        if (resId != null) {
            return Uri.parse("android.resource://${app.packageName}/$resId")
        }
        return stationLogoUrls(station).firstOrNull()?.let { Uri.parse(it) }
    }

    private fun buildMediaItem(station: RadioStation): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(station.name)
            .setArtist(station.name)
            .setArtworkUri(stationArtworkUri(station))
            .build()
        return MediaItem.Builder()
            .setUri(station.streamUrl)
            .setMediaMetadata(metadata)
            .build()
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
                // Push album art to notification/lock screen when available.
                if (url != null) RadioService.icyArtworkUri.value = Uri.parse(url)
            } catch (_: Exception) {
                itunesCache[key] = null
            }
        }
    }

    private fun selectStation(station: RadioStation) {
        _currentStation.value = station
        _currentTrack.value = TrackInfo()
        _albumArtUrl.value = null
        _hasStartedPlaying.value = false
        RadioService.icyTitle.value = null
        RadioService.icyArtworkUri.value = null
        mediaController?.run {
            setMediaItem(buildMediaItem(station))
            prepare()
        }
    }

    fun switchStation(station: RadioStation) {
        prefs.edit().putString("last_station_id", station.id).apply()
        _isPlaying.value = false
        _currentStation.value = station
        _currentTrack.value = TrackInfo()
        _albumArtUrl.value = null
        _hasStartedPlaying.value = false
        RadioService.icyTitle.value = null
        RadioService.icyArtworkUri.value = null
        mediaController?.run {
            stop()
            playWhenReady = false
            setMediaItem(buildMediaItem(station))
            prepare()
        }
    }

    fun togglePlayback() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            _isPlaying.value = false
            controller.pause()
        } else {
            controller.play()
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
        mediaController?.removeListener(playerListener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
        mediaController = null
    }
}
