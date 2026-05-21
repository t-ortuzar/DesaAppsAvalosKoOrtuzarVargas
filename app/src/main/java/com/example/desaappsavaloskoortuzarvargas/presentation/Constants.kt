package com.example.desaappsavaloskoortuzarvargas.presentation

import androidx.compose.ui.graphics.Color

/**
 * App-wide color constants used across multiple components.
 * Centralizes hardcoded hex colors that were scattered throughout the codebase.
 */
object AppColors {
    val F2PBlue = Color(0xFF2196F3)
    val FreeGreen = Color(0xFF4CAF50)
    val HistoricalGold = Color(0xFFFFD700)
    val UrgentOrange = Color(0xFFFF9800)
}

/**
 * Popular tags shown as filter chips in the catalog screen.
 */
val POPULAR_TAGS = listOf(
    "Action", "RPG", "FPS", "Open World", "Horror", "Survival",
    "Co-op", "Indie", "Puzzle", "Racing", "Sports", "Souls-like", "Roguelike", "Strategy"
)

/**
 * Platform names used as filter chips in the offers screen.
 */
val STORE_PLATFORMS = listOf(
    "Steam", "Epic Games", "GOG", "EA Play", "Ubisoft+", "Battle.net", "G2A", "Eneba"
)

