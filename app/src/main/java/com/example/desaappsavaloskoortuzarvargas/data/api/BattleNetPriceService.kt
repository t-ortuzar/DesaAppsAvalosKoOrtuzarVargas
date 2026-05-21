package com.example.desaappsavaloskoortuzarvargas.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Service to fetch prices from Battle.net / Blizzard Shop for Argentina.
 *
 * Battle.net does not have an official public pricing API, but their
 * web shop loads product data via accessible JSON endpoints.
 * We attempt to use Battle.net's shop search API with Argentine locale.
 *
 * Note: Battle.net only sells Blizzard/Activision games (Diablo, WoW,
 * Overwatch, CoD, StarCraft, etc.). Many of these are also on Steam.
 * If this service fails, those games' prices are still available
 * through the Steam/Epic services.
 */
class BattleNetPriceService {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Search for a game on the Battle.net shop and return the Argentine price.
     * Uses Blizzard's shop catalog API with AR region.
     */
    suspend fun searchGamePrice(title: String): StorePrice? = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(title, "UTF-8")
            // Blizzard shop catalog endpoint with AR locale
            val url = URL(
                "https://us.shop.battle.net/api/catalog?q=$encoded&locale=es-AR&country=AR"
            )
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("Accept", "application/json")
            conn.setRequestProperty("User-Agent", "Mozilla/5.0")

            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().readText()
                val parsed = json.decodeFromString<JsonObject>(response)

                val products = parsed["products"]?.jsonArray ?: return@withContext null
                if (products.isEmpty()) return@withContext null

                val firstProduct = products[0].jsonObject
                val productTitle = firstProduct["name"]?.jsonPrimitive?.content ?: title
                val priceObj = firstProduct["price"]?.jsonObject

                val currentPrice = priceObj?.get("amount")?.jsonPrimitive?.content
                    ?.toFloatOrNull()
                val originalPrice = priceObj?.get("originalAmount")?.jsonPrimitive?.content
                    ?.toFloatOrNull()
                val currency = priceObj?.get("currency")?.jsonPrimitive?.content ?: "USD"
                val productSlug = firstProduct["slug"]?.jsonPrimitive?.content ?: ""

                if (currentPrice == null) return@withContext null

                val msrp = originalPrice ?: currentPrice
                val discountPct = if (msrp > 0 && currentPrice < msrp) {
                    ((1 - currentPrice / msrp) * 100).toInt()
                } else 0

                StorePrice(
                    storeName = "Battle.net",
                    currentPrice = currentPrice,
                    originalPrice = msrp,
                    discountPercent = discountPct,
                    currency = currency,
                    isFree = currentPrice == 0f,
                    storeUrl = "https://us.shop.battle.net/es-ar/product/$productSlug"
                )
            } else null
        } catch (_: Exception) {
            null
        }
    }
}

