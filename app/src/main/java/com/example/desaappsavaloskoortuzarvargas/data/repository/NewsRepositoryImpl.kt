package com.example.desaappsavaloskoortuzarvargas.data.repository

import com.example.desaappsavaloskoortuzarvargas.data.catalog.GameCatalog
import com.example.desaappsavaloskoortuzarvargas.domain.model.News
import com.example.desaappsavaloskoortuzarvargas.domain.repository.NewsRepository

class NewsRepositoryImpl : NewsRepository {

    private val allNews = GameCatalog.generateNews()

    override suspend fun getAllNews(): Result<List<News>> = try {
        Result.success(allNews.sortedByDescending { it.date })
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun getNewsByGameId(gameId: Int): Result<List<News>> = try {
        Result.success(allNews.filter { it.gameId == gameId }.sortedByDescending { it.date })
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun getNewsByCategory(category: String): Result<List<News>> = try {
        Result.success(allNews.filter { it.category == category }.sortedByDescending { it.date })
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun getNewsByPlatform(platform: String): Result<List<News>> = try {
        Result.success(allNews.filter { it.platform == platform }.sortedByDescending { it.date })
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun getNewsByFavorites(favoriteGameIds: List<Int>): Result<List<News>> = try {
        Result.success(allNews.filter { it.gameId in favoriteGameIds }.sortedByDescending { it.date })
    } catch (e: Exception) { Result.failure(e) }
}
