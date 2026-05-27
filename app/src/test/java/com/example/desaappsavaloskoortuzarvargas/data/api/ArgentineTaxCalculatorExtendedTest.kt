package com.example.desaappsavaloskoortuzarvargas.data.api

import org.junit.Assert.*
import org.junit.Test

/**
 * Extended tests for ArgentineTaxCalculator covering additional edge cases.
 */
class ArgentineTaxCalculatorExtendedTest {

    @Test
    fun `usdToArs with very small price`() {
        val result = ArgentineTaxCalculator.usdToArs(0.01f, 1500.0)
        assertEquals(15f, result, 0.1f)
    }

    @Test
    fun `usdToArs with large price`() {
        val result = ArgentineTaxCalculator.usdToArs(69.99f, 1840.0)
        assertEquals(69.99f * 1840f, result, 1f)
    }

    @Test
    fun `formatArs small value`() {
        val result = ArgentineTaxCalculator.formatArs(1.5f)
        assertTrue(result.startsWith("ARS $"))
    }

    @Test
    fun `formatArs very large value`() {
        val result = ArgentineTaxCalculator.formatArs(999999.99f)
        assertTrue(result.startsWith("ARS $"))
        assertTrue(result.contains("."))
    }

    @Test
    fun `formatArs single digit`() {
        val result = ArgentineTaxCalculator.formatArs(5f)
        assertEquals("ARS $5,00", result)
    }

    @Test
    fun `formatArs hundreds`() {
        val result = ArgentineTaxCalculator.formatArs(150f)
        assertEquals("ARS $150,00", result)
    }

    @Test
    fun `formatArs thousands`() {
        val result = ArgentineTaxCalculator.formatArs(1500f)
        assertEquals("ARS $1.500,00", result)
    }

    @Test
    fun `formatArs ten thousands`() {
        val result = ArgentineTaxCalculator.formatArs(10999f)
        assertEquals("ARS $10.999,00", result)
    }

    @Test
    fun `calculateBreakdown with zero price`() {
        val breakdown = ArgentineTaxCalculator.calculateBreakdown(0f, 1000.0)
        assertEquals(0f, breakdown.baseUsd, 0.01f)
        assertEquals(0f, breakdown.baseArs, 0.01f)
        assertEquals(0f, breakdown.iva, 0.01f)
        assertEquals(0f, breakdown.percGanancias, 0.01f)
        assertEquals(0f, breakdown.totalArs, 0.01f)
    }

    @Test
    fun `calculateBreakdown totalArs is sum of components`() {
        val breakdown = ArgentineTaxCalculator.calculateBreakdown(50f, 1000.0)
        val expectedTotal = breakdown.baseArs + breakdown.iva + breakdown.percGanancias
        assertEquals(expectedTotal, breakdown.totalArs, 0.01f)
    }

    @Test
    fun `calculateBreakdown iva is 21 percent`() {
        val breakdown = ArgentineTaxCalculator.calculateBreakdown(100f, 1000.0)
        assertEquals(breakdown.baseArs * 0.21f, breakdown.iva, 0.01f)
    }

    @Test
    fun `calculateBreakdown percGanancias is 30 percent`() {
        val breakdown = ArgentineTaxCalculator.calculateBreakdown(100f, 1000.0)
        assertEquals(breakdown.baseArs * 0.30f, breakdown.percGanancias, 0.01f)
    }

    @Test
    fun `TaxBreakdown data class fields`() {
        val breakdown = ArgentineTaxCalculator.TaxBreakdown(
            baseUsd = 10f,
            dolarOficial = 1000.0,
            baseArs = 10000f,
            iva = 2100f,
            percGanancias = 3000f,
            totalArs = 15100f
        )
        assertEquals(10f, breakdown.baseUsd, 0.01f)
        assertEquals(1000.0, breakdown.dolarOficial, 0.01)
        assertEquals(10000f, breakdown.baseArs, 0.01f)
        assertEquals(2100f, breakdown.iva, 0.01f)
        assertEquals(3000f, breakdown.percGanancias, 0.01f)
        assertEquals(15100f, breakdown.totalArs, 0.01f)
    }

    @Test
    fun `TaxBreakdown equality`() {
        val a = ArgentineTaxCalculator.calculateBreakdown(10f, 1000.0)
        val b = ArgentineTaxCalculator.calculateBreakdown(10f, 1000.0)
        assertEquals(a, b)
    }
}

