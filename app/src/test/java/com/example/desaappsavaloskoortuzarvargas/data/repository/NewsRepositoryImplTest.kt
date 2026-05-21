package com.example.desaappsavaloskoortuzarvargas.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class NewsRepositoryImplTest {

    private lateinit var repo: NewsRepositoryImpl

    @Before
    fun setup() { repo = NewsRepositoryImpl() }

    @Test
    fun `getAllNews returns sorted news`() = runTest {
        val result = repo.getAllNews()
        assertTrue(result.isSuccess)
        val news = result.getOrNull()!!
        assertTrue(news.isNotEmpty())
        for (i in 0 until news.size - 1) {
            assertTrue(news[i].date >= news[i + 1].date)
        }
    }

    @Test
    fun `getNewsByGameId returns filtered news`() = runTest {
        val allNews = repo.getAllNews().getOrNull()!!
        val gameId = allNews.first().gameId!!
        val result = repo.getNewsByGameId(gameId)
        assertTrue(result.isSuccess)
        result.getOrNull()!!.forEach { assertEquals(gameId, it.gameId) }
    }

    @Test
    fun `getNewsByCategory returns filtered news`() = runTest {
        val result = repo.getNewsByCategory("discount")
        assertTrue(result.isSuccess)
        result.getOrNull()!!.forEach { assertEquals("discount", it.category) }
    }

    @Test
    fun `getNewsByPlatform returns filtered news`() = runTest {
        val result = repo.getNewsByPlatform("Steam")
        assertTrue(result.isSuccess)
        result.getOrNull()!!.forEach { assertEquals("Steam", it.platform) }
    }

    @Test
    fun `getNewsByFavorites returns filtered news`() = runTest {
        val result = repo.getNewsByFavorites(listOf(1, 2))
        assertTrue(result.isSuccess)
        result.getOrNull()!!.forEach { assertTrue(it.gameId in listOf(1, 2)) }
    }

    @Test
    fun `getNewsByFavorites with empty list returns empty`() = runTest {
        val result = repo.getNewsByFavorites(emptyList())
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `getNewsByCategory with nonexistent category returns empty`() = runTest {
        val result = repo.getNewsByCategory("nonexistent")
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }
}

