package com.example.desaappsavaloskoortuzarvargas.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.desaappsavaloskoortuzarvargas.R
import com.example.desaappsavaloskoortuzarvargas.domain.model.Game
import java.util.Locale

@Composable
fun GameCard(
    game: Game,
    modifier: Modifier = Modifier,
    onGameClick: (Game) -> Unit,
    onFavoriteClick: (Game) -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onGameClick(game) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            CardHeaderImage(
                imageUrl = game.imageUrl,
                contentDescription = game.name,
                height = 200.dp
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = game.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = stringResource(R.string.content_desc_rating),
                                tint = Color.Yellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = String.format(Locale.US, "%.1f", game.rating),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                            Text(
                                text = stringResource(R.string.game_rating_source),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }

                    FavoriteButton(game = game, onClick = { onFavoriteClick(game) })
                }

                // Tags
                if (game.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    TagChips(tags = game.tags)
                }

                if (game.historicalDiscount > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.game_historical_discount_short, game.historicalDiscount),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Green,
                        fontWeight = FontWeight.Bold
                    )
                }

                // DLC count
                if (game.dlcs.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.game_dlcs_available, game.dlcs.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Platform chips — show available stores for all games (F2P and paid).
                // Real prices are fetched from store APIs on the detail screen.
                val isF2P = game.tags.contains("Free2Play")
                if (isF2P) {
                    Text(
                        text = stringResource(R.string.game_free_to_play_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
                if (game.availablePlatforms.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        game.availablePlatforms.forEach { platform ->
                            Text(
                                text = platform,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameCardSmall(
    game: Game,
    modifier: Modifier = Modifier,
    onGameClick: (Game) -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onGameClick(game) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(6.dp)
    ) {
        Column {
            CardHeaderImage(
                imageUrl = game.imageUrl,
                contentDescription = game.name,
                height = 120.dp,
                cornerRadius = 6.dp
            )

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = game.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.game_rating_star, game.rating),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * Reusable favorite toggle button.
 */
@Composable
fun FavoriteButton(
    game: Game,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(40.dp)
    ) {
        Icon(
            imageVector = if (game.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = stringResource(R.string.content_desc_favorite),
            tint = if (game.isFavorite) Color.Red else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
    }
}

