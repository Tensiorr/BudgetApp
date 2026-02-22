package com.tensiorr.budgetapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.tensiorr.budgetapp.data.dao.CategoryDao
import com.tensiorr.budgetapp.data.dao.SavingsGoalDao
import com.tensiorr.budgetapp.data.dao.TagDao
import com.tensiorr.budgetapp.data.entity.Category
import com.tensiorr.budgetapp.data.entity.SavingsGoal
import com.tensiorr.budgetapp.data.entity.Tag
import com.tensiorr.budgetapp.data.entity.Transaction
import com.tensiorr.budgetapp.data.entity.TransactionType
import com.tensiorr.budgetapp.ui.dialogs.CustomDatePickerDialog
import com.tensiorr.budgetapp.ui.models.DateFormatOption
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun AddTransactionScreen(
    categoryDao: CategoryDao,
    tagDao: TagDao,
    savingsGoalDao: SavingsGoalDao,
    transactionToEdit: Transaction? = null,
    existingTagId: Long? = null,
    dateFormat: DateFormatOption,
    onSave: (Transaction, Long?) -> Unit
) {
    var amountText by remember {
        mutableStateOf(
            transactionToEdit?.let { (it.amountInCents / 100.0).toString() } ?: ""
        )
    }

    var transactionType by remember {
        mutableStateOf(transactionToEdit?.type ?: TransactionType.EXPENSE)
    }

    var selectedSavingsGoalId by remember {
        mutableStateOf(transactionToEdit?.savingsGoalId)
    }

    var date by remember { mutableStateOf(transactionToEdit?.date ?: LocalDate.now()) }
    var comment by remember { mutableStateOf(transactionToEdit?.comment ?: "") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedTagId by remember { mutableStateOf<Long?>(existingTagId) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showSavingsGoalDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }
    var showNewCategoryDialog by remember { mutableStateOf(false) }
    var showNewTagDialog by remember { mutableStateOf(false) }

    var newCategoryName by remember { mutableStateOf("") }
    var newTagName by remember { mutableStateOf("") }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    val categories = categoryDao.getCategoriesForType(transactionType)
        .collectAsState(initial = emptyList())

    val tagsForCategory = remember(selectedCategoryId) {
        if (selectedCategoryId != null) {
            tagDao.getTagsForCategory(selectedCategoryId!!)
        } else {
            null
        }
    }?.collectAsState(initial = emptyList())

    val activeSavingsGoals = savingsGoalDao.getActiveGoals()
        .collectAsState(initial = emptyList())

    LaunchedEffect(transactionType) {
        if (transactionType == TransactionType.SAVING) {
            selectedCategoryId = null
            selectedTagId = null
        } else {
            selectedSavingsGoalId = null
        }
    }

    LaunchedEffect(existingTagId, categories.value) {
        if (existingTagId != null && selectedCategoryId == null && categories.value.isNotEmpty()) {
            val tag = tagDao.getTagById(existingTagId)
            tag?.let {
                selectedCategoryId = it.categoryId
            }
        }
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
                    text = if (transactionToEdit == null) "Nowa transakcja" else "Edytuj transakcję",
                    style = MaterialTheme.typography.headlineMedium
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { newValue ->
                        val normalized = newValue.replace(',', '.')
                        if (normalized.isEmpty() || normalized.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            amountText = newValue
                        }
                    },
                    label = { Text("Kwota (PLN)") },
                    placeholder = { Text("0.00") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = transactionType == TransactionType.EXPENSE,
                        onClick = { transactionType = TransactionType.EXPENSE },
                        label = { Text("Wydatek") },
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = transactionType == TransactionType.INCOME,
                        onClick = { transactionType = TransactionType.INCOME },
                        label = { Text("Przychód") },
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = transactionType == TransactionType.SAVING,
                        onClick = { transactionType = TransactionType.SAVING },
                        label = { Text("Oszczędności") },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (transactionType == TransactionType.SAVING) {
                    OutlinedButton(
                        onClick = { showSavingsGoalDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (selectedSavingsGoalId == null) {
                                "Wybierz skarbonkę"
                            } else {
                                "Zmień skarbonkę"
                            }
                        )
                    }

                    selectedSavingsGoalId?.let { goalId ->
                        val goal = activeSavingsGoals.value.find { it.id == goalId }
                        goal?.let {
                            AssistChip(
                                onClick = { selectedSavingsGoalId = null },
                                label = { Text(it.name) },
                                trailingIcon = {
                                    Icon(Icons.Default.Close, contentDescription = "Usuń")
                                }
                            )
                        }
                    }
                }

                if (transactionType != TransactionType.SAVING) {
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
                }

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Data: ${dateFormat.format(date)}")
                }

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Komentarz (opcjonalnie)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }

            Button(
                onClick = {
                    val amountInCents = (amountText.replace(',', '.')
                        .toDoubleOrNull()
                        ?.times(100))
                        ?.toInt() ?: 0

                    if (amountInCents > 0) {
                        val transaction = Transaction(
                            id = transactionToEdit?.id ?: 0,
                            amountInCents = amountInCents,
                            type = transactionType,
                            date = date,
                            comment = comment.ifBlank { null },
                            savingsGoalId = if (transactionType == TransactionType.SAVING) {
                                selectedSavingsGoalId
                            } else {
                                null
                            }
                        )

                        val tagIdToSave = if (transactionType == TransactionType.SAVING) {
                            null
                        } else {
                            selectedTagId
                        }

                        onSave(transaction, tagIdToSave)
                    }
                },
                enabled = amountText.isNotBlank() &&
                        amountText.replace(',', '.').toDoubleOrNull() != null &&
                        (transactionType != TransactionType.SAVING || selectedSavingsGoalId != null),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(if (transactionToEdit == null) "Dodaj" else "Zapisz")
            }
        }

        if (showSavingsGoalDialog) {
            AlertDialog(
                onDismissRequest = { showSavingsGoalDialog = false },
                title = { Text("Wybierz skarbonkę") },
                text = {
                    LazyColumn {
                        items(activeSavingsGoals.value) { goal ->
                            TextButton(
                                onClick = {
                                    selectedSavingsGoalId = goal.id
                                    showSavingsGoalDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = goal.name,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "${formatAmount(goal.currentAmount)} / ${formatAmount(goal.targetAmount)} PLN",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        item {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            TextButton(
                                onClick = {
                                    showSavingsGoalDialog = false
                                    showAddGoalDialog = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Nowa skarbonka")
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showSavingsGoalDialog = false }) {
                        Text("Anuluj")
                    }
                }
            )
        }

        if (showAddGoalDialog) {
            AddEditSavingsGoalDialog(
                goal = null,
                savingsGoalDao = savingsGoalDao,
                dateFormat = dateFormat,
                onDismiss = { showAddGoalDialog = false },
                onConfirm = { name, targetAmount, deadline ->
                    scope.launch {
                        val newGoalId = savingsGoalDao.insert(
                            SavingsGoal(
                                name = name,
                                targetAmount = targetAmount,
                                deadline = deadline,
                                createdAt = LocalDate.now()
                            )
                        )
                        selectedSavingsGoalId = newGoalId
                        showAddGoalDialog = false
                    }
                }
            )
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
                                        transactionType
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
                                                transactionType = transactionType
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
                                                transactionType = transactionType
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

        if (showDatePicker) {
            CustomDatePickerDialog(
                initialDate = date,
                onDateSelected = { selectedDate ->
                    date = selectedDate
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false },
                dateFormat = dateFormat
            )
        }

        snackbarMessage?.let { message ->
            Dialog(onDismissRequest = { snackbarMessage = null }) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { snackbarMessage = null }) {
                            Text("OK", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}