package com.example.desaappsavaloskoortuzarvargas.data.catalog

import org.junit.Assert.*
import org.junit.Test

/**
 * Extended tests for GameCatalog covering getSteamAppIdsByName,
 * news generation edge cases, and price history details.
 */
class GameCatalogExtendedTest {

    @Test
    fun `getSteamAppIdsByName returns non-empty map`() {
        val map = GameCatalog.getSteamAppIdsByName()
        assertTrue(map.isNotEmpty())
    }

    @Test
    fun `getSteamAppIdsByName contains Elden Ring`() {
        val map = GameCatalog.getSteamAppIdsByName()
        assertTrue(map.containsKey("Elden Ring"))
        assertEquals(1245620, map["Elden Ring"])
    }

    @Test
    fun `getSteamAppIdsByName keys match game names`() {
        val map = GameCatalog.getSteamAppIdsByName()
        val games = GameCatalog.generateGames()
        val gameNames = games.map { it.name }.toSet()
        map.keys.forEach { name ->
            assertTrue("$name not in game catalog", name in gameNames)
        }
    }

    @Test
    fun `getSteamAppIdsByName values are positive`() {
        val map = GameCatalog.getSteamAppIdsByName()
        map.values.forEach { appId ->
            assertTrue("Steam App ID should be positive: $appId", appId > 0)
        }
    }

    @Test
    fun `getSteamAppIdsByName excludes non-Steam games`() {
        val map = GameCatalog.getSteamAppIdsByName()
        // Alan Wake 2 is Epic exclusive and shouldn't have a Steam App ID
        assertFalse(map.containsKey("Alan Wake 2"))
    }

    @Test
    fun `generateNews returns exactly 60 items`() {
        val news = GameCatalog.generateNews()
        assertEquals(60, news.size)
    }

    @Test
    fun `generateNews all have non-empty titles`() {
        val news = GameCatalog.generateNews()
        news.forEach { assertTrue("News ${it.id} has empty title", it.title.isNotEmpty()) }
    }

    @Test
    fun `generateNews all have non-empty content`() {
        val news = GameCatalog.generateNews()
        news.forEach { assertTrue("News ${it.id} has empty content", it.content.isNotEmpty()) }
    }

    @Test
    fun `generateNews all have valid dates`() {
        val news = GameCatalog.generateNews()
        val dateRegex = Regex("\\d{4}-\\d{2}-\\d{2}")
        news.forEach { assertTrue("Invalid date: ${it.date}", it.date.matches(dateRegex)) }
    }

    @Test
    fun `generateNews all have non-null gameId`() {
        val news = GameCatalog.generateNews()
        news.forEach { assertNotNull("News ${it.id} has null gameId", it.gameId) }
    }

    @Test
    fun `generateNews categories are valid`() {
        val validCategories = setOf("discount", "update", "event")
        val news = GameCatalog.generateNews()
        news.forEach { assertTrue("Invalid category: ${it.category}", it.category in validCategories) }
    }

    @Test
    fun `generateNews has all three categories`() {
        val news = GameCatalog.generateNews()
        val categories = news.map { it.category }.toSet()
        assertTrue(categories.contains("discount"))
        assertTrue(categories.contains("update"))
        assertTrue(categories.contains("event"))
    }

    @Test
    fun `generatePriceHistory covers 20 games`() {
        val history = GameCatalog.generatePriceHistory()
        val gameIds = history.map { it.gameId }.toSet()
        assertEquals(20, gameIds.size)
    }

    @Test
    fun `generatePriceHistory has 12 months per game per platform`() {
        val history = GameCatalog.generatePriceHistory()
        val firstGameId = history.first().gameId
        val firstGameHistory = history.filter { it.gameId == firstGameId }
        // Should have entries for each platform across 12 months
        assertTrue(firstGameHistory.size >= 12)
    }

    @Test
    fun `generatePriceHistory has some historical lowest`() {
        val history = GameCatalog.generatePriceHistory()
        assertTrue(history.any { it.isHistoricalLowest })
    }

    @Test
    fun `generatePriceHistory discounts are between 0 and 100`() {
        val history = GameCatalog.generatePriceHistory()
        history.forEach {
            assertTrue("Discount ${it.discount} out of range", it.discount in 0..100)
        }
    }

    @Test
    fun `generatePriceHistory prices are non-negative`() {
        val history = GameCatalog.generatePriceHistory()
        history.forEach {
            assertTrue("Price ${it.price} is negative", it.price >= 0f)
        }
    }

    @Test
    fun `paid games have Steam App IDs matching`() {
        val games = GameCatalog.generateGames()
        val steamMap = GameCatalog.getSteamAppIdsByName()
        games.filter { it.steamAppId > 0 }.forEach { game ->
            assertTrue(
                "${game.name} steamAppId should match getSteamAppIdsByName",
                steamMap[game.name] == game.steamAppId
            )
        }
    }

    @Test
    fun `all games have ratings between 0 and 10`() {
        val games = GameCatalog.generateGames()
        games.forEach { game ->
            assertTrue("${game.name} rating ${game.rating} out of range",
                game.rating in 0.0..10.0)
        }
    }

    @Test
    fun `DLC prices are lower than base game prices`() {
        val games = GameCatalog.generateGames()
        games.filter { it.dlcs.isNotEmpty() }.forEach { game ->
            game.dlcs.forEach { dlc ->
                dlc.currentPrices.forEach { (platform, dlcPrice) ->
                    val basePrice = game.currentPrices[platform]
                    if (basePrice != null) {
                        assertTrue(
                            "DLC ${dlc.name} price $dlcPrice >= base price $basePrice on $platform",
                            dlcPrice <= basePrice
                        )
                    }
                }
            }
        }
    }
}

