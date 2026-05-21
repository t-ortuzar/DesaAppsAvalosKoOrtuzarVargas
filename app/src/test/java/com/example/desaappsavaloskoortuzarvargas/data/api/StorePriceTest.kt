package com.example.desaappsavaloskoortuzarvargas.data.api

import org.junit.Assert.*
import org.junit.Test

class StorePriceTest {

    @Test
    fun `StorePrice ARS is recognized as ARS`() {
        val price = StorePrice(
            storeName = "Steam",
            currentPrice = 8799f,
            originalPrice = 10999f,
            discountPercent = 20,
            currency = "ARS"
        )
        assertTrue(price.isArs)
        assertTrue(price.isDiscounted)
        assertFalse(price.isFree)
    }

    @Test
    fun `StorePrice USD is not ARS`() {
        val price = StorePrice(
            storeName = "GOG",
            currentPrice = 29.99f,
            originalPrice = 29.99f,
            discountPercent = 0,
            currency = "USD"
        )
        assertFalse(price.isArs)
        assertFalse(price.isDiscounted)
    }

    @Test
    fun `StorePrice free game`() {
        val price = StorePrice(
            storeName = "Epic Games",
            currentPrice = 0f,
            originalPrice = 0f,
            discountPercent = 0,
            currency = "ARS",
            isFree = true
        )
        assertTrue(price.isFree)
        assertTrue(price.isArs)
    }

    @Test
    fun `StorePrice with formatted price`() {
        val price = StorePrice(
            storeName = "Epic Games",
            currentPrice = 8799f,
            originalPrice = 10999f,
            discountPercent = 20,
            currency = "ARS",
            formattedPrice = "ARS$ 8.799,00",
            formattedOriginal = "ARS$ 10.999,00"
        )
        assertEquals("ARS$ 8.799,00", price.formattedPrice)
    }

    @Test
    fun `StorePrice with imageUrl from Epic`() {
        val price = StorePrice(
            storeName = "Epic Games",
            currentPrice = 5000f,
            originalPrice = 5000f,
            discountPercent = 0,
            currency = "ARS",
            imageUrl = "https://cdn1.epicgames.com/offer/image.jpg"
        )
        assertEquals("https://cdn1.epicgames.com/offer/image.jpg", price.imageUrl)
    }

    @Test
    fun `StorePrice imageUrl defaults to empty`() {
        val price = StorePrice(
            storeName = "Steam",
            currentPrice = 100f,
            originalPrice = 100f,
            discountPercent = 0,
            currency = "ARS"
        )
        assertEquals("", price.imageUrl)
    }

    @Test
    fun `SteamGamePrice currency detection`() {
        val arPrice = SteamGamePrice(
            appId = 1245620, name = "Elden Ring",
            priceCents = 879900, retailPriceCents = 1099900,
            discountPercent = 20, isFree = false,
            headerImageUrl = "", currency = "ARS"
        )
        assertTrue(arPrice.isArs)
        assertEquals(8799f, arPrice.price, 0.01f)
        assertEquals(10999f, arPrice.retailPrice, 0.01f)
    }

    @Test
    fun `SteamGamePrice free game`() {
        val freeGame = SteamGamePrice(
            appId = 730, name = "Counter-Strike 2",
            priceCents = 0, retailPriceCents = 0,
            discountPercent = 0, isFree = true,
            headerImageUrl = "", currency = "ARS"
        )
        assertTrue(freeGame.isFree)
        assertEquals(0f, freeGame.price, 0.01f)
    }
}

