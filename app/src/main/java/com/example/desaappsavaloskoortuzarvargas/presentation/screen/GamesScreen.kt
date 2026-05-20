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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.desaappsavaloskoortuzarvargas.R
import com.example.desaappsavaloskoortuzarvargas.domain.model.Game
import com.example.desaappsavaloskoortuzarvargas.presentation.component.GameCard
import com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel.GamesViewModel

@Composable
fun GamesScreen(
    viewModel: GamesViewModel,
    onGameSelected: (Game) -> Unit,
    modifier: Modifier = Modifier
) {
    val allGames by viewModel.allGames.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // Popular tags to show as filters
    val popularTags = listOf("Action", "RPG", "FPS", "Open World", "Horror", "Survival",
        "Co-op", "Indie", "Puzzle", "Racing", "Sports", "Souls-like", "Roguelike", "Strategy")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search bar
        TextField(
            value = searchQuery,
            onValueChange = { query ->
                searchQuery = query
                viewModel.searchGames(query)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.games_search_placeholder)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = stringResource(R.string.action_search)
                )
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Tag filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                onClick = { viewModel.clearTagFilter(); searchQuery = "" },
                label = { Text(stringResource(R.string.label_all)) },
                selected = selectedTag == null
            )
            popularTags.forEach { tag ->
                FilterChip(
                    onClick = {
                        if (selectedTag == tag) {
                            viewModel.clearTagFilter()
                        } else {
                            viewModel.filterByTag(tag)
                        }
                    },
                    label = { Text(tag) },
                    selected = selectedTag == tag
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when {
            isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) { CircularProgressIndicator() }
            }
            allGames.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) { Text(stringResource(R.string.games_no_results)) }
            }
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(allGames) { game ->
                        GameCard(
                            game = game,
                            onGameClick = onGameSelected,
                            onFavoriteClick = { viewModel.toggleFavorite(it) }
                        )
                    }
                }
            }
        }
    }
}
