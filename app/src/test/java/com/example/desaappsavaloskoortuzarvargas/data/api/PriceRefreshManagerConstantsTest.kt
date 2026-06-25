package com.example.desaappsavaloskoortuzarvargas.data.api

import org.junit.Assert.*
import org.junit.Test

class PriceRefreshManagerConstantsTest {

    @Test
    fun `DETAIL_STALE_MS is 5 minutes`() {
        assertEquals(5 * 60 * 1000L, PriceRefreshManager.DETAIL_STALE_MS)
    }

    @Test
    fun `OFFERS_REFRESH_INTERVAL_MS is 15 minutes`() {
        assertEquals(15 * 60 * 1000L, PriceRefreshManager.OFFERS_REFRESH_INTERVAL_MS)
    }

    @Test
    fun `CATALOG_REFRESH_INTERVAL_MS is 1 hour`() {
        assertEquals(60 * 60 * 1000L, PriceRefreshManager.CATALOG_REFRESH_INTERVAL_MS)
    }

    @Test
    fun `BATCH_SIZE is 5`() {
        assertEquals(5, PriceRefreshManager.BATCH_SIZE)
    }

    @Test
    fun `BATCH_DELAY_MS is 3 seconds`() {
        assertEquals(3000L, PriceRefreshManager.BATCH_DELAY_MS)
    }

    @Test
    fun `INTRA_BATCH_DELAY_MS is 500ms`() {
        assertEquals(500L, PriceRefreshManager.INTRA_BATCH_DELAY_MS)
    }

    @Test
    fun `DETAIL_STALE_MS is less than OFFERS_REFRESH_INTERVAL_MS`() {
        assertTrue(PriceRefreshManager.DETAIL_STALE_MS < PriceRefreshManager.OFFERS_REFRESH_INTERVAL_MS)
    }

    @Test
    fun `OFFERS_REFRESH_INTERVAL_MS is less than CATALOG_REFRESH_INTERVAL_MS`() {
        assertTrue(PriceRefreshManager.OFFERS_REFRESH_INTERVAL_MS < PriceRefreshManager.CATALOG_REFRESH_INTERVAL_MS)
    }

    @Test
    fun `INTRA_BATCH_DELAY_MS is less than BATCH_DELAY_MS`() {
        assertTrue(PriceRefreshManager.INTRA_BATCH_DELAY_MS < PriceRefreshManager.BATCH_DELAY_MS)
    }
}
