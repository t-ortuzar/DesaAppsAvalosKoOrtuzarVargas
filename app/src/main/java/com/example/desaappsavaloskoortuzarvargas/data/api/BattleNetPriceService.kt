package com.example.desaappsavaloskoortuzarvargas.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.regex.Pattern

/**
 * Service to fetch prices from the Battle.net / Blizzard Shop for Argentina.
 *
 * Primary strategy: scrape the Battle.net Argentina product page when a known slug is provided.
 * Battle.net AR product pages (us.shop.battle.net/es-ar/product/{slug}) embed pricing in
 * Open Graph meta tags, JSON-LD, and visible price text.
 *
 * Fallback: Blizzard shop catalog/search API with AR locale.
 * Last resort: isVerifiedLink=true card when slug is known but price can't be fetched.
 */
class BattleNetPriceService {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    // ──────────────────────────────────────────────────────────────
    //  Public API
    // ──────────────────────────────────────────────────────────────

    /**
     * Fetch the price for [title] from the Battle.net Shop (Argentina).
     * When [productSlug] is provided (from catalog), the product page is scraped directly.
     */
    suspend fun searchGamePrice(title: String, productSlug: String? = null): StorePrice? {
        // Strategy 1: scrape the known AR product page directly
        if (productSlug != null) {
            val pageUrl = "https://us.shop.battle.net/es-ar/product/$productSlug"
            scrapeProductPage(pageUrl, title)?.let { return it }
        }
        // Strategy 2: Blizzard shop catalog API with various endpoint formats
        searchViaApi(title, productSlug)?.let { return it }
        // Strategy 3: link-only fallback when slug is known
        if (productSlug != null) {
            return StorePrice(
                storeName = "Battle.net",
                currentPrice = 0f,
                originalPrice = 0f,
                discountPercent = 0,
                currency = "USD",
                isFree = false,
                storeUrl = "https://us.shop.battle.net/es-ar/product/$productSlug",
                isVerifiedLink = true
            )
        }
        return null
    }

    // ──────────────────────────────────────────────────────────────
    //  Product page scraping (primary)
    // ──────────────────────────────────────────────────────────────

    private suspend fun scrapeProductPage(pageUrl: String, title: String): StorePrice? =
        withContext(Dispatchers.IO) {
            try {
                val conn = openArConnection(pageUrl)
                if (conn.responseCode != 200) return@withContext null
                val html = conn.inputStream.bufferedReader().readText()

                // Strategy 0: meta tags (Open Graph / itemprop)
                extractFromMeta(html, pageUrl)?.let { return@withContext it }
                // Strategy 1: JSON-LD structured data
                extractFromJsonLd(html, pageUrl)?.let { return@withContext it }
                // Strategy 2: Next.js /_next/data/ endpoint — Blizzard's shop is Next.js SSR.
                // The /_next/data/{buildId}/{path}.json endpoint returns full product data as JSON
                // including pricing, even when the page's visible HTML is loaded client-side.
                val markerIdx = html.indexOf("""id="__NEXT_DATA__"""")
                if (markerIdx >= 0) {
                    try {
                        val contentStart = html.indexOf('>', markerIdx) + 1
                        val contentEnd   = html.indexOf("</script>", contentStart)
                        val buildId: String? = if (contentEnd > contentStart) {
                            val jsonStr = html.substring(contentStart, contentEnd).trim()
                            Regex(""""buildId"\s*:\s*"([^"]+)"""").find(jsonStr)?.groupValues?.get(1)
                        } else {
                            Regex(""""buildId"\s*:\s*"([^"]+)"""").find(html)?.groupValues?.get(1)
                        }
                        if (buildId != null) {
                            // Extract the page path from the URL: /es-ar/product/{slug}
                            val pagePath = pageUrl
                                .removePrefix("https://us.shop.battle.net")
                                .removePrefix("http://us.shop.battle.net")
                                .trimEnd('/')
                            val nextDataUrl = "https://us.shop.battle.net/_next/data/$buildId$pagePath.json"
                            val ndConn = openArConnection(nextDataUrl)
                            if (ndConn.responseCode == 200) {
                                val ndJson = ndConn.inputStream.bufferedReader().readText()
                                extractPriceFromNextData(ndJson, pageUrl)?.let { return@withContext it }
                            }
                        }
                    } catch (_: Exception) { }
                }
                // Strategy 3: body text ARS/USD price scan
                extractFromBodyText(html, pageUrl)
            } catch (_: Exception) { null }
        }

    /**
     * Parse price data from a Battle.net Next.js _next/data JSON response.
     * Blizzard's product page props include price info at various paths.
     */
    private fun extractPriceFromNextData(ndJson: String, pageUrl: String): StorePrice? {
        return try {
            val root = json.parseToJsonElement(ndJson)
            extractPriceFromJson(root)?.let { (price, orig, currency) ->
                if (price > 0f) buildStorePrice(price, orig, currency, pageUrl) else null
            }
        } catch (_: Exception) { null }
    }

    private fun extractFromMeta(html: String, pageUrl: String): StorePrice? {
        val pricePatterns = listOf(
            """<meta[^>]+property=["'](?:og:price:amount|product:price:amount)["'][^>]+content=["']([0-9.,]+)["']""",
            """<meta[^>]+content=["']([0-9.,]+)["'][^>]+property=["'](?:og:price:amount|product:price:amount)["']""",
            """<meta[^>]+itemprop=["']price["'][^>]+content=["']([0-9.,]+)["']""",
            """<meta[^>]+content=["']([0-9.,]+)["'][^>]+itemprop=["']price["']"""
        )
        val currencyPatterns = listOf(
            """<meta[^>]+property=["'](?:og:price:currency|product:price:currency)["'][^>]+content=["']([A-Z]{3})["']""",
            """<meta[^>]+content=["']([A-Z]{3})["'][^>]+property=["'](?:og:price:currency|product:price:currency)["']"""
        )
        var priceStr: String? = null
        for (p in pricePatterns) {
            val m = Pattern.compile(p, Pattern.CASE_INSENSITIVE).matcher(html)
            if (m.find()) { priceStr = m.group(1); break }
        }
        val price = priceStr?.let { parsePrice(it) } ?: return null
        if (price <= 0f) return null
        var currency = "USD"
        for (p in currencyPatterns) {
            val m = Pattern.compile(p, Pattern.CASE_INSENSITIVE).matcher(html)
            if (m.find()) { currency = m.group(1) ?: "USD"; break }
        }
        return buildStorePrice(price, price, currency, pageUrl)
    }

    private fun extractFromJsonLd(html: String, pageUrl: String): StorePrice? {
        val pattern = Pattern.compile(
            """<script[^>]+type=["']application/ld\+json["'][^>]*>(.*?)</script>""",
            Pattern.DOTALL or Pattern.CASE_INSENSITIVE
        )
        val matcher = pattern.matcher(html)
        while (matcher.find()) {
            try {
                val root = json.parseToJsonElement(matcher.group(1)!!)
                extractPriceFromJson(root)?.let { (price, orig, currency) ->
                    return buildStorePrice(price, orig, currency, pageUrl)
                }
            } catch (_: Exception) { }
        }
        return null
    }

    private fun extractFromBodyText(html: String, pageUrl: String): StorePrice? {
        // Battle.net AR store shows prices in ARS (e.g. "$ 3.999,99" or "ARS 3.999")
        val patterns = listOf(
            """ARS\s*\$?\s*([\d]{1,3}(?:[.,][\d]{3})*[.,][\d]{2})""",
            """ARS\s*\$?\s*([\d]{3,})""",
            """\$\s*([\d]{1,3}\.[\d]{3},[\d]{2})""",
            """\$\s*([\d]{1,3},[\d]{3}\.[\d]{2})""",
            """(?<![0-9.,])([\d]{1,3}\.[\d]{3},[\d]{2})(?![0-9.,])""",
            // Also catch USD prices (Battle.net AR might charge in USD)
            """USD\s*\$?\s*([\d]+(?:[.,][\d]+)?)""",
            """US\$\s*([\d]+(?:[.,][\d]+)?)"""
        )
        val candidates = mutableListOf<Float>()
        for (p in patterns) {
            val m = Pattern.compile(p, Pattern.CASE_INSENSITIVE).matcher(html)
            while (m.find()) {
                val v = parsePrice(m.group(1) ?: continue) ?: continue
                if (v >= 1f && v < 1_000_000f) candidates.add(v)
            }
        }
        if (candidates.isEmpty()) return null
        val max = candidates.max()
        val min = candidates.filter { it >= max * 0.1f }.min()
        // Detect currency from context: if max value > 1000, likely ARS; else USD
        val currency = if (max > 100f) "ARS" else "USD"
        return buildStorePrice(min, max, currency, pageUrl)
    }

    // ──────────────────────────────────────────────────────────────
    //  Blizzard catalog API (fallback)
    // ──────────────────────────────────────────────────────────────

    private suspend fun searchViaApi(title: String, productSlug: String?): StorePrice? =
        withContext(Dispatchers.IO) {
            val encoded = URLEncoder.encode(title, "UTF-8")
            val pageUrl = if (productSlug != null)
                "https://us.shop.battle.net/es-ar/product/$productSlug" else null

            // Try multiple catalog endpoint formats — Blizzard changes their API periodically
            val endpoints = listOf(
                "https://us.shop.battle.net/api/catalog?q=$encoded&locale=es_AR&country=AR",
                "https://us.shop.battle.net/api/catalog?q=$encoded&locale=es-AR&country=AR",
                "https://us.shop.battle.net/api/search?q=$encoded&locale=es_AR&country=AR",
                "https://shop.battle.net/api/catalog?q=$encoded&locale=es_AR&country=AR"
            )

            for (endpoint in endpoints) {
                try {
                    val conn = openArConnection(endpoint)
                    conn.setRequestProperty("Accept", "application/json")
                    if (conn.responseCode != 200) continue
                    val response = conn.inputStream.bufferedReader().readText()
                    val parsed = runCatching { json.decodeFromString<JsonObject>(response) }.getOrNull()
                        ?: continue

                    // Try several response structure patterns
                    val products = parsed["products"]?.let {
                        when (it) {
                            is JsonArray -> it
                            is JsonObject -> it["items"]?.let { items ->
                                if (items is JsonArray) items else null
                            }
                            else -> null
                        }
                    } ?: parsed["results"]?.let { if (it is JsonArray) it else null }
                      ?: continue

                    if ((products as? JsonArray)?.isEmpty() != false) continue
                    val firstProduct = (products as JsonArray)[0].jsonObject

                    val slug = firstProduct["slug"]?.jsonPrimitive?.content ?: ""
                    if (!slugMatchesTitle(title, slug)) continue

                    val priceObj = firstProduct["price"]?.jsonObject
                        ?: firstProduct["pricing"]?.jsonObject
                        ?: continue

                    val currentPrice = priceObj["amount"]?.jsonPrimitive?.content?.toFloatOrNull()
                        ?: priceObj["value"]?.jsonPrimitive?.content?.toFloatOrNull()
                        ?: priceObj["finalPrice"]?.jsonPrimitive?.content?.toFloatOrNull()
                        ?: continue
                    val originalPrice = priceObj["originalAmount"]?.jsonPrimitive?.content?.toFloatOrNull()
                        ?: priceObj["listPrice"]?.jsonPrimitive?.content?.toFloatOrNull()
                        ?: currentPrice
                    val currency = priceObj["currency"]?.jsonPrimitive?.content ?: "USD"
                    val discountPct = if (originalPrice > currentPrice && originalPrice > 0)
                        ((1 - currentPrice / originalPrice) * 100).toInt() else 0

                    return@withContext StorePrice(
                        storeName = "Battle.net",
                        currentPrice = currentPrice,
                        originalPrice = originalPrice,
                        discountPercent = discountPct,
                        currency = currency,
                        isFree = currentPrice == 0f,
                        storeUrl = pageUrl ?: if (slug.isNotEmpty())
                            "https://us.shop.battle.net/es-ar/product/$slug"
                        else "https://us.shop.battle.net/es-ar?q=$encoded"
                    )
                } catch (_: Exception) { continue }
            }
            null
        }

    // ──────────────────────────────────────────────────────────────
    //  Helpers
    // ──────────────────────────────────────────────────────────────

    private fun extractPriceFromJson(element: JsonElement, depth: Int = 0): Triple<Float, Float, String>? {
        if (depth > 12) return null
        return when (element) {
            is JsonObject -> {
                element["offers"]?.let { extractPriceFromJson(it, depth + 1) }?.let { return it }
                val priceRaw = element["price"]?.jsonPrimitive?.content?.let { parsePrice(it) }
                    ?: element["value"]?.jsonPrimitive?.content?.let { parsePrice(it) }
                if (priceRaw != null && priceRaw > 0f) {
                    val currency = element["priceCurrency"]?.jsonPrimitive?.content
                        ?: element["currency"]?.jsonPrimitive?.content ?: "USD"
                    val divisor = if (priceRaw > 100_000f) 100f else 1f
                    return Triple(priceRaw / divisor, priceRaw / divisor, currency)
                }
                for ((_, child) in element) {
                    extractPriceFromJson(child, depth + 1)?.let { return it }
                }
                null
            }
            is JsonArray -> {
                for (item in element) { extractPriceFromJson(item, depth + 1)?.let { return it } }
                null
            }
            else -> null
        }
    }

    private fun buildStorePrice(current: Float, original: Float, currency: String, pageUrl: String): StorePrice {
        val discountPct = if (original > current && original > 0f)
            ((1f - current / original) * 100).toInt() else 0
        return StorePrice(
            storeName = "Battle.net",
            currentPrice = current,
            originalPrice = original,
            discountPercent = discountPct,
            currency = currency,
            isFree = current == 0f,
            storeUrl = pageUrl
        )
    }

    private fun parsePrice(value: String): Float? {
        val s = value.trim()
        return when {
            s.contains(',') && s.lastIndexOf(',') > s.lastIndexOf('.') ->
                s.replace(".", "").replace(",", ".").toFloatOrNull()
            s.contains('.') && s.contains(',') -> s.replace(",", "").toFloatOrNull()
            s.contains('.') -> s.toFloatOrNull()
            else -> s.replace(",", "").toFloatOrNull()
        }
    }

    private fun slugMatchesTitle(query: String, slug: String): Boolean {
        if (slug.isEmpty()) return true
        val slugWords = slug.replace("-", " ").lowercase()
        val stopWords = setOf("the", "a", "an", "of", "in", "to")
        val queryWords = query.lowercase().split(" ").filter { it.length >= 4 && it !in stopWords }
        if (queryWords.isEmpty()) return true
        return queryWords.any { slugWords.contains(it) }
    }

    private fun openArConnection(endpoint: String): HttpURLConnection {
        val conn = URL(endpoint).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 10000
        conn.readTimeout = 10000
        conn.instanceFollowRedirects = true
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/json,*/*;q=0.9")
        conn.setRequestProperty("Accept-Language", "es-AR,es;q=0.9,en;q=0.8")
        conn.setRequestProperty("User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
        conn.setRequestProperty("Referer", "https://us.shop.battle.net/es-ar/")
        conn.setRequestProperty("Cookie", "country=AR; locale=es_AR; BNET_REGION=AR")
        return conn
    }
}
