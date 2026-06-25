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

    /**
     * Set the mapping from game name → Steam App ID.
     * Called once when the game catalog is loaded.
     */
    fun setSteamAppIds(map: Map<String, Int>) {
        steamAppIdMap = map
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
                    fetchAndCachePrices(gameName, steamAppId)
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
     * Fetch prices from all 7 stores in parallel for a single game.
     * [platforms] filters which stores to query — only stores in this list are called.
     * Null means "query all stores".
     */
    private suspend fun fetchAllStorePrices(
        gameName: String,
        steamAppId: Int? = null,
        platforms: List<String>? = null
    ): List<StorePrice> = kotlinx.coroutines.coroutineScope {
        val prices = mutableListOf<StorePrice>()
        val callAll = platforms == null

        val steamJob = async {
            if ((callAll || "Steam" in platforms!!) && steamAppId != null && steamAppId > 0) {
                try {
                    steamPriceService.getArgentinePrice(steamAppId)?.let { arPrice ->
                        StorePrice(
                            storeName = "Steam",
                            currentPrice = arPrice.price,
                            originalPrice = arPrice.retailPrice,
                            discountPercent = arPrice.discountPercent,
                            currency = arPrice.currency,
                            isFree = arPrice.isFree,
                            storeUrl = "https://store.steampowered.com/app/$steamAppId",
                            discountEndTimestamp = arPrice.discountEndTimestamp
                        )
                    }
                } catch (_: Exception) { null }
            } else null
        }

        val epicJob = async {
            if (callAll || "Epic Games" in platforms!!) {
                try { epicPriceService.searchGamePrice(gameName) } catch (_: Exception) { null }
            } else null
        }

        val gogJob = async {
            if (callAll || "GOG" in platforms!!) {
                try { gogPriceService.searchGamePrice(gameName) } catch (_: Exception) { null }
            } else null
        }

        val xboxJob = async {
            if (callAll || "Xbox / Microsoft" in platforms!!) {
                try { xboxPriceService.searchGamePrice(gameName) } catch (_: Exception) { null }
            } else null
        }

        val ubisoftJob = async {
            if (callAll || "Ubisoft" in platforms!!) {
                try { ubisoftPriceService.searchGamePrice(gameName) } catch (_: Exception) { null }
            } else null
        }

        val battleNetJob = async {
            if (callAll || "Battle.net" in platforms!!) {
                try { battleNetPriceService.searchGamePrice(gameName) } catch (_: Exception) { null }
            } else null
        }

        val eaJob = async {
            if (callAll || "EA" in platforms!!) {
                try { eaPriceService.searchGamePrice(gameName) } catch (_: Exception) { null }
            } else null
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
            gamePriceDao.deletePricesForGameByName(gameName)
            gamePriceDao.insertAll(prices.map { price ->
                GamePriceEntity(
                    gameId = 0,
                    gameName = gameName,
                    storeName = price.storeName,
                    currentPrice = price.currentPrice,
                    retailPrice = price.originalPrice,
                    savings = price.discountPercent.toFloat(),
                    dealUrl = price.storeUrl,
                    currency = price.currency,
                    discountEndTimestamp = price.discountEndTimestamp
                )
            })
            // Record price history snapshot for historical tracking
            val now = System.currentTimeMillis()
            priceHistoryDao.insertAll(prices.map { price ->
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
        isFree = currentPrice == 0f,
        storeUrl = dealUrl,
        discountEndTimestamp = discountEndTimestamp
    )
}

