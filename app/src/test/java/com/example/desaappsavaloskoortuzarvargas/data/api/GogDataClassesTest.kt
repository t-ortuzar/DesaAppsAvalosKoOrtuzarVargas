package com.example.desaappsavaloskoortuzarvargas.data.api

import org.junit.Assert.*
import org.junit.Test

class GogDataClassesTest {

    @Test
    fun `GogCatalogResponse defaults to empty products`() {
        val response = GogCatalogResponse()
        assertTrue(response.products.isEmpty())
    }

    @Test
    fun `GogCatalogResponse with products`() {
        val product = GogProduct(id = 123, title = "The Witcher 3")
        val response = GogCatalogResponse(products = listOf(product))
        assertEquals(1, response.products.size)
        assertEquals("The Witcher 3", response.products[0].title)
    }

    @Test
    fun `GogProduct defaults`() {
        val product = GogProduct()
        assertEquals(0L, product.id)
        assertEquals("", product.title)
        assertNull(product.price)
        assertEquals("", product.storeLink)
    }

    @Test
    fun `GogProduct with values`() {
        val priceData = GogPriceData(
            amount = "29.99",
            baseAmount = "39.99",
            finalAmount = "29.99",
            isDiscounted = true,
            discountPercentage = 25,
            currency = "USD"
        )
        val product = GogProduct(
            id = 456,
            title = "Cyberpunk 2077",
            price = priceData,
            storeLink = "/game/cyberpunk_2077"
        )
        assertEquals(456L, product.id)
        assertEquals("Cyberpunk 2077", product.title)
        assertNotNull(product.price)
        assertEquals("/game/cyberpunk_2077", product.storeLink)
    }

    @Test
    fun `GogPriceData defaults`() {
        val price = GogPriceData()
        assertEquals("0", price.amount)
        assertEquals("0", price.baseAmount)
        assertEquals("0", price.finalAmount)
        assertFalse(price.isDiscounted)
        assertEquals(0, price.discountPercentage)
        assertEquals("USD", price.currency)
        assertFalse(price.isFree)
    }

    @Test
    fun `GogPriceData discounted game`() {
        val price = GogPriceData(
            amount = "29.99",
            baseAmount = "59.99",
            finalAmount = "29.99",
            isDiscounted = true,
            discountPercentage = 50,
            currency = "USD"
        )
        assertTrue(price.isDiscounted)
        assertEquals(50, price.discountPercentage)
        assertEquals("29.99", price.finalAmount)
    }

    @Test
    fun `GogPriceData free game`() {
        val price = GogPriceData(
            amount = "0",
            baseAmount = "0",
            finalAmount = "0",
            isFree = true,
            currency = "USD"
        )
        assertTrue(price.isFree)
        assertEquals("0", price.finalAmount)
    }

    @Test
    fun `GogPriceData with ARS currency`() {
        val price = GogPriceData(currency = "ARS")
        assertEquals("ARS", price.currency)
    }

    @Test
    fun `GogCatalogResponse with multiple products`() {
        val products = listOf(
            GogProduct(id = 1, title = "Game 1"),
            GogProduct(id = 2, title = "Game 2"),
            GogProduct(id = 3, title = "Game 3")
        )
        val response = GogCatalogResponse(products = products)
        assertEquals(3, response.products.size)
        assertEquals("Game 2", response.products[1].title)
    }
}

