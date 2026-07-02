package com.example.desaappsavaloskoortuzarvargas.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.regex.Pattern

/**
 * Service to fetch prices from the Ubisoft Connect Store for Argentina.
 *
 * Primary strategy: scrape the catalog-verified product page URL when provided.
 * Ubisoft's product pages (Salesforce Commerce Cloud / Demandware) embed pricing in:
 *   - Open Graph / product meta tags
 *   - JSON-LD structured data
 *   - Inline script variables (window.digitalData, pageContext)
 *   - Visible ARS price text (body text scan)
 *
 * Fallback: Ubisoft's internal Demandware search API.
 * Last resort: isVerifiedLink=true — show a clickable link without a price.
 */
class UbisoftPriceService {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    // ──────────────────────────────────────────────────────────────
    //  Public API
    // ──────────────────────────────────────────────────────────────

    /**
     * Fetch the price for [title] from the Ubisoft Store.
     * When [pageUrl] is provided (from catalog), uses the Demandware product API first.
     */
    suspend fun searchGamePrice(title: String, pageUrl: String? = null): StorePrice? {
        // Strategy 1: Demandware product API with the product ID extracted from the catalog URL.
        // This is the most reliable approach — bypasses page rendering issues.
        if (pageUrl != null) {
            val productId = extractDemandwareProductId(pageUrl)
            if (productId != null) {
                callDemandwareProductApi(productId, pageUrl)?.let { return it }
            }
        }
        // Strategy 2: scrape the verified catalog page URL
        if (pageUrl != null) {
            scrapeUbisoftPage(pageUrl, title)?.let { return it }
        }
        // Strategy 3: Demandware search suggestions API
        searchViaApi(title)?.let { return it }
        // Strategy 4: link-only fallback — user can still open the store page
        if (pageUrl != null) {
            return StorePrice(
                storeName = "Ubisoft",
                currentPrice = 0f,
                originalPrice = 0f,
                discountPercent = 0,
                currency = "ARS",
                isFree = false,
                storeUrl = pageUrl,
                isVerifiedLink = true
            )
        }
        return null
    }

    /**
     * Extract the Demandware product ID (24-char hex / ObjectId) from the catalog URL.
     * Example: ".../the-crew-motorfest/63bc67dbd406ab22f1174305.html" → "63bc67dbd406ab22f1174305"
     */
    private fun extractDemandwareProductId(url: String): String? {
        // Try ObjectId pattern: 24 hex chars (MongoDB ObjectId format used by Demandware/SFCC)
        val objectIdPattern = Regex("""([0-9a-f]{24})(?:\.html)?""", RegexOption.IGNORE_CASE)
        objectIdPattern.find(url)?.groupValues?.get(1)?.let { return it }
        // Fallback: any 10+ hex chars between slashes before ".html"
        val hexPattern = Regex("""/([0-9a-f]{10,})\.html""", RegexOption.IGNORE_CASE)
        hexPattern.find(url)?.groupValues?.get(1)?.let { return it }
        return null
    }

    /**
     * Call the Demandware Product-Variation AJAX endpoint directly with the product ID.
     * This endpoint is used by Ubisoft's SFCC storefront for dynamic product data
     * and reliably returns JSON with current pricing for the Argentine store.
     */
    private suspend fun callDemandwareProductApi(productId: String, pageUrl: String): StorePrice? =
        withContext(Dispatchers.IO) {
            val endpoints = listOf(
                // Product-Variation: returns product details with pricing for SFCC
                "https://store.ubisoft.com/on/demandware.store/Sites-us-ubisoft-Site/es_AR/Product-Variation?pid=$productId&Quantity=1&format=ajax",
                // Product-Show with JSON: alternative endpoint
                "https://store.ubisoft.com/on/demandware.store/Sites-us-ubisoft-Site/es_AR/Product-Show?pid=$productId&format=ajax",
                // GetTileLink: used for product card rendering
                "https://store.ubisoft.com/on/demandware.store/Sites-us-ubisoft-Site/es_AR/Product-GetTileLink?pid=$productId&Quantity=1&format=ajax"
            )
            for (endpoint in endpoints) {
                try {
                    val conn = openBrowserConnection(endpoint)
                    if (conn.responseCode != 200) continue
                    val responseText = conn.inputStream.bufferedReader().readText()

                    // Try to parse as JSON first (some SFCC endpoints return JSON)
                    try {
                        val parsed = json.parseToJsonElement(responseText)
                        extractPriceFromJsonElement(parsed)?.let { (price, orig, currency) ->
                            if (price > 0f) return@withContext buildStorePrice(price, orig, currency, pageUrl)
                        }
                    } catch (_: Exception) { }

                    // If JSON parsing fails, treat response as HTML and apply text strategies
                    extractFromMeta(responseText, pageUrl)?.let { return@withContext it }
                    extractFromJsonLd(responseText, pageUrl)?.let { return@withContext it }
                    extractFromBodyText(responseText, pageUrl)?.let { return@withContext it }
                } catch (_: Exception) { continue }
            }
            null
        }

    // ──────────────────────────────────────────────────────────────
    //  Page scraping (primary)
    // ──────────────────────────────────────────────────────────────

    /**
     * Scrape a Ubisoft product page for price data.
     * Strategies in order of reliability:
     *   0. Open Graph / product meta tags
     *   1. JSON-LD structured data
     *   2. Inline script variable scan (Demandware patterns)
     *   3. Full HTML body ARS price text scan
     */
    private suspend fun scrapeUbisoftPage(pageUrl: String, title: String): StorePrice? =
        withContext(Dispatchers.IO) {
            try {
                val conn = openBrowserConnection(pageUrl)
                if (conn.responseCode != 200) return@withContext null
                val html = conn.inputStream.bufferedReader().readText()

                extractFromMeta(html, pageUrl)?.let { return@withContext it }
                extractFromJsonLd(html, pageUrl)?.let { return@withContext it }
                extractFromScripts(html, pageUrl)?.let { return@withContext it }
                extractFromBodyText(html, pageUrl)
            } catch (_: Exception) { null }
        }

    /** Strategy 0: Open Graph / product meta tags. */
    private fun extractFromMeta(html: String, pageUrl: String): StorePrice? {
        val priceMetaPatterns = listOf(
            """<meta[^>]+property=["']product:price:amount["'][^>]+content=["']([0-9.,]+)["']""",
            """<meta[^>]+content=["']([0-9.,]+)["'][^>]+property=["']product:price:amount["']""",
            """<meta[^>]+name=["']price["'][^>]+content=["']([0-9.,]+)["']""",
            """<meta[^>]+itemprop=["']price["'][^>]+content=["']([0-9.,]+)["']""",
            """<meta[^>]+content=["']([0-9.,]+)["'][^>]+itemprop=["']price["']"""
        )
        val currencyMetaPatterns = listOf(
            """<meta[^>]+property=["']product:price:currency["'][^>]+content=["']([A-Z]{3})["']""",
            """<meta[^>]+content=["']([A-Z]{3})["'][^>]+property=["']product:price:currency["']"""
        )
        var priceStr: String? = null
        for (pattern in priceMetaPatterns) {
            val m = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(html)
            if (m.find()) { priceStr = m.group(1); break }
        }
        val price = priceStr?.let { parseFormattedPrice(it) } ?: return null
        if (price <= 0f) return null
        var currency = "ARS"
        for (pattern in currencyMetaPatterns) {
            val m = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(html)
            if (m.find()) { currency = m.group(1) ?: "ARS"; break }
        }
        return buildStorePrice(price, price, currency, pageUrl)
    }

    /** Strategy 1: JSON-LD structured data. */
    private fun extractFromJsonLd(html: String, pageUrl: String): StorePrice? {
        val pattern = Pattern.compile(
            """<script[^>]+type=["']application/ld\+json["'][^>]*>(.*?)</script>""",
            Pattern.DOTALL or Pattern.CASE_INSENSITIVE
        )
        val matcher = pattern.matcher(html)
        while (matcher.find()) {
            try {
                val root = json.parseToJsonElement(matcher.group(1)!!)
                extractPriceFromJsonElement(root)?.let { (price, orig, currency) ->
                    return buildStorePrice(price, orig, currency, pageUrl)
                }
            } catch (_: Exception) { }
        }
        return null
    }

    /** Strategy 2: Inline script variable scan (Demandware/SFCC patterns). */
    private fun extractFromScripts(html: String, pageUrl: String): StorePrice? {
        val scriptPattern = Pattern.compile(
            """<script[^>]*>(.*?)</script>""",
            Pattern.DOTALL or Pattern.CASE_INSENSITIVE
        )
        val matcher = scriptPattern.matcher(html)
        val pricePatterns = listOf(
            // Demandware "sales":{"value":55999}
            """"sales"\s*:\s*\{[^}]*"value"\s*:\s*([0-9]+(?:\.[0-9]+)?)""",
            // Generic "value":55999 (only large numbers to avoid false positives)
            """"value"\s*:\s*([0-9]{4,})""",
            // finalPrice / salePrice / currentPrice variable assignments
            """["'](?:finalPrice|salePrice|currentPrice|price)["']\s*:\s*([0-9]{3,})"""
        )
        while (matcher.find()) {
            val script = matcher.group(1) ?: continue
            if (!script.contains("price", ignoreCase = true)) continue
            for (pattern in pricePatterns) {
                val m = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(script)
                if (m.find()) {
                    val v = parseFormattedPrice(m.group(1) ?: continue) ?: continue
                    if (v >= 500f && v < 1_000_000f) return buildStorePrice(v, v, "ARS", pageUrl)
                }
            }
        }
        return null
    }

    /** Strategy 3: Body text ARS price scan. */
    private fun extractFromBodyText(html: String, pageUrl: String): StorePrice? {
        val pricePatterns = listOf(
            """ARS\s*\$?\s*([\d]{1,3}(?:[.,][\d]{3})*[.,][\d]{2})""",
            """ARS\s*\$?\s*([\d]{4,})""",
            """\$\s*([\d]{1,3}\.[\d]{3},[\d]{2})""",
            """\$\s*([\d]{1,3},[\d]{3}\.[\d]{2})""",
            """(?<![0-9.,])([\d]{1,3}\.[\d]{3},[\d]{2})(?![0-9.,])""",
            """(?<![0-9.,])([\d]{1,3},[\d]{3}\.[\d]{2})(?![0-9.,])"""
        )
        val candidates = mutableListOf<Float>()
        for (pattern in pricePatterns) {
            val m = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(html)
            while (m.find()) {
                val v = parseFormattedPrice(m.group(1) ?: continue) ?: continue
                if (v >= 500f && v < 1_000_000f) candidates.add(v)
            }
        }
        if (candidates.isEmpty()) return null
        val original = candidates.max()
        val current  = candidates.filter { it >= original * 0.1f }.min()
        return buildStorePrice(current, original, "ARS", pageUrl)
    }

    // ──────────────────────────────────────────────────────────────
    //  Demandware search API (fallback)
    // ──────────────────────────────────────────────────────────────

    private suspend fun searchViaApi(title: String): StorePrice? = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(title, "UTF-8")
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

            if (conn.responseCode != 200) return@withContext null
            val response = conn.inputStream.bufferedReader().readText()
            val parsed = json.decodeFromString<JsonObject>(response)
            val products = parsed["products"]?.jsonObject?.get("hits")?.jsonArray
                ?: return@withContext null
            if (products.isEmpty()) return@withContext null

            val firstHit = products[0].jsonObject
            val productTitle = firstHit["title"]?.jsonPrimitive?.content ?: title
            if (productTitle.isNotEmpty() && !titlesMatch(title, productTitle)) return@withContext null

            val priceValue = firstHit["price"]?.jsonObject
            val currentPrice = priceValue?.get("sales")?.jsonObject?.get("value")
                ?.jsonPrimitive?.content?.toFloatOrNull() ?: return@withContext null
            val originalPrice = priceValue.get("list")?.jsonObject?.get("value")
                ?.jsonPrimitive?.content?.toFloatOrNull() ?: currentPrice
            val currency = priceValue.get("sales")?.jsonObject?.get("currency")
                ?.jsonPrimitive?.content ?: "USD"
            val productUrl = firstHit["url"]?.jsonPrimitive?.content ?: ""

            buildStorePrice(
                currentPrice, originalPrice, currency,
                if (productUrl.isNotEmpty()) {
                    if (productUrl.startsWith("http")) productUrl
                    else "https://store.ubisoft.com$productUrl"
                } else "https://store.ubisoft.com/ofertas/games?lang=es_AR"
            )
        } catch (_: Exception) { null }
    }

    // ──────────────────────────────────────────────────────────────
    //  Helpers
    // ──────────────────────────────────────────────────────────────

    private fun extractPriceFromJsonElement(
        element: JsonElement, depth: Int = 0
    ): Triple<Float, Float, String>? {
        if (depth > 15) return null
        return when (element) {
            is JsonObject -> {
                // Follow "offers" key (schema.org pattern)
                element["offers"]?.let { extractPriceFromJsonElement(it, depth + 1) }?.let { return it }
                // Try to read price directly from this object
                val priceRaw = element["price"]?.jsonPrimitive?.content?.let { parseFormattedPrice(it) }
                    ?: element["value"]?.jsonPrimitive?.content?.let { parseFormattedPrice(it) }
                if (priceRaw != null && priceRaw > 0f) {
                    val currency = element["priceCurrency"]?.jsonPrimitive?.content
                        ?: element["currency"]?.jsonPrimitive?.content ?: "ARS"
                    val divisor = if (priceRaw > 100_000f) 100f else 1f
                    return Triple(priceRaw / divisor, priceRaw / divisor, currency)
                }
                for ((_, child) in element) {
                    extractPriceFromJsonElement(child, depth + 1)?.let { return it }
                }
                null
            }
            is JsonArray -> {
                for (item in element) {
                    extractPriceFromJsonElement(item, depth + 1)?.let { return it }
                }
                null
            }
            else -> null
        }
    }

    private fun buildStorePrice(
        current: Float, original: Float, currency: String, pageUrl: String
    ): StorePrice {
        val discountPct = if (original > current && original > 0f)
            ((1f - current / original) * 100).toInt() else 0
        return StorePrice(
            storeName = "Ubisoft",
            currentPrice = current,
            originalPrice = original,
            discountPercent = discountPct,
            currency = currency,
            isFree = current == 0f,
            storeUrl = pageUrl
        )
    }

    private fun parseFormattedPrice(value: String): Float? {
        val s = value.trim()
        return when {
            s.contains(',') && s.lastIndexOf(',') > s.lastIndexOf('.') ->
                s.replace(".", "").replace(",", ".").toFloatOrNull()
            s.contains('.') && s.contains(',') -> s.replace(",", "").toFloatOrNull()
            s.contains('.') -> s.toFloatOrNull()
            else -> s.replace(",", "").toFloatOrNull()
        }
    }

    private fun titlesMatch(query: String, candidate: String): Boolean {
        fun norm(s: String) = s.lowercase().replace(Regex("[':,.-]"), " ")
            .replace(Regex("\\s+"), " ").trim()
        val q = norm(query); val c = norm(candidate)
        if (q == c || q.contains(c) || c.contains(q)) return true
        val stop = setOf("the", "a", "an", "of", "in", "to", "at", "for", "and")
        val qf = q.split(" ").filter { it.length >= 4 && it !in stop }.toSet()
        val cf = c.split(" ").filter { it.length >= 4 && it !in stop }.toSet()
        if (qf.isEmpty() || cf.isEmpty()) return false
        return qf.intersect(cf).size >= (minOf(qf.size, cf.size) + 1) / 2
    }

    private fun openBrowserConnection(endpoint: String): HttpURLConnection {
        val conn = URL(endpoint).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 10000
        conn.readTimeout = 10000
        conn.instanceFollowRedirects = true
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/json,*/*;q=0.9")
        conn.setRequestProperty("Accept-Language", "es-AR,es;q=0.9,en;q=0.8")
        conn.setRequestProperty("User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
        conn.setRequestProperty("Referer", "https://store.ubisoft.com/")
        conn.setRequestProperty("Cookie", "country=ar; lang=es_AR")
        return conn
    }
}
