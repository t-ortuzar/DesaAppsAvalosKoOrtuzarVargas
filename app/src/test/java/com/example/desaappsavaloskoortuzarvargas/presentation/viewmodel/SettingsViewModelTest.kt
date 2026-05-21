package com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel

import com.example.desaappsavaloskoortuzarvargas.domain.model.*
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private lateinit var getUserSettings: GetUserSettingsUseCase
    private lateinit var updateUserName: UpdateUserNameUseCase
    private lateinit var updateEmail: UpdateEmailUseCase
    private lateinit var updateCountry: UpdateCountryUseCase
    private lateinit var updateLanguage: UpdateLanguageUseCase
    private lateinit var setGlobalNotifs: SetGlobalNotificationsUseCase
    private lateinit var updateGameNotifPref: UpdateGameNotificationPrefUseCase
    private lateinit var getNotifications: GetInAppNotificationsUseCase
    private lateinit var getUnreadCount: GetUnreadNotificationCountUseCase
    private lateinit var markRead: MarkNotificationReadUseCase
    private lateinit var generateNotifs: GenerateDiscountNotificationsUseCase
    private lateinit var getFavorites: GetFavoritesUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        getUserSettings = mock()
        updateUserName = mock()
        updateEmail = mock()
        updateCountry = mock()
        updateLanguage = mock()
        setGlobalNotifs = mock()
        updateGameNotifPref = mock()
        getNotifications = mock()
        getUnreadCount = mock()
        markRead = mock()
        generateNotifs = mock()
        getFavorites = mock()
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private suspend fun setupDefaults() {
        whenever(getUserSettings.invoke()).thenReturn(UserSettings())
        whenever(getNotifications.invoke()).thenReturn(emptyList())
        whenever(getUnreadCount.invoke()).thenReturn(0)
        whenever(getFavorites.invoke()).thenReturn(Result.success(emptyList()))
    }

    private fun createViewModel(): SettingsViewModel {
        return SettingsViewModel(
            getUserSettings, updateUserName, updateEmail, updateCountry,
            updateLanguage, setGlobalNotifs, updateGameNotifPref,
            getNotifications, getUnreadCount, markRead, generateNotifs, getFavorites
        )
    }

    @Test
    fun `init loads settings and refreshes notifications`() = runTest(dispatcher) {
        setupDefaults()
        val vm = createViewModel()
        advanceUntilIdle()
        assertEquals("Player", vm.userSettings.value.userName)
        verify(getUserSettings, atLeastOnce()).invoke()
    }

    @Test
    fun `updateUserName updates settings`() = runTest(dispatcher) {
        setupDefaults()
        val updated = UserSettings(userName = "NewName")
        val vm = createViewModel()
        advanceUntilIdle()

        whenever(getUserSettings.invoke()).thenReturn(updated)
        vm.updateUserName("NewName")
        advanceUntilIdle()

        verify(updateUserName).invoke("NewName")
        assertEquals("NewName", vm.userSettings.value.userName)
    }

    @Test
    fun `updateEmail updates settings`() = runTest(dispatcher) {
        setupDefaults()
        val vm = createViewModel()
        advanceUntilIdle()

        whenever(getUserSettings.invoke()).thenReturn(UserSettings(email = "a@b.com"))
        vm.updateEmail("a@b.com")
        advanceUntilIdle()

        verify(updateEmail).invoke("a@b.com")
        assertEquals("a@b.com", vm.userSettings.value.email)
    }

    @Test
    fun `updateCountry updates settings`() = runTest(dispatcher) {
        setupDefaults()
        val vm = createViewModel()
        advanceUntilIdle()

        whenever(getUserSettings.invoke()).thenReturn(UserSettings(country = "Chile", countryCode = "CL"))
        vm.updateCountry("Chile", "CL")
        advanceUntilIdle()

        verify(updateCountry).invoke("Chile", "CL")
        assertEquals("Chile", vm.userSettings.value.country)
    }

    @Test
    fun `updateLanguage is suspend and updates settings`() = runTest(dispatcher) {
        setupDefaults()
        val vm = createViewModel()
        advanceUntilIdle()

        whenever(getUserSettings.invoke()).thenReturn(UserSettings(languageCode = "es"))
        vm.updateLanguage("es")
        advanceUntilIdle()

        verify(updateLanguage).invoke("es")
        assertEquals("es", vm.userSettings.value.languageCode)
    }

    @Test
    fun `setGlobalNotifications updates settings`() = runTest(dispatcher) {
        setupDefaults()
        val vm = createViewModel()
        advanceUntilIdle()

        whenever(getUserSettings.invoke()).thenReturn(UserSettings(globalNotificationsEnabled = false))
        vm.setGlobalNotifications(false)
        advanceUntilIdle()

        verify(setGlobalNotifs).invoke(false)
        assertFalse(vm.userSettings.value.globalNotificationsEnabled)
    }

    @Test
    fun `updateGameNotificationPref updates settings`() = runTest(dispatcher) {
        setupDefaults()
        val vm = createViewModel()
        advanceUntilIdle()

        val pref = GameNotificationPref(1, "Game1", notifyOffers = false)
        vm.updateGameNotificationPref(pref)
        advanceUntilIdle()

        verify(updateGameNotifPref).invoke(pref)
    }

    @Test
    fun `markAsRead updates notifications`() = runTest(dispatcher) {
        setupDefaults()
        val vm = createViewModel()
        advanceUntilIdle()

        vm.markAsRead(42)
        advanceUntilIdle()

        verify(markRead).invoke(42)
        verify(getNotifications, atLeast(2)).invoke()
        verify(getUnreadCount, atLeast(2)).invoke()
    }

    @Test
    fun `refreshNotifications generates and loads notifications`() = runTest(dispatcher) {
        setupDefaults()
        val games = listOf(
            Game(id = 1, name = "G", description = "D", releaseDate = "2024",
                imageUrl = "u", rating = 8.0, currentPrices = emptyMap())
        )
        whenever(getFavorites.invoke()).thenReturn(Result.success(games))
        whenever(getNotifications.invoke()).thenReturn(emptyList())
        whenever(getUnreadCount.invoke()).thenReturn(0)

        val vm = createViewModel()
        advanceUntilIdle()

        verify(generateNotifs).invoke(listOf(1))
    }
}

