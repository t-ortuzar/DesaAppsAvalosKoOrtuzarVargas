package com.example.desaappsavaloskoortuzarvargas.data.api

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Represents a known sale event on a digital games store.
 */
data class UpcomingSaleEvent(
    val name: String,
    val store: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val description: String,
    /** Hex color (0xAARRGGBB) used as card accent per store. */
    val accentColor: Long
) {
    val today: LocalDate get() = LocalDate.now()

    /** True if today falls within [startDate, endDate]. */
    val isActive: Boolean
        get() = !today.isBefore(startDate) && !today.isAfter(endDate)

    /** Days until the sale starts (0 if active, negative if already ended). */
    val daysUntilStart: Long
        get() = ChronoUnit.DAYS.between(today, startDate)

    /** Total duration of the sale in days. */
    val durationDays: Int
        get() = (ChronoUnit.DAYS.between(startDate, endDate) + 1).toInt()

    /** True if this event is still upcoming or active (not past). */
    val isRelevant: Boolean
        get() = !today.isAfter(endDate)
}

/**
 * Provides a static calendar of known annual sales events for major PC/console stores.
 * Dates are based on historical patterns and known announcements.
 *
 * Typically updated each year with actual dates once announced.
 */
object SalesCalendarService {

    private const val STEAM_COLOR  = 0xFF1B2838L
    private const val EPIC_COLOR   = 0xFF313131L
    private const val GOG_COLOR    = 0xFF86328AL
    private const val XBOX_COLOR   = 0xFF107C10L
    private const val EA_COLOR     = 0xFFFF4500L
    private const val UBISOFT_COLOR = 0xFF0D47A1L

    /**
     * Returns all known sale events for 2026, sorted by start date.
     * Only events that haven't ended yet are included.
     */
    fun getUpcomingEvents(): List<UpcomingSaleEvent> {
        val allEvents = listOf(
            // ── Steam ──────────────────────────────────────────────────────────
            UpcomingSaleEvent(
                name = "Steam Summer Sale",
                store = "Steam",
                startDate = LocalDate.of(2026, 6, 25),
                endDate   = LocalDate.of(2026, 7, 9),
                description = "Up to 90% off thousands of games. One of the biggest sales of the year.",
                accentColor = STEAM_COLOR
            ),
            UpcomingSaleEvent(
                name = "Steam Halloween Sale",
                store = "Steam",
                startDate = LocalDate.of(2026, 10, 29),
                endDate   = LocalDate.of(2026, 11, 2),
                description = "Deep discounts on horror, thriller and atmospheric games.",
                accentColor = STEAM_COLOR
            ),
            UpcomingSaleEvent(
                name = "Steam Autumn Sale",
                store = "Steam",
                startDate = LocalDate.of(2026, 11, 25),
                endDate   = LocalDate.of(2026, 12, 2),
                description = "End-of-year discounts ahead of the holidays.",
                accentColor = STEAM_COLOR
            ),
            UpcomingSaleEvent(
                name = "Steam Winter Sale",
                store = "Steam",
                startDate = LocalDate.of(2026, 12, 22),
                endDate   = LocalDate.of(2027, 1, 5),
                description = "Holiday mega-sale. Two weeks of discounts up to 95%.",
                accentColor = STEAM_COLOR
            ),
            // ── Epic Games ─────────────────────────────────────────────────────
            UpcomingSaleEvent(
                name = "Epic Holiday Sale",
                store = "Epic Games",
                startDate = LocalDate.of(2026, 12, 17),
                endDate   = LocalDate.of(2027, 1, 6),
                description = "Holiday discounts plus free games every day during the event.",
                accentColor = EPIC_COLOR
            ),
            // ── GOG ────────────────────────────────────────────────────────────
            UpcomingSaleEvent(
                name = "GOG Summer Sale",
                store = "GOG",
                startDate = LocalDate.of(2026, 6, 26),
                endDate   = LocalDate.of(2026, 7, 13),
                description = "DRM-free summer deals on classic and modern games.",
                accentColor = GOG_COLOR
            ),
            UpcomingSaleEvent(
                name = "GOG Winter Sale",
                store = "GOG",
                startDate = LocalDate.of(2026, 12, 17),
                endDate   = LocalDate.of(2027, 1, 3),
                description = "End-of-year DRM-free deals. Up to 90% off.",
                accentColor = GOG_COLOR
            ),
            // ── Xbox / Microsoft ───────────────────────────────────────────────
            UpcomingSaleEvent(
                name = "Xbox Summer Sale",
                store = "Xbox / Microsoft",
                startDate = LocalDate.of(2026, 7, 1),
                endDate   = LocalDate.of(2026, 7, 28),
                description = "Xbox Game Pass deals and up to 75% off selected titles.",
                accentColor = XBOX_COLOR
            ),
            UpcomingSaleEvent(
                name = "Xbox Black Friday Sale",
                store = "Xbox / Microsoft",
                startDate = LocalDate.of(2026, 11, 24),
                endDate   = LocalDate.of(2026, 12, 7),
                description = "Black Friday and Cyber Monday discounts on console and PC games.",
                accentColor = XBOX_COLOR
            ),
            // ── EA ─────────────────────────────────────────────────────────────
            UpcomingSaleEvent(
                name = "EA Summer Sale",
                store = "EA",
                startDate = LocalDate.of(2026, 7, 8),
                endDate   = LocalDate.of(2026, 7, 22),
                description = "Discounts on EA titles across sports, FPS and RPG games.",
                accentColor = EA_COLOR
            ),
            // ── Ubisoft ────────────────────────────────────────────────────────
            UpcomingSaleEvent(
                name = "Ubisoft Summer Sale",
                store = "Ubisoft",
                startDate = LocalDate.of(2026, 6, 30),
                endDate   = LocalDate.of(2026, 7, 14),
                description = "Up to 80% off Ubisoft titles on Ubisoft Connect.",
                accentColor = UBISOFT_COLOR
            )
        )

        return allEvents
            .filter { it.isRelevant }
            .sortedWith(compareByDescending<UpcomingSaleEvent> { it.isActive }.thenBy { it.startDate })
    }
}

