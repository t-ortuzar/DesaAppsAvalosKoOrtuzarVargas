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
 * EA's public API is limited, but their storefront exposes some
 * accessible endpoints. We attempt to use EA's search API with
 * Argentine country code.
 *
 * Note: Most EA games are also available on Steam and Epic Games,
 * so if this service fails, those stores will typically cover EA titles.
 */
class EAPriceService {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Search for a game on the EA store and return the Argentine price.
     * Attempts to use EA's storefront API with AR country code.
     */
    suspend fun searchGamePrice(title: String): StorePrice? = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(title, "UTF-8")
            // EA's search/catalog API endpoint
            val url = URL(
                "https://api2.origin.com/ecommerce2/public/supercat/AR/es_AR?searchTerm=$encoded"
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

                // Origin API response structure varies — try common patterns
                val offers = parsed["offers"]?.jsonArray
                    ?: parsed["games"]?.jsonArray
                    ?: return@withContext null

                if (offers.isEmpty()) return@withContext null

                val firstOffer = offers[0].jsonObject
                val gameName = firstOffer["displayName"]?.jsonPrimitive?.content
                    ?: firstOffer["name"]?.jsonPrimitive?.content
                    ?: title
                val priceObj = firstOffer["pricing"]?.jsonObject
                    ?: firstOffer["price"]?.jsonObject

                val currentPrice = priceObj?.get("finalPrice")?.jsonPrimitive?.content
                    ?.toFloatOrNull()
                    ?: priceObj?.get("currentPrice")?.jsonPrimitive?.content?.toFloatOrNull()
                val originalPrice = priceObj?.get("basePrice")?.jsonPrimitive?.content
                    ?.toFloatOrNull()
                    ?: priceObj?.get("originalPrice")?.jsonPrimitive?.content?.toFloatOrNull()
                val currency = priceObj?.get("currency")?.jsonPrimitive?.content ?: "USD"

                if (currentPrice == null) return@withContext null

                // EA prices from Origin API are typically in cents
                val currentPriceFloat = if (currentPrice > 1000) currentPrice / 100f else currentPrice
                val originalPriceFloat = if ((originalPrice ?: 0f) > 1000) (originalPrice ?: currentPrice) / 100f else (originalPrice ?: currentPrice)

                val discountPct = if (originalPriceFloat > 0 && currentPriceFloat < originalPriceFloat) {
                    ((1 - currentPriceFloat / originalPriceFloat) * 100).toInt()
                } else 0

                StorePrice(
                    storeName = "EA",
                    currentPrice = currentPriceFloat,
                    originalPrice = originalPriceFloat,
                    discountPercent = discountPct,
                    currency = currency,
                    isFree = currentPriceFloat == 0f,
                    storeUrl = run {
                        // offerId from Origin API (e.g. "Origin.OFR.50.0001066") is not a URL slug.
                        // Use EA App's search instead so the user lands near the game.
                        val encoded = URLEncoder.encode(gameName.ifBlank { title }, "UTF-8")
                        "https://www.ea.com/search#category=mlbgame&q=$encoded"
                    }
                )
            } else null
        } catch (_: Exception) {
            null
        }
    }
}

