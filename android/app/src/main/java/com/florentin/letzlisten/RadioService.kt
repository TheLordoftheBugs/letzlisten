package com.florentin.letzlisten

import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * Background service for radio playback.
 * Owns the ExoPlayer and MediaSession so playback continues in the background
 * and media controls appear on the lock screen and notification shade.
 */
class RadioService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this).build()

        // ICY stream metadata (track title) arrives on the ExoPlayer but is NOT
        // automatically forwarded to connected MediaControllers via MediaSession.
        // Fix: when the effective title changes (ICY update), write it back into
        // the MediaItem's static metadata via replaceMediaItem() so MediaControllers
        // receive the change through their onMediaMetadataChanged callback.
        player.addListener(object : Player.Listener {
            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                val streamTitle = mediaMetadata.title ?: return
                val currentItem = player.currentMediaItem ?: return
                if (streamTitle == currentItem.mediaMetadata.title) return
                val updatedItem = currentItem.buildUpon()
                    .setMediaMetadata(
                        currentItem.mediaMetadata.buildUpon()
                            .setTitle(streamTitle)
                            .build()
                    )
                    .build()
                player.replaceMediaItem(player.currentMediaItemIndex, updatedItem)
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
