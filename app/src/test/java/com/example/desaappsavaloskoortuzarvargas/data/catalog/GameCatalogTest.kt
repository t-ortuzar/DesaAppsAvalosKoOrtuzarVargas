package com.example.desaappsavaloskoortuzarvargas.data.catalog

import org.junit.Assert.*
import org.junit.Test

class GameCatalogTest {

    @Test
    fun `generateGames returns 114 games (100 paid + 14 F2P)`() {
        val games = GameCatalog.generateGames()
        assertEquals(114, games.size)
    }

    @Test
    fun `games have unique ids`() {
        val games = GameCatalog.generateGames()
        val ids = games.map { it.id }.toSet()
        assertEquals(games.size, ids.size)
    }

    @Test
    fun `paid games have non-empty fields`() {
        val games = GameCatalog.generateGames().filter { !it.tags.contains("Free2Play") }
        assertTrue(games.size >= 100)
        games.forEach {
            assertTrue("${it.name} has empty name", it.name.isNotEmpty())
            assertTrue("${it.name} has empty description", it.description.isNotEmpty())
            assertTrue("${it.name} has empty releaseDate", it.releaseDate.isNotEmpty())
            assertTrue("${it.name} has no rating", it.rating > 0)
            assertTrue("${it.name} has no prices", it.currentPrices.isNotEmpty())
            assertTrue("${it.name} has no tags", it.tags.isNotEmpty())
            assertTrue("${it.name} has no platforms", it.availablePlatforms.isNotEmpty())
        }
    }

    @Test
    fun `F2P games have correct defaults`() {
        val f2pGames = GameCatalog.generateGames().filter { it.tags.contains("Free2Play") }
        assertTrue("Should have at least 14 F2P games", f2pGames.size >= 14)
        f2pGames.forEach {
            assertTrue("${it.name} should have Free2Play tag", it.tags.contains("Free2Play"))
            assertTrue("${it.name} should have empty prices", it.currentPrices.isEmpty())
            assertTrue("${it.name} should have a release date", it.releaseDate.isNotEmpty())
            assertTrue("${it.name} should have available platforms", it.availablePlatforms.isNotEmpty())
        }
    }

    @Test
    fun `some games have DLCs`() {
        val games = GameCatalog.generateGames()
        assertTrue(games.any { it.dlcs.isNotEmpty() })
    }

    @Test
    fun `DLCs have correct gameId references`() {
        val games = GameCatalog.generateGames()
        games.forEach { game ->
            game.dlcs.forEach { dlc ->
                assertEquals(game.id, dlc.gameId)
            }
        }
    }


    @Test
    fun `generateNews returns 60 news items`() {
        val news = GameCatalog.generateNews()
        assertEquals(60, news.size)
    }

    @Test
    fun `news have unique ids`() {
        val news = GameCatalog.generateNews()
        val ids = news.map { it.id }.toSet()
        assertEquals(news.size, ids.size)
    }

    @Test
    fun `news have valid categories`() {
        val validCategories = setOf("discount", "update", "event")
        val news = GameCatalog.generateNews()
        news.forEach { assertTrue(it.category in validCategories) }
    }

    @Test
    fun `generatePriceHistory returns data for 20 games`() {
        val history = GameCatalog.generatePriceHistory()
        assertTrue(history.isNotEmpty())
        val gameIds = history.map { it.gameId }.toSet()
        assertEquals(20, gameIds.size)
    }

    @Test
    fun `price history entries have valid data`() {
        val history = GameCatalog.generatePriceHistory()
        history.forEach {
            assertTrue(it.price >= 0)
            assertTrue(it.discount in 0..100)
            assertTrue(it.platform.isNotEmpty())
            assertTrue(it.date.isNotEmpty())
        }
    }

    @Test
    fun `Alan Wake 2 is Epic exclusive with no Steam image`() {
        val games = GameCatalog.generateGames()
        val game9 = games.first { it.id == 9 } // Alan Wake 2
        assertEquals("Alan Wake 2", game9.name)
        // Alan Wake 2 is NOT on Steam — image should be empty (fetched at runtime from Epic)
        assertTrue("Alan Wake 2 should have empty imageUrl for runtime Epic fetch",
            game9.imageUrl.isEmpty())
        assertEquals("Alan Wake 2 should only be on Epic Games",
            listOf("Epic Games"), game9.availablePlatforms)
        assertEquals("Alan Wake 2 should have no Steam App ID",
            0, game9.steamAppId)
    }

    @Test
    fun `Steam games have Steam CDN image URLs`() {
        val games = GameCatalog.generateGames()
        val eldenRing = games.first { it.id == 1 } // Elden Ring - on Steam
        assertTrue(eldenRing.imageUrl.contains("steamstatic.com"))
        assertTrue(eldenRing.imageUrl.contains("1245620")) // Elden Ring Steam App ID
    }

    @Test
    fun `games only have prices for their available platforms`() {
        val games = GameCatalog.generateGames()
        games.forEach { game ->
            game.currentPrices.keys.forEach { key ->
                assertTrue("$key not in ${game.availablePlatforms}", key in game.availablePlatforms)
            }
        }
    }

    @Test
    fun `F2P catalog games include well-known titles`() {
        val games = GameCatalog.generateGames()
        val f2pNames = games.filter { it.tags.contains("Free2Play") }.map { it.name }
        assertTrue(f2pNames.contains("League of Legends"))
        assertTrue(f2pNames.contains("Valorant"))
        assertTrue(f2pNames.contains("Fortnite"))
        assertTrue(f2pNames.contains("Counter-Strike 2"))
        assertTrue(f2pNames.contains("Dota 2"))
    }

    @Test
    fun `Valorant is Riot Games exclusive and NOT on Steam`() {
        val games = GameCatalog.generateGames()
        val valorant = games.first { it.name == "Valorant" }
        assertEquals(listOf("Riot Games"), valorant.availablePlatforms)
        assertFalse("Valorant should NOT be on Steam",
            valorant.availablePlatforms.contains("Steam"))
        assertEquals(0, valorant.steamAppId)
    }

    @Test
    fun `Genshin Impact and Honkai Star Rail are NOT on Steam`() {
        val games = GameCatalog.generateGames()
        val genshin = games.first { it.name == "Genshin Impact" }
        val honkai = games.first { it.name == "Honkai: Star Rail" }
        assertFalse(genshin.availablePlatforms.contains("Steam"))
        assertFalse(honkai.availablePlatforms.contains("Steam"))
        assertTrue(genshin.availablePlatforms.contains("Epic Games"))
        assertTrue(honkai.availablePlatforms.contains("Epic Games"))
    }

    @Test
    fun `DiscountedGame model supports all offer types`() {
        val sale = com.example.desaappsavaloskoortuzarvargas.domain.model.DiscountedGame(
            gameId = 1, gameName = "Test", imageUrl = "", platform = "Steam",
            originalPrice = 59.99f, currentPrice = 29.99f, discountPercentage = 50,
            offerType = com.example.desaappsavaloskoortuzarvargas.domain.model.OfferType.SALE
        )
        val priceDrop = sale.copy(
            offerType = com.example.desaappsavaloskoortuzarvargas.domain.model.OfferType.PERMANENT_PRICE_DROP,
            previousBasePrice = 59.99f
        )
        val tempFree = sale.copy(
            currentPrice = 0f, discountPercentage = 100, isTemporarilyFree = true,
            offerType = com.example.desaappsavaloskoortuzarvargas.domain.model.OfferType.TEMPORARILY_FREE,
            endTimestamp = System.currentTimeMillis() + 86400000
        )
        val f2p = sale.copy(
            originalPrice = 0f, currentPrice = 0f, discountPercentage = 100, isF2P = true,
            offerType = com.example.desaappsavaloskoortuzarvargas.domain.model.OfferType.F2P
        )
        assertEquals(com.example.desaappsavaloskoortuzarvargas.domain.model.OfferType.SALE, sale.offerType)
        assertEquals(com.example.desaappsavaloskoortuzarvargas.domain.model.OfferType.PERMANENT_PRICE_DROP, priceDrop.offerType)
        assertEquals(59.99f, priceDrop.previousBasePrice!!, 0.01f)
        assertEquals(com.example.desaappsavaloskoortuzarvargas.domain.model.OfferType.TEMPORARILY_FREE, tempFree.offerType)
        assertNotNull(tempFree.endTimestamp)
        assertEquals(com.example.desaappsavaloskoortuzarvargas.domain.model.OfferType.F2P, f2p.offerType)
    }
}
