package com.example.desaappsavaloskoortuzarvargas.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Service to fetch REAL prices from GOG for Argentina.
 *
 * Uses the GOG Catalog API v1 with dynamic JSON parsing to be resilient
 * against field type variations (string vs number) across API versions.
 *
 * GOG does not support ARS regional pricing, so prices are returned in USD.
 * The UI converts USD → ARS using the dólar tarjeta rate.
 *
 * Endpoint: https://catalog.gog.com/v1/catalog
 */
class GogPriceService {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun searchGamePrice(title: String): StorePrice? = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(title, "UTF-8")
            val url = URL(
                "https://catalog.gog.com/v1/catalog?limit=5&query=$encoded&countryCode=AR&locale=es-AR&currencyCode=USD"
            )
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.setRequestProperty("Accept", "application/json")

            if (conn.responseCode != 200) return@withContext null

            val responseText = conn.inputStream.bufferedReader().readText()
            val root = json.decodeFromString<JsonObject>(responseText)
            val products = root["products"]?.jsonArray ?: return@withContext null
            if (products.isEmpty()) return@withContext null

            // Exact title match only — never fall back to first result
            val match = products.mapNotNull { it as? JsonObject }
                .firstOrNull { product ->
                    val productTitle = product["title"]?.jsonPrimitive?.contentOrNull ?: ""
                    productTitle.equals(title, ignoreCase = true)
                } ?: return@withContext null

            // ── Price extraction ──
            // GOG API v1 uses field names "final" and "base" for prices.
            // Values may come as strings OR numbers depending on the API version.
            val priceObj = match["price"]?.jsonObject
            val finalPrice = priceObj?.let { readPrice(it, "final", "finalAmount", "amount") } ?: 0f
            val basePrice  = priceObj?.let { readPrice(it, "base",  "baseAmount")  } ?: finalPrice
            val discountPctApi = priceObj?.let { readInt(it, "discountPercentage", "discount") } ?: 0
            val discountPct = if (discountPctApi > 0) discountPctApi
                              else if (basePrice > finalPrice && basePrice > 0f)
                                  ((1f - finalPrice / basePrice) * 100).toInt()
                              else 0
            val isFree = priceObj?.get("isFree")?.jsonPrimitive?.booleanOrNull ?: false

            if (finalPrice == 0f && !isFree) return@withContext null

            // ── URL extraction ──
            // Correct GOG URL format (confirmed): https://www.gog.com/es/game/{slug}
            // The slug from the API is reliable (e.g., "baldurs_gate_iii" for BG3).
            // NEVER use a title-derived slug — GOG uses Roman numerals, abbreviations, etc.
            // that cannot be reliably derived from the title ("3" ≠ "iii").
            val storeLink = match["storeLink"]?.jsonPrimitive?.contentOrNull ?: ""
            val apiSlug   = match["slug"]?.jsonPrimitive?.contentOrNull ?: ""

            val storeUrl = when {
                storeLink.startsWith("http") -> {
                    // The API may return /en/ or another locale; replace with /es/ for consistency
                    storeLink
                        .replace(Regex("/[a-z]{2}/game/"), "/es/game/")
                        .replace("/game/", "/es/game/")
                        .replace("https://www.gog.com/es/es/game/", "https://www.gog.com/es/game/") // avoid double
                }
                storeLink.isNotEmpty() -> {
                    val path = if (storeLink.startsWith("/")) storeLink else "/$storeLink"
                    "https://www.gog.com$path"
                }
                apiSlug.isNotEmpty() -> "https://www.gog.com/es/game/$apiSlug"
                // Fallback: search page — do NOT use derived slug (unreliable, e.g. "3" vs "iii")
                else -> "https://www.gog.com/games?search=${URLEncoder.encode(title, "UTF-8")}"
            }

            StorePrice(
                storeName = "GOG",
                currentPrice = finalPrice,
                originalPrice = basePrice,
                discountPercent = discountPct,
                currency = "USD",
                isFree = isFree,
                storeUrl = storeUrl
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Read a price value from the price JSON object.
     * Tries each candidate field name in order; handles both string and number JSON types.
     */
    private fun readPrice(priceObj: JsonObject, vararg fields: String): Float {
        for (field in fields) {
            val elem = priceObj[field]?.jsonPrimitive ?: continue
            val v = elem.doubleOrNull?.toFloat()
                ?: elem.contentOrNull?.toFloatOrNull()
                ?: continue
            if (v > 0f) return v
        }
        return 0f
    }

    /**
     * Read an integer value (e.g., discountPercentage) that may be a JSON string or number.
     */
    private fun readInt(priceObj: JsonObject, vararg fields: String): Int {
        for (field in fields) {
            val elem = priceObj[field]?.jsonPrimitive ?: continue
            val v = elem.doubleOrNull?.toInt()
                ?: elem.contentOrNull?.toIntOrNull()
                ?: continue
            if (v > 0) return v
        }
        return 0
    }
}
