package com.example.desaappsavaloskoortuzarvargas.data.api

import org.junit.Assert.*
import org.junit.Test

/**
 * Extended tests for StorePrice covering more edge cases.
 */
class StorePriceExtendedTest {

    @Test
    fun `StorePrice isDiscounted true for positive discount`() {
        val price = StorePrice(
            storeName = "Steam", currentPrice = 5000f, originalPrice = 10000f,
            discountPercent = 50, currency = "ARS"
        )
        assertTrue(price.isDiscounted)
    }

    @Test
    fun `StorePrice isDiscounted false for zero discount`() {
        val price = StorePrice(
            storeName = "Steam", currentPrice = 10000f, originalPrice = 10000f,
            discountPercent = 0, currency = "ARS"
        )
        assertFalse(price.isDiscounted)
    }

    @Test
    fun `StorePrice isArs true for ARS`() {
        val price = StorePrice(
            storeName = "Steam", currentPrice = 100f, originalPrice = 100f,
            discountPercent = 0, currency = "ARS"
        )
        assertTrue(price.isArs)
    }

    @Test
    fun `StorePrice isArs false for USD`() {
        val price = StorePrice(
            storeName = "GOG", currentPrice = 29.99f, originalPrice = 29.99f,
            discountPercent = 0, currency = "USD"
        )
        assertFalse(price.isArs)
    }

    @Test
    fun `StorePrice isArs false for EUR`() {
        val price = StorePrice(
            storeName = "GOG", currentPrice = 25f, originalPrice = 25f,
            discountPercent = 0, currency = "EUR"
        )
        assertFalse(price.isArs)
    }

    @Test
    fun `StorePrice with storeUrl`() {
        val price = StorePrice(
            storeName = "Steam", currentPrice = 100f, originalPrice = 100f,
            discountPercent = 0, currency = "ARS",
            storeUrl = "https://store.steampowered.com/app/1245620"
        )
        assertEquals("https://store.steampowered.com/app/1245620", price.storeUrl)
    }

    @Test
    fun `StorePrice storeUrl defaults to empty`() {
        val price = StorePrice(
            storeName = "Steam", currentPrice = 100f, originalPrice = 100f,
            discountPercent = 0, currency = "ARS"
        )
        assertEquals("", price.storeUrl)
    }

    @Test
    fun `StorePrice formattedPrice and formattedOriginal`() {
        val price = StorePrice(
            storeName = "Epic Games", currentPrice = 5000f, originalPrice = 10000f,
            discountPercent = 50, currency = "ARS",
            formattedPrice = "ARS$ 5.000,00",
            formattedOriginal = "ARS$ 10.000,00"
        )
        assertEquals("ARS$ 5.000,00", price.formattedPrice)
        assertEquals("ARS$ 10.000,00", price.formattedOriginal)
    }

    @Test
    fun `StorePrice discountEndTimestamp`() {
        val ts = System.currentTimeMillis() + 86400000L
        val price = StorePrice(
            storeName = "Steam", currentPrice = 5000f, originalPrice = 10000f,
            discountPercent = 50, currency = "ARS",
            discountEndTimestamp = ts
        )
        assertEquals(ts, price.discountEndTimestamp)
    }

    @Test
    fun `StorePrice discountEndTimestamp defaults to null`() {
        val price = StorePrice(
            storeName = "Steam", currentPrice = 100f, originalPrice = 100f,
            discountPercent = 0, currency = "ARS"
        )
        assertNull(price.discountEndTimestamp)
    }

    @Test
    fun `StorePrice copy changes only specified fields`() {
        val original = StorePrice(
            storeName = "Steam", currentPrice = 5000f, originalPrice = 10000f,
            discountPercent = 50, currency = "ARS", isFree = false,
            imageUrl = "https://img.com/game.jpg"
        )
        val updated = original.copy(currentPrice = 3000f, discountPercent = 70)
        assertEquals("Steam", updated.storeName)
        assertEquals(3000f, updated.currentPrice, 0.01f)
        assertEquals(70, updated.discountPercent)
        assertEquals("https://img.com/game.jpg", updated.imageUrl)
    }

    @Test
    fun `StorePrice all store names`() {
        val storeNames = listOf("Steam", "Epic Games", "GOG", "Xbox / Microsoft", "EA", "Ubisoft", "Battle.net")
        storeNames.forEach { name ->
            val price = StorePrice(
                storeName = name, currentPrice = 100f, originalPrice = 100f,
                discountPercent = 0, currency = "ARS"
            )
            assertEquals(name, price.storeName)
        }
    }
}

