package com.example.desaappsavaloskoortuzarvargas.domain.model

import org.junit.Assert.*
import org.junit.Test

class GameModelTest {

    @Test
    fun `Game has correct defaults`() {
        val game = Game(id = 1, name = "Test", description = "Desc", releaseDate = "2024-01-01",
            imageUrl = "http://img.com/1.jpg", rating = 8.5, currentPrices = mapOf("Steam" to 29.99f))
        assertFalse(game.isFavorite)
        assertEquals(0, game.historicalDiscount)
        assertTrue(game.tags.isEmpty())
        assertTrue(game.dlcs.isEmpty())
        assertTrue(game.availablePlatforms.isEmpty())
    }

    @Test
    fun `Game with all fields`() {
        val dlc = DLC(id = 10, name = "DLC1", gameId = 1, imageUrl = "url",
            currentPrices = mapOf("Steam" to 9.99f), historicalDiscount = 20,
            releaseDate = "2024-06-01", description = "DLC desc")
        val game = Game(id = 1, name = "Test", description = "Desc", releaseDate = "2024",
            imageUrl = "url", rating = 9.0, currentPrices = mapOf("Steam" to 59.99f),
            isFavorite = true, historicalDiscount = 75, tags = listOf("Action", "RPG"),
            dlcs = listOf(dlc), availablePlatforms = listOf("PC", "PS5"))
        assertTrue(game.isFavorite)
        assertEquals(75, game.historicalDiscount)
        assertEquals(2, game.tags.size)
        assertEquals(1, game.dlcs.size)
    }

    @Test
    fun `DLC has correct defaults`() {
        val dlc = DLC(id = 1, name = "DLC", gameId = 1, imageUrl = "url", currentPrices = emptyMap())
        assertEquals(0, dlc.historicalDiscount)
        assertEquals("", dlc.releaseDate)
        assertEquals("", dlc.description)
    }

    @Test
    fun `PriceHistory data class`() {
        val h = PriceHistory(gameId = 1, platform = "Steam", price = 19.99f, discount = 50, date = "2024-01-01")
        assertEquals(1, h.gameId)
        assertFalse(h.isHistoricalLowest)
    }

    @Test
    fun `PriceHistory with historical lowest`() {
        val h = PriceHistory(gameId = 1, platform = "GOG", price = 4.99f, discount = 90, date = "2024", isHistoricalLowest = true)
        assertTrue(h.isHistoricalLowest)
    }
}

