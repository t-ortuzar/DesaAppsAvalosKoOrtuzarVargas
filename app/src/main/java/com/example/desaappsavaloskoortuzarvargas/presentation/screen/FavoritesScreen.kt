package com.example.desaappsavaloskoortuzarvargas.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.desaappsavaloskoortuzarvargas.presentation.component.LabeledSwitchRow
import com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel.GamesViewModel
import com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel.SettingsViewModel

@Composable
fun FavoritesScreen(
    viewModel: GamesViewModel,
    settingsViewModel: SettingsViewModel,
    onGameSelected: (Game) -> Unit,
    modifier: Modifier = Modifier
) {
    val favorites by viewModel.favorites.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val userSettings by settingsViewModel.userSettings.collectAsState()

    var showNotifPrefs by remember { mutableStateOf<Game?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.label_favorites_count, favorites.size),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (favorites.isEmpty() && !isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "⭐",
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.favorites_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(favorites) { game ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onGameSelected(game) },
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(game.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    text = game.tags.joinToString(", "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }

                            // Per-game notification settings
                            IconButton(onClick = { showNotifPrefs = game }) {
                                Icon(
                                    Icons.Filled.Notifications,
                                    stringResource(R.string.label_notification_prefs),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Remove from favorites
                            IconButton(onClick = { viewModel.toggleFavorite(game) }) {
                                Icon(Icons.Filled.Delete, stringResource(R.string.action_remove), tint = Color.Red)
                            }
                        }
                    }
                }
            }
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
                }) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotifPrefs = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}
