package com.mladwig.indieradio.data.local


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_stations")
data class  FavoriteStationEntity(
    @PrimaryKey
    val stationId: String,
    val favoritedAt: Long = System.currentTimeMillis()
)