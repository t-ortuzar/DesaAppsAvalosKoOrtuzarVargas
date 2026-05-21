package com.example.desaappsavaloskoortuzarvargas.presentation.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.desaappsavaloskoortuzarvargas.R
import com.example.desaappsavaloskoortuzarvargas.domain.model.DiscountedGame
import com.example.desaappsavaloskoortuzarvargas.presentation.STORE_PLATFORMS
import com.example.desaappsavaloskoortuzarvargas.presentation.component.DiscountCard
import com.example.desaappsavaloskoortuzarvargas.presentation.component.LoadingContent
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
    val freeFilter by viewModel.freeFilter.collectAsState()
    val selectedPlatform by viewModel.selectedPlatform.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        R.string.offers_tab_discounts,
        R.string.offers_tab_favorites,
        R.string.offers_tab_hist_low,
        R.string.offers_tab_free
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, titleRes ->
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
                    text = { Text(stringResource(titleRes)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Platform filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                onClick = { viewModel.setPlatformFilter(null) },
                label = { Text(stringResource(R.string.label_all)) },
                selected = selectedPlatform == null
            )
            STORE_PLATFORMS.forEach { platform ->
                FilterChip(
                    onClick = {
                        viewModel.setPlatformFilter(
                            if (selectedPlatform == platform) null else platform
                        )
                    },
                    label = { Text(platform) },
                    selected = selectedPlatform == platform
                )
            }
        }

        // Free tab sub-filter: F2P vs Temporarily Free
        if (selectedTabIndex == 3) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    onClick = { viewModel.setFreeFilter(OffersViewModel.FreeFilter.TEMP_FREE_ONLY) },
                    label = { Text(stringResource(R.string.offers_filter_temp_free)) },
                    selected = freeFilter == OffersViewModel.FreeFilter.TEMP_FREE_ONLY
                )
                FilterChip(
                    onClick = { viewModel.setFreeFilter(OffersViewModel.FreeFilter.F2P_ONLY) },
                    label = { Text(stringResource(R.string.offers_filter_f2p)) },
                    selected = freeFilter == OffersViewModel.FreeFilter.F2P_ONLY
                )
                FilterChip(
                    onClick = { viewModel.setFreeFilter(OffersViewModel.FreeFilter.ALL) },
                    label = { Text(stringResource(R.string.offers_filter_all_free)) },
                    selected = freeFilter == OffersViewModel.FreeFilter.ALL
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val displayedDiscounts = when (selectedTabIndex) {
            0 -> currentDiscounts
            1 -> favoriteDiscounts
            2 -> historicalLowDiscounts
            3 -> freeGames
            else -> currentDiscounts
        }

        LoadingContent(
            isLoading = isLoading,
            items = displayedDiscounts,
            emptyMessage = stringResource(R.string.offers_no_discounts)
        ) { discounts ->
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(discounts) { discount ->
                    DiscountCard(
                        discount = discount,
                        onGameClick = onDiscountSelected
                    )
                }
            }
        }
    }
}
