package com.example.desaappsavaloskoortuzarvargas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cached game price from CheapShark API.
 * Composite key: gameId + storeName uniquely identifies a price entry.
 */
@Entity(tableName = "game_prices")
data class GamePriceEntity(
    @field:PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameId: Int,
    val gameName: String,
    val storeName: String,
    val currentPrice: Float,
    val retailPrice: Float,
    val savings: Float,
    val dealUrl: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

