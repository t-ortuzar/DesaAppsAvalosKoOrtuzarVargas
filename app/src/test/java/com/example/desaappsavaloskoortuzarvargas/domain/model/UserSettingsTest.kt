package com.example.desaappsavaloskoortuzarvargas.domain.model

import org.junit.Assert.*
import org.junit.Test

class UserSettingsTest {

    @Test
    fun `UserSettings has correct defaults`() {
        val settings = UserSettings()
        assertEquals("Player", settings.userName)
        assertEquals("", settings.email)
        assertEquals("Argentina", settings.country)
        assertEquals("AR", settings.countryCode)
        assertEquals("en", settings.languageCode)
        assertTrue(settings.globalNotificationsEnabled)
        assertTrue(settings.gameNotificationPrefs.isEmpty())
    }

    @Test
    fun `UserSettings can be constructed with custom values`() {
        val prefs = mapOf(1 to GameNotificationPref(1, "Game1"))
        val settings = UserSettings(
            userName = "TestUser",
            email = "test@test.com",
            country = "Chile",
            countryCode = "CL",
            languageCode = "es",
            globalNotificationsEnabled = false,
            gameNotificationPrefs = prefs
        )
        assertEquals("TestUser", settings.userName)
        assertEquals("test@test.com", settings.email)
        assertEquals("Chile", settings.country)
        assertEquals("CL", settings.countryCode)
        assertEquals("es", settings.languageCode)
        assertFalse(settings.globalNotificationsEnabled)
        assertEquals(1, settings.gameNotificationPrefs.size)
    }

    @Test
    fun `GameNotificationPref has correct defaults`() {
        val pref = GameNotificationPref(gameId = 1, gameName = "Test")
        assertEquals(1, pref.gameId)
        assertEquals("Test", pref.gameName)
        assertTrue(pref.notifyOffers)
        assertTrue(pref.notifyNews)
        assertTrue(pref.notifyHistoricalLow)
    }

    @Test
    fun `GameNotificationPref with custom values`() {
        val pref = GameNotificationPref(
            gameId = 5, gameName = "Game5",
            notifyOffers = false, notifyNews = false, notifyHistoricalLow = false
        )
        assertFalse(pref.notifyOffers)
        assertFalse(pref.notifyNews)
        assertFalse(pref.notifyHistoricalLow)
    }

    @Test
    fun `InAppNotification creation`() {
        val notif = InAppNotification(
            id = 1, gameId = 2, type = NotificationType.DISCOUNT,
            gameName = "TestGame", discountPercentage = 50,
            platform = "Steam", timestamp = 12345L
        )
        assertEquals(1, notif.id)
        assertEquals(2, notif.gameId)
        assertEquals(NotificationType.DISCOUNT, notif.type)
        assertEquals("TestGame", notif.gameName)
        assertEquals(50, notif.discountPercentage)
        assertEquals("Steam", notif.platform)
        assertEquals(12345L, notif.timestamp)
        assertFalse(notif.isRead)
    }

    @Test
    fun `InAppNotification with null gameId`() {
        val notif = InAppNotification(
            id = 1, gameId = null, type = NotificationType.NEWS,
            gameName = "News", discountPercentage = 0,
            platform = "All", timestamp = 100L, isRead = true
        )
        assertNull(notif.gameId)
        assertTrue(notif.isRead)
    }

    @Test
    fun `NotificationType enum values`() {
        val values = NotificationType.values()
        assertEquals(4, values.size)
        assertTrue(values.contains(NotificationType.DISCOUNT))
        assertTrue(values.contains(NotificationType.HISTORICAL_LOW))
        assertTrue(values.contains(NotificationType.NEWS))
        assertTrue(values.contains(NotificationType.FREE_GAME))
    }

    @Test
    fun `CountryInfo data class`() {
        val country = CountryInfo("Argentina", "AR", "ar", "USD")
        assertEquals("Argentina", country.name)
        assertEquals("AR", country.code)
        assertEquals("ar", country.steamCc)
        assertEquals("USD", country.currency)
    }

    @Test
    fun `SUPPORTED_COUNTRIES has 10 countries`() {
        assertEquals(10, SUPPORTED_COUNTRIES.size)
    }

    @Test
    fun `SUPPORTED_COUNTRIES contains Argentina as first`() {
        assertEquals("Argentina", SUPPORTED_COUNTRIES.first().name)
        assertEquals("AR", SUPPORTED_COUNTRIES.first().code)
    }

    @Test
    fun `SUPPORTED_COUNTRIES contains all expected countries`() {
        val names = SUPPORTED_COUNTRIES.map { it.name }
        assertTrue(names.contains("Argentina"))
        assertTrue(names.contains("Brasil"))
        assertTrue(names.contains("Chile"))
        assertTrue(names.contains("Colombia"))
        assertTrue(names.contains("México"))
        assertTrue(names.contains("Estados Unidos"))
        assertTrue(names.contains("España"))
        assertTrue(names.contains("Uruguay"))
        assertTrue(names.contains("Perú"))
        assertTrue(names.contains("Paraguay"))
    }

    @Test
    fun `countryCodeToFlag returns correct flag for AR`() {
        val flag = countryCodeToFlag("AR")
        assertNotEquals("", flag)
        assertEquals(4, flag.length) // Two regional indicator symbols, each is 2 chars in UTF-16
    }

    @Test
    fun `countryCodeToFlag returns correct flag for US`() {
        val flag = countryCodeToFlag("US")
        assertNotEquals("", flag)
    }

    @Test
    fun `countryCodeToFlag with lowercase input`() {
        val flag = countryCodeToFlag("ar")
        assertNotEquals("", flag)
        // Should produce same result as uppercase
        assertEquals(countryCodeToFlag("AR"), flag)
    }

    @Test
    fun `countryCodeToFlag with invalid length returns empty`() {
        assertEquals("", countryCodeToFlag("A"))
        assertEquals("", countryCodeToFlag("ABC"))
        assertEquals("", countryCodeToFlag(""))
    }

    @Test
    fun `ALL_TAGS has 27 tags`() {
        assertEquals(27, ALL_TAGS.size)
    }

    @Test
    fun `ALL_TAGS contains expected tags`() {
        assertTrue(ALL_TAGS.contains("Action"))
        assertTrue(ALL_TAGS.contains("RPG"))
        assertTrue(ALL_TAGS.contains("FPS"))
        assertTrue(ALL_TAGS.contains("Open World"))
        assertTrue(ALL_TAGS.contains("Free2Play"))
        assertTrue(ALL_TAGS.contains("Battle Royale"))
    }
}

