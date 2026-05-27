package com.example.desaappsavaloskoortuzarvargas.data.api

import org.junit.Assert.*
import org.junit.Test

class SteamDataClassesTest {

    // ── SteamPriceOverview ──

    @Test
    fun `SteamPriceOverview defaults`() {
        val overview = SteamPriceOverview()
        assertEquals("USD", overview.currency)
        assertEquals(0, overview.initial)
        assertEquals(0, overview.final)
        assertEquals(0, overview.discount_percent)
        assertEquals("", overview.initial_formatted)
        assertEquals("", overview.final_formatted)
    }

    @Test
    fun `SteamPriceOverview with ARS values`() {
        val overview = SteamPriceOverview(
            currency = "ARS",
            initial = 1099900,
            final = 879900,
            discount_percent = 20,
            initial_formatted = "ARS$ 10.999,00",
            final_formatted = "ARS$ 8.799,00"
        )
        assertEquals("ARS", overview.currency)
        assertEquals(1099900, overview.initial)
        assertEquals(879900, overview.final)
        assertEquals(20, overview.discount_percent)
    }

    // ── SteamAppData ──

    @Test
    fun `SteamAppData defaults`() {
        val data = SteamAppData()
        assertEquals("", data.name)
        assertEquals(0, data.steam_appid)
        assertFalse(data.is_free)
        assertEquals("", data.header_image)
        assertNull(data.price_overview)
    }

    @Test
    fun `SteamAppData with values`() {
        val overview = SteamPriceOverview(currency = "ARS", final = 879900)
        val data = SteamAppData(
            name = "Elden Ring",
            steam_appid = 1245620,
            is_free = false,
            header_image = "https://cdn.steam.com/header.jpg",
            price_overview = overview
        )
        assertEquals("Elden Ring", data.name)
        assertEquals(1245620, data.steam_appid)
        assertNotNull(data.price_overview)
        assertEquals(879900, data.price_overview!!.final)
    }

    @Test
    fun `SteamAppData free game has no price overview`() {
        val data = SteamAppData(
            name = "Counter-Strike 2",
            steam_appid = 730,
            is_free = true
        )
        assertTrue(data.is_free)
        assertNull(data.price_overview)
    }

    // ── SteamAppResult ──

    @Test
    fun `SteamAppResult defaults`() {
        val result = SteamAppResult()
        assertFalse(result.success)
        assertNull(result.data)
    }

    @Test
    fun `SteamAppResult success with data`() {
        val appData = SteamAppData(name = "Test", steam_appid = 123)
        val result = SteamAppResult(success = true, data = appData)
        assertTrue(result.success)
        assertNotNull(result.data)
        assertEquals("Test", result.data!!.name)
    }

    @Test
    fun `SteamAppResult failure`() {
        val result = SteamAppResult(success = false, data = null)
        assertFalse(result.success)
        assertNull(result.data)
    }

    // ── SteamGamePrice ──

    @Test
    fun `SteamGamePrice price calculation from cents`() {
        val price = SteamGamePrice(
            appId = 1245620, name = "Elden Ring",
            priceCents = 879900, retailPriceCents = 1099900,
            discountPercent = 20, isFree = false,
            headerImageUrl = "https://img.com", currency = "ARS"
        )
        assertEquals(8799f, price.price, 0.01f)
        assertEquals(10999f, price.retailPrice, 0.01f)
        assertTrue(price.isArs)
    }

    @Test
    fun `SteamGamePrice USD currency`() {
        val price = SteamGamePrice(
            appId = 100, name = "Test",
            priceCents = 5999, retailPriceCents = 5999,
            discountPercent = 0, isFree = false,
            headerImageUrl = "", currency = "USD"
        )
        assertFalse(price.isArs)
        assertEquals(59.99f, price.price, 0.01f)
    }

    @Test
    fun `SteamGamePrice free game`() {
        val price = SteamGamePrice(
            appId = 730, name = "CS2",
            priceCents = 0, retailPriceCents = 0,
            discountPercent = 0, isFree = true,
            headerImageUrl = "", currency = "ARS"
        )
        assertTrue(price.isFree)
        assertEquals(0f, price.price, 0.01f)
        assertEquals(0f, price.retailPrice, 0.01f)
    }

    @Test
    fun `SteamGamePrice with discount end timestamp`() {
        val ts = System.currentTimeMillis() + 86400000L
        val price = SteamGamePrice(
            appId = 100, name = "Test",
            priceCents = 2999, retailPriceCents = 5999,
            discountPercent = 50, isFree = false,
            headerImageUrl = "", currency = "ARS",
            discountEndTimestamp = ts
        )
        assertEquals(ts, price.discountEndTimestamp)
        assertEquals(50, price.discountPercent)
    }

    @Test
    fun `SteamGamePrice discountEndTimestamp defaults to null`() {
        val price = SteamGamePrice(
            appId = 100, name = "Test",
            priceCents = 100, retailPriceCents = 100,
            discountPercent = 0, isFree = false,
            headerImageUrl = "", currency = "ARS"
        )
        assertNull(price.discountEndTimestamp)
    }

    @Test
    fun `SteamGamePrice zero cents`() {
        val price = SteamGamePrice(
            appId = 1, name = "X",
            priceCents = 0, retailPriceCents = 0,
            discountPercent = 0, isFree = false,
            headerImageUrl = "", currency = "ARS"
        )
        assertEquals(0f, price.price, 0.01f)
    }
}

