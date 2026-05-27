package com.example.desaappsavaloskoortuzarvargas.data.api

import org.junit.Assert.*
import org.junit.Test

class DolarCotizacionTest {

    @Test
    fun `DolarCotizacion defaults`() {
        val cot = DolarCotizacion()
        assertEquals("", cot.moneda)
        assertEquals("", cot.casa)
        assertEquals("", cot.nombre)
        assertEquals(0.0, cot.compra, 0.01)
        assertEquals(0.0, cot.venta, 0.01)
        assertEquals("", cot.fechaActualizacion)
    }

    @Test
    fun `DolarCotizacion with values`() {
        val cot = DolarCotizacion(
            moneda = "USD",
            casa = "tarjeta",
            nombre = "Tarjeta",
            compra = 1700.0,
            venta = 1840.0,
            fechaActualizacion = "2026-05-27T12:00:00.000Z"
        )
        assertEquals("USD", cot.moneda)
        assertEquals("tarjeta", cot.casa)
        assertEquals("Tarjeta", cot.nombre)
        assertEquals(1700.0, cot.compra, 0.01)
        assertEquals(1840.0, cot.venta, 0.01)
        assertEquals("2026-05-27T12:00:00.000Z", cot.fechaActualizacion)
    }

    @Test
    fun `DolarCotizacion copy creates independent instance`() {
        val original = DolarCotizacion(moneda = "USD", venta = 1840.0)
        val copy = original.copy(venta = 1900.0)
        assertEquals(1840.0, original.venta, 0.01)
        assertEquals(1900.0, copy.venta, 0.01)
    }

    @Test
    fun `DolarCotizacion equality`() {
        val a = DolarCotizacion(moneda = "USD", venta = 1840.0)
        val b = DolarCotizacion(moneda = "USD", venta = 1840.0)
        assertEquals(a, b)
    }

    @Test
    fun `DolarCotizacion inequality`() {
        val a = DolarCotizacion(moneda = "USD", venta = 1840.0)
        val b = DolarCotizacion(moneda = "USD", venta = 1900.0)
        assertNotEquals(a, b)
    }
}

