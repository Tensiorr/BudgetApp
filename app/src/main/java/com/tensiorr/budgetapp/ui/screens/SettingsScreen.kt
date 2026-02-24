package com.tensiorr.budgetapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tensiorr.budgetapp.BuildConfig
import com.tensiorr.budgetapp.data.preferences.UserPreferences
import com.tensiorr.budgetapp.ui.viewmodel.AuthViewModel
import com.tensiorr.budgetapp.util.AuthState
import com.tensiorr.budgetapp.util.SyncManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Main settings screen with configuration options.
 *
 * Available settings:
 * - Account info and logout
 * - Manage categories and tags
 * - Theme selection (light/dark/system)
 * - Statistics view
 * - Date format selection
 * - Check for app updates
 */
@Composable
fun SettingsScreen(
    onNavigateToCategories: () -> Unit,
    onNavigateToTheme: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToDateFormat: () -> Unit,
    onCheckForUpdates: () -> Unit,
    themeDisplayText: String,
    dateFormatDisplayText: String,
    authViewModel: AuthViewModel,
    isGuestMode: Boolean = false,
    preferences: UserPreferences
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }
    val lastSyncTimestamp by preferences.lastSyncTimestampFlow.collectAsStateWithLifecycle(initialValue = null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Ustawienia",
            style = MaterialTheme.typography.headlineMedium
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isGuestMode) {
                item {
                    Text(
                        text = "Konto",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 4.dp)
                    )
                }

                item {
                    val scope = rememberCoroutineScope()

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Korzystasz jako gość",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = "Zaloguj się aby synchronizować dane w chmurze",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            HorizontalDivider()

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch {
                                            preferences.markGuestToLogin()
                                            preferences.setGuestMode(false)
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Zaloguj się",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }

            } else if (authState is AuthState.Authenticated) {
                val authenticatedState = authState as AuthState.Authenticated

                item {
                    Text(
                        text = "Konto",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 4.dp)
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(
                                        text = "Email",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = authenticatedState.email ?: "Brak emaila",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (lastSyncTimestamp != null) {
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Sync,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Column {
                                        Text(
                                            text = "Ostatnia synchronizacja",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = formatSyncTime(lastSyncTimestamp!!),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            val scope = rememberCoroutineScope()
                            val context = LocalContext.current

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch {
                                            SyncManager.triggerImmediateSync(context)
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CloudSync,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Synchronizuj teraz",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            HorizontalDivider()

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showLogoutDialog = true }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.ExitToApp,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Wyloguj się",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            item {
                Text(
                    text = "Dane",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                )
            }

            item {
                SettingsMenuItem(
                    icon = Icons.Default.Category,
                    title = "Zarządzaj kategoriami",
                    subtitle = "Edytuj i usuń kategorie oraz tagi",
                    onClick = onNavigateToCategories
                )
            }

            item {
                SettingsMenuItem(
                    icon = Icons.Default.ShowChart,
                    title = "Statystyki",
                    subtitle = "Podsumowanie danych",
                    onClick = onNavigateToStatistics
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            item {
                Text(
                    text = "Wygląd",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                )
            }

            item {
                SettingsMenuItem(
                    icon = Icons.Default.DarkMode,
                    title = "Motyw",
                    subtitle = themeDisplayText,
                    onClick = onNavigateToTheme
                )
            }

            item {
                SettingsMenuItem(
                    icon = Icons.Default.CalendarToday,
                    title = "Format daty",
                    subtitle = dateFormatDisplayText,
                    onClick = onNavigateToDateFormat
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            item {
                Text(
                    text = "Aplikacja",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                )
            }

            item {
                SettingsMenuItem(
                    icon = Icons.Default.SystemUpdate,
                    title = "Sprawdź aktualizacje",
                    subtitle = "Wersja ${BuildConfig.VERSION_NAME}",
                    onClick = onCheckForUpdates
                )
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Wyloguj się") },
            text = { Text("Czy na pewno chcesz się wylogować?\n\nWszystkie lokalne dane zostaną usunięte.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        authViewModel.signOut(clearLocalData = true)
                        showLogoutDialog = false
                    }
                ) {
                    Text("Wyloguj", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }
}

/**
 * Clickable settings menu item with icon, title, and subtitle.
 *
 * @param icon Leading icon (optional)
 * @param title Main text
 * @param subtitle Secondary text
 * @param onClick Action when clicked
 * @param enabled Whether item is clickable
 */
@Composable
private fun SettingsMenuItem(
    icon: ImageVector? = null,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { onClick() }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (enabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (enabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (enabled) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Otwórz"
                )
            }
        }
    }
}

/**
 * Format sync timestamp to readable string.
 */
private fun formatSyncTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Przed chwilą"
        diff < 3600_000 -> "${diff / 60_000} min temu"
        diff < 86400_000 -> "${diff / 3600_000} godz. temu"
        else -> {
            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("pl", "PL"))
            dateFormat.format(Date(timestamp))
        }
    }
}