package com.example.desaappsavaloskoortuzarvargas.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.desaappsavaloskoortuzarvargas.domain.model.SUPPORTED_COUNTRIES
import com.example.desaappsavaloskoortuzarvargas.domain.model.countryCodeToFlag
import com.example.desaappsavaloskoortuzarvargas.presentation.component.LabeledSwitchRow
import com.example.desaappsavaloskoortuzarvargas.presentation.component.SettingsCard
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
    onSignOut: () -> Unit = {},
    onLoginRequest: () -> Unit = {},
    isGuest: Boolean = false,
    modifier: Modifier = Modifier
) {
    val userSettings by settingsViewModel.userSettings.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var editingName by remember { mutableStateOf(false) }
    var editingEmail by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf(userSettings.userName) }
    var tempEmail by remember { mutableStateOf(userSettings.email) }
    var showCountryDropdown by remember { mutableStateOf(false) }
    var showLanguageDropdown by remember { mutableStateOf(false) }
    var showLanguageConfirm by remember { mutableStateOf(false) }
    var pendingLanguageCode by remember { mutableStateOf<String?>(null) }
    var showSignOutConfirm by remember { mutableStateOf(false) }

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
            SettingsCard {
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

        // Country selection
        item {
            Text(stringResource(R.string.label_region), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        item {
            SettingsCard {
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
                                text = {
                                    Text(
                                        text = if (country.isAvailable) {
                                            "${countryCodeToFlag(country.code)} ${country.name}"
                                        } else {
                                            "${countryCodeToFlag(country.code)} ${country.name} (${stringResource(R.string.label_coming_soon)})"
                                        },
                                        color = if (country.isAvailable) Color.Unspecified else Color.Gray
                                    )
                                },
                                onClick = {
                                    if (country.isAvailable) {
                                        settingsViewModel.updateCountry(country.name, country.code)
                                        showCountryDropdown = false
                                    }
                                },
                                enabled = country.isAvailable
                            )
                        }
                }
            }
        }

        // Language selection
        item {
            SettingsCard {
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

        // Notifications section
        item {
            Text(stringResource(R.string.label_notifications), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        item {
            SettingsCard {
                LabeledSwitchRow(
                    label = stringResource(R.string.label_enable_notifications),
                    checked = userSettings.globalNotificationsEnabled,
                    onCheckedChange = { settingsViewModel.setGlobalNotifications(it) }
                )
            }
        }

        // Appearance section
        item {
            Text(
                "Appearance",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            SettingsCard {
                LabeledSwitchRow(
                    label = stringResource(R.string.label_dark_mode),
                    checked = userSettings.darkMode,
                    onCheckedChange = { settingsViewModel.updateDarkMode(it) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }

        // Sign out button — only shown for authenticated users, NOT for guests
        if (!isGuest) {
            item {
                Button(
                    onClick = { showSignOutConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(stringResource(R.string.action_sign_out))
                }
            }
        } else {
            // Guest mode — show "Iniciar sesión" button
            item {
                Button(
                    onClick = { onLoginRequest() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Iniciar sesión")
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }


    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            title = { Text(stringResource(R.string.sign_out_confirm_title)) },
            text = { Text(stringResource(R.string.sign_out_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showSignOutConfirm = false
                    onSignOut()
                }) {
                    Text(
                        stringResource(R.string.action_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirm = false }) {
                    Text(stringResource(R.string.action_no_apply))
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
