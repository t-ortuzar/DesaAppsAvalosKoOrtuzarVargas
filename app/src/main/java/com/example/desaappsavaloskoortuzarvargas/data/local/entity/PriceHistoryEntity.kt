package com.example.desaappsavaloskoortuzarvargas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Records every price snapshot we observe over time.
 * Used to detect:
 *  - Historical low prices (current < all previous)
 *  - Permanent price drops (base price reduced by publisher)
 */
@Entity(tableName = "price_history")
data class PriceHistoryEntity(
    @field:PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameName: String,
    val storeName: String,
    val currentPrice: Float,
    val retailPrice: Float,       // base/original price (non-discounted)
    val discountPercent: Int,
    val currency: String = "ARS",
    val timestamp: Long = System.currentTimeMillis()
)

