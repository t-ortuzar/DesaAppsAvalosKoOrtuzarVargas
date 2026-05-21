package com.example.desaappsavaloskoortuzarvargas.data.api

import org.junit.Assert.*
import org.junit.Test

class CheapSharkDataClassesTest {

    @Test
    fun `CheapSharkDeal has correct defaults`() {
        val deal = CheapSharkDeal()
        assertEquals("", deal.internalName)
        assertEquals("", deal.title)
        assertEquals("", deal.dealID)
        assertEquals("", deal.storeID)
        assertEquals("", deal.gameID)
        assertEquals("0", deal.salePrice)
        assertEquals("0", deal.normalPrice)
        assertEquals("0", deal.savings)
        assertEquals("0", deal.metacriticScore)
        assertEquals("0", deal.steamRatingPercent)
        assertEquals(0L, deal.releaseDate)
        assertEquals("", deal.thumb)
    }

    @Test
    fun `CheapSharkDeal with values`() {
        val deal = CheapSharkDeal(
            internalName = "test", title = "Test Game", dealID = "abc",
            storeID = "1", gameID = "123", salePrice = "9.99",
            normalPrice = "29.99", savings = "66.6", metacriticScore = "85",
            steamRatingPercent = "90", releaseDate = 1234567890L, thumb = "http://thumb.jpg"
        )
        assertEquals("Test Game", deal.title)
        assertEquals("9.99", deal.salePrice)
    }

    @Test
    fun `CheapSharkGameLookup defaults`() {
        val lookup = CheapSharkGameLookup()
        assertNull(lookup.info)
        assertTrue(lookup.deals.isEmpty())
    }

    @Test
    fun `CheapSharkGameInfo defaults`() {
        val info = CheapSharkGameInfo()
        assertEquals("", info.title)
        assertNull(info.steamAppID)
        assertEquals("", info.thumb)
    }

    @Test
    fun `CheapSharkStoreDeal defaults`() {
        val deal = CheapSharkStoreDeal()
        assertEquals("", deal.storeID)
        assertEquals("", deal.dealID)
        assertEquals("0", deal.price)
        assertEquals("0", deal.retailPrice)
        assertEquals("0", deal.savings)
    }

    @Test
    fun `CheapSharkGameSearchResult defaults`() {
        val result = CheapSharkGameSearchResult()
        assertEquals("", result.gameID)
        assertNull(result.steamAppID)
        assertEquals("0", result.cheapest)
        assertNull(result.cheapestDealID)
        assertEquals("", result.external)
    }

    @Test
    fun `GamePrice data class`() {
        val price = GamePrice(
            storeName = "Steam", currentPrice = 9.99f,
            retailPrice = 29.99f, savings = 66.7f,
            dealUrl = "http://deal.com"
        )
        assertEquals("Steam", price.storeName)
        assertEquals(9.99f, price.currentPrice, 0.01f)
        assertEquals(29.99f, price.retailPrice, 0.01f)
    }
}

