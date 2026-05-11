package com.example.desaappsavaloskoortuzarvargas.domain.model

data class UserSettings(
    val userName: String = "Player",
    val email: String = "",
    val country: String = "Argentina",
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
    val title: String,
    val message: String,
    val gameId: Int?,
    val type: NotificationType,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

enum class NotificationType {
    DISCOUNT, HISTORICAL_LOW, NEWS, FREE_GAME
}

val SUPPORTED_COUNTRIES = listOf(
    "Argentina", "Brasil", "Chile", "Colombia", "México",
    "Estados Unidos", "España", "Uruguay", "Perú", "Paraguay"
)

val ALL_TAGS = listOf(
    "Action", "Adventure", "RPG", "FPS", "TPS",
    "Strategy", "RTS", "Simulation", "Racing", "Sports",
    "Horror", "Survival", "Puzzle", "Platformer", "Fighting",
    "Open World", "Souls-like", "Roguelike", "Co-op", "MMO",
    "Indie", "Narrative", "Sandbox", "Building", "Battle Royale",
    "Card Game", "Free2Play"
)

