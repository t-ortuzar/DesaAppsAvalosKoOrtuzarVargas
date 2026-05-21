package com.example.desaappsavaloskoortuzarvargas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.desaappsavaloskoortuzarvargas.data.local.entity.GameImageEntity

@Dao
interface GameImageDao {

    @Query("SELECT * FROM game_images WHERE gameId = :gameId")
    suspend fun getImageForGame(gameId: Int): GameImageEntity?

    @Query("SELECT * FROM game_images")
    suspend fun getAllImages(): List<GameImageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(image: GameImageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(images: List<GameImageEntity>)

    @Query("DELETE FROM game_images")
    suspend fun deleteAll()
}

