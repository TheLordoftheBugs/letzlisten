package com.florentin.letzlisten

import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * Background service for radio playback.
 * Enables playback to continue when the app is in the background,
 * and shows media controls on the lock screen and notification shade.
 */
class RadioService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

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
