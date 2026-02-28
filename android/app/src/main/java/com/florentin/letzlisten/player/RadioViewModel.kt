package com.florentin.letzlisten.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.florentin.letzlisten.data.RadioStation
import com.florentin.letzlisten.data.StationsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TrackInfo(
    val title: String = "",
    val artist: String = ""
) {
    val isUnknown: Boolean get() = title.isBlank() && artist.isBlank()
}

class RadioViewModel(application: Application) : AndroidViewModel(application) {

    private val exoPlayer = ExoPlayer.Builder(application).build()

    private val _stations = MutableStateFlow<List<RadioStation>>(emptyList())
    val stations: StateFlow<List<RadioStation>> = _stations.asStateFlow()

    private val _currentStation = MutableStateFlow<RadioStation?>(null)
    val currentStation: StateFlow<RadioStation?> = _currentStation.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentTrack = MutableStateFlow(TrackInfo())
    val currentTrack: StateFlow<TrackInfo> = _currentTrack.asStateFlow()

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
            _stations.value = local.filter { it.isEnabled }
                .sortedBy { it.name }

            // Auto-play first station on launch
            _stations.value.firstOrNull()?.let { switchStation(it) }

            // Try to refresh from remote
            val remote = StationsRepository.fetchRemote()
            if (remote.isNotEmpty()) {
                _stations.value = remote.filter { it.isEnabled }
                    .sortedBy { it.name }
            }
        }
    }

    fun switchStation(station: RadioStation) {
        _currentStation.value = station
        _currentTrack.value = TrackInfo()
        _isLoading.value = true
        exoPlayer.setMediaItem(MediaItem.fromUri(station.streamUrl))
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun togglePlayback() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }
}
