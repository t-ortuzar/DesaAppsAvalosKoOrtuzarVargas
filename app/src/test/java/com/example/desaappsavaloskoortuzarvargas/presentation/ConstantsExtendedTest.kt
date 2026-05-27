package com.example.desaappsavaloskoortuzarvargas.presentation

import org.junit.Assert.*
import org.junit.Test

/**
 * Extended tests for presentation constants.
 */
class ConstantsExtendedTest {

    @Test
    fun `POPULAR_TAGS has 14 entries`() {
        assertEquals(14, POPULAR_TAGS.size)
    }

    @Test
    fun `POPULAR_TAGS contains key genres`() {
        assertTrue(POPULAR_TAGS.contains("Action"))
        assertTrue(POPULAR_TAGS.contains("RPG"))
        assertTrue(POPULAR_TAGS.contains("FPS"))
        assertTrue(POPULAR_TAGS.contains("Open World"))
        assertTrue(POPULAR_TAGS.contains("Horror"))
        assertTrue(POPULAR_TAGS.contains("Survival"))
        assertTrue(POPULAR_TAGS.contains("Co-op"))
        assertTrue(POPULAR_TAGS.contains("Indie"))
        assertTrue(POPULAR_TAGS.contains("Puzzle"))
        assertTrue(POPULAR_TAGS.contains("Racing"))
        assertTrue(POPULAR_TAGS.contains("Sports"))
        assertTrue(POPULAR_TAGS.contains("Souls-like"))
        assertTrue(POPULAR_TAGS.contains("Roguelike"))
        assertTrue(POPULAR_TAGS.contains("Strategy"))
    }

    @Test
    fun `POPULAR_TAGS has no duplicates`() {
        assertEquals(POPULAR_TAGS.size, POPULAR_TAGS.toSet().size)
    }

    @Test
    fun `POPULAR_TAGS entries are non-empty`() {
        POPULAR_TAGS.forEach { assertTrue(it.isNotEmpty()) }
    }

    @Test
    fun `STORE_PLATFORMS has 7 entries`() {
        assertEquals(7, STORE_PLATFORMS.size)
    }

    @Test
    fun `STORE_PLATFORMS contains all stores`() {
        assertTrue(STORE_PLATFORMS.contains("Steam"))
        assertTrue(STORE_PLATFORMS.contains("Epic Games"))
        assertTrue(STORE_PLATFORMS.contains("GOG"))
        assertTrue(STORE_PLATFORMS.contains("Xbox / Microsoft"))
        assertTrue(STORE_PLATFORMS.contains("EA"))
        assertTrue(STORE_PLATFORMS.contains("Ubisoft"))
        assertTrue(STORE_PLATFORMS.contains("Battle.net"))
    }

    @Test
    fun `STORE_PLATFORMS has no duplicates`() {
        assertEquals(STORE_PLATFORMS.size, STORE_PLATFORMS.toSet().size)
    }

    @Test
    fun `STORE_PLATFORMS entries are non-empty`() {
        STORE_PLATFORMS.forEach { assertTrue(it.isNotEmpty()) }
    }

    @Test
    fun `AppColors F2PBlue is not transparent`() {
        assertNotEquals(0L, AppColors.F2PBlue.value.toLong())
    }

    @Test
    fun `AppColors FreeGreen is not transparent`() {
        assertNotEquals(0L, AppColors.FreeGreen.value.toLong())
    }

    @Test
    fun `AppColors HistoricalGold is not transparent`() {
        assertNotEquals(0L, AppColors.HistoricalGold.value.toLong())
    }

    @Test
    fun `AppColors UrgentOrange is not transparent`() {
        assertNotEquals(0L, AppColors.UrgentOrange.value.toLong())
    }

    @Test
    fun `AppColors PriceDropPurple is not transparent`() {
        assertNotEquals(0L, AppColors.PriceDropPurple.value.toLong())
    }

    @Test
    fun `all AppColors are distinct`() {
        val colors = listOf(
            AppColors.F2PBlue,
            AppColors.FreeGreen,
            AppColors.HistoricalGold,
            AppColors.UrgentOrange,
            AppColors.PriceDropPurple
        )
        assertEquals(colors.size, colors.toSet().size)
    }
}

