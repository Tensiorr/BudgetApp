package com.tensiorr.budgetapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.tensiorr.budgetapp.BuildConfig

/**
 * Main settings screen with configuration options.
 *
 * Available settings:
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
    dateFormatDisplayText: String
) {
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
                    icon = Icons.Default.DarkMode,
                    title = "Motyw",
                    subtitle = themeDisplayText,
                    onClick = onNavigateToTheme
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
                SettingsMenuItem(
                    icon = Icons.Default.SystemUpdate,
                    title = "Sprawdź aktualizacje",
                    subtitle = "Wersja ${BuildConfig.VERSION_NAME}",
                    onClick = onCheckForUpdates
                )
            }
        }
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