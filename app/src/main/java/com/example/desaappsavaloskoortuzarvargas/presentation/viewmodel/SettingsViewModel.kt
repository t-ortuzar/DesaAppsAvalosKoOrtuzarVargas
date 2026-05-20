package com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.desaappsavaloskoortuzarvargas.domain.model.GameNotificationPref
import com.example.desaappsavaloskoortuzarvargas.domain.model.InAppNotification
import com.example.desaappsavaloskoortuzarvargas.domain.model.UserSettings
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GenerateDiscountNotificationsUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetFavoritesUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetInAppNotificationsUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetUnreadNotificationCountUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetUserSettingsUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.MarkNotificationReadUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.SetGlobalNotificationsUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.UpdateCountryUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.UpdateEmailUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.UpdateGameNotificationPrefUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.UpdateLanguageUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.UpdateUserNameUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val getUserSettingsUseCase: GetUserSettingsUseCase,
    private val updateUserNameUseCase: UpdateUserNameUseCase,
    private val updateEmailUseCase: UpdateEmailUseCase,
    private val updateCountryUseCase: UpdateCountryUseCase,
    private val updateLanguageUseCase: UpdateLanguageUseCase,
    private val setGlobalNotificationsUseCase: SetGlobalNotificationsUseCase,
    private val updateGameNotificationPrefUseCase: UpdateGameNotificationPrefUseCase,
    private val getInAppNotificationsUseCase: GetInAppNotificationsUseCase,
    private val getUnreadNotificationCountUseCase: GetUnreadNotificationCountUseCase,
    private val markNotificationReadUseCase: MarkNotificationReadUseCase,
    private val generateDiscountNotificationsUseCase: GenerateDiscountNotificationsUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase
) : ViewModel() {

    private val _userSettings = MutableStateFlow(UserSettings())
    val userSettings: StateFlow<UserSettings> = _userSettings.asStateFlow()

    private val _notifications = MutableStateFlow<List<InAppNotification>>(emptyList())
    val notifications: StateFlow<List<InAppNotification>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    init {
        loadSettings()
        refreshNotifications()
    }

    fun loadSettings() {
        viewModelScope.launch {
            _userSettings.value = getUserSettingsUseCase()
        }
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            updateUserNameUseCase(name)
            _userSettings.value = getUserSettingsUseCase()
        }
    }

    fun updateEmail(email: String) {
        viewModelScope.launch {
            updateEmailUseCase(email)
            _userSettings.value = getUserSettingsUseCase()
        }
    }

    fun updateCountry(country: String, countryCode: String = "AR") {
        viewModelScope.launch {
            updateCountryUseCase(country, countryCode)
            _userSettings.value = getUserSettingsUseCase()
        }
    }

    fun updateLanguage(languageCode: String) {
        viewModelScope.launch {
            updateLanguageUseCase(languageCode)
            _userSettings.value = getUserSettingsUseCase()
        }
    }

    fun setGlobalNotifications(enabled: Boolean) {
        viewModelScope.launch {
            setGlobalNotificationsUseCase(enabled)
            _userSettings.value = getUserSettingsUseCase()
        }
    }

    fun updateGameNotificationPref(pref: GameNotificationPref) {
        viewModelScope.launch {
            updateGameNotificationPrefUseCase(pref)
            _userSettings.value = getUserSettingsUseCase()
        }
    }

    fun refreshNotifications() {
        viewModelScope.launch {
            getFavoritesUseCase().onSuccess { favorites ->
                val ids = favorites.map { it.id }
                generateDiscountNotificationsUseCase(ids)
                _notifications.value = getInAppNotificationsUseCase()
                _unreadCount.value = getUnreadNotificationCountUseCase()
            }
        }
    }

    fun markAsRead(notificationId: Int) {
        viewModelScope.launch {
            markNotificationReadUseCase(notificationId)
            _notifications.value = getInAppNotificationsUseCase()
            _unreadCount.value = getUnreadNotificationCountUseCase()
        }
    }
}

