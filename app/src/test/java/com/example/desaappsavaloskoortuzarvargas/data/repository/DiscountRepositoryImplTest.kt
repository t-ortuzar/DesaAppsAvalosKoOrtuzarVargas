package com.example.desaappsavaloskoortuzarvargas.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DiscountRepositoryImplTest {

    private lateinit var repo: DiscountRepositoryImpl

    @Before
    fun setup() { repo = DiscountRepositoryImpl() }

    @Test
    fun `getCurrentDiscounts returns non-free discounts sorted`() = runTest {
        val result = repo.getCurrentDiscounts()
        assertTrue(result.isSuccess)
        val discounts = result.getOrNull()!!
        assertTrue(discounts.isNotEmpty())
        discounts.forEach { assertFalse(it.isFree) }
        // Check sorted descending by discount percentage
        for (i in 0 until discounts.size - 1) {
            assertTrue(discounts[i].discountPercentage >= discounts[i + 1].discountPercentage)
        }
    }

    @Test
    fun `getFavoriteDiscounts filters by game ids`() = runTest {
        val all = repo.getCurrentDiscounts().getOrNull()!!
        val ids = all.take(3).map { it.gameId }
        val result = repo.getFavoriteDiscounts(ids)
        assertTrue(result.isSuccess)
        result.getOrNull()!!.forEach { assertTrue(it.gameId in ids) }
    }

    @Test
    fun `getFavoriteDiscounts with empty ids returns empty`() = runTest {
        val result = repo.getFavoriteDiscounts(emptyList())
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `getHistoricalLowDiscounts returns only historical lows`() = runTest {
        val result = repo.getHistoricalLowDiscounts()
        assertTrue(result.isSuccess)
        result.getOrNull()!!.forEach { assertTrue(it.isHistoricalLowest) }
    }

    @Test
    fun `getFreeGames returns only free games`() = runTest {
        val result = repo.getFreeGames()
        assertTrue(result.isSuccess)
        val free = result.getOrNull()!!
        assertTrue(free.isNotEmpty())
        free.forEach { assertTrue(it.isFree) }
    }

    @Test
    fun `getDiscountsByPlatform filters by platform`() = runTest {
        val result = repo.getDiscountsByPlatform("Steam")
        assertTrue(result.isSuccess)
        result.getOrNull()!!.forEach { assertEquals("Steam", it.platform) }
    }

    @Test
    fun `getDiscountsByPlatform with nonexistent platform returns empty`() = runTest {
        val result = repo.getDiscountsByPlatform("NonExistent")
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }
}

