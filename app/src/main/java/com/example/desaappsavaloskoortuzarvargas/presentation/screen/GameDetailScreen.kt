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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.desaappsavaloskoortuzarvargas.data.api.StoreRegionAvailability
import com.example.desaappsavaloskoortuzarvargas.data.api.ArgentineTaxCalculator
import com.example.desaappsavaloskoortuzarvargas.R
import com.example.desaappsavaloskoortuzarvargas.domain.model.Game
import com.example.desaappsavaloskoortuzarvargas.domain.model.countryCodeToFlag
import com.example.desaappsavaloskoortuzarvargas.presentation.component.DetailSection
import com.example.desaappsavaloskoortuzarvargas.presentation.component.FavoriteButton
import com.example.desaappsavaloskoortuzarvargas.presentation.component.SectionHeader
import com.example.desaappsavaloskoortuzarvargas.presentation.component.TagChips
import com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel.GamesViewModel
import com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel.SettingsViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailScreen(
    game: Game,
    viewModel: GamesViewModel,
    settingsViewModel: SettingsViewModel,
    onBackClick: () -> Unit,
    onFavoriteClick: (Game) -> Unit
) {
    val realPrices by viewModel.realPrices.collectAsState()
    val isLoadingPrices by viewModel.isLoadingPrices.collectAsState()
    val userSettings by settingsViewModel.userSettings.collectAsState()
    val pricesFromCache by viewModel.pricesFromCache.collectAsState()
    val showArs by viewModel.showArs.collectAsState()
    val dolarRate by viewModel.dolarTarjetaRate.collectAsState()

    // Filter prices by stores available in user's region
    val regionFilteredPrices = realPrices.filter { price ->
        StoreRegionAvailability.isAvailableInRegion(price.storeName, userSettings.countryCode)
    }

    // Load real prices when entering the screen
    LaunchedEffect(game.id) {
        viewModel.loadRealPrices(game.name, game.steamAppId)
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
                    TagChips(tags = game.tags)
                }
            }

            // Available platforms
            if (game.availablePlatforms.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(R.string.game_available_on), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                DetailSection(
                    title = stringResource(R.string.game_release_date),
                    value = game.releaseDate
                )
            }

            item {
                DetailSection(
                    title = stringResource(R.string.game_rating_label),
                    value = stringResource(R.string.game_rating_format, game.rating)
                )
            }

            item {
                if (game.historicalDiscount > 0) {
                    DetailSection(
                        title = stringResource(R.string.game_historical_discount),
                        value = "${game.historicalDiscount}%",
                        valueColor = Color.Green
                    )
                }
            }

            item {
                DetailSection(
                    title = stringResource(R.string.game_description),
                    value = game.description
                )
            }

            // Real prices from CheapShark API
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionHeader(stringResource(R.string.game_prices))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isLoadingPrices) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            }
                            // Currency toggle button
                            TextButton(onClick = { viewModel.toggleCurrency() }) {
                                Text(
                                    text = if (showArs) "ARS \uD83C\uDDE6\uD83C\uDDF7" else "USD \uD83C\uDDFA\uD83C\uDDF8",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Dolar tarjeta rate info (when showing ARS)
                    if (showArs && dolarRate != null) {
                        Text(
                            text = stringResource(R.string.dolar_tarjeta_rate, String.format("%.2f", dolarRate)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Region indicator
                    Text(
                        text = stringResource(
                            R.string.game_prices_region,
                            countryCodeToFlag(userSettings.countryCode),
                            userSettings.country
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Cached prices warning
                    if (pricesFromCache) {
                        Text(
                            text = stringResource(R.string.prices_from_cache),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFF9800),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (regionFilteredPrices.isNotEmpty()) {
                        regionFilteredPrices.sortedBy { it.currentPrice }.forEach { price ->
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
                                                text = stringResource(
                                                    R.string.game_off_percent,
                                                    price.savings.roundToInt()
                                                ),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.Green
                                            )
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        if (price.currentPrice > 0) {
                                            if (showArs) {
                                                val arsPrice = viewModel.convertToArs(price.currentPrice)
                                                Text(
                                                    text = ArgentineTaxCalculator.formatArs(arsPrice),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = stringResource(R.string.game_price_usd, price.currentPrice),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.Gray
                                                )
                                            } else {
                                                Text(
                                                    text = stringResource(R.string.game_price_usd, price.currentPrice),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = stringResource(R.string.game_free),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Green
                                            )
                                        }
                                        if (price.retailPrice > price.currentPrice) {
                                            Text(
                                                text = if (showArs) {
                                                    ArgentineTaxCalculator.formatArs(viewModel.convertToArs(price.retailPrice))
                                                } else {
                                                    stringResource(R.string.game_price_usd, price.retailPrice)
                                                },
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
                            stringResource(R.string.game_no_live_data),
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
                                    text = if (entry.value > 0) {
                                        stringResource(R.string.game_price_usd_simple, entry.value)
                                    } else {
                                        stringResource(R.string.game_not_available)
                                    },
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
                                            stringResource(R.string.game_price_usd_simple, price),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                if (dlc.historicalDiscount > 0) {
                                    Text(
                                        text = stringResource(
                                            R.string.game_dlc_hist_discount,
                                            dlc.historicalDiscount
                                        ),
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
