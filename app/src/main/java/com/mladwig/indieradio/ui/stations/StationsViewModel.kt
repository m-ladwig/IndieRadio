package com.mladwig.indieradio.ui.stations

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mladwig.indieradio.data.StationRepository
import com.mladwig.indieradio.model.RadioStation
import com.mladwig.indieradio.player.PlaybackState
import com.mladwig.indieradio.player.RadioPlayerManager
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

    private val playerManager = RadioPlayerManager(application)

    private val _uiState = MutableStateFlow(StationsUiState())
    val uiState: StateFlow<StationsUiState> = _uiState.asStateFlow()

    init{
        loadStations()
        observePlaybackState()
    }

    private fun loadStations(){
        _uiState.value = _uiState.value.copy(
            stations = StationRepository.getStations()
        )
    }

    private fun observePlaybackState(){
        viewModelScope.launch {
            playerManager.playbackState.collect { state ->
                when(state) {
                    is PlaybackState.Playing -> {
                        _uiState.value = _uiState.value.copy(
                            isPlaying = true,
                            isBuffering = false,
                            errorMessage = null
                        )
                    }
                    is PlaybackState.Paused -> {
                        _uiState.value = _uiState.value.copy(
                            isPlaying = false,
                            isBuffering = false
                        )
                    }
                    is PlaybackState.Buffering -> {
                        _uiState.value = _uiState.value.copy(
                            isBuffering = true
                        )
                    }
                    is PlaybackState.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isPlaying = false,
                            isBuffering = false,
                            errorMessage = state.message
                        )
                    }
                    is PlaybackState.Idle -> {
                        _uiState.value = _uiState.value.copy(
                            isPlaying = false,
                            isBuffering = false
                        )
                    }
                }
            }
        }
    }

    fun onStationSelected(station: RadioStation) {
        _uiState.value = _uiState.value.copy(
            currentStation = station,
            errorMessage = null
        )
        playerManager.playStation(station)
    }

    fun onPlayPauseClicked() {
        if(_uiState.value.isPlaying) {
            playerManager.pause()
        }else {
            playerManager.resume()
        }
    }

    //calls when ViewModel is destroyed
    //releases ExoPlayer to prevent memory leaks
    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}