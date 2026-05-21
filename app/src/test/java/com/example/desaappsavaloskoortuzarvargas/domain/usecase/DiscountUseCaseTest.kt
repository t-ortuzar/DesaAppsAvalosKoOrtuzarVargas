package com.example.desaappsavaloskoortuzarvargas.domain.usecase

import com.example.desaappsavaloskoortuzarvargas.domain.model.DiscountedGame
import com.example.desaappsavaloskoortuzarvargas.domain.repository.DiscountRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class DiscountUseCaseTest {

    private lateinit var repo: DiscountRepository
    private val sampleDiscount = DiscountedGame(gameId = 1, gameName = "Test", imageUrl = "url",
        platform = "Steam", originalPrice = 59.99f, currentPrice = 29.99f, discountPercentage = 50)

    @Before
    fun setup() { repo = mock() }

    @Test
    fun `GetCurrentDiscountsUseCase delegates to repository`() = runTest {
        whenever(repo.getCurrentDiscounts()).thenReturn(Result.success(listOf(sampleDiscount)))
        val result = GetCurrentDiscountsUseCase(repo)()
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
    }

    @Test
    fun `GetFavoriteDiscountsUseCase delegates to repository`() = runTest {
        whenever(repo.getFavoriteDiscounts(listOf(1, 2))).thenReturn(Result.success(listOf(sampleDiscount)))
        val result = GetFavoriteDiscountsUseCase(repo)(listOf(1, 2))
        assertTrue(result.isSuccess)
        verify(repo).getFavoriteDiscounts(listOf(1, 2))
    }

    @Test
    fun `GetHistoricalLowDiscountsUseCase delegates to repository`() = runTest {
        whenever(repo.getHistoricalLowDiscounts()).thenReturn(Result.success(emptyList()))
        val result = GetHistoricalLowDiscountsUseCase(repo)()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `GetFreeGamesUseCase delegates to repository`() = runTest {
        whenever(repo.getFreeGames()).thenReturn(Result.success(listOf(sampleDiscount)))
        val result = GetFreeGamesUseCase(repo)()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `GetCurrentDiscountsUseCase propagates failure`() = runTest {
        whenever(repo.getCurrentDiscounts()).thenReturn(Result.failure(RuntimeException("Error")))
        val result = GetCurrentDiscountsUseCase(repo)()
        assertTrue(result.isFailure)
    }

    @Test
    fun `GetFavoriteDiscountsUseCase propagates failure`() = runTest {
        whenever(repo.getFavoriteDiscounts(any())).thenReturn(Result.failure(RuntimeException("Error")))
        val result = GetFavoriteDiscountsUseCase(repo)(listOf(1))
        assertTrue(result.isFailure)
    }

    @Test
    fun `GetHistoricalLowDiscountsUseCase propagates failure`() = runTest {
        whenever(repo.getHistoricalLowDiscounts()).thenReturn(Result.failure(RuntimeException("Error")))
        val result = GetHistoricalLowDiscountsUseCase(repo)()
        assertTrue(result.isFailure)
    }

    @Test
    fun `GetFreeGamesUseCase propagates failure`() = runTest {
        whenever(repo.getFreeGames()).thenReturn(Result.failure(RuntimeException("Error")))
        val result = GetFreeGamesUseCase(repo)()
        assertTrue(result.isFailure)
    }

    @Test
    fun `GetPriceDropsUseCase delegates to repository`() = runTest {
        whenever(repo.getPriceDrops()).thenReturn(Result.success(listOf(sampleDiscount)))
        val result = GetPriceDropsUseCase(repo)()
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
    }

    @Test
    fun `GetPriceDropsUseCase propagates failure`() = runTest {
        whenever(repo.getPriceDrops()).thenReturn(Result.failure(RuntimeException("Error")))
        val result = GetPriceDropsUseCase(repo)()
        assertTrue(result.isFailure)
    }
}
