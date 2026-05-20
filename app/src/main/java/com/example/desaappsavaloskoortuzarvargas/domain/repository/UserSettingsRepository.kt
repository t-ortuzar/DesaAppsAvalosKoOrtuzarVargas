package com.example.desaappsavaloskoortuzarvargas.domain.repository

import com.example.desaappsavaloskoortuzarvargas.domain.model.GameNotificationPref
import com.example.desaappsavaloskoortuzarvargas.domain.model.InAppNotification
import com.example.desaappsavaloskoortuzarvargas.domain.model.UserSettings

interface UserSettingsRepository {
    suspend fun getUserSettings(): UserSettings
    suspend fun updateUserName(name: String)
    suspend fun updateEmail(email: String)
    suspend fun updateCountry(country: String, countryCode: String = "AR")
    suspend fun updateLanguage(languageCode: String)
    suspend fun setGlobalNotifications(enabled: Boolean)
    suspend fun updateGameNotificationPref(pref: GameNotificationPref)
    suspend fun getGameNotificationPref(gameId: Int): GameNotificationPref?
    suspend fun getInAppNotifications(): List<InAppNotification>
    suspend fun markNotificationRead(notificationId: Int)
    suspend fun getUnreadNotificationCount(): Int
    suspend fun generateDiscountNotifications(favoriteGameIds: List<Int>)
}

