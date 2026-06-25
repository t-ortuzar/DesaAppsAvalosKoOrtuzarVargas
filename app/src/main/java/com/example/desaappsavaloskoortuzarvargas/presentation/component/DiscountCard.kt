package com.example.desaappsavaloskoortuzarvargas.presentation.component

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.desaappsavaloskoortuzarvargas.R
import com.example.desaappsavaloskoortuzarvargas.data.api.ArgentineTaxCalculator
import com.example.desaappsavaloskoortuzarvargas.domain.model.DiscountedGame
import com.example.desaappsavaloskoortuzarvargas.domain.model.OfferType
import com.example.desaappsavaloskoortuzarvargas.presentation.AppColors
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DiscountCard(
    discount: DiscountedGame,
    modifier: Modifier = Modifier,
    onGameClick: (DiscountedGame) -> Unit,
    showInArs: Boolean = false,
    dolarRate: Double? = null,
    convertToArs: (Float) -> Float = { it }
) {
    val context = LocalContext.current

    fun openStoreUrl(url: String) {
        if (url.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                if (discount.storeUrl.isNotEmpty()) {
                    openStoreUrl(discount.storeUrl)
                } else {
                    onGameClick(discount)
                }
            },
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
                        discount.offerType == OfferType.PERMANENT_PRICE_DROP -> PriceBadge(
                            text = stringResource(R.string.badge_price_drop),
                            backgroundColor = AppColors.PriceDropPurple
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

                // Price info + countdown row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Left: prices
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
                                    text = formatPrice(discount.originalPrice, showInArs, convertToArs),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                                Text(
                                    text = stringResource(R.string.price_free_now),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.FreeGreen
                                )
                            }
                        }
                        discount.offerType == OfferType.PERMANENT_PRICE_DROP -> {
                            Column {
                                if (discount.previousBasePrice != null) {
                                    Text(
                                        text = formatPrice(discount.previousBasePrice, showInArs, convertToArs),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                                Text(
                                    text = formatPrice(discount.currentPrice, showInArs, convertToArs),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.PriceDropPurple
                                )
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
                                    text = formatPrice(discount.originalPrice, showInArs, convertToArs),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                                Text(
                                    text = formatPrice(discount.currentPrice, showInArs, convertToArs),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Green
                                )
                            }
                        }
                    }

                    // Right: historical low + countdown stacked
                    Column(horizontalAlignment = Alignment.End) {
                        if (discount.isHistoricalLowest && !discount.isF2P) {
                            Text(
                                text = stringResource(R.string.historical_low_label),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.HistoricalGold
                            )
                        }
                        if (!discount.isF2P && !discount.isFree && discount.discountPercentage > 0) {
                            if (discount.isHistoricalLowest) Spacer(modifier = Modifier.height(4.dp))
                            val endTs = discount.endTimestamp
                            if (endTs != null) {
                                OfferCountdown(endTimestamp = endTs)
                            } else if (discount.endDate != null) {
                                Text(
                                    text = stringResource(R.string.price_until, discount.endDate),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AppColors.UrgentOrange
                                )
                            }
                        }
                    }
                }

                // "Ver en tienda" buy button — shown when a store URL is available
                if (discount.storeUrl.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { openStoreUrl(discount.storeUrl) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = when {
                                discount.isF2P -> stringResource(R.string.btn_play_free)
                                discount.isTemporarilyFree || discount.isFree -> stringResource(R.string.btn_get_free)
                                else -> stringResource(R.string.btn_buy_now)
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Format a price in ARS or USD depending on the toggle.
 */
private fun formatPrice(price: Float, showInArs: Boolean, convertToArs: (Float) -> Float): String {
    return if (showInArs) {
        val arsPrice = convertToArs(price)
        ArgentineTaxCalculator.formatArs(arsPrice)
    } else {
        "USD $${String.format(Locale.US, "%.2f", price)}"
    }
}

/**
 * Countdown timer that shows:
 * - Full date (e.g., "Hasta el 30 de mayo") if > 48 hours remain
 * - Hours and minutes (e.g., "23h 45m restantes") if < 48 hours remain
 * - "Oferta finalizada" if expired
 */
@Composable
fun OfferCountdown(endTimestamp: Long) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Tick every minute (or every second if < 1 hour)
    LaunchedEffect(endTimestamp) {
        while (true) {
            now = System.currentTimeMillis()
            val remaining = endTimestamp - now
            val tickInterval = if (remaining < 60 * 60 * 1000L) 1_000L else 60_000L
            delay(tickInterval)
        }
    }

    val remaining = endTimestamp - now
    val fortyEightHoursMs = 48L * 60 * 60 * 1000

    val text = when {
        remaining <= 0 -> stringResource(R.string.offer_ended)
        remaining < fortyEightHoursMs -> {
            val hours = (remaining / (60 * 60 * 1000)).toInt()
            val minutes = ((remaining % (60 * 60 * 1000)) / (60 * 1000)).toInt()
            stringResource(R.string.offer_countdown_hours, hours, minutes)
        }
        else -> {
            val dateFormat = SimpleDateFormat("d 'de' MMMM", Locale.getDefault())
            val dateStr = dateFormat.format(Date(endTimestamp))
            stringResource(R.string.offer_countdown_date, dateStr)
        }
    }

    val color = when {
        remaining <= 0 -> Color.Gray
        remaining < fortyEightHoursMs -> AppColors.UrgentOrange
        else -> AppColors.UrgentOrange
    }

    val bgColor = when {
        remaining <= 0 -> Color.Gray.copy(alpha = 0.1f)
        remaining < fortyEightHoursMs -> AppColors.UrgentOrange.copy(alpha = 0.12f)
        else -> AppColors.UrgentOrange.copy(alpha = 0.08f)
    }

    Text(
        text = "⏱ $text",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = if (remaining in 1 until fortyEightHoursMs) FontWeight.Bold else FontWeight.Normal,
        color = color,
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}
