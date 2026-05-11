package com.example.desaappsavaloskoortuzarvargas.domain.usecase

import com.example.desaappsavaloskoortuzarvargas.domain.model.Game
import com.example.desaappsavaloskoortuzarvargas.domain.repository.GameRepository

class GetAllGamesUseCase(private val gameRepository: GameRepository) {
    suspend operator fun invoke(): Result<List<Game>> = gameRepository.getAllGames()
}

class GetGameByIdUseCase(private val gameRepository: GameRepository) {
    suspend operator fun invoke(id: Int): Result<Game> = gameRepository.getGameById(id)
}

class SearchGamesUseCase(private val gameRepository: GameRepository) {
    suspend operator fun invoke(query: String): Result<List<Game>> = gameRepository.searchGames(query)
}

class GetGamesByTagUseCase(private val gameRepository: GameRepository) {
    suspend operator fun invoke(tag: String): Result<List<Game>> = gameRepository.getGamesByTag(tag)
}

class AddToFavoritesUseCase(private val gameRepository: GameRepository) {
    suspend operator fun invoke(game: Game): Result<Unit> = gameRepository.addToFavorites(game)
}

class RemoveFromFavoritesUseCase(private val gameRepository: GameRepository) {
    suspend operator fun invoke(gameId: Int): Result<Unit> = gameRepository.removeFromFavorites(gameId)
}

class GetFavoritesUseCase(private val gameRepository: GameRepository) {
    suspend operator fun invoke(): Result<List<Game>> = gameRepository.getFavorites()
}

class IsFavoriteUseCase(private val gameRepository: GameRepository) {
    suspend operator fun invoke(gameId: Int): Result<Boolean> = gameRepository.isFavorite(gameId)
}

class GetPriceHistoryUseCase(private val gameRepository: GameRepository) {
    suspend operator fun invoke(gameId: Int) = gameRepository.getPriceHistory(gameId)
}
