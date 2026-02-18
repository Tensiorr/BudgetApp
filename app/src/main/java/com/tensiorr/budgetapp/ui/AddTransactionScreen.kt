package com.tensiorr.budgetapp.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.tensiorr.budgetapp.data.dao.CategoryDao
import com.tensiorr.budgetapp.data.dao.TagDao
import com.tensiorr.budgetapp.data.entity.Category
import com.tensiorr.budgetapp.data.entity.Tag
import com.tensiorr.budgetapp.data.entity.Transaction
import com.tensiorr.budgetapp.data.entity.TransactionType
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import kotlin.math.roundToInt

/**
 * Screen for adding/editing transactions with two-level category/tag selection.
 *
 * Flow:
 * 1. User fills in amount, type, date, and optional comment
 * 2. User selects category → then selects tag from that category
 * 3. On save, creates or updates transaction with optional tag relationship
 *
 * @param transactionToEdit If provided, enters edit mode and pre-fills fields
 * @param existingTagId Tag ID associated with transaction being edited
 */
@Composable
fun AddTransactionScreen(
    categoryDao: CategoryDao,
    tagDao: TagDao,
    transactionToEdit: Transaction? = null,
    existingTagId: Long? = null,
    onSave: (Transaction, Long?) -> Unit
) {
    var amount by remember {
        mutableStateOf(
            transactionToEdit?.let { (it.amountInCents / 100.0).toString() } ?: ""
        )
    }
    var type by remember { mutableStateOf(transactionToEdit?.type ?: TransactionType.EXPENSE) }
    var date by remember { mutableStateOf(transactionToEdit?.date ?: LocalDate.now()) }
    var comment by remember { mutableStateOf(transactionToEdit?.comment ?: "") }

    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedTagId by remember { mutableStateOf(existingTagId) }

    var showCategoryDialog by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }
    var showNewCategoryDialog by remember { mutableStateOf(false) }
    var showNewTagDialog by remember { mutableStateOf(false) }

    var newCategoryName by remember { mutableStateOf("") }
    var newTagName by remember { mutableStateOf("") }

    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val categories = categoryDao.getCategoriesForType(type)
        .collectAsState(initial = emptyList())

    val tagsForCategory = remember(selectedCategoryId) {
        if (selectedCategoryId != null) {
            tagDao.getTagsForCategory(selectedCategoryId!!)
        } else {
            null
        }
    }?.collectAsState(initial = emptyList())

    LaunchedEffect(existingTagId, categories.value) {
        if (existingTagId != null && selectedCategoryId == null && categories.value.isNotEmpty()) {
            val tag = tagDao.getTagById(existingTagId)
            tag?.let {
                selectedCategoryId = it.categoryId
            }
        }
    }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val datePickerDialog = remember {
        val calendar = Calendar.getInstance()
        calendar.time = java.util.Date.from(
            date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
        )

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

    fun showMessage(message: String) {
        snackbarMessage = message
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (transactionToEdit != null) "Edytuj transakcję" else "Nowa transakcja",
                    style = MaterialTheme.typography.headlineMedium
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { newValue ->
                        val normalized = newValue.replace(',', '.')
                        if (normalized.isEmpty() ||
                            normalized.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
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
                            type = if (isIncome) TransactionType.INCOME
                            else TransactionType.EXPENSE
                            selectedCategoryId = null
                            selectedTagId = null
                        }
                    )
                    Text("Przychód")
                }

                OutlinedButton(
                    onClick = { showCategoryDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Dodaj kategorię")
                }

                selectedTagId?.let { tagId ->
                    val tag = tagsForCategory?.value?.find { it.id == tagId }
                    val category = categories.value.find { it.id == selectedCategoryId }

                    if (tag != null && category != null) {
                        AssistChip(
                            onClick = {
                                selectedTagId = null
                                selectedCategoryId = null
                            },
                            label = { Text("${category.name} > ${tag.name}") },
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
            }

            Button(
                onClick = {
                    val amountInCents = (amount.replace(',', '.')
                        .toDoubleOrNull()
                        ?.times(100))
                        ?.roundToInt() ?: 0

                    if (amountInCents > 0) {
                        val transaction = Transaction(
                            id = transactionToEdit?.id ?: 0,
                            amountInCents = amountInCents,
                            type = type,
                            date = date,
                            comment = comment.ifBlank { null }
                        )
                        onSave(transaction, selectedTagId)
                    }
                },
                enabled = amount.isNotBlank() && amount.replace(',', '.').toDoubleOrNull() != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Zapisz")
            }
        }

        if (showCategoryDialog) {
            AlertDialog(
                onDismissRequest = { showCategoryDialog = false },
                title = { Text("Wybierz kategorię") },
                text = {
                    LazyColumn {
                        items(categories.value) { category ->
                            TextButton(
                                onClick = {
                                    selectedCategoryId = category.id
                                    showCategoryDialog = false
                                    showTagDialog = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = category.name,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        item {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            TextButton(
                                onClick = {
                                    showCategoryDialog = false
                                    showNewCategoryDialog = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Nowa kategoria")
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showCategoryDialog = false }) {
                        Text("Anuluj")
                    }
                }
            )
        }

        if (showTagDialog && selectedCategoryId != null) {
            val categoryName = categories.value
                .find { it.id == selectedCategoryId }?.name ?: ""

            AlertDialog(
                onDismissRequest = { showTagDialog = false },
                title = { Text("Wybierz tag z kategorii \"$categoryName\"") },
                text = {
                    LazyColumn {
                        tagsForCategory?.value?.let { tags ->
                            items(tags) { tag ->
                                TextButton(
                                    onClick = {
                                        selectedTagId = tag.id
                                        showTagDialog = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = tag.name,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        item {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            TextButton(
                                onClick = {
                                    showTagDialog = false
                                    showNewTagDialog = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Nowy tag")
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = {
                        showTagDialog = false
                        selectedCategoryId = null
                    }) {
                        Text("Wstecz")
                    }
                }
            )
        }

        if (showNewCategoryDialog) {
            AlertDialog(
                onDismissRequest = { showNewCategoryDialog = false },
                title = { Text("Nowa kategoria") },
                text = {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("Nazwa kategorii") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newCategoryName.isNotBlank()) {
                                scope.launch {
                                    val existing = categoryDao.getCategoryByNameAndType(
                                        newCategoryName.trim(),
                                        type
                                    )

                                    if (existing != null) {
                                        selectedCategoryId = existing.id
                                        val categoryName = existing.name
                                        newCategoryName = ""
                                        showNewCategoryDialog = false
                                        showTagDialog = true
                                        showMessage("Kategoria '$categoryName' już istnieje")
                                    } else {
                                        val categoryId = categoryDao.insert(
                                            Category(
                                                name = newCategoryName.trim(),
                                                transactionType = type
                                            )
                                        )
                                        selectedCategoryId = categoryId
                                        newCategoryName = ""
                                        showNewCategoryDialog = false
                                        showTagDialog = true
                                    }
                                }
                            }
                        }
                    ) {
                        Text("Dodaj")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        newCategoryName = ""
                        showNewCategoryDialog = false
                    }) {
                        Text("Anuluj")
                    }
                }
            )
        }

        if (showNewTagDialog && selectedCategoryId != null) {
            AlertDialog(
                onDismissRequest = { showNewTagDialog = false },
                title = { Text("Nowy tag") },
                text = {
                    OutlinedTextField(
                        value = newTagName,
                        onValueChange = { newTagName = it },
                        label = { Text("Nazwa tagu") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newTagName.isNotBlank() && selectedCategoryId != null) {
                                scope.launch {
                                    val existing = tagDao.getTagByNameAndCategory(
                                        newTagName.trim(),
                                        selectedCategoryId!!
                                    )

                                    if (existing != null) {
                                        selectedTagId = existing.id
                                        val tagName = existing.name
                                        newTagName = ""
                                        showNewTagDialog = false
                                        showMessage("Tag '$tagName' już istnieje w tej kategorii")
                                    } else {
                                        val tagId = tagDao.insertTag(
                                            Tag(
                                                name = newTagName.trim(),
                                                categoryId = selectedCategoryId!!,
                                                transactionType = type
                                            )
                                        )
                                        selectedTagId = tagId
                                        newTagName = ""
                                        showNewTagDialog = false
                                    }
                                }
                            }
                        }
                    ) {
                        Text("Dodaj")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        newTagName = ""
                        showNewTagDialog = false
                    }) {
                        Text("Anuluj")
                    }
                }
            )
        }

        snackbarMessage?.let { message ->
            Dialog(onDismissRequest = { snackbarMessage = null }) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.inverseSurface,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { snackbarMessage = null }) {
                            Text("OK", color = MaterialTheme.colorScheme.inversePrimary)
                        }
                    }
                }
            }
        }
    }
}