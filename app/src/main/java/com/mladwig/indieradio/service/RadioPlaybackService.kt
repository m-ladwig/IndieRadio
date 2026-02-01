package com.mladwig.indieradio.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Binder
import android.os.IBinder
import androidx.compose.material3.TabRowDefaults
import android.media.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.mladwig.indieradio.model.RadioStation

class RadioPlaybackService : MediaSessionService() {

    //audio focus
    private var audioFocusRequest: AudioFocusRequest? = null
    private var hasAudioFocus = false

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener{ focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                //Regain focus, resume playback if was playing
                if (hasAudioFocus && !player.isPlaying) {
                    player.play()
                }
                player.volume = 1.0f
            }
            AudioManager.AUDIOFOCUS_LOSS ->{
                //Lost focus permanently - pause
                hasAudioFocus = false
                player.pause()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                //phone call/alarm - pause
                player.pause()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                //Lost focus but can duck (i.e. notification sound) - lower volume
                player.volume = 0.3f
            }
        }
    }
    //player
    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer
    //headphone removal
    private var becomingNoisyReceiver : BecomingNoisyReceiver? = null

    companion object {
        private const val CHANNEL_ID = "radio_playback_channel"

        //Exposes current station for ViewModel to observe
        var currentStation: RadioStation? = null
            private set

        //Static reference to the service instance
        private var instance : RadioPlaybackService? = null

        fun getInstance(): RadioPlaybackService? = instance
    }

    //Binder for local binding
    inner class LocalBinder : Binder() {
        fun getService(): RadioPlaybackService = this@RadioPlaybackService
    }

    private val binder = LocalBinder()

    override fun onCreate() {
        super.onCreate()
        instance = this

        //create the player
        player = ExoPlayer.Builder(this).build()

        //register headphone unplug listener
        becomingNoisyReceiver = BecomingNoisyReceiver(player)
        becomingNoisyReceiver?.register(this)

        //listener for state changes
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState : Int){
                //State updates handled by MediaController listeners
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                //State updates handled by MediaController listeners
            }
        })

        //request audio focus
        requestAudioFocus()

        //create the MediaSession
        mediaSession = MediaSession.Builder(this, player)
            .build()

        //Create notification channel
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        //return custom binder for local binding
        return if (intent?.action == "LOCAL_BIND") {
            binder
        } else {
            super.onBind(intent)
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) : MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        //swiping away from recent apps --> close the app
        stopSelf()
    }

    override fun onDestroy() {
        abandonAudioFocus()
        instance = null
        becomingNoisyReceiver?.unregister(this)
        becomingNoisyReceiver = null
        player.stop()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    private fun requestAudioFocus() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setOnAudioFocusChangeListener(audioFocusChangeListener)
            .build()

        val result = audioManager.requestAudioFocus(audioFocusRequest!!)
        hasAudioFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonAudioFocus() {
        audioFocusRequest?.let {request ->
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.abandonAudioFocusRequest(request)
        }
        hasAudioFocus = false
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

    private class BecomingNoisyReceiver(
        private val player: Player
    ) : BroadcastReceiver() {

        private var registered = false

        fun register(context: Context) {
            if (!registered) {
                val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
                context.registerReceiver(this, filter)
                registered = true
            }
        }

        fun unregister(context: Context) {
            if (registered) {
                context.unregisterReceiver(this)
                registered = false
            }
        }

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                //pauses playback if headphones unplugged.
                player.pause()
            }
        }
    }

}