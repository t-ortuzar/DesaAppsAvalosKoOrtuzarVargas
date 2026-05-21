package com.example.desaappsavaloskoortuzarvargas.domain.model

data class UserSettings(
    val userName: String = "Player",
    val email: String = "",
    val country: String = "Argentina",
    val countryCode: String = "AR",
    val languageCode: String = "en",
    val globalNotificationsEnabled: Boolean = true,
    val gameNotificationPrefs: Map<Int, GameNotificationPref> = emptyMap()
)

data class GameNotificationPref(
    val gameId: Int,
    val gameName: String,
    val notifyOffers: Boolean = true,
    val notifyNews: Boolean = true,
    val notifyHistoricalLow: Boolean = true
)

data class InAppNotification(
    val id: Int,
    val gameId: Int?,
    val type: NotificationType,
    val gameName: String,
    val discountPercentage: Int,
    val platform: String,
    val timestamp: Long,
    val isRead: Boolean = false
)

enum class NotificationType {
    DISCOUNT, HISTORICAL_LOW, NEWS, FREE_GAME
}

data class CountryInfo(
    val name: String,
    val code: String,       // ISO 3166-1 alpha-2
    val steamCc: String,    // Steam country code for regional pricing
    val currency: String,   // Display currency label
    val isAvailable: Boolean = false // Whether this region is currently supported
)

val SUPPORTED_COUNTRIES = listOf(
    CountryInfo("Argentina", "AR", "ar", "ARS", isAvailable = true),
    CountryInfo("Brasil", "BR", "br", "USD", isAvailable = false),
    CountryInfo("Chile", "CL", "cl", "USD", isAvailable = false),
    CountryInfo("Colombia", "CO", "co", "USD", isAvailable = false),
    CountryInfo("México", "MX", "mx", "USD", isAvailable = false),
    CountryInfo("Estados Unidos", "US", "us", "USD", isAvailable = false),
    CountryInfo("España", "ES", "es", "USD", isAvailable = false),
    CountryInfo("Uruguay", "UY", "uy", "USD", isAvailable = false),
    CountryInfo("Perú", "PE", "pe", "USD", isAvailable = false),
    CountryInfo("Paraguay", "PY", "py", "USD", isAvailable = false)
)

/**
 * Convert ISO country code to flag emoji using regional indicator symbols.
 * Each letter A-Z is mapped to Unicode regional indicator A-Z (U+1F1E6 to U+1F1FF).
 */
fun countryCodeToFlag(countryCode: String): String {
    if (countryCode.length != 2) return ""
    val first = Character.codePointAt(countryCode.uppercase(), 0) - 0x41 + 0x1F1E6
    val second = Character.codePointAt(countryCode.uppercase(), 1) - 0x41 + 0x1F1E6
    return String(Character.toChars(first)) + String(Character.toChars(second))
}

val ALL_TAGS = listOf(
    "Action", "Adventure", "RPG", "FPS", "TPS",
    "Strategy", "RTS", "Simulation", "Racing", "Sports",
    "Horror", "Survival", "Puzzle", "Platformer", "Fighting",
    "Open World", "Souls-like", "Roguelike", "Co-op", "MMO",
    "Indie", "Narrative", "Sandbox", "Building", "Battle Royale",
    "Card Game", "Free2Play"
)

