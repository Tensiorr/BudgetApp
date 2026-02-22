package com.tensiorr.budgetapp.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tensiorr.budgetapp.domain.model.AppUpdate
import com.tensiorr.budgetapp.ui.viewmodel.UpdateUiState

/**
 * Dialog overlay for app update flow.
 *
 * Displays different dialogs based on update state:
 * - UpdateAvailable: Shows changelog and download button
 * - Downloading: Shows progress bar
 * - ReadyToInstall: Prompts user to install
 * - NeedsInstallPermission: Directs to settings
 * - NoUpdateAvailable: Confirms app is up to date
 * - Error: Shows error message
 */
@Composable
fun UpdateDialog(
    uiState: UpdateUiState,
    onDownload: () -> Unit,
    onInstall: () -> Unit,
    onDismiss: () -> Unit,
    onRequestInstallPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is UpdateUiState.UpdateAvailable -> {
            UpdateAvailableDialog(
                update = uiState.update,
                onDownload = onDownload,
                onDismiss = onDismiss
            )
        }
        is UpdateUiState.Downloading -> {
            DownloadingDialog(progress = uiState.progress)
        }
        is UpdateUiState.ReadyToInstall -> {
            ReadyToInstallDialog(
                onInstall = onInstall,
                onDismiss = onDismiss
            )
        }
        is UpdateUiState.NeedsInstallPermission -> {
            PermissionNeededDialog(
                onOpenSettings = onRequestInstallPermission,
                onDismiss = onDismiss
            )
        }
        is UpdateUiState.NoUpdateAvailable -> {
            NoUpdateDialog(onDismiss = onDismiss)
        }
        is UpdateUiState.Error -> {
            ErrorDialog(
                message = uiState.message,
                onDismiss = onDismiss
            )
        }
        else -> { /* No dialog */ }
    }
}

@Composable
private fun UpdateAvailableDialog(
    update: AppUpdate,
    onDownload: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.GetApp,
                contentDescription = null
            )
        },
        title = {
            Text("Dostępna aktualizacja")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Wersja ${update.version}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Rozmiar: ${update.getFormattedSize()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Co nowego:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = update.changelog.ifBlank { "Brak informacji o zmianach" },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(onClick = onDownload) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pobierz")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Później")
            }
        }
    )
}

@Composable
private fun DownloadingDialog(progress: Int) {
    AlertDialog(
        onDismissRequest = { /* Non-dismissible during download */ },
        title = {
            Text("Pobieranie aktualizacji")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "$progress%",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = { }
    )
}

@Composable
private fun ReadyToInstallDialog(
    onInstall: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.GetApp,
                contentDescription = null
            )
        },
        title = {
            Text("Gotowe do instalacji")
        },
        text = {
            Text("Aktualizacja została pobrana. Kliknij \"Instaluj\" aby kontynuować.")
        },
        confirmButton = {
            Button(onClick = onInstall) {
                Text("Instaluj")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}

@Composable
private fun PermissionNeededDialog(
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Wymagane uprawnienie")
        },
        text = {
            Text("Aby zainstalować aktualizację, musisz włączyć uprawnienie do instalacji aplikacji z tego źródła.")
        },
        confirmButton = {
            Button(onClick = onOpenSettings) {
                Text("Otwórz ustawienia")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}

@Composable
private fun NoUpdateDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text("Jesteś na bieżąco")
        },
        text = {
            Text("Masz najnowszą wersję aplikacji.")
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Composable
private fun ErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Błąd")
        },
        text = {
            Text(message)
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}