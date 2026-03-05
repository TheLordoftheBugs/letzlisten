package com.florentin.letzlisten

import android.net.Uri
import androidx.media3.common.Metadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.extractor.metadata.icy.IcyInfo
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class RadioService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    companion object {
        /** Raw ICY StreamTitle, updated on every ICY frame. Consumed by RadioViewModel. */
        val icyTitle = MutableStateFlow<String?>(null)

        /**
         * Artwork URI to display in the notification/lock screen.
         * Reset to the station logo on each track change, then updated to album art
         * once the iTunes lookup completes. Set by RadioViewModel.
         */
        val icyArtworkUri = MutableStateFlow<Uri?>(null)
    }

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this).build()

        player.addListener(object : Player.Listener {
            override fun onMetadata(metadata: Metadata) {
                for (i in 0 until metadata.length()) {
                    val entry = metadata[i]
                    if (entry is IcyInfo) {
                        val raw = entry.title?.takeIf { it.isNotBlank() } ?: continue
                        icyTitle.value = raw
                        // Immediately update the MediaItem so notification/lock screen
                        // reflects the current track without waiting for the ViewModel.
                        val currentItem = player.currentMediaItem ?: break
                        val parts = raw.split(" - ", limit = 2)
                        val updatedMeta = currentItem.mediaMetadata.buildUpon()
                            .setTitle(if (parts.size == 2) parts[1].trim() else raw)
                            .setArtist(
                                if (parts.size == 2) parts[0].trim()
                                else currentItem.mediaMetadata.artist
                            )
                            .build()
                        player.replaceMediaItem(
                            player.currentMediaItemIndex,
                            currentItem.buildUpon().setMediaMetadata(updatedMeta).build()
                        )
                        break
                    }
                }
            }
        })

        // When the ViewModel pushes a new artwork URI (station logo reset or iTunes album art),
        // patch it into the current MediaItem so the notification updates.
        serviceScope.launch {
            icyArtworkUri.collect { uri ->
                uri ?: return@collect
                val currentItem = player.currentMediaItem ?: return@collect
                val updatedMeta = currentItem.mediaMetadata.buildUpon()
                    .setArtworkUri(uri)
                    .build()
                player.replaceMediaItem(
                    player.currentMediaItemIndex,
                    currentItem.buildUpon().setMediaMetadata(updatedMeta).build()
                )
            }
        }

        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onDestroy() {
        serviceScope.cancel()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
