package com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.desaappsavaloskoortuzarvargas.data.local.SettingsKeys
import com.example.desaappsavaloskoortuzarvargas.data.local.settingsDataStore
import com.example.desaappsavaloskoortuzarvargas.data.remote.MongoAuthService
import com.example.desaappsavaloskoortuzarvargas.domain.model.MongoUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val user: MongoUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val mongoAuthService: MongoAuthService,
    private val context: Context
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        checkExistingSession()
    }

    /** Check if a userId is already stored → auto-login on subsequent launches. */
    private fun checkExistingSession() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val prefs = context.settingsDataStore.data.first()
                val userId = prefs[SettingsKeys.MONGO_USER_ID]
                if (!userId.isNullOrEmpty()) {
                    val result = mongoAuthService.getUserById(userId)
                    if (result.isSuccess) {
                        _authState.value = AuthState.Authenticated(result.getOrThrow())
                    } else {
                        // Session invalid — clear stored ID and ask for login
                        clearSession()
                        _authState.value = AuthState.Unauthenticated
                    }
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            } catch (_: Exception) {
                // MongoDB unreachable or session check failed → let user in anyway
                _authState.value = AuthState.Unauthenticated
            }
            _isLoading.value = false
        }
    }

    fun register(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authState.value = AuthState.Loading
            val result = mongoAuthService.register(username, password)
            if (result.isSuccess) {
                val user = result.getOrThrow()
                saveSession(user)
                _authState.value = AuthState.Authenticated(user)
            } else {
                val err = result.exceptionOrNull()
                _authState.value = AuthState.Error(
                    MongoAuthService.friendlyError(err ?: Exception("Registration failed"))
                )
            }
            _isLoading.value = false
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authState.value = AuthState.Loading
            val result = mongoAuthService.login(username, password)
            if (result.isSuccess) {
                val user = result.getOrThrow()
                saveSession(user)
                _authState.value = AuthState.Authenticated(user)
            } else {
                val err = result.exceptionOrNull()
                _authState.value = AuthState.Error(
                    MongoAuthService.friendlyError(err ?: Exception("Login failed"))
                )
            }
            _isLoading.value = false
        }
    }

    /** Continue without account — skip auth and go straight to the app. */
    fun skipAuth() {
        _authState.value = AuthState.Authenticated(
            MongoUser(id = "guest", username = "guest")
        )
    }

    /** Push local favorites to MongoDB. */
    fun syncFavorites(favoriteGameIds: List<Int>) {
        val state = _authState.value
        if (state !is AuthState.Authenticated || state.user.id == "guest") return
        viewModelScope.launch {
            mongoAuthService.syncFavorites(state.user.id, favoriteGameIds)
        }
    }

    private suspend fun saveSession(user: MongoUser) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.MONGO_USER_ID]   = user.id
            prefs[SettingsKeys.MONGO_USERNAME]  = user.username
        }
    }

    private suspend fun clearSession() {
        context.settingsDataStore.edit { prefs ->
            prefs.remove(SettingsKeys.MONGO_USER_ID)
            prefs.remove(SettingsKeys.MONGO_USERNAME)
        }
    }

    override fun onCleared() {
        super.onCleared()
        mongoAuthService.close()
    }
}

