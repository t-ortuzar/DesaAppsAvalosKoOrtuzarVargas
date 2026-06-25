package com.example.desaappsavaloskoortuzarvargas.data.api

/**
 * Typed data classes for GOG catalog API responses.
 * Kept for test compatibility — the GogPriceService uses JsonObject
 * parsing internally for resilience, but these models document the
 * expected GOG API shape.
 */
data class GogCatalogResponse(
    val products: List<GogProduct> = emptyList()
)

data class GogProduct(
    val id: Long = 0L,
    val title: String = "",
    val price: GogPriceData? = null,
    val storeLink: String = ""
)

data class GogPriceData(
    val amount: String = "0",
    val baseAmount: String = "0",
    val finalAmount: String = "0",
    val isDiscounted: Boolean = false,
    val discountPercentage: Int = 0,
    val currency: String = "USD",
    val isFree: Boolean = false
)

