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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.desaappsavaloskoortuzarvargas.R
import com.example.desaappsavaloskoortuzarvargas.domain.model.Game
import com.example.desaappsavaloskoortuzarvargas.domain.model.GameNotificationPref
import com.example.desaappsavaloskoortuzarvargas.domain.model.NotificationType
import com.example.desaappsavaloskoortuzarvargas.domain.model.SUPPORTED_COUNTRIES
import com.example.desaappsavaloskoortuzarvargas.domain.model.countryCodeToFlag
import com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel.GamesViewModel
import com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel.SettingsViewModel
import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.launch

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

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var editingName by remember { mutableStateOf(false) }
    var editingEmail by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf(userSettings.userName) }
    var tempEmail by remember { mutableStateOf(userSettings.email) }
    var showCountryDropdown by remember { mutableStateOf(false) }
    var showLanguageDropdown by remember { mutableStateOf(false) }
    var showNotifPrefs by remember { mutableStateOf<Game?>(null) }
    var showLanguageConfirm by remember { mutableStateOf(false) }
    var pendingLanguageCode by remember { mutableStateOf<String?>(null) }

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
            Text(stringResource(R.string.label_profile), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
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
                            Text(stringResource(R.string.label_username), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
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
                            Text(if (editingName) stringResource(R.string.action_save) else stringResource(R.string.action_edit))
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
                            Text(stringResource(R.string.label_email), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            if (editingEmail) {
                                OutlinedTextField(
                                    value = tempEmail,
                                    onValueChange = { tempEmail = it },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(0.7f)
                                )
                            } else {
                                Text(
                                    text = if (userSettings.email.isEmpty()) stringResource(R.string.label_not_set) else userSettings.email,
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
                            Text(if (editingEmail) stringResource(R.string.action_save) else stringResource(R.string.action_edit))
                        }
                    }
                }
            }
        }

        // Country selection
        item {
            Text(stringResource(R.string.label_region), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.label_country_affects_prices), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    OutlinedButton(
                        onClick = { showCountryDropdown = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("${countryCodeToFlag(userSettings.countryCode)} ${userSettings.country}")
                    }
                    DropdownMenu(
                        expanded = showCountryDropdown,
                        onDismissRequest = { showCountryDropdown = false }
                    ) {
                        SUPPORTED_COUNTRIES.forEach { country ->
                            DropdownMenuItem(
                                text = { Text("${countryCodeToFlag(country.code)} ${country.name}") },
                                onClick = {
                                    settingsViewModel.updateCountry(country.name, country.code)
                                    showCountryDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Language selection
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.label_language), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    val languageOptions = listOf(
                        "en" to stringResource(R.string.language_english),
                        "es" to stringResource(R.string.language_spanish)
                    )
                    val currentLanguageLabel = languageOptions.firstOrNull { it.first == userSettings.languageCode }?.second
                        ?: languageOptions.first().second
                    OutlinedButton(
                        onClick = { showLanguageDropdown = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(currentLanguageLabel)
                    }
                    DropdownMenu(
                        expanded = showLanguageDropdown,
                        onDismissRequest = { showLanguageDropdown = false }
                    ) {
                        languageOptions.forEach { (code, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    pendingLanguageCode = code
                                    showLanguageConfirm = true
                                    showLanguageDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Notifications section
        item {
            Text(stringResource(R.string.label_notifications), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
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
                        Text(stringResource(R.string.label_enable_notifications), style = MaterialTheme.typography.bodyMedium)
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
                        stringResource(R.string.label_alerts_unread, unreadCount),
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
                        val title = when (notification.type) {
                            NotificationType.HISTORICAL_LOW -> stringResource(R.string.notif_title_historical_low)
                            NotificationType.DISCOUNT -> stringResource(R.string.notif_title_discount)
                            NotificationType.NEWS -> stringResource(R.string.nav_news)
                            NotificationType.FREE_GAME -> stringResource(R.string.offers_tab_free)
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
                            NotificationType.NEWS -> notification.gameName
                            NotificationType.FREE_GAME -> notification.gameName
                        }
                        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(message, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        }

        // Favorites management
        item {
            Text(stringResource(R.string.label_favorites_count, favorites.size), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        if (favorites.isEmpty()) {
            item {
                Text(stringResource(R.string.label_no_favorites), style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
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
                        Icon(
                            Icons.Filled.Notifications,
                            stringResource(R.string.label_notification_prefs),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Remove from favorites
                    IconButton(onClick = { gamesViewModel.toggleFavorite(game) }) {
                        Icon(Icons.Filled.Delete, stringResource(R.string.action_remove), tint = Color.Red)
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
            title = { Text(stringResource(R.string.label_notifications_for_game, game.name)) },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.label_offers))
                        Switch(checked = notifyOffers, onCheckedChange = { notifyOffers = it })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.label_news))
                        Switch(checked = notifyNews, onCheckedChange = { notifyNews = it })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.label_historical_low))
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

    if (showLanguageConfirm && pendingLanguageCode != null) {
        AlertDialog(
            onDismissRequest = {
                showLanguageConfirm = false
                pendingLanguageCode = null
            },
            title = { Text(stringResource(R.string.label_language)) },
            text = { Text(stringResource(R.string.language_restart_message)) },
            confirmButton = {
                TextButton(onClick = {
                    val languageCode = pendingLanguageCode ?: return@TextButton
                    showLanguageConfirm = false
                    pendingLanguageCode = null
                    // Persist language first, then apply locale and recreate
                    coroutineScope.launch {
                        settingsViewModel.updateLanguage(languageCode)
                        AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(languageCode)
                        )
                        (context as? Activity)?.recreate()
                    }
                }) {
                    Text(stringResource(R.string.action_apply_changes))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showLanguageConfirm = false
                    pendingLanguageCode = null
                }) {
                    Text(stringResource(R.string.action_no_apply))
                }
            }
        )
    }
}
