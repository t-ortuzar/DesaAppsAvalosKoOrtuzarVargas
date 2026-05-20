package com.example.desaappsavaloskoortuzarvargas.domain.usecase

import com.example.desaappsavaloskoortuzarvargas.domain.model.GameNotificationPref
import com.example.desaappsavaloskoortuzarvargas.domain.model.InAppNotification
import com.example.desaappsavaloskoortuzarvargas.domain.model.UserSettings
import com.example.desaappsavaloskoortuzarvargas.domain.repository.UserSettingsRepository

class GetUserSettingsUseCase(private val repo: UserSettingsRepository) {
    suspend operator fun invoke(): UserSettings = repo.getUserSettings()
}

class UpdateUserNameUseCase(private val repo: UserSettingsRepository) {
    suspend operator fun invoke(name: String) = repo.updateUserName(name)
}

class UpdateEmailUseCase(private val repo: UserSettingsRepository) {
    suspend operator fun invoke(email: String) = repo.updateEmail(email)
}

class UpdateCountryUseCase(private val repo: UserSettingsRepository) {
    suspend operator fun invoke(country: String, countryCode: String = "AR") = repo.updateCountry(country, countryCode)
}

class UpdateLanguageUseCase(private val repo: UserSettingsRepository) {
    suspend operator fun invoke(languageCode: String) = repo.updateLanguage(languageCode)
}

class SetGlobalNotificationsUseCase(private val repo: UserSettingsRepository) {
    suspend operator fun invoke(enabled: Boolean) = repo.setGlobalNotifications(enabled)
}

class UpdateGameNotificationPrefUseCase(private val repo: UserSettingsRepository) {
    suspend operator fun invoke(pref: GameNotificationPref) = repo.updateGameNotificationPref(pref)
}

class GetInAppNotificationsUseCase(private val repo: UserSettingsRepository) {
    suspend operator fun invoke(): List<InAppNotification> = repo.getInAppNotifications()
}

class GetUnreadNotificationCountUseCase(private val repo: UserSettingsRepository) {
    suspend operator fun invoke(): Int = repo.getUnreadNotificationCount()
}

class MarkNotificationReadUseCase(private val repo: UserSettingsRepository) {
    suspend operator fun invoke(notificationId: Int) = repo.markNotificationRead(notificationId)
}

class GenerateDiscountNotificationsUseCase(private val repo: UserSettingsRepository) {
    suspend operator fun invoke(favoriteGameIds: List<Int>) = repo.generateDiscountNotifications(favoriteGameIds)
}

