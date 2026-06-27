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
    private val database: GameTrackerDatabase? = null
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        checkExistingSession()
    }

    /** Check Firebase Auth state — Firebase already persists the login token locally,
     *  so no network call is needed. The user stays logged in until they sign out. */
    private fun checkExistingSession() {
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            // Firebase has a valid cached session — restore immediately, no network needed
            val username = currentUser.email
                ?.removeSuffix("@argengamer.app") ?: currentUser.uid
            _authState.value = AuthState.Authenticated(
                AppUser(id = currentUser.uid, username = username)
            )
        } else {
            _authState.value = AuthState.Unauthenticated
        }
        _isLoading.value = false
    }

    fun register(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authState.value = AuthState.Loading
            val result = authService.register(username, password)
            if (result.isSuccess) {
                val user = result.getOrThrow()
                saveSession(user)
                _authState.value = AuthState.Authenticated(user)
            } else {
                val err = result.exceptionOrNull()
                _authState.value = AuthState.Error(
                    FirebaseAuthService.friendlyError(err ?: Exception("Registration failed"))
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
                _authState.value = AuthState.Authenticated(user)
            } else {
                val err = result.exceptionOrNull()
                _authState.value = AuthState.Error(
                    FirebaseAuthService.friendlyError(err ?: Exception("Login failed"))
                )
            }
            _isLoading.value = false
        }
    }

    /** Continue without account — resets all user data to defaults then enters as guest. */
    fun skipAuth() {
        viewModelScope.launch {
            resetUserData()
            _authState.value = AuthState.Authenticated(AppUser(id = "guest", username = "guest"))
        }
    }

    /** Sign out — clears Firebase Auth session, resets all user data to defaults. */
    fun signOut() {
        Firebase.auth.signOut()
        viewModelScope.launch { resetUserData() }
        _authState.value = AuthState.Unauthenticated
    }

    /** Push local favourites to Firebase Firestore. */
    fun syncFavorites(favoriteGameIds: List<Int>) {
        val state = _authState.value
        if (state !is AuthState.Authenticated || state.user.id == "guest") return
        viewModelScope.launch {
            authService.syncFavorites(state.user.id, favoriteGameIds)
        }
    }

    private suspend fun saveSession(user: AppUser) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.MONGO_USER_ID]  = user.id
            prefs[SettingsKeys.MONGO_USERNAME] = user.username
        }
    }

    /**
     * Reset all user-specific data to defaults.
     * Called on sign-out and on guest/anonymous login so that:
     *  - The next session always starts clean (no previous user's data leaks through)
     *  - Favorites list is cleared from the Room database
     *  - Display name resets to "Player", dark mode resets to true (dark), etc.
     */
    private suspend fun resetUserData() {
        // 1. Clear DataStore — remove session tokens + reset all user preferences to defaults
        context.settingsDataStore.edit { prefs ->
            prefs.remove(SettingsKeys.MONGO_USER_ID)
            prefs.remove(SettingsKeys.MONGO_USERNAME)
            prefs.remove(SettingsKeys.USER_NAME)      // resets to "Player" (default in repository)
            prefs.remove(SettingsKeys.EMAIL)
            prefs.remove(SettingsKeys.LANGUAGE_CODE)
            prefs.remove(SettingsKeys.COUNTRY)
            prefs.remove(SettingsKeys.COUNTRY_CODE)
            prefs.remove(SettingsKeys.GLOBAL_NOTIFICATIONS)
            // Explicitly set dark mode back to default (dark = true)
            prefs[SettingsKeys.DARK_MODE] = true
        }
        // 2. Clear all favorites from the Room database
        try {
            database?.favoriteGameDao()?.clearAllFavorites()
        } catch (_: Exception) { /* non-critical — ignore DB errors during sign-out */ }
    }

    /** @deprecated Use resetUserData() instead. Kept for compatibility. */
    private suspend fun clearSession() = resetUserData()

    override fun onCleared() {
        super.onCleared()
        authService.close()
    }
}

