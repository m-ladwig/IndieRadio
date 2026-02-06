package com.mladwig.indieradio.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteStationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class IndieRadioDatabase : RoomDatabase() {

    abstract fun favoriteStationDao(): FavoriteStationDao

    companion object {
        @Volatile
        private var INSTANCE: IndieRadioDatabase? = null

        fun getDatabase(context: Context): IndieRadioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IndieRadioDatabase::class.java,
                    "indieradio_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}