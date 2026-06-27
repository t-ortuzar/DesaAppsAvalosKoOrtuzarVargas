package com.example.desaappsavaloskoortuzarvargas.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.rememberLazyListState
import com.example.desaappsavaloskoortuzarvargas.di.ServiceLocator
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.AddToFavoritesUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetAllGamesUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetAllNewsUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetCurrentDiscountsUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetFavoriteDiscountsUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetFavoritesUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetFreeGamesUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetGameByIdUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetGamesByTagUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetHistoricalLowDiscountsUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetInAppNotificationsUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetNewsByFavoritesUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetNewsByGameIdUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetPriceHistoryUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetPriceDropsUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetUnreadNotificationCountUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetUserSettingsUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GenerateDiscountNotificationsUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.MarkNotificationReadUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.RemoveFromFavoritesUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.SearchGamesUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.SetGlobalNotificationsUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.UpdateCountryUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.UpdateDarkModeUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.UpdateEmailUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.UpdateGameNotificationPrefUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.UpdateLanguageUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.UpdateUserNameUseCase
import com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel.GamesViewModel
import com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel.NewsViewModel
import com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel.OffersViewModel
import com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel.SettingsViewModel
import com.example.desaappsavaloskoortuzarvargas.presentation.navigation.NavigationStateManager
import com.example.desaappsavaloskoortuzarvargas.R

@Composable
fun MainScreen(onSignOut: () -> Unit = {}, isGuest: Boolean = false) {
    val navState = remember { NavigationStateManager() }
    val currentTab by navState.currentTab.collectAsState()
    val selectedGame by navState.selectedGame.collectAsState()
    val selectedNews by navState.selectedNews.collectAsState()

    val gameRepository        = ServiceLocator.gameRepository
    val newsRepository        = ServiceLocator.newsRepository
    val discountRepository    = ServiceLocator.discountRepository
    val userSettingsRepository = ServiceLocator.userSettingsRepository
    val priceRefreshManager   = ServiceLocator.priceRefreshManager
    val dolarService          = ServiceLocator.dolarService
    val epicPriceService      = ServiceLocator.epicPriceService
    val database              = ServiceLocator.database
    val connectivityObserver  = ServiceLocator.connectivityObserver

    // Use viewModel() so ViewModels survive configuration changes (rotation etc.)
    val gamesViewModel: GamesViewModel = viewModel {
        GamesViewModel(
            GetAllGamesUseCase(gameRepository),
            GetGameByIdUseCase(gameRepository),
            SearchGamesUseCase(gameRepository),
            AddToFavoritesUseCase(gameRepository),
            RemoveFromFavoritesUseCase(gameRepository),
            GetFavoritesUseCase(gameRepository),
            GetPriceHistoryUseCase(gameRepository),
            GetGamesByTagUseCase(gameRepository),
            priceRefreshManager,
            dolarService,
            epicPriceService,
            database.gameImageDao(),
            connectivityObserver
        )
    }

    val newsViewModel: NewsViewModel = viewModel {
        NewsViewModel(
            GetAllNewsUseCase(newsRepository),
            GetNewsByGameIdUseCase(newsRepository),
            GetNewsByFavoritesUseCase(newsRepository),
            GetFavoritesUseCase(gameRepository)
        )
    }

    val offersViewModel: OffersViewModel = viewModel {
        OffersViewModel(
            GetCurrentDiscountsUseCase(discountRepository),
            GetFavoriteDiscountsUseCase(discountRepository),
            GetHistoricalLowDiscountsUseCase(discountRepository),
            GetFreeGamesUseCase(discountRepository),
            GetFavoritesUseCase(gameRepository),
            GetPriceDropsUseCase(discountRepository)
        )
    }

    val settingsViewModel: SettingsViewModel = viewModel {
        SettingsViewModel(
            GetUserSettingsUseCase(userSettingsRepository),
            UpdateUserNameUseCase(userSettingsRepository),
            UpdateEmailUseCase(userSettingsRepository),
            UpdateCountryUseCase(userSettingsRepository),
            UpdateLanguageUseCase(userSettingsRepository),
            SetGlobalNotificationsUseCase(userSettingsRepository),
            UpdateGameNotificationPrefUseCase(userSettingsRepository),
            GetInAppNotificationsUseCase(userSettingsRepository),
            GetUnreadNotificationCountUseCase(userSettingsRepository),
            MarkNotificationReadUseCase(userSettingsRepository),
            GenerateDiscountNotificationsUseCase(userSettingsRepository),
            GetFavoritesUseCase(gameRepository),
            UpdateDarkModeUseCase(userSettingsRepository)
        )
    }

    val unreadCount by settingsViewModel.unreadCount.collectAsState()
    val isOnline    by gamesViewModel.isOnline.collectAsState()

    // Start periodic background price refresh and populate all lookup maps.
    // Also reload user settings fresh from DataStore each time MainScreen enters composition —
    // this ensures that after a sign-out + sign-in (or guest login), the SettingsViewModel
    // shows the reset defaults ("Player", dark mode, etc.) rather than stale in-memory state
    // from the previous user's session.
    LaunchedEffect(Unit) {
        settingsViewModel.loadSettings()  // force fresh DataStore read on every MainScreen entry
        val catalog = com.example.desaappsavaloskoortuzarvargas.data.catalog.GameCatalog
        priceRefreshManager.setSteamAppIds(catalog.getSteamAppIdsByName())
        priceRefreshManager.setGamePlatforms(catalog.getGamePlatformsByName())
        priceRefreshManager.setXboxProductIds(catalog.getXboxProductIdsByName())
        priceRefreshManager.setXboxTitleHints(catalog.getXboxTitleHintsByName())
        priceRefreshManager.setPerStoreSearchHints(catalog.getPerStoreSearchHintsByName())
        priceRefreshManager.setEpicVerifiedUrls(catalog.getEpicUrlsByName())
        priceRefreshManager.setEaGameUrls(catalog.getEaGameUrlsByName())
        priceRefreshManager.setUbisoftVerifiedUrls(catalog.getUbisoftUrlsByName())
        priceRefreshManager.setBattleNetSlugs(catalog.getBattleNetSlugsByName())
        priceRefreshManager.startPeriodicRefresh()
        val allNames = catalog.generateGames()
            .filter { !it.tags.contains("Free2Play") }
            .map { it.name }
        priceRefreshManager.ensureAllGamesCached(allNames) {
            offersViewModel.loadCurrentDiscounts()
            offersViewModel.loadFreeGames()
        }
        offersViewModel.loadCurrentDiscounts()
        offersViewModel.loadFreeGames()
    }

    val catalogListState = rememberLazyListState()

    BackHandler(enabled = navState.isBackEnabled) {
        navState.handleBack()
    }

    // Detail screens
    when {
        selectedGame != null -> {
            GameDetailScreen(
                game = selectedGame!!,
                viewModel = gamesViewModel,
                onBackClick = { navState.clearSelectedGame() },
                onFavoriteClick = { game ->
                    gamesViewModel.toggleFavorite(game)
                    navState.updateSelectedGame(game.copy(isFavorite = !game.isFavorite))
                }
            )
            return
        }
        selectedNews != null -> {
            NewsDetailScreen(
                news = selectedNews!!,
                onBackClick = { navState.clearSelectedNews() }
            )
            return
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AnimatedVisibility(
                visible = !isOnline,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFF6B00))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.SignalWifiOff,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = stringResource(R.string.offline_banner),
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar {
                val iconColor = MaterialTheme.colorScheme.primary

                NavigationBarItem(
                    icon = { Icon(Icons.Filled.LocalOffer, stringResource(R.string.nav_offers)) },
                    label = { Text(stringResource(R.string.nav_offers)) },
                    selected = currentTab == NavigationStateManager.TAB_OFFERS,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor   = iconColor,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedTextColor   = iconColor,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    onClick = { navState.selectTab(NavigationStateManager.TAB_OFFERS) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Search, stringResource(R.string.nav_catalog)) },
                    label = { Text(stringResource(R.string.nav_catalog)) },
                    selected = currentTab == NavigationStateManager.TAB_CATALOG,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor   = iconColor,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedTextColor   = iconColor,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    onClick = { navState.selectTab(NavigationStateManager.TAB_CATALOG) }
                )
                // ── Notifications tab (replaces Favorites) ──
                NavigationBarItem(
                    icon = {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge { Text("$unreadCount") }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Filled.Notifications,
                                contentDescription = stringResource(R.string.nav_notifications)
                            )
                        }
                    },
                    label = { Text(stringResource(R.string.nav_notifications)) },
                    selected = currentTab == NavigationStateManager.TAB_NOTIFICATIONS,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor   = iconColor,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedTextColor   = iconColor,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    onClick = {
                        navState.selectTab(NavigationStateManager.TAB_NOTIFICATIONS)
                        gamesViewModel.loadFavorites()
                        settingsViewModel.refreshNotifications()
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Newspaper, stringResource(R.string.nav_news)) },
                    label = { Text(stringResource(R.string.nav_news)) },
                    selected = currentTab == NavigationStateManager.TAB_NEWS,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor   = iconColor,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedTextColor   = iconColor,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    onClick = { navState.selectTab(NavigationStateManager.TAB_NEWS) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Settings, stringResource(R.string.nav_settings)) },
                    label = { Text(stringResource(R.string.nav_settings)) },
                    selected = currentTab == NavigationStateManager.TAB_SETTINGS,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor   = iconColor,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedTextColor   = iconColor,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    onClick = {
                        navState.selectTab(NavigationStateManager.TAB_SETTINGS)
                        settingsViewModel.refreshNotifications()
                    }
                )
            }
        }
    ) { paddingValues ->
        val dolarRate by gamesViewModel.dolarTarjetaRate.collectAsState()
        when (currentTab) {
            NavigationStateManager.TAB_OFFERS -> OffersScreen(
                viewModel = offersViewModel,
                onDiscountSelected = { discount ->
                    val game = gamesViewModel.allGames.value.find { it.id == discount.gameId }
                    if (game != null) navState.selectGame(game)
                },
                modifier = Modifier.padding(paddingValues),
                dolarRate = dolarRate,
                convertToArs = { gamesViewModel.convertToArs(it) }
            )
            NavigationStateManager.TAB_CATALOG -> GamesScreen(
                viewModel = gamesViewModel,
                onGameSelected = { navState.selectGame(it) },
                modifier = Modifier.padding(paddingValues),
                listState = catalogListState
            )
            NavigationStateManager.TAB_NOTIFICATIONS -> NotificationsScreen(
                gamesViewModel = gamesViewModel,
                settingsViewModel = settingsViewModel,
                onGameSelected = { navState.selectGame(it) },
                modifier = Modifier.padding(paddingValues)
            )
            NavigationStateManager.TAB_NEWS -> NewsScreen(
                viewModel = newsViewModel,
                onNewsSelected = { navState.selectNews(it) },
                onGameClicked = { gameId ->
                    val game = gamesViewModel.allGames.value.find { it.id == gameId }
                    if (game != null) navState.selectGame(game)
                },
                modifier = Modifier.padding(paddingValues)
            )
            NavigationStateManager.TAB_SETTINGS -> SettingsScreen(
                settingsViewModel = settingsViewModel,
                onSignOut = onSignOut,
                isGuest = isGuest,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}
