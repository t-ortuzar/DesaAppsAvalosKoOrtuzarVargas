package com.example.desaappsavaloskoortuzarvargas.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.desaappsavaloskoortuzarvargas.R
import com.example.desaappsavaloskoortuzarvargas.domain.model.Game
import com.example.desaappsavaloskoortuzarvargas.domain.model.GameNotificationPref
import com.example.desaappsavaloskoortuzarvargas.domain.model.InAppNotification
import com.example.desaappsavaloskoortuzarvargas.domain.model.NotificationType
import com.example.desaappsavaloskoortuzarvargas.presentation.component.LabeledSwitchRow
import com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel.GamesViewModel
import com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel.SettingsViewModel

/**
 * Combined Notifications + Favorites screen.
 *
 * Tab 0 — Alerts: price drop & historical-low notifications
 * Tab 1 — Favorites: the user's saved games with per-game notification settings
 */
@Composable
fun NotificationsScreen(
    gamesViewModel: GamesViewModel,
    settingsViewModel: SettingsViewModel,
    onGameSelected: (Game) -> Unit,
    modifier: Modifier = Modifier
) {
    val favorites   by gamesViewModel.favorites.collectAsState()
    val isLoading   by gamesViewModel.isLoading.collectAsState()
    val userSettings by settingsViewModel.userSettings.collectAsState()
    val notifications by settingsViewModel.notifications.collectAsState()
    val unreadCount  by settingsViewModel.unreadCount.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showNotifPrefs by remember { mutableStateOf<Game?>(null) }

    val tabTitles = listOf(
        stringResource(R.string.tab_alerts),
        stringResource(R.string.tab_favorites)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Header ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.nav_notifications),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$unreadCount",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ── Tab row ──
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(title)
                            // Show unread badge on Alerts tab
                            if (index == 0 && unreadCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Color(0xFFE53935), CircleShape)
                                )
                            }
                        }
                    }
                )
            }
        }

        // ── Content ──
        when (selectedTab) {
            0 -> AlertsTab(
                notifications = notifications,
                onMarkRead = { settingsViewModel.markAsRead(it) },
                onMarkAllRead = { settingsViewModel.markAllAsRead() }
            )
            1 -> FavoritesTab(
                favorites = favorites,
                isLoading = isLoading,
                onGameSelected = onGameSelected,
                onToggleFavorite = { gamesViewModel.toggleFavorite(it) },
                onShowNotifPrefs = { showNotifPrefs = it }
            )
        }
    }

    // Per-game notification preference dialog
    showNotifPrefs?.let { game ->
        val currentPref = userSettings.gameNotificationPrefs[game.id]
            ?: GameNotificationPref(game.id, game.name)

        var notifyOffers by remember(game.id) { mutableStateOf(currentPref.notifyOffers) }
        var notifyNews by remember(game.id) { mutableStateOf(currentPref.notifyNews) }
        var notifyHistorical by remember(game.id) { mutableStateOf(currentPref.notifyHistoricalLow) }

        AlertDialog(
            onDismissRequest = { showNotifPrefs = null },
            title = { Text(stringResource(R.string.label_notifications_for_game, game.name)) },
            text = {
                Column {
                    LabeledSwitchRow(
                        label = stringResource(R.string.label_offers),
                        checked = notifyOffers,
                        onCheckedChange = { notifyOffers = it }
                    )
                    LabeledSwitchRow(
                        label = stringResource(R.string.label_news),
                        checked = notifyNews,
                        onCheckedChange = { notifyNews = it }
                    )
                    LabeledSwitchRow(
                        label = stringResource(R.string.label_historical_low),
                        checked = notifyHistorical,
                        onCheckedChange = { notifyHistorical = it }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    settingsViewModel.updateGameNotificationPref(
                        GameNotificationPref(
                            gameId = game.id,
                            gameName = game.name,
                            notifyOffers = notifyOffers,
                            notifyNews = notifyNews,
                            notifyHistoricalLow = notifyHistorical
                        )
                    )
                    showNotifPrefs = null
                }) { Text(stringResource(R.string.action_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showNotifPrefs = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────
//  Alerts tab
// ─────────────────────────────────────────────────────────────

@Composable
private fun AlertsTab(
    notifications: List<InAppNotification>,
    onMarkRead: (Int) -> Unit,
    onMarkAllRead: () -> Unit
) {
    if (notifications.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Filled.NotificationsNone,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.notifications_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onMarkAllRead) {
                    Text(
                        stringResource(R.string.notifications_mark_all_read),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        items(notifications) { notification ->
            NotificationCard(notification = notification, onMarkRead = onMarkRead)
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun NotificationCard(
    notification: InAppNotification,
    onMarkRead: (Int) -> Unit
) {
    val isUnread = !notification.isRead
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (isUnread) onMarkRead(notification.id) },
        elevation = CardDefaults.cardElevation(if (isUnread) 3.dp else 1.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnread)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Unread indicator dot
            if (isUnread) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
                Spacer(Modifier.width(8.dp))
            } else {
                Spacer(Modifier.width(16.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                val title = when (notification.type) {
                    NotificationType.HISTORICAL_LOW -> "🏆 " + stringResource(R.string.notif_title_historical_low)
                    NotificationType.DISCOUNT       -> "💰 " + stringResource(R.string.notif_title_discount)
                    NotificationType.NEWS           -> "📰 " + stringResource(R.string.nav_news)
                    NotificationType.FREE_GAME      -> "🎮 " + stringResource(R.string.offers_tab_free)
                }
                val message = when (notification.type) {
                    NotificationType.HISTORICAL_LOW -> stringResource(
                        R.string.notif_message_historical_low,
                        notification.gameName,
                        notification.discountPercentage,
                        notification.platform
                    )
                    NotificationType.DISCOUNT -> stringResource(
                        R.string.notif_message_discount,
                        notification.gameName,
                        notification.discountPercentage,
                        notification.platform
                    )
                    NotificationType.NEWS     -> notification.gameName
                    NotificationType.FREE_GAME -> notification.gameName
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Favorites tab
// ─────────────────────────────────────────────────────────────

@Composable
private fun FavoritesTab(
    favorites: List<Game>,
    isLoading: Boolean,
    onGameSelected: (Game) -> Unit,
    onToggleFavorite: (Game) -> Unit,
    onShowNotifPrefs: (Game) -> Unit
) {
    if (favorites.isEmpty() && !isLoading) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "⭐", style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.favorites_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.label_favorites_count, favorites.size),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
            )
        }

        items(favorites) { game ->
            FavoriteGameCard(
                game = game,
                onGameSelected = onGameSelected,
                onRemove = onToggleFavorite,
                onNotifPrefs = onShowNotifPrefs
            )
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun FavoriteGameCard(
    game: Game,
    onGameSelected: (Game) -> Unit,
    onRemove: (Game) -> Unit,
    onNotifPrefs: (Game) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onGameSelected(game) },
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = Color(0xFFFDD835),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    game.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = game.tags.take(3).joinToString(", "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Notification prefs
            IconButton(onClick = { onNotifPrefs(game) }) {
                Icon(
                    Icons.Filled.Notifications,
                    contentDescription = stringResource(R.string.label_notification_prefs),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Remove from favorites
            IconButton(onClick = { onRemove(game) }) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.action_remove),
                    tint = Color(0xFFE53935)
                )
            }
        }
    }
}



