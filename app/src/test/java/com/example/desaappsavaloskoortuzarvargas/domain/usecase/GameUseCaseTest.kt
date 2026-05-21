package com.example.desaappsavaloskoortuzarvargas.domain.usecase

import com.example.desaappsavaloskoortuzarvargas.domain.model.*
import com.example.desaappsavaloskoortuzarvargas.domain.repository.GameRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class GameUseCaseTest {

    private lateinit var repo: GameRepository
    private val sampleGame = Game(id = 1, name = "Test", description = "D", releaseDate = "2024",
        imageUrl = "url", rating = 8.0, currentPrices = mapOf("Steam" to 29.99f))

    @Before
    fun setup() {
        repo = mock()
    }

    @Test
    fun `GetAllGamesUseCase delegates to repository`() = runTest {
        whenever(repo.getAllGames()).thenReturn(Result.success(listOf(sampleGame)))
        val result = GetAllGamesUseCase(repo)()
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        verify(repo).getAllGames()
    }

    @Test
    fun `GetGameByIdUseCase delegates to repository`() = runTest {
        whenever(repo.getGameById(1)).thenReturn(Result.success(sampleGame))
        val result = GetGameByIdUseCase(repo)(1)
        assertTrue(result.isSuccess)
        assertEquals("Test", result.getOrNull()?.name)
        verify(repo).getGameById(1)
    }

    @Test
    fun `SearchGamesUseCase delegates to repository`() = runTest {
        whenever(repo.searchGames("test")).thenReturn(Result.success(listOf(sampleGame)))
        val result = SearchGamesUseCase(repo)("test")
        assertTrue(result.isSuccess)
        verify(repo).searchGames("test")
    }

    @Test
    fun `GetGamesByTagUseCase delegates to repository`() = runTest {
        whenever(repo.getGamesByTag("Action")).thenReturn(Result.success(listOf(sampleGame)))
        val result = GetGamesByTagUseCase(repo)("Action")
        assertTrue(result.isSuccess)
        verify(repo).getGamesByTag("Action")
    }

    @Test
    fun `AddToFavoritesUseCase delegates to repository`() = runTest {
        whenever(repo.addToFavorites(sampleGame)).thenReturn(Result.success(Unit))
        val result = AddToFavoritesUseCase(repo)(sampleGame)
        assertTrue(result.isSuccess)
        verify(repo).addToFavorites(sampleGame)
    }

    @Test
    fun `RemoveFromFavoritesUseCase delegates to repository`() = runTest {
        whenever(repo.removeFromFavorites(1)).thenReturn(Result.success(Unit))
        val result = RemoveFromFavoritesUseCase(repo)(1)
        assertTrue(result.isSuccess)
        verify(repo).removeFromFavorites(1)
    }

    @Test
    fun `GetFavoritesUseCase delegates to repository`() = runTest {
        whenever(repo.getFavorites()).thenReturn(Result.success(listOf(sampleGame)))
        val result = GetFavoritesUseCase(repo)()
        assertTrue(result.isSuccess)
        verify(repo).getFavorites()
    }

    @Test
    fun `GetPriceHistoryUseCase delegates to repository`() = runTest {
        val history = listOf(PriceHistory(1, "Steam", 29.99f, 0, "2024"))
        whenever(repo.getPriceHistory(1)).thenReturn(Result.success(history))
        val result = GetPriceHistoryUseCase(repo)(1)
        assertTrue(result.isSuccess)
        verify(repo).getPriceHistory(1)
    }

    @Test
    fun `GetAllGamesUseCase propagates failure`() = runTest {
        whenever(repo.getAllGames()).thenReturn(Result.failure(RuntimeException("Network error")))
        val result = GetAllGamesUseCase(repo)()
        assertTrue(result.isFailure)
    }
}

