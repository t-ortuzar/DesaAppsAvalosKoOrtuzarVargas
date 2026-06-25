package com.example.desaappsavaloskoortuzarvargas.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

// ── GOG Catalog API response models ──

@Serializable
data class GogCatalogResponse(
    val products: List<GogProduct> = emptyList()
)

@Serializable
data class GogProduct(
    val id: Long = 0,
    val title: String = "",
    val slug: String = "",
    val price: GogPriceData? = null,
    val storeLink: String = ""
)

/**
 * GOG Catalog API v1 price object.
 *
 * The API returns fields named "final" and "base" (not "finalAmount"/"baseAmount").
 * discountPercentage is returned as a STRING, not an integer.
 * We also keep legacy field names as fallbacks for older API versions.
 */
@Serializable
data class GogPriceData(
    // GOG catalog API v1 field names
    @SerialName("final")
    val finalPrice: String = "0",
    @SerialName("base")
    val basePrice: String = "0",
    val discountPercentage: String = "0",   // returned as String by GOG API
    val isFree: Boolean = false,
    val currency: String = "USD",
    // Legacy / alternate field names (some API versions or responses)
    val amount: String = "0",
    val baseAmount: String = "0",
    val finalAmount: String = "0",
    val isDiscounted: Boolean = false
) {
    /** Resolve the current sale price from whichever field is populated. */
    fun resolvedFinalPrice(): Float =
        finalPrice.toFloatOrNull()?.takeIf { it > 0f }
            ?: finalAmount.toFloatOrNull()?.takeIf { it > 0f }
            ?: amount.toFloatOrNull()
            ?: 0f

    /** Resolve the original/base price from whichever field is populated. */
    fun resolvedBasePrice(): Float {
        val final = resolvedFinalPrice()
        return basePrice.toFloatOrNull()?.takeIf { it > 0f }
            ?: baseAmount.toFloatOrNull()?.takeIf { it > 0f }
            ?: final
    }

    /** Resolve the discount percentage, computing it from prices if the API returned 0. */
    fun resolvedDiscountPct(): Int {
        val fromApi = discountPercentage.toIntOrNull() ?: 0
        if (fromApi > 0) return fromApi
        val final = resolvedFinalPrice()
        val base = resolvedBasePrice()
        return if (base > final && base > 0f) ((1f - final / base) * 100).toInt() else 0
    }
}

/**
 * Service to fetch REAL prices from GOG for Argentina.
 *
 * GOG uses their catalog API v1 with countryCode=AR and currencyCode=USD
 * (GOG does not support ARS regional pricing).
 *
 * Endpoint: https://catalog.gog.com/v1/catalog
 */
class GogPriceService {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun searchGamePrice(title: String): StorePrice? = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(title, "UTF-8")
            val url = URL(
                "https://catalog.gog.com/v1/catalog?limit=5&query=$encoded&countryCode=AR&locale=es-AR&currencyCode=USD"
            )
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.setRequestProperty("Accept", "application/json")

            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().readText()
                val catalog = json.decodeFromString<GogCatalogResponse>(response)

                if (catalog.products.isEmpty()) return@withContext null

                // Only accept an exact title match to avoid wrong-game results.
                val match = catalog.products.firstOrNull { product ->
                    product.title.equals(title, ignoreCase = true)
                } ?: return@withContext null

                val priceData = match.price ?: return@withContext null

                val finalPrice = priceData.resolvedFinalPrice()
                val basePrice  = priceData.resolvedBasePrice()
                val discountPct = priceData.resolvedDiscountPct()

                // Skip if price is 0 and the game isn't explicitly marked free.
                if (finalPrice == 0f && !priceData.isFree) return@withContext null

                // Build the store URL.
                // storeLink may be a full URL ("https://www.gog.com/en/game/slug"),
                // a relative path ("/game/slug"), or empty.
                // slug is the slug of the game page (e.g. "baldurs_gate_3").
                val storeUrl = when {
                    match.storeLink.startsWith("http") -> match.storeLink
                    match.storeLink.isNotEmpty() -> {
                        val link = if (match.storeLink.startsWith("/")) match.storeLink else "/${match.storeLink}"
                        "https://www.gog.com$link"
                    }
                    match.slug.isNotEmpty() -> "https://www.gog.com/game/${match.slug}"
                    else -> "https://www.gog.com/games?search=${URLEncoder.encode(title, "UTF-8")}"
                }

                StorePrice(
                    storeName = "GOG",
                    currentPrice = finalPrice,
                    originalPrice = basePrice,
                    discountPercent = discountPct,
                    currency = priceData.currency.ifBlank { "USD" },
                    isFree = priceData.isFree,
                    storeUrl = storeUrl
                )
            } else null
        } catch (_: Exception) {
            null
        }
    }
}
