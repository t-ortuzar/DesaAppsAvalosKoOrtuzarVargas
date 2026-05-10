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
    val historicalDiscount: Int = 0 // Porcentaje del descuento histórico más grande
)

@Serializable
data class PriceHistory(
    val gameId: Int,
    val platform: String,
    val price: Float,
    val discount: Int, // Porcentaje de descuento
    val date: String,
    val isHistoricalLowest: Boolean = false
)

@Serializable
data class Platform(
    val id: String,
    val name: String,
    val icon: String
)

data class UserFavorite(
    val gameId: Int,
    val gameName: String,
    val addedDate: String
)

