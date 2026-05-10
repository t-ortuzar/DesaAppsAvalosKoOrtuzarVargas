package com.example.desaappsavaloskoortuzarvargas.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.desaappsavaloskoortuzarvargas.domain.model.DiscountedGame
import com.example.desaappsavaloskoortuzarvargas.presentation.component.DiscountCard
import com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel.OffersViewModel

@Composable
fun OffersScreen(
    viewModel: OffersViewModel,
    onDiscountSelected: (DiscountedGame) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentDiscounts by viewModel.currentDiscounts.collectAsState()
    val favoriteDiscounts by viewModel.favoriteDiscounts.collectAsState()
    val historicalLowDiscounts by viewModel.historicalLowDiscounts.collectAsState()
    val freeGames by viewModel.freeGames.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val filterType by viewModel.filterType.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val tabs = listOf("All Discounts", "Favorite\nDiscounts", "Historical\nLow", "Free")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tab row
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = {
                        selectedTabIndex = index
                        when (index) {
                            0 -> viewModel.loadCurrentDiscounts()
                            1 -> viewModel.loadFavoriteDiscounts()
                            2 -> viewModel.loadHistoricalLowDiscounts()
                            3 -> viewModel.showFreeGames()
                        }
                    },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val displayedDiscounts = when (selectedTabIndex) {
            0 -> currentDiscounts
            1 -> favoriteDiscounts
            2 -> historicalLowDiscounts
            3 -> freeGames
            else -> currentDiscounts
        }

        when {
            isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            displayedDiscounts.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("No discounts available")
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayedDiscounts) { discount ->
                        DiscountCard(
                            discount = discount,
                            onGameClick = onDiscountSelected
                        )
                    }
                }
            }
        }
    }
}


