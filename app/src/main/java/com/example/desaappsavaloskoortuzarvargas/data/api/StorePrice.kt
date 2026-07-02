package com.example.desaappsavaloskoortuzarvargas.data.api

/**
 * Unified price result from any store (Steam, Epic, GOG).
 * Every price here is the REAL price for Argentina, fetched
 * directly from the store's API with the Argentine country code.
 */
data class StorePrice(
    val storeName: String,
    val currentPrice: Float,
    val originalPrice: Float,
    val discountPercent: Int,
    val currency: String,        // "ARS", "USD", etc.
    val isFree: Boolean = false,
    val storeUrl: String = "",
    val formattedPrice: String = "",     // Pre-formatted by the store (e.g., "ARS$ 8.799,00")
    val formattedOriginal: String = "",  // Pre-formatted original price
    val imageUrl: String = "",           // Game image from this store's CDN (landscape preferred)
    val discountEndTimestamp: Long? = null,  // Epoch millis when the discount/free period ends (null = unknown)
    val isGamePass: Boolean = false,     // True when included in Xbox Game Pass
    val isEaPlay: Boolean = false,       // True when included in EA Play subscription (shown as free on Steam)
    val isVerifiedLink: Boolean = false  // True when store link is catalog-verified but no live price available.
                                         // These entries are shown as "Ver en [store] →" and are NOT cached in DB.
) {
    val isDiscounted: Boolean get() = discountPercent > 0
    val isArs: Boolean get() = currency == "ARS"
}

