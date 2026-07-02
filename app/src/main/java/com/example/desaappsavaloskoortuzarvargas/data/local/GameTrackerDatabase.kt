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
    entities = [
        GamePriceEntity::class,
        GameImageEntity::class,
        FavoriteGameEntity::class,
        PriceHistoryEntity::class
    ],
    version = 12,
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

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DELETE FROM game_prices")
                database.execSQL("DELETE FROM price_history")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE game_prices ADD COLUMN isGamePass INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        /** 7→8: added local_users table (temporary MongoDB fallback, now removed). */
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS local_users (
                        id TEXT NOT NULL PRIMARY KEY, username TEXT NOT NULL,
                        pwdHash TEXT NOT NULL, email TEXT NOT NULL DEFAULT '',
                        favoritesJson TEXT NOT NULL DEFAULT '[]', createdAt INTEGER NOT NULL
                    )"""
                )
            }
        }

        /** 8→9: drop local_users table — auth migrated to Firebase. */
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS local_users")
            }
        }

        /**
         * 9→10: evict stale cached prices for games whose store platform list
         * was corrected (e.g. Persona 5 Royal — removed GOG & Epic entries that
         * pointed to wrong/non-existent pages). Prices will be re-fetched fresh
         * on the next app launch from the correct stores (Steam + Xbox only).
         */
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "DELETE FROM game_prices WHERE gameName = 'Persona 5 Royal' AND storeName IN ('GOG', 'Epic Games')"
                )
                database.execSQL(
                    "DELETE FROM price_history WHERE gameName = 'Persona 5 Royal' AND storeName IN ('GOG', 'Epic Games')"
                )
            }
        }

        /** 10→11: add isEaPlay column to game_prices — marks games included in EA Play subscription.
         *  Also clears any stale Steam $0 entries (e.g. Madden NFL, EA Sports FC) so they get
         *  re-fetched and correctly tagged as EA Play instead of showing $0. */
        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE game_prices ADD COLUMN isEaPlay INTEGER NOT NULL DEFAULT 0"
                )
                // Remove any existing Steam price entries that have $0 and aren't Game Pass.
                // These are EA Play games that were cached incorrectly before this fix.
                // They will be re-fetched and correctly tagged as isEaPlay=1 on next refresh.
                database.execSQL(
                    "DELETE FROM game_prices WHERE storeName = 'Steam' AND currentPrice = 0 AND isGamePass = 0"
                )
            }
        }

        /** 11→12: clear all cached EA prices so they are re-fetched with the new scraping
         *  approach. Also evict prices for EA-platform games so Epic/Xbox entries
         *  are refreshed with the updated product IDs and search hints. */
        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Remove any stale EA price entries (Origin API results, wrong URLs, etc.)
                database.execSQL("DELETE FROM game_prices WHERE storeName = 'EA'")
                database.execSQL("DELETE FROM price_history WHERE storeName = 'EA'")
                // Also clear prices for known EA-platform games so Epic/Xbox entries
                // are re-fetched with the new product IDs and per-store search hints.
                val eaGames = listOf(
                    "Dead Space Remake", "Star Wars Jedi: Survivor", "EA Sports FC 25",
                    "Madden NFL 25", "F1 24", "Need for Speed Unbound",
                    "It Takes Two", "A Way Out", "Dragon Age: The Veilguard",
                    "Mass Effect Legendary Edition", "Star Wars Jedi: Fallen Order", "Titanfall 2"
                )
                val inClause = eaGames.joinToString(",") { "'${it.replace("'", "''")}'" }
                database.execSQL("DELETE FROM game_prices WHERE gameName IN ($inClause)")
            }
        }

        fun getInstance(context: Context): GameTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameTrackerDatabase::class.java,
                    "game_tracker_db"
                )
                .addMigrations(
                    MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9,
                    MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}