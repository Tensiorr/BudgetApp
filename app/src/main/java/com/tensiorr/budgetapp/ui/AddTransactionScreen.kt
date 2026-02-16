package com.tensiorr.budgetapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tensiorr.budgetapp.data.entity.Transaction
import com.tensiorr.budgetapp.data.entity.TransactionType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import kotlin.math.roundToInt
import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.graphics.Color


@Composable
fun AddTransactionScreen(onSave: (Transaction, List<String>) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var comment by remember { mutableStateOf("") }

    var selectedTags by remember { mutableStateOf(listOf<String>()) }
    var newTagName by remember { mutableStateOf("") }

    val context = LocalContext.current

    val datePickerDialog = remember {
        val calendar = Calendar.getInstance()
        calendar.time = java.util.Date.from(date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                date = LocalDate.of(year, month + 1, dayOfMonth)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Nowa transakcja",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = amount,
            onValueChange = { newValue ->
                val normalized = newValue.replace(',', '.')
                if (normalized.isEmpty() || normalized.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                    amount = newValue
                }
            },
            label = { Text("Kwota (PLN)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Wydatek")
            Switch(
                checked = type == TransactionType.INCOME,
                onCheckedChange = { isIncome ->
                    type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE
                }
            )
            Text("Przychód")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newTagName,
                onValueChange = { newTagName = it },
                label = { Text("Dodaj tag") },
                modifier = Modifier.weight(1f)
            )
            Button(

                onClick = {
                    if (newTagName.isNotBlank()) {
                        selectedTags = selectedTags + newTagName.trim()
                        newTagName = ""
                    }
                }
            ) {
                Text("Dodaj")
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            selectedTags.forEach { tag ->
                AssistChip(
                    onClick = {
                        selectedTags = selectedTags - tag  // Usuń tag po kliknięciu
                    },
                    label = { Text(tag) },
                    trailingIcon = {
                        Icon(Icons.Default.Close, contentDescription = "Usuń")
                    }
                )
            }
        }

        Box {
            OutlinedTextField(
                value = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                onValueChange = { },
                label = { Text("Data") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )
            Surface(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { datePickerDialog.show() },
                color = Color.Transparent
            ) { }
        }

        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("Komentarz (opcjonalnie)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                val amountInCents = (amount.replace(',', '.').toDoubleOrNull()?.times(100))?.roundToInt() ?: 0
                if (amountInCents > 0) {
                    val transaction = Transaction(
                        amountInCents = amountInCents,
                        type = type,
                        date = date,
                        comment = comment.ifBlank { null }
                    )
                    onSave(transaction, selectedTags)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Zapisz")
        }
    }
}