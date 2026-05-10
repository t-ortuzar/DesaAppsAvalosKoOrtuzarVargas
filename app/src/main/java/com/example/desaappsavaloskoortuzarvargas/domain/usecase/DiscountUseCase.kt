package com.example.desaappsavaloskoortuzarvargas.domain.usecase

import com.example.desaappsavaloskoortuzarvargas.domain.model.DiscountedGame
import com.example.desaappsavaloskoortuzarvargas.domain.repository.DiscountRepository

class GetCurrentDiscountsUseCase(private val discountRepository: DiscountRepository) {
    suspend operator fun invoke(): Result<List<DiscountedGame>> = discountRepository.getCurrentDiscounts()
}

class GetFavoriteDiscountsUseCase(private val discountRepository: DiscountRepository) {
    suspend operator fun invoke(favoriteGameIds: List<Int>): Result<List<DiscountedGame>> =
        discountRepository.getFavoriteDiscounts(favoriteGameIds)
}

class GetHistoricalLowDiscountsUseCase(private val discountRepository: DiscountRepository) {
    suspend operator fun invoke(): Result<List<DiscountedGame>> = discountRepository.getHistoricalLowDiscounts()
}

class GetFreeGamesUseCase(private val discountRepository: DiscountRepository) {
    suspend operator fun invoke(): Result<List<DiscountedGame>> = discountRepository.getFreeGames()
}

class GetDiscountsByPlatformUseCase(private val discountRepository: DiscountRepository) {
    suspend operator fun invoke(platform: String): Result<List<DiscountedGame>> =
        discountRepository.getDiscountsByPlatform(platform)
}
