package com.example.desaappsavaloskoortuzarvargas.domain.model

data class AppUser(
    val id: String,
    val username: String,
    val email: String = "",
    val favoriteGameIds: List<Int> = emptyList(),
    val displayName: String = "",
    val darkMode: Boolean = true,
    val languageCode: String = "en",
    val country: String = "Argentina",
    val countryCode: String = "AR",
    val globalNotifications: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)