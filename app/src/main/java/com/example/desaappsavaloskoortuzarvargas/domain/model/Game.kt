package com.example.desaappsavaloskoortuzarvargas.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Game(
    val id: Int,
    val name: String,
    val description: String,
    val releaseDate: String,
    val imageUrl: String,
    val rating: Double,
    val currentPrices: Map<String, Float>,
    val isFavorite: Boolean = false,
    val historicalDiscount: Int = 0,
    val tags: List<String> = emptyList(),
    val dlcs: List<DLC> = emptyList(),
    val availablePlatforms: List<String> = emptyList()
)

@Serializable
data class DLC(
    val id: Int,
    val name: String,
    val gameId: Int,
    val imageUrl: String,
    val currentPrices: Map<String, Float>,
    val historicalDiscount: Int = 0,
    val releaseDate: String = "",
    val description: String = ""
)

@Serializable
data class PriceHistory(
    val gameId: Int,
    val platform: String,
    val price: Float,
    val discount: Int,
    val date: String,
    val isHistoricalLowest: Boolean = false
)

