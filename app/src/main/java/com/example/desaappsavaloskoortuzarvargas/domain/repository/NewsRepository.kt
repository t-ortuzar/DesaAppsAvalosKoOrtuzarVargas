package com.example.desaappsavaloskoortuzarvargas.domain.repository

import com.example.desaappsavaloskoortuzarvargas.domain.model.News

interface NewsRepository {
    suspend fun getAllNews(): Result<List<News>>
    suspend fun getNewsByGameId(gameId: Int): Result<List<News>>
    suspend fun getNewsByCategory(category: String): Result<List<News>>
    suspend fun getNewsByPlatform(platform: String): Result<List<News>>
    suspend fun getNewsByFavorites(favoriteGameIds: List<Int>): Result<List<News>>
}

