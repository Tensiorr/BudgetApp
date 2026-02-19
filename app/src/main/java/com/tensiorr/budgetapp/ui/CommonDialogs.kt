package com.tensiorr.budgetapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Dialog for selecting custom date range with start and end date pickers.
 * Uses Material3 DatePicker for better UX and dark mode support.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDateRangeDialog(
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, LocalDate) -> Unit
) {
    var startDate by remember { mutableStateOf(LocalDate.now().minusMonths(1)) }
    var endDate by remember { mutableStateOf(LocalDate.now()) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Własny zakres dat") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showStartDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Od: ${startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}")
                }

                OutlinedButton(
                    onClick = { showEndDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Do: ${endDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(startDate, endDate) },
                enabled = !startDate.isAfter(endDate)
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )

    if (showStartDatePicker) {
        Material3DatePickerDialog(
            initialDate = startDate,
            onDateSelected = { selectedDate ->
                startDate = selectedDate
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        Material3DatePickerDialog(
            initialDate = endDate,
            onDateSelected = { selectedDate ->
                endDate = selectedDate
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}

/**
 * Material3 DatePicker dialog with proper dark mode support.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Material3DatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(selectedDate)
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = "Wybierz datę",
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                )
            }
        )
    }
}
