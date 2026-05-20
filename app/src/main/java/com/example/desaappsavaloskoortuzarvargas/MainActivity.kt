package com.example.desaappsavaloskoortuzarvargas

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.desaappsavaloskoortuzarvargas.presentation.screen.MainScreen
import com.example.desaappsavaloskoortuzarvargas.ui.theme.DesaAppsAvalosKoOrtuzarVargasTheme

class MainActivity : AppCompatActivity() {
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
