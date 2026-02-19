package com.tensiorr.budgetapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tensiorr.budgetapp.BuildConfig
import com.tensiorr.budgetapp.data.preferences.UserPreferences

/**
 * Main settings screen with menu of configuration options.
 *
 * Available options:
 * - Manage categories and tags
 * - Theme selection
 */
@Composable
fun SettingsScreen(
    onNavigateToCategories: () -> Unit,
    onNavigateToTheme: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    themeDisplayText: String
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
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            item {
                SettingsMenuItem(
                    icon = Icons.Default.Info,
                    title = "O aplikacji",
                    subtitle = "Wersja ${BuildConfig.VERSION_NAME}",
                    onClick = { },
                    enabled = false
                )
            }
        }
    }
}

/**
 * Clickable settings menu item.
 */
@Composable
fun SettingsMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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