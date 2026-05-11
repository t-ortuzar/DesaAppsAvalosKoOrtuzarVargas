package com.example.desaappsavaloskoortuzarvargas.presentation.component

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.desaappsavaloskoortuzarvargas.domain.model.DiscountedGame

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
            // Image with discount badge
            AsyncImage(
                model = discount.imageUrl,
                contentDescription = discount.gameName,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                contentScale = ContentScale.Crop
            )

            // Content
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
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
                        discount.isF2P -> {
                            Text(
                                text = "F2P",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier
                                    .background(Color(0xFF2196F3), shape = RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        discount.isTemporarilyFree -> {
                            Text(
                                text = "FREE!",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier
                                    .background(Color(0xFF4CAF50), shape = RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        discount.isFree -> {
                            Text(
                                text = "FREE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier
                                    .background(Color.Green, shape = RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        else -> {
                            Text(
                                text = "-${discount.discountPercentage}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier
                                    .background(Color.Red, shape = RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
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
                                text = "Free to Play",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2196F3)
                            )
                        }
                        discount.isTemporarilyFree -> {
                            Column {
                                Text(
                                    text = "Was: $${"%.2f".format(discount.originalPrice)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "FREE NOW",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50)
                                )
                                if (discount.endDate != null) {
                                    Text(
                                        text = "Until: ${discount.endDate}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFFF9800)
                                    )
                                }
                            }
                        }
                        discount.isFree -> {
                            Text(
                                text = "FREE",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Green
                            )
                        }
                        else -> {
                            Column {
                                Text(
                                    text = "$${"%.2f".format(discount.originalPrice)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "$${"%.2f".format(discount.currentPrice)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Green
                                )
                            }
                        }
                    }

                    if (discount.isHistoricalLowest && !discount.isF2P) {
                        Text(
                            text = "★ Historical Low",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                    }
                }
            }
        }
    }
}
