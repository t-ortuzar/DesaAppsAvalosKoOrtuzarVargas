package com.example.desaappsavaloskoortuzarvargas.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.desaappsavaloskoortuzarvargas.domain.model.Game
import com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel.GamesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailScreen(
    game: Game,
    viewModel: GamesViewModel,
    onBackClick: () -> Unit,
    onFavoriteClick: (Game) -> Unit
) {
    val realPrices by viewModel.realPrices.collectAsState()
    val isLoadingPrices by viewModel.isLoadingPrices.collectAsState()

    // Load real prices when entering the screen
    LaunchedEffect(game.id) {
        viewModel.loadRealPrices(game.name)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar
        TopAppBar(
            title = { Text(game.name) },
            navigationIcon = {
                IconButton(onClick = {
                    viewModel.clearRealPrices()
                    onBackClick()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { onFavoriteClick(game) }) {
                    Icon(
                        imageVector = if (game.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (game.isFavorite) Color.Red else Color.Gray
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AsyncImage(
                    model = game.imageUrl,
                    contentDescription = game.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // Tags
            if (game.tags.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        game.tags.forEach { tag ->
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Available platforms
            if (game.availablePlatforms.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Available on", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            game.availablePlatforms.forEach { platform ->
                                Text(
                                    text = platform,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Release Date", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(game.releaseDate, style = MaterialTheme.typography.bodyMedium)
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Rating", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${"%.1f".format(game.rating)} / 10", style = MaterialTheme.typography.bodyMedium)
                }
            }

            item {
                if (game.historicalDiscount > 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Historical Discount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${game.historicalDiscount}%", style = MaterialTheme.typography.bodyMedium, color = Color.Green)
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(game.description, style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Real prices from CheapShark API
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Prices (Live from CheapShark)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (isLoadingPrices) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        }
                    }

                    if (realPrices.isNotEmpty()) {
                        realPrices.sortedBy { it.currentPrice }.forEach { price ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(1.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(price.storeName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        if (price.savings > 0) {
                                            Text(
                                                text = "${"%.0f".format(price.savings)}% off",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.Green
                                            )
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = if (price.currentPrice > 0) "$${"%.2f".format(price.currentPrice)}" else "FREE",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (price.currentPrice > 0) MaterialTheme.colorScheme.primary else Color.Green
                                        )
                                        if (price.retailPrice > price.currentPrice) {
                                            Text(
                                                text = "$${"%.2f".format(price.retailPrice)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (!isLoadingPrices) {
                        Text(
                            "No live data available — showing cached prices",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        // Fallback to mock prices (only available platforms)
                        game.currentPrices.entries.forEach { entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(entry.key, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = if (entry.value > 0) "$${"%.2f".format(entry.value)}" else "N/A",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // DLCs section
            if (game.dlcs.isNotEmpty()) {
                item {
                    Text("DLCs & Expansions (${game.dlcs.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                game.dlcs.forEach { dlc ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = dlc.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                if (dlc.description.isNotEmpty()) {
                                    Text(
                                        text = dlc.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray,
                                        maxLines = 2
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                // Only show DLC prices for platforms where the game is available
                                val dlcAvailablePrices = if (game.availablePlatforms.isNotEmpty()) {
                                    dlc.currentPrices.filter { it.key in game.availablePlatforms }
                                } else {
                                    dlc.currentPrices
                                }
                                dlcAvailablePrices.entries.take(3).forEach { (platform, price) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(platform, style = MaterialTheme.typography.labelSmall)
                                        Text(
                                            "$${"%.2f".format(price)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                if (dlc.historicalDiscount > 0) {
                                    Text(
                                        text = "Hist. discount: ${dlc.historicalDiscount}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Green,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
