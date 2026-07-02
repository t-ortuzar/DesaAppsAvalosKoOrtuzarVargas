package com.example.desaappsavaloskoortuzarvargas.data.api

import com.example.desaappsavaloskoortuzarvargas.data.local.ConnectivityObserver
import com.example.desaappsavaloskoortuzarvargas.data.local.dao.GamePriceDao
import com.example.desaappsavaloskoortuzarvargas.data.local.dao.PriceHistoryDao
import com.example.desaappsavaloskoortuzarvargas.data.local.entity.GamePriceEntity
import com.example.desaappsavaloskoortuzarvargas.data.local.entity.PriceHistoryEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Manages background price refreshing with batching and staleness logic.
 *
 * Strategy:
 * - **On game detail open**: Show cached prices instantly, then refresh from APIs
 *   in the background. Only hit the network if cache is older than [DETAIL_STALE_MS].
 * - **Offers section** (high priority): Refresh stale offer prices every
 *   [OFFERS_REFRESH_INTERVAL_MS] in batches of [BATCH_SIZE] with [BATCH_DELAY_MS]
 *   between batches.
 * - **General catalog**: Refresh in the background every [CATALOG_REFRESH_INTERVAL_MS],
 *   processing the oldest-cached games first, in batches.
 *
 * All network calls go through the individual store services (Steam, Epic, GOG, etc.).
 * This class coordinates WHEN and HOW MANY calls to make, not WHERE to call.
 */
class PriceRefreshManager(
    private val gamePriceDao: GamePriceDao,
    private val priceHistoryDao: PriceHistoryDao,
    private val steamPriceService: SteamPriceService,
    private val epicPriceService: EpicPriceService,
    private val gogPriceService: GogPriceService,
    private val xboxPriceService: XboxPriceService,
    private val ubisoftPriceService: UbisoftPriceService,
    private val battleNetPriceService: BattleNetPriceService,
    private val eaPriceService: EAPriceService,
    private val connectivityObserver: ConnectivityObserver
) {
    companion object {
        /** Cache is "stale" for individual game detail after 5 minutes. */
        const val DETAIL_STALE_MS = 5 * 60 * 1000L    // 5 min

        /** Offers refresh cycle: every 15 minutes. */
        const val OFFERS_REFRESH_INTERVAL_MS = 15 * 60 * 1000L  // 15 min

        /** General catalog refresh cycle: every 1 hour. */
        const val CATALOG_REFRESH_INTERVAL_MS = 60 * 60 * 1000L  // 1 h

        /** Number of games to refresh per batch. */
        const val BATCH_SIZE = 5

        /** Delay between batches to avoid overwhelming APIs. */
        const val BATCH_DELAY_MS = 3_000L  // 3 seconds

        /** Delay between individual game fetches within a batch. */
        const val INTRA_BATCH_DELAY_MS = 500L  // 500ms
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var offersRefreshJob: Job? = null
    private var catalogRefreshJob: Job? = null

    // Steam App IDs for quick lookup (populated from GameCatalog)
    private var steamAppIdMap: Map<String, Int> = emptyMap()

    // Platform lists per game — limits which stores are queried in background refresh.
    private var gamePlatformsMap: Map<String, List<String>> = emptyMap()

    // Xbox product IDs per game (name → productId) — enables direct product lookup.
    private var xboxProductIdMap: Map<String, String> = emptyMap()

    // Xbox title hints per game (name → localized title for search matching).
    private var xboxTitleHintMap: Map<String, String> = emptyMap()

    /**
     * Per-store search title overrides (game name → store name → search title).
     * When a game has different names on different stores (e.g. "Dead Space Remake" is listed
     * as "Dead Space" on EA and Xbox), the hint tells each service what title to search for.
     * perStoreHintMap takes priority over xboxTitleHintMap for Xbox.
     */
    private var perStoreHintMap: Map<String, Map<String, String>> = emptyMap()

    /**
     * Verified Epic Games Store URLs per game (name → full URL).
     * When set, overrides the URL returned by the Epic GraphQL API, which sometimes
     * returns outdated/short slugs that resolve to 404 pages.
     * The price itself is always taken from the API; only the URL is overridden.
     */
    private var epicVerifiedUrlMap: Map<String, String> = emptyMap()

    /**
     * EA game page URLs per game (name → EA store URL).
     * When set, EAPriceService scrapes the page directly instead of using the
     * unreliable Origin search API. Much higher success rate for known games.
     */
    private var eaGameUrlMap: Map<String, String> = emptyMap()

    /**
     * Ubisoft Store verified URLs per game (name → Ubisoft store URL).
     * When set and the Ubisoft search API fails, a link-only card is shown so
     * users can still navigate to the store page.
     */
    private var ubiVerifiedUrlMap: Map<String, String> = emptyMap()

    /**
     * Battle.net product slugs per game (name → slug used in Battle.net product URLs).
     * Enables direct product page lookup: us.shop.battle.net/es-ar/product/{slug}
     */
    private var battleNetSlugMap: Map<String, String> = emptyMap()

    /**
     * Set the mapping from game name → Steam App ID.
     * Called once when the game catalog is loaded.
     */
    fun setSteamAppIds(map: Map<String, Int>) {
        steamAppIdMap = map
    }

    /**
     * Set the mapping from game name → available store platforms.
     * When set, background refresh only queries stores where the game is actually sold.
     * This prevents GOG from being called for Steam-only games, etc.
     */
    fun setGamePlatforms(map: Map<String, List<String>>) {
        gamePlatformsMap = map
    }

    /** Set the mapping from game name → Xbox product ID for direct lookups. */
    fun setXboxProductIds(map: Map<String, String>) {
        xboxProductIdMap = map
    }

    /** Set the mapping from game name → localized Xbox title hint for better search matching. */
    fun setXboxTitleHints(map: Map<String, String>) {
        xboxTitleHintMap = map
    }

    /**
     * Set per-store search title overrides (game name → store name → search title).
     * These hints allow each store to be searched with the title it recognizes, even when
     * our catalog uses a different name (e.g. "Dead Space Remake" vs "Dead Space" on EA/Xbox).
     */
    fun setPerStoreSearchHints(map: Map<String, Map<String, String>>) {
        perStoreHintMap = map
    }

    /**
     * Set the mapping from game name → verified Epic Games Store URL.
     * When set, overrides the storeUrl from the Epic GraphQL API response so the
     * user is always directed to the correct page (the API sometimes returns slugs
     * that resolve to 404 pages, e.g. "black-myth-wukong" vs "black-myth-wukong-87a72b").
     */
    fun setEpicVerifiedUrls(map: Map<String, String>) {
        epicVerifiedUrlMap = map
    }

    /**
     * Set the mapping from game name → EA game page URL.
     * When set, EAPriceService will scrape the page directly rather than using
     * the Origin search API, which is unreliable for Argentina pricing.
     */
    fun setEaGameUrls(map: Map<String, String>) {
        eaGameUrlMap = map
    }

    /**
     * Set the mapping from game name → Ubisoft Store AR URL.
     * When set and the Ubisoft search API returns no result, a link-only
     * price card is shown so users can still open the store page.
     */
    fun setUbisoftVerifiedUrls(map: Map<String, String>) {
        ubiVerifiedUrlMap = map
    }

    /**
     * Set the mapping from game name → Battle.net product slug.
     * When set, BattleNetPriceService can scrape the product page directly instead of
     * relying on the search API, which is less reliable.
     */
    fun setBattleNetSlugs(map: Map<String, String>) {
        battleNetSlugMap = map
    }

    // ──────────────────────────────────────────────────────────────
    //  Single-game refresh (used when user opens a game detail)
    // ──────────────────────────────────────────────────────────────

    /**
     * Check if cached prices for [gameName] are fresh enough.
     * Returns true if prices exist and are newer than [maxAge].
     */
    suspend fun isCacheFresh(gameName: String, maxAge: Long = DETAIL_STALE_MS): Boolean {
        val oldest = gamePriceDao.getOldestTimestamp(gameName) ?: return false
        return (System.currentTimeMillis() - oldest) < maxAge
    }

    /**
     * Get cached prices for a game. Returns empty list if none cached.
     */
    suspend fun getCachedPrices(gameName: String): List<StorePrice> {
        return gamePriceDao.getPricesForGameByName(gameName).map { it.toStorePrice() }
    }

    /**
     * Get ALL cached prices from the database (all games, all stores).
     */
    suspend fun getAllCachedPrices(): List<GamePriceEntity> {
        return gamePriceDao.getAllPrices()
    }

    /**
     * Check if the current price for a game/store is the all-time historical low.
     */
    suspend fun isHistoricalLow(gameName: String, storeName: String, currentPrice: Float): Boolean {
        if (currentPrice <= 0f) return false
        val historicalLow = priceHistoryDao.getHistoricalLowPrice(gameName, storeName)
        return historicalLow == null || currentPrice <= historicalLow
    }

    /**
     * Detect a permanent base price drop: when the publisher reduced the non-discounted price.
     * Returns the previous retail price if a drop is detected, null otherwise.
     */
    suspend fun detectPriceDrop(gameName: String, storeName: String, currentRetailPrice: Float): Float? {
        if (currentRetailPrice <= 0f) return null
        val previousRetail = priceHistoryDao.getPreviousRetailPrice(gameName, storeName) ?: return null
        // Require at least 10% drop to consider it a "permanent price drop"
        return if (currentRetailPrice < previousRetail * 0.9f) previousRetail else null
    }

    /**
     * Fetch fresh prices for a single game from all stores.
     * Saves to cache. Returns the fetched prices.
     * [platforms] limits which stores are queried (null = all stores).
     */
    suspend fun fetchAndCachePrices(
        gameName: String,
        steamAppId: Int? = null,
        platforms: List<String>? = null
    ): List<StorePrice> {
        if (!(connectivityObserver.isConnected())) return emptyList()

        val prices = fetchAllStorePrices(gameName, steamAppId, platforms)
        if (prices.isNotEmpty()) {
            savePricesToCache(gameName, prices)
        }
        return prices
    }

    // ──────────────────────────────────────────────────────────────
    //  Background batch refresh
    // ──────────────────────────────────────────────────────────────

    /**
     * Start the periodic background refresh for offers and catalog.
     * Call once from the main activity / app initialization.
     */
    fun startPeriodicRefresh() {
        startOffersRefreshLoop()
        startCatalogRefreshLoop()
    }

    /**
     * Stop all background refresh jobs.
     */
    fun stopPeriodicRefresh() {
        offersRefreshJob?.cancel()
        catalogRefreshJob?.cancel()
    }

    /**
     * Refresh a specific list of game names (e.g., games shown in offers).
     * Processes in batches of [BATCH_SIZE].
     *
     * @param onBatchComplete Called after each batch so the UI can update progressively.
     */
    suspend fun refreshBatch(
        gameNames: List<String>,
        onBatchComplete: (suspend () -> Unit)? = null
    ) {
        if (!(connectivityObserver.isConnected())) return

        val batches = gameNames.chunked(BATCH_SIZE)
        for (batch in batches) {
            for (gameName in batch) {
                try {
                    val steamAppId = steamAppIdMap[gameName]
                    // Use the platform list from the catalog so we only query stores
                    // where the game is actually sold. Null = query all stores (fallback
                    // when the platform map hasn't been populated yet).
                    val platforms = gamePlatformsMap[gameName]
                    fetchAndCachePrices(gameName, steamAppId, platforms)
                } catch (e: CancellationException) { throw e }
                catch (_: Exception) { /* skip failed game, continue batch */ }
                delay(INTRA_BATCH_DELAY_MS)
            }
            // Notify after each batch so UI can refresh with new data
            onBatchComplete?.invoke()
            if (batch != batches.last()) {
                delay(BATCH_DELAY_MS)
            }
        }
    }

    /**
     * Refresh the stalest cached games. Called periodically for
     * general catalog freshness.
     */
    suspend fun refreshStalestGames(maxGames: Int = BATCH_SIZE * 3) {
        if (!(connectivityObserver.isConnected())) return

        val staleThreshold = System.currentTimeMillis() - CATALOG_REFRESH_INTERVAL_MS
        val staleNames = gamePriceDao.getStaleGameNames(staleThreshold)
            .take(maxGames)

        if (staleNames.isNotEmpty()) {
            refreshBatch(staleNames)
        }
    }

    /**
     * Ensure all catalog games have been fetched at least once.
     * Games that already have cached prices are skipped.
     * Called on app startup to populate the offers section.
     *
     * @param onBatchComplete Called after each batch finishes so the UI
     *        can refresh progressively instead of waiting for all games.
     */
    suspend fun ensureAllGamesCached(
        allGameNames: List<String>,
        onBatchComplete: (suspend () -> Unit)? = null
    ) {
        if (!(connectivityObserver.isConnected())) return

        val cachedNames = gamePriceDao.getAllCachedGameNamesOldestFirst().toSet()
        val uncached = allGameNames.filter { it !in cachedNames }

        if (uncached.isNotEmpty()) {
            refreshBatch(uncached, onBatchComplete)
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  Private helpers
    // ──────────────────────────────────────────────────────────────

    private fun startOffersRefreshLoop() {
        offersRefreshJob?.cancel()
        offersRefreshJob = scope.launch {
            while (true) {
                delay(OFFERS_REFRESH_INTERVAL_MS)
                try {
                    // Refresh all cached games — offers are most important
                    val staleThreshold = System.currentTimeMillis() - OFFERS_REFRESH_INTERVAL_MS
                    val stale = gamePriceDao.getStaleGameNames(staleThreshold)
                    if (stale.isNotEmpty()) {
                        refreshBatch(stale)
                    }
                } catch (e: CancellationException) { throw e }
                catch (_: Exception) { }
            }
        }
    }

    private fun startCatalogRefreshLoop() {
        catalogRefreshJob?.cancel()
        catalogRefreshJob = scope.launch {
            // Initial delay — let the app start up first
            delay(60_000)
            while (true) {
                try {
                    refreshStalestGames()
                } catch (e: CancellationException) { throw e }
                catch (_: Exception) { }
                delay(CATALOG_REFRESH_INTERVAL_MS)
            }
        }
    }

    /**
     * Fetch prices from all stores in parallel for a single game.
     *
     * The [platforms] parameter is respected: each store is only queried if the game
     * is listed on that platform in the catalog. This prevents false positives such as
     * a game appearing on Epic when it's actually only on Steam/GOG.
     *
     * Steam is always skipped when no [steamAppId] is provided (no reliable text-search).
     * If [platforms] is null all stores are queried (backwards-compatible fallback).
     */
    private suspend fun fetchAllStorePrices(
        gameName: String,
        steamAppId: Int? = null,
        platforms: List<String>? = null
    ): List<StorePrice> = kotlinx.coroutines.coroutineScope {
        val prices = mutableListOf<StorePrice>()

        // Per-platform flags — if platforms list is null, query everything (safe fallback)
        val queryEpic      = platforms == null || "Epic Games"        in platforms
        val queryGog       = platforms == null || "GOG"               in platforms
        val queryXbox      = platforms == null || "Xbox / Microsoft"  in platforms
        val queryUbisoft   = platforms == null || "Ubisoft"           in platforms
        val queryBattleNet = platforms == null || "Battle.net"        in platforms
        val queryEa        = platforms == null || "EA"                in platforms

        // Steam: requires known App ID — no reliable text search alternative
        val steamJob = async {
            if (steamAppId != null && steamAppId > 0) {
                try {
                    steamPriceService.getArgentinePrice(steamAppId)?.let { arPrice ->
                        // Detect EA Play: Steam marks some EA games as free (is_free=true) or
                        // shows no AR pricing (price_overview=null → price=0) because
                        // they're only available via EA Play subscription on Steam.
                        // We detect this when price=0 on Steam AND the game is on the EA platform.
                        // F2P games are excluded from the price-fetch loop, so any 0-price
                        // EA game reaching here is an EA Play subscription title.
                        val isEaPlay = arPrice.price == 0f &&
                            gamePlatformsMap[gameName]?.contains("EA") == true
                        StorePrice(
                            storeName = "Steam",
                            currentPrice = arPrice.price,
                            originalPrice = arPrice.retailPrice,
                            discountPercent = arPrice.discountPercent,
                            currency = arPrice.currency,
                            // Don't mark as "free" if it's an EA Play subscription entry
                            isFree = arPrice.isFree && !isEaPlay,
                            storeUrl = "https://store.steampowered.com/app/$steamAppId",
                            discountEndTimestamp = arPrice.discountEndTimestamp,
                            isEaPlay = isEaPlay
                        )
                    }
                } catch (_: Exception) { null }
            } else null
        }

        // Epic: page scrape first (ensures base edition price), then GraphQL search fallback
        // The isVerifiedLink fallback is created OUTSIDE the try-catch so it always fires
        // even if an exception escapes the price-fetch attempts.
        val epicJob = async {
            if (!queryEpic) return@async null
            val epicTitle = perStoreHintMap[gameName]?.get("Epic Games") ?: gameName
            val verifiedUrl = epicVerifiedUrlMap[gameName]

            var result: StorePrice? = runCatching {
                var r: StorePrice? = null

                // Attempt 1: scrape the verified product page directly.
                // This guarantees we get the BASE GAME edition price (not Gold/Ultimate which
                // can rank higher in GraphQL search results for some titles like AC Shadows).
                if (verifiedUrl != null) {
                    r = epicPriceService.fetchPriceFromProductPage(verifiedUrl, gameName)
                }

                // Attempt 2: GraphQL search with the hint title (colon stripped etc.)
                if (r == null) {
                    r = epicPriceService.searchGamePrice(epicTitle)
                }

                // Attempt 3: if hint search failed and hint differs from original, try original name
                if (r == null && epicTitle != gameName) {
                    r = epicPriceService.searchGamePrice(gameName)
                }

                // Handle EA Play on Epic: some games (e.g. Jedi Survivor) are available via
                // EA Play Pro subscription on Epic, which can make them appear as price=0/free
                // in the GraphQL results. Both EpicPriceService and the page scraper already try
                // to prefer the non-zero standalone purchase price (see EpicPriceService).
                // If after all attempts the result is still isFree=true AND the game is on the EA
                // platform, we invalidate it — it means the real standalone price was not found
                // and we should show an isVerifiedLink card instead of showing "$0 / FREE".
                if (r != null && r.isFree && r.currentPrice == 0f &&
                    gamePlatformsMap[gameName]?.contains("EA") == true) {
                    r = null  // EA Play subscription price — standalone purchase price not found
                }

                r
            }.getOrNull()

            // Attempt 4: if we still have nothing but have a verified URL, show link-only card
            // so the user can at least navigate to the Epic store page.
            // This runs OUTSIDE runCatching so it always fires even if an exception occurred above.
            if (result == null && verifiedUrl != null) {
                result = StorePrice(
                    storeName = "Epic Games",
                    currentPrice = 0f,
                    originalPrice = 0f,
                    discountPercent = 0,
                    currency = "USD",
                    isFree = false,
                    storeUrl = verifiedUrl,
                    isVerifiedLink = true
                )
            }

            if (result != null && verifiedUrl != null && !result.isVerifiedLink)
                result.copy(storeUrl = verifiedUrl)
            else result
        }

        // GOG: text search with 4-pass bidirectional matching
        val gogJob = async {
            if (!queryGog) return@async null
            try {
                val gogTitle = perStoreHintMap[gameName]?.get("GOG") ?: gameName
                gogPriceService.searchGamePrice(gogTitle)
            } catch (_: Exception) { null }
        }

        // Xbox: direct product ID lookup (most reliable) → title search fallback
        // NOTE: if a known product ID is provided, we trust the PC-validation result from
        // fetchProductPrice and do NOT fall back to searchGamePrice on null — a null result
        // means the product was confirmed console-only (e.g. a game only on Xbox Series X|S).
        // We only fall back to text search when NO product ID is known.
        val xboxJob = async {
            if (!queryXbox) return@async null
            try {
                val productId = xboxProductIdMap[gameName]
                val titleHint = perStoreHintMap[gameName]?.get("Xbox / Microsoft")
                    ?: xboxTitleHintMap[gameName]
                if (productId != null) {
                    // Direct product lookup — trust null result (console-only or not found)
                    xboxPriceService.fetchProductPrice(productId, gameName)
                } else {
                    // No known product ID → text search (includes PC platform validation)
                    xboxPriceService.searchGamePrice(gameName, titleHint)
                }
            } catch (_: Exception) { null }
        }

        // Ubisoft: page scrape with catalog URL → Demandware search API → isVerifiedLink
        val ubisoftJob = async {
            if (!queryUbisoft) return@async null
            try {
                val ubisoftTitle = perStoreHintMap[gameName]?.get("Ubisoft") ?: gameName
                val verifiedUrl = ubiVerifiedUrlMap[gameName]
                ubisoftPriceService.searchGamePrice(ubisoftTitle, verifiedUrl)
            } catch (_: Exception) { null }
        }

        // Battle.net: product page scraping → API search → isVerifiedLink
        val battleNetJob = async {
            if (!queryBattleNet) return@async null
            try {
                val battleNetTitle = perStoreHintMap[gameName]?.get("Battle.net") ?: gameName
                val productSlug = battleNetSlugMap[gameName]
                battleNetPriceService.searchGamePrice(battleNetTitle, productSlug)
            } catch (_: Exception) { null }
        }

        // EA (Origin): page scrape with catalog URL → Origin API fallback → isVerifiedLink
        val eaJob = async {
            if (!queryEa) return@async null
            try {
                val eaTitle = perStoreHintMap[gameName]?.get("EA") ?: gameName
                val eaPageUrl = eaGameUrlMap[gameName]
                eaPriceService.searchGamePrice(eaTitle, eaPageUrl)
            } catch (_: Exception) { null }
        }

        steamJob.await()?.let { prices.add(it) }
        epicJob.await()?.let { prices.add(it) }
        gogJob.await()?.let { prices.add(it) }
        xboxJob.await()?.let { prices.add(it) }
        ubisoftJob.await()?.let { prices.add(it) }
        battleNetJob.await()?.let { prices.add(it) }
        eaJob.await()?.let { prices.add(it) }

        prices
    }

    private suspend fun savePricesToCache(gameName: String, prices: List<StorePrice>) {
        try {
            // Never cache "verified link" entries — they have no real price and get re-created
            // on every live fetch. Caching them would persist a $0 entry with no useful data.
            val cacheable = prices.filter { !it.isVerifiedLink }
            if (cacheable.isEmpty() && prices.any { it.isVerifiedLink }) {
                // All results were link-only — clear any stale prices but write nothing new
                gamePriceDao.deletePricesForGameByName(gameName)
                return
            }
            gamePriceDao.deletePricesForGameByName(gameName)
            gamePriceDao.insertAll(cacheable.map { price ->
                GamePriceEntity(
                    gameId = 0,
                    gameName = gameName,
                    storeName = price.storeName,
                    currentPrice = price.currentPrice,
                    retailPrice = price.originalPrice,
                    savings = price.discountPercent.toFloat(),
                    dealUrl = price.storeUrl,
                    currency = price.currency,
                    discountEndTimestamp = price.discountEndTimestamp,
                    isGamePass = price.isGamePass,
                    isEaPlay = price.isEaPlay
                )
            })
            // Record price history snapshot — skip link-only entries (no real price data)
            val now = System.currentTimeMillis()
            priceHistoryDao.insertAll(cacheable.map { price ->
                PriceHistoryEntity(
                    gameName = gameName,
                    storeName = price.storeName,
                    currentPrice = price.currentPrice,
                    retailPrice = price.originalPrice,
                    discountPercent = price.discountPercent,
                    currency = price.currency,
                    timestamp = now
                )
            })
        } catch (_: Exception) { }
    }

    private fun GamePriceEntity.toStorePrice() = StorePrice(
        storeName = storeName,
        currentPrice = currentPrice,
        originalPrice = retailPrice,
        discountPercent = savings.toInt(),
        currency = currency,
        isFree = currentPrice == 0f && !isGamePass && !isEaPlay,  // Don't mark as "free" if it's a subscription entry
        storeUrl = dealUrl,
        discountEndTimestamp = discountEndTimestamp,
        isGamePass = isGamePass,
        isEaPlay = isEaPlay
    )
}

