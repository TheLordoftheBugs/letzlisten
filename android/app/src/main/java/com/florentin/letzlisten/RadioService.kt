package com.florentin.letzlisten

import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import kotlinx.coroutines.flow.MutableStateFlow

class RadioService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    companion object {
        /**
         * ICY stream metadata forwarded here whenever ExoPlayer fires onMediaMetadataChanged.
         * Observed by RadioViewModel so it never misses a track change, regardless of whether
         * MediaSession propagates timed metadata to connected MediaControllers.
         */
        val icyMetadata = MutableStateFlow<MediaMetadata?>(null)
    }

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this).build()
        player.addListener(object : Player.Listener {
            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                icyMetadata.value = mediaMetadata
            }
        })
        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
