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
import androidx.compose.ui.graphics.Color
import com.tensiorr.budgetapp.data.dao.TagDao
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll


@Composable
fun AddTransactionScreen(tagDao: TagDao, onSave: (Transaction, List<String>) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var comment by remember { mutableStateOf("") }

    var selectedTags by remember { mutableStateOf(listOf<String>()) }
    var tempSelectedTags by remember { mutableStateOf(listOf<String>()) }

    var newTagInDialog by remember { mutableStateOf("") }

    var showTagDialog by remember { mutableStateOf(false) }
    var showNewTagDialog by remember { mutableStateOf(false) }

    val existingTags = tagDao.getAllTags()
        .collectAsState(initial = emptyList())
        .value
        .filter { it.transactionType == type }

    val allAvailableTags = remember(existingTags, tempSelectedTags) {
        val dbTagNames = existingTags.map { it.name }
        val newTags = tempSelectedTags.filter { !dbTagNames.contains(it) }
        existingTags.map { it.name } + newTags
    }

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

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
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

            OutlinedButton(
                onClick = {
                    tempSelectedTags = selectedTags
                    showTagDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Dodaj kategorie")
            }

            if (selectedTags.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedTags.forEach { tag ->
                        AssistChip(
                            onClick = { selectedTags = selectedTags - tag },
                            label = { Text(tag) }
                        )
                    }
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
        }
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



    if (showTagDialog) {
        AlertDialog(
            onDismissRequest = { showTagDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Wybierz kategorie")
                    IconButton(onClick = { showNewTagDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Nowa kategoria")
                    }
                }
            },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(allAvailableTags) { tagName ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = tempSelectedTags.contains(tagName),
                                onCheckedChange = { checked ->
                                    tempSelectedTags = if (checked) {
                                        tempSelectedTags + tagName
                                    } else {
                                        tempSelectedTags - tagName
                                    }
                                }
                            )
                            Text(tagName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedTags = tempSelectedTags
                    showTagDialog = false
                }) {
                    Text("Dodaj")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTagDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }

    if (showNewTagDialog) {
        AlertDialog(
            onDismissRequest = { showNewTagDialog = false },
            title = { Text("Nowa kategoria") },
            text = {
                OutlinedTextField(
                    value = newTagInDialog,
                    onValueChange = { newTagInDialog = it },
                    label = { Text("Nazwa kategorii") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTagInDialog.isNotBlank() && !tempSelectedTags.contains(newTagInDialog.trim())) {
                            tempSelectedTags = tempSelectedTags + newTagInDialog.trim()
                            newTagInDialog = ""
                            showNewTagDialog = false
                        }
                    }
                ) {
                    Text("Dodaj")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    newTagInDialog = ""
                    showNewTagDialog = false
                }) {
                    Text("Anuluj")
                }
            }
        )
    }
}