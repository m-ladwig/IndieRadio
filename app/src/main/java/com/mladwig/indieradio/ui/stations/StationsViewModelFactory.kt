package com.mladwig.indieradio.ui.stations

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import com.mladwig.indieradio.data.StationRepository
import com.mladwig.indieradio.data.local.IndieRadioDatabase

/**
 * Factory for creating StationsViewModel with custom dependencies
 *
 * Allows dependency injection for ttesting while still providing defaults for prod
 *
 * @param application Application context
 * @param repository Option repository override (primarily for testing)
 */
class StationsViewModelFactory (
    private val application: Application,
    private val repository: StationRepository? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        //Verify creation of the right ViewModel type
        if (modelClass.isAssignableFrom(StationsViewModel::class.java)) {
            val actualRepository = repository ?: createDefaultRepository()
            return StationsViewModel(application, actualRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }

    /**
     * Creates the default repository for production use
     */
    private fun createDefaultRepository(): StationRepository {
        val database = IndieRadioDatabase.getDatabase(application)
        return StationRepository(database.favoriteStationDao())
    }
}