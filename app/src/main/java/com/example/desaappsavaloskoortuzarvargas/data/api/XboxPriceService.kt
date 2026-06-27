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
 * URL format: https://www.xbox.com/es-AR/games/store/{slug}/{ProductId}
 * (No /0010 suffix — that specific SKU path causes 404 for some products)
 *
 * When a known Xbox product ID is provided, uses direct product lookup for accuracy.
 * Always validates that the returned product is available on PC (Windows.Desktop),
 * to prevent showing console-only Xbox games in this PC-focused app.
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

    /**
     * Parse a Microsoft Store formatted price string to a Float value.
     * Handles Argentine format (e.g. "ARS$ 5.699,00") and US format ("5699.00").
     */
    private fun parseFormattedArsPrice(formatted: String): Float? {
        if (formatted.isBlank()) return null
        // Strip everything except digits, dots, and commas
        val stripped = formatted.replace(Regex("[^0-9.,]"), "")
        if (stripped.isEmpty()) return null
        return try {
            when {
                // Argentine format: last separator is comma → comma = decimal, dot = thousands
                // e.g. "5.699,00" → 5699.00
                stripped.contains(',') &&
                stripped.lastIndexOf(',') > stripped.lastIndexOf('.') ->
                    stripped.replace(".", "").replace(",", ".").toFloat()
                // US format: only dots → e.g. "5699.00"
                stripped.contains('.') && !stripped.contains(',') ->
                    stripped.toFloat()
                // No separators
                else -> stripped.replace(",", "").toFloat()
            }
        } catch (_: NumberFormatException) { null }
    }

    /**
     * Validate that a game is PC-available by checking the Xbox website's PC-filtered search.
     * URL: https://www.xbox.com/es-AR/search/results/games?q={query}&PlayWith=PC
     *
     * The Xbox website's server-side rendering includes the search results (product IDs) in
     * __NEXT_DATA__ within the initial HTML. If the game's productId does NOT appear in the
     * PC-filtered results, it is console-only and must not be shown in this PC-focused app.
     *
     * Returns true (permissive) on any network / parsing error.
     */
    private suspend fun validateXboxPcSearch(query: String, productId: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val encoded = URLEncoder.encode(query, "UTF-8")
                val url = URL(
                    "https://www.xbox.com/es-AR/search/results/games?q=$encoded&PlayWith=PC"
                )
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 8000
                conn.readTimeout = 12000
                conn.instanceFollowRedirects = true
                conn.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                )
                conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,*/*;q=0.8")
                conn.setRequestProperty("Accept-Language", "es-AR,es;q=0.9,en;q=0.8")
                if (conn.responseCode != 200) return@withContext true // Permissive on error

                val html = conn.inputStream.bufferedReader().readText()

                // If the response is suspiciously short (< 5 KB) the page likely didn't
                // render its search results server-side — fall back to permissive.
                if (html.length < 5000) return@withContext true

                // The Xbox website embeds search results (including product IDs) in
                // __NEXT_DATA__ and in data attributes in the SSR-rendered HTML.
                // If the productId is absent from the PC-filtered results page, the game is
                // console-only.
                html.contains(productId, ignoreCase = true)
            } catch (_: Exception) {
                true // Permissive on network / IO error
            }
        }

    /** Extract a StorePrice from a known MsStoreCardModel result. */
    private fun extractPrice(match: MsStoreCardModel, slug: String): StorePrice? {
        if (match.productId.isEmpty()) return null

        // URL without /0010 suffix — works for all products and avoids 404
        // for games where the 0010 (standard edition) SKU doesn't exist.
        val storeUrl = "https://www.xbox.com/es-AR/games/store/$slug/${match.productId}"

        val baseSku = match.skusSummary.firstOrNull { it.skuId == "0010" }
            ?: match.skusSummary.firstOrNull()

        val defaultSalePrice = baseSku?.salePrices
            ?.firstOrNull { it.badgeId == "default" }
            ?.price

        // Game Pass detection: prefer explicit displayPrice text over price == 0.0 alone.
        // Also treat price=0.0 WITH a strikethrough price as Game Pass (the struck-through
        // value is the normal purchase price shown to the user in the store UI).
        val isGamePass = match.displayPrice.contains("incluido", ignoreCase = true) ||
            match.displayPrice.contains("included", ignoreCase = true) ||
            match.displayPrice.contains("game pass", ignoreCase = true) ||
            match.displayPrice.contains("gamepass", ignoreCase = true) ||
            (match.price == 0.0 && match.strikethroughPrice.isNotEmpty())

        // For Game Pass games, the strikethroughPrice is the standard purchase price that
        // the store crosses out for subscribers — parse it as the retail price.
        val strikethroughRetail = if (isGamePass && match.strikethroughPrice.isNotEmpty())
            parseFormattedArsPrice(match.strikethroughPrice)
        else null

        // Retail price: prefer explicit sale price, then MSRP, then strikethrough, then top-level
        val retailPrice = when {
            defaultSalePrice != null && defaultSalePrice > 0.0 -> defaultSalePrice.toFloat()
            baseSku != null && baseSku.msrp > 0.0               -> baseSku.msrp.toFloat()
            strikethroughRetail != null && strikethroughRetail > 0f -> strikethroughRetail
            match.price > 0.0                                    -> match.price.toFloat()
            else                                                 -> 0f
        }

        val msrp = baseSku?.msrp?.toFloat()?.takeIf { it > 0f } ?: retailPrice

        // Nothing useful to show — no price and not a Game Pass title
        if (retailPrice == 0f && !isGamePass) return null

        val discountPct = if (msrp > retailPrice && msrp > 0f)
            ((1f - retailPrice / msrp) * 100).toInt() else 0

        // currentPrice = standard retail price (what the user pays to own the game).
        // When isGamePass = true the UI also shows "✓ Xbox Game Pass" alongside the price,
        // so the user knows it's included in their subscription but can also buy it outright.
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
     * Fetch price for a game from Xbox Store by known product ID.
     * Uses the Microsoft Display Catalog API for direct product lookup —
     * much more reliable than text search since product IDs are not text-searchable.
     *
     * Endpoint: displaycatalog.mp.microsoft.com/v7.0/products?bigIds={id}&market=AR
     */
    suspend fun fetchProductPrice(productId: String, gameName: String): StorePrice? =
        withContext(Dispatchers.IO) {
            try {
                // Primary: Display Catalog API — accepts product IDs directly
                val catalogUrl = URL(
                    "https://displaycatalog.mp.microsoft.com/v7.0/products" +
                    "?bigIds=${URLEncoder.encode(productId, "UTF-8")}" +
                    "&market=AR&languages=es-AR&MS-CV=DGU1mcuYo0WMMp"
                )
                val conn = catalogUrl.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 10000
                conn.readTimeout = 10000
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                if (conn.responseCode == 200) {
                    val parsed = runCatching {
                        json.decodeFromString<MsStoreSearchResponse>(
                            conn.inputStream.bufferedReader().readText()
                        )
                    }.getOrNull()
                    val product = parsed?.Products
                        ?.firstOrNull { it.ProductId.equals(productId, ignoreCase = true) }
                        ?: parsed?.Products?.firstOrNull()
                    if (product != null) {
                        // PC platform validation: only accept products available on Windows.Desktop.
                        // This prevents console-only Xbox games from showing up in our PC-focused app.
                        // If AllowedPlatforms is absent/empty for an availability, we accept it
                        // (many PC games don't have explicit platform restrictions in the catalog).
                        val isPcAvailable = product.DisplaySkuAvailabilities.any { sku ->
                            sku.Availabilities.any { avail ->
                                val platforms = avail.Conditions?.ClientConditions?.AllowedPlatforms
                                platforms.isNullOrEmpty() ||
                                platforms.any { p ->
                                    val name = p.PlatformName.lowercase()
                                    name.contains("windows.desktop") ||
                                    name.contains("windows.pc") ||
                                    name == "windows"
                                }
                            }
                        }
                        if (!isPcAvailable && product.DisplaySkuAvailabilities.any { sku ->
                            sku.Availabilities.any { it.Conditions?.ClientConditions?.AllowedPlatforms?.isNotEmpty() == true }
                        }) {
                            // Product has explicit platform restrictions and none are PC → skip
                            return@withContext null
                        }

                        val priceData = product.DisplaySkuAvailabilities
                            .asSequence()
                            .flatMap { it.Availabilities.asSequence() }
                            .mapNotNull { it.OrderManagementData?.Price }
                            .firstOrNull { it.ListPrice > 0 }
                        if (priceData != null) {
                            val retailPrice = priceData.ListPrice.toFloat()
                            val msrp = if (priceData.MSRP > 0) priceData.MSRP.toFloat() else retailPrice
                            val currency = priceData.CurrencyCode.ifEmpty { "ARS" }
                            val discountPct = if (msrp > retailPrice && msrp > 0f)
                                ((1f - retailPrice / msrp) * 100).toInt() else 0
                            val slug = titleSlug(gameName)
                            // No /0010 suffix — avoids 404 for products without a standard SKU
                            val storeUrl = "https://www.xbox.com/es-AR/games/store/$slug/$productId"
                            return@withContext StorePrice(
                                storeName = "Xbox / Microsoft",
                                currentPrice = retailPrice,
                                originalPrice = msrp,
                                discountPercent = discountPct,
                                currency = currency,
                                isFree = false,
                                storeUrl = storeUrl
                            )
                        }
                    }
                }

                // Fallback: use the storeedgefd search API (less reliable for product IDs,
                // but might work as a secondary attempt)
                val searchUrl = URL(
                    "https://storeedgefd.dsx.mp.microsoft.com/v9.0/search" +
                    "?market=AR&locale=es-AR&query=${URLEncoder.encode(gameName, "UTF-8")}&deviceFamily=Windows.Desktop"
                )
                val conn2 = searchUrl.openConnection() as HttpURLConnection
                conn2.requestMethod = "GET"
                conn2.connectTimeout = 10000
                conn2.readTimeout = 10000
                conn2.setRequestProperty("Accept", "application/json")
                conn2.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                if (conn2.responseCode != 200) return@withContext null
                val parsed2 = runCatching {
                    json.decodeFromString<MsStoreApiResponse>(
                        conn2.inputStream.bufferedReader().readText()
                    )
                }.getOrNull() ?: return@withContext null
                val results = parsed2.payload?.searchResults ?: return@withContext null
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

                // Normalize punctuation so "Star Wars Jedi Survivor" matches
                // Xbox results titled "Star Wars Jedi: Survivor" (colon stripped).
                fun normPunct(t: String) = t.lowercase().trim()
                    .replace(Regex("[:'\"!?,.]"), " ")
                    .replace(Regex("\\s+"), " ").trim()
                val normQuery = normPunct(rawQuery)

                // ── Title matching ──
                var match = results.firstOrNull { normPunct(it.title) == normQuery }

                if (match == null) {
                    match = results.firstOrNull { normPunct(stripEditionSuffix(it.title)) == normQuery }
                }

                if (match == null) {
                    match = results.firstOrNull {
                        normPunct(it.title).startsWith(normQuery) &&
                        normPunct(it.title) != normQuery
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

                // PC validation: verify via the Xbox website's PC-filtered search.
                // The storeedgefd API with deviceFamily=Windows.Desktop does NOT reliably
                // filter out console-only games (e.g. Baldur's Gate 3 appears in results
                // even though it is Xbox Series X|S only — not on PC/Windows Store).
                // We cross-check by fetching the Xbox website search with &PlayWith=PC:
                // if the product ID is absent from the SSR-rendered page, the game is
                // console-only and must be excluded.
                if (match.productId.isNotEmpty()) {
                    val isPcAvailable = validateXboxPcSearch(searchQuery, match.productId)
                    if (!isPcAvailable) return@withContext null
                }

                extractPrice(match, titleSlug(title))
            } catch (_: Exception) {
                null
            }
        }
}

