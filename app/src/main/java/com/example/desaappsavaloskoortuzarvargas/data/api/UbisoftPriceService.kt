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
 * Service to fetch prices from the Ubisoft Connect Store for Argentina.
 *
 * Ubisoft's store API is not officially public, but their storefront
 * uses accessible JSON endpoints. We try the Ubisoft Store API with
 * Argentine locale. If it fails, returns null gracefully.
 *
 * Note: Most Ubisoft games are also available on Steam and Epic Games,
 * so those services will typically cover Ubisoft titles too.
 */
class UbisoftPriceService {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Search for a game on the Ubisoft Store and return the Argentine price.
     * Uses Ubisoft's internal store API with AR locale.
     */
    suspend fun searchGamePrice(title: String): StorePrice? = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(title, "UTF-8")
            // Ubisoft's storefront search API (Demandware-based)
            val url = URL(
                "https://store.ubisoft.com/on/demandware.store/Sites-us-ubisoft-Site/es_AR/" +
                "Search-GetSuggestions?q=$encoded"
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

                // Try to extract product info from the suggestions response
                val products = parsed["products"]?.jsonObject?.get("hits")?.jsonArray
                    ?: return@withContext null

                if (products.isEmpty()) return@withContext null

                val firstHit = products[0].jsonObject
                val productTitle = firstHit["title"]?.jsonPrimitive?.content ?: title
                val priceValue = firstHit["price"]?.jsonObject
                val currentPrice = priceValue?.get("sales")?.jsonObject?.get("value")
                    ?.jsonPrimitive?.content?.toFloatOrNull()
                val originalPrice = priceValue?.get("list")?.jsonObject?.get("value")
                    ?.jsonPrimitive?.content?.toFloatOrNull()
                val currency = priceValue?.get("sales")?.jsonObject?.get("currency")
                    ?.jsonPrimitive?.content ?: "USD"
                val productUrl = firstHit["url"]?.jsonPrimitive?.content ?: ""

                if (currentPrice == null) return@withContext null

                val msrp = originalPrice ?: currentPrice
                val discountPct = if (msrp > 0 && currentPrice < msrp) {
                    ((1 - currentPrice / msrp) * 100).toInt()
                } else 0

                StorePrice(
                    storeName = "Ubisoft",
                    currentPrice = currentPrice,
                    originalPrice = msrp,
                    discountPercent = discountPct,
                    currency = currency,
                    isFree = currentPrice == 0f,
                    storeUrl = if (productUrl.isNotEmpty()) {
                        val link = if (productUrl.startsWith("http")) productUrl
                                   else "https://store.ubisoft.com$productUrl"
                        link
                    } else {
                        val encoded = URLEncoder.encode(title, "UTF-8")
                        "https://store.ubisoft.com/search?q=$encoded"
                    }
                )
            } else null
        } catch (_: Exception) {
            null
        }
    }
}

