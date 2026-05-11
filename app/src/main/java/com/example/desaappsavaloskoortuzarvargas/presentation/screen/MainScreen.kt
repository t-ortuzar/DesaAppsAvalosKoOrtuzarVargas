package com.example.desaappsavaloskoortuzarvargas.presentation.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.desaappsavaloskoortuzarvargas.di.ServiceLocator
import com.example.desaappsavaloskoortuzarvargas.domain.model.Game
import com.example.desaappsavaloskoortuzarvargas.domain.model.News
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
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetUnreadNotificationCountUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetUserSettingsUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GenerateDiscountNotificationsUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.MarkNotificationReadUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.RemoveFromFavoritesUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.SearchGamesUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.SetGlobalNotificationsUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.UpdateCountryUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.UpdateEmailUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.UpdateGameNotificationPrefUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.UpdateUserNameUseCase
import com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel.GamesViewModel
import com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel.NewsViewModel
import com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel.OffersViewModel
import com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel.SettingsViewModel

@Composable
fun MainScreen() {
    var currentTab by remember { mutableIntStateOf(1) } // Catalog in the middle
    var selectedGame by remember { mutableStateOf<Game?>(null) }
    var selectedNews by remember { mutableStateOf<News?>(null) }

    val gameRepository = ServiceLocator.gameRepository
    val newsRepository = ServiceLocator.newsRepository
    val discountRepository = ServiceLocator.discountRepository
    val userSettingsRepository = ServiceLocator.userSettingsRepository
    val cheapSharkService = ServiceLocator.cheapSharkService

    val gamesViewModel = remember {
        GamesViewModel(
            GetAllGamesUseCase(gameRepository),
            GetGameByIdUseCase(gameRepository),
            SearchGamesUseCase(gameRepository),
            AddToFavoritesUseCase(gameRepository),
            RemoveFromFavoritesUseCase(gameRepository),
            GetFavoritesUseCase(gameRepository),
            GetPriceHistoryUseCase(gameRepository),
            GetGamesByTagUseCase(gameRepository),
            cheapSharkService
        )
    }

    val newsViewModel = remember {
        NewsViewModel(
            GetAllNewsUseCase(newsRepository),
            GetNewsByGameIdUseCase(newsRepository),
            GetNewsByFavoritesUseCase(newsRepository),
            GetFavoritesUseCase(gameRepository)
        )
    }

    val offersViewModel = remember {
        OffersViewModel(
            GetCurrentDiscountsUseCase(discountRepository),
            GetFavoriteDiscountsUseCase(discountRepository),
            GetHistoricalLowDiscountsUseCase(discountRepository),
            GetFreeGamesUseCase(discountRepository),
            GetFavoritesUseCase(gameRepository)
        )
    }

    val settingsViewModel = remember {
        SettingsViewModel(
            GetUserSettingsUseCase(userSettingsRepository),
            UpdateUserNameUseCase(userSettingsRepository),
            UpdateEmailUseCase(userSettingsRepository),
            UpdateCountryUseCase(userSettingsRepository),
            SetGlobalNotificationsUseCase(userSettingsRepository),
            UpdateGameNotificationPrefUseCase(userSettingsRepository),
            GetInAppNotificationsUseCase(userSettingsRepository),
            GetUnreadNotificationCountUseCase(userSettingsRepository),
            MarkNotificationReadUseCase(userSettingsRepository),
            GenerateDiscountNotificationsUseCase(userSettingsRepository),
            GetFavoritesUseCase(gameRepository)
        )
    }

    val unreadCount by settingsViewModel.unreadCount.collectAsState()

    // Detail screens
    when {
        selectedGame != null -> {
            GameDetailScreen(
                game = selectedGame!!,
                viewModel = gamesViewModel,
                settingsViewModel = settingsViewModel,
                onBackClick = { selectedGame = null },
                onFavoriteClick = { game ->
                    gamesViewModel.toggleFavorite(game)
                    selectedGame = game.copy(isFavorite = !game.isFavorite)
                }
            )
            return
        }
        selectedNews != null -> {
            NewsDetailScreen(
                news = selectedNews!!,
                onBackClick = { selectedNews = null }
            )
            return
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.LocalOffer, contentDescription = "Offers") },
                    label = { Text("Offers") },
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.VideogameAsset, contentDescription = "Catalog") },
                    label = { Text("Catalog") },
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Newspaper, contentDescription = "News") },
                    label = { Text("News") },
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 }
                )
                NavigationBarItem(
                    icon = {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge { Text("$unreadCount") }
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings")
                        }
                    },
                    label = { Text("Settings") },
                    selected = currentTab == 3,
                    onClick = {
                        currentTab = 3
                        settingsViewModel.refreshNotifications()
                    }
                )
            }
        }
    ) { paddingValues ->
        when (currentTab) {
            0 -> OffersScreen(
                viewModel = offersViewModel,
                onDiscountSelected = { discount ->
                    val game = gamesViewModel.allGames.value.find { it.id == discount.gameId }
                    if (game != null) {
                        selectedGame = game
                    }
                },
                modifier = Modifier.padding(paddingValues)
            )
            1 -> GamesScreen(
                viewModel = gamesViewModel,
                onGameSelected = { selectedGame = it },
                modifier = Modifier.padding(paddingValues)
            )
            2 -> NewsScreen(
                viewModel = newsViewModel,
                onNewsSelected = { selectedNews = it },
                onGameClicked = { gameId ->
                    val game = gamesViewModel.allGames.value.find { it.id == gameId }
                    if (game != null) {
                        selectedGame = game
                    }
                },
                modifier = Modifier.padding(paddingValues)
            )
            3 -> SettingsScreen(
                settingsViewModel = settingsViewModel,
                gamesViewModel = gamesViewModel,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}
