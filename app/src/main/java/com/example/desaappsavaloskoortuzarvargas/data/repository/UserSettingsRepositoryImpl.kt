package com.example.desaappsavaloskoortuzarvargas.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.example.desaappsavaloskoortuzarvargas.data.local.SettingsKeys
import com.example.desaappsavaloskoortuzarvargas.data.local.settingsDataStore
import com.example.desaappsavaloskoortuzarvargas.domain.model.GameNotificationPref
import com.example.desaappsavaloskoortuzarvargas.domain.model.InAppNotification
import com.example.desaappsavaloskoortuzarvargas.domain.model.NotificationType
import com.example.desaappsavaloskoortuzarvargas.domain.model.UserSettings
import com.example.desaappsavaloskoortuzarvargas.domain.repository.DiscountRepository
import com.example.desaappsavaloskoortuzarvargas.domain.repository.UserSettingsRepository
import kotlinx.coroutines.flow.first

class UserSettingsRepositoryImpl(
    private val context: Context,
    private val discountRepository: DiscountRepository? = null
) : UserSettingsRepository {

    private var userSettings = UserSettings()
    private val notifications = mutableListOf<InAppNotification>()
    private val gamePrefs = mutableMapOf<Int, GameNotificationPref>()
    private var nextNotifId = 1

    override suspend fun getUserSettings(): UserSettings {
        val prefs = context.settingsDataStore.data.first()
        userSettings = UserSettings(
            userName = prefs[SettingsKeys.USER_NAME] ?: "Player",
            email = prefs[SettingsKeys.EMAIL] ?: "",
            country = prefs[SettingsKeys.COUNTRY] ?: "Argentina",
            countryCode = prefs[SettingsKeys.COUNTRY_CODE] ?: "AR",
            languageCode = prefs[SettingsKeys.LANGUAGE_CODE] ?: "en",
            globalNotificationsEnabled = prefs[SettingsKeys.GLOBAL_NOTIFICATIONS] ?: true,
            gameNotificationPrefs = gamePrefs.toMap(),
            darkMode = prefs[SettingsKeys.DARK_MODE] ?: true
        )
        return userSettings
    }

    override suspend fun updateUserName(name: String) {
        context.settingsDataStore.edit { it[SettingsKeys.USER_NAME] = name }
        userSettings = userSettings.copy(userName = name)
    }

    override suspend fun updateEmail(email: String) {
        context.settingsDataStore.edit { it[SettingsKeys.EMAIL] = email }
        userSettings = userSettings.copy(email = email)
    }

    override suspend fun updateCountry(country: String, countryCode: String) {
        context.settingsDataStore.edit {
            it[SettingsKeys.COUNTRY] = country
            it[SettingsKeys.COUNTRY_CODE] = countryCode
        }
        userSettings = userSettings.copy(country = country, countryCode = countryCode)
    }

    override suspend fun updateLanguage(languageCode: String) {
        context.settingsDataStore.edit { it[SettingsKeys.LANGUAGE_CODE] = languageCode }
        userSettings = userSettings.copy(languageCode = languageCode)
    }

    override suspend fun setGlobalNotifications(enabled: Boolean) {
        context.settingsDataStore.edit { it[SettingsKeys.GLOBAL_NOTIFICATIONS] = enabled }
        userSettings = userSettings.copy(globalNotificationsEnabled = enabled)
    }

    override suspend fun updateDarkMode(isDark: Boolean) {
        context.settingsDataStore.edit { it[SettingsKeys.DARK_MODE] = isDark }
        userSettings = userSettings.copy(darkMode = isDark)
    }

    override suspend fun updateGameNotificationPref(pref: GameNotificationPref) {
        gamePrefs[pref.gameId] = pref
        userSettings = userSettings.copy(gameNotificationPrefs = gamePrefs.toMap())
    }

    override suspend fun getGameNotificationPref(gameId: Int): GameNotificationPref? {
        return gamePrefs[gameId]
    }

    override suspend fun getInAppNotifications(): List<InAppNotification> {
        return notifications.sortedByDescending { it.timestamp }
    }

    override suspend fun markNotificationRead(notificationId: Int) {
        val idx = notifications.indexOfFirst { it.id == notificationId }
        if (idx != -1) {
            notifications[idx] = notifications[idx].copy(isRead = true)
        }
    }

    override suspend fun getUnreadNotificationCount(): Int {
        return notifications.count { !it.isRead }
    }

    override suspend fun generateDiscountNotifications(favoriteGameIds: List<Int>) {
        val discounts = discountRepository?.getCurrentDiscounts()?.getOrNull() ?: emptyList()
        val favoriteDiscounts = discounts.filter { it.gameId in favoriteGameIds && !it.isF2P }

        // Clear old auto-generated notifications
        notifications.removeAll { it.type == NotificationType.DISCOUNT || it.type == NotificationType.HISTORICAL_LOW }

        for (discount in favoriteDiscounts) {
            val pref = gamePrefs[discount.gameId]

            if (discount.isHistoricalLowest && (pref?.notifyHistoricalLow != false)) {
                notifications.add(
                    InAppNotification(
                        id = nextNotifId++,
                        gameId = discount.gameId,
                        type = NotificationType.HISTORICAL_LOW,
                        gameName = discount.gameName,
                        discountPercentage = discount.discountPercentage,
                        platform = discount.platform,
                        timestamp = System.currentTimeMillis()
                    )
                )
            } else if (pref?.notifyOffers != false) {
                notifications.add(
                    InAppNotification(
                        id = nextNotifId++,
                        gameId = discount.gameId,
                        type = NotificationType.DISCOUNT,
                        gameName = discount.gameName,
                        discountPercentage = discount.discountPercentage,
                        platform = discount.platform,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }
}
