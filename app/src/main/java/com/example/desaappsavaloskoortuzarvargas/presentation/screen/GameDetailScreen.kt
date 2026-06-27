package com.example.desaappsavaloskoortuzarvargas.presentation.screen

import android.content.Intent
import android.net.Uri
import com.example.desaappsavaloskoortuzarvargas.data.catalog.GameCatalog
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
                    // Always show the catalog's declared platforms as informational chips.
                    // These chips mean "the game exists on this platform" — they are independent
                    // of whether the price API returned data. Price cards below are separate:
                    // they only appear when a store returns a live price.
                    val availableOnPlatforms = game.availablePlatforms
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(R.string.game_available_on_header), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            availableOnPlatforms.forEach { platform ->
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
                                    viewModel = viewModel,
                                    gameName = game.name,
                                    steamAppId = game.steamAppId,
                                    gameId = game.id
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
                                gameId = game.id,
                                usdPrice = usdPrice,
                                dolarRate = dolarRate,
                                showInArs = showInArs,
                                viewModel = viewModel
                            )
                        }

                        // No prices at all — only shown after loading completes.
                        // Platforms with no price are intentionally not shown (no price = link unverified).
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
                                // DLC prices come from Steam API together with the parent
                                // game's price. Until a real price is fetched, show which
                                // platforms the DLC is available on.
                                val dlcPlatforms = game.availablePlatforms.filter { it == "Steam" || it == "Epic Games" }
                                if (dlcPlatforms.isNotEmpty()) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        dlcPlatforms.forEach { platform ->
                                            Text(
                                                text = platform,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier
                                                    .background(
                                                        MaterialTheme.colorScheme.secondaryContainer,
                                                        RoundedCornerShape(4.dp)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
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
    viewModel: GamesViewModel,
    gameName: String = "",
    steamAppId: Int = 0,
    gameId: Int = 0
) {
    val context = LocalContext.current
    // Prefer the catalog-verified URL (epicSlugs, gogGameUrls, xboxProductIds, etc.)
    // over the raw API URL which may contain UUID slugs or malformed paths.
    val catalogUrl = if (gameId > 0 && gameName.isNotEmpty()) {
        buildStoreUrl(price.storeName, gameName, steamAppId, gameId)
    } else ""
    val effectiveUrl = catalogUrl.takeIf { it.isNotEmpty() } ?: price.storeUrl
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (effectiveUrl.isNotEmpty()) {
                    Modifier.clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(effectiveUrl))
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
                if (price.isGamePass) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "✓ Xbox Game Pass",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF107C10),   // Xbox green
                        fontWeight = FontWeight.Bold
                    )
                }
                if (price.isEaPlay) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "✓ EA Play",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFF6600),   // EA orange
                        fontWeight = FontWeight.Bold
                    )
                }
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
                } else if (price.isGamePass && price.currentPrice == 0f) {
                    // Game Pass entry but no retail price available → link to store
                    Text(
                        text = "Ver precio en tienda →",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (price.isEaPlay && price.currentPrice == 0f) {
                    // EA Play entry — included via subscription, no standalone purchase price
                    Text(
                        text = "Ver en EA App →",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFFF6600)
                    )
                } else if (price.isVerifiedLink) {
                    // Catalog-verified store link — price couldn't be fetched from live API.
                    val storeLinkLabel = when (price.storeName) {
                        "EA" -> "Ver precio en EA App →"
                        "Ubisoft" -> "Ver precio en Ubisoft →"
                        "Epic Games" -> "Ver precio en Epic Games →"
                        "Battle.net" -> "Ver precio en Battle.net →"
                        else -> "Ver precio en ${price.storeName} →"
                    }
                    Text(
                        text = storeLinkLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFFF6600)
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

/** Builds the store URL for a platform, used by StorePriceCard and CatalogPriceRow. */
private fun buildStoreUrl(
    platform: String,
    gameName: String,
    steamAppId: Int,
    gameId: Int
): String = when (platform) {
    "Steam" -> if (steamAppId > 0) "https://store.steampowered.com/app/$steamAppId" else ""
    "GOG" -> {
        GameCatalog.getGogUrl(gameId) ?: run {
            val romanMap = mapOf(
                "12" to "xii", "11" to "xi", "10" to "x",
                "9" to "ix", "8" to "viii", "7" to "vii", "6" to "vi",
                "5" to "v", "4" to "iv", "3" to "iii", "2" to "ii", "1" to "i"
            )
            var slug = gameName.lowercase()
                .replace("'", "").replace(":", "").replace(".", "")
                .replace("-", " ").trim()
            romanMap.forEach { (digit, roman) ->
                slug = slug.replace(Regex("\\b$digit\\b"), roman)
            }
            slug = slug.replace(Regex("\\s+"), "_")
                .replace(Regex("[^a-z0-9_]"), "")
            if (slug.isNotEmpty()) "https://www.gog.com/en/game/$slug" else ""
        }
    }
    "Epic Games" -> {
        // Prefer hardcoded verified slug; fall back to title-derived slug
        GameCatalog.getEpicUrl(gameId) ?: run {
            val slug = gameName.lowercase()
                .replace("'", "").replace(":", "").trim()
                .replace(Regex("\\s+"), "-")
                .replace(Regex("[^a-z0-9-]"), "")
            if (slug.isNotEmpty()) "https://store.epicgames.com/en-US/p/$slug" else ""
        }
    }
    "Xbox / Microsoft" -> {
        val productId = GameCatalog.getXboxProductId(gameId)
        if (productId != null) {
            // Use the exact known product URL — no /0010 suffix to avoid 404 on games
            // where that specific SKU doesn't exist (console-only SKUs, etc.)
            val titleHint = GameCatalog.getXboxTitleHint(gameId)
            val slug = (titleHint ?: gameName).lowercase()
                .replace("'", "").replace(":", "").replace(".", "")
                .replace(Regex("[^a-z0-9]+"), "-")
                .trim('-')
            "https://www.xbox.com/es-AR/games/store/$slug/$productId"
        } else {
            "https://www.xbox.com/es-AR/search?q=${java.net.URLEncoder.encode(gameName, "UTF-8")}"
        }
    }
    "EA" -> GameCatalog.getEaUrl(gameId)
        ?: "https://www.ea.com/search#q=${java.net.URLEncoder.encode(gameName, "UTF-8")}"
    "Ubisoft" -> GameCatalog.getUbisoftUrl(gameId)
        ?: "https://store.ubisoft.com/ofertas/games?lang=es_AR"
    "Battle.net" -> {
        val slug = GameCatalog.getBattleNetSlug(gameId)
        if (slug != null) "https://us.shop.battle.net/es-ar/product/$slug"
        else {
            val encoded = java.net.URLEncoder.encode(gameName, "UTF-8")
            "https://us.shop.battle.net/es-ar?q=$encoded"
        }
    }
    else -> ""
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
    gameId: Int = 0,
    usdPrice: Float,
    dolarRate: Double?,
    showInArs: Boolean,
    viewModel: GamesViewModel
) {
    val context = LocalContext.current
    val storeUrl = buildStoreUrl(platform, gameName, steamAppId, gameId)
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
