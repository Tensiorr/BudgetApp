package com.tensiorr.budgetapp.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tensiorr.budgetapp.data.dao.CategoryDao
import com.tensiorr.budgetapp.data.dao.TransactionDao
import com.tensiorr.budgetapp.data.entity.Category
import com.tensiorr.budgetapp.data.entity.Transaction
import com.tensiorr.budgetapp.data.entity.TransactionType
import com.tensiorr.budgetapp.data.entity.TransactionWithTags
import com.tensiorr.budgetapp.ui.theme.Green
import com.tensiorr.budgetapp.ui.theme.Red
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

/**
 * Extension function for capitalizing first character of a string.
 */
fun String.capitalize(): String = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
}

/**
 * Sealed class representing date range selection types.
 * Can be either a specific month or a custom date range.
 */
sealed class DateRangeType {
    data class Month(val yearMonth: YearMonth) : DateRangeType()
    data class Custom(val startDate: LocalDate, val endDate: LocalDate) : DateRangeType()
}

/**
 * Gets list of available months from transactions, sorted newest first.
 * Returns current month if no transactions exist.
 */
fun getAvailableMonths(transactions: List<Transaction>): List<YearMonth> {
    if (transactions.isEmpty()) return listOf(YearMonth.now())

    val months = transactions.map { YearMonth.from(it.date) }.distinct().sorted()
    return if (months.isEmpty()) listOf(YearMonth.now()) else months.reversed()
}

/**
 * Screen displaying financial summaries with date range filtering.
 *
 * Features:
 * - Monthly or custom date range selection
 * - Balance, income, and expense summary cards
 * - Category breakdown for expenses and income with percentages
 * - Expandable categories showing individual tags
 */
@Composable
fun SummaryScreen(
    transactionDao: TransactionDao,
    categoryDao: CategoryDao,
    transactions: List<TransactionWithTags>,
    categories: List<Category>
) {
    var selectedRangeType by remember { mutableStateOf<DateRangeType>(
        DateRangeType.Month(YearMonth.now())
    ) }

    val availableMonths = remember(transactions) {
        getAvailableMonths(transactions.map { it.transaction })
    }

    val filteredTransactions = remember(transactions, selectedRangeType) {
        transactions.filter { transactionWithTags ->
            val date = transactionWithTags.transaction.date
            when (val range = selectedRangeType) {
                is DateRangeType.Month -> {
                    YearMonth.from(date) == range.yearMonth
                }
                is DateRangeType.Custom -> {
                    !date.isBefore(range.startDate) && !date.isAfter(range.endDate)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Zestawienia",
            style = MaterialTheme.typography.headlineMedium
        )

        DateRangeSelector(
            selectedRange = selectedRangeType,
            availableMonths = availableMonths,
            onRangeChange = { selectedRangeType = it }
        )

        SummaryCards(
            filteredTransactions = filteredTransactions,
            categories = categories
        )
    }
}

/**
 * Dropdown selector for choosing date range (monthly or custom).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeSelector(
    selectedRange: DateRangeType,
    availableMonths: List<YearMonth>,
    onRangeChange: (DateRangeType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf(false) }

    val displayText = when (selectedRange) {
        is DateRangeType.Month -> {
            val formatter = DateTimeFormatter.ofPattern("LLLL yyyy")
            selectedRange.yearMonth.format(formatter).capitalize()
        }
        is DateRangeType.Custom -> {
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            "${selectedRange.startDate.format(formatter)} - ${selectedRange.endDate.format(formatter)}"
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text("Okres") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableMonths.forEach { month ->
                val formatter = DateTimeFormatter.ofPattern("LLLL yyyy")
                DropdownMenuItem(
                    text = { Text(month.format(formatter).capitalize()) },
                    onClick = {
                        onRangeChange(DateRangeType.Month(month))
                        expanded = false
                    }
                )
            }

            HorizontalDivider()

            DropdownMenuItem(
                text = { Text("Własny zakres...") },
                onClick = {
                    expanded = false
                    showCustomDialog = true
                }
            )
        }
    }

    if (showCustomDialog) {
        CustomDateRangeDialog(
            onDismiss = { showCustomDialog = false },
            onConfirm = { startDate, endDate ->
                onRangeChange(DateRangeType.Custom(startDate, endDate))
                showCustomDialog = false
            }
        )
    }
}

/**
 * Dialog for selecting custom date range with start and end date pickers.
 */
@Composable
fun CustomDateRangeDialog(
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, LocalDate) -> Unit
) {
    var startDate by remember { mutableStateOf(LocalDate.now().minusMonths(1)) }
    var endDate by remember { mutableStateOf(LocalDate.now()) }

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Własny zakres dat") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        calendar.time = java.util.Date.from(
                            startDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
                        )

                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                startDate = LocalDate.of(year, month + 1, day)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Od: ${startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}")
                }

                OutlinedButton(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        calendar.time = java.util.Date.from(
                            endDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
                        )

                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                endDate = LocalDate.of(year, month + 1, day)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
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
}

/**
 * Displays summary cards showing balance, income, expense totals,
 * and category breakdowns for both expenses and income.
 */
@Composable
fun SummaryCards(
    filteredTransactions: List<TransactionWithTags>,
    categories: List<Category>
) {
    val income = filteredTransactions
        .filter { it.transaction.type == TransactionType.INCOME }
        .sumOf { it.transaction.amountInCents }

    val expenses = filteredTransactions
        .filter { it.transaction.type == TransactionType.EXPENSE }
        .sumOf { it.transaction.amountInCents }

    val balance = income - expenses

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    balance > 0 -> MaterialTheme.colorScheme.primaryContainer
                    balance < 0 -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "SALDO",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = formatAmount(balance) + " PLN",
                    style = MaterialTheme.typography.headlineLarge
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "PRZYCHODY",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = formatAmount(income) + " PLN",
                        style = MaterialTheme.typography.titleLarge,
                        color = Green
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "WYDATKI",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = formatAmount(expenses) + " PLN",
                        style = MaterialTheme.typography.titleLarge,
                        color = Red
                    )
                }
            }
        }

        if (filteredTransactions.isNotEmpty()) {
            if (expenses > 0) {
                CategoryBreakdown(
                    filteredTransactions = filteredTransactions,
                    totalAmount = expenses,
                    categories = categories,
                    transactionType = TransactionType.EXPENSE,
                    title = "KATEGORIE WYDATKÓW"
                )
            }

            if (income > 0) {
                CategoryBreakdown(
                    filteredTransactions = filteredTransactions,
                    totalAmount = income,
                    categories = categories,
                    transactionType = TransactionType.INCOME,
                    title = "KATEGORIE PRZYCHODÓW"
                )
            }
        } else {
            Text(
                text = "Brak transakcji w wybranym okresie",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

/**
 * Formats amount in cents to PLN string with comma decimal separator.
 * Example: 15000 cents -> "150,00"
 */
fun formatAmount(cents: Int): String {
    return String.format("%,.2f", cents / 100.0).replace(',', ' ').replace('.', ',')
}

/**
 * Displays category breakdown list with percentages and expandable tag details.
 */
@Composable
fun CategoryBreakdown(
    filteredTransactions: List<TransactionWithTags>,
    totalAmount: Int,
    categories: List<Category>,
    transactionType: TransactionType,
    title: String
) {
    val transactionsByCategory = filteredTransactions
        .filter { it.transaction.type == transactionType }
        .flatMap { transactionWithTags ->
            transactionWithTags.tags.map { tag ->
                tag to transactionWithTags.transaction.amountInCents
            }
        }
        .groupBy { it.first.categoryId }
        .mapValues { (_, pairs) ->
            pairs.sumOf { it.second }
        }
        .toList()
        .sortedByDescending { it.second }

    if (transactionsByCategory.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp)
        )

        transactionsByCategory.forEach { (categoryId, amount) ->
            key(categoryId, transactionType, filteredTransactions.size) {
                CategoryItem(
                    categoryId = categoryId,
                    amount = amount,
                    totalAmount = totalAmount,
                    transactions = filteredTransactions,
                    categories = categories,
                    transactionType = transactionType
                )
            }
        }
    }
}

/**
 * Individual category card showing name, percentage, and total amount.
 * Expandable to show breakdown by tags.
 */
@Composable
fun CategoryItem(
    categoryId: Long,
    amount: Int,
    totalAmount: Int,
    transactions: List<TransactionWithTags>,
    categories: List<Category>,
    transactionType: TransactionType
) {
    var expanded by remember { mutableStateOf(false) }

    val categoryName = categories.firstOrNull { it.id == categoryId }?.name
        ?: "Nieznana kategoria"

    val percentage = if (totalAmount > 0) {
        (amount.toFloat() / totalAmount * 100).toInt()
    } else 0

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
                        text = categoryName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = formatAmount(amount) + " PLN",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            if (expanded) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                val tagAmounts = transactions
                    .filter { it.transaction.type == transactionType }
                    .flatMap { transactionWithTags ->
                        transactionWithTags.tags
                            .filter { it.categoryId == categoryId }
                            .map { tag ->
                                tag to transactionWithTags.transaction.amountInCents
                            }
                    }
                    .groupBy { it.first }
                    .mapValues { (_, pairs) -> pairs.sumOf { it.second } }

                tagAmounts.forEach { (tag, tagAmount) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "↳ ${tag.name}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = formatAmount(tagAmount) + " PLN",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}