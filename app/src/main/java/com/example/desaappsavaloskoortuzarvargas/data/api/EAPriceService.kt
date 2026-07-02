package com.example.desaappsavaloskoortuzarvargas.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.regex.Pattern

/**
 * Service to fetch prices from the EA App store for Argentina.
 *
 * Primary strategy: scrape the EA game page HTML (using catalog-verified URLs).
 * EA uses Next.js so pricing data is embedded in <script id="__NEXT_DATA__">
 * as structured JSON. We also check JSON-LD and regex patterns as fallbacks.
 *
 * Fallback: try the old Origin supercat API endpoints.
 */
class EAPriceService {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    // ──────────────────────────────────────────────────────────────
    //  Public API
    // ──────────────────────────────────────────────────────────────

    /**
     * Fetch the price for [title] from the EA store.
     * When [gameUrl] is provided (from catalog), the page is scraped directly —
     * this is far more reliable than the Origin search API.
     */
    suspend fun searchGamePrice(title: String, gameUrl: String? = null): StorePrice? {
        if (gameUrl != null) {
            scrapeEaPage(gameUrl, title)?.let { return it }
        }
        // Try the Origin API search as a secondary attempt
        tryOriginApi(title)?.let { return it }

        // Last resort: if we have a verified catalog URL but couldn't scrape a price
        // (EA renders prices client-side in many regions), return a "link-only" entry.
        // This ensures the user can still reach the EA store page even without a live price.
        if (gameUrl != null) {
            val buyUrl = gameUrl.removeSuffix("/").let { base ->
                if (!base.endsWith("/buy")) "$base/buy" else base
            }
            return StorePrice(
                storeName = "EA",
                currentPrice = 0f,
                originalPrice = 0f,
                discountPercent = 0,
                currency = "USD",
                isFree = false,
                storeUrl = buyUrl,
                isVerifiedLink = true   // Price unknown — user must check on the EA website
            )
        }
        return null
    }

    // ──────────────────────────────────────────────────────────────
    //  Page scraping (primary)
    // ──────────────────────────────────────────────────────────────

    /**
     * Scrape the EA game page for pricing data.
     *
     * URL order: try the catalog's original /es/ URLs FIRST (user-verified to work),
     * then try /es-ar/ variants. /es-ar/ often redirects or returns pages without prices.
     *
     * Extraction order:
     *   A. Extract masterTitleId/offerId from page → call Origin API by ID (most reliable)
     *   B. Full HTML price extraction (meta tags, __NEXT_DATA__, JSON-LD, script regex, body text)
     */
    private suspend fun scrapeEaPage(baseUrl: String, title: String): StorePrice? =
        withContext(Dispatchers.IO) {
            val cleanBase = baseUrl.removeSuffix("/buy").removeSuffix("/")
            val esArBase = cleanBase
                .replace("//www.ea.com/es/", "//www.ea.com/es-ar/")
                .replace("//www.ea.com/en-us/", "//www.ea.com/es-ar/")
                .replace("//www.ea.com/en/", "//www.ea.com/es-ar/")

            // Try original /es/ URLs first — the user confirmed these show prices.
            // /es-ar/ variants may return empty pages or redirect loops.
            val urlsToTry = listOf(
                "$cleanBase/buy",  // /es/…/buy ← primary (user-verified)
                cleanBase,
                "$esArBase/buy",
                esArBase
            ).distinct()

            for (url in urlsToTry) {
                try {
                    val conn = openBrowserConnection(url)
                    if (conn.responseCode != 200) continue
                    val html = conn.inputStream.bufferedReader().readText()

                    // Strategy A: extract masterTitleId / offerId from page and call Origin API
                    val gameId = extractMasterTitleId(html)
                    if (gameId != null) {
                        tryOriginApiById(gameId)?.let { return@withContext it }
                    }

                    // Strategy B: scan HTML for price data directly (returns price + buildId)
                    val (priceFromHtml, buildId) = extractPriceFromHtml(html, url, title)
                    if (priceFromHtml != null) return@withContext priceFromHtml

                    // Strategy 1b: Next.js /_next/data/ endpoint.
                    // EA loads prices client-side; the /_next/data/ API returns full SSR JSON with prices.
                    // Try multiple path variants: original locale, es-ar, with/without /buy suffix.
                    if (buildId != null) {
                        try {
                            val rawPath = url
                                .removePrefix("https://www.ea.com")
                                .removePrefix("http://www.ea.com")
                                .trimEnd('/')
                            val esArPath = rawPath
                                .replace("/es/", "/es-ar/")
                                .replace("/en-us/", "/es-ar/")
                                .replace("/en/", "/es-ar/")
                            val rawBuyPath  = if (!rawPath.endsWith("/buy"))  "$rawPath/buy"  else rawPath
                            val esArBuyPath = if (!esArPath.endsWith("/buy")) "$esArPath/buy" else esArPath

                            val nextDataPaths = listOf(
                                esArBuyPath,   // /es-ar/…/buy  ← preferred (AR pricing)
                                rawBuyPath,    // /es/…/buy     ← original locale
                                esArPath,      // /es-ar/…      ← without /buy
                                rawPath        // /es/…         ← original without /buy
                            ).distinct()

                            for (ndPath in nextDataPaths) {
                                try {
                                    val nextDataUrl = "https://www.ea.com/_next/data/$buildId$ndPath.json"
                                    val ndConn = openBrowserConnection(nextDataUrl)
                                    if (ndConn.responseCode != 200) continue
                                    val ndJson = ndConn.inputStream.bufferedReader().readText()
                                    val ndRoot = json.parseToJsonElement(ndJson)
                                    extractPriceFromJsonElement(ndRoot, "ARS")
                                        ?.let { (price, orig, currency, isEaPlay) ->
                                            return@withContext buildStorePrice(
                                                price, orig, currency, url, isEaPlay
                                            )
                                        }
                                } catch (_: Exception) { continue }
                            }
                        } catch (_: Exception) { }
                    }

                } catch (_: Exception) { continue }
            }
            null
        }

    /**
     * Extract a masterTitleId or offerId from the EA game page HTML.
     * These IDs allow a direct Origin API lookup (much more reliable than title search).
     */
    private fun extractMasterTitleId(html: String): String? {
        val idPatterns = listOf(
            """"masterTitleId"\s*:\s*"?(\d{4,})"?""",          // numeric masterTitleId in JSON
            """data-master-title-id=["'](\d{4,})["']""",        // HTML attribute
            """"masterTitle"\s*:\s*\{[^}]*"id"\s*:\s*"?(\d+)"?""", // nested id
            """"offerId"\s*:\s*"(Origin\.OFR\.[^"]+)"""",        // Origin offer ID
            """data-offer-id=["'](Origin\.[^"']+)["']"""         // HTML attribute
        )
        for (pattern in idPatterns) {
            val m = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(html)
            if (m.find()) return m.group(1)
        }
        return null
    }

    /**
     * Call the Origin API using a known masterTitleId or offerId.
     * This bypasses the unreliable text search and goes directly to the product.
     */
    private suspend fun tryOriginApiById(id: String): StorePrice? =
        withContext(Dispatchers.IO) {
            val encodedId = URLEncoder.encode(id, "UTF-8")
            val endpoints = listOf(
                // masterTitleId-based lookup
                "https://api3.origin.com/ecommerce2/public/supercat/AR/es_AR?masterTitleIds=$encodedId",
                "https://api1.origin.com/ecommerce2/public/supercat/AR/es_AR?masterTitleIds=$encodedId",
                // ids= parameter (works for both masterTitleId and offerId)
                "https://api3.origin.com/ecommerce2/public/supercat/AR/es_AR?ids=$encodedId",
                "https://api1.origin.com/ecommerce2/public/supercat/AR/es_AR?ids=$encodedId",
                // Direct offer endpoint
                "https://api3.origin.com/ecommerce2/public/offer/$encodedId/AR/es_AR"
            )
            for (endpoint in endpoints) {
                try {
                    val conn = openBrowserConnection(endpoint)
                    if (conn.responseCode != 200) continue
                    val response = conn.inputStream.bufferedReader().readText()
                    val parsed = json.decodeFromString<JsonObject>(response)
                    val offers = parsed["offers"]?.jsonArray
                        ?: parsed["games"]?.jsonArray
                        ?: parsed["items"]?.jsonArray
                        ?: parsed["results"]?.jsonArray
                        ?: continue
                    for (item in offers.take(3)) {
                        val obj = try { item.jsonObject } catch (_: Exception) { continue }
                        val priceResult = extractPriceFromObject(obj, "ARS") ?: continue
                        return@withContext buildStorePrice(
                            priceResult.current, priceResult.original,
                            priceResult.currency,
                            "https://www.ea.com/es/games",   // placeholder — real URL set by catalog
                            priceResult.isEaPlay
                        )
                    }
                } catch (_: Exception) { continue }
            }
            null
        }

    /**
     * Extract a StorePrice from the raw HTML of an EA game page.
     * Tries strategies in order:
     *   0. Meta tags (Open Graph product price tags — fastest, most reliable)
     *   1. __NEXT_DATA__ embedded JSON (Next.js app shell); also returns buildId for the caller
     *   2. JSON-LD structured data (<script type="application/ld+json">)
     *   3. Regex scan of all <script> tag content
     *   4. Full HTML body ARS price text scan (visible price text like "ARS $27,189.56")
     *
     * NOTE: EA loads prices client-side via JS on many pages, so strategies 1–4 often fail
     * against the initial HTML. The caller (scrapeEaPage) follows up with a Next.js
     * /_next/data/ fetch (Strategy 1b) using the buildId extracted here.
     */
    private fun extractPriceFromHtml(
        html: String, pageUrl: String, title: String
    ): Pair<StorePrice?, String?> {  // returns (price or null, nextjs buildId or null)
        val isArUrl = pageUrl.contains("/es-ar/") || pageUrl.contains("/es/")
                       || pageUrl.contains("country=ar", ignoreCase = true)
        val fallbackCurrency = if (isArUrl) "ARS" else "USD"
        var buildId: String? = null

        // Strategy 0: Meta tags — standard e-commerce structured meta tags
        extractPriceFromMeta(html, pageUrl, fallbackCurrency)?.let { return Pair(it, buildId) }

        // Strategy 1: __NEXT_DATA__ (Next.js page props JSON)
        // Use string-search approach — more robust than regex on large JSON objects.
        val markerIdx = html.indexOf("""id="__NEXT_DATA__"""")
        if (markerIdx >= 0) {
            try {
                val contentStart = html.indexOf('>', markerIdx) + 1
                val contentEnd   = html.indexOf("</script>", contentStart)
                if (contentEnd > contentStart) {
                    val jsonStr = html.substring(contentStart, contentEnd).trim()
                    val root = json.parseToJsonElement(jsonStr)
                    buildId = (root as? JsonObject)?.get("buildId")?.jsonPrimitive?.content
                    // Also try simple regex as fallback for buildId
                    if (buildId == null) {
                        buildId = Regex(""""buildId"\s*:\s*"([^"]+)"""").find(jsonStr)?.groupValues?.get(1)
                    }
                    extractPriceFromJsonElement(root, fallbackCurrency)?.let { (price, orig, currency, isEaPlay) ->
                        return Pair(buildStorePrice(price, orig, currency, pageUrl, isEaPlay), buildId)
                    }
                }
            } catch (_: Exception) {
                // Try extracting buildId with simple regex even if JSON parsing fails
                if (buildId == null) {
                    buildId = Regex(""""buildId"\s*:\s*"([^"]+)"""").find(html)?.groupValues?.get(1)
                }
            }
        } else {
            // No __NEXT_DATA__ tag but might still have buildId elsewhere in the page
            buildId = Regex(""""buildId"\s*:\s*"([^"]+)"""").find(html)?.groupValues?.get(1)
        }

        // Strategy 2: JSON-LD structured data
        val jsonLdPattern = Pattern.compile(
            """<script[^>]+type=["']application/ld\+json["'][^>]*>(.*?)</script>""",
            Pattern.DOTALL or Pattern.CASE_INSENSITIVE
        )
        val ldMatcher = jsonLdPattern.matcher(html)
        while (ldMatcher.find()) {
            try {
                val root = json.parseToJsonElement(ldMatcher.group(1)!!)
                extractPriceFromJsonElement(root, fallbackCurrency)?.let { (price, orig, currency, isEaPlay) ->
                    return Pair(buildStorePrice(price, orig, currency, pageUrl, isEaPlay), buildId)
                }
            } catch (_: Exception) { }
        }

        // Strategy 3: Regex scan of all inline <script> content
        val scriptPattern = Pattern.compile(
            """<script[^>]*>(.*?)</script>""",
            Pattern.DOTALL or Pattern.CASE_INSENSITIVE
        )
        val scriptMatcher = scriptPattern.matcher(html)
        while (scriptMatcher.find()) {
            val scriptContent = scriptMatcher.group(1) ?: continue
            if (!scriptContent.contains("price", ignoreCase = true)) continue
            extractPriceFromScript(scriptContent, fallbackCurrency)?.let { (price, orig, currency, isEaPlay) ->
                return Pair(buildStorePrice(price, orig, currency, pageUrl, isEaPlay), buildId)
            }
        }

        // Strategy 4: Full HTML body scan for visible ARS/USD price text
        extractPriceFromBodyText(html, pageUrl, fallbackCurrency)?.let {
            return Pair(it, buildId)
        }

        return Pair(null, buildId)
    }

    /**
     * Strategy 0: Look for standard e-commerce meta tags.
     * EA and many stores include Open Graph product price tags.
     */
    private fun extractPriceFromMeta(html: String, pageUrl: String, fallbackCurrency: String): StorePrice? {
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

        var currency = fallbackCurrency
        for (pattern in currencyMetaPatterns) {
            val m = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(html)
            if (m.find()) { currency = m.group(1) ?: fallbackCurrency; break }
        }

        return buildStorePrice(price, price, currency, pageUrl, false)
    }

    /**
     * Strategy 4: Scan the full HTML body for visible price text.
     * EA buy pages render the price as visible text: e.g. "ARS $27,189.56"
     * Since the app runs on the user's Argentine device, EA should return ARS prices.
     */
    private fun extractPriceFromBodyText(html: String, pageUrl: String, fallbackCurrency: String): StorePrice? {
        // EA buy pages render prices as visible text. Since the app runs on the user's
        // Argentine device, the page should contain ARS prices in one of these formats:
        //   "ARS $27.189,56"  "$ 27.189,56"  "$27,189.56"  "27.189,56"
        val isArPage = pageUrl.contains("/es-ar/") || pageUrl.contains("/es/")
        val currency = if (isArPage) "ARS" else fallbackCurrency

        val pricePatterns = listOf(
            // With explicit ARS prefix: "ARS $27.189,56" or "ARS 27.189,56"
            """ARS\s*\$?\s*([\d]{1,3}(?:[.,][\d]{3})*[.,][\d]{2})""",
            """ARS\s*\$?\s*([\d]{4,})""",
            // Dollar sign with Argentine format (dot=thousands, comma=decimal): "$27.189,56"
            """\$\s*([\d]{1,3}\.[\d]{3},[\d]{2})""",
            // Dollar sign with English format: "$27,189.56"
            """\$\s*([\d]{1,3},[\d]{3}\.[\d]{2})""",
            // Raw number Argentine format: "27.189,56" (word boundaries)
            """(?<![0-9.,])([\d]{1,3}\.[\d]{3},[\d]{2})(?![0-9.,])""",
            // Raw number English format: "27,189.56"
            """(?<![0-9.,])([\d]{1,3},[\d]{3}\.[\d]{2})(?![0-9.,])"""
        )

        val candidates = mutableListOf<Float>()
        for (pattern in pricePatterns) {
            val matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(html)
            while (matcher.find()) {
                val raw = matcher.group(1) ?: continue
                val v = parseFormattedPrice(raw) ?: continue
                // Sanity bounds: ARS game prices roughly ARS 500 – ARS 500,000
                if (v >= 100f && v < 1_000_000f) candidates.add(v)
            }
        }

        if (candidates.isEmpty()) return null
        val original = candidates.max()
        val current  = candidates.filter { it >= original * 0.1f }.min()

        return buildStorePrice(current, original, currency, pageUrl, false)
    }

    /** Parse a localized price string to a Float value (handles both . and , as decimal). */
    private fun parseFormattedPrice(value: String): Float? {
        val s = value.trim()
        return when {
            // Spanish/Argentine format: last separator is comma → "27.189,56"
            s.contains(',') && s.lastIndexOf(',') > s.lastIndexOf('.') ->
                s.replace(".", "").replace(",", ".").toFloatOrNull()
            // English format with comma thousands: "27,189.56"
            s.contains('.') && s.contains(',') ->
                s.replace(",", "").toFloatOrNull()
            // Only dots: could be "27189.56" or "27.189" (ambiguous)
            s.contains('.') ->
                s.toFloatOrNull()
            // Pure integer: "27189"
            else ->
                s.replace(",", "").toFloatOrNull()
        }
    }

    /**
     * Recursively walk a JSON element tree looking for objects that contain
     * both a price key and an optional currency key.
     * Returns (currentPrice, originalPrice, currency, isEaPlay) or null.
     */
    private fun extractPriceFromJsonElement(
        element: JsonElement,
        fallbackCurrency: String,
        depth: Int = 0
    ): PriceResult? {
        if (depth > 40) return null

        when (element) {
            is JsonObject -> {
                // Try to read a price directly from this object
                extractPriceFromObject(element, fallbackCurrency)?.let { return it }
                // Recurse into children
                for ((_, child) in element) {
                    extractPriceFromJsonElement(child, fallbackCurrency, depth + 1)
                        ?.let { return it }
                }
            }
            is JsonArray -> {
                for (item in element) {
                    extractPriceFromJsonElement(item, fallbackCurrency, depth + 1)
                        ?.let { return it }
                }
            }
            else -> { /* primitives: nothing to do */ }
        }
        return null
    }

    private fun extractPriceFromObject(obj: JsonObject, fallbackCurrency: String): PriceResult? {
        // Price field names used by EA's various API/page structures
        // Ordered from most specific to most generic to avoid false matches
        val finalPriceKeys  = listOf(
            "finalPrice", "salePrice", "discountedPrice", "currentPrice", "offerPrice",
            // EA App / new storefront
            "discountedAmount", "sellablePrice", "priceAfterDiscount", "activationPrice",
            // Generic
            "price", "amount"
        )
        val basePriceKeys   = listOf(
            "basePrice", "regularPrice", "originalPrice", "retailPrice", "msrp",
            // EA App / new storefront
            "fullAmount", "listPrice", "undiscountedPrice", "strikethroughPrice"
        )
        val currencyKeys    = listOf("currency", "currencyCode", "priceCurrency", "currencyIso")
        val eaPlayKeys      = listOf("subscriptionPlan", "includesPlay", "subscriptionIncluded",
                                     "eaPlayIncluded", "playIncluded", "vaultEligible")

        val finalRaw = finalPriceKeys.firstNotNullOfOrNull { obj[it]?.jsonPrimitive?.content?.toFloatOrNull() }
            ?: return null
        if (finalRaw < 0) return null

        val baseRaw = basePriceKeys.firstNotNullOfOrNull { obj[it]?.jsonPrimitive?.content?.toFloatOrNull() }
            ?: finalRaw

        val currency = currencyKeys.firstNotNullOfOrNull { obj[it]?.jsonPrimitive?.content }
            ?: fallbackCurrency

        // Detect EA Play membership inclusion
        val isEaPlay = eaPlayKeys.any { key ->
            obj[key]?.jsonPrimitive?.content?.let { v ->
                v.equals("true", ignoreCase = true) || v == "1"
            } == true
        }

        // Origin API returns some prices in cents (e.g. 699900 for ARS 6,999.00)
        val divisor = if (finalRaw > 100_000f) 100f else 1f
        val finalPrice = finalRaw / divisor
        val basePrice  = baseRaw  / divisor

        return PriceResult(finalPrice, basePrice, currency, isEaPlay)
    }

    /** Extract price from a raw <script> block using regex. */
    private fun extractPriceFromScript(script: String, fallbackCurrency: String): PriceResult? {
        // Look for finalPrice / currentPrice / price patterns
        val pricePatterns = listOf(
            """["\']finalPrice["\']\s*:\s*["']?([0-9]+\.?[0-9]*)["']?""",
            """["\']currentPrice["\']\s*:\s*["']?([0-9]+\.?[0-9]*)["']?""",
            """["\']salePrice["\']\s*:\s*["']?([0-9]+\.?[0-9]*)["']?""",
            """["\']price["\']\s*:\s*["']?([0-9]+\.?[0-9]*)["']?"""
        )
        val basePricePatterns = listOf(
            """["\']basePrice["\']\s*:\s*["']?([0-9]+\.?[0-9]*)["']?""",
            """["\']originalPrice["\']\s*:\s*["']?([0-9]+\.?[0-9]*)["']?"""
        )
        val currencyPattern = Pattern.compile(
            """["\'](?:currency|currencyCode|priceCurrency)["\']\s*:\s*["']([A-Z]{3})["']"""
        )

        var finalPrice: Float? = null
        for (pattern in pricePatterns) {
            val m = Pattern.compile(pattern).matcher(script)
            if (m.find()) {
                finalPrice = m.group(1)?.toFloatOrNull()
                if (finalPrice != null && finalPrice > 0) break
            }
        }
        if (finalPrice == null || finalPrice <= 0) return null

        var basePrice = finalPrice
        for (pattern in basePricePatterns) {
            val m = Pattern.compile(pattern).matcher(script)
            if (m.find()) {
                basePrice = m.group(1)?.toFloatOrNull() ?: finalPrice
                break
            }
        }

        val currMatcher = currencyPattern.matcher(script)
        val currency = if (currMatcher.find()) currMatcher.group(1) ?: fallbackCurrency
                       else fallbackCurrency

        // Use non-nullable locals for arithmetic (var smart-cast limitation)
        val fp = finalPrice!!
        val bp = basePrice ?: fp
        val divisor = if (fp > 100_000f) 100f else 1f
        return PriceResult(fp / divisor, bp / divisor, currency, false)
    }

    private fun buildStorePrice(
        current: Float,
        original: Float,
        currency: String,
        pageUrl: String,
        isEaPlay: Boolean
    ): StorePrice {
        val discountPct = if (original > current && original > 0)
            ((1f - current / original) * 100).toInt() else 0
        // Construct a clean buy URL: use the es-ar locale if available
        val storeUrl = if (pageUrl.endsWith("/buy")) pageUrl
                       else "$pageUrl/buy".replace("//buy", "/buy")
        return StorePrice(
            storeName    = "EA",
            currentPrice = current,
            originalPrice = original,
            discountPercent = discountPct,
            currency    = currency,
            isFree      = current == 0f && !isEaPlay,
            storeUrl    = storeUrl,
            isEaPlay    = isEaPlay
        )
    }

    // ──────────────────────────────────────────────────────────────
    //  Origin API fallback (for games without a known page URL)
    // ──────────────────────────────────────────────────────────────

    private suspend fun tryOriginApi(title: String): StorePrice? =
        withContext(Dispatchers.IO) {
            val encoded = URLEncoder.encode(title, "UTF-8")
            val fallbackUrl = "https://www.ea.com/es-ar/search#q=$encoded"
            val endpoints = listOf(
                "https://api3.origin.com/ecommerce2/public/supercat/AR/es_AR?searchTerm=$encoded",
                "https://api1.origin.com/ecommerce2/public/supercat/AR/es_AR?searchTerm=$encoded",
                "https://api2.origin.com/ecommerce2/public/supercat/AR/es_AR?searchTerm=$encoded",
                "https://api3.origin.com/ecommerce2/public/supercat/WW/en_US?searchTerm=$encoded",
                "https://api1.origin.com/ecommerce2/public/supercat/WW/en_US?searchTerm=$encoded"
            )
            for (endpoint in endpoints) {
                try {
                    val conn = openBrowserConnection(endpoint)
                    if (conn.responseCode != 200) continue
                    val response = conn.inputStream.bufferedReader().readText()
                    val parsed = json.decodeFromString<JsonObject>(response)
                    val offers = parsed["offers"]?.jsonArray
                        ?: parsed["games"]?.jsonArray
                        ?: parsed["items"]?.jsonArray
                        ?: parsed["results"]?.jsonArray
                        ?: continue
                    for (item in offers.take(5)) {
                        val obj = try { item.jsonObject } catch (_: Exception) { continue }
                        val resultName = obj["displayName"]?.jsonPrimitive?.content
                            ?: obj["name"]?.jsonPrimitive?.content ?: ""
                        if (resultName.isNotEmpty() && !titlesMatch(title, resultName)) continue
                        val priceResult = extractPriceFromObject(obj, "USD") ?: continue
                        return@withContext buildStorePrice(
                            priceResult.current, priceResult.original,
                            priceResult.currency, fallbackUrl, priceResult.isEaPlay
                        )
                    }
                } catch (_: Exception) { continue }
            }
            null
        }

    // ──────────────────────────────────────────────────────────────
    //  Helpers
    // ──────────────────────────────────────────────────────────────

    private data class PriceResult(
        val current: Float,
        val original: Float,
        val currency: String,
        val isEaPlay: Boolean
    )

    private fun titlesMatch(query: String, candidate: String): Boolean {
        fun norm(s: String) = s.lowercase()
            .replace(Regex("[':,.-]"), " ")
            .replace(Regex("\\s+"), " ").trim()
        val q = norm(query); val c = norm(candidate)
        if (q == c || q.contains(c) || c.contains(q)) return true
        val qWords = q.split(" ").filter { it.length >= 3 }
        if (qWords.size == 1) return c.contains(qWords[0])
        val stop = setOf("the", "a", "an", "of", "in", "to", "at", "for", "and")
        val qf = qWords.filter { it.length >= 4 && it !in stop }.toSet()
        val cf = c.split(" ").filter { it.length >= 4 && it !in stop }.toSet()
        if (qf.isEmpty() || cf.isEmpty()) return false
        val common = qf.intersect(cf)
        return common.size >= (minOf(qf.size, cf.size) + 1) / 2
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
        conn.setRequestProperty("Referer", "https://www.ea.com/es-ar/")
        conn.setRequestProperty("Cookie", "country=ar; locale=es_AR")
        return conn
    }
}
