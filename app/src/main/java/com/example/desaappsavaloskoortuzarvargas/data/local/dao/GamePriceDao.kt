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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(prices: List<GamePriceEntity>)

    @Query("DELETE FROM game_prices WHERE gameId = :gameId")
    suspend fun deletePricesForGame(gameId: Int)

    @Query("DELETE FROM game_prices WHERE gameName = :gameName")
    suspend fun deletePricesForGameByName(gameName: String)

    @Query("DELETE FROM game_prices")
    suspend fun deleteAll()
}

