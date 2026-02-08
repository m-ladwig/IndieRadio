package com.mladwig.indieradio.ui.stations

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.mladwig.indieradio.data.StationRepository
import com.mladwig.indieradio.data.local.IndieRadioDatabase
import com.mladwig.indieradio.model.RadioStation
import com.mladwig.indieradio.player.MediaControllerManager
import com.mladwig.indieradio.player.PlaybackState
import com.mladwig.indieradio.player.RadioPlayerManager
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
    val errorMessage: String? = null
)

class StationsViewModel(application: Application) : AndroidViewModel(application) {

    private val mediaControllerManager = MediaControllerManager(application)
    private var mediaController : MediaController? = null

    private val stationRepository : StationRepository by lazy {
        val database = IndieRadioDatabase.getDatabase(application)
        StationRepository(database.favoriteStationDao())
    }

    private val _uiState = MutableStateFlow(StationsUiState())
    val uiState: StateFlow<StationsUiState> = _uiState.asStateFlow()

    init{
        loadStations()
        connectToService()
    }

    private fun loadStations(){
        _uiState.value = _uiState.value.copy(
            stations = stationRepository.getStations()
        )
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

        //Tell the service to play the selected station
        val service = getService()
        service?.playStation(station)
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

    private fun startService() {
        val intent = Intent(getApplication(), RadioPlaybackService::class.java)
        getApplication<Application>().startService(intent)
    }

    private fun getService(): RadioPlaybackService? {
        return RadioPlaybackService.getInstance()
    }

    //calls when ViewModel is destroyed
    //releases ExoPlayer to prevent memory leaks
    override fun onCleared() {
        super.onCleared()
        mediaControllerManager.release()
    }
}