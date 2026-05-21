package com.example.desaappsavaloskoortuzarvargas.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

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
    val priceUsdCents: Int,        // Price in cents (e.g., 4799 = $47.99)
    val retailPriceUsdCents: Int,
    val discountPercent: Int,
    val isFree: Boolean,
    val headerImageUrl: String,
    val currency: String
) {
    val priceUsd: Float get() = priceUsdCents / 100f
    val retailPriceUsd: Float get() = retailPriceUsdCents / 100f
}

/**
 * Service to fetch real prices and images from the Steam Store API.
 * Endpoint: https://store.steampowered.com/api/appdetails?appids={id}&cc=ar
 */
class SteamPriceService {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Get price and image info for a Steam app.
     * Uses cc=ar to get the Argentine store version (Steam shows USD in Argentina).
     */
    suspend fun getAppDetails(steamAppId: Int): SteamGamePrice? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://store.steampowered.com/api/appdetails?appids=$steamAppId&cc=ar&l=spanish")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.setRequestProperty("Accept", "application/json")

            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().readText()
                // Response is: { "APPID": { "success": true, "data": { ... } } }
                // Parse manually since the key is dynamic
                val parsed = json.decodeFromString<Map<String, SteamAppResult>>(response)
                val result = parsed[steamAppId.toString()]

                if (result?.success == true && result.data != null) {
                    val data = result.data
                    val priceOverview = data.price_overview

                    SteamGamePrice(
                        appId = steamAppId,
                        name = data.name,
                        priceUsdCents = priceOverview?.final ?: 0,
                        retailPriceUsdCents = priceOverview?.initial ?: 0,
                        discountPercent = priceOverview?.discount_percent ?: 0,
                        isFree = data.is_free,
                        headerImageUrl = data.header_image,
                        currency = priceOverview?.currency ?: "USD"
                    )
                } else null
            } else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Get prices for multiple Steam apps in a batch.
     * Steam API supports up to ~50 apps per request, but we'll do them individually
     * to avoid rate limiting.
     */
    suspend fun getMultipleAppDetails(steamAppIds: List<Int>): Map<Int, SteamGamePrice> {
        val results = mutableMapOf<Int, SteamGamePrice>()
        for (appId in steamAppIds) {
            getAppDetails(appId)?.let { results[appId] = it }
        }
        return results
    }
}

