package com.example.desaappsavaloskoortuzarvargas.presentation.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.desaappsavaloskoortuzarvargas.data.api.ArgentineTaxCalculator
import com.example.desaappsavaloskoortuzarvargas.data.api.StorePrice
import com.example.desaappsavaloskoortuzarvargas.R
import com.example.desaappsavaloskoortuzarvargas.domain.model.Game
import com.example.desaappsavaloskoortuzarvargas.presentation.component.DetailSection
import com.example.desaappsavaloskoortuzarvargas.presentation.component.FavoriteButton
import com.example.desaappsavaloskoortuzarvargas.presentation.component.OfferCountdown
import com.example.desaappsavaloskoortuzarvargas.presentation.component.SectionHeader
import com.example.desaappsavaloskoortuzarvargas.presentation.component.TagChips
import com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel.GamesViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailScreen(
    game: Game,
    viewModel: GamesViewModel,
    onBackClick: () -> Unit,
    onFavoriteClick: (Game) -> Unit
) {
    val storePrices by viewModel.storePrices.collectAsState()
    val isLoadingPrices by viewModel.isLoadingPrices.collectAsState()
    val pricesFromCache by viewModel.pricesFromCache.collectAsState()
    val dolarRate by viewModel.dolarTarjetaRate.collectAsState()
    val gameDetailImageUrl by viewModel.gameDetailImageUrl.collectAsState()

    // Currency toggle: true = show in ARS, false = show in USD
    var showInArs by remember { mutableStateOf(true) }

    // Use dynamic image from store API if available, otherwise game's static image
    val displayImageUrl = gameDetailImageUrl ?: game.imageUrl

    // Load real prices when entering the screen — only query stores the game is on
    LaunchedEffect(game.id) {
        viewModel.loadRealPrices(game.name, game.steamAppId, game.availablePlatforms)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text(game.name) },
            navigationIcon = {
                IconButton(onClick = {
                    viewModel.clearRealPrices()
                    onBackClick()
                }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.label_back)
                    )
                }
            },
            actions = {
                FavoriteButton(game = game, onClick = { onFavoriteClick(game) })
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Game image (FillWidth to avoid cropping) ──
            item {
                if (displayImageUrl.isNullOrEmpty()) {
                    // Placeholder for games without images
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = game.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    AsyncImage(
                        model = displayImageUrl,
                        contentDescription = game.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.FillWidth
                    )
                }
            }

            if (game.tags.isNotEmpty()) {
                item { TagChips(tags = game.tags) }
            }

            if (game.availablePlatforms.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(R.string.game_available_on_header), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
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
                if (game.releaseDate.isNotEmpty()) {
                    DetailSection(title = stringResource(R.string.game_release_date), value = game.releaseDate)
                }
            }
            item { DetailSection(title = stringResource(R.string.game_rating_label), value = stringResource(R.string.game_rating_format, game.rating)) }
            item {
                // Historical discount must be >= any current real discount
                val maxCurrentDiscount = storePrices.maxOfOrNull { it.discountPercent } ?: 0
                val effectiveHistorical = maxOf(game.historicalDiscount, maxCurrentDiscount)
                if (effectiveHistorical > 0) {
                    DetailSection(title = stringResource(R.string.game_historical_discount), value = "${effectiveHistorical}%", valueColor = Color.Green)
                }
            }
            item { DetailSection(title = stringResource(R.string.game_description), value = game.description) }

            // F2P games: show "Disponible en" instead of prices
            val isF2P = game.tags.contains("Free2Play")
            if (isF2P) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader(stringResource(R.string.game_free_to_play_label))
                        game.availablePlatforms.forEach { platform ->
                            Text(
                                text = stringResource(R.string.game_available_on, platform),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {

                // ── Currency toggle ──
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.currency_display_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = showInArs,
                                onClick = { showInArs = true },
                                label = { Text("ARS 🇦🇷") }
                            )
                            FilterChip(
                                selected = !showInArs,
                                onClick = { showInArs = false },
                                label = { Text("USD 🇺🇸") }
                            )
                        }
                    }
                    if (showInArs && dolarRate != null) {
                        Text(
                            text = stringResource(R.string.dolar_tarjeta_rate, String.format(Locale.US, "%.2f", dolarRate)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // ===== UNIFIED PRICES SECTION =====
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SectionHeader("💰 " + stringResource(R.string.game_prices_header))
                            if (isLoadingPrices) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            }
                        }

                        if (pricesFromCache) {
                            Text(
                                text = stringResource(R.string.prices_from_cache),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFF9800),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Live API prices first (Steam, Epic, GOG, etc.)
                        if (storePrices.isNotEmpty()) {
                            storePrices.forEach { price ->
                                StorePriceCard(
                                    price = price,
                                    dolarRate = dolarRate,
                                    showInArs = showInArs,
                                    viewModel = viewModel
                                )
                            }
                        }

                        // Catalog reference prices for platforms NOT covered by live API
                        val livePlatforms = storePrices.map { it.storeName }.toSet()
                        val catalogPrices = game.currentPrices.filter { it.key !in livePlatforms }
                        catalogPrices.forEach { (platform, usdPrice) ->
                            CatalogPriceRow(
                                platform = platform,
                                gameName = game.name,
                                steamAppId = game.steamAppId,
                                usdPrice = usdPrice,
                                dolarRate = dolarRate,
                                showInArs = showInArs,
                                viewModel = viewModel
                            )
                        }

                        // No prices at all
                        if (storePrices.isEmpty() && catalogPrices.isEmpty() && !isLoadingPrices) {
                            Text(
                                stringResource(R.string.game_no_live_data),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            } // end else (non-F2P)

            // DLCs section
            if (game.dlcs.isNotEmpty()) {
                item {
                    SectionHeader(stringResource(R.string.game_dlcs_expansions, game.dlcs.size))
                }
                game.dlcs.forEach { dlc ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = dlc.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                if (dlc.description.isNotEmpty()) {
                                    Text(text = dlc.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 2)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                val dlcAvailablePrices = if (game.availablePlatforms.isNotEmpty()) {
                                    dlc.currentPrices.filter { it.key in game.availablePlatforms }
                                } else dlc.currentPrices
                                dlcAvailablePrices.entries.take(3).forEach { (platform, price) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(platform, style = MaterialTheme.typography.labelSmall)
                                        if (showInArs) {
                                            val arsPrice = viewModel.convertToArs(price)
                                            Text(
                                                ArgentineTaxCalculator.formatArs(arsPrice),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        } else {
                                            Text(
                                                stringResource(R.string.game_price_usd_simple, price),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                                if (dlc.historicalDiscount > 0) {
                                    Text(
                                        text = stringResource(R.string.game_dlc_hist_discount, dlc.historicalDiscount),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Green, fontWeight = FontWeight.Bold
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

/**
 * Card showing a real store price for Argentina.
 * Respects the [showInArs] toggle to display in ARS or USD.
 * Tapping opens the store product page directly.
 */
@Composable
private fun StorePriceCard(
    price: StorePrice,
    dolarRate: Double?,
    showInArs: Boolean,
    viewModel: GamesViewModel
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (price.storeUrl.isNotEmpty()) {
                    Modifier.clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(price.storeUrl))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                } else Modifier
            ),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = price.storeName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (price.isDiscounted) {
                    Text(
                        text = "-${price.discountPercent}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Green,
                        fontWeight = FontWeight.Bold
                    )
                    // Countdown timer if end date is known
                    val endTs = price.discountEndTimestamp
                    if (endTs != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        OfferCountdown(endTimestamp = endTs)
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                if (price.isFree) {
                    Text(
                        text = stringResource(R.string.game_free),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Green
                    )
                } else if (price.isArs) {
                    if (showInArs) {
                        Text(
                            text = ArgentineTaxCalculator.formatArs(price.currentPrice),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (price.isDiscounted) {
                            Text(
                                text = ArgentineTaxCalculator.formatArs(price.originalPrice),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    } else {
                        val usdApprox = if (dolarRate != null && dolarRate > 0) {
                            price.currentPrice / dolarRate.toFloat()
                        } else price.currentPrice
                        Text(
                            text = "USD $${String.format(Locale.US, "%.2f", usdApprox)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "≈ ${ArgentineTaxCalculator.formatArs(price.currentPrice)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    if (showInArs && dolarRate != null) {
                        val arsApprox = viewModel.convertToArs(price.currentPrice)
                        Text(
                            text = ArgentineTaxCalculator.formatArs(arsApprox),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "USD $${String.format(Locale.US, "%.2f", price.currentPrice)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "USD $${String.format(Locale.US, "%.2f", price.currentPrice)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (dolarRate != null) {
                            val arsPrice = viewModel.convertToArs(price.currentPrice)
                            Text(
                                text = "≈ ${ArgentineTaxCalculator.formatArs(arsPrice)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (price.isDiscounted) {
                        Text(
                            text = "USD $${String.format(Locale.US, "%.2f", price.originalPrice)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

/**
 * Row showing a catalog reference price for a platform.
 * Builds game-specific store URLs using the game name and Steam App ID.
 */
@Composable
private fun CatalogPriceRow(
    platform: String,
    gameName: String,
    steamAppId: Int,
    usdPrice: Float,
    dolarRate: Double?,
    showInArs: Boolean,
    viewModel: GamesViewModel
) {
    val context = LocalContext.current
    val storeUrl = when (platform) {
        "Steam" -> if (steamAppId > 0) "https://store.steampowered.com/app/$steamAppId" else ""
        "GOG" -> {
            val slug = gameName.lowercase()
                .replace("'", "").replace("`", "").replace(".", "")
                .replace(":", " ").replace("-", " ").trim()
                .replace(Regex("\\s+"), "_")
                .replace(Regex("[^a-z0-9_]"), "")
                .replace(Regex("_+"), "_").trim('_')
            if (slug.isNotEmpty()) "https://www.gog.com/game/$slug" else "https://www.gog.com"
        }
        "Epic Games" -> {
            val slug = gameName.lowercase()
                .replace("'", "").replace(":", "").trim()
                .replace(Regex("\\s+"), "-")
                .replace(Regex("[^a-z0-9-]"), "")
            if (slug.isNotEmpty()) "https://store.epicgames.com/p/$slug" else ""
        }
        "Xbox / Microsoft" -> "https://www.xbox.com/games/store/search?q=${java.net.URLEncoder.encode(gameName, "UTF-8")}"
        "EA" -> "https://www.ea.com/search#q=${java.net.URLEncoder.encode(gameName, "UTF-8")}"
        "Ubisoft" -> "https://store.ubisoft.com/search?q=${java.net.URLEncoder.encode(gameName, "UTF-8")}"
        "Battle.net" -> "https://us.shop.battle.net/es-ar"
        else -> ""
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (storeUrl.isNotEmpty()) {
                    Modifier.clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(storeUrl))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                } else Modifier
            ),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = platform,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                if (showInArs && dolarRate != null) {
                    val arsPrice = viewModel.convertToArs(usdPrice)
                    Text(
                        text = ArgentineTaxCalculator.formatArs(arsPrice),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "USD $${String.format(Locale.US, "%.2f", usdPrice)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "USD $${String.format(Locale.US, "%.2f", usdPrice)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (dolarRate != null) {
                        val arsPrice = viewModel.convertToArs(usdPrice)
                        Text(
                            text = "≈ ${ArgentineTaxCalculator.formatArs(arsPrice)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
