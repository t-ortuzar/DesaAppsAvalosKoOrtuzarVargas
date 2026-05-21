package com.example.desaappsavaloskoortuzarvargas.data.mock

import org.junit.Assert.*
import org.junit.Test

class MockDataGeneratorTest {

    @Test
    fun `generateGames returns 100 games`() {
        val games = MockDataGenerator.generateGames()
        assertEquals(100, games.size)
    }

    @Test
    fun `games have unique ids`() {
        val games = MockDataGenerator.generateGames()
        val ids = games.map { it.id }.toSet()
        assertEquals(games.size, ids.size)
    }

    @Test
    fun `games have non-empty fields`() {
        val games = MockDataGenerator.generateGames()
        games.forEach {
            assertTrue(it.name.isNotEmpty())
            assertTrue(it.description.isNotEmpty())
            assertTrue(it.releaseDate.isNotEmpty())
            assertTrue(it.imageUrl.isNotEmpty())
            assertTrue(it.rating > 0)
            assertTrue(it.currentPrices.isNotEmpty())
            assertTrue(it.tags.isNotEmpty())
            assertTrue(it.availablePlatforms.isNotEmpty())
        }
    }

    @Test
    fun `some games have DLCs`() {
        val games = MockDataGenerator.generateGames()
        assertTrue(games.any { it.dlcs.isNotEmpty() })
    }

    @Test
    fun `DLCs have correct gameId references`() {
        val games = MockDataGenerator.generateGames()
        games.forEach { game ->
            game.dlcs.forEach { dlc ->
                assertEquals(game.id, dlc.gameId)
            }
        }
    }

    @Test
    fun `generateDiscounts returns discounts with all types`() {
        val discounts = MockDataGenerator.generateDiscounts()
        assertTrue(discounts.isNotEmpty())
        assertTrue(discounts.any { !it.isFree }) // paid discounts
        assertTrue(discounts.any { it.isF2P }) // F2P
        assertTrue(discounts.any { it.isTemporarilyFree }) // temp free
    }

    @Test
    fun `paid discounts have valid prices`() {
        val discounts = MockDataGenerator.generateDiscounts().filter { !it.isFree }
        discounts.forEach {
            assertTrue(it.originalPrice > 0)
            assertTrue(it.currentPrice >= 0)
            assertTrue(it.discountPercentage in 1..99)
        }
    }

    @Test
    fun `F2P games have zero prices`() {
        val f2p = MockDataGenerator.generateDiscounts().filter { it.isF2P }
        f2p.forEach {
            assertEquals(0f, it.originalPrice, 0.01f)
            assertEquals(0f, it.currentPrice, 0.01f)
        }
    }

    @Test
    fun `temp free games have original price but zero current`() {
        val tempFree = MockDataGenerator.generateDiscounts().filter { it.isTemporarilyFree }
        tempFree.forEach {
            assertTrue(it.originalPrice > 0)
            assertEquals(0f, it.currentPrice, 0.01f)
            assertNotNull(it.endDate)
        }
    }

    @Test
    fun `generateNews returns 60 news items`() {
        val news = MockDataGenerator.generateNews()
        assertEquals(60, news.size)
    }

    @Test
    fun `news have unique ids`() {
        val news = MockDataGenerator.generateNews()
        val ids = news.map { it.id }.toSet()
        assertEquals(news.size, ids.size)
    }

    @Test
    fun `news have valid categories`() {
        val validCategories = setOf("discount", "update", "event")
        val news = MockDataGenerator.generateNews()
        news.forEach { assertTrue(it.category in validCategories) }
    }

    @Test
    fun `generatePriceHistory returns data for 20 games`() {
        val history = MockDataGenerator.generatePriceHistory()
        assertTrue(history.isNotEmpty())
        val gameIds = history.map { it.gameId }.toSet()
        assertEquals(20, gameIds.size)
    }

    @Test
    fun `price history entries have valid data`() {
        val history = MockDataGenerator.generatePriceHistory()
        history.forEach {
            assertTrue(it.price >= 0)
            assertTrue(it.discount in 0..100)
            assertTrue(it.platform.isNotEmpty())
            assertTrue(it.date.isNotEmpty())
        }
    }

    @Test
    fun `alternative image used for game 9`() {
        val games = MockDataGenerator.generateGames()
        val game9 = games.first { it.id == 9 }
        assertTrue(game9.imageUrl.contains("gaming-cdn.com"))
    }

    @Test
    fun `games only have prices for their available platforms`() {
        val games = MockDataGenerator.generateGames()
        games.forEach { game ->
            game.currentPrices.keys.forEach { key ->
                assertTrue("$key not in ${game.availablePlatforms}", key in game.availablePlatforms)
            }
        }
    }
}

