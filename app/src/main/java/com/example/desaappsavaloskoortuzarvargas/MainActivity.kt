package com.example.desaappsavaloskoortuzarvargas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.desaappsavaloskoortuzarvargas.presentation.screen.MainScreen
import com.example.desaappsavaloskoortuzarvargas.ui.theme.DesaAppsAvalosKoOrtuzarVargasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DesaAppsAvalosKoOrtuzarVargasTheme {
                MainScreen()
            }
        }
    }
}
