package com.example.desaappsavaloskoortuzarvargas.data.api

import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class CheapSharkServiceTest {

    private val service = CheapSharkService()
    private val json = Json { ignoreUnknownKeys = true }

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

    // --- Data class serialization tests ---

    @Test
    fun `CheapSharkDeal deserializes from JSON`() {
        val jsonStr = """{"internalName":"TESTGAME","title":"Test Game","dealID":"abc","storeID":"1","gameID":"123","salePrice":"9.99","normalPrice":"19.99","savings":"50.0","metacriticScore":"85","steamRatingPercent":"90","releaseDate":1609459200,"thumb":"http://img.jpg"}"""
        val deal = json.decodeFromString<CheapSharkDeal>(jsonStr)
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
    fun `CheapSharkDeal default values`() {
        val deal = CheapSharkDeal()
        assertEquals("", deal.title)
        assertEquals("", deal.internalName)
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
    fun `CheapSharkGameSearchResult deserializes from JSON`() {
        val jsonStr = """{"gameID":"123","steamAppID":"456","cheapest":"4.99","cheapestDealID":"deal1","external":"Test","internalName":"test","thumb":"http://t.jpg"}"""
        val result = json.decodeFromString<CheapSharkGameSearchResult>(jsonStr)
        assertEquals("123", result.gameID)
        assertEquals("456", result.steamAppID)
        assertEquals("4.99", result.cheapest)
        assertEquals("deal1", result.cheapestDealID)
        assertEquals("Test", result.external)
        assertEquals("test", result.internalName)
        assertEquals("http://t.jpg", result.thumb)
    }

    @Test
    fun `CheapSharkGameSearchResult default values`() {
        val result = CheapSharkGameSearchResult()
        assertEquals("", result.gameID)
        assertNull(result.steamAppID)
        assertEquals("0", result.cheapest)
        assertNull(result.cheapestDealID)
        assertEquals("", result.external)
        assertEquals("", result.internalName)
        assertEquals("", result.thumb)
    }

    @Test
    fun `CheapSharkGameLookup deserializes with info and deals`() {
        val jsonStr = """{"info":{"title":"Game","steamAppID":"789","thumb":"http://t.jpg"},"deals":[{"storeID":"1","dealID":"d1","price":"9.99","retailPrice":"19.99","savings":"50.0"}]}"""
        val lookup = json.decodeFromString<CheapSharkGameLookup>(jsonStr)
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
    fun `CheapSharkGameLookup default values`() {
        val lookup = CheapSharkGameLookup()
        assertNull(lookup.info)
        assertTrue(lookup.deals.isEmpty())
    }

    @Test
    fun `CheapSharkGameInfo default values`() {
        val info = CheapSharkGameInfo()
        assertEquals("", info.title)
        assertNull(info.steamAppID)
        assertEquals("", info.thumb)
    }

    @Test
    fun `CheapSharkStoreDeal default values`() {
        val deal = CheapSharkStoreDeal()
        assertEquals("", deal.storeID)
        assertEquals("", deal.dealID)
        assertEquals("0", deal.price)
        assertEquals("0", deal.retailPrice)
        assertEquals("0", deal.savings)
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
