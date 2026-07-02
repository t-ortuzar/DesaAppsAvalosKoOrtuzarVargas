package com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.desaappsavaloskoortuzarvargas.data.local.GameTrackerDatabase
import com.example.desaappsavaloskoortuzarvargas.data.local.SettingsKeys
import com.example.desaappsavaloskoortuzarvargas.data.local.settingsDataStore
import com.example.desaappsavaloskoortuzarvargas.data.remote.FirebaseAuthService
import com.example.desaappsavaloskoortuzarvargas.domain.model.AppUser
import com.example.desaappsavaloskoortuzarvargas.domain.repository.GameRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val user: AppUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val authService: FirebaseAuthService,
    private val context: Context,
    private val database: GameTrackerDatabase? = null,
    private val gameRepository: GameRepository? = null
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Increments every time a Firestore → local sync completes.
     * Observed by MainScreen to trigger UI reloads (favorites, settings).
     */
    private val _syncVersion = MutableStateFlow(0)
    val syncVersion: StateFlow<Int> = _syncVersion.asStateFlow()

    init {
        checkExistingSession()
    }

    private fun checkExistingSession() {
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            val username = currentUser.email
                ?.removeSuffix("@argengamer.app") ?: currentUser.uid
            _authState.value = AuthState.Authenticated(
                AppUser(id = currentUser.uid, username = username)
            )
            // Async: download full profile from Firestore and apply locally
            viewModelScope.launch {
                applyFirestoreProfile(currentUser.uid)
            }
        } else {
            _authState.value = AuthState.Unauthenticated
        }
        _isLoading.value = false
    }

    /** Download Firestore profile → apply ALL preferences to DataStore + GameRepository. */
    private suspend fun applyFirestoreProfile(userId: String) {
        authService.getUserById(userId).onSuccess { user ->
            applyUserLocally(user)
            _authState.value = AuthState.Authenticated(user)
            _syncVersion.value += 1
        }.onFailure {
            // Non-fatal — continue with whatever is in DataStore
            _syncVersion.value += 1
        }
    }

    /** Apply a downloaded AppUser to local DataStore and in-memory GameRepository. */
    private suspend fun applyUserLocally(user: AppUser) {
        context.settingsDataStore.edit { prefs ->
            if (user.displayName.isNotEmpty()) prefs[SettingsKeys.USER_NAME] = user.displayName
            if (user.email.isNotEmpty())       prefs[SettingsKeys.EMAIL]     = user.email
            prefs[SettingsKeys.DARK_MODE]           = user.darkMode
            prefs[SettingsKeys.LANGUAGE_CODE]       = user.languageCode
            prefs[SettingsKeys.COUNTRY]             = user.country
            prefs[SettingsKeys.COUNTRY_CODE]        = user.countryCode
            prefs[SettingsKeys.GLOBAL_NOTIFICATIONS] = user.globalNotifications
        }
        gameRepository?.initializeFavorites(user.favoriteGameIds.toSet())
    }

    fun register(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authState.value = AuthState.Loading
            val result = authService.register(username, password)
            if (result.isSuccess) {
                val user = result.getOrThrow()
                saveSession(user)
                _syncVersion.value += 1
                _authState.value = AuthState.Authenticated(user)
            } else {
                _authState.value = AuthState.Error(
                    FirebaseAuthService.friendlyError(result.exceptionOrNull() ?: Exception("Registration failed"))
                )
            }
            _isLoading.value = false
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authState.value = AuthState.Loading
            val result = authService.login(username, password)
            if (result.isSuccess) {
                val user = result.getOrThrow()
                saveSession(user)
                applyUserLocally(user)
                _syncVersion.value += 1
                _authState.value = AuthState.Authenticated(user)
            } else {
                _authState.value = AuthState.Error(
                    FirebaseAuthService.friendlyError(result.exceptionOrNull() ?: Exception("Login failed"))
                )
            }
            _isLoading.value = false
        }
    }

    fun skipAuth() {
        viewModelScope.launch {
            resetUserData()
            _syncVersion.value += 1
            _authState.value = AuthState.Authenticated(AppUser(id = "guest", username = "guest"))
        }
    }

    fun signOut() {
        Firebase.auth.signOut()
        viewModelScope.launch { resetUserData() }
        _authState.value = AuthState.Unauthenticated
    }

    /**
     * Re-download ALL preferences from Firestore and apply locally.
     * Call on app resume so both devices stay in sync.
     */
    fun refreshFromFirebase() {
        val state = _authState.value
        if (state !is AuthState.Authenticated || state.user.id == "guest") return
        viewModelScope.launch {
            applyFirestoreProfile(state.user.id)
        }
    }

    /**
     * Upload ALL current local preferences + favorites to Firestore in one call.
     * Call after any preference change so other devices pick it up on next resume.
     */
    fun syncAll() {
        val state = _authState.value
        if (state !is AuthState.Authenticated || state.user.id == "guest") return
        viewModelScope.launch {
            val prefs       = context.settingsDataStore.data.first()
            val favoriteIds = gameRepository?.getFavorites()?.getOrDefault(emptyList())?.map { it.id } ?: emptyList()
            authService.syncAllUserData(
                userId             = state.user.id,
                displayName        = prefs[SettingsKeys.USER_NAME] ?: "Player",
                email              = prefs[SettingsKeys.EMAIL] ?: "",
                favoriteGameIds    = favoriteIds,
                darkMode           = prefs[SettingsKeys.DARK_MODE] ?: true,
                languageCode       = prefs[SettingsKeys.LANGUAGE_CODE] ?: "en",
                country            = prefs[SettingsKeys.COUNTRY] ?: "Argentina",
                countryCode        = prefs[SettingsKeys.COUNTRY_CODE] ?: "AR",
                globalNotifications = prefs[SettingsKeys.GLOBAL_NOTIFICATIONS] ?: true
            )
        }
    }

    private suspend fun saveSession(user: AppUser) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.MONGO_USER_ID]  = user.id
            prefs[SettingsKeys.MONGO_USERNAME] = user.username
        }
    }

    private suspend fun resetUserData() {
        context.settingsDataStore.edit { prefs ->
            prefs.remove(SettingsKeys.MONGO_USER_ID)
            prefs.remove(SettingsKeys.MONGO_USERNAME)
            prefs.remove(SettingsKeys.USER_NAME)
            prefs.remove(SettingsKeys.EMAIL)
            prefs.remove(SettingsKeys.LANGUAGE_CODE)
            prefs.remove(SettingsKeys.COUNTRY)
            prefs.remove(SettingsKeys.COUNTRY_CODE)
            prefs.remove(SettingsKeys.GLOBAL_NOTIFICATIONS)
            prefs[SettingsKeys.DARK_MODE] = true
        }
        gameRepository?.initializeFavorites(emptySet())
        try { database?.favoriteGameDao()?.clearAllFavorites() } catch (_: Exception) { }
    }

    override fun onCleared() {
        super.onCleared()
        authService.close()
    }
}
