package com.tensiorr.budgetapp.ui.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * Dialog asking what to do with local guest data when logging in.
 */
@Composable
fun GuestDataDialog(
    onKeepAndMerge: () -> Unit,
    onDiscardAndDownload: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Masz lokalne dane") },
        text = {
            Text(
                "Korzystałeś z aplikacji jako gość i masz lokalne dane.\n\n" +
                        "Co chcesz zrobić z tymi danymi?"
            )
        },
        confirmButton = {
            TextButton(onClick = onKeepAndMerge) {
                Text("Zachowaj i połącz")
            }
        },
        dismissButton = {
            TextButton(onClick = onDiscardAndDownload) {
                Text("Usuń i pobierz z chmury")
            }
        }
    )
}