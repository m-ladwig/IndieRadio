package com.mladwig.indieradio.ui.stations

import androidx.lifecycle.ViewModel
import com.mladwig.indieradio.data.StationRepository
import com.mladwig.indieradio.model.RadioStation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


data class StationsUiState(
    val stations: List<RadioStation> = emptyList(),
    val currentStation: RadioStation? = null,
    val isPlaying: Boolean = false
)

class StationsViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(StationsUiState())
    val uiState: StateFlow<StationsUiState> = _uiState.asStateFlow()

    init{
        loadStations()
    }

    private fun loadStations(){
        _uiState.value = _uiState.value.copy(
            stations = StationRepository.getStations()
        )
    }

    fun onStationSelected(station: RadioStation) {
        _uiState.value = _uiState.value.copy(
            currentStation = station,
            isPlaying = true
        )
    }

    fun onPlayPauseClicked() {
        _uiState.value = _uiState.value.copy(
            isPlaying = !_uiState.value.isPlaying
        )
    }
}