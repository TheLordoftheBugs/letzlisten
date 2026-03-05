package com.florentin.letzlisten

import androidx.media3.common.Metadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.extractor.metadata.icy.IcyInfo
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import kotlinx.coroutines.flow.MutableStateFlow

class RadioService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    companion object {
        /**
         * Raw ICY StreamTitle pushed here every time the stream sends new inline metadata.
         * Using onMetadata(IcyInfo) is more reliable than onMediaMetadataChanged for live
         * radio streams: it fires on the actual ICY frame, not on a merged/cached snapshot.
         * Observed by RadioViewModel to update the track display in real time.
         */
        val icyTitle = MutableStateFlow<String?>(null)
    }

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this).build()
        player.addListener(object : Player.Listener {
            override fun onMetadata(metadata: Metadata) {
                for (i in 0 until metadata.length()) {
                    val entry = metadata[i]
                    if (entry is IcyInfo) {
                        val title = entry.title?.takeIf { it.isNotBlank() } ?: continue
                        icyTitle.value = title
                        break
                    }
                }
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
