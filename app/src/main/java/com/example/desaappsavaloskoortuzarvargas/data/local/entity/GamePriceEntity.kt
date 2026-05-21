package com.example.desaappsavaloskoortuzarvargas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cached game price from store APIs (Steam, Epic, GOG).
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
    val currency: String = "ARS",
    val lastUpdated: Long = System.currentTimeMillis(),
    val discountEndTimestamp: Long? = null  // Epoch millis when the discount ends (from store API)
)
