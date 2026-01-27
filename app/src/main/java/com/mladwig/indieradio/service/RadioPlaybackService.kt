package com.mladwig.indieradio.service

import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.mladwig.indieradio.model.RadioStation

class RadioPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    companion object {
        private const val CHANNEL_ID = "radio_playback_channel"

        //Exposes current station for ViewModel to observe
        var currentStation: RadioStation? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()

        //create the player
        player = ExoPlayer.Builder(this).build()

        //listener for state changes
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState : Int){
                //TODO add state update handlers
            }

            override fun onIsPlayingChanged(isPlaying: Boolean){}
            //TODO add play/pause state handler
        })


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

    //Public function for the ViewModel to call
    fun playStation(station : RadioStation) {
        currentStation = station

        val mediaItem = MediaItem.Builder()
            .setUri(station.streamUrl)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(station.name)
                    .setArtist(station.location)
                    .setAlbumTitle(station.genre)
                    .build()
            )
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
    }
}