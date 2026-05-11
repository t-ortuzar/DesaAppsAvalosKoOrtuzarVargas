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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
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
    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar
        TopAppBar(
            title = { Text(game.name) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
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

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Current Prices", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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

            // DLCs section
            if (game.dlcs.isNotEmpty()) {
                item {
                    Text("DLCs & Expansions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                game.dlcs.forEach { dlc ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = dlc.imageUrl,
                                        contentDescription = dlc.name,
                                        modifier = Modifier
                                            .height(60.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Column(modifier = Modifier.padding(start = 12.dp)) {
                                        Text(
                                            text = dlc.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = dlc.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                dlc.currentPrices.entries.take(3).forEach { (platform, price) ->
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
