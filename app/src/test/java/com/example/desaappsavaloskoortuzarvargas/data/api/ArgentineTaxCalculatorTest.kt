package com.example.desaappsavaloskoortuzarvargas.data.api

import org.junit.Assert.*
import org.junit.Test

class ArgentineTaxCalculatorTest {

    @Test
    fun `usdToArs with known rate`() {
        val result = ArgentineTaxCalculator.usdToArs(10f, 1500.0)
        assertEquals(15000f, result, 0.01f)
    }

    @Test
    fun `usdToArs with null rate uses fallback`() {
        val result = ArgentineTaxCalculator.usdToArs(10f, null)
        assertEquals(18400f, result, 0.01f) // fallback = 1840.0
    }

    @Test
    fun `usdToArs with zero price returns zero`() {
        val result = ArgentineTaxCalculator.usdToArs(0f, 1500.0)
        assertEquals(0f, result, 0.01f)
    }

    @Test
    fun `formatArs formats correctly`() {
        val result = ArgentineTaxCalculator.formatArs(8799f)
        assertEquals("ARS $8.799,00", result)
    }

    @Test
    fun `formatArs formats large numbers`() {
        val result = ArgentineTaxCalculator.formatArs(123456.78f)
        assertTrue(result.startsWith("ARS $"))
        assertTrue(result.contains("."))
    }

    @Test
    fun `formatArs formats zero`() {
        val result = ArgentineTaxCalculator.formatArs(0f)
        assertEquals("ARS $0,00", result)
    }

    @Test
    fun `calculateBreakdown returns correct components`() {
        val breakdown = ArgentineTaxCalculator.calculateBreakdown(10f, 1000.0)
        assertEquals(10f, breakdown.baseUsd, 0.01f)
        assertEquals(10000f, breakdown.baseArs, 0.01f) // 10 * 1000
        assertEquals(2100f, breakdown.iva, 0.01f) // 21% of 10000
        assertEquals(3000f, breakdown.percGanancias, 0.01f) // 30% of 10000
        assertEquals(15100f, breakdown.totalArs, 0.01f) // 10000 + 2100 + 3000
    }
}

