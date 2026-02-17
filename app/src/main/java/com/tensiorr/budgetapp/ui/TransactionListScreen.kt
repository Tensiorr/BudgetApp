package com.tensiorr.budgetapp.ui

import androidx.compose.foundation.background
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

/**
 * Screen displaying list of transactions with balance summary.
 *
 * Features:
 * - Balance card at the top showing total income minus expenses
 * - Scrollable list of transactions with swipe-to-delete functionality
 * - Each transaction shows amount, category/tag path, date, and optional comment
 * - Confirmation dialog before deleting
 */
@Composable
fun TransactionListScreen(
    transactionsWithTags: List<TransactionWithTags>,
    categories: List<Category>,
    onDelete: (Transaction) -> Unit
) {
    val balance = CalculateBalance(transactionsWithTags.map { it.transaction })
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Saldo: ${balance / 100.0} PLN",
                style = MaterialTheme.typography.headlineMedium,
                color = if (balance > 0) Green else if (balance < 0) Red else Black,
                modifier = Modifier.padding(16.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(
                items = transactionsWithTags,
                key = { it.transaction.id }
            ) { transactionWithTags ->
                SwipeToDeleteItem(
                    onDelete = { transactionToDelete = transactionWithTags.transaction }
                ) {
                    TransactionItem(
                        transaction = transactionWithTags.transaction,
                        tags = transactionWithTags.tags,
                        categories = categories
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
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
}

/**
 * Individual transaction item card showing amount, tags with category path, date, and comment.
 */
@Composable
fun TransactionItem(
    transaction: Transaction,
    tags: List<Tag>,
    categories: List<Category>
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
        modifier = Modifier.fillMaxWidth()
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
                            onClick = { },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            Text(
                text = transaction.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
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