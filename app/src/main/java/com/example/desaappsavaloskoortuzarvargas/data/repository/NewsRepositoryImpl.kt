package com.example.desaappsavaloskoortuzarvargas.data.repository

import com.example.desaappsavaloskoortuzarvargas.data.api.SteamNewsItem
import com.example.desaappsavaloskoortuzarvargas.data.api.SteamNewsService
import com.example.desaappsavaloskoortuzarvargas.data.catalog.GameCatalog
import com.example.desaappsavaloskoortuzarvargas.domain.model.News
import com.example.desaappsavaloskoortuzarvargas.domain.repository.NewsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class NewsRepositoryImpl(
    private val steamNewsService: SteamNewsService = SteamNewsService()
) : NewsRepository {

    /** Static fallback news generated from the catalog */
    private val staticNews = GameCatalog.generateNews()

    /** Combined live + static news, refreshed once per session per cache TTL */
    private var cachedNews: List<News>? = null
    private var lastFetchMs: Long = 0L
    private val SESSION_CACHE_TTL_MS = 15 * 60 * 1000L  // 15 minutes

    /**
     * Top games by rating that have Steam App IDs.
     * We fetch real news from the Steam News API for these games.
     * Ordered by Metacritic score descending; limited to the most actively updated titles.
     */
    private val topSteamGames: List<Pair<Int, Int>> by lazy {
        // Map: gameId → steamAppId  (only for games with steamAppId > 0)
        val allGames = GameCatalog.generateGames()
        allGames
            .filter { it.steamAppId > 0 }
            .sortedByDescending { it.rating }
            .take(20)
            .map { it.id to it.steamAppId }
    }

    /** steamAppId → gameId reverse map */
    private val appIdToGameId: Map<Int, Int> by lazy {
        topSteamGames.associate { (gameId, appId) -> appId to gameId }
    }

    /** gameId → imageUrl map for news card thumbnails */
    private val gameIdToImage: Map<Int, String> by lazy {
        GameCatalog.generateGames().associate { it.id to it.imageUrl }
    }

    // ──────────────────────────────────────────────────────────────

    override suspend fun getAllNews(): Result<List<News>> = try {
        Result.success(getCombinedNews().sortedByDescending { it.date })
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun getNewsByGameId(gameId: Int): Result<List<News>> = try {
        Result.success(getCombinedNews().filter { it.gameId == gameId }.sortedByDescending { it.date })
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun getNewsByCategory(category: String): Result<List<News>> = try {
        Result.success(getCombinedNews().filter { it.category == category }.sortedByDescending { it.date })
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun getNewsByPlatform(platform: String): Result<List<News>> = try {
        Result.success(getCombinedNews().filter { it.platform == platform }.sortedByDescending { it.date })
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun getNewsByFavorites(favoriteGameIds: List<Int>): Result<List<News>> = try {
        Result.success(getCombinedNews().filter { it.gameId in favoriteGameIds }.sortedByDescending { it.date })
    } catch (e: Exception) { Result.failure(e) }

    // ──────────────────────────────────────────────────────────────
    // Internal helpers
    // ──────────────────────────────────────────────────────────────

    private suspend fun getCombinedNews(): List<News> {
        val now = System.currentTimeMillis()
        val cached = cachedNews
        if (cached != null && now - lastFetchMs < SESSION_CACHE_TTL_MS) return cached

        val liveNews = fetchSteamNews()
        // Merge: live news first, then static (deduplicate by title)
        val liveTitles = liveNews.map { it.title }.toSet()
        val combined = liveNews + staticNews.filter { it.title !in liveTitles }
        cachedNews = combined
        lastFetchMs = now
        return combined
    }

    /**
     * Fetch real developer news from the Steam News API for the top 20 rated games.
     * Requests are made in parallel; any individual failure is silently skipped.
     */
    private suspend fun fetchSteamNews(): List<News> = coroutineScope {
        var idCounter = 100_000
        topSteamGames.map { (gameId, appId) ->
            async {
                try {
                    steamNewsService.getNewsForApp(appId, count = 4)
                        ?.mapNotNull { item -> item.toNews(idCounter++, gameId) }
                        ?: emptyList()
                } catch (_: Exception) { emptyList() }
            }
        }.flatMap { deferred ->
            try { deferred.await() } catch (_: Exception) { emptyList() }
        }
    }

    private fun SteamNewsItem.toNews(id: Int, gameId: Int): News? {
        if (title.isBlank()) return null
        return News(
            id = id,
            title = title,
            content = contents.ifBlank { "Read the full announcement on Steam." },
            imageUrl = gameIdToImage[gameId] ?: "",
            date = dateFormatted,
            gameId = gameId,
            platform = "Steam",
            category = category
        )
    }
}
