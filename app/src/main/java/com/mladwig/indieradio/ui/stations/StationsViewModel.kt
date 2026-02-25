package com.mladwig.indieradio.ui.stations

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.mladwig.indieradio.data.StationRepository
import com.mladwig.indieradio.data.local.IndieRadioDatabase
import com.mladwig.indieradio.model.RadioStation
import com.mladwig.indieradio.player.MediaControllerManager
import com.mladwig.indieradio.service.RadioPlaybackService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class StationsUiState(
    val stations: List<RadioStation> = emptyList(),
    val currentStation: RadioStation? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val errorMessage: String? = null,
    val favoriteStationIds: Set<String> = emptySet()
)

class StationsViewModel(
    application: Application,
    private val repository: StationRepository
) : AndroidViewModel(application) {

    private val mediaControllerManager = MediaControllerManager(application)
    private var mediaController : MediaController? = null

    private val _uiState = MutableStateFlow(StationsUiState())
    val uiState: StateFlow<StationsUiState> = _uiState.asStateFlow()

    init{
        loadStations()
        observeFavorites()
        connectToService()
    }

    private fun loadStations(){
        _uiState.value = _uiState.value.copy(
            stations = repository.getStations()
        )
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            repository.getFavoriteStationIds().collect { favoriteIds ->
                _uiState.value = _uiState.value.copy(
                    favoriteStationIds = favoriteIds
                )
            }
        }
    }

    private fun connectToService(){
        viewModelScope.launch {
            try {
                mediaController = mediaControllerManager.getController()
                observePlayerState()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to connect to playback service"
                )
            }
        }
    }

    private fun observePlayerState() {
        mediaController?.addListener(object : Player.Listener{
            override fun onPlaybackStateChanged(playbackState: Int) {
                _uiState.value = _uiState.value.copy(
                    isBuffering = playbackState == Player.STATE_BUFFERING,
                    isPlaying = playbackState == Player.STATE_READY && mediaController?.isPlaying == true
                )
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.value = _uiState.value.copy(
                    isPlaying = isPlaying,
                    isBuffering = false
                )
            }

            override fun onPlayerError(error: PlaybackException) {
                 _uiState.value = _uiState.value.copy(
                     isPlaying = false,
                     isBuffering = false,
                     errorMessage = when {
                         error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ->
                             "Network connection failed. Please check your internet."
                         error.errorCode == PlaybackException.ERROR_CODE_IO_INVALID_HTTP_CONTENT_TYPE ->
                             "Invalid stream format for ${_uiState.value.currentStation?.name}"
                         else -> "Playback error: ${error.message ?: "Unknown error"}"
                     }
                 )
            }
        })
    }

    fun onStationSelected(station: RadioStation) {
        viewModelScope.launch {
            //starts the service if not already running.
            startService()
        }

        //Update UI immediately.
        _uiState.value = _uiState.value.copy(
            currentStation = station,
            errorMessage = null
        )

        //Play via MediaController
        mediaController?.let { controller ->
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

            controller.setMediaItem(mediaItem)
            controller.prepare()
            controller.play()
        }
    }

    fun onPlayPauseClicked() {
        mediaController?.let { controller ->
            if (controller.isPlaying) {
                controller.pause()
            } else {
                controller.play()
            }
        }
    }

    fun onFavoriteClicked(station: RadioStation) {
        viewModelScope.launch {
            repository.toggleFavorite(station.id)
        }
    }

    private fun startService() {
        val intent = Intent(getApplication(), RadioPlaybackService::class.java)
        getApplication<Application>().startService(intent)
    }

    //calls when ViewModel is destroyed
    //releases ExoPlayer to prevent memory leaks
    override fun onCleared() {
        super.onCleared()
        mediaControllerManager.release()
    }
}