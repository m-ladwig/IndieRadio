package com.mladwig.indieradio.data

import com.mladwig.indieradio.data.local.FavoriteStationDao
import com.mladwig.indieradio.data.local.FavoriteStationEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class StationRepositoryTest {
    // Mock DAO
    private lateinit var mockDao: FavoriteStationDao

    // Real repo using mock DAO
    private lateinit var repository: StationRepository

    @Before
    fun setup() {
        // Create mock DAO before each test
        mockDao = mockk(relaxed = true)

        // Create repo with mock.
        repository = StationRepository(mockDao)
    }

    @Test
    fun `getStations returns correct number of stations`() {
        // When: Getting stations
        val stations = repository.getStations()

        //Then: should return 9 stations
        assertEquals(9, stations.size)
    }

    @Test
    fun `getStations includes Radio Milwaukee`() {
        // When: Getting stations
        val stations = repository.getStations()

        //Then: Should include Radio Milwaukee
        val radioMke = stations.find { it.id == "radiomke" }
        assertEquals("88Nine Radio Milwaukee", radioMke?.name)
    }

    @Test
    fun `toggleFavorite adds station when not favorited`() = runTest {
        //Given: Radio Milwaukee is not a favorite station
        coEvery { mockDao.getFavorite("radiomke") } returns null

        //When: toggleFavorites is called on Radio Milwaukee
        repository.toggleFavorite("radiomke")

        //Then: Radio Milwaukee should then be present in favorites
        coVerify { mockDao.insertFavorite(any()) }
    }

    @Test
    fun `toggleFavorite removes station when already favorited`() = runTest{
        // Given: Station is already favorited
        val existingFavorite = FavoriteStationEntity("radiomke")
        coEvery { mockDao.getFavorite("radiomke") } returns existingFavorite

        // When: toggling favorite
        repository.toggleFavorite("radiomke")

        // Then: Should delete favorite
        coVerify { mockDao.deleteFavorite(existingFavorite) }
    }

    @Test
    fun `isFavorite returns true when station is favorited`() = runTest {
        //Given: Station is favorited
        val favorite = FavoriteStationEntity("radiomke")
        coEvery { mockDao.getFavorite("radiomke") } returns favorite

        // When: checking if favorited
        val result = repository.isFavorite("radiomke")

        // Then: Should return true
        assertTrue(result)
    }

    @Test
    fun `isFavorite returns false when station is not favorited`() = runTest {
        // Given: Station is not favorited
        coEvery { mockDao.getFavorite("radiomke") } returns null

        // When: checking if favorited
        val result = repository.isFavorite("radiomke")

        //Then : should return false
        assertFalse(result)
    }

    @Test
    fun `getFavoriteStationIds transforms entities to an ID set`() = runTest {
        //Given: two favorited stations
        val favorites = listOf(
            FavoriteStationEntity("radiomke"),
            FavoriteStationEntity("kexp")
        )
        coEvery { mockDao.getAllFavorites() } returns flowOf(favorites)

        // When: getting favorite IDs
        val idsFlow = repository.getFavoriteStationIds()

        //Then: should emit set of ids
        idsFlow.collect { ids ->
            assertEquals(setOf("radiomke", "kexp"), ids)
        }
    }
}