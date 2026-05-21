package com.example.desaappsavaloskoortuzarvargas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.desaappsavaloskoortuzarvargas.data.local.entity.GamePriceEntity

@Dao
interface GamePriceDao {

    @Query("SELECT * FROM game_prices WHERE gameId = :gameId")
    suspend fun getPricesForGame(gameId: Int): List<GamePriceEntity>

    @Query("SELECT * FROM game_prices WHERE gameName = :gameName")
    suspend fun getPricesForGameByName(gameName: String): List<GamePriceEntity>

    @Query("SELECT * FROM game_prices")
    suspend fun getAllPrices(): List<GamePriceEntity>

    /** Get all cached prices that currently have an active discount (savings > 0). */
    @Query("SELECT * FROM game_prices WHERE savings > 0 ORDER BY savings DESC")
    suspend fun getDiscountedPrices(): List<GamePriceEntity>

    /** Get all cached prices where the game is currently free. */
    @Query("SELECT * FROM game_prices WHERE currentPrice = 0")
    suspend fun getFreePrices(): List<GamePriceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(prices: List<GamePriceEntity>)

    @Query("DELETE FROM game_prices WHERE gameId = :gameId")
    suspend fun deletePricesForGame(gameId: Int)

    @Query("DELETE FROM game_prices WHERE gameName = :gameName")
    suspend fun deletePricesForGameByName(gameName: String)

    @Query("DELETE FROM game_prices")
    suspend fun deleteAll()

    /** Get the oldest lastUpdated timestamp for a given game. Null if no prices cached. */
    @Query("SELECT MIN(lastUpdated) FROM game_prices WHERE gameName = :gameName")
    suspend fun getOldestTimestamp(gameName: String): Long?

    /** Get all distinct game names that have cached prices, ordered by oldest first. */
    @Query("SELECT gameName FROM game_prices GROUP BY gameName ORDER BY MIN(lastUpdated) ASC")
    suspend fun getAllCachedGameNamesOldestFirst(): List<String>

    /** Get game names whose prices are older than the given timestamp. */
    @Query("SELECT DISTINCT gameName FROM game_prices WHERE lastUpdated < :olderThan ORDER BY lastUpdated ASC")
    suspend fun getStaleGameNames(olderThan: Long): List<String>

    /** Count of distinct games with cached prices. */
    @Query("SELECT COUNT(DISTINCT gameName) FROM game_prices")
    suspend fun getCachedGameCount(): Int
}
