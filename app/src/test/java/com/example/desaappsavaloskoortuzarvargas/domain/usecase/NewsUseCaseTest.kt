package com.example.desaappsavaloskoortuzarvargas.domain.usecase

import com.example.desaappsavaloskoortuzarvargas.domain.model.News
import com.example.desaappsavaloskoortuzarvargas.domain.repository.NewsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class NewsUseCaseTest {

    private lateinit var repo: NewsRepository
    private val sampleNews = News(id = 1, title = "Title", content = "Content", imageUrl = "url",
        date = "2024-01-01", gameId = 1, platform = "Steam", category = "discount")

    @Before
    fun setup() { repo = mock() }

    @Test
    fun `GetAllNewsUseCase delegates to repository`() = runTest {
        whenever(repo.getAllNews()).thenReturn(Result.success(listOf(sampleNews)))
        val result = GetAllNewsUseCase(repo)()
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
    }

    @Test
    fun `GetNewsByGameIdUseCase delegates to repository`() = runTest {
        whenever(repo.getNewsByGameId(1)).thenReturn(Result.success(listOf(sampleNews)))
        val result = GetNewsByGameIdUseCase(repo)(1)
        assertTrue(result.isSuccess)
        verify(repo).getNewsByGameId(1)
    }

    @Test
    fun `GetNewsByFavoritesUseCase delegates to repository`() = runTest {
        whenever(repo.getNewsByFavorites(listOf(1, 2))).thenReturn(Result.success(listOf(sampleNews)))
        val result = GetNewsByFavoritesUseCase(repo)(listOf(1, 2))
        assertTrue(result.isSuccess)
        verify(repo).getNewsByFavorites(listOf(1, 2))
    }

    @Test
    fun `GetAllNewsUseCase propagates failure`() = runTest {
        whenever(repo.getAllNews()).thenReturn(Result.failure(RuntimeException("Error")))
        val result = GetAllNewsUseCase(repo)()
        assertTrue(result.isFailure)
    }

    @Test
    fun `GetNewsByGameIdUseCase propagates failure`() = runTest {
        whenever(repo.getNewsByGameId(any())).thenReturn(Result.failure(RuntimeException("Error")))
        val result = GetNewsByGameIdUseCase(repo)(1)
        assertTrue(result.isFailure)
    }

    @Test
    fun `GetNewsByFavoritesUseCase propagates failure`() = runTest {
        whenever(repo.getNewsByFavorites(any())).thenReturn(Result.failure(RuntimeException("Error")))
        val result = GetNewsByFavoritesUseCase(repo)(listOf(1))
        assertTrue(result.isFailure)
    }
}
