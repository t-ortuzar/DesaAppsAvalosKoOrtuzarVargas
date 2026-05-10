package com.example.desaappsavaloskoortuzarvargas.domain.repository

import com.example.desaappsavaloskoortuzarvargas.domain.model.Game
import com.example.desaappsavaloskoortuzarvargas.domain.model.PriceHistory
import com.example.desaappsavaloskoortuzarvargas.domain.model.UserFavorite

interface GameRepository {
    suspend fun getAllGames(): Result<List<Game>>
    suspend fun getGameById(id: Int): Result<Game>
    suspend fun searchGames(query: String): Result<List<Game>>
    suspend fun getPriceHistory(gameId: Int): Result<List<PriceHistory>>
    suspend fun addToFavorites(game: Game): Result<Unit>
    suspend fun removeFromFavorites(gameId: Int): Result<Unit>
    suspend fun getFavorites(): Result<List<Game>>
    suspend fun isFavorite(gameId: Int): Result<Boolean>
}

