package com.example.desaappsavaloskoortuzarvargas.presentation.screen

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.desaappsavaloskoortuzarvargas.R
import com.example.desaappsavaloskoortuzarvargas.domain.model.Game
import com.example.desaappsavaloskoortuzarvargas.presentation.POPULAR_TAGS
import com.example.desaappsavaloskoortuzarvargas.presentation.STORE_PLATFORMS
import com.example.desaappsavaloskoortuzarvargas.presentation.component.GameCard
import com.example.desaappsavaloskoortuzarvargas.presentation.component.LoadingContent
import com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel.GamesViewModel
import com.example.desaappsavaloskoortuzarvargas.ui.theme.AccentCyan
import java.util.Locale

@Composable
fun GamesScreen(
    viewModel: GamesViewModel,
    onGameSelected: (Game) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    val allGames by viewModel.allGames.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()
    val selectedStore by viewModel.selectedStore.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // Apply store filter client-side on top of tag/search results
    val displayedGames = remember(allGames, selectedStore) {
        if (selectedStore == null) allGames
        else allGames.filter { it.availablePlatforms.contains(selectedStore) }
    }

    // Voice recognition launcher — uses Google's built-in speech-to-text
    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                searchQuery = spoken
                viewModel.searchGames(spoken)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search bar with mic button
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
            trailingIcon = {
                IconButton(onClick = {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Buscá un juego por voz")
                        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    }
                    voiceLauncher.launch(intent)
                }) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "Búsqueda por voz",
                        tint = AccentCyan
                    )
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Genre / tag filter chips
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
            POPULAR_TAGS.forEach { tag ->
                FilterChip(
                    onClick = {
                        if (selectedTag == tag) viewModel.clearTagFilter()
                        else viewModel.filterByTag(tag)
                    },
                    label = { Text(tag) },
                    selected = selectedTag == tag
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Store filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                onClick = { viewModel.clearStoreFilter() },
                label = { Text(stringResource(R.string.filter_all_stores)) },
                selected = selectedStore == null
            )
            STORE_PLATFORMS.forEach { store ->
                FilterChip(
                    onClick = { viewModel.filterByStore(store) },
                    label = { Text(store) },
                    selected = selectedStore == store
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LoadingContent(
            isLoading = isLoading,
            items = displayedGames,
            emptyMessage = stringResource(R.string.games_no_results)
        ) { games ->
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(games) { game ->
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
