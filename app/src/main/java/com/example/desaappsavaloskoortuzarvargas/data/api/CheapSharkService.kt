package com.example.desaappsavaloskoortuzarvargas.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

@Serializable
data class CheapSharkDeal(
    val internalName: String = "",
    val title: String = "",
    val dealID: String = "",
    val storeID: String = "",
    val gameID: String = "",
    val salePrice: String = "0",
    val normalPrice: String = "0",
    val savings: String = "0",
    val metacriticScore: String = "0",
    val steamRatingPercent: String = "0",
    val releaseDate: Long = 0,
    val thumb: String = ""
)

@Serializable
data class CheapSharkGameLookup(
    val info: CheapSharkGameInfo? = null,
    val deals: List<CheapSharkStoreDeal> = emptyList()
)

@Serializable
data class CheapSharkGameInfo(
    val title: String = "",
    val steamAppID: String? = null,
    val thumb: String = ""
)

@Serializable
data class CheapSharkStoreDeal(
    val storeID: String = "",
    val dealID: String = "",
    val price: String = "0",
    val retailPrice: String = "0",
    val savings: String = "0"
)

@Serializable
data class CheapSharkGameSearchResult(
    val gameID: String = "",
    val steamAppID: String? = null,
    val cheapest: String = "0",
    val cheapestDealID: String? = null,
    val external: String = "",
    val internalName: String = "",
    val thumb: String = ""
)

data class GamePrice(
    val storeName: String,
    val currentPrice: Float,
    val retailPrice: Float,
    val savings: Float,
    val dealUrl: String
)

/**
 * Stores that are globally accessible or available in specific regions.
 * Stores NOT listed for a region are filtered out from results.
 * "global" stores are accessible from anywhere.
 */
object StoreRegionAvailability {
    // Stores accessible globally (digital keys, no region lock on purchase)
    private val globalStores = setOf(
        "Steam", "GOG", "Humble Store", "Fanatical", "IndieGala",
        "GreenManGaming", "Epic Games", "DreamGame", "GameBillet", "2Game"
    )

    // Stores with restricted regional availability
    private val regionRestricted = mapOf(
        "Origin (EA)" to setOf("US", "ES", "MX", "BR", "AR", "CL", "CO", "UY", "PE", "PY"),
        "Uplay (Ubisoft)" to setOf("US", "ES", "MX", "BR", "AR", "CL", "CO", "UY", "PE", "PY"),
        "Blizzard" to setOf("US", "ES", "MX", "BR", "AR", "CL", "CO", "UY", "PE", "PY"),
        "GamersGate" to setOf("US", "ES"),
        "Gamesplanet" to setOf("US", "ES"),
        "WinGameStore" to setOf("US"),
        "Gamesload" to setOf("US", "ES"),
        "DLGamer" to setOf("US", "ES", "MX", "BR"),
        "Noctre" to setOf("US", "ES"),
        "Voidu" to setOf("US", "ES")
    )

    fun isAvailableInRegion(storeName: String, countryCode: String): Boolean {
        if (storeName in globalStores) return true
        val allowedCountries = regionRestricted[storeName]
        return allowedCountries?.contains(countryCode) ?: true
    }
}

class CheapSharkService {

    private val json = Json { ignoreUnknownKeys = true }

    // CheapShark store ID → display name mapping
    private val storeNames = mapOf(
        "1" to "Steam",
        "2" to "GamersGate",
        "3" to "GreenManGaming",
        "7" to "GOG",
        "8" to "Origin (EA)",
        "11" to "Humble Store",
        "13" to "Uplay (Ubisoft)",
        "15" to "Fanatical",
        "21" to "WinGameStore",
        "23" to "GameBillet",
        "24" to "Voidu",
        "25" to "Epic Games",
        "27" to "Gamesplanet",
        "28" to "Gamesload",
        "29" to "2Game",
        "30" to "IndieGala",
        "31" to "Blizzard",
        "33" to "DLGamer",
        "34" to "Noctre",
        "35" to "DreamGame"
    )

    /**
     * Search for a game by name and return its CheapShark gameID
     */
    suspend fun searchGame(title: String): List<CheapSharkGameSearchResult> = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(title, "UTF-8")
            val url = URL("https://www.cheapshark.com/api/1.0/games?title=$encoded&limit=5")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000

            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().readText()
                json.decodeFromString<List<CheapSharkGameSearchResult>>(response)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get all current deals/prices for a specific game by CheapShark gameID
     */
    suspend fun getGameDeals(cheapSharkGameId: String): List<GamePrice> = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://www.cheapshark.com/api/1.0/games?id=$cheapSharkGameId")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000

            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().readText()
                val gameLookup = json.decodeFromString<CheapSharkGameLookup>(response)
                gameLookup.deals.mapNotNull { deal ->
                    val storeName = storeNames[deal.storeID] ?: "Store #${deal.storeID}"
                    val price = deal.price.toFloatOrNull() ?: return@mapNotNull null
                    val retail = deal.retailPrice.toFloatOrNull() ?: price
                    val savings = deal.savings.toFloatOrNull() ?: 0f
                    GamePrice(
                        storeName = storeName,
                        currentPrice = price,
                        retailPrice = retail,
                        savings = savings,
                        dealUrl = "https://www.cheapshark.com/redirect?dealID=${deal.dealID}"
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Search for best deals (top discounts across all stores)
     */
    suspend fun getTopDeals(limit: Int = 20): List<CheapSharkDeal> = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://www.cheapshark.com/api/1.0/deals?pageSize=$limit&sortBy=Savings&desc=0")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000

            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().readText()
                json.decodeFromString<List<CheapSharkDeal>>(response)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Search deals by game title
     */
    suspend fun searchDeals(title: String): List<CheapSharkDeal> = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(title, "UTF-8")
            val url = URL("https://www.cheapshark.com/api/1.0/deals?title=$encoded&pageSize=10")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000

            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().readText()
                json.decodeFromString<List<CheapSharkDeal>>(response)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getStoreName(storeId: String): String = storeNames[storeId] ?: "Store #$storeId"
}

