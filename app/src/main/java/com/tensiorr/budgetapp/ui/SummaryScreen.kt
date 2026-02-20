package com.tensiorr.budgetapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tensiorr.budgetapp.data.entity.Category
import com.tensiorr.budgetapp.data.entity.Tag
import com.tensiorr.budgetapp.data.entity.Transaction
import com.tensiorr.budgetapp.data.entity.TransactionType
import com.tensiorr.budgetapp.data.entity.TransactionWithTags
import com.tensiorr.budgetapp.data.preferences.UserPreferences
import com.tensiorr.budgetapp.ui.theme.Green
import com.tensiorr.budgetapp.ui.theme.Red
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

fun String.capitalize(): String = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
}

sealed class DateRangeType {
    data class Month(val yearMonth: YearMonth) : DateRangeType()
    data class Custom(val startDate: LocalDate, val endDate: LocalDate) : DateRangeType()
}

fun getAvailableMonths(transactions: List<Transaction>): List<YearMonth> {
    if (transactions.isEmpty()) return listOf(YearMonth.now())
    val months = transactions.map { YearMonth.from(it.date) }.distinct().sorted()
    return if (months.isEmpty()) listOf(YearMonth.now()) else months.reversed()
}

@Composable
fun SummaryScreen(
    transactions: List<TransactionWithTags>,
    categories: List<Category>,
    dateFormat: DateFormatOption
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
                is DateRangeType.Month -> YearMonth.from(date) == range.yearMonth
                is DateRangeType.Custom -> !date.isBefore(range.startDate) && !date.isAfter(range.endDate)
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
            onRangeChange = { selectedRangeType = it },
            dateFormat = dateFormat
        )

        SummaryCards(
            filteredTransactions = filteredTransactions,
            categories = categories,
            dateFormat = dateFormat
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeSelector(
    selectedRange: DateRangeType,
    availableMonths: List<YearMonth>,
    onRangeChange: (DateRangeType) -> Unit,
    dateFormat: DateFormatOption = DateFormatOption.DD_MM_YYYY
) {
    var expanded by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf(false) }

    val displayText = when (selectedRange) {
        is DateRangeType.Month -> {
            val formatter = DateTimeFormatter.ofPattern("LLLL yyyy")
            selectedRange.yearMonth.format(formatter).capitalize()
        }
        is DateRangeType.Custom -> {
            "${dateFormat.format(selectedRange.startDate)} - ${dateFormat.format(selectedRange.endDate)}"
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
            },
            dateFormat = dateFormat
        )
    }
}

@Composable
fun SummaryCards(
    filteredTransactions: List<TransactionWithTags>,
    categories: List<Category>,
    dateFormat: DateFormatOption
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
                Text(text = "SALDO", style = MaterialTheme.typography.labelMedium)
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
                    Text(text = "PRZYCHODY", style = MaterialTheme.typography.labelSmall)
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
                    Text(text = "WYDATKI", style = MaterialTheme.typography.labelSmall)
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
                    title = "KATEGORIE WYDATKÓW",
                    dateFormat = dateFormat
                )
            }

            if (income > 0) {
                CategoryBreakdown(
                    filteredTransactions = filteredTransactions,
                    totalAmount = income,
                    categories = categories,
                    transactionType = TransactionType.INCOME,
                    title = "KATEGORIE PRZYCHODÓW",
                    dateFormat = dateFormat
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

fun formatAmount(cents: Int): String {
    return String.format("%,.2f", cents / 100.0).replace(',', ' ').replace('.', ',')
}

@Composable
fun CategoryBreakdown(
    filteredTransactions: List<TransactionWithTags>,
    totalAmount: Int,
    categories: List<Category>,
    transactionType: TransactionType,
    title: String,
    dateFormat: DateFormatOption
) {
    val transactionsByCategory = filteredTransactions
        .filter { it.transaction.type == transactionType }
        .flatMap { transactionWithTags ->
            transactionWithTags.tags.map { tag ->
                tag to transactionWithTags.transaction.amountInCents
            }
        }
        .groupBy { it.first.categoryId }
        .mapValues { (_, pairs) -> pairs.sumOf { it.second } }
        .toList()
        .sortedByDescending { it.second }

    val untaggedTransactions = filteredTransactions
        .filter { it.transaction.type == transactionType }
        .filter { it.tags.isEmpty() }

    val untaggedAmount = untaggedTransactions.sumOf { it.transaction.amountInCents }

    if (transactionsByCategory.isEmpty() && untaggedTransactions.isEmpty()) return

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
                    transactionType = transactionType,
                    dateFormat = dateFormat
                )
            }
        }

        if (untaggedTransactions.isNotEmpty()) {
            key("untagged", transactionType, filteredTransactions.size) {
                UntaggedCategoryItem(
                    amount = untaggedAmount,
                    totalAmount = totalAmount,
                    transactions = untaggedTransactions,
                    transactionType = transactionType,
                    dateFormat = dateFormat
                )
            }
        }
    }
}

@Composable
fun CategoryItem(
    categoryId: Long,
    amount: Int,
    totalAmount: Int,
    transactions: List<TransactionWithTags>,
    categories: List<Category>,
    transactionType: TransactionType,
    dateFormat: DateFormatOption
) {
    var expanded by remember { mutableStateOf(false) }
    var expandedTags by remember { mutableStateOf<Set<Long>>(emptySet()) }

    val categoryName = categories.firstOrNull { it.id == categoryId }?.name
        ?: "Nieznana kategoria"

    val percentage = if (totalAmount > 0) {
        (amount.toFloat() / totalAmount * 100).toInt()
    } else 0

    Card(modifier = Modifier.fillMaxWidth()) {
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
                    Text(text = categoryName, style = MaterialTheme.typography.titleMedium)
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

                val tagGroups = transactions
                    .filter { it.transaction.type == transactionType }
                    .flatMap { transactionWithTags ->
                        transactionWithTags.tags
                            .filter { it.categoryId == categoryId }
                            .map { tag -> tag to transactionWithTags }
                    }
                    .groupBy { it.first }

                tagGroups.forEach { (tag, tagTransactions) ->
                    val tagAmount = tagTransactions.sumOf { it.second.transaction.amountInCents }
                    val isExpanded = expandedTags.contains(tag.id)

                    TagItemWithTransactions(
                        tag = tag,
                        amount = tagAmount,
                        transactions = tagTransactions.map { it.second },
                        isExpanded = isExpanded,
                        onToggleExpand = {
                            expandedTags = if (isExpanded) {
                                expandedTags - tag.id
                            } else {
                                expandedTags + tag.id
                            }
                        },
                        dateFormat = dateFormat
                    )
                }
            }
        }
    }
}

@Composable
fun TagItemWithTransactions(
    tag: Tag,
    amount: Int,
    transactions: List<TransactionWithTags>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    dateFormat: DateFormatOption
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(text = "↳ ${tag.name}", style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                text = formatAmount(amount) + " PLN",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (isExpanded) {
            val sortedTransactions = transactions.sortedByDescending { it.transaction.date }
            sortedTransactions.forEach { transactionWithTags ->
                TransactionDetailItem(
                    transaction = transactionWithTags.transaction,
                    dateFormat = dateFormat
                )
            }
        }
    }
}

@Composable
fun UntaggedCategoryItem(
    amount: Int,
    totalAmount: Int,
    transactions: List<TransactionWithTags>,
    transactionType: TransactionType,
    dateFormat: DateFormatOption
) {
    var expanded by remember { mutableStateOf(false) }

    val percentage = if (totalAmount > 0) {
        (amount.toFloat() / totalAmount * 100).toInt()
    } else 0

    Card(modifier = Modifier.fillMaxWidth()) {
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
                        text = "Nieprzypisane",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

                val sortedTransactions = transactions.sortedByDescending { it.transaction.date }
                sortedTransactions.forEach { transactionWithTags ->
                    TransactionDetailItem(
                        transaction = transactionWithTags.transaction,
                        dateFormat = dateFormat
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionDetailItem(
    transaction: Transaction,
    dateFormat: DateFormatOption
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, top = 4.dp, bottom = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "• ${dateFormat.format(transaction.date)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatAmount(transaction.amountInCents) + " PLN",
                style = MaterialTheme.typography.bodySmall
            )
        }

        transaction.comment?.let { comment ->
            Text(
                text = "\"$comment\"",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp, top = 2.dp)
            )
        }
    }
}