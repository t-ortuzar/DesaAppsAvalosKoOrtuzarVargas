package com.example.desaappsavaloskoortuzarvargas.domain.repository

import com.example.desaappsavaloskoortuzarvargas.domain.model.DiscountedGame

interface DiscountRepository {
    suspend fun getCurrentDiscounts(): Result<List<DiscountedGame>>
    suspend fun getFavoriteDiscounts(favoriteGameIds: List<Int>): Result<List<DiscountedGame>>
    suspend fun getHistoricalLowDiscounts(): Result<List<DiscountedGame>>
    suspend fun getFreeGames(): Result<List<DiscountedGame>>
    suspend fun getDiscountsByPlatform(platform: String): Result<List<DiscountedGame>>
    suspend fun getPriceDrops(): Result<List<DiscountedGame>>
}

