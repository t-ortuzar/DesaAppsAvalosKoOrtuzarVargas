package com.example.desaappsavaloskoortuzarvargas.data.repository

import com.example.desaappsavaloskoortuzarvargas.data.mock.MockDataGenerator
import com.example.desaappsavaloskoortuzarvargas.domain.model.DiscountedGame
import com.example.desaappsavaloskoortuzarvargas.domain.repository.DiscountRepository

class DiscountRepositoryImpl : DiscountRepository {

    private val allDiscounts = MockDataGenerator.generateDiscounts()

    override suspend fun getCurrentDiscounts(): Result<List<DiscountedGame>> = try {
        Result.success(allDiscounts.filter { !it.isFree }.sortedByDescending { it.discountPercentage })
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun getFavoriteDiscounts(favoriteGameIds: List<Int>): Result<List<DiscountedGame>> = try {
        Result.success(allDiscounts.filter { it.gameId in favoriteGameIds }.sortedByDescending { it.discountPercentage })
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun getHistoricalLowDiscounts(): Result<List<DiscountedGame>> = try {
        Result.success(allDiscounts.filter { it.isHistoricalLowest }.sortedByDescending { it.discountPercentage })
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun getFreeGames(): Result<List<DiscountedGame>> = try {
        Result.success(allDiscounts.filter { it.isFree })
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun getDiscountsByPlatform(platform: String): Result<List<DiscountedGame>> = try {
        Result.success(allDiscounts.filter { it.platform == platform }.sortedByDescending { it.discountPercentage })
    } catch (e: Exception) { Result.failure(e) }
}
