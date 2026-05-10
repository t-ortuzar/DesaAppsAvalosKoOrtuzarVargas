package com.example.desaappsavaloskoortuzarvargas.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DiscountedGame(
    val gameId: Int,
    val gameName: String,
    val imageUrl: String,
    val platform: String,
    val originalPrice: Float,
    val currentPrice: Float,
    val discountPercentage: Int,
    val isFree: Boolean = false,
    val endDate: String? = null,
    val isHistoricalLowest: Boolean = false
)

