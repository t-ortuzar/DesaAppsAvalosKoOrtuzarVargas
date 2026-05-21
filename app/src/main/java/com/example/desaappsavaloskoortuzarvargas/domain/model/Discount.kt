package com.example.desaappsavaloskoortuzarvargas.domain.model

import kotlinx.serialization.Serializable

/**
 * Types of offers shown in the Offers screen.
 */
@Serializable
enum class OfferType {
    /** Regular sale / percentage discount */
    SALE,
    /** Publisher permanently reduced the base price */
    PERMANENT_PRICE_DROP,
    /** Game is free for a limited time */
    TEMPORARILY_FREE,
    /** Game is always free to play */
    F2P
}

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
    val isF2P: Boolean = false,
    val isTemporarilyFree: Boolean = false,
    val endDate: String? = null,
    val isHistoricalLowest: Boolean = false,
    val tags: List<String> = emptyList(),
    val offerType: OfferType = OfferType.SALE,
    val endTimestamp: Long? = null,         // epoch millis when deal ends
    val previousBasePrice: Float? = null    // for PERMANENT_PRICE_DROP: what the old base price was
)

