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
 * Service to fetch prices from the EA App (formerly Origin) for Argentina.
 *
 * EA's public API is limited. We try multiple known endpoints and parse
 * the best available response. If all fail the game will show a store-link
 * fallback card via GameDetailScreen's StoreLinkOnlyCard.
 */
class EAPriceService {

    private val json = Json { ignoreUnknownKeys = true }

    private fun buildEaUrl(gameName: String): String {
        val encoded = URLEncoder.encode(gameName, "UTF-8")
        return "https://www.ea.com/search#q=$encoded"
    }

    /** Try EA's Origin supercat API. Returns null if the endpoint is down or format mismatch. */
    private suspend fun tryOriginApi(title: String): StorePrice? =
        withContext(Dispatchers.IO) {
            val endpoints = listOf(
                "https://api3.origin.com/ecommerce2/public/supercat/AR/es_AR?searchTerm=${URLEncoder.encode(title, "UTF-8")}",
                "https://api1.origin.com/ecommerce2/public/supercat/AR/es_AR?searchTerm=${URLEncoder.encode(title, "UTF-8")}"
            )
            for (endpoint in endpoints) {
                try {
                    val conn = URL(endpoint).openConnection() as HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.connectTimeout = 6000
                    conn.readTimeout = 6000
                    conn.setRequestProperty("Accept", "application/json")
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0")

                    if (conn.responseCode != 200) continue

                    val response = conn.inputStream.bufferedReader().readText()
                    val parsed = json.decodeFromString<JsonObject>(response)

                    val offers = parsed["offers"]?.jsonArray
                        ?: parsed["games"]?.jsonArray
                        ?: parsed["items"]?.jsonArray
                        ?: continue

                    if (offers.isEmpty()) continue

                    val firstOffer = offers[0].jsonObject
                    val gameName = firstOffer["displayName"]?.jsonPrimitive?.content
                        ?: firstOffer["name"]?.jsonPrimitive?.content
                        ?: title
                    val priceObj = firstOffer["pricing"]?.jsonObject
                        ?: firstOffer["price"]?.jsonObject

                    val currentPrice = priceObj?.get("finalPrice")?.jsonPrimitive?.content?.toFloatOrNull()
                        ?: priceObj?.get("currentPrice")?.jsonPrimitive?.content?.toFloatOrNull()
                    val originalPrice = priceObj?.get("basePrice")?.jsonPrimitive?.content?.toFloatOrNull()
                        ?: priceObj?.get("originalPrice")?.jsonPrimitive?.content?.toFloatOrNull()
                    val currency = priceObj?.get("currency")?.jsonPrimitive?.content ?: "USD"

                    if (currentPrice == null) continue

                    val currentPriceFloat = if (currentPrice > 1000) currentPrice / 100f else currentPrice
                    val originalPriceFloat = if ((originalPrice ?: 0f) > 1000) (originalPrice ?: currentPrice) / 100f
                                            else (originalPrice ?: currentPrice)

                    val discountPct = if (originalPriceFloat > 0 && currentPriceFloat < originalPriceFloat) {
                        ((1 - currentPriceFloat / originalPriceFloat) * 100).toInt()
                    } else 0

                    return@withContext StorePrice(
                        storeName = "EA",
                        currentPrice = currentPriceFloat,
                        originalPrice = originalPriceFloat,
                        discountPercent = discountPct,
                        currency = currency,
                        isFree = currentPriceFloat == 0f,
                        storeUrl = "https://www.ea.com/search#q=${URLEncoder.encode(gameName.ifBlank { title }, "UTF-8")}"
                    )
                } catch (_: Exception) { continue }
            }
            null
        }

    /**
     * Search for a game on the EA store and return the Argentine price.
     * Returns null if no price can be fetched — GameDetailScreen shows a link-only fallback.
     */
    suspend fun searchGamePrice(title: String): StorePrice? = tryOriginApi(title)
}
