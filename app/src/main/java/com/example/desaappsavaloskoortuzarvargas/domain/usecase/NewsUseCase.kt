package com.example.desaappsavaloskoortuzarvargas.domain.usecase

import com.example.desaappsavaloskoortuzarvargas.domain.model.News
import com.example.desaappsavaloskoortuzarvargas.domain.repository.NewsRepository

class GetAllNewsUseCase(private val newsRepository: NewsRepository) {
    suspend operator fun invoke(): Result<List<News>> = newsRepository.getAllNews()
}

class GetNewsByGameIdUseCase(private val newsRepository: NewsRepository) {
    suspend operator fun invoke(gameId: Int): Result<List<News>> = newsRepository.getNewsByGameId(gameId)
}

class GetNewsByCategoryUseCase(private val newsRepository: NewsRepository) {
    suspend operator fun invoke(category: String): Result<List<News>> = newsRepository.getNewsByCategory(category)
}

class GetNewsByPlatformUseCase(private val newsRepository: NewsRepository) {
    suspend operator fun invoke(platform: String): Result<List<News>> = newsRepository.getNewsByPlatform(platform)
}

class GetNewsByFavoritesUseCase(private val newsRepository: NewsRepository) {
    suspend operator fun invoke(favoriteGameIds: List<Int>): Result<List<News>> =
        newsRepository.getNewsByFavorites(favoriteGameIds)
}
