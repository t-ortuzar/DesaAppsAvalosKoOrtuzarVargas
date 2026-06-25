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

            // Normalise a title for comparison: lowercase + collapse all apostrophe variants
            // + strip extra whitespace. This handles GOG returning "Baldur\u2019s Gate 3"
            // while the catalog stores "Baldur's Gate 3" (or vice versa).
            fun normalise(t: String): String = t
                .replace('\u2019', '\'').replace('\u2018', '\'')
                .replace('\u201C', '"').replace('\u201D', '"')
                .trim().lowercase()

            // Strip Roman numerals AND Arabic digits so "Gate 3" == "Gate III" == "Gate".
            // Roman numeral tokens recognised: I II III IV V VI VII VIII IX X XI XII
            val romanPattern = Regex(
                """(?<![a-z])(x{0,3})(ix|iv|v?i{0,3})(?![a-z])""",
                setOf(RegexOption.IGNORE_CASE)
            )
            fun stripNumerals(t: String): String = t
                .replace(romanPattern, "")
                .replace(Regex("\\d+"), "")
                .replace(Regex("\\s+"), " ")
                .trim()

            val normTitle  = normalise(title)
            val fuzzyTitle = stripNumerals(normTitle)

            val productList = products.mapNotNull { it as? JsonObject }

            // 1st pass: exact normalised match
            var match: JsonObject? = productList.firstOrNull { product ->
                val productTitle = product["title"]?.jsonPrimitive?.contentOrNull ?: ""
                normalise(productTitle) == normTitle
            }

            // 2nd pass: fuzzy match – ignore trailing/embedded numerals (e.g. "3" vs "III")
            if (match == null) {
                match = productList.firstOrNull { product ->
                    val productTitle = product["title"]?.jsonPrimitive?.contentOrNull ?: ""
                    stripNumerals(normalise(productTitle)) == fuzzyTitle && fuzzyTitle.isNotEmpty()
                }
            }

            // 3rd pass: if only one result returned, accept it outright
            if (match == null && products.size == 1) {
                match = productList.firstOrNull()
            }

            // 4th pass: accept the TOP result from the API.
            // The GOG catalog search ranks results by relevance, so when we search for
            // "Baldur's Gate 3" the first result is "Baldur's Gate III". We verify the
            // first significant word of both titles matches to avoid completely wrong games.
            // IMPORTANT: skip common stopwords ("the", "a", "an", etc.) and require
            // length >= 5 so that short common words like "wild", "dark", "dead" do not
            // accidentally match unrelated promoted titles (e.g. "The Witcher 3: Wild Hunt").
            // We also require the match to be bidirectional: the firstWord must appear
            // in the candidate AND the candidate's first significant word must appear in
            // the query title, to prevent one-sided false positives.
            if (match == null) {
                val stopWords = setOf("the", "a", "an", "of", "in", "to", "at", "for", "and", "or",
                    "de", "la", "el", "los", "las", "un", "una")
                val firstWord = normTitle.split(" ", "'", ":", "-")
                    .firstOrNull { it.length >= 5 && it !in stopWords }
                if (firstWord != null) {
                    match = productList.firstOrNull { product ->
                        val productTitle = (product["title"]?.jsonPrimitive?.contentOrNull ?: "").lowercase()
                        // Forward check: query's firstWord must appear in the candidate title
                        if (!productTitle.contains(firstWord)) return@firstOrNull false
                        // Reverse check: candidate's first significant word must appear in the query title
                        val candidateFirstWord = productTitle.split(" ", "'", ":", "-")
                            .firstOrNull { it.length >= 5 && it !in stopWords }
                        candidateFirstWord == null || normTitle.contains(candidateFirstWord)
                    }
                }
            }

            if (match == null) return@withContext null

            // ── Price extraction ──
            // GOG API v1 uses field names "final" and "base" for prices.
            // Values may come as strings OR numbers depending on the API version.
            // Newer API versions may nest prices inside "finalMoney"/"baseMoney" objects.
            val priceObj = match["price"]?.jsonObject
            val finalMoney = priceObj?.get("finalMoney")?.jsonObject
            val baseMoney  = priceObj?.get("baseMoney")?.jsonObject

            // Prices come as "$30.69" (with $ symbol), so we use finalMoney/baseMoney
            // objects whose "amount" field is a clean decimal string like "30.69".
            // readPriceOrNull returns null (not 0f) so the ?: chain works correctly.
            val finalPrice = priceObj?.let { readPriceOrNull(it, "final", "finalAmount", "amount") }
                ?: finalMoney?.let { readPriceOrNull(it, "amount") }
                ?: 0f
            val basePrice  = priceObj?.let { readPriceOrNull(it, "base", "baseAmount") }
                ?: baseMoney?.let { readPriceOrNull(it, "amount") }
                ?: finalPrice

            // "discount" field comes as "-25%" — strip non-digit characters before parsing.
            val discountPctApi = priceObj?.let { readIntSanitised(it, "discountPercentage", "discount") } ?: 0
            val discountPct = if (discountPctApi > 0) discountPctApi
                              else if (basePrice > finalPrice && basePrice > 0f)
                                  ((1f - finalPrice / basePrice) * 100).toInt()
                              else 0
            val isFree = priceObj?.get("isFree")?.jsonPrimitive?.booleanOrNull ?: false

            if (finalPrice == 0f && !isFree) return@withContext null

            // ── URL extraction ──
            // Use the storeLink exactly as returned by the API (GOG returns /en/game/{slug}).
            // Do NOT force /es/ — GOG does not have Spanish locale game pages and
            // will redirect to the search results page instead.
            val storeLink = match["storeLink"]?.jsonPrimitive?.contentOrNull ?: ""
            val apiSlug   = match["slug"]?.jsonPrimitive?.contentOrNull ?: ""

            // Build the direct game URL from the slug.
            // storeLink from the API is usually a full URL like "https://www.gog.com/en/game/{slug}"
            // or a relative path like "/en/game/{slug}". apiSlug is the raw slug string.
            val storeUrl = when {
                storeLink.startsWith("http") -> storeLink          // full URL – use as-is
                storeLink.isNotEmpty() -> {
                    val path = if (storeLink.startsWith("/")) storeLink else "/$storeLink"
                    "https://www.gog.com$path"
                }
                apiSlug.isNotEmpty() -> "https://www.gog.com/en/game/$apiSlug"
                else -> return@withContext null                     // no slug → skip this entry
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
     * Read a price value from a JSON object, returning **null** (not 0f) when no
     * parseable field is found. This is important so the ?: chaining in the caller
     * can fall through to the finalMoney/baseMoney nested objects.
     *
     * Handles both JSON number primitives and string values that may contain a
     * currency symbol (e.g. "$30.69") — the symbol is stripped before parsing.
     */
    private fun readPriceOrNull(priceObj: JsonObject, vararg fields: String): Float? {
        for (field in fields) {
            val elem = priceObj[field]?.jsonPrimitive ?: continue
            // Try as a JSON number first
            val fromNumber = elem.doubleOrNull?.toFloat()
            if (fromNumber != null && fromNumber > 0f) return fromNumber
            // Try as a string, stripping currency symbols / whitespace
            val raw = elem.contentOrNull ?: continue
            val cleaned = raw.replace(Regex("[^0-9.]"), "")
            val fromString = cleaned.toFloatOrNull() ?: continue
            if (fromString > 0f) return fromString
        }
        return null
    }

    /**
     * Read an integer value (e.g. discountPercentage) that may be a JSON string or number.
     * Strips non-digit characters so values like "-25%" or "25%" are parsed correctly.
     */
    private fun readIntSanitised(priceObj: JsonObject, vararg fields: String): Int {
        for (field in fields) {
            val elem = priceObj[field]?.jsonPrimitive ?: continue
            val fromNumber = elem.doubleOrNull?.toInt()
            if (fromNumber != null && fromNumber > 0) return fromNumber
            val raw = elem.contentOrNull ?: continue
            val cleaned = raw.replace(Regex("[^0-9]"), "")
            val fromString = cleaned.toIntOrNull() ?: continue
            if (fromString > 0) return fromString
        }
        return 0
    }
}
