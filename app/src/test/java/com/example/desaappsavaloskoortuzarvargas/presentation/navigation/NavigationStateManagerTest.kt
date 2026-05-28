package com.example.desaappsavaloskoortuzarvargas.presentation.navigation

import com.example.desaappsavaloskoortuzarvargas.domain.model.Game
import com.example.desaappsavaloskoortuzarvargas.domain.model.News
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class NavigationStateManagerTest {

    private lateinit var navState: NavigationStateManager

    private val sampleGame = Game(
        id = 1, name = "Test Game", description = "Desc", releaseDate = "2024",
        imageUrl = "url", rating = 8.0, currentPrices = mapOf("Steam" to 29.99f)
    )

    private val sampleGame2 = Game(
        id = 2, name = "Test Game 2", description = "Desc2", releaseDate = "2025",
        imageUrl = "url2", rating = 9.0, currentPrices = mapOf("Epic" to 19.99f)
    )

    private val sampleNews = News(
        id = 1, title = "Test News", content = "Content",
        imageUrl = "url", date = "2024-01-01", gameId = 1,
        platform = "Steam", category = "Review"
    )

    @Before
    fun setup() {
        navState = NavigationStateManager()
    }

    // ==================== Initial State ====================

    @Test
    fun `initial tab is catalog (home)`() {
        assertEquals(NavigationStateManager.TAB_CATALOG, navState.currentTab.value)
    }

    @Test
    fun `initial selected game is null`() {
        assertNull(navState.selectedGame.value)
    }

    @Test
    fun `initial selected news is null`() {
        assertNull(navState.selectedNews.value)
    }

    @Test
    fun `back is not enabled on initial state`() {
        assertFalse(navState.isBackEnabled)
    }

    // ==================== Tab Navigation ====================

    @Test
    fun `selectTab changes current tab`() {
        navState.selectTab(NavigationStateManager.TAB_OFFERS)
        assertEquals(NavigationStateManager.TAB_OFFERS, navState.currentTab.value)
    }

    @Test
    fun `selectTab to non-home tab enables back`() {
        navState.selectTab(NavigationStateManager.TAB_SETTINGS)
        assertTrue(navState.isBackEnabled)
    }

    @Test
    fun `selectTab to home tab disables back`() {
        navState.selectTab(NavigationStateManager.TAB_OFFERS)
        navState.selectTab(NavigationStateManager.TAB_CATALOG)
        assertFalse(navState.isBackEnabled)
    }

    @Test
    fun `navigate through all tabs`() {
        val tabs = listOf(
            NavigationStateManager.TAB_OFFERS,
            NavigationStateManager.TAB_CATALOG,
            NavigationStateManager.TAB_FAVORITES,
            NavigationStateManager.TAB_NEWS,
            NavigationStateManager.TAB_SETTINGS
        )
        for (tab in tabs) {
            navState.selectTab(tab)
            assertEquals(tab, navState.currentTab.value)
        }
    }

    // ==================== Game Selection ====================

    @Test
    fun `selectGame sets selected game`() {
        navState.selectGame(sampleGame)
        assertEquals(sampleGame, navState.selectedGame.value)
    }

    @Test
    fun `selectGame enables back`() {
        navState.selectGame(sampleGame)
        assertTrue(navState.isBackEnabled)
    }

    @Test
    fun `clearSelectedGame clears game`() {
        navState.selectGame(sampleGame)
        navState.clearSelectedGame()
        assertNull(navState.selectedGame.value)
    }

    @Test
    fun `updateSelectedGame replaces game`() {
        navState.selectGame(sampleGame)
        val updated = sampleGame.copy(isFavorite = true)
        navState.updateSelectedGame(updated)
        assertEquals(true, navState.selectedGame.value?.isFavorite)
    }

    // ==================== News Selection ====================

    @Test
    fun `selectNews sets selected news`() {
        navState.selectNews(sampleNews)
        assertEquals(sampleNews, navState.selectedNews.value)
    }

    @Test
    fun `selectNews enables back`() {
        navState.selectNews(sampleNews)
        assertTrue(navState.isBackEnabled)
    }

    @Test
    fun `clearSelectedNews clears news`() {
        navState.selectNews(sampleNews)
        navState.clearSelectedNews()
        assertNull(navState.selectedNews.value)
    }

    // ==================== Back Handler ====================

    @Test
    fun `handleBack from game detail clears game and returns true`() {
        navState.selectGame(sampleGame)
        val consumed = navState.handleBack()
        assertTrue(consumed)
        assertNull(navState.selectedGame.value)
    }

    @Test
    fun `handleBack from news detail clears news and returns true`() {
        navState.selectNews(sampleNews)
        val consumed = navState.handleBack()
        assertTrue(consumed)
        assertNull(navState.selectedNews.value)
    }

    @Test
    fun `handleBack from non-home tab goes to home and returns true`() {
        navState.selectTab(NavigationStateManager.TAB_SETTINGS)
        val consumed = navState.handleBack()
        assertTrue(consumed)
        assertEquals(NavigationStateManager.TAB_CATALOG, navState.currentTab.value)
    }

    @Test
    fun `handleBack from home tab returns false`() {
        val consumed = navState.handleBack()
        assertFalse(consumed)
    }

    @Test
    fun `handleBack prioritizes game over news`() {
        navState.selectNews(sampleNews)
        navState.selectGame(sampleGame)
        navState.handleBack()
        // Game should be cleared first, news still there
        assertNull(navState.selectedGame.value)
        assertNotNull(navState.selectedNews.value)
    }

    @Test
    fun `handleBack prioritizes game over tab change`() {
        navState.selectTab(NavigationStateManager.TAB_OFFERS)
        navState.selectGame(sampleGame)
        navState.handleBack()
        // Game cleared, tab stays on offers
        assertNull(navState.selectedGame.value)
        assertEquals(NavigationStateManager.TAB_OFFERS, navState.currentTab.value)
    }

    @Test
    fun `handleBack prioritizes news over tab change`() {
        navState.selectTab(NavigationStateManager.TAB_NEWS)
        navState.selectNews(sampleNews)
        navState.handleBack()
        // News cleared, tab stays
        assertNull(navState.selectedNews.value)
        assertEquals(NavigationStateManager.TAB_NEWS, navState.currentTab.value)
    }

    @Test
    fun `multiple backs from game on non-home tab returns to home`() {
        navState.selectTab(NavigationStateManager.TAB_FAVORITES)
        navState.selectGame(sampleGame)

        // First back: clears game, returns to favorites tab
        assertTrue(navState.handleBack())
        assertNull(navState.selectedGame.value)
        assertEquals(NavigationStateManager.TAB_FAVORITES, navState.currentTab.value)

        // Second back: returns to catalog (home)
        assertTrue(navState.handleBack())
        assertEquals(NavigationStateManager.TAB_CATALOG, navState.currentTab.value)

        // Third back: nothing to do, returns false (app should exit)
        assertFalse(navState.handleBack())
    }

    @Test
    fun `back from each tab goes to home`() {
        val nonHomeTabs = listOf(
            NavigationStateManager.TAB_OFFERS,
            NavigationStateManager.TAB_FAVORITES,
            NavigationStateManager.TAB_NEWS,
            NavigationStateManager.TAB_SETTINGS
        )
        for (tab in nonHomeTabs) {
            navState.selectTab(tab)
            assertTrue(navState.handleBack())
            assertEquals(NavigationStateManager.TAB_CATALOG, navState.currentTab.value)
        }
    }

    // ==================== Constants ====================

    @Test
    fun `tab constants have correct values`() {
        assertEquals(0, NavigationStateManager.TAB_OFFERS)
        assertEquals(1, NavigationStateManager.TAB_CATALOG)
        assertEquals(2, NavigationStateManager.TAB_FAVORITES)
        assertEquals(3, NavigationStateManager.TAB_NEWS)
        assertEquals(4, NavigationStateManager.TAB_SETTINGS)
    }

    @Test
    fun `default home tab is catalog`() {
        assertEquals(NavigationStateManager.TAB_CATALOG, NavigationStateManager.DEFAULT_HOME_TAB)
    }

    @Test
    fun `custom home tab works`() {
        val customNav = NavigationStateManager(homeTab = NavigationStateManager.TAB_OFFERS)
        assertEquals(NavigationStateManager.TAB_OFFERS, customNav.currentTab.value)
        assertFalse(customNav.isBackEnabled)

        customNav.selectTab(NavigationStateManager.TAB_CATALOG)
        assertTrue(customNav.isBackEnabled)
        customNav.handleBack()
        assertEquals(NavigationStateManager.TAB_OFFERS, customNav.currentTab.value)
    }

    // ==================== Edge Cases ====================

    @Test
    fun `isBackEnabled with game selected on home tab`() {
        navState.selectGame(sampleGame)
        assertTrue(navState.isBackEnabled)
    }

    @Test
    fun `clearing game on home tab disables back`() {
        navState.selectGame(sampleGame)
        navState.clearSelectedGame()
        assertFalse(navState.isBackEnabled)
    }

    @Test
    fun `selecting different games updates correctly`() {
        navState.selectGame(sampleGame)
        assertEquals(sampleGame.id, navState.selectedGame.value?.id)
        navState.selectGame(sampleGame2)
        assertEquals(sampleGame2.id, navState.selectedGame.value?.id)
    }
}


