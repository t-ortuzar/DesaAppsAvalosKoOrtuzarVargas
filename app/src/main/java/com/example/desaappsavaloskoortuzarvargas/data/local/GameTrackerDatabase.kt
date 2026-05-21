package com.example.desaappsavaloskoortuzarvargas.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.desaappsavaloskoortuzarvargas.data.local.dao.GameImageDao
import com.example.desaappsavaloskoortuzarvargas.data.local.dao.GamePriceDao
import com.example.desaappsavaloskoortuzarvargas.data.local.entity.GameImageEntity
import com.example.desaappsavaloskoortuzarvargas.data.local.entity.GamePriceEntity

@Database(
    entities = [GamePriceEntity::class, GameImageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class GameTrackerDatabase : RoomDatabase() {

    abstract fun gamePriceDao(): GamePriceDao
    abstract fun gameImageDao(): GameImageDao

    companion object {
        @Volatile
        private var INSTANCE: GameTrackerDatabase? = null

        fun getInstance(context: Context): GameTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameTrackerDatabase::class.java,
                    "game_tracker_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

