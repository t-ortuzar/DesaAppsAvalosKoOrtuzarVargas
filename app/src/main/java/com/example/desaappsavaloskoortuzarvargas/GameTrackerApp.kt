package com.example.desaappsavaloskoortuzarvargas

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.desaappsavaloskoortuzarvargas.data.local.SettingsKeys
import com.example.desaappsavaloskoortuzarvargas.data.local.settingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class GameTrackerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        val languageCode = runBlocking {
            applicationContext.settingsDataStore.data.first()[SettingsKeys.LANGUAGE_CODE] ?: "en"
        }
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageCode))
    }

    companion object {
        lateinit var appContext: Context
            private set
    }
}
