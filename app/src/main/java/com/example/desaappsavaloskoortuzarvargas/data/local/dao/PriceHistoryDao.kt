package com.example.desaappsavaloskoortuzarvargas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.desaappsavaloskoortuzarvargas.data.local.entity.PriceHistoryEntity

@Dao
interface PriceHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: PriceHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<PriceHistoryEntity>)

    /** Get the all-time lowest price we have recorded for a game on a store. */
    @Query("SELECT MIN(currentPrice) FROM price_history WHERE gameName = :gameName AND storeName = :storeName")
    suspend fun getHistoricalLowPrice(gameName: String, storeName: String): Float?

    /** Get the all-time lowest price across ALL stores for a game. */
    @Query("SELECT MIN(currentPrice) FROM price_history WHERE gameName = :gameName AND currentPrice > 0")
    suspend fun getHistoricalLowPriceAnyStore(gameName: String): Float?

    /**
     * Get the most recent previous retail (base) price for a game on a store.
     * Used to detect permanent price drops: if current retailPrice < previous retailPrice.
     */
    @Query("""
        SELECT retailPrice FROM price_history 
        WHERE gameName = :gameName AND storeName = :storeName AND retailPrice > 0
        ORDER BY timestamp DESC LIMIT 1 OFFSET 1
    """)
    suspend fun getPreviousRetailPrice(gameName: String, storeName: String): Float?

    /** Get all history for a game, newest first. */
    @Query("SELECT * FROM price_history WHERE gameName = :gameName ORDER BY timestamp DESC")
    suspend fun getHistoryForGame(gameName: String): List<PriceHistoryEntity>

    /** Delete history older than a given timestamp to keep DB size manageable. */
    @Query("DELETE FROM price_history WHERE timestamp < :olderThan")
    suspend fun deleteOlderThan(olderThan: Long)

    /** Count total records (for maintenance). */
    @Query("SELECT COUNT(*) FROM price_history")
    suspend fun getCount(): Int
}

