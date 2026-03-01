package com.mladwig.indieradio.ui.stations

import android.app.Application
import android.provider.MediaStore
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mladwig.indieradio.data.StationRepository
import com.mladwig.indieradio.data.local.FavoriteStationEntity
import com.mladwig.indieradio.model.RadioStation
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.theories.suppliers.TestedOn

@OptIn(ExperimentalCoroutinesApi::class)
class StationsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    // Mocks
    private lateinit var mockApplication: Application
    private lateinit var mockRepository: StationRepository

    private lateinit var viewModel: StationsViewModel


    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mocks
        mockApplication = mockk(relaxed = true)
        mockRepository = mockk(relaxed = true)

        // Default repository behavior
        every { mockRepository.getStations() } returns createTestStations()
        coEvery { mockRepository.getFavoriteStationIds() } returns flowOf(emptySet())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has correct stations`() = runTest {
        // When: Viewmodel is created
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then: State contains stations from repository
        val state = viewModel.uiState.value
        assertEquals(3, state.stations.size)
        assertTrue(state.stations.any {it.id == "test1"})
    }

    @Test
    fun `initial state has no favorites`() = runTest {
        // When: ViewModel is created
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then: favorite IDs should be empty
        val state = viewModel.uiState.value
        assertTrue(state.favoriteStationIds.isEmpty())
    }

    @Test
    fun `onFavoriteClicked calls repository toggleFavorite`() = runTest {
        // Given: ViewModel is initialized
        viewModel = createViewModel()
        advanceUntilIdle()

        val testStation = createTestStations()[0]

        // When: user clicks favorite
        viewModel.onFavoriteClicked(testStation)
        advanceUntilIdle()

        // Then: Repository toggleFavorite should be called
        coVerify { mockRepository.toggleFavorite(("test1")) }
    }

    @Test
    fun `favorite IDs update when repository emits new data`() = runTest {
        // Given: Repository will emit favorite IDs
        coEvery { mockRepository.getFavoriteStationIds() } returns
                flowOf(setOf("test1", "test2"))

        // When: ViewModel is created
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then: UI state should contain favorite IDs
        val state = viewModel.uiState.value
        assertEquals(setOf("test1", "test2"), state.favoriteStationIds)
    }

    @Test
    fun `currentStation is null initially`() = runTest {
        // When: ViewModel is created
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then: Current station should be null
        val state = viewModel.uiState.value
        assertEquals(null, state.currentStation)
    }

    @Test
    fun `onStationSelected updates currentStation`() = runTest {
        // Given: ViewModel is initialized
        viewModel = createViewModel()
        advanceUntilIdle()

        val testStation = createTestStations()[0]

        //When: User selects a station
        viewModel.onStationSelected(testStation)
        advanceUntilIdle()

        // Then: Current station should be updated
        val state = viewModel.uiState.value
        assertEquals("test1", state.currentStation?.id)
    }

    @Test
    fun `isPlaying is false initially`() = runTest {
        // When: ViewModel is created
        viewModel = createViewModel()
        advanceUntilIdle()

        //Then: Should not be playing.
        val state = viewModel.uiState.value
        assertFalse(state.isPlaying)
    }

    @Test
    fun `isBuffering is false initially`() = runTest {
        // When: ViewModel is created
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then: Should not be buffering
        val state = viewModel.uiState.value
        assertFalse(state.isBuffering)
    }

    // Helper to create ViewModel with mocks
    private fun createViewModel(): StationsViewModel {
        return StationsViewModel(
            application = mockApplication,
            repository = mockRepository
        )
    }


    // Helper for test stations
    private fun createTestStations(): List<RadioStation> {
        return listOf(
            RadioStation(
                id = "test1",
                name = "Test Station 1",
                streamUrl = "http://test1.com",
                description = "Test 1",
                genre = "Test",
                location = "Anytown, OH"
            ),
            RadioStation(
                id = "test2",
                name = "Test Station 2",
                streamUrl = "http://test2.com",
                description = "Test 2",
                genre = "Test",
                location = "Nowhere, AK"
            ),
            RadioStation(
                id = "test3",
                name = "Test Station 3",
                streamUrl = "http://test3.com",
                description = "Test 3",
                genre = "Test",
                location = "Anywhere, CA"
            )
        )
    }

}