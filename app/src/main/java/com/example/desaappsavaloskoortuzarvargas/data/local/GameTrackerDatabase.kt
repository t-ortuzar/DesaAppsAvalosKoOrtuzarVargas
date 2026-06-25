package com.example.desaappsavaloskoortuzarvargas.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 6,
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

        /**
         * Migration 5→6: clears stale cached prices and price history (which may contain
         * wrong GOG entries for non-GOG games). Favorites and cached images are preserved.
         */
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DELETE FROM game_prices")
                database.execSQL("DELETE FROM price_history")
            }
        }

        fun getInstance(context: Context): GameTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameTrackerDatabase::class.java,
                    "game_tracker_db"
                )
                .addMigrations(MIGRATION_5_6)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
