package com.mladwig.indieradio.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteStationDao {

    //get favorite station IDs (reactive - updates automatically)
    @Query("SELECT * FROM favorite_stations ORDER BY favoritedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteStationEntity>>

    //Check if a specific station is favorited
    @Query("SELECT * FROM favorite_stations WHERE stationId = :stationId")
    suspend fun getFavorite(stationId: String): FavoriteStationEntity?

    //add a station to favorites
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteStationEntity)

    //remove a station from favorites
    @Delete
    suspend fun deleteFavorite(favorite: FavoriteStationEntity)

    //Alternative: delete by ID
    @Query("DELETE FROM favorite_stations WHERE stationId = :stationId")
    suspend fun deleteFavoriteById(stationId: String)
}