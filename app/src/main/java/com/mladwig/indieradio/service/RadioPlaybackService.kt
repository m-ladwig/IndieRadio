package com.mladwig.indieradio.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.mladwig.indieradio.MainActivity
import com.mladwig.indieradio.R

class RadioPlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    companion object {
        private const val NOTIFICATION_ID = 101
        private const val CHANNEL_ID = "radio_playback_channel"
    }

    override fun onCreate() {
        super.onCreate()

        //create the player
        player = ExoPlayer.Builder(this).build()

        //create the MediaSession
        mediaSession = MediaSession.Builder(this, player)
            .build()

        //Create notification channel
        createNotificationChannel()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) : MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Radio Playback",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows currently playing radio station"
            setShowBadge(false)
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}