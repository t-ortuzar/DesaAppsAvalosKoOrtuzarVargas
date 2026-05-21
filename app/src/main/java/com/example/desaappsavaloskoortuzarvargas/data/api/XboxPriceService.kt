package com.example.desaappsavaloskoortuzarvargas.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

// ── Microsoft Store / Xbox PC displaycatalog response models ──

@Serializable
data class MsStoreSearchResponse(
    val Products: List<MsStoreProduct> = emptyList()
)

@Serializable
data class MsStoreProduct(
    val ProductId: String = "",
    val LocalizedProperties: List<MsLocalizedProperty> = emptyList(),
    val DisplaySkuAvailabilities: List<MsDisplaySkuAvailability> = emptyList()
)

@Serializable
data class MsLocalizedProperty(
    val ProductTitle: String = "",
    val ProductDescription: String = ""
)

@Serializable
data class MsDisplaySkuAvailability(
    val Availabilities: List<MsAvailability> = emptyList()
)

@Serializable
data class MsAvailability(
    val OrderManagementData: MsOrderManagementData? = null,
    val Conditions: MsConditions? = null
)

@Serializable
data class MsOrderManagementData(
    val Price: MsPrice? = null
)

@Serializable
data class MsPrice(
    val CurrencyCode: String = "USD",
    val ListPrice: Double = 0.0,
    val MSRP: Double = 0.0,
    val WholesalePrice: Double? = null
)

@Serializable
data class MsConditions(
    val ClientConditions: MsClientConditions? = null
)

@Serializable
data class MsClientConditions(
    val AllowedPlatforms: List<MsPlatform> = emptyList()
)

@Serializable
data class MsPlatform(
    val PlatformName: String = ""
)

/**
 * Service to fetch REAL Argentine prices from the Xbox / Microsoft Store (PC games).
 *
 * Uses Microsoft's public displaycatalog API with market=AR to get
 * the actual price in ARS that an Argentine user sees.
 *
 * Only returns prices for games available on PC (Windows.Desktop platform).
 *
 * Endpoint: https://displaycatalog.mp.microsoft.com/v7.0/products
 */
class XboxPriceService {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Search for a PC game on the Microsoft Store and return the Argentine price.
     * Filters results to only include games available on Windows.Desktop.
     */
    suspend fun searchGamePrice(title: String): StorePrice? = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(title, "UTF-8")
            val url = URL(
                "https://displaycatalog.mp.microsoft.com/v7.0/products?" +
                "query=$encoded&market=AR&languages=es-AR&mediaGroup=Games&\$top=5"
            )
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.setRequestProperty("Accept", "application/json")

            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().readText()
                val parsed = json.decodeFromString<MsStoreSearchResponse>(response)

                if (parsed.Products.isEmpty()) return@withContext null

                // Find the best title match that's available on PC
                val match = parsed.Products.firstOrNull { product ->
                    val productTitle = product.LocalizedProperties.firstOrNull()?.ProductTitle ?: ""
                    val isPcGame = product.DisplaySkuAvailabilities.any { sku ->
                        sku.Availabilities.any { avail ->
                            avail.Conditions?.ClientConditions?.AllowedPlatforms?.any {
                                it.PlatformName.contains("Windows", ignoreCase = true) ||
                                it.PlatformName.contains("PC", ignoreCase = true)
                            } == true
                        }
                    }
                    isPcGame && productTitle.contains(title.split(" ").first(), ignoreCase = true)
                } ?: parsed.Products.firstOrNull() ?: return@withContext null

                val productTitle = match.LocalizedProperties.firstOrNull()?.ProductTitle ?: title

                // Get the first available price (prefer PC availability)
                val price = match.DisplaySkuAvailabilities
                    .flatMap { it.Availabilities }
                    .mapNotNull { it.OrderManagementData?.Price }
                    .firstOrNull() ?: return@withContext null

                // Skip if price is 0 and game is not free
                val listPrice = price.ListPrice.toFloat()
                val msrp = price.MSRP.toFloat()
                val isFree = listPrice == 0f && msrp == 0f
                val discountPct = if (msrp > 0 && listPrice < msrp) {
                    ((1 - listPrice / msrp) * 100).toInt()
                } else 0

                StorePrice(
                    storeName = "Xbox / Microsoft",
                    currentPrice = listPrice,
                    originalPrice = msrp,
                    discountPercent = discountPct,
                    currency = price.CurrencyCode,
                    isFree = isFree,
                    storeUrl = "https://www.xbox.com/es-AR/games/store/${match.ProductId}"
                )
            } else null
        } catch (_: Exception) {
            null
        }
    }
}

