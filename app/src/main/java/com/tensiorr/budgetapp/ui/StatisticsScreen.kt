package com.tensiorr.budgetapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tensiorr.budgetapp.data.dao.CategoryDao
import com.tensiorr.budgetapp.data.dao.TagDao
import com.tensiorr.budgetapp.data.dao.TransactionDao
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

/**
 * Screen displaying app statistics.
 *
 * Shows:
 * - Total number of transactions
 * - Total number of categories
 * - Total number of tags
 * - Date of first transaction
 */
@Composable
fun StatisticsScreen(
    transactionDao: TransactionDao,
    categoryDao: CategoryDao,
    tagDao: TagDao,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var transactionCount by remember { mutableIntStateOf(0) }
    var categoryCount by remember { mutableIntStateOf(0) }
    var tagCount by remember { mutableIntStateOf(0) }
    var firstTransactionDate by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            transactionCount = transactionDao.getTransactionCount()
            categoryCount = categoryDao.getCategoryCount()
            tagCount = tagDao.getTagCount()

            val firstDate = transactionDao.getFirstTransactionDate()
            firstTransactionDate = firstDate?.format(
                DateTimeFormatter.ofPattern("dd.MM.yyyy")
            )

            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Wstecz")
            }
            Text(
                text = "Statystyki",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatisticItem(
                        label = "Transakcje",
                        value = transactionCount.toString()
                    )

                    HorizontalDivider()

                    StatisticItem(
                        label = "Kategorie",
                        value = categoryCount.toString()
                    )

                    HorizontalDivider()

                    StatisticItem(
                        label = "Tagi",
                        value = tagCount.toString()
                    )

                    if (firstTransactionDate != null) {
                        HorizontalDivider()

                        StatisticItem(
                            label = "Pierwsza transakcja",
                            value = firstTransactionDate!!
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual statistic item with label and value.
 */
@Composable
fun StatisticItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}