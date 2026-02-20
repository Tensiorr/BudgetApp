package com.tensiorr.budgetapp.ui

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
import java.time.LocalDate

/**
 * Screen for selecting date format preference.
 */
@Composable
fun DateFormatSelectionScreen(
    preferences: UserPreferences,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val currentDateFormat by preferences.dateFormatFlow
        .collectAsState(initial = "DD.MM.YYYY")

    val exampleDate = LocalDate.of(2026, 3, 15)

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
                text = "Format daty",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        Text(
            text = "Wybierz sposób wyświetlania dat w aplikacji",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(DateFormatOption.values().size) { index ->
                val option = DateFormatOption.values()[index]
                val isSelected = currentDateFormat == option.displayName

                DateFormatOptionItem(
                    option = option,
                    exampleDate = exampleDate,
                    isSelected = isSelected,
                    onClick = {
                        scope.launch {
                            preferences.setDateFormat(option.displayName)
                        }
                    }
                )
            }
        }
    }
}

/**
 * Individual date format option card.
 */
@Composable
fun DateFormatOptionItem(
    option: DateFormatOption,
    exampleDate: LocalDate,
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
                    text = option.displayName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Przykład: ${option.format(exampleDate)}",
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