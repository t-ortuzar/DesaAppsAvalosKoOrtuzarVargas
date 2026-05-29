package com.example.desaappsavaloskoortuzarvargas.presentation.navigation

import com.example.desaappsavaloskoortuzarvargas.domain.model.Game
import com.example.desaappsavaloskoortuzarvargas.domain.model.News
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages navigation state for the app, including tab selection,
 * detail screen navigation, and back gesture handling.
 *
 * Back behavior:
 * - From a detail screen → returns to the previous list/tab
 * - From any non-home tab → returns to the home tab (Catalog)
 * - From the home tab → does not consume the back event (app exits)
 */
class NavigationStateManager(
    val homeTab: Int = DEFAULT_HOME_TAB
) {
    private val _currentTab = MutableStateFlow(homeTab)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    private val _selectedGame = MutableStateFlow<Game?>(null)
    val selectedGame: StateFlow<Game?> = _selectedGame.asStateFlow()

    private val _selectedNews = MutableStateFlow<News?>(null)
    val selectedNews: StateFlow<News?> = _selectedNews.asStateFlow()

    /** Whether the back handler should be enabled (i.e., there's somewhere to go back to). */
    val isBackEnabled: Boolean
        get() = _selectedGame.value != null || _selectedNews.value != null || _currentTab.value != homeTab

    fun selectTab(tab: Int) {
        _currentTab.value = tab
    }

    fun selectGame(game: Game) {
        _selectedGame.value = game
    }

    fun updateSelectedGame(game: Game) {
        _selectedGame.value = game
    }

    fun selectNews(news: News) {
        _selectedNews.value = news
    }

    fun clearSelectedGame() {
        _selectedGame.value = null
    }

    fun clearSelectedNews() {
        _selectedNews.value = null
    }

    /**
     * Handles the back gesture/button press.
     * Returns true if the event was consumed, false if the system should handle it (exit app).
     */
    fun handleBack(): Boolean {
        return when {
            _selectedGame.value != null -> {
                _selectedGame.value = null
                true
            }
            _selectedNews.value != null -> {
                _selectedNews.value = null
                true
            }
            _currentTab.value != homeTab -> {
                _currentTab.value = homeTab
                true
            }
            else -> false
        }
    }

    companion object {
        const val TAB_OFFERS = 0
        const val TAB_CATALOG = 1
        const val TAB_FAVORITES = 2
        const val TAB_NEWS = 3
        const val TAB_SETTINGS = 4
        const val DEFAULT_HOME_TAB = TAB_CATALOG
    }
}
