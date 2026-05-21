package com.example.desaappsavaloskoortuzarvargas.data.api

import org.junit.Assert.*
import org.junit.Test

class CheapSharkServiceTest {

    private val service = CheapSharkService()

    @Test
    fun `getStoreName returns correct name for known store`() {
        assertEquals("Steam", service.getStoreName("1"))
        assertEquals("GOG", service.getStoreName("7"))
        assertEquals("Epic Games", service.getStoreName("25"))
        assertEquals("Humble Store", service.getStoreName("11"))
        assertEquals("Fanatical", service.getStoreName("15"))
        assertEquals("GreenManGaming", service.getStoreName("3"))
        assertEquals("Blizzard", service.getStoreName("31"))
    }

    @Test
    fun `getStoreName returns fallback for unknown store`() {
        assertEquals("Store #999", service.getStoreName("999"))
        assertEquals("Store #0", service.getStoreName("0"))
    }

    @Test
    fun `getStoreName covers all mapped stores`() {
        assertEquals("GamersGate", service.getStoreName("2"))
        assertEquals("Origin (EA)", service.getStoreName("8"))
        assertEquals("Uplay (Ubisoft)", service.getStoreName("13"))
        assertEquals("WinGameStore", service.getStoreName("21"))
        assertEquals("GameBillet", service.getStoreName("23"))
        assertEquals("Voidu", service.getStoreName("24"))
        assertEquals("Gamesplanet", service.getStoreName("27"))
        assertEquals("Gamesload", service.getStoreName("28"))
        assertEquals("2Game", service.getStoreName("29"))
        assertEquals("IndieGala", service.getStoreName("30"))
        assertEquals("DLGamer", service.getStoreName("33"))
        assertEquals("Noctre", service.getStoreName("34"))
        assertEquals("DreamGame", service.getStoreName("35"))
    }

    // --- Data class construction tests ---

    @Test
    fun `CheapSharkDeal with all fields`() {
        val deal = CheapSharkDeal(
            internalName = "TESTGAME", title = "Test Game", dealID = "abc",
            storeID = "1", gameID = "123", salePrice = "9.99",
            normalPrice = "19.99", savings = "50.0", metacriticScore = "85",
            steamRatingPercent = "90", releaseDate = 1609459200L, thumb = "http://img.jpg"
        )
        assertEquals("Test Game", deal.title)
        assertEquals("9.99", deal.salePrice)
        assertEquals("19.99", deal.normalPrice)
        assertEquals("50.0", deal.savings)
        assertEquals("1", deal.storeID)
        assertEquals("123", deal.gameID)
        assertEquals("abc", deal.dealID)
        assertEquals("TESTGAME", deal.internalName)
        assertEquals("85", deal.metacriticScore)
        assertEquals("90", deal.steamRatingPercent)
        assertEquals(1609459200L, deal.releaseDate)
        assertEquals("http://img.jpg", deal.thumb)
    }

    @Test
    fun `CheapSharkGameSearchResult with all fields`() {
        val result = CheapSharkGameSearchResult(
            gameID = "123", steamAppID = "456", cheapest = "4.99",
            cheapestDealID = "deal1", external = "Test",
            internalName = "test", thumb = "http://t.jpg"
        )
        assertEquals("123", result.gameID)
        assertEquals("456", result.steamAppID)
        assertEquals("4.99", result.cheapest)
        assertEquals("deal1", result.cheapestDealID)
        assertEquals("Test", result.external)
        assertEquals("test", result.internalName)
        assertEquals("http://t.jpg", result.thumb)
    }

    @Test
    fun `CheapSharkGameLookup with info and deals`() {
        val info = CheapSharkGameInfo(title = "Game", steamAppID = "789", thumb = "http://t.jpg")
        val deal = CheapSharkStoreDeal(storeID = "1", dealID = "d1", price = "9.99", retailPrice = "19.99", savings = "50.0")
        val lookup = CheapSharkGameLookup(info = info, deals = listOf(deal))
        assertNotNull(lookup.info)
        assertEquals("Game", lookup.info?.title)
        assertEquals("789", lookup.info?.steamAppID)
        assertEquals("http://t.jpg", lookup.info?.thumb)
        assertEquals(1, lookup.deals.size)
        assertEquals("9.99", lookup.deals[0].price)
        assertEquals("19.99", lookup.deals[0].retailPrice)
        assertEquals("50.0", lookup.deals[0].savings)
        assertEquals("1", lookup.deals[0].storeID)
        assertEquals("d1", lookup.deals[0].dealID)
    }


    @Test
    fun `GamePrice data class properties`() {
        val gp = GamePrice("Steam", 9.99f, 19.99f, 50f, "http://deal")
        assertEquals("Steam", gp.storeName)
        assertEquals(9.99f, gp.currentPrice, 0.01f)
        assertEquals(19.99f, gp.retailPrice, 0.01f)
        assertEquals(50f, gp.savings, 0.01f)
        assertEquals("http://deal", gp.dealUrl)
    }

    @Test
    fun `GamePrice equality and copy`() {
        val gp1 = GamePrice("Steam", 9.99f, 19.99f, 50f, "http://deal")
        val gp2 = gp1.copy()
        assertEquals(gp1, gp2)
        assertEquals(gp1.hashCode(), gp2.hashCode())
        val gp3 = gp1.copy(storeName = "GOG")
        assertNotEquals(gp1, gp3)
    }

    // --- Additional StoreRegionAvailability coverage ---

    @Test
    fun `Voidu regional availability`() {
        assertTrue(StoreRegionAvailability.isAvailableInRegion("Voidu", "US"))
        assertTrue(StoreRegionAvailability.isAvailableInRegion("Voidu", "ES"))
        assertFalse(StoreRegionAvailability.isAvailableInRegion("Voidu", "AR"))
    }

    @Test
    fun `Noctre regional availability`() {
        assertTrue(StoreRegionAvailability.isAvailableInRegion("Noctre", "US"))
        assertTrue(StoreRegionAvailability.isAvailableInRegion("Noctre", "ES"))
        assertFalse(StoreRegionAvailability.isAvailableInRegion("Noctre", "AR"))
    }

    @Test
    fun `Uplay regional availability`() {
        assertTrue(StoreRegionAvailability.isAvailableInRegion("Uplay (Ubisoft)", "US"))
        assertTrue(StoreRegionAvailability.isAvailableInRegion("Uplay (Ubisoft)", "AR"))
        assertFalse(StoreRegionAvailability.isAvailableInRegion("Uplay (Ubisoft)", "JP"))
    }

    @Test
    fun `Gamesload regional availability`() {
        assertTrue(StoreRegionAvailability.isAvailableInRegion("Gamesload", "US"))
        assertTrue(StoreRegionAvailability.isAvailableInRegion("Gamesload", "ES"))
        assertFalse(StoreRegionAvailability.isAvailableInRegion("Gamesload", "AR"))
    }

    @Test
    fun `WinGameStore US only`() {
        assertTrue(StoreRegionAvailability.isAvailableInRegion("WinGameStore", "US"))
        assertFalse(StoreRegionAvailability.isAvailableInRegion("WinGameStore", "ES"))
    }

    @Test
    fun `Origin regional availability`() {
        assertTrue(StoreRegionAvailability.isAvailableInRegion("Origin (EA)", "US"))
        assertTrue(StoreRegionAvailability.isAvailableInRegion("Origin (EA)", "AR"))
        assertTrue(StoreRegionAvailability.isAvailableInRegion("Origin (EA)", "CL"))
        assertTrue(StoreRegionAvailability.isAvailableInRegion("Origin (EA)", "CO"))
        assertTrue(StoreRegionAvailability.isAvailableInRegion("Origin (EA)", "UY"))
        assertTrue(StoreRegionAvailability.isAvailableInRegion("Origin (EA)", "PE"))
        assertTrue(StoreRegionAvailability.isAvailableInRegion("Origin (EA)", "PY"))
        assertFalse(StoreRegionAvailability.isAvailableInRegion("Origin (EA)", "JP"))
    }
}
