package com.example.desaappsavaloskoortuzarvargas.data.local.entity

import org.junit.Assert.*
import org.junit.Test

class EntityDataClassesTest {

    // ── GamePriceEntity ──

    @Test
    fun `GamePriceEntity defaults`() {
        val entity = GamePriceEntity(
            gameId = 1,
            gameName = "Elden Ring",
            storeName = "Steam",
            currentPrice = 8799f,
            retailPrice = 10999f,
            savings = 20f,
            dealUrl = "https://store.steampowered.com/app/1245620"
        )
        assertEquals(0L, entity.id)
        assertEquals("ARS", entity.currency)
        assertTrue(entity.lastUpdated > 0)
        assertNull(entity.discountEndTimestamp)
    }

    @Test
    fun `GamePriceEntity with all fields`() {
        val ts = System.currentTimeMillis()
        val entity = GamePriceEntity(
            id = 5,
            gameId = 1,
            gameName = "Test Game",
            storeName = "Epic Games",
            currentPrice = 5000f,
            retailPrice = 10000f,
            savings = 50f,
            dealUrl = "https://epic.com",
            currency = "USD",
            lastUpdated = ts,
            discountEndTimestamp = ts + 86400000
        )
        assertEquals(5L, entity.id)
        assertEquals("USD", entity.currency)
        assertEquals(ts, entity.lastUpdated)
        assertNotNull(entity.discountEndTimestamp)
    }

    @Test
    fun `GamePriceEntity copy`() {
        val entity = GamePriceEntity(
            gameId = 1, gameName = "Test", storeName = "Steam",
            currentPrice = 100f, retailPrice = 200f, savings = 50f,
            dealUrl = "url"
        )
        val updated = entity.copy(currentPrice = 150f, savings = 25f)
        assertEquals(150f, updated.currentPrice, 0.01f)
        assertEquals(25f, updated.savings, 0.01f)
        assertEquals("Test", updated.gameName)
    }

    // ── FavoriteGameEntity ──

    @Test
    fun `FavoriteGameEntity creation`() {
        val entity = FavoriteGameEntity(gameId = 1, gameName = "Elden Ring")
        assertEquals(1, entity.gameId)
        assertEquals("Elden Ring", entity.gameName)
        assertTrue(entity.addedAt > 0)
    }

    @Test
    fun `FavoriteGameEntity with custom addedAt`() {
        val entity = FavoriteGameEntity(gameId = 2, gameName = "Test", addedAt = 12345L)
        assertEquals(12345L, entity.addedAt)
    }

    @Test
    fun `FavoriteGameEntity equality`() {
        val ts = 100L
        val a = FavoriteGameEntity(gameId = 1, gameName = "Game", addedAt = ts)
        val b = FavoriteGameEntity(gameId = 1, gameName = "Game", addedAt = ts)
        assertEquals(a, b)
    }

    // ── GameImageEntity ──

    @Test
    fun `GameImageEntity creation`() {
        val entity = GameImageEntity(
            gameId = 1,
            gameName = "Elden Ring",
            imageUrl = "https://cdn.steam.com/header.jpg"
        )
        assertEquals(1, entity.gameId)
        assertEquals("Elden Ring", entity.gameName)
        assertEquals("https://cdn.steam.com/header.jpg", entity.imageUrl)
        assertTrue(entity.lastUpdated > 0)
    }

    @Test
    fun `GameImageEntity with custom lastUpdated`() {
        val entity = GameImageEntity(
            gameId = 1, gameName = "Test",
            imageUrl = "url", lastUpdated = 999L
        )
        assertEquals(999L, entity.lastUpdated)
    }

    @Test
    fun `GameImageEntity empty imageUrl`() {
        val entity = GameImageEntity(gameId = 1, gameName = "Test", imageUrl = "")
        assertEquals("", entity.imageUrl)
    }

    // ── PriceHistoryEntity ──

    @Test
    fun `PriceHistoryEntity defaults`() {
        val entity = PriceHistoryEntity(
            gameName = "Elden Ring",
            storeName = "Steam",
            currentPrice = 8799f,
            retailPrice = 10999f,
            discountPercent = 20
        )
        assertEquals(0L, entity.id)
        assertEquals("ARS", entity.currency)
        assertTrue(entity.timestamp > 0)
    }

    @Test
    fun `PriceHistoryEntity with all fields`() {
        val ts = 123456789L
        val entity = PriceHistoryEntity(
            id = 10,
            gameName = "Test",
            storeName = "GOG",
            currentPrice = 29.99f,
            retailPrice = 59.99f,
            discountPercent = 50,
            currency = "USD",
            timestamp = ts
        )
        assertEquals(10L, entity.id)
        assertEquals("GOG", entity.storeName)
        assertEquals(29.99f, entity.currentPrice, 0.01f)
        assertEquals(59.99f, entity.retailPrice, 0.01f)
        assertEquals(50, entity.discountPercent)
        assertEquals("USD", entity.currency)
        assertEquals(ts, entity.timestamp)
    }

    @Test
    fun `PriceHistoryEntity no discount`() {
        val entity = PriceHistoryEntity(
            gameName = "Game",
            storeName = "Steam",
            currentPrice = 5999f,
            retailPrice = 5999f,
            discountPercent = 0
        )
        assertEquals(0, entity.discountPercent)
        assertEquals(entity.currentPrice, entity.retailPrice, 0.01f)
    }

    @Test
    fun `PriceHistoryEntity free game`() {
        val entity = PriceHistoryEntity(
            gameName = "Free Game",
            storeName = "Epic Games",
            currentPrice = 0f,
            retailPrice = 5999f,
            discountPercent = 100
        )
        assertEquals(0f, entity.currentPrice, 0.01f)
        assertEquals(100, entity.discountPercent)
    }
}

