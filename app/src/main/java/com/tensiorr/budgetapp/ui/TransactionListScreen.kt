package com.tensiorr.budgetapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tensiorr.budgetapp.data.entity.Category
import com.tensiorr.budgetapp.data.entity.Tag
import com.tensiorr.budgetapp.data.entity.Transaction
import com.tensiorr.budgetapp.data.entity.TransactionType
import com.tensiorr.budgetapp.data.entity.TransactionWithTags
import com.tensiorr.budgetapp.ui.theme.Black
import com.tensiorr.budgetapp.ui.theme.Green
import com.tensiorr.budgetapp.ui.theme.Red
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.ui.text.style.TextAlign
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale
import androidx.compose.foundation.layout.FlowRow
import com.tensiorr.budgetapp.data.dao.TagDao

/**
 * Screen displaying list of transactions with balance summary.
 *
 * Features:
 * - Balance card at the top showing total income minus expenses
 * - Scrollable list of transactions with swipe-to-delete functionality
 * - Each transaction shows amount, category/tag path, date, and optional comment
 * - Confirmation dialog before deleting
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    transactionsWithTags: List<TransactionWithTags>,
    categories: List<Category>,
    tagDao: TagDao,
    dateFormat: DateFormatOption,
    onDelete: (Transaction) -> Unit,
    onEdit: (Transaction, Long?) -> Unit
) {
    var filterState by remember { mutableStateOf(FilterState()) }
    var showFilterDialog by remember { mutableStateOf(false) }

    val filteredTransactions = remember(transactionsWithTags, filterState) {
        applyFilters(transactionsWithTags, filterState)
    }

    val balance = CalculateBalance(filteredTransactions.map { it.transaction })
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Saldo: ${balance / 100.0} PLN",
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (balance > 0) Green else if (balance < 0) Red else Black
                )
                if (filterState.hasActiveFilters()) {
                    Text(
                        text = "(dla wybranych filtrów)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = false,
                onClick = { showFilterDialog = true },
                label = {
                    Text(if (filterState.hasActiveFilters()) "Edytuj filtry" else "Filtry")
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            if (filterState.transactionType != TransactionTypeFilter.ALL) {
                FilterChip(
                    selected = true,
                    onClick = {
                        filterState = filterState.copy(
                            transactionType = TransactionTypeFilter.ALL
                        )
                    },
                    label = {
                        Text(
                            when (filterState.transactionType) {
                                TransactionTypeFilter.EXPENSE -> "Wydatki"
                                TransactionTypeFilter.INCOME -> "Przychody"
                                else -> ""
                            }
                        )
                    },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Usuń",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            filterState.categoryIds.forEach { categoryId ->
                val category = categories.find { it.id == categoryId }
                if (category != null) {
                    FilterChip(
                        selected = true,
                        onClick = {
                            filterState = filterState.copy(
                                categoryIds = filterState.categoryIds - categoryId
                            )
                        },
                        label = { Text(category.name) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Usuń",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            filterState.tagIds.forEach { tagId ->
                var tagName: String? = null
                categories.forEach { category ->
                    val tags = tagDao.getTagsForCategory(category.id)
                        .collectAsState(initial = emptyList()).value
                    tags.find { it.id == tagId }?.let { tagName = it.name }
                }

                if (tagName != null) {
                    FilterChip(
                        selected = true,
                        onClick = {
                            filterState = filterState.copy(
                                tagIds = filterState.tagIds - tagId
                            )
                        },
                        label = { Text(tagName!!) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Usuń",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
            if (filterState.dateRange !is DateRangeFilter.AllTime) {
                FilterChip(
                    selected = true,
                    onClick = {
                        filterState = filterState.copy(
                            dateRange = DateRangeFilter.AllTime
                        )
                    },
                    label = {
                        Text(
                            when (val range = filterState.dateRange) {
                                is DateRangeFilter.Custom -> {
                                    "${dateFormat.format(range.startDate)} - ${dateFormat.format(range.endDate)}"
                                }
                                else -> range.displayName()
                            }
                        )
                    },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Usuń",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp
            )
        ) {
            if (filteredTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (filterState.hasActiveFilters()) {
                                "Brak transakcji dla podanych filtrów"
                            } else {
                                "Brak transakcji.\nDodaj pierwszą klikając zakładkę '+'"
                            },
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            } else {
                val groupedTransactions = filteredTransactions.groupBy {
                    YearMonth.from(it.transaction.date)
                }

                groupedTransactions.forEach { (yearMonth, transactions) ->
                    item(key = "header_$yearMonth") {
                        Text(
                            text = yearMonth.format(
                                DateTimeFormatter.ofPattern("LLLL yyyy", Locale("pl", "PL"))
                            ).replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 8.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    items(
                        items = transactions,
                        key = { it.transaction.id }
                    ) { transactionWithTags ->
                        SwipeToDeleteItem(
                            onDelete = { transactionToDelete = transactionWithTags.transaction }
                        ) {
                            TransactionItem(
                                transaction = transactionWithTags.transaction,
                                tags = transactionWithTags.tags,
                                categories = categories,
                                dateFormat = dateFormat,
                                onClick = {
                                    val tagId = transactionWithTags.tags.firstOrNull()?.id
                                    onEdit(transactionWithTags.transaction, tagId)
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    transactionToDelete?.let { transaction ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Usuń transakcję") },
            text = { Text("Czy na pewno chcesz usunąć tę transakcję?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(transaction)
                        transactionToDelete = null
                    }
                ) {
                    Text("Usuń", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Anuluj")
                }
            }
        )
    }

    if (showFilterDialog) {
        FilterDialog(
            filterState = filterState,
            categories = categories,
            tagDao = tagDao,
            dateFormat = dateFormat,
            onDismiss = { showFilterDialog = false },
            onApply = { newFilterState ->
                filterState = newFilterState
            }
        )
    }
}

/**
 * Applies filters to transaction list.
 */
fun applyFilters(
    transactions: List<TransactionWithTags>,
    filterState: FilterState
): List<TransactionWithTags> {
    return transactions.filter { transactionWithTags ->
        val transaction = transactionWithTags.transaction

        val typeMatch = when (filterState.transactionType) {
            TransactionTypeFilter.ALL -> true
            TransactionTypeFilter.EXPENSE -> transaction.type == TransactionType.EXPENSE
            TransactionTypeFilter.INCOME -> transaction.type == TransactionType.INCOME
        }
        if (!typeMatch) return@filter false

        if (filterState.categoryIds.isNotEmpty()) {
            val hasCategory = transactionWithTags.tags.any { tag ->
                filterState.categoryIds.contains(tag.categoryId)
            }
            if (!hasCategory) return@filter false
        }

        if (filterState.tagIds.isNotEmpty()) {
            val hasTag = transactionWithTags.tags.any { tag ->
                filterState.tagIds.contains(tag.id)
            }
            if (!hasTag) return@filter false
        }

        val dateMatch = when (val range = filterState.dateRange) {
            is DateRangeFilter.AllTime -> true
            is DateRangeFilter.ThisMonth -> {
                YearMonth.from(transaction.date) == YearMonth.now()
            }
            is DateRangeFilter.LastMonth -> {
                YearMonth.from(transaction.date) == YearMonth.now().minusMonths(1)
            }
            is DateRangeFilter.Last3Months -> {
                val threeMonthsAgo = LocalDate.now().minusMonths(3)
                !transaction.date.isBefore(threeMonthsAgo)
            }
            is DateRangeFilter.Custom -> {
                !transaction.date.isBefore(range.startDate) &&
                        !transaction.date.isAfter(range.endDate)
            }
        }
        if (!dateMatch) return@filter false

        true
    }
}

/**
 * Individual transaction item card showing amount, tags with category path, date, and comment.
 */
@Composable
fun TransactionItem(
    transaction: Transaction,
    tags: List<Tag>,
    categories: List<Category>,
    dateFormat: DateFormatOption,
    onClick: () -> Unit
) {
    val amountText = if (transaction.type == TransactionType.INCOME) {
        "+${transaction.amountInCents / 100.0} PLN"
    } else {
        "-${transaction.amountInCents / 100.0} PLN"
    }

    val color = if (transaction.type == TransactionType.INCOME) {
        Green
    } else {
        Red
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = amountText,
                style = MaterialTheme.typography.headlineMedium,
                color = color
            )

            if (tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    tags.forEach { tag ->
                        val category = categories.find { it.id == tag.categoryId }
                        val label = if (category != null) {
                            "${category.name} > ${tag.name}"
                        } else {
                            tag.name
                        }

                        AssistChip(
                            onClick = onClick,
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            Text(
                text = dateFormat.format(transaction.date),
                style = MaterialTheme.typography.bodyMedium
            )

            transaction.comment?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * Calculates total balance from list of transactions.
 * Income adds to balance, expenses subtract.
 */
fun CalculateBalance(transactions: List<Transaction>): Int {
    var balance: Int = 0
    transactions.forEach { transaction ->
        if (transaction.type == TransactionType.INCOME) {
            balance += transaction.amountInCents
        } else {
            balance -= transaction.amountInCents
        }
    }
    return balance
}

/**
 * Wrapper component enabling swipe-to-delete gesture with limited swipe distance.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteItem(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                false
            } else {
                false
            }
        },
        positionalThreshold = { totalDistance ->
            totalDistance * 0.25f
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Red)
                    .padding(end = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Usuń",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) {
        content()
    }
}

/**
 * Dialog for selecting transaction filters.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    filterState: FilterState,
    categories: List<Category>,
    tagDao: TagDao,
    dateFormat: DateFormatOption,
    onDismiss: () -> Unit,
    onApply: (FilterState) -> Unit
) {
    var tempFilterState by remember { mutableStateOf(filterState) }

    val filteredCategories = remember(tempFilterState.transactionType, categories) {
        when (tempFilterState.transactionType) {
            TransactionTypeFilter.ALL -> categories
            TransactionTypeFilter.EXPENSE -> categories.filter { it.transactionType == TransactionType.EXPENSE }
            TransactionTypeFilter.INCOME -> categories.filter { it.transactionType == TransactionType.INCOME }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtry") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                item {
                    Column {
                        Text(
                            text = "Typ transakcji",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        FilterChip(
                            selected = tempFilterState.transactionType == TransactionTypeFilter.ALL,
                            onClick = {
                                tempFilterState = tempFilterState.copy(
                                    transactionType = TransactionTypeFilter.ALL,
                                    categoryIds = emptySet(),
                                    tagIds = emptySet()
                                )
                            },
                            label = { Text("Wszystkie") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = tempFilterState.transactionType == TransactionTypeFilter.EXPENSE,
                                onClick = {
                                    tempFilterState = tempFilterState.copy(
                                        transactionType = TransactionTypeFilter.EXPENSE,
                                        categoryIds = emptySet(),
                                        tagIds = emptySet()
                                    )
                                },
                                label = { Text("Wydatki") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = tempFilterState.transactionType == TransactionTypeFilter.INCOME,
                                onClick = {
                                    tempFilterState = tempFilterState.copy(
                                        transactionType = TransactionTypeFilter.INCOME,
                                        categoryIds = emptySet(),
                                        tagIds = emptySet()
                                    )
                                },
                                label = { Text("Przychody") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item {
                    Column {
                        Text(
                            text = "Kategorie",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filteredCategories.forEach { category ->
                                FilterChip(
                                    selected = tempFilterState.categoryIds.contains(category.id),
                                    onClick = {
                                        tempFilterState = if (tempFilterState.categoryIds.contains(category.id)) {
                                            tempFilterState.copy(
                                                categoryIds = tempFilterState.categoryIds - category.id
                                            )
                                        } else {
                                            tempFilterState.copy(
                                                categoryIds = tempFilterState.categoryIds + category.id
                                            )
                                        }
                                    },
                                    label = { Text(category.name) }
                                )
                            }
                        }
                    }
                }

                item {
                    Column {
                        Text(
                            text = "Tagi",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val allCategoryTags = filteredCategories
                            .filter {
                                tempFilterState.categoryIds.isEmpty() ||
                                        tempFilterState.categoryIds.contains(it.id)
                            }
                            .map { category ->
                                tagDao.getTagsForCategory(category.id)
                                    .collectAsState(initial = emptyList()).value
                            }
                            .flatten()
                            .distinctBy { it.id }

                        if (allCategoryTags.isEmpty()) {
                            Text(
                                text = if (tempFilterState.categoryIds.isEmpty()) {
                                    "Wybierz kategorię aby zobaczyć tagi"
                                } else {
                                    "Brak tagów w wybranych kategoriach"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                allCategoryTags.forEach { tag ->
                                    FilterChip(
                                        selected = tempFilterState.tagIds.contains(tag.id),
                                        onClick = {
                                            tempFilterState = if (tempFilterState.tagIds.contains(tag.id)) {
                                                tempFilterState.copy(
                                                    tagIds = tempFilterState.tagIds - tag.id
                                                )
                                            } else {
                                                tempFilterState.copy(
                                                    tagIds = tempFilterState.tagIds + tag.id
                                                )
                                            }
                                        },
                                        label = { Text(tag.name) }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Column {
                        Text(
                            text = "Okres",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilterChip(
                                selected = tempFilterState.dateRange is DateRangeFilter.AllTime,
                                onClick = {
                                    tempFilterState = tempFilterState.copy(
                                        dateRange = DateRangeFilter.AllTime
                                    )
                                },
                                label = { Text("Wszystkie czasy") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            FilterChip(
                                selected = tempFilterState.dateRange is DateRangeFilter.ThisMonth,
                                onClick = {
                                    tempFilterState = tempFilterState.copy(
                                        dateRange = DateRangeFilter.ThisMonth
                                    )
                                },
                                label = { Text("Ten miesiąc") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            FilterChip(
                                selected = tempFilterState.dateRange is DateRangeFilter.LastMonth,
                                onClick = {
                                    tempFilterState = tempFilterState.copy(
                                        dateRange = DateRangeFilter.LastMonth
                                    )
                                },
                                label = { Text("Ostatni miesiąc") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            FilterChip(
                                selected = tempFilterState.dateRange is DateRangeFilter.Last3Months,
                                onClick = {
                                    tempFilterState = tempFilterState.copy(
                                        dateRange = DateRangeFilter.Last3Months
                                    )
                                },
                                label = { Text("Ostatnie 3 miesiące") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            var showCustomDateDialog by remember { mutableStateOf(false) }

                            FilterChip(
                                selected = tempFilterState.dateRange is DateRangeFilter.Custom,
                                onClick = { showCustomDateDialog = true },
                                label = {
                                    Text(
                                        if (tempFilterState.dateRange is DateRangeFilter.Custom) {
                                            (tempFilterState.dateRange as DateRangeFilter.Custom).let {
                                                "${dateFormat.format(it.startDate)} - ${dateFormat.format(it.endDate)}"
                                            }
                                        } else {
                                            "Własny zakres..."
                                        }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (showCustomDateDialog) {
                                CustomDateRangeDialog(
                                    onDismiss = { showCustomDateDialog = false },
                                    onConfirm = { startDate, endDate ->
                                        tempFilterState = tempFilterState.copy(
                                            dateRange = DateRangeFilter.Custom(startDate, endDate)
                                        )
                                        showCustomDateDialog = false
                                    },
                                    dateFormat = dateFormat
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onApply(tempFilterState)
                    onDismiss()
                }
            ) {
                Text("Zastosuj")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = {
                        tempFilterState = FilterState()
                        onApply(FilterState())
                        onDismiss()
                    }
                ) {
                    Text("Wyczyść")
                }
                TextButton(onClick = onDismiss) {
                    Text("Anuluj")
                }
            }
        }
    )
}