package com.mladwig.indieradio.player

import android.content.Context
import android.media.browse.MediaBrowser
import android.provider.MediaStore
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.mladwig.indieradio.model.RadioStation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class PlaybackState {
    object Idle : PlaybackState()
    object Buffering : PlaybackState()
    object Playing : PlaybackState()
    object Paused: PlaybackState()
    data class Error(val message: String) : PlaybackState()
}

class RadioPlayerManager(context: Context) {
    private val player: ExoPlayer = ExoPlayer.Builder(context).build()

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    //Track which station is currently loaded
    private var currentStation : RadioStation? = null

    init{
        setupPlayerListener()
    }

    //Listens to ExoPlayer's internal state changes and converts them to PlaybackState
    private fun setupPlayerListener(){
        player.addListener(object: Player.Listener {

            //Called when player state changes
            override fun onPlaybackStateChanged(state: Int) {
                _playbackState.value = when (state) {
                    Player.STATE_IDLE -> PlaybackState.Idle
                    Player.STATE_BUFFERING -> PlaybackState.Buffering
                    Player.STATE_READY -> {
                        //Ready means stream is loaded, but is it actually playing?
                        if(player.playWhenReady) PlaybackState.Playing
                        else PlaybackState.Paused
                    }
                    Player.STATE_ENDED -> PlaybackState.Idle
                    else -> PlaybackState.Idle
                }
            }

            //Error handling
            override fun onPlayerError(error: PlaybackException) {
                _playbackState.value = PlaybackState.Error(
                    error.message ?: "Unknown playback error"
                )
            }

            //handles play/pause state changes
            override fun onIsLoadingChanged(isPlaying: Boolean) {
                if (isPlaying){
                    _playbackState.value = PlaybackState.Playing
                }
            }
        })
    }

    fun playStation(station: RadioStation) {
        currentStation = station

        //creates an ExoPlayer MediaItem
        val mediaItem = MediaItem.Builder()
            .setUri(station.streamUrl)
            .setMediaMetadata(
                androidx.media3.common.MediaMetadata.Builder()
                    .setTitle(station.name)
                    .setArtist(station.location)
                    .build()
            )
            .build()

        player.setMediaItem(mediaItem)

        player.prepare()

        player.playWhenReady = true

    }

        //Pause playback (while keeping stream loaded)
        fun pause(){
            player.playWhenReady = false
        }

        fun resume() {
            player.playWhenReady = true
        }

        //stops playback and release stream
        fun stop() {
            player.stop()
            currentStation = null
        }

        //cleans up when done, prevents memory leaks!
        fun release() {
            player.release()
        }

        //Helpers!
        fun isPlaying(): Boolean {
            return player.isPlaying
        }

        fun getCurrentStation(): RadioStation? {
            return currentStation
        }
}