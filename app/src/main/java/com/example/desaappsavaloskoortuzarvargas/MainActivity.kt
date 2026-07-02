package com.example.desaappsavaloskoortuzarvargas

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.desaappsavaloskoortuzarvargas.data.local.SettingsKeys
import com.example.desaappsavaloskoortuzarvargas.data.local.settingsDataStore
import com.example.desaappsavaloskoortuzarvargas.data.remote.FirebaseAuthService
import com.example.desaappsavaloskoortuzarvargas.di.ServiceLocator
import com.example.desaappsavaloskoortuzarvargas.presentation.screen.LoginScreen
import com.example.desaappsavaloskoortuzarvargas.presentation.screen.MainScreen
import com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel.AuthState
import com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel.AuthViewModel
import com.example.desaappsavaloskoortuzarvargas.ui.theme.DesaAppsAvalosKoOrtuzarVargasTheme
import kotlinx.coroutines.flow.map

class MainActivity : AppCompatActivity() {

    private val authService by lazy { FirebaseAuthService() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDark by remember { settingsDataStore.data.map { prefs -> prefs[SettingsKeys.DARK_MODE] ?: true } }
                .collectAsState(initial = true)

            DesaAppsAvalosKoOrtuzarVargasTheme(darkTheme = isDark) {
                val authViewModel: AuthViewModel = viewModel {
                    AuthViewModel(
                        authService,
                        applicationContext,
                        ServiceLocator.database,
                        ServiceLocator.gameRepository
                    )
                }
                val authState   by authViewModel.authState.collectAsState()
                val syncVersion by authViewModel.syncVersion.collectAsState()

                // Refresh all preferences from Firestore every time the app comes to foreground
                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            authViewModel.refreshFromFirebase()
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }

                when (authState) {
                    is AuthState.Loading -> { /* Splash while checking session */ }
                    is AuthState.Authenticated -> {
                        val isGuest = (authState as AuthState.Authenticated).user.id == "guest"
                        MainScreen(
                            onSignOut            = { authViewModel.signOut() },
                            onLoginRequest       = { authViewModel.signOut() },
                            isGuest              = isGuest,
                            syncVersion          = syncVersion,
                            onPreferencesChanged = { authViewModel.syncAll() },
                            onRefreshFromFirebase = { authViewModel.refreshFromFirebase() }
                        )
                    }
                    else -> LoginScreen(
                        authViewModel = authViewModel,
                        onAuthSuccess = { }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        authService.close()
    }
}
