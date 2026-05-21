package com.example.desaappsavaloskoortuzarvargas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.desaappsavaloskoortuzarvargas.data.local.entity.FavoriteGameEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteGameDao {

    @Query("SELECT * FROM favorite_games ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteGameEntity>>

    @Query("SELECT * FROM favorite_games ORDER BY addedAt DESC")
    suspend fun getAllFavoritesList(): List<FavoriteGameEntity>

    @Query("SELECT gameId FROM favorite_games")
    suspend fun getAllFavoriteIds(): List<Int>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_games WHERE gameId = :gameId)")
    suspend fun isFavorite(gameId: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteGameEntity)

    @Query("DELETE FROM favorite_games WHERE gameId = :gameId")
    suspend fun removeFavorite(gameId: Int)

    @Query("SELECT COUNT(*) FROM favorite_games")
    suspend fun getFavoriteCount(): Int
}

