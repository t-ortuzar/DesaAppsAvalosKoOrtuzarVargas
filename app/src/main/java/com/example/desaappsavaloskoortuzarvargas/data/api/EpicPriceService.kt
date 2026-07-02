package com.example.desaappsavaloskoortuzarvargas.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL

// ── Epic Games Store GraphQL response models ──

@Serializable
data class EpicGraphQLResponse(
    val data: EpicCatalogData? = null
)

@Serializable
data class EpicCatalogData(
    val Catalog: EpicCatalog? = null
)

@Serializable
data class EpicCatalog(
    val searchStore: EpicSearchStore? = null
)

@Serializable
data class EpicSearchStore(
    val elements: List<EpicElement> = emptyList()
)

@Serializable
data class EpicKeyImage(
    val type: String = "",
    val url: String = ""
)

@Serializable
data class EpicElement(
    val title: String = "",
    val urlSlug: String = "",
    val price: EpicPriceInfo? = null,
    val keyImages: List<EpicKeyImage> = emptyList(),
    val promotions: EpicPromotions? = null
)

@Serializable
data class EpicPromotions(
    val promotionalOffers: List<EpicPromotionalOffer> = emptyList(),
    val upcomingPromotionalOffers: List<EpicPromotionalOffer> = emptyList()
)

@Serializable
data class EpicPromotionalOffer(
    val promotionalOffers: List<EpicPromotionDetail> = emptyList()
)

@Serializable
data class EpicPromotionDetail(
    val startDate: String = "",
    val endDate: String = ""
)

@Serializable
data class EpicPriceInfo(
    val totalPrice: EpicTotalPrice? = null
)

@Serializable
data class EpicTotalPrice(
    val originalPrice: Int = 0,    // In cents (e.g., 879900 = ARS 8799.00)
    val discountPrice: Int = 0,
    val discount: Int = 0,         // Discount amount in cents
    val currencyCode: String = "ARS",
    val fmtPrice: EpicFmtPrice? = null
)

@Serializable
data class EpicFmtPrice(
    val originalPrice: String = "",
    val discountPrice: String = ""
)

/**
 * Service to fetch REAL Argentine prices from the Epic Games Store.
 *
 * Uses Epic's public GraphQL API with country=AR to get the actual
 * price in ARS that an Argentine user sees on the store.
 *
 * Endpoint: https://graphql.epicgames.com/graphql
 */
class EpicPriceService {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Search for a game on the Epic Games Store and return the Argentine price.
     * First searches with category="games/edition/base" to get the standard edition.
     * Falls back to an unfiltered search (catches games without a "base" category).
     */
    suspend fun searchGamePrice(title: String): StorePrice? =
        searchWithTerm(title, useBaseFilter = true)
            ?: searchWithTerm(title, useBaseFilter = false)

    private suspend fun searchWithTerm(title: String, useBaseFilter: Boolean): StorePrice? = withContext(Dispatchers.IO) {
        try {
            val categoryLine = if (useBaseFilter) """category: "games/edition/base",""" else ""
            val query = """
                query searchStoreQuery(${'$'}country: String!, ${'$'}keywords: String!, ${'$'}count: Int) {
                    Catalog {
                        searchStore(
                            country: ${'$'}country,
                            keywords: ${'$'}keywords,
                            count: ${'$'}count,
                            $categoryLine
                            sortBy: "relevancy",
                            sortDir: "DESC"
                        ) {
                            elements {
                                title
                                urlSlug
                                keyImages { type url }
                                price(country: ${'$'}country) {
                                    totalPrice {
                                        originalPrice
                                        discountPrice
                                        discount
                                        currencyCode
                                        fmtPrice(locale: "es-AR") {
                                            originalPrice
                                            discountPrice
                                        }
                                    }
                                }
                                promotions {
                                    promotionalOffers {
                                        promotionalOffers { startDate endDate }
                                    }
                                }
                            }
                        }
                    }
                }
            """.trimIndent()

            val requestBody = json.encodeToString(
                kotlinx.serialization.json.JsonObject.serializer(),
                kotlinx.serialization.json.buildJsonObject {
                    put("query", kotlinx.serialization.json.JsonPrimitive(query))
                    put("variables", kotlinx.serialization.json.buildJsonObject {
                        put("country", kotlinx.serialization.json.JsonPrimitive("AR"))
                        put("keywords", kotlinx.serialization.json.JsonPrimitive(title))
                        put("count", kotlinx.serialization.json.JsonPrimitive(10))
                    })
                }
            )

            val url = URL("https://store.epicgames.com/graphql")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Accept", "application/json")
            conn.setRequestProperty("Origin", "https://store.epicgames.com")
            conn.setRequestProperty("Referer", "https://store.epicgames.com/")
            conn.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Mobile Safari/537.36"
            )
            conn.doOutput = true
            conn.outputStream.bufferedWriter().use { it.write(requestBody) }

            if (conn.responseCode != 200) return@withContext null
            val response = conn.inputStream.bufferedReader().readText()
            val parsed = json.decodeFromString<EpicGraphQLResponse>(response)
            val elements = parsed.data?.Catalog?.searchStore?.elements ?: return@withContext null
            if (elements.isEmpty()) return@withContext null

            // Normalise titles: lowercase, strip punctuation (colons, apostrophes, etc.),
            // then drop edition suffixes so "Star Wars Jedi Survivor" matches
            // "Star Wars Jedi: Survivor" and "Cyberpunk 2077" matches
            // "Cyberpunk 2077 Standard Edition".
            fun norm(t: String) = t.trim().lowercase()
                .replace(Regex("[:'\"!?,.]"), " ")   // normalize punctuation → spaces
                .replace(Regex("\\s*(standard|complete|ultimate|definitive|game of the year|goty|deluxe|premium)\\s*(edition)?\\s*$"), "")
                .replace(Regex("\\s+"), " ")
                .trim()

            val normTitle = norm(title)

            // Pass 1: exact norm match
            var match = elements.firstOrNull { norm(it.title) == normTitle }
            // Pass 2: result title starts with our normalized title
            if (match == null) {
                match = elements.firstOrNull { norm(it.title).startsWith(normTitle) }
            }
            // Pass 3: our normalized title starts with result (title is a substring)
            if (match == null) {
                match = elements.firstOrNull { normTitle.startsWith(norm(it.title)) && norm(it.title).length > 5 }
            }
            // Pass 4: lenient — all significant words in our title appear in the result title
            if (match == null) {
                val titleWords = normTitle.split(" ").filter { it.length > 4 }.toSet()
                if (titleWords.size >= 2) {
                    match = elements.firstOrNull { candidate ->
                        val cWords = norm(candidate.title).split(" ").filter { it.length > 4 }.toSet()
                        titleWords.intersect(cWords).size >= titleWords.size - 1
                    }
                }
            }
            if (match == null) return@withContext null

            // Prefer a match with a real purchase price (originalPrice > 0) over an EA Play
            // subscription entry that shows the game as free (originalPrice = 0).
            // Some games like Jedi Survivor appear on Epic both via EA Play (price=0) and as
            // a standalone purchase. We want the standalone price, not the subscription price.
            val matchedOriginalPrice = match.price?.totalPrice?.originalPrice ?: 0
            if (matchedOriginalPrice == 0) {
                // The matched element has no purchase price — look for another element in the
                // results that has the same (or similar) title AND has a non-zero price.
                val betterMatch = elements.firstOrNull { elem ->
                    val ep = elem.price?.totalPrice ?: return@firstOrNull false
                    ep.originalPrice > 0 && (
                        norm(elem.title) == normTitle ||
                        norm(elem.title).startsWith(normTitle) ||
                        normTitle.startsWith(norm(elem.title))
                    )
                }
                if (betterMatch != null) match = betterMatch
            }

            val totalPrice = match.price?.totalPrice ?: return@withContext null

                // Extract a landscape image from Epic's keyImages
                val epicImageUrl = match.keyImages.firstOrNull {
                    it.type == "DieselStoreFrontWide" || it.type == "OfferImageWide"
                }?.url ?: match.keyImages.firstOrNull {
                    it.type == "DieselGameBoxWide" || it.type == "Thumbnail"
                }?.url ?: match.keyImages.firstOrNull()?.url ?: ""

                // Epic returns 0 for free games
                val isFree = totalPrice.originalPrice == 0 && totalPrice.discountPrice == 0
                val discountPct = if (totalPrice.originalPrice > 0) {
                    ((totalPrice.discount.toFloat() / totalPrice.originalPrice) * 100).toInt()
                } else 0

                // Extract promotion end date if available
                val promoEndTs = try {
                    val endDateStr = match.promotions
                        ?.promotionalOffers?.firstOrNull()
                        ?.promotionalOffers?.firstOrNull()
                        ?.endDate
                    if (!endDateStr.isNullOrEmpty()) {
                        // Epic dates are ISO 8601: "2026-06-05T15:00:00.000Z"
                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                        sdf.parse(endDateStr)?.time
                    } else null
                } catch (_: Exception) { null }

                // Build the store URL. The Epic Store requires a locale in the path;
                // /p/{slug} without locale returns 404. Use /en-US/p/{slug}.
                // Validate the slug: Epic's API sometimes returns a UUID-like offer ID
                // (e.g. "2afe81b7bdb443e79f7b9d85b6e3024f") instead of a readable slug.
                // If the slug looks like a UUID or contains path separators from the locale
                // prefix (e.g. "en-US/p/cyberpunk-2077"), sanitise it.
                val rawSlug = match.urlSlug.trim()
                    .removePrefix("/").removePrefix("en-US/p/").removePrefix("p/")
                    .trim('/')
                // A UUID-like slug is 32 hex chars (no hyphens) or a standard UUID format.
                // Such slugs do NOT work as Epic store page URLs — fall back to search.
                val uuidPattern = Regex("^[0-9a-f]{32}$|^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
                val storeUrl = if (rawSlug.isNotEmpty() && !uuidPattern.matches(rawSlug)) {
                    "https://store.epicgames.com/en-US/p/$rawSlug"
                } else {
                    val encoded = java.net.URLEncoder.encode(title, "UTF-8")
                    "https://store.epicgames.com/en-US/browse?q=$encoded"
                }

                StorePrice(
                    storeName = "Epic Games",
                    currentPrice = totalPrice.discountPrice / 100f,
                    originalPrice = totalPrice.originalPrice / 100f,
                    discountPercent = discountPct,
                    currency = totalPrice.currencyCode,
                    isFree = isFree,
                    storeUrl = storeUrl,
                    formattedPrice = totalPrice.fmtPrice?.discountPrice ?: "",
                    formattedOriginal = totalPrice.fmtPrice?.originalPrice ?: "",
                    imageUrl = epicImageUrl,
                    discountEndTimestamp = promoEndTs
                )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Fetch ONLY the game image from Epic (lightweight, no price data).
     * Used at catalog load time for non-Steam games to populate their images.
     * Prefers wide/landscape images similar to Steam headers.
     */
    suspend fun fetchGameImage(title: String): String? = withContext(Dispatchers.IO) {
        try {
            val imageQuery = """
                query searchStoreQuery(${'$'}keywords: String!, ${'$'}count: Int) {
                    Catalog {
                        searchStore(
                            country: "US",
                            keywords: ${'$'}keywords,
                            count: ${'$'}count,
                            category: "games/edition/base",
                            sortBy: "relevancy",
                            sortDir: "DESC"
                        ) {
                            elements {
                                title
                                keyImages { type url }
                            }
                        }
                    }
                }
            """.trimIndent()

            val requestBody = json.encodeToString(
                kotlinx.serialization.json.JsonObject.serializer(),
                kotlinx.serialization.json.buildJsonObject {
                    put("query", kotlinx.serialization.json.JsonPrimitive(imageQuery))
                    put("variables", kotlinx.serialization.json.buildJsonObject {
                        put("keywords", kotlinx.serialization.json.JsonPrimitive(title))
                        put("count", kotlinx.serialization.json.JsonPrimitive(3))
                    })
                }
            )

            val url = URL("https://store.epicgames.com/graphql")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Accept", "application/json")
            conn.setRequestProperty("Origin", "https://store.epicgames.com")
            conn.setRequestProperty("Referer", "https://store.epicgames.com/")
            conn.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Mobile Safari/537.36"
            )
            conn.doOutput = true
            conn.outputStream.bufferedWriter().use { it.write(requestBody) }

            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().readText()
                val parsed = json.decodeFromString<EpicGraphQLResponse>(response)
                val elements = parsed.data?.Catalog?.searchStore?.elements ?: return@withContext null

                val match = elements.firstOrNull { it.title.equals(title, ignoreCase = true) }
                    ?: return@withContext null

                // Prefer wide/landscape images (like Steam headers)
                match.keyImages.firstOrNull {
                    it.type == "DieselStoreFrontWide" || it.type == "OfferImageWide"
                }?.url ?: match.keyImages.firstOrNull {
                    it.type == "DieselGameBoxWide" || it.type == "Thumbnail"
                }?.url ?: match.keyImages.firstOrNull()?.url
            } else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Fallback: scrape the Epic Store product page directly to extract pricing.
     * Epic uses Next.js SSR — the initial HTML contains __NEXT_DATA__ with all page props
     * including the price, so this works even when the GraphQL search returns no matches.
     *
     * Tries multiple URL formats to maximise the chance of getting ARS prices:
     *   1. /p/{slug}          — no locale prefix (simplest, clearest for cookies)
     *   2. /es-MX/p/{slug}    — Spanish locale (user-confirmed working format)
     *   3. original pageUrl   — as provided by the catalog
     *
     * Used when the GraphQL search fails for a game that has a known verified URL.
     */
    suspend fun fetchPriceFromProductPage(pageUrl: String, gameName: String): StorePrice? =
        withContext(Dispatchers.IO) {
            try {
                // Extract the bare slug from whatever URL format we received
                val slug = pageUrl
                    .removePrefix("https://store.epicgames.com/")
                    .removePrefix("en-US/p/").removePrefix("es-MX/p/").removePrefix("p/")
                    .substringBefore("?").trim('/')

                val urlsToTry = listOf(
                    "https://store.epicgames.com/p/$slug",
                    "https://store.epicgames.com/es-MX/p/$slug",
                    pageUrl.substringBefore("?")
                ).distinct()

                for (url in urlsToTry) {
                    fetchFromSingleEpicPage(url, gameName)?.let { return@withContext it }
                }
                null
            } catch (_: Exception) { null }
        }

    /** Fetch and parse a single Epic product page URL. Returns null if price not found. */
    private suspend fun fetchFromSingleEpicPage(pageUrl: String, gameName: String): StorePrice? =
        withContext(Dispatchers.IO) {
            try {
                val url = URL(pageUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 10000
                conn.readTimeout = 10000
                conn.instanceFollowRedirects = true
                conn.setRequestProperty("Accept", "text/html,application/json,*/*;q=0.9")
                conn.setRequestProperty("Accept-Language", "es-AR,es;q=0.9,en;q=0.8")
                conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                conn.setRequestProperty("Cookie", "EPIC_COUNTRY=AR; EPIC_LOCALE_COOKIE=es-AR")
                if (conn.responseCode != 200) return@withContext null

                val html = conn.inputStream.bufferedReader().readText()

                // Find __NEXT_DATA__ using a simple string search — more reliable than regex
                // because the regex lazy quantifier can fail on large nested JSON objects.
                val markerIdx = html.indexOf("""id="__NEXT_DATA__"""")
                if (markerIdx < 0) return@withContext null
                val contentStart = html.indexOf('>', markerIdx) + 1
                val contentEnd   = html.indexOf("</script>", contentStart)
                if (contentEnd <= contentStart) return@withContext null
                val jsonStr = html.substring(contentStart, contentEnd).trim()

                val root = json.parseToJsonElement(jsonStr)
                // Recursively search for Epic's price structure:
                // {"totalPrice": {"discountPrice": N, "originalPrice": N, "currencyCode": "ARS"}}
                val priceObj = findEpicTotalPrice(root, 0) ?: return@withContext null

                val discountPrice  = priceObj["discountPrice"]?.jsonPrimitive?.content?.toIntOrNull() ?: return@withContext null
                val originalPrice  = priceObj["originalPrice"]?.jsonPrimitive?.content?.toIntOrNull() ?: discountPrice
                val currency       = priceObj["currencyCode"]?.jsonPrimitive?.content ?: "ARS"
                val discountPct    = if (originalPrice > 0 && discountPrice < originalPrice)
                    ((1f - discountPrice.toFloat() / originalPrice) * 100).toInt() else 0

                StorePrice(
                    storeName = "Epic Games",
                    currentPrice = discountPrice / 100f,
                    originalPrice = originalPrice / 100f,
                    discountPercent = discountPct,
                    currency = currency,
                    isFree = discountPrice == 0 && originalPrice == 0,
                    storeUrl = pageUrl
                )
            } catch (_: Exception) { null }
        }

    /**
     * Recursively walk a JSON tree looking for an Epic "totalPrice" object with discountPrice.
     * First pass: prefer non-zero prices (originalPrice > 0) — this avoids picking up the
     * EA Play subscription price ($0) for games like Jedi Survivor that also have a standalone
     * purchase price.
     * Second pass: accept any price object (zero prices) as a last resort.
     */
    private fun findEpicTotalPrice(element: JsonElement, depth: Int): JsonObject? =
        findEpicTotalPriceFiltered(element, depth, requireNonZero = true)
            ?: findEpicTotalPriceFiltered(element, depth, requireNonZero = false)

    private fun findEpicTotalPriceFiltered(element: JsonElement, depth: Int, requireNonZero: Boolean): JsonObject? {
        if (depth > 50) return null
        return when (element) {
            is JsonObject -> {
                // Check if this object IS the totalPrice object
                if (element["discountPrice"] != null && element["originalPrice"] != null
                    && element["currencyCode"] != null) {
                    val orig = element["originalPrice"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
                    if (!requireNonZero || orig > 0) return element
                }
                // Check for a "totalPrice" key
                val totalPrice = element["totalPrice"]
                if (totalPrice is JsonObject && totalPrice["discountPrice"] != null) {
                    val orig = totalPrice["originalPrice"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
                    if (!requireNonZero || orig > 0) return totalPrice
                }
                // Recurse into children
                for ((_, child) in element) {
                    findEpicTotalPriceFiltered(child, depth + 1, requireNonZero)?.let { return it }
                }
                null
            }
            is JsonArray -> {
                for (item in element) {
                    findEpicTotalPriceFiltered(item, depth + 1, requireNonZero)?.let { return it }
                }
                null
            }
            else -> null
        }
    }
}

