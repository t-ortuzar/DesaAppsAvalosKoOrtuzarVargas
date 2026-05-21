package com.example.desaappsavaloskoortuzarvargas.data.api

/**
 * Calculates the final price in ARS for Argentine digital purchases,
 * following the same model as Steamcito (https://steamcito.com.ar).
 *
 * When paying with credit/debit card in Argentina, digital purchases
 * are subject to multiple taxes on top of the base USD price:
 *
 * The "dólar tarjeta" rate from DolarAPI already includes:
 *   - Official exchange rate
 *   - IVA (21%)
 *   - Percepción Ganancias (30%)
 *   - Impuesto PAIS (if applicable)
 *
 * So the conversion is simply: USD price × dólar tarjeta sell rate.
 */
object ArgentineTaxCalculator {

    // Fallback rate if API is unavailable (should be updated periodically)
    private const val FALLBACK_DOLAR_TARJETA = 1840.0

    /**
     * Convert USD price to ARS using the "dólar tarjeta" rate.
     * This gives the final price an Argentine pays with their card.
     *
     * @param usdPrice The price in US dollars
     * @param dolarTarjetaVenta The "dólar tarjeta" sell rate (from DolarAPI)
     * @return The final price in ARS
     */
    fun usdToArs(usdPrice: Float, dolarTarjetaVenta: Double?): Float {
        val rate = dolarTarjetaVenta ?: FALLBACK_DOLAR_TARJETA
        return (usdPrice * rate).toFloat()
    }

    /**
     * Format an ARS price for display (e.g., "ARS $87.838,05").
     * Uses Argentine locale formatting (dot for thousands, comma for decimals).
     */
    fun formatArs(arsPrice: Float): String {
        val wholePart = arsPrice.toLong()
        val decimalPart = ((arsPrice - wholePart) * 100).toInt()
        val formatted = wholePart.toString()
            .reversed()
            .chunked(3)
            .joinToString(".")
            .reversed()
        return "ARS $$formatted,${String.format("%02d", decimalPart)}"
    }

    /**
     * Breakdown of the tax components for educational display.
     */
    data class TaxBreakdown(
        val baseUsd: Float,
        val dolarOficial: Double,
        val baseArs: Float,         // USD × official rate
        val iva: Float,             // 21% of baseArs
        val percGanancias: Float,   // 30% of baseArs
        val totalArs: Float
    )

    /**
     * Calculate detailed tax breakdown (for informational purposes).
     * Note: The dólar tarjeta rate already includes all taxes,
     * but this breaks them down for transparency.
     */
    fun calculateBreakdown(usdPrice: Float, dolarOficial: Double): TaxBreakdown {
        val baseArs = (usdPrice * dolarOficial).toFloat()
        val iva = baseArs * 0.21f
        val percGanancias = baseArs * 0.30f
        val totalArs = baseArs + iva + percGanancias
        return TaxBreakdown(
            baseUsd = usdPrice,
            dolarOficial = dolarOficial,
            baseArs = baseArs,
            iva = iva,
            percGanancias = percGanancias,
            totalArs = totalArs
        )
    }
}

