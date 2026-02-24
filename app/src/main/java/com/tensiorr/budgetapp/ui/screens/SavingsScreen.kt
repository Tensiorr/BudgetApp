package com.tensiorr.budgetapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tensiorr.budgetapp.data.dao.SavingsGoalDao
import com.tensiorr.budgetapp.data.entity.SavingsGoal
import com.tensiorr.budgetapp.data.repository.SyncRepository
import com.tensiorr.budgetapp.ui.dialogs.CustomDatePickerDialog
import com.tensiorr.budgetapp.ui.models.DateFormatOption
import com.tensiorr.budgetapp.ui.viewmodel.AuthViewModel
import com.tensiorr.budgetapp.util.AuthState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import androidx.compose.ui.platform.LocalContext
import com.tensiorr.budgetapp.util.SyncManager

/**
 * Screen for managing savings goals (piggy banks).
 *
 * Features:
 * - View active and archived savings goals
 * - Create, edit, and delete goals
 * - Archive/unarchive goals
 * - Track progress towards targets
 * - View deadline status
 *
 * @param savingsGoalDao DAO for savings goal operations
 * @param dateFormat Current date format preference
 * @param activeGoals List of active savings goals (preloaded)
 * @param archivedGoals List of archived savings goals (preloaded)
 * @param onNavigateBack Callback when navigating back
 */
@Composable
fun SavingsScreen(
    savingsGoalDao: SavingsGoalDao,
    dateFormat: DateFormatOption,
    activeGoals: List<SavingsGoal>,
    archivedGoals: List<SavingsGoal>,
    onNavigateBack: () -> Unit = {},
    syncRepository: SyncRepository? = null,
    authViewModel: AuthViewModel? = null,
    isGuestMode: Boolean = false
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var showArchivedGoals by remember { mutableStateOf(false) }
    var goalToEdit by remember { mutableStateOf<SavingsGoal?>(null) }
    var goalToDelete by remember { mutableStateOf<SavingsGoal?>(null) }
    var goalToUnarchive by remember { mutableStateOf<SavingsGoal?>(null) }

    val totalSavings = activeGoals.sumOf { it.currentAmount }

    if (showArchivedGoals) {
        BackHandler {
            showArchivedGoals = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Skarbonki",
                    style = MaterialTheme.typography.headlineMedium
                )

                IconButton(onClick = { showArchivedGoals = !showArchivedGoals }) {
                    Icon(
                        imageVector = if (showArchivedGoals) {
                            Icons.Default.Unarchive
                        } else {
                            Icons.Default.Archive
                        },
                        contentDescription = if (showArchivedGoals) {
                            "Pokaż aktywne"
                        } else {
                            "Pokaż archiwum"
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (!showArchivedGoals && activeGoals.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "ŁĄCZNIE",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = formatAmount(totalSavings) + " PLN",
                            style = MaterialTheme.typography.headlineLarge
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val goalsToShow = if (showArchivedGoals) {
                    archivedGoals
                } else {
                    activeGoals
                }

                if (goalsToShow.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (showArchivedGoals) {
                                    "Brak zarchiwizowanych skarbonek"
                                } else {
                                    "Brak skarbonek.\nDodaj pierwszą klikając '+'"
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                items(goalsToShow, key = { it.id }) { goal ->
                    SavingsGoalItem(
                        goal = goal,
                        dateFormat = dateFormat,
                        isArchived = showArchivedGoals,
                        onEdit = { goalToEdit = goal },
                        onArchive = {
                            scope.launch {
                                savingsGoalDao.setArchived(goal.id, true)
                            }
                        },
                        onUnarchive = { goalToUnarchive = goal },
                        onDelete = { goalToDelete = goal },
                        isGuestMode = isGuestMode
                    )
                }
            }
        }

        if (!showArchivedGoals) {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj skarbonkę")
            }
        }
    }

    if (showAddDialog) {
        AddEditSavingsGoalDialog(
            goal = null,
            savingsGoalDao = savingsGoalDao,
            dateFormat = dateFormat,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, targetAmount, deadline ->
                scope.launch {
                    savingsGoalDao.insert(
                        SavingsGoal(
                            name = name,
                            targetAmount = targetAmount,
                            deadline = deadline
                        )
                    )

                    if (!isGuestMode) {
                        SyncManager.triggerImmediateSync(context)
                    }

                    showAddDialog = false
                }
            }
        )
    }

    goalToEdit?.let { goal ->
        AddEditSavingsGoalDialog(
            goal = goal,
            savingsGoalDao = savingsGoalDao,
            dateFormat = dateFormat,
            onDismiss = { goalToEdit = null },
            onConfirm = { name, targetAmount, deadline ->
                scope.launch {
                    savingsGoalDao.update(
                        goal.copy(
                            name = name,
                            targetAmount = targetAmount,
                            deadline = deadline,
                            updatedAt = System.currentTimeMillis()
                        )
                    )

                    if (!isGuestMode) {
                        SyncManager.triggerImmediateSync(context)
                    }

                    goalToEdit = null
                }
            }
        )
    }

    goalToDelete?.let { goal ->
        DeleteSavingsGoalDialog(
            goal = goal,
            savingsGoalDao = savingsGoalDao,
            isArchived = showArchivedGoals,
            onDismiss = { goalToDelete = null },
            onConfirm = {
                scope.launch {
                    if (!isGuestMode && syncRepository != null && authViewModel != null) {
                        val authState = authViewModel.authState.value
                        if (authState is AuthState.Authenticated) {
                            syncRepository.deleteSavingsGoal(authState.userId, goal)
                        } else {
                            savingsGoalDao.delete(goal)
                        }
                    } else {
                        savingsGoalDao.delete(goal)
                    }
                    goalToDelete = null
                }
            }
        )
    }

    goalToUnarchive?.let { goal ->
        UnarchiveSavingsGoalDialog(
            goal = goal,
            savingsGoalDao = savingsGoalDao,
            onDismiss = { goalToUnarchive = null },
            onConfirm = {
                scope.launch {
                    val activeGoals = savingsGoalDao.getActiveGoals().first()
                    var newName = goal.name
                    var counter = 1

                    while (activeGoals.any { it.name.equals(newName, ignoreCase = true) }) {
                        newName = "${goal.name}($counter)"
                        counter++
                    }

                    if (newName != goal.name) {
                        savingsGoalDao.update(
                            goal.copy(
                                name = newName,
                                isArchived = false,
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    } else {
                        savingsGoalDao.setArchived(goal.id, false)
                    }

                    if (!isGuestMode) {
                        SyncManager.triggerImmediateSync(context)
                    }

                    goalToUnarchive = null
                }
            }
        )
    }
}

@Composable
fun SavingsGoalItem(
    goal: SavingsGoal,
    dateFormat: DateFormatOption,
    isArchived: Boolean,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onUnarchive: () -> Unit,
    onDelete: () -> Unit,
    isGuestMode: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    var showArchiveDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val progressPercentage = if (goal.targetAmount > 0) {
        ((goal.currentAmount.toFloat() / goal.targetAmount) * 100).toInt()
    } else {
        0
    }

    val progressBar = if (goal.targetAmount > 0) {
        (goal.currentAmount.toFloat() / goal.targetAmount).coerceIn(0f, 1f)
    } else {
        0f
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .clickable { expanded = !expanded }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.name,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "${formatAmount(goal.currentAmount)} / ${formatAmount(goal.targetAmount)} PLN",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Zwiń" else "Rozwiń",
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progressBar },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
            )

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$progressPercentage%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                goal.deadline?.let { deadline ->
                    val isPastDeadline = deadline.isBefore(LocalDate.now())
                    Text(
                        text = "Termin: ${dateFormat.format(deadline)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isPastDeadline) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                } ?: run {
                    Text(
                        text = "Bez terminu",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (expanded) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (!isArchived) {
                        IconButton(onClick = onEdit) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edytuj"
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            if (!isArchived) {
                                showArchiveDialog = true
                            } else {
                                onUnarchive()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isArchived) {
                                Icons.Default.Unarchive
                            } else {
                                Icons.Default.Archive
                            },
                            contentDescription = if (isArchived) {
                                "Przywróć"
                            } else {
                                "Archiwizuj"
                            }
                        )
                    }

                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Usuń",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }

    if (showArchiveDialog) {
        AlertDialog(
            onDismissRequest = { showArchiveDialog = false },
            title = { Text("Archiwizuj skarbonkę") },
            text = { Text("Czy na pewno chcesz zarchiwizować skarbonkę \"${goal.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onArchive()

                        if (!isGuestMode) {
                            SyncManager.triggerImmediateSync(context)
                        }

                        showArchiveDialog = false
                    }
                ) {
                    Text("Archiwizuj")
                }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }
}

@Composable
fun AddEditSavingsGoalDialog(
    goal: SavingsGoal?,
    savingsGoalDao: SavingsGoalDao,
    dateFormat: DateFormatOption,
    onDismiss: () -> Unit,
    onConfirm: (name: String, targetAmount: Int, deadline: LocalDate?) -> Unit
) {
    var name by remember { mutableStateOf(goal?.name ?: "") }
    var targetAmountText by remember {
        mutableStateOf(
            goal?.let { (it.targetAmount / 100.0).toString() } ?: ""
        )
    }
    var deadline by remember { mutableStateOf(goal?.deadline) }
    var showDatePicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (goal == null) "Nowa skarbonka" else "Edytuj skarbonkę") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("Nazwa") },
                    placeholder = { Text("np. Wakacje, Nowy laptop") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = targetAmountText,
                    onValueChange = { newValue ->
                        val normalized = newValue.replace(',', '.')
                        if (normalized.isEmpty() || normalized.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            targetAmountText = newValue
                        }
                    },
                    label = { Text("Kwota docelowa (PLN)") },
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        deadline?.let { "Termin: ${dateFormat.format(it)}" } ?: "Ustaw termin (opcjonalnie)"
                    )
                }

                if (deadline != null) {
                    TextButton(
                        onClick = { deadline = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Usuń termin")
                    }
                }
            }
        },
        confirmButton = {
            val scope = rememberCoroutineScope()
            TextButton(
                onClick = {
                    val targetAmount = (targetAmountText.replace(',', '.')
                        .toDoubleOrNull()
                        ?.times(100))
                        ?.toInt() ?: 0

                    if (name.isNotBlank() && targetAmount > 0) {
                        scope.launch {
                            val activeSavingsGoals = savingsGoalDao.getActiveGoals().first()
                            val duplicate = activeSavingsGoals.find {
                                it.name.equals(name.trim(), ignoreCase = true) &&
                                        (goal == null || it.id != goal.id)
                            }

                            if (duplicate != null) {
                                errorMessage = "Skarbonka o nazwie \"${name.trim()}\" już istnieje!"
                            } else {
                                onConfirm(name.trim(), targetAmount, deadline)
                            }
                        }
                    }
                },
                enabled = name.isNotBlank() &&
                        targetAmountText.isNotBlank() &&
                        targetAmountText.replace(',', '.').toDoubleOrNull() != null &&
                        (targetAmountText.replace(',', '.').toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Zapisz")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )

    if (showDatePicker) {
        CustomDatePickerDialog(
            initialDate = deadline ?: LocalDate.now(),
            onDateSelected = { selectedDate ->
                deadline = selectedDate
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
            dateFormat = dateFormat
        )
    }
}

@Composable
fun UnarchiveSavingsGoalDialog(
    goal: SavingsGoal,
    savingsGoalDao: SavingsGoalDao,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Przywróć skarbonkę") },
        text = { Text("Czy na pewno chcesz przywrócić skarbonkę \"${goal.name}\"?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Przywróć")
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
fun DeleteSavingsGoalDialog(
    goal: SavingsGoal,
    savingsGoalDao: SavingsGoalDao,
    isArchived: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var transactionCount by remember { mutableIntStateOf(-1) }

    LaunchedEffect(goal.id) {
        transactionCount = savingsGoalDao.getTransactionCount(goal.id)
    }

    if (transactionCount < 0) {
        return
    }

    if (transactionCount > 0 && !isArchived) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Usuń skarbonkę") },
            text = {
                Text(
                    "Skarbonka \"${goal.name}\" ma $transactionCount " +
                            (if (transactionCount == 1) "przypisaną transakcję.\n\n" else "przypisanych transakcji.\n\n") +
                            "Transakcje zostaną oznaczone jako \"(Usunięta skarbonka)\"."
                )
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                savingsGoalDao.setArchived(goal.id, true)
                                onDismiss()
                            }
                        }
                    ) {
                        Text("Archiwizuj")
                    }
                    TextButton(
                        onClick = onConfirm,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Usuń")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Anuluj")
                }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Usuń skarbonkę") },
            text = {
                if (transactionCount > 0) {
                    Text(
                        "Skarbonka \"${goal.name}\" ma $transactionCount " +
                                (if (transactionCount == 1) "przypisaną transakcję.\n\n" else "przypisanych transakcji.\n\n") +
                                "Transakcje zostaną oznaczone jako \"(Usunięta skarbonka)\".\n\n" +
                                "Czy na pewno chcesz usunąć tę skarbonkę?"
                    )
                } else {
                    Text("Czy na pewno chcesz usunąć skarbonkę \"${goal.name}\"?")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Usuń")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Anuluj")
                }
            }
        )
    }
}