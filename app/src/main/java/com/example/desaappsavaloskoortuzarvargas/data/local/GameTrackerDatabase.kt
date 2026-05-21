package com.example.desaappsavaloskoortuzarvargas.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.desaappsavaloskoortuzarvargas.data.local.dao.FavoriteGameDao
import com.example.desaappsavaloskoortuzarvargas.data.local.dao.GameImageDao
import com.example.desaappsavaloskoortuzarvargas.data.local.dao.GamePriceDao
import com.example.desaappsavaloskoortuzarvargas.data.local.dao.PriceHistoryDao
import com.example.desaappsavaloskoortuzarvargas.data.local.entity.FavoriteGameEntity
import com.example.desaappsavaloskoortuzarvargas.data.local.entity.GameImageEntity
import com.example.desaappsavaloskoortuzarvargas.data.local.entity.GamePriceEntity
import com.example.desaappsavaloskoortuzarvargas.data.local.entity.PriceHistoryEntity

@Database(
    entities = [GamePriceEntity::class, GameImageEntity::class, FavoriteGameEntity::class, PriceHistoryEntity::class],
    version = 5,
    exportSchema = false
)
abstract class GameTrackerDatabase : RoomDatabase() {

    abstract fun gamePriceDao(): GamePriceDao
    abstract fun gameImageDao(): GameImageDao
    abstract fun favoriteGameDao(): FavoriteGameDao
    abstract fun priceHistoryDao(): PriceHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: GameTrackerDatabase? = null

        fun getInstance(context: Context): GameTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameTrackerDatabase::class.java,
                    "game_tracker_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
