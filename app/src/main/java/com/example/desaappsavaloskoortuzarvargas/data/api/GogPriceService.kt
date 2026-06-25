package com.example.desaappsavaloskoortuzarvargas.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

@Serializable
data class GogPriceData(
    val amount: String = "0",
    val baseAmount: String = "0",
    val finalAmount: String = "0",
    val isDiscounted: Boolean = false,
    val discountPercentage: Int = 0,
    val currency: String = "USD",
    val isFree: Boolean = false
)

/**
 * Service to fetch REAL prices from GOG for Argentina.
 *
 * GOG uses their catalog API with countryCode parameter.
 * Note: GOG may show prices in USD for Argentina (they don't always
 * have ARS regional pricing), but the price IS what you'd actually pay.
 *
 * Endpoint: https://catalog.gog.com/v1/catalog
 */
class GogPriceService {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Search for a game on GOG and return its Argentine price.
     * Returns null if the game is not found or an error occurs.
     */
    suspend fun searchGamePrice(title: String): StorePrice? = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(title, "UTF-8")
            val url = URL(
                "https://catalog.gog.com/v1/catalog?limit=5&query=$encoded&countryCode=AR&locale=es-AR&currencyCode=ARS"
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

                // Only use an exact title match — do NOT fall back to first result.
                // Returning a wrong game is worse than returning null.
                val match = catalog.products.firstOrNull { product ->
                    product.title.equals(title, ignoreCase = true)
                } ?: return@withContext null

                val priceData = match.price ?: return@withContext null

                val finalPrice = priceData.finalAmount.toFloatOrNull() ?: 0f
                val basePrice = priceData.baseAmount.toFloatOrNull() ?: finalPrice

                // If the API returns 0 and the game is NOT explicitly marked as free,
                // it means GOG doesn't have ARS regional pricing — skip to avoid showing
                // a paid game as "FREE".
                if (finalPrice == 0f && !priceData.isFree) return@withContext null

                // Build the store URL.  GOG's API returns a `slug` field (e.g. "what_remains_of_edith_finch").
                // The product page is at gog.com/game/{slug}.
                // storeLink (if present) is a relative path like "/game/slug" – preferred when available.
                // Fallback: title search so the user at least lands near the game.
                val storeUrl = when {
                    match.storeLink.isNotEmpty() -> {
                        val link = if (match.storeLink.startsWith("/")) match.storeLink else "/${match.storeLink}"
                        "https://www.gog.com$link"
                    }
                    match.slug.isNotEmpty() -> "https://www.gog.com/game/${match.slug}"
                    else -> {
                        val encoded = URLEncoder.encode(title, "UTF-8")
                        "https://www.gog.com/games?search=$encoded"
                    }
                }

                StorePrice(
                    storeName = "GOG",
                    currentPrice = finalPrice,
                    originalPrice = basePrice,
                    discountPercent = priceData.discountPercentage,
                    currency = priceData.currency,
                    isFree = priceData.isFree,
                    storeUrl = storeUrl
                )
            } else null
        } catch (_: Exception) {
            null
        }
    }
}

