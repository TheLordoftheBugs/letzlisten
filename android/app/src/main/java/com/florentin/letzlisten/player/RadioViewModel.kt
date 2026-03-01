package com.florentin.letzlisten.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.florentin.letzlisten.data.StationsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentTrack = MutableStateFlow(TrackInfo())
    val currentTrack: StateFlow<TrackInfo> = _currentTrack.asStateFlow()

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
            }
            override fun onPlaybackStateChanged(state: Int) {
                _isLoading.value = state == Player.STATE_BUFFERING
            }
        })
    }

    private fun loadStations() {
        viewModelScope.launch {
            val local = StationsRepository.loadFromAssets(getApplication())
            _stations.value = local.filter { it.isEnabled }.sortedBy { it.name }
            _stations.value.firstOrNull()?.let { selectStation(it) }

            val remote = StationsRepository.fetchRemote()
            if (remote.isNotEmpty()) {
                _stations.value = remote.filter { it.isEnabled }.sortedBy { it.name }
            }
        }
    }

    private fun selectStation(station: com.florentin.letzlisten.data.RadioStation) {
        _currentStation.value = station
        _currentTrack.value = TrackInfo()
        exoPlayer.setMediaItem(MediaItem.fromUri(station.streamUrl))
        exoPlayer.prepare()
    }

    fun switchStation(station: com.florentin.letzlisten.data.RadioStation) {
        _currentStation.value = station
        _currentTrack.value = TrackInfo()
        _isLoading.value = true
        exoPlayer.setMediaItem(MediaItem.fromUri(station.streamUrl))
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun togglePlayback() {
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
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
