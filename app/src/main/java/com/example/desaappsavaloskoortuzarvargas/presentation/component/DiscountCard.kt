package com.example.desaappsavaloskoortuzarvargas.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.desaappsavaloskoortuzarvargas.domain.model.DiscountedGame
import com.example.desaappsavaloskoortuzarvargas.presentation.AppColors

@Composable
fun DiscountCard(
    discount: DiscountedGame,
    modifier: Modifier = Modifier,
    onGameClick: (DiscountedGame) -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onGameClick(discount) },
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            CardHeaderImage(
                imageUrl = discount.imageUrl,
                contentDescription = discount.gameName,
                height = 180.dp
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = discount.gameName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = discount.platform,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }

                    // Badge
                    when {
                        discount.isF2P -> PriceBadge(
                            text = stringResource(R.string.badge_f2p),
                            backgroundColor = AppColors.F2PBlue
                        )
                        discount.isTemporarilyFree -> PriceBadge(
                            text = stringResource(R.string.badge_free_exclaim),
                            backgroundColor = AppColors.FreeGreen
                        )
                        discount.isFree -> PriceBadge(
                            text = stringResource(R.string.badge_free),
                            backgroundColor = Color.Green
                        )
                        else -> PriceBadge(
                            text = stringResource(R.string.discount_badge_percent, discount.discountPercentage),
                            backgroundColor = Color.Red
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Price info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    when {
                        discount.isF2P -> {
                            Text(
                                text = stringResource(R.string.price_free_to_play),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.F2PBlue
                            )
                        }
                        discount.isTemporarilyFree -> {
                            Column {
                                Text(
                                    text = stringResource(R.string.price_was, discount.originalPrice),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                                Text(
                                    text = stringResource(R.string.price_free_now),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.FreeGreen
                                )
                                if (discount.endDate != null) {
                                    Text(
                                        text = stringResource(R.string.price_until, discount.endDate),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AppColors.UrgentOrange
                                    )
                                }
                            }
                        }
                        discount.isFree -> {
                            Text(
                                text = stringResource(R.string.badge_free),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Green
                            )
                        }
                        else -> {
                            Column {
                                Text(
                                    text = stringResource(R.string.game_price_usd_simple, discount.originalPrice),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                                Text(
                                    text = stringResource(R.string.game_price_usd_simple, discount.currentPrice),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Green
                                )
                            }
                        }
                    }

                    if (discount.isHistoricalLowest && !discount.isF2P) {
                        Text(
                            text = stringResource(R.string.historical_low_label),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.HistoricalGold
                        )
                    }
                }
            }
        }
    }
}
