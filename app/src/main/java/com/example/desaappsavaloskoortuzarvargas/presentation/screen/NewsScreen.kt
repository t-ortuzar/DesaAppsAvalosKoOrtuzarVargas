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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.desaappsavaloskoortuzarvargas.domain.model.News
import com.example.desaappsavaloskoortuzarvargas.presentation.component.NewsCard
import com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel.NewsViewModel

@Composable
fun NewsScreen(
    viewModel: NewsViewModel,
    onNewsSelected: (News) -> Unit,
    modifier: Modifier = Modifier
) {
    val allNews by viewModel.allNews.collectAsState()
    val favoritesNews by viewModel.favoritesNews.collectAsState()
    val filterType by viewModel.filterType.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val displayedNews = when (filterType) {
        NewsViewModel.FilterType.ALL -> allNews
        NewsViewModel.FilterType.FAVORITES -> favoritesNews
        NewsViewModel.FilterType.BY_GAME -> favoritesNews
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Filter buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.resetFilter() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("All News")
            }
            Button(
                onClick = { viewModel.loadFavoritesNews() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Favorites News")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
            displayedNews.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("No news found")
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayedNews) { news ->
                        NewsCard(
                            news = news,
                            onNewsClick = onNewsSelected
                        )
                    }
                }
            }
        }
    }
}


