package com.example.desaappsavaloskoortuzarvargas.domain.model

import org.junit.Assert.*
import org.junit.Test

class DiscountModelExtendedTest {

    @Test
    fun `OfferType has 4 values`() {
        val values = OfferType.values()
        assertEquals(4, values.size)
    }

    @Test
    fun `OfferType SALE`() {
        assertEquals("SALE", OfferType.SALE.name)
    }

    @Test
    fun `OfferType PERMANENT_PRICE_DROP`() {
        assertEquals("PERMANENT_PRICE_DROP", OfferType.PERMANENT_PRICE_DROP.name)
    }

    @Test
    fun `OfferType TEMPORARILY_FREE`() {
        assertEquals("TEMPORARILY_FREE", OfferType.TEMPORARILY_FREE.name)
    }

    @Test
    fun `OfferType F2P`() {
        assertEquals("F2P", OfferType.F2P.name)
    }

    @Test
    fun `OfferType valueOf works`() {
        assertEquals(OfferType.SALE, OfferType.valueOf("SALE"))
        assertEquals(OfferType.F2P, OfferType.valueOf("F2P"))
    }

    @Test
    fun `DiscountedGame minimal construction`() {
        val d = DiscountedGame(
            gameId = 1, gameName = "Test", imageUrl = "",
            platform = "Steam", originalPrice = 60f, currentPrice = 30f,
            discountPercentage = 50
        )
        assertEquals(1, d.gameId)
        assertEquals("Test", d.gameName)
        assertEquals(50, d.discountPercentage)
        assertFalse(d.isFree)
        assertFalse(d.isF2P)
        assertFalse(d.isTemporarilyFree)
        assertNull(d.endDate)
        assertFalse(d.isHistoricalLowest)
        assertTrue(d.tags.isEmpty())
        assertEquals(OfferType.SALE, d.offerType)
        assertNull(d.endTimestamp)
        assertNull(d.previousBasePrice)
    }

    @Test
    fun `DiscountedGame isFree flag`() {
        val d = DiscountedGame(
            gameId = 1, gameName = "Free", imageUrl = "",
            platform = "Epic Games", originalPrice = 0f, currentPrice = 0f,
            discountPercentage = 0, isFree = true
        )
        assertTrue(d.isFree)
    }

    @Test
    fun `DiscountedGame with endDate`() {
        val d = DiscountedGame(
            gameId = 1, gameName = "Sale", imageUrl = "",
            platform = "Steam", originalPrice = 60f, currentPrice = 30f,
            discountPercentage = 50, endDate = "2026-06-15"
        )
        assertEquals("2026-06-15", d.endDate)
    }

    @Test
    fun `DiscountedGame with tags`() {
        val d = DiscountedGame(
            gameId = 1, gameName = "Tagged", imageUrl = "",
            platform = "Steam", originalPrice = 60f, currentPrice = 30f,
            discountPercentage = 50, tags = listOf("Action", "RPG")
        )
        assertEquals(2, d.tags.size)
        assertTrue(d.tags.contains("Action"))
    }

    @Test
    fun `DiscountedGame copy preserves unmodified fields`() {
        val original = DiscountedGame(
            gameId = 1, gameName = "Test", imageUrl = "url",
            platform = "Steam", originalPrice = 60f, currentPrice = 30f,
            discountPercentage = 50, tags = listOf("Action")
        )
        val copy = original.copy(currentPrice = 20f, discountPercentage = 67)
        assertEquals("Test", copy.gameName)
        assertEquals(20f, copy.currentPrice, 0.01f)
        assertEquals(67, copy.discountPercentage)
        assertEquals(1, copy.tags.size)
    }

    @Test
    fun `DiscountedGame endTimestamp with value`() {
        val ts = System.currentTimeMillis() + 86400000L
        val d = DiscountedGame(
            gameId = 1, gameName = "T", imageUrl = "",
            platform = "Steam", originalPrice = 60f, currentPrice = 30f,
            discountPercentage = 50, endTimestamp = ts
        )
        assertEquals(ts, d.endTimestamp)
    }

    @Test
    fun `DiscountedGame previousBasePrice for price drop`() {
        val d = DiscountedGame(
            gameId = 1, gameName = "T", imageUrl = "",
            platform = "Steam", originalPrice = 40f, currentPrice = 40f,
            discountPercentage = 0,
            offerType = OfferType.PERMANENT_PRICE_DROP,
            previousBasePrice = 60f
        )
        assertEquals(60f, d.previousBasePrice!!, 0.01f)
    }

    @Test
    fun `DiscountedGame equality`() {
        val a = DiscountedGame(
            gameId = 1, gameName = "T", imageUrl = "", platform = "S",
            originalPrice = 60f, currentPrice = 30f, discountPercentage = 50
        )
        val b = DiscountedGame(
            gameId = 1, gameName = "T", imageUrl = "", platform = "S",
            originalPrice = 60f, currentPrice = 30f, discountPercentage = 50
        )
        assertEquals(a, b)
    }

    @Test
    fun `DiscountedGame inequality on different prices`() {
        val a = DiscountedGame(
            gameId = 1, gameName = "T", imageUrl = "", platform = "S",
            originalPrice = 60f, currentPrice = 30f, discountPercentage = 50
        )
        val b = a.copy(currentPrice = 40f)
        assertNotEquals(a, b)
    }
}


