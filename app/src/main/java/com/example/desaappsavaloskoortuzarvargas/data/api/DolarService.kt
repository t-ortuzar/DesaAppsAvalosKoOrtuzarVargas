package com.example.desaappsavaloskoortuzarvargas.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class DolarCotizacion(
    val moneda: String = "",
    val casa: String = "",
    val nombre: String = "",
    val compra: Double = 0.0,
    val venta: Double = 0.0,
    val fechaActualizacion: String = ""
)

/**
 * Service to fetch the Argentine dollar exchange rates from DolarAPI.
 * Uses https://dolarapi.com/v1/dolares/tarjeta for the "dólar tarjeta"
 * which includes IVA (21%) + Percepción Ganancias (30%) + PAIS tax.
 *
 * This is the same rate Steamcito uses to calculate final prices
 * for Argentines paying with credit/debit card.
 */
class DolarService {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Get the "dólar tarjeta" sell rate (venta).
     * This is the effective rate for digital purchases with Argentine cards.
     * Returns null if the API call fails.
     */
    suspend fun getDolarTarjeta(): DolarCotizacion? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://dolarapi.com/v1/dolares/tarjeta")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000

            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().readText()
                json.decodeFromString<DolarCotizacion>(response)
            } else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Get all dollar exchange rates.
     */
    suspend fun getAllRates(): List<DolarCotizacion> = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://dolarapi.com/v1/dolares")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000

            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().readText()
                json.decodeFromString<List<DolarCotizacion>>(response)
            } else emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}

