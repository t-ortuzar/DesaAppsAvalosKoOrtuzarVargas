package com.example.desaappsavaloskoortuzarvargas.domain.usecase

import com.example.desaappsavaloskoortuzarvargas.domain.model.*
import com.example.desaappsavaloskoortuzarvargas.domain.repository.UserSettingsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class SettingsUseCaseTest {

    private lateinit var repo: UserSettingsRepository

    @Before
    fun setup() { repo = mock() }

    @Test
    fun `GetUserSettingsUseCase delegates to repository`() = runTest {
        whenever(repo.getUserSettings()).thenReturn(UserSettings())
        val result = GetUserSettingsUseCase(repo)()
        assertEquals("Player", result.userName)
        verify(repo).getUserSettings()
    }

    @Test
    fun `UpdateUserNameUseCase delegates to repository`() = runTest {
        UpdateUserNameUseCase(repo)("NewName")
        verify(repo).updateUserName("NewName")
    }

    @Test
    fun `UpdateEmailUseCase delegates to repository`() = runTest {
        UpdateEmailUseCase(repo)("test@test.com")
        verify(repo).updateEmail("test@test.com")
    }

    @Test
    fun `UpdateCountryUseCase delegates to repository with default code`() = runTest {
        UpdateCountryUseCase(repo)("Argentina")
        verify(repo).updateCountry("Argentina", "AR")
    }

    @Test
    fun `UpdateCountryUseCase delegates to repository with custom code`() = runTest {
        UpdateCountryUseCase(repo)("Chile", "CL")
        verify(repo).updateCountry("Chile", "CL")
    }

    @Test
    fun `UpdateLanguageUseCase delegates to repository`() = runTest {
        UpdateLanguageUseCase(repo)("es")
        verify(repo).updateLanguage("es")
    }

    @Test
    fun `SetGlobalNotificationsUseCase delegates to repository`() = runTest {
        SetGlobalNotificationsUseCase(repo)(false)
        verify(repo).setGlobalNotifications(false)
    }

    @Test
    fun `UpdateGameNotificationPrefUseCase delegates to repository`() = runTest {
        val pref = GameNotificationPref(1, "Game1")
        UpdateGameNotificationPrefUseCase(repo)(pref)
        verify(repo).updateGameNotificationPref(pref)
    }

    @Test
    fun `GetInAppNotificationsUseCase delegates to repository`() = runTest {
        whenever(repo.getInAppNotifications()).thenReturn(emptyList())
        val result = GetInAppNotificationsUseCase(repo)()
        assertTrue(result.isEmpty())
        verify(repo).getInAppNotifications()
    }

    @Test
    fun `GetUnreadNotificationCountUseCase delegates to repository`() = runTest {
        whenever(repo.getUnreadNotificationCount()).thenReturn(5)
        val result = GetUnreadNotificationCountUseCase(repo)()
        assertEquals(5, result)
    }

    @Test
    fun `MarkNotificationReadUseCase delegates to repository`() = runTest {
        MarkNotificationReadUseCase(repo)(42)
        verify(repo).markNotificationRead(42)
    }

    @Test
    fun `GenerateDiscountNotificationsUseCase delegates to repository`() = runTest {
        GenerateDiscountNotificationsUseCase(repo)(listOf(1, 2, 3))
        verify(repo).generateDiscountNotifications(listOf(1, 2, 3))
    }
}

