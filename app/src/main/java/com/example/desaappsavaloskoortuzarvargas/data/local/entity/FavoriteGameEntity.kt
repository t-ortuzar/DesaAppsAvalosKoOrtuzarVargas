package com.example.desaappsavaloskoortuzarvargas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persisted favorite game. Stored in Room so favorites survive app restarts.
 */
@Entity(tableName = "favorite_games")
data class FavoriteGameEntity(
    @field:PrimaryKey
    val gameId: Int,
    val gameName: String,
    val addedAt: Long = System.currentTimeMillis()
)

