package com.example.desaappsavaloskoortuzarvargas.presentation

import org.junit.Assert.*
import org.junit.Test

class ConstantsTest {

    @Test
    fun `AppColors has correct hex values`() {
        assertNotNull(AppColors.F2PBlue)
        assertNotNull(AppColors.FreeGreen)
        assertNotNull(AppColors.HistoricalGold)
        assertNotNull(AppColors.UrgentOrange)
    }

    @Test
    fun `POPULAR_TAGS has expected size`() {
        assertEquals(14, POPULAR_TAGS.size)
    }

    @Test
    fun `POPULAR_TAGS contains key tags`() {
        assertTrue(POPULAR_TAGS.contains("Action"))
        assertTrue(POPULAR_TAGS.contains("RPG"))
        assertTrue(POPULAR_TAGS.contains("FPS"))
        assertTrue(POPULAR_TAGS.contains("Strategy"))
    }

    @Test
    fun `STORE_PLATFORMS has expected size`() {
        assertEquals(7, STORE_PLATFORMS.size)
    }

    @Test
    fun `STORE_PLATFORMS contains key platforms`() {
        assertTrue(STORE_PLATFORMS.contains("Steam"))
        assertTrue(STORE_PLATFORMS.contains("Epic Games"))
        assertTrue(STORE_PLATFORMS.contains("GOG"))
    }
}

