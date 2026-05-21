package com.example.desaappsavaloskoortuzarvargas.domain.model

import org.junit.Assert.*
import org.junit.Test

class NewsModelTest {
    @Test
    fun `News data class`() {
        val news = News(id = 1, title = "Title", content = "Content", imageUrl = "url",
            date = "2024-01-01", gameId = 5, platform = "Steam", category = "discount")
        assertEquals(1, news.id)
        assertEquals("Title", news.title)
        assertEquals(5, news.gameId)
        assertEquals("discount", news.category)
    }

    @Test
    fun `News with null gameId`() {
        val news = News(id = 2, title = "T", content = "C", imageUrl = "u",
            date = "2024", gameId = null, platform = "All", category = "event")
        assertNull(news.gameId)
    }
}

