package com.tensiorr.budgetapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tensiorr.budgetapp.data.preferences.UserPreferences
import kotlinx.coroutines.launch

/**
 * Screen for selecting app theme mode.
 *
 * Options:
 * - Light mode
 * - Dark mode
 * - System default (follows device settings)
 */
@Composable
fun ThemeSelectionScreen(
    preferences: UserPreferences,
    currentThemeMode: String,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Wstecz")
            }
            Text(
                text = "Motyw",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                ThemeOptionItem(
                    title = "Jasny",
                    subtitle = "Zawsze jasny motyw",
                    isSelected = currentThemeMode == "LIGHT",
                    onClick = {
                        scope.launch {
                            preferences.setThemeMode("LIGHT")
                        }
                    }
                )
            }

            item {
                ThemeOptionItem(
                    title = "Ciemny",
                    subtitle = "Zawsze ciemny motyw",
                    isSelected = currentThemeMode == "DARK",
                    onClick = {
                        scope.launch {
                            preferences.setThemeMode("DARK")
                        }
                    }
                )
            }

            item {
                ThemeOptionItem(
                    title = "Systemowy",
                    subtitle = "Dostosuj do ustawień urządzenia",
                    isSelected = currentThemeMode == "SYSTEM",
                    onClick = {
                        scope.launch {
                            preferences.setThemeMode("SYSTEM")
                        }
                    }
                )
            }
        }
    }
}

/**
 * Individual theme option card with checkmark when selected.
 */
@Composable
fun ThemeOptionItem(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Wybrano",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}