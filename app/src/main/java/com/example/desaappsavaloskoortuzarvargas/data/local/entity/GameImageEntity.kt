package com.example.desaappsavaloskoortuzarvargas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cached game image URL, mapped from game ID.
 */
@Entity(tableName = "game_images")
data class GameImageEntity(
    @field:PrimaryKey
    val gameId: Int,
    val gameName: String,
    val imageUrl: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

