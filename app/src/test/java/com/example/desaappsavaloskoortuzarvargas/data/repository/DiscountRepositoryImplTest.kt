package com.example.desaappsavaloskoortuzarvargas.data.repository

import com.example.desaappsavaloskoortuzarvargas.domain.model.DiscountedGame
import com.example.desaappsavaloskoortuzarvargas.domain.model.OfferType
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for the DiscountedGame model and OfferType used by the real offers system.
 *
 * Note: DiscountRepositoryImpl now requires Room DAOs and PriceRefreshManager,
 * so integration tests are done at the instrumented test level.
 * These unit tests validate the domain model behavior.
 */
class DiscountRepositoryImplTest {

    private val sampleDiscount = DiscountedGame(
        gameId = 1, gameName = "Test Game", imageUrl = "url",
        platform = "Steam", originalPrice = 59.99f, currentPrice = 29.99f,
        discountPercentage = 50
    )

    @Test
    fun `DiscountedGame defaults to SALE offer type`() {
        assertEquals(OfferType.SALE, sampleDiscount.offerType)
    }

    @Test
    fun `DiscountedGame PERMANENT_PRICE_DROP has previousBasePrice`() {
        val priceDrop = sampleDiscount.copy(
            offerType = OfferType.PERMANENT_PRICE_DROP,
            previousBasePrice = 59.99f,
            currentPrice = 39.99f,
            discountPercentage = 0
        )
        assertEquals(OfferType.PERMANENT_PRICE_DROP, priceDrop.offerType)
        assertEquals(59.99f, priceDrop.previousBasePrice!!, 0.01f)
        assertFalse(priceDrop.isFree)
    }

    @Test
    fun `DiscountedGame TEMPORARILY_FREE`() {
        val tempFree = sampleDiscount.copy(
            currentPrice = 0f, discountPercentage = 100,
            isFree = true, isTemporarilyFree = true,
            offerType = OfferType.TEMPORARILY_FREE,
            endTimestamp = System.currentTimeMillis() + 86400000
        )
        assertTrue(tempFree.isFree)
        assertTrue(tempFree.isTemporarilyFree)
        assertNotNull(tempFree.endTimestamp)
        assertEquals(OfferType.TEMPORARILY_FREE, tempFree.offerType)
    }

    @Test
    fun `DiscountedGame F2P`() {
        val f2p = DiscountedGame(
            gameId = 2, gameName = "F2P Game", imageUrl = "url",
            platform = "Riot Games", originalPrice = 0f, currentPrice = 0f,
            discountPercentage = 100, isFree = true, isF2P = true,
            offerType = OfferType.F2P
        )
        assertTrue(f2p.isF2P)
        assertTrue(f2p.isFree)
        assertEquals(OfferType.F2P, f2p.offerType)
    }

    @Test
    fun `DiscountedGame endTimestamp defaults to null`() {
        assertNull(sampleDiscount.endTimestamp)
    }

    @Test
    fun `DiscountedGame historical low flag`() {
        val histLow = sampleDiscount.copy(isHistoricalLowest = true)
        assertTrue(histLow.isHistoricalLowest)
        assertFalse(sampleDiscount.isHistoricalLowest)
    }

    @Test
    fun `sorting discounts by percentage`() {
        val discounts = listOf(
            sampleDiscount.copy(discountPercentage = 30),
            sampleDiscount.copy(discountPercentage = 75),
            sampleDiscount.copy(discountPercentage = 50)
        ).sortedByDescending { it.discountPercentage }
        assertEquals(75, discounts[0].discountPercentage)
        assertEquals(50, discounts[1].discountPercentage)
        assertEquals(30, discounts[2].discountPercentage)
    }

    @Test
    fun `filtering discounts by platform`() {
        val discounts = listOf(
            sampleDiscount.copy(platform = "Steam"),
            sampleDiscount.copy(platform = "Epic Games"),
            sampleDiscount.copy(platform = "Steam")
        )
        val steamOnly = discounts.filter { it.platform == "Steam" }
        assertEquals(2, steamOnly.size)
        val epicOnly = discounts.filter { it.platform == "Epic Games" }
        assertEquals(1, epicOnly.size)
    }

    @Test
    fun `filtering non-existent platform returns empty`() {
        val discounts = listOf(sampleDiscount)
        val filtered = discounts.filter { it.platform == "NonExistent" }
        assertTrue(filtered.isEmpty())
    }
}
