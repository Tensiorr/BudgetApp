package com.tensiorr.budgetapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tensiorr.budgetapp.data.entity.Tag
import com.tensiorr.budgetapp.data.entity.Transaction
import com.tensiorr.budgetapp.data.entity.TransactionType
import com.tensiorr.budgetapp.data.entity.TransactionWithTags
import com.tensiorr.budgetapp.ui.theme.Black
import com.tensiorr.budgetapp.ui.theme.Green
import com.tensiorr.budgetapp.ui.theme.Red
import java.time.format.DateTimeFormatter

@Composable
fun TransactionListScreen(transactionsWithTags: List<TransactionWithTags>) {
    val balance = CalculateBalance(transactionsWithTags.map { it.transaction })
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
            items(transactionsWithTags) { transactionWithTags ->
                TransactionItem(
                    transaction = transactionWithTags.transaction,
                    tags = transactionWithTags.tags
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction, tags: List<Tag>) {
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
                        AssistChip(
                            onClick = { },
                            label = { Text(tag.name, style = MaterialTheme.typography.labelSmall) }
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

fun CalculateBalance(transactions: List<Transaction>): Int {
    var balance: Int = 0
    transactions.forEach { transaction ->
        if (transaction.type == TransactionType.INCOME) {
            balance += transaction.amountInCents
        }
        else {
            balance -= transaction.amountInCents
        }
    }
    return balance
}