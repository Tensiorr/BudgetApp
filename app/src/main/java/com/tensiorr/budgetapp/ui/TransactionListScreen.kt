package com.tensiorr.budgetapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tensiorr.budgetapp.data.entity.Transaction
import com.tensiorr.budgetapp.data.entity.TransactionType
import java.time.format.DateTimeFormatter

@Composable
fun TransactionListScreen(transactions: List<Transaction>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(transactions) { transaction ->
            TransactionItem(transaction)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    val amountText = if (transaction.type == TransactionType.INCOME) {
        "+${transaction.amountInCents / 100.0} PLN"
    } else {
        "-${transaction.amountInCents / 100.0} PLN"
    }

    val color = if (transaction.type == TransactionType.INCOME) {
        Color.Green
    } else {
        Color.Red
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