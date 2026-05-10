package com.example.desaappsavaloskoortuzarvargas.presentation.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.desaappsavaloskoortuzarvargas.di.ServiceLocator
import com.example.desaappsavaloskoortuzarvargas.domain.model.DiscountedGame
import com.example.desaappsavaloskoortuzarvargas.domain.model.Game
import com.example.desaappsavaloskoortuzarvargas.domain.model.News
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.AddToFavoritesUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetAllGamesUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetFavoritesUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetGameByIdUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetPriceHistoryUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.RemoveFromFavoritesUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.SearchGamesUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetAllNewsUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetNewsByGameIdUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetNewsByFavoritesUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetCurrentDiscountsUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetFavoriteDiscountsUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetHistoricalLowDiscountsUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetFreeGamesUseCase
import com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel.GamesViewModel
import com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel.NewsViewModel
import com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel.OffersViewModel

@Composable
fun MainScreen() {
    var currentTab by remember { mutableIntStateOf(0) }
    var selectedGame by remember { mutableStateOf<Game?>(null) }
    var selectedNews by remember { mutableStateOf<News?>(null) }
    var selectedDiscount by remember { mutableStateOf<DiscountedGame?>(null) }

    // Create use cases
    val gameRepository = ServiceLocator.gameRepository
    val newsRepository = ServiceLocator.newsRepository
    val discountRepository = ServiceLocator.discountRepository

    val gamesViewModel = remember {
        GamesViewModel(
            GetAllGamesUseCase(gameRepository),
            GetGameByIdUseCase(gameRepository),
            SearchGamesUseCase(gameRepository),
            AddToFavoritesUseCase(gameRepository),
            RemoveFromFavoritesUseCase(gameRepository),
            GetFavoritesUseCase(gameRepository),
            GetPriceHistoryUseCase(gameRepository)
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

    // If a game, news, or discount is selected, show the detail screen
    when {
        selectedGame != null -> {
            GameDetailScreen(
                game = selectedGame!!,
                viewModel = gamesViewModel,
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
        selectedDiscount != null -> {
            // For now, we'll just go back when a discount is selected
            // In a real app, you might show more details
            selectedDiscount = null
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Games") },
                    label = { Text("Catalog") },
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Favorite, contentDescription = "News") },
                    label = { Text("News") },
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.LocalOffer, contentDescription = "Offers") },
                    label = { Text("Offers") },
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 }
                )
            }
        }
    ) { paddingValues ->
        when (currentTab) {
            0 -> GamesScreen(
                viewModel = gamesViewModel,
                onGameSelected = { selectedGame = it },
                modifier = Modifier.padding(paddingValues)
            )
            1 -> NewsScreen(
                viewModel = newsViewModel,
                onNewsSelected = { selectedNews = it },
                modifier = Modifier.padding(paddingValues)
            )
            2 -> OffersScreen(
                viewModel = offersViewModel,
                onDiscountSelected = { selectedDiscount = it },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}
