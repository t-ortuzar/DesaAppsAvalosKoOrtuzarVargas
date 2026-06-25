package com.example.desaappsavaloskoortuzarvargas.data.repository

import com.example.desaappsavaloskoortuzarvargas.data.api.PriceRefreshManager
import com.example.desaappsavaloskoortuzarvargas.data.catalog.GameCatalog
import com.example.desaappsavaloskoortuzarvargas.data.local.dao.GamePriceDao
import com.example.desaappsavaloskoortuzarvargas.data.local.entity.GamePriceEntity
import com.example.desaappsavaloskoortuzarvargas.domain.model.DiscountedGame
import com.example.desaappsavaloskoortuzarvargas.domain.model.OfferType
import com.example.desaappsavaloskoortuzarvargas.domain.repository.DiscountRepository

/**
 * Discount repository that reads REAL data from cached API prices.
 * No more mock discounts — all data comes from actual store APIs.
 */
class DiscountRepositoryImpl(
    private val gamePriceDao: GamePriceDao,
    private val priceRefreshManager: PriceRefreshManager
) : DiscountRepository {

    // Cache the game catalog for lookups
    private val catalogGames by lazy { GameCatalog.generateGames() }
    private val catalogByName by lazy { catalogGames.associateBy { it.name } }

    override suspend fun getCurrentDiscounts(): Result<List<DiscountedGame>> = try {
        val discountedPrices = gamePriceDao.getDiscountedPrices()
        val result = discountedPrices.mapNotNull { entity -> toDiscountedGame(entity) }
            .sortedByDescending { it.discountPercentage }
        Result.success(result)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun getFavoriteDiscounts(favoriteGameIds: List<Int>): Result<List<DiscountedGame>> = try {
        val favoriteNames = favoriteGameIds.mapNotNull { id ->
            catalogGames.firstOrNull { it.id == id }?.name
        }.toSet()
        val discountedPrices = gamePriceDao.getDiscountedPrices()
        val result = discountedPrices
            .filter { it.gameName in favoriteNames }
            .mapNotNull { entity -> toDiscountedGame(entity) }
            .sortedByDescending { it.discountPercentage }
        Result.success(result)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun getHistoricalLowDiscounts(): Result<List<DiscountedGame>> = try {
        val discountedPrices = gamePriceDao.getDiscountedPrices()
        val result = discountedPrices.mapNotNull { entity ->
            val game = toDiscountedGame(entity) ?: return@mapNotNull null
            val isHistLow = priceRefreshManager.isHistoricalLow(
                entity.gameName, entity.storeName, entity.currentPrice
            )
            if (isHistLow) game.copy(isHistoricalLowest = true) else null
        }.sortedByDescending { it.discountPercentage }
        Result.success(result)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun getFreeGames(): Result<List<DiscountedGame>> = try {
        // Temporarily free games from API (currently free, normally paid)
        val freePrices = gamePriceDao.getFreePrices()
        val tempFree = freePrices
            .filter { it.retailPrice > 0f } // Was paid, now free → temporarily free
            .mapNotNull { entity ->
                toDiscountedGame(entity)?.copy(
                    isFree = true,
                    isTemporarilyFree = true,
                    discountPercentage = 100,
                    offerType = OfferType.TEMPORARILY_FREE
                )
            }

        // F2P games from catalog (always free)
        val f2pGames = catalogGames.filter {
            it.currentPrices.isEmpty() && it.availablePlatforms.isNotEmpty()
        }.map { game ->
            val platform = game.availablePlatforms.firstOrNull() ?: "PC"
            val storeUrl = when {
                game.steamAppId > 0 -> "https://store.steampowered.com/app/${game.steamAppId}"
                platform == "Epic Games" -> "https://store.epicgames.com"
                platform == "Riot Games" -> "https://www.riotgames.com"
                platform == "HoYoverse" -> "https://www.hoyoverse.com"
                else -> ""
            }
            DiscountedGame(
                gameId = game.id,
                gameName = game.name,
                imageUrl = game.imageUrl,
                platform = platform,
                originalPrice = 0f,
                currentPrice = 0f,
                discountPercentage = 100,
                isFree = true,
                isF2P = true,
                isTemporarilyFree = false,
                offerType = OfferType.F2P,
                tags = game.tags,
                storeUrl = storeUrl
            )
        }

        Result.success(tempFree + f2pGames)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun getDiscountsByPlatform(platform: String): Result<List<DiscountedGame>> = try {
        val discountedPrices = gamePriceDao.getDiscountedPrices()
        val result = discountedPrices
            .filter { it.storeName.equals(platform, ignoreCase = true) }
            .mapNotNull { entity -> toDiscountedGame(entity) }
            .sortedByDescending { it.discountPercentage }
        Result.success(result)
    } catch (e: Exception) { Result.failure(e) }

    /**
     * Get games that had a permanent base price reduction (not a sale).
     */
    override suspend fun getPriceDrops(): Result<List<DiscountedGame>> = try {
        val allPrices = gamePriceDao.getAllPrices()
        val result = allPrices.mapNotNull { entity ->
            val previousRetail = priceRefreshManager.detectPriceDrop(
                entity.gameName, entity.storeName, entity.retailPrice
            ) ?: return@mapNotNull null
            toDiscountedGame(entity)?.copy(
                offerType = OfferType.PERMANENT_PRICE_DROP,
                previousBasePrice = previousRetail
            )
        }
        Result.success(result)
    } catch (e: Exception) { Result.failure(e) }

    /**
     * Convert a GamePriceEntity to a DiscountedGame using catalog data for metadata.
     */
    private suspend fun toDiscountedGame(entity: GamePriceEntity): DiscountedGame? {
        val catalogGame = catalogByName[entity.gameName] ?: return null
        val isHistLow = try {
            priceRefreshManager.isHistoricalLow(entity.gameName, entity.storeName, entity.currentPrice)
        } catch (_: Exception) { false }

        return DiscountedGame(
            gameId = catalogGame.id,
            gameName = entity.gameName,
            imageUrl = catalogGame.imageUrl,
            platform = entity.storeName,
            originalPrice = entity.retailPrice,
            currentPrice = entity.currentPrice,
            discountPercentage = entity.savings.toInt(),
            isFree = entity.currentPrice == 0f,
            isF2P = false,
            isTemporarilyFree = entity.currentPrice == 0f && entity.retailPrice > 0f,
            isHistoricalLowest = isHistLow,
            tags = catalogGame.tags,
            offerType = if (entity.currentPrice == 0f && entity.retailPrice > 0f) {
                OfferType.TEMPORARILY_FREE
            } else {
                OfferType.SALE
            },
            endTimestamp = entity.discountEndTimestamp,
            storeUrl = entity.dealUrl
        )
    }
}
