package com.example.desaappsavaloskoortuzarvargas.domain.model

import org.junit.Assert.*
import org.junit.Test

class DiscountModelTest {

    @Test
    fun `DiscountedGame has correct defaults`() {
        val d = DiscountedGame(gameId = 1, gameName = "Test", imageUrl = "url",
            platform = "Steam", originalPrice = 59.99f, currentPrice = 29.99f, discountPercentage = 50)
        assertFalse(d.isFree)
        assertFalse(d.isF2P)
        assertFalse(d.isTemporarilyFree)
        assertNull(d.endDate)
        assertFalse(d.isHistoricalLowest)
        assertTrue(d.tags.isEmpty())
    }

    @Test
    fun `DiscountedGame with all fields`() {
        val d = DiscountedGame(gameId = 1, gameName = "Free Game", imageUrl = "url",
            platform = "Epic", originalPrice = 0f, currentPrice = 0f, discountPercentage = 100,
            isFree = true, isF2P = false, isTemporarilyFree = true,
            endDate = "2024-12-31", isHistoricalLowest = true, tags = listOf("Action"))
        assertTrue(d.isFree)
        assertTrue(d.isTemporarilyFree)
        assertTrue(d.isHistoricalLowest)
        assertEquals("2024-12-31", d.endDate)
        assertEquals(1, d.tags.size)
    }

    @Test
    fun `DiscountedGame F2P flag`() {
        val d = DiscountedGame(gameId = 1, gameName = "F2P", imageUrl = "url",
            platform = "Steam", originalPrice = 0f, currentPrice = 0f,
            discountPercentage = 0, isF2P = true)
        assertTrue(d.isF2P)
    }
}

