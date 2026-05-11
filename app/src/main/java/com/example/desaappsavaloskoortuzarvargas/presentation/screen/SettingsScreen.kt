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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.desaappsavaloskoortuzarvargas.domain.model.Game
import com.example.desaappsavaloskoortuzarvargas.domain.model.GameNotificationPref
import com.example.desaappsavaloskoortuzarvargas.domain.model.SUPPORTED_COUNTRIES
import com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel.GamesViewModel
import com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    gamesViewModel: GamesViewModel,
    modifier: Modifier = Modifier
) {
    val userSettings by settingsViewModel.userSettings.collectAsState()
    val notifications by settingsViewModel.notifications.collectAsState()
    val unreadCount by settingsViewModel.unreadCount.collectAsState()
    val favorites by gamesViewModel.favorites.collectAsState()

    var editingName by remember { mutableStateOf(false) }
    var editingEmail by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf(userSettings.userName) }
    var tempEmail by remember { mutableStateOf(userSettings.email) }
    var showCountryDropdown by remember { mutableStateOf(false) }
    var showNotifPrefs by remember { mutableStateOf<Game?>(null) }

    // Load favorites on first composition
    remember { gamesViewModel.loadFavorites(); true }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Profile section
        item {
            Text("Profile", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Username
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Username", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            if (editingName) {
                                OutlinedTextField(
                                    value = tempName,
                                    onValueChange = { tempName = it },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(0.7f)
                                )
                            } else {
                                Text(userSettings.userName, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        TextButton(onClick = {
                            if (editingName) {
                                settingsViewModel.updateUserName(tempName)
                            } else {
                                tempName = userSettings.userName
                            }
                            editingName = !editingName
                        }) {
                            Text(if (editingName) "Save" else "Edit")
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Email
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Email", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            if (editingEmail) {
                                OutlinedTextField(
                                    value = tempEmail,
                                    onValueChange = { tempEmail = it },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(0.7f)
                                )
                            } else {
                                Text(
                                    text = if (userSettings.email.isEmpty()) "Not set" else userSettings.email,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (userSettings.email.isEmpty()) Color.Gray else Color.Unspecified
                                )
                            }
                        }
                        TextButton(onClick = {
                            if (editingEmail) {
                                settingsViewModel.updateEmail(tempEmail)
                            } else {
                                tempEmail = userSettings.email
                            }
                            editingEmail = !editingEmail
                        }) {
                            Text(if (editingEmail) "Save" else "Edit")
                        }
                    }
                }
            }
        }

        // Country selection
        item {
            Text("Region", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Country (affects prices)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    OutlinedButton(
                        onClick = { showCountryDropdown = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🌍 ${userSettings.country}")
                    }
                    DropdownMenu(
                        expanded = showCountryDropdown,
                        onDismissRequest = { showCountryDropdown = false }
                    ) {
                        SUPPORTED_COUNTRIES.forEach { country ->
                            DropdownMenuItem(
                                text = { Text(country) },
                                onClick = {
                                    settingsViewModel.updateCountry(country)
                                    showCountryDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Notifications section
        item {
            Text("Notifications", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Notifications", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = userSettings.globalNotificationsEnabled,
                            onCheckedChange = { settingsViewModel.setGlobalNotifications(it) }
                        )
                    }
                }
            }
        }

        // In-app notifications
        if (notifications.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Alerts ($unreadCount unread)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            items(notifications.take(10)) { notification ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { settingsViewModel.markAsRead(notification.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (notification.isRead)
                            MaterialTheme.colorScheme.surface
                        else
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    elevation = CardDefaults.cardElevation(1.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(notification.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(notification.message, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        }

        // Favorites management
        item {
            Text("Favorites (${favorites.size})", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        if (favorites.isEmpty()) {
            item {
                Text("No favorites yet. Add games from the catalog!", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }

        items(favorites) { game ->
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                        Icon(Icons.Filled.Notifications, "Notification prefs", tint = MaterialTheme.colorScheme.primary)
                    }

                    // Remove from favorites
                    IconButton(onClick = { gamesViewModel.toggleFavorite(game) }) {
                        Icon(Icons.Filled.Delete, "Remove", tint = Color.Red)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
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
            title = { Text("Notifications: ${game.name}") },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Offers")
                        Switch(checked = notifyOffers, onCheckedChange = { notifyOffers = it })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("News")
                        Switch(checked = notifyNews, onCheckedChange = { notifyNews = it })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Historical Low")
                        Switch(checked = notifyHistorical, onCheckedChange = { notifyHistorical = it })
                    }
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
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotifPrefs = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

