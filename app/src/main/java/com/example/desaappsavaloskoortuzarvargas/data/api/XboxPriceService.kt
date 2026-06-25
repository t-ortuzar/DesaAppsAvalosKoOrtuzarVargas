package com.example.desaappsavaloskoortuzarvargas.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

// ── Microsoft Store storeedgefd v9.0 actual response models ──
// Structure confirmed from live API:
// { "Payload": { "SearchResults": [ { "ProductId": "...", "Title": "...", "Price": 0.0, ... } ] } }

@Serializable
data class MsStoreApiResponse(
    @SerialName("Payload") val payload: MsStorePayload? = null
)

@Serializable
data class MsStorePayload(
    @SerialName("SearchResults") val searchResults: List<MsStoreCardModel> = emptyList()
)

@Serializable
data class MsStoreCardModel(
    @SerialName("ProductId")          val productId: String = "",
    @SerialName("Title")              val title: String = "",
    @SerialName("Price")              val price: Double = 0.0,
    @SerialName("DisplayPrice")       val displayPrice: String = "",
    @SerialName("StrikethroughPrice") val strikethroughPrice: String = "",
    @SerialName("SkusSummary")        val skusSummary: List<MsSkuSummary> = emptyList()
)

@Serializable
data class MsSkuSummary(
    @SerialName("SkuId")       val skuId: String = "",
    @SerialName("MSRP")        val msrp: Double = 0.0,
    @SerialName("SalePrices")  val salePrices: List<MsSalePrice> = emptyList()
)

@Serializable
data class MsSalePrice(
    @SerialName("Price")        val price: Double = 0.0,
    @SerialName("DisplayPrice") val displayPrice: String = "",
    @SerialName("BadgeId")      val badgeId: String = ""
)

// Legacy models kept to avoid breaking any remaining references
@Serializable data class MsEdgeSearchResponse(val SearchResults: List<MsEdgeSearchResult> = emptyList())
@Serializable data class MsEdgeSearchResult(val SearchId: String = "", val Payload: List<MsEdgePayloadItem> = emptyList())
@Serializable data class MsEdgePayloadItem(val PayloadType: String = "", val ProductSummaryDetails: MsEdgeProductDetails? = null, val ProductDetails: MsEdgeProductDetails? = null) { fun details() = ProductSummaryDetails ?: ProductDetails }
@Serializable data class MsEdgeProductDetails(val ProductId: String = "", val Title: String = "", val Price: MsEdgePrice? = null)
@Serializable data class MsEdgePrice(val ListPrice: Double = 0.0, val MSRP: Double = 0.0, val CurrencyCode: String = "USD")
@Serializable data class MsStoreSearchResponse(val Products: List<MsStoreProduct> = emptyList())
@Serializable data class MsStoreProduct(val ProductId: String = "", val LocalizedProperties: List<MsLocalizedProperty> = emptyList(), val DisplaySkuAvailabilities: List<MsDisplaySkuAvailability> = emptyList())
@Serializable data class MsLocalizedProperty(val ProductTitle: String = "", val ProductDescription: String = "")
@Serializable data class MsDisplaySkuAvailability(val Availabilities: List<MsAvailability> = emptyList())
@Serializable data class MsAvailability(val OrderManagementData: MsOrderManagementData? = null, val Conditions: MsConditions? = null)
@Serializable data class MsOrderManagementData(val Price: MsPrice? = null)
@Serializable data class MsPrice(val CurrencyCode: String = "USD", val ListPrice: Double = 0.0, val MSRP: Double = 0.0, val WholesalePrice: Double? = null)
@Serializable data class MsConditions(val ClientConditions: MsClientConditions? = null)
@Serializable data class MsClientConditions(val AllowedPlatforms: List<MsPlatform> = emptyList())
@Serializable data class MsPlatform(val PlatformName: String = "")

/**
 * Service to fetch prices from the Xbox / Microsoft Store (PC games).
 *
 * Uses the Microsoft Store storeedgefd v9.0 search API.
 * URL format: https://www.xbox.com/es-ar/games/store/{slug}/{ProductId}/0010
 *
 * When a known Xbox product ID is provided, uses direct product lookup for accuracy.
 */
class XboxPriceService {

    private val json = Json { ignoreUnknownKeys = true }

    private fun titleSlug(title: String): String =
        title.lowercase()
            .replace("'", "").replace(":", "").replace(".", "")
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .ifEmpty { "game" }

    /** Strip edition/upgrade suffixes from an API-returned title for fuzzy matching. */
    private fun stripEditionSuffix(t: String): String =
        t.lowercase().trim()
            .replace(Regex("\\s+(premium|deluxe|ultimate|standard|digital|gold|complete|royal|edicin|edicion)\\s*(edition|upgrade|content|pack|digital)?\\s*$"), "")
            .replace(Regex("\\s+(edition|upgrade)\\s*$"), "")
            .trim()

    /** Extract a StorePrice from a known MsStoreCardModel result. */
    private fun extractPrice(match: MsStoreCardModel, slug: String): StorePrice? {
        if (match.productId.isEmpty()) return null

        val storeUrl = "https://www.xbox.com/es-ar/games/store/$slug/${match.productId}/0010"

        val baseSku = match.skusSummary.firstOrNull { it.skuId == "0010" }
            ?: match.skusSummary.firstOrNull()

        val defaultSalePrice = baseSku?.salePrices
            ?.firstOrNull { it.badgeId == "default" }
            ?.price

        // Retail price: prefer explicit sale price, then MSRP, then top-level price
        val retailPrice = when {
            defaultSalePrice != null && defaultSalePrice > 0.0 -> defaultSalePrice.toFloat()
            baseSku != null && baseSku.msrp > 0.0 -> baseSku.msrp.toFloat()
            match.price > 0.0 -> match.price.toFloat()
            else -> 0f
        }

        val msrp = (baseSku?.msrp?.toFloat() ?: retailPrice)

        val isGamePass = match.price == 0.0 ||
            match.displayPrice.contains("incluido", ignoreCase = true) ||
            match.displayPrice.contains("included", ignoreCase = true)

        // Nothing useful to show — no price and not explicitly Game Pass
        if (retailPrice == 0f && !isGamePass) return null

        val discountPct = if (msrp > retailPrice && msrp > 0f)
            ((1f - retailPrice / msrp) * 100).toInt() else 0

        return StorePrice(
            storeName = "Xbox / Microsoft",
            currentPrice = retailPrice,
            originalPrice = if (msrp > 0f) msrp else retailPrice,
            discountPercent = discountPct,
            currency = "ARS",
            isFree = false,
            storeUrl = storeUrl,
            isGamePass = isGamePass
        )
    }

    /**
     * Fetch price for a game from Xbox Store by known product ID (direct lookup).
     * More reliable than search — bypasses language/title matching issues.
     */
    suspend fun fetchProductPrice(productId: String, gameName: String): StorePrice? =
        withContext(Dispatchers.IO) {
            try {
                val url = URL(
                    "https://storeedgefd.dsx.mp.microsoft.com/v9.0/search" +
                    "?market=AR&locale=es-AR&query=${URLEncoder.encode(productId, "UTF-8")}&deviceFamily=Windows.Desktop"
                )
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 10000
                conn.readTimeout = 10000
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                if (conn.responseCode != 200) return@withContext null

                val parsed = runCatching {
                    json.decodeFromString<MsStoreApiResponse>(
                        conn.inputStream.bufferedReader().readText()
                    )
                }.getOrNull() ?: return@withContext null

                val results = parsed.payload?.searchResults ?: return@withContext null

                // Find exact product ID match first, then fall back to first result
                val match = results.firstOrNull { it.productId.equals(productId, ignoreCase = true) }
                    ?: results.firstOrNull()
                    ?: return@withContext null

                extractPrice(match, titleSlug(gameName))
            } catch (_: Exception) { null }
        }

    /**
     * Search for a game on Xbox Store.
     * [titleHint] overrides the search query with a localized title (e.g., Spanish).
     */
    suspend fun searchGamePrice(title: String, titleHint: String? = null): StorePrice? =
        withContext(Dispatchers.IO) {
            try {
                val searchQuery = titleHint ?: title
                val encoded = URLEncoder.encode(searchQuery, "UTF-8")
                val url = URL(
                    "https://storeedgefd.dsx.mp.microsoft.com/v9.0/search" +
                    "?market=AR&locale=es-AR&query=$encoded&deviceFamily=Windows.Desktop"
                )
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 10000
                conn.readTimeout = 10000
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120"
                )
                if (conn.responseCode != 200) return@withContext null

                val responseText = conn.inputStream.bufferedReader().readText()
                val parsed = runCatching {
                    json.decodeFromString<MsStoreApiResponse>(responseText)
                }.getOrNull() ?: return@withContext null

                val results = parsed.payload?.searchResults ?: return@withContext null
                if (results.isEmpty()) return@withContext null

                val rawQuery = (titleHint ?: title).lowercase().trim()

                // ── Title matching ──
                var match = results.firstOrNull { it.title.lowercase().trim() == rawQuery }

                if (match == null) {
                    match = results.firstOrNull { stripEditionSuffix(it.title) == rawQuery }
                }

                if (match == null) {
                    match = results.firstOrNull {
                        it.title.lowercase().trim().startsWith(rawQuery) &&
                        it.title.lowercase().trim() != rawQuery
                    }
                }

                if (match == null) {
                    // Use original English title words for matching even when searching with hint
                    val searchTitle = titleHint ?: title
                    val firstWord = searchTitle.lowercase().split(" ").firstOrNull { it.length > 3 }
                    if (firstWord != null) {
                        match = results.firstOrNull { it.title.lowercase().contains(firstWord) }
                    }
                }

                if (match == null) return@withContext null

                extractPrice(match, titleSlug(title))
            } catch (_: Exception) {
                null
            }
        }
}

