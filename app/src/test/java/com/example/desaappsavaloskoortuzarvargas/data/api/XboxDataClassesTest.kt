package com.example.desaappsavaloskoortuzarvargas.data.api

import org.junit.Assert.*
import org.junit.Test

class XboxDataClassesTest {

    // ── MsStoreSearchResponse ──

    @Test
    fun `MsStoreSearchResponse defaults to empty`() {
        val response = MsStoreSearchResponse()
        assertTrue(response.Products.isEmpty())
    }

    @Test
    fun `MsStoreSearchResponse with products`() {
        val product = MsStoreProduct(ProductId = "ABC123")
        val response = MsStoreSearchResponse(Products = listOf(product))
        assertEquals(1, response.Products.size)
        assertEquals("ABC123", response.Products[0].ProductId)
    }

    // ── MsStoreProduct ──

    @Test
    fun `MsStoreProduct defaults`() {
        val product = MsStoreProduct()
        assertEquals("", product.ProductId)
        assertTrue(product.LocalizedProperties.isEmpty())
        assertTrue(product.DisplaySkuAvailabilities.isEmpty())
    }

    @Test
    fun `MsStoreProduct with localized properties`() {
        val props = listOf(MsLocalizedProperty(ProductTitle = "Halo Infinite", ProductDescription = "FPS game"))
        val product = MsStoreProduct(ProductId = "HALO1", LocalizedProperties = props)
        assertEquals("Halo Infinite", product.LocalizedProperties[0].ProductTitle)
        assertEquals("FPS game", product.LocalizedProperties[0].ProductDescription)
    }

    // ── MsLocalizedProperty ──

    @Test
    fun `MsLocalizedProperty defaults`() {
        val prop = MsLocalizedProperty()
        assertEquals("", prop.ProductTitle)
        assertEquals("", prop.ProductDescription)
    }

    // ── MsDisplaySkuAvailability ──

    @Test
    fun `MsDisplaySkuAvailability defaults`() {
        val sku = MsDisplaySkuAvailability()
        assertTrue(sku.Availabilities.isEmpty())
    }

    @Test
    fun `MsDisplaySkuAvailability with availabilities`() {
        val avail = MsAvailability()
        val sku = MsDisplaySkuAvailability(Availabilities = listOf(avail))
        assertEquals(1, sku.Availabilities.size)
    }

    // ── MsAvailability ──

    @Test
    fun `MsAvailability defaults`() {
        val avail = MsAvailability()
        assertNull(avail.OrderManagementData)
        assertNull(avail.Conditions)
    }

    @Test
    fun `MsAvailability with order data`() {
        val price = MsPrice(CurrencyCode = "ARS", ListPrice = 8799.0, MSRP = 10999.0)
        val orderData = MsOrderManagementData(Price = price)
        val avail = MsAvailability(OrderManagementData = orderData)
        assertNotNull(avail.OrderManagementData)
        assertEquals(8799.0, avail.OrderManagementData!!.Price!!.ListPrice, 0.01)
    }

    // ── MsOrderManagementData ──

    @Test
    fun `MsOrderManagementData defaults`() {
        val data = MsOrderManagementData()
        assertNull(data.Price)
    }

    // ── MsPrice ──

    @Test
    fun `MsPrice defaults`() {
        val price = MsPrice()
        assertEquals("USD", price.CurrencyCode)
        assertEquals(0.0, price.ListPrice, 0.01)
        assertEquals(0.0, price.MSRP, 0.01)
        assertNull(price.WholesalePrice)
    }

    @Test
    fun `MsPrice with ARS values`() {
        val price = MsPrice(
            CurrencyCode = "ARS",
            ListPrice = 8799.0,
            MSRP = 10999.0,
            WholesalePrice = 7500.0
        )
        assertEquals("ARS", price.CurrencyCode)
        assertEquals(8799.0, price.ListPrice, 0.01)
        assertEquals(10999.0, price.MSRP, 0.01)
        assertEquals(7500.0, price.WholesalePrice!!, 0.01)
    }

    @Test
    fun `MsPrice free game`() {
        val price = MsPrice(CurrencyCode = "ARS", ListPrice = 0.0, MSRP = 0.0)
        assertEquals(0.0, price.ListPrice, 0.01)
        assertEquals(0.0, price.MSRP, 0.01)
    }

    // ── MsConditions / MsClientConditions / MsPlatform ──

    @Test
    fun `MsConditions defaults`() {
        val conditions = MsConditions()
        assertNull(conditions.ClientConditions)
    }

    @Test
    fun `MsClientConditions defaults`() {
        val cc = MsClientConditions()
        assertTrue(cc.AllowedPlatforms.isEmpty())
    }

    @Test
    fun `MsClientConditions with platforms`() {
        val platforms = listOf(
            MsPlatform(PlatformName = "Windows.Desktop"),
            MsPlatform(PlatformName = "Xbox.Console")
        )
        val cc = MsClientConditions(AllowedPlatforms = platforms)
        assertEquals(2, cc.AllowedPlatforms.size)
        assertEquals("Windows.Desktop", cc.AllowedPlatforms[0].PlatformName)
    }

    @Test
    fun `MsPlatform defaults`() {
        val platform = MsPlatform()
        assertEquals("", platform.PlatformName)
    }

    // ── Full chain test ──

    @Test
    fun `full product chain with price and conditions`() {
        val price = MsPrice(CurrencyCode = "ARS", ListPrice = 5999.0, MSRP = 7999.0)
        val orderData = MsOrderManagementData(Price = price)
        val platform = MsPlatform(PlatformName = "Windows.Desktop")
        val conditions = MsConditions(
            ClientConditions = MsClientConditions(AllowedPlatforms = listOf(platform))
        )
        val avail = MsAvailability(OrderManagementData = orderData, Conditions = conditions)
        val sku = MsDisplaySkuAvailability(Availabilities = listOf(avail))
        val prop = MsLocalizedProperty(ProductTitle = "Forza Horizon 5")
        val product = MsStoreProduct(
            ProductId = "FORZA5",
            LocalizedProperties = listOf(prop),
            DisplaySkuAvailabilities = listOf(sku)
        )

        assertEquals("FORZA5", product.ProductId)
        assertEquals("Forza Horizon 5", product.LocalizedProperties[0].ProductTitle)
        val actualPrice = product.DisplaySkuAvailabilities[0].Availabilities[0]
            .OrderManagementData!!.Price!!
        assertEquals("ARS", actualPrice.CurrencyCode)
        assertEquals(5999.0, actualPrice.ListPrice, 0.01)

        val actualPlatform = product.DisplaySkuAvailabilities[0].Availabilities[0]
            .Conditions!!.ClientConditions!!.AllowedPlatforms[0]
        assertEquals("Windows.Desktop", actualPlatform.PlatformName)
    }
}

