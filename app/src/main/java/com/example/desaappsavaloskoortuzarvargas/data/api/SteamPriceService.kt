package com.example.desaappsavaloskoortuzarvargas.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

@Serializable
data class SteamPriceOverview(
    val currency: String = "USD",
    val initial: Int = 0,
    val final: Int = 0,
    val discount_percent: Int = 0,
    val initial_formatted: String = "",
    val final_formatted: String = ""
)

@Serializable
data class SteamAppData(
    val name: String = "",
    val steam_appid: Int = 0,
    val is_free: Boolean = false,
    val header_image: String = "",
    val price_overview: SteamPriceOverview? = null
)

@Serializable
data class SteamAppResult(
    val success: Boolean = false,
    val data: SteamAppData? = null
)

data class SteamGamePrice(
    val appId: Int,
    val name: String,
    val priceCents: Int,           // Price in smallest unit of currency (e.g., 879900 = ARS $8,799.00)
    val retailPriceCents: Int,
    val discountPercent: Int,
    val isFree: Boolean,
    val headerImageUrl: String,
    val currency: String,          // "ARS" when cc=ar, "USD" when cc=us, etc.
    val discountEndTimestamp: Long? = null  // Unix epoch SECONDS when discount ends (from Steam package_groups)
) {
    /** Current price in the currency returned by the API (ARS for Argentina). */
    val price: Float get() = priceCents / 100f
    /** Retail (non-discounted) price in the same currency. */
    val retailPrice: Float get() = retailPriceCents / 100f
    /** True if the price is in Argentine pesos. */
    val isArs: Boolean get() = currency == "ARS"
}

/**
 * Service to fetch real prices and images from the Steam Store API.
 *
 * IMPORTANT: Steam has regional pricing for Argentina.
 * Using cc=ar returns the REAL price that Argentine users see, in ARS.
 * Using cc=us returns the US price in USD.
 *
 * Some publishers set significantly lower prices for Argentina (e.g., an
 * AAA game that costs USD 59.99 in the US might cost ARS 8,799 in Argentina).
 * This is NOT just a currency conversion — it's a separate, publisher-set price.
 */
class SteamPriceService {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Get price info for a Steam app in a specific region.
     * @param steamAppId The Steam application ID
     * @param countryCode ISO country code ("ar" for Argentina, "us" for US, etc.)
     * @param language Language for the response ("spanish", "english", etc.)
     */
    suspend fun getAppDetails(
        steamAppId: Int,
        countryCode: String = "ar",
        language: String = "spanish"
    ): SteamGamePrice? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://store.steampowered.com/api/appdetails?appids=$steamAppId&cc=$countryCode&l=$language")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.setRequestProperty("Accept", "application/json")

            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().readText()
                val parsed = json.decodeFromString<Map<String, SteamAppResult>>(response)
                val result = parsed[steamAppId.toString()]

                if (result?.success == true && result.data != null) {
                    val data = result.data
                    val priceOverview = data.price_overview

                    // Extract discount end timestamp from raw JSON (package_groups → subs → discount_expiration)
                    var discountEndTs: Long? = try {
                        val rawJson = json.parseToJsonElement(response)
                        val appData = rawJson.jsonObject[steamAppId.toString()]?.jsonObject?.get("data")?.jsonObject
                        val packageGroups = appData?.get("package_groups")?.jsonArray
                        val subs = packageGroups?.firstOrNull()?.jsonObject?.get("subs")?.jsonArray
                        val expiration = subs?.firstOrNull()?.jsonObject?.get("discount_expiration")?.jsonPrimitive?.longOrNull
                        if (expiration != null && expiration > 0) expiration * 1000L else null // Convert seconds → millis
                    } catch (_: Exception) { null }

                    // Fallback: if there's a discount but no end timestamp from API, scrape the store page
                    val discountPct = priceOverview?.discount_percent ?: 0
                    if (discountEndTs == null && discountPct > 0) {
                        discountEndTs = scrapeDiscountEndDate(steamAppId)
                    }

                    SteamGamePrice(
                        appId = steamAppId,
                        name = data.name,
                        priceCents = priceOverview?.final ?: 0,
                        retailPriceCents = priceOverview?.initial ?: 0,
                        discountPercent = priceOverview?.discount_percent ?: 0,
                        isFree = data.is_free,
                        headerImageUrl = data.header_image,
                        currency = priceOverview?.currency ?: if (countryCode == "ar") "ARS" else "USD",
                        discountEndTimestamp = discountEndTs
                    )
                } else null
            } else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Get the Argentine regional price for a Steam app.
     * Returns the price in ARS as set by the publisher for the Argentine store.
     */
    suspend fun getArgentinePrice(steamAppId: Int): SteamGamePrice? =
        getAppDetails(steamAppId, countryCode = "ar", language = "spanish")

    /**
     * Get the US price for a Steam app (for comparison purposes).
     */
    suspend fun getUsPrice(steamAppId: Int): SteamGamePrice? =
        getAppDetails(steamAppId, countryCode = "us", language = "english")

    /**
     * Get prices for multiple Steam apps in a batch.
     * Steam API doesn't support true batching, so we fetch individually.
     */
    suspend fun getMultipleAppDetails(
        steamAppIds: List<Int>,
        countryCode: String = "ar"
    ): Map<Int, SteamGamePrice> {
        val results = mutableMapOf<Int, SteamGamePrice>()
        for (appId in steamAppIds) {
            getAppDetails(appId, countryCode)?.let { results[appId] = it }
        }
        return results
    }

    /**
     * Scrape the Steam store page HTML to extract the discount end timestamp.
     * Steam embeds a countdown timer with a Unix timestamp in the page's JavaScript
     * (e.g., `data-countdown-date="1717603200"` or `discount_countdown_init`).
     * Returns epoch millis, or null if not found.
     */
    private suspend fun scrapeDiscountEndDate(steamAppId: Int): Long? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://store.steampowered.com/app/$steamAppId/")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            // Pretend to be a browser so Steam doesn't block/redirect
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            conn.setRequestProperty("Accept-Language", "es-AR,es;q=0.9")
            conn.setRequestProperty("Cookie", "birthtime=0; wants_mature_content=1; lastagecheckage=1-0-2000")
            conn.instanceFollowRedirects = true

            if (conn.responseCode == 200) {
                val html = conn.inputStream.bufferedReader().readText()

                // Strategy 1: Look for discount_expiration in embedded JSON data
                // Steam sometimes embeds: "discount_expiration":1717603200
                val jsonPattern = Pattern.compile("\"discount_expiration\"\\s*:\\s*(\\d{10,})")
                val jsonMatcher = jsonPattern.matcher(html)
                if (jsonMatcher.find()) {
                    val ts = jsonMatcher.group(1)?.toLongOrNull()
                    if (ts != null && ts > 0) return@withContext ts * 1000L
                }

                // Strategy 2: Look for data-countdown-date attribute
                // e.g., data-countdown-date="1717603200"
                val countdownPattern = Pattern.compile("data-countdown-date=\"(\\d{10,})\"")
                val countdownMatcher = countdownPattern.matcher(html)
                if (countdownMatcher.find()) {
                    val ts = countdownMatcher.group(1)?.toLongOrNull()
                    if (ts != null && ts > 0) return@withContext ts * 1000L
                }

                // Strategy 3: Look for InitDailyDealTimer or discount_countdown_init with timestamp
                // e.g., InitDailyDealTimer( $J('#game_area_purchase_...'), 1717603200 );
                val timerPattern = Pattern.compile("(?:InitDailyDealTimer|discount_countdown_init|SetCountdown)\\s*\\([^)]*?(\\d{10,})")
                val timerMatcher = timerPattern.matcher(html)
                if (timerMatcher.find()) {
                    val ts = timerMatcher.group(1)?.toLongOrNull()
                    if (ts != null && ts > 0) return@withContext ts * 1000L
                }

                // Strategy 4: Look for DiscountCountdown with Unix timestamp in React/JS data
                val reactPattern = Pattern.compile("\"expiry\"\\s*:\\s*(\\d{10,})")
                val reactMatcher = reactPattern.matcher(html)
                if (reactMatcher.find()) {
                    val ts = reactMatcher.group(1)?.toLongOrNull()
                    if (ts != null && ts > 0) return@withContext ts * 1000L
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }
}

