package com.example.desaappsavaloskoortuzarvargas.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

private const val DATASTORE_NAME = "user_settings"

val Context.settingsDataStore by preferencesDataStore(name = DATASTORE_NAME)

object SettingsKeys {
    val USER_NAME = stringPreferencesKey("user_name")
    val EMAIL = stringPreferencesKey("email")
    val COUNTRY = stringPreferencesKey("country")
    val COUNTRY_CODE = stringPreferencesKey("country_code")
    val LANGUAGE_CODE = stringPreferencesKey("language_code")
    val GLOBAL_NOTIFICATIONS = booleanPreferencesKey("global_notifications")
}

