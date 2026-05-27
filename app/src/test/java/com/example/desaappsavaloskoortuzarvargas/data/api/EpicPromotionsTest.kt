package com.example.desaappsavaloskoortuzarvargas.data.api

import org.junit.Assert.*
import org.junit.Test

/**
 * Extended tests for Epic Games data classes not covered by EpicDataClassesTest.
 */
class EpicPromotionsTest {

    @Test
    fun `EpicPromotions defaults`() {
        val promos = EpicPromotions()
        assertTrue(promos.promotionalOffers.isEmpty())
        assertTrue(promos.upcomingPromotionalOffers.isEmpty())
    }

    @Test
    fun `EpicPromotions with active offers`() {
        val detail = EpicPromotionDetail(
            startDate = "2026-05-20T15:00:00.000Z",
            endDate = "2026-06-03T15:00:00.000Z"
        )
        val offer = EpicPromotionalOffer(promotionalOffers = listOf(detail))
        val promos = EpicPromotions(promotionalOffers = listOf(offer))
        assertEquals(1, promos.promotionalOffers.size)
        assertEquals("2026-06-03T15:00:00.000Z",
            promos.promotionalOffers[0].promotionalOffers[0].endDate)
    }

    @Test
    fun `EpicPromotions with upcoming offers`() {
        val detail = EpicPromotionDetail(
            startDate = "2026-06-01T15:00:00.000Z",
            endDate = "2026-06-15T15:00:00.000Z"
        )
        val offer = EpicPromotionalOffer(promotionalOffers = listOf(detail))
        val promos = EpicPromotions(upcomingPromotionalOffers = listOf(offer))
        assertTrue(promos.promotionalOffers.isEmpty())
        assertEquals(1, promos.upcomingPromotionalOffers.size)
    }

    @Test
    fun `EpicPromotionalOffer defaults`() {
        val offer = EpicPromotionalOffer()
        assertTrue(offer.promotionalOffers.isEmpty())
    }

    @Test
    fun `EpicPromotionDetail defaults`() {
        val detail = EpicPromotionDetail()
        assertEquals("", detail.startDate)
        assertEquals("", detail.endDate)
    }

    @Test
    fun `EpicPriceInfo defaults`() {
        val info = EpicPriceInfo()
        assertNull(info.totalPrice)
    }

    @Test
    fun `EpicPriceInfo with totalPrice`() {
        val total = EpicTotalPrice(
            originalPrice = 879900,
            discountPrice = 439900,
            discount = 440000,
            currencyCode = "ARS"
        )
        val info = EpicPriceInfo(totalPrice = total)
        assertNotNull(info.totalPrice)
        assertEquals(879900, info.totalPrice!!.originalPrice)
        assertEquals(439900, info.totalPrice!!.discountPrice)
    }

    @Test
    fun `EpicCatalog defaults`() {
        val catalog = EpicCatalog()
        assertNull(catalog.searchStore)
    }

    @Test
    fun `EpicCatalog with searchStore`() {
        val store = EpicSearchStore(elements = emptyList())
        val catalog = EpicCatalog(searchStore = store)
        assertNotNull(catalog.searchStore)
        assertTrue(catalog.searchStore!!.elements.isEmpty())
    }

    @Test
    fun `EpicElement with promotions`() {
        val detail = EpicPromotionDetail(
            startDate = "2026-05-20T00:00:00.000Z",
            endDate = "2026-05-30T00:00:00.000Z"
        )
        val promos = EpicPromotions(
            promotionalOffers = listOf(EpicPromotionalOffer(listOf(detail)))
        )
        val element = EpicElement(
            title = "Discounted Game",
            urlSlug = "discounted-game",
            promotions = promos
        )
        assertNotNull(element.promotions)
        assertEquals(1, element.promotions!!.promotionalOffers.size)
    }

    @Test
    fun `EpicTotalPrice with formatted prices`() {
        val fmt = EpicFmtPrice(
            originalPrice = "ARS$ 8.799,00",
            discountPrice = "ARS$ 4.399,00"
        )
        val total = EpicTotalPrice(
            originalPrice = 879900,
            discountPrice = 439900,
            discount = 440000,
            currencyCode = "ARS",
            fmtPrice = fmt
        )
        assertNotNull(total.fmtPrice)
        assertEquals("ARS$ 8.799,00", total.fmtPrice!!.originalPrice)
        assertEquals("ARS$ 4.399,00", total.fmtPrice!!.discountPrice)
    }

    @Test
    fun `EpicTotalPrice free game`() {
        val total = EpicTotalPrice(
            originalPrice = 0,
            discountPrice = 0,
            discount = 0,
            currencyCode = "ARS"
        )
        assertEquals(0, total.originalPrice)
        assertEquals(0, total.discountPrice)
    }

    @Test
    fun `EpicElement null promotions`() {
        val element = EpicElement(title = "Game", promotions = null)
        assertNull(element.promotions)
    }
}

