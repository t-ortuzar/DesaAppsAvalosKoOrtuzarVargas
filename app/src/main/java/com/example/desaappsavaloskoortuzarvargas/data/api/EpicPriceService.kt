package com.example.desaappsavaloskoortuzarvargas.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
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

    private val searchQuery = """
        query searchStoreQuery(${'$'}country: String!, ${'$'}keywords: String!, ${'$'}count: Int) {
            Catalog {
                searchStore(
                    country: ${'$'}country,
                    keywords: ${'$'}keywords,
                    count: ${'$'}count,
                    category: "games/edition/base",
                    sortBy: "relevancy",
                    sortDir: "DESC"
                ) {
                    elements {
                        title
                        urlSlug
                        keyImages {
                            type
                            url
                        }
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
                                promotionalOffers {
                                    startDate
                                    endDate
                                }
                            }
                        }
                    }
                }
            }
        }
    """.trimIndent()

    /**
     * Search for a game on the Epic Games Store and return the Argentine price.
     * Returns null if the game is not found or an error occurs.
     */
    suspend fun searchGamePrice(title: String): StorePrice? = withContext(Dispatchers.IO) {
        try {
            val requestBody = json.encodeToString(
                kotlinx.serialization.json.JsonObject.serializer(),
                kotlinx.serialization.json.buildJsonObject {
                    put("query", kotlinx.serialization.json.JsonPrimitive(searchQuery))
                    put("variables", kotlinx.serialization.json.buildJsonObject {
                        put("country", kotlinx.serialization.json.JsonPrimitive("AR"))
                        put("keywords", kotlinx.serialization.json.JsonPrimitive(title))
                        put("count", kotlinx.serialization.json.JsonPrimitive(3))
                    })
                }
            )

            // graphql.epicgames.com/graphql was deprecated (404 as of 2025).
            // The current endpoint is store.epicgames.com/graphql.
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

                // Normalise titles: lowercase, drop edition suffixes ("Standard Edition",
                // "Complete Edition", etc.) so "Cyberpunk 2077" matches
                // "Cyberpunk 2077 Standard Edition" if an exact match isn't found.
                fun norm(t: String) = t.trim().lowercase()
                    .replace(Regex("\\s*(standard|complete|ultimate|definitive|game of the year|goty|deluxe|premium)\\s*(edition)?\\s*$"), "")
                    .trim()

                val normTitle = norm(title)
                val match = elements.firstOrNull { norm(it.title) == normTitle }
                    ?: elements.firstOrNull { norm(it.title).startsWith(normTitle) }
                    ?: return@withContext null

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
            } else null
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
}

