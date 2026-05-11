package com.example.desaappsavaloskoortuzarvargas.data.repository

import com.example.desaappsavaloskoortuzarvargas.data.mock.MockDataGenerator
import com.example.desaappsavaloskoortuzarvargas.domain.model.GameNotificationPref
import com.example.desaappsavaloskoortuzarvargas.domain.model.InAppNotification
import com.example.desaappsavaloskoortuzarvargas.domain.model.NotificationType
import com.example.desaappsavaloskoortuzarvargas.domain.model.UserSettings
import com.example.desaappsavaloskoortuzarvargas.domain.repository.UserSettingsRepository

class UserSettingsRepositoryImpl : UserSettingsRepository {

    private var userSettings = UserSettings()
    private val notifications = mutableListOf<InAppNotification>()
    private val gamePrefs = mutableMapOf<Int, GameNotificationPref>()
    private var nextNotifId = 1

    override suspend fun getUserSettings(): UserSettings = userSettings

    override suspend fun updateUserName(name: String) {
        userSettings = userSettings.copy(userName = name)
    }

    override suspend fun updateEmail(email: String) {
        userSettings = userSettings.copy(email = email)
    }

    override suspend fun updateCountry(country: String, countryCode: String) {
        userSettings = userSettings.copy(country = country, countryCode = countryCode)
    }

    override suspend fun setGlobalNotifications(enabled: Boolean) {
        userSettings = userSettings.copy(globalNotificationsEnabled = enabled)
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
        val discounts = MockDataGenerator.generateDiscounts()
        val favoriteDiscounts = discounts.filter { it.gameId in favoriteGameIds && !it.isF2P }

        // Clear old auto-generated notifications
        notifications.removeAll { it.type == NotificationType.DISCOUNT || it.type == NotificationType.HISTORICAL_LOW }

        for (discount in favoriteDiscounts) {
            val pref = gamePrefs[discount.gameId]

            if (discount.isHistoricalLowest && (pref?.notifyHistoricalLow != false)) {
                notifications.add(
                    InAppNotification(
                        id = nextNotifId++,
                        title = "🔥 Historical Low!",
                        message = "${discount.gameName} is at its lowest price ever: -${discount.discountPercentage}% on ${discount.platform}!",
                        gameId = discount.gameId,
                        type = NotificationType.HISTORICAL_LOW
                    )
                )
            } else if (pref?.notifyOffers != false) {
                notifications.add(
                    InAppNotification(
                        id = nextNotifId++,
                        title = "💰 Discount Alert",
                        message = "${discount.gameName} is ${discount.discountPercentage}% off on ${discount.platform}!",
                        gameId = discount.gameId,
                        type = NotificationType.DISCOUNT
                    )
                )
            }
        }
    }
}

