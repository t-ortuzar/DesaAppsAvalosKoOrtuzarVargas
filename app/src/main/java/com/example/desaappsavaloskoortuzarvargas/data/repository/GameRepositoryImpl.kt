package com.example.desaappsavaloskoortuzarvargas.data.repository

import com.example.desaappsavaloskoortuzarvargas.data.catalog.GameCatalog
import com.example.desaappsavaloskoortuzarvargas.domain.model.Game
import com.example.desaappsavaloskoortuzarvargas.domain.model.PriceHistory
import com.example.desaappsavaloskoortuzarvargas.domain.repository.GameRepository

class GameRepositoryImpl : GameRepository {

    private val allGames = GameCatalog.generateGames()
    private val priceHistory = GameCatalog.generatePriceHistory()
    private val favoriteIds = mutableSetOf<Int>()

    override suspend fun getAllGames(): Result<List<Game>> = try {
        Result.success(allGames.map { it.copy(isFavorite = it.id in favoriteIds) })
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun getGameById(id: Int): Result<Game> = try {
        val game = allGames.first { it.id == id }
        Result.success(game.copy(isFavorite = game.id in favoriteIds))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun searchGames(query: String): Result<List<Game>> = try {
        val filtered = allGames.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.tags.any { tag -> tag.contains(query, ignoreCase = true) }
        }
        Result.success(filtered.map { it.copy(isFavorite = it.id in favoriteIds) })
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun getGamesByTag(tag: String): Result<List<Game>> = try {
        val filtered = allGames.filter { it.tags.contains(tag) }
        Result.success(filtered.map { it.copy(isFavorite = it.id in favoriteIds) })
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun getPriceHistory(gameId: Int): Result<List<PriceHistory>> = try {
        Result.success(priceHistory.filter { it.gameId == gameId })
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun addToFavorites(game: Game): Result<Unit> = try {
        favoriteIds.add(game.id)
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun removeFromFavorites(gameId: Int): Result<Unit> = try {
        favoriteIds.remove(gameId)
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun getFavorites(): Result<List<Game>> = try {
        Result.success(allGames.filter { it.id in favoriteIds }.map { it.copy(isFavorite = true) })
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun isFavorite(gameId: Int): Result<Boolean> = try {
        Result.success(gameId in favoriteIds)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun initializeFavorites(ids: Set<Int>): Result<Unit> = try {
        favoriteIds.clear()
        favoriteIds.addAll(ids)
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }
}
