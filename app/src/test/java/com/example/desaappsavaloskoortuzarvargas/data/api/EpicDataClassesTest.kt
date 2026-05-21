package com.example.desaappsavaloskoortuzarvargas.data.api

import org.junit.Assert.*
import org.junit.Test

class EpicDataClassesTest {

    @Test
    fun `EpicKeyImage defaults`() {
        val img = EpicKeyImage()
        assertEquals("", img.type)
        assertEquals("", img.url)
    }

    @Test
    fun `EpicKeyImage with values`() {
        val img = EpicKeyImage(type = "DieselStoreFrontWide", url = "https://example.com/img.jpg")
        assertEquals("DieselStoreFrontWide", img.type)
        assertEquals("https://example.com/img.jpg", img.url)
    }

    @Test
    fun `EpicElement defaults`() {
        val elem = EpicElement()
        assertEquals("", elem.title)
        assertEquals("", elem.urlSlug)
        assertNull(elem.price)
        assertTrue(elem.keyImages.isEmpty())
    }

    @Test
    fun `EpicElement with keyImages`() {
        val elem = EpicElement(
            title = "Alan Wake 2",
            urlSlug = "alan-wake-2",
            keyImages = listOf(
                EpicKeyImage("DieselStoreFrontWide", "https://cdn.epicgames.com/wide.jpg"),
                EpicKeyImage("Thumbnail", "https://cdn.epicgames.com/thumb.jpg")
            )
        )
        assertEquals("Alan Wake 2", elem.title)
        assertEquals(2, elem.keyImages.size)
        assertEquals("DieselStoreFrontWide", elem.keyImages[0].type)
    }

    @Test
    fun `EpicTotalPrice defaults`() {
        val price = EpicTotalPrice()
        assertEquals(0, price.originalPrice)
        assertEquals(0, price.discountPrice)
        assertEquals(0, price.discount)
        assertEquals("ARS", price.currencyCode)
        assertNull(price.fmtPrice)
    }

    @Test
    fun `EpicFmtPrice defaults`() {
        val fmt = EpicFmtPrice()
        assertEquals("", fmt.originalPrice)
        assertEquals("", fmt.discountPrice)
    }

    @Test
    fun `EpicGraphQLResponse defaults`() {
        val resp = EpicGraphQLResponse()
        assertNull(resp.data)
    }

    @Test
    fun `EpicCatalogData defaults`() {
        val data = EpicCatalogData()
        assertNull(data.Catalog)
    }

    @Test
    fun `EpicSearchStore defaults`() {
        val store = EpicSearchStore()
        assertTrue(store.elements.isEmpty())
    }

    @Test
    fun `full response chain`() {
        val response = EpicGraphQLResponse(
            data = EpicCatalogData(
                Catalog = EpicCatalog(
                    searchStore = EpicSearchStore(
                        elements = listOf(
                            EpicElement(
                                title = "Fortnite",
                                urlSlug = "fortnite",
                                keyImages = listOf(EpicKeyImage("Thumbnail", "https://img.com/fn.jpg")),
                                price = EpicPriceInfo(
                                    totalPrice = EpicTotalPrice(
                                        originalPrice = 0,
                                        discountPrice = 0,
                                        currencyCode = "ARS"
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
        val elems = response.data?.Catalog?.searchStore?.elements
        assertNotNull(elems)
        assertEquals(1, elems!!.size)
        assertEquals("Fortnite", elems[0].title)
        assertEquals(0, elems[0].price?.totalPrice?.originalPrice)
    }
}

