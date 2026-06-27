package com.example.desaappsavaloskoortuzarvargas.presentation.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.desaappsavaloskoortuzarvargas.R
import com.example.desaappsavaloskoortuzarvargas.domain.model.News
import com.example.desaappsavaloskoortuzarvargas.presentation.component.LoadingContent
import com.example.desaappsavaloskoortuzarvargas.presentation.component.NewsCard
import com.example.desaappsavaloskoortuzarvargas.presentation.component.UpcomingSaleCard
import com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel.NewsViewModel

@Composable
fun NewsScreen(
    viewModel: NewsViewModel,
    onNewsSelected: (News) -> Unit,
    onGameClicked: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val allNews by viewModel.allNews.collectAsState()
    val favoritesNews by viewModel.favoritesNews.collectAsState()
    val filterType by viewModel.filterType.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val upcomingSales by viewModel.upcomingSales.collectAsState()

    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val displayedNews = when (filterType) {
        NewsViewModel.FilterType.ALL -> allNews
        NewsViewModel.FilterType.FAVORITES -> favoritesNews
        NewsViewModel.FilterType.BY_GAME -> favoritesNews
    }.let { news ->
        if (selectedCategory != null) news.filter { it.category == selectedCategory } else news
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // ── Upcoming Sales section ────────────────────────────────────────────
        if (upcomingSales.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.news_upcoming_sales_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.news_upcoming_sales_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(end = 8.dp)
                ) {
                    items(upcomingSales) { event ->
                        UpcomingSaleCard(event = event)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // ── Filters ───────────────────────────────────────────────────────────
        item {
            // Source filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    onClick = { viewModel.resetFilter() },
                    label = { Text(stringResource(R.string.label_all)) },
                    selected = filterType == NewsViewModel.FilterType.ALL
                )
                FilterChip(
                    onClick = { viewModel.loadFavoritesNews() },
                    label = { Text(stringResource(R.string.label_favorites)) },
                    selected = filterType == NewsViewModel.FilterType.FAVORITES
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Category filter
            val categories = listOf(
                "discount" to stringResource(R.string.news_category_discounts),
                "update"   to stringResource(R.string.news_category_updates),
                "event"    to stringResource(R.string.news_category_events)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    onClick = { selectedCategory = null },
                    label = { Text(stringResource(R.string.news_filter_all_categories)) },
                    selected = selectedCategory == null
                )
                categories.forEach { (cat, label) ->
                    FilterChip(
                        onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                        label = { Text(label) },
                        selected = selectedCategory == cat
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // ── News list ─────────────────────────────────────────────────────────
        if (isLoading) {
            item {
                LoadingContent(
                    isLoading = true,
                    items = emptyList<News>(),
                    emptyMessage = stringResource(R.string.news_no_results)
                ) { }
            }
        } else if (displayedNews.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.news_no_results),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        } else {
            items(displayedNews, key = { it.id }) { newsItem ->
                NewsCard(
                    news = newsItem,
                    onNewsClick = onNewsSelected,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }
    }
}
