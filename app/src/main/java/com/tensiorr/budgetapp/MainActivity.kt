package com.tensiorr.budgetapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Text
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Icon
import com.tensiorr.budgetapp.data.database.AppDatabase
import com.tensiorr.budgetapp.data.entity.Transaction
import com.tensiorr.budgetapp.data.entity.TransactionTagCrossRef
import com.tensiorr.budgetapp.ui.AddTransactionScreen
import com.tensiorr.budgetapp.ui.ManageCategoriesScreen
import com.tensiorr.budgetapp.ui.SettingsScreen
import com.tensiorr.budgetapp.ui.SummaryScreen
import com.tensiorr.budgetapp.ui.TransactionListScreen
import com.tensiorr.budgetapp.ui.theme.BudgetAppTheme
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val db = AppDatabase.getDatabase(this)
        setContent {
            BudgetAppTheme {
                BudgetAppNavigation(db = db)
            }
        }
    }
}

@Composable
fun BudgetAppNavigation(db: AppDatabase) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var transactionToEdit by remember { mutableStateOf<Pair<Transaction, Long?>?>(null) }
    var showManageCategories by remember { mutableStateOf(false) }

    val dao = db.transactionDao()
    val tagDao = db.tagDao()
    val categoryDao = db.categoryDao()

    val scope = rememberCoroutineScope()
    val transactionsWithTags = dao.getAllTransactionsWithTags()
        .collectAsState(initial = emptyList())

    val categories = categoryDao.getAllCategories()
        .collectAsState(initial = emptyList())

    fun navigateToTab(tab: Int) {
        transactionToEdit = null
        showManageCategories = false
        selectedTab = tab
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Lista") },
                    label = { Text("Lista") },
                    selected = selectedTab == 0,
                    onClick = { navigateToTab(0) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Zestawienia") },
                    label = { Text("Zestawienia") },
                    selected = selectedTab == 1,
                    onClick = { navigateToTab(1) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Add, contentDescription = "Dodaj") },
                    label = { Text("Dodaj") },
                    selected = selectedTab == 2,
                    onClick = { navigateToTab(2) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Ustawienia") },
                    label = { Text("Ustawienia") },
                    selected = selectedTab == 3,
                    onClick = { navigateToTab(3) }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when {
                showManageCategories -> {
                    BackHandler {
                        showManageCategories = false
                        selectedTab = 3
                    }
                    ManageCategoriesScreen(
                        categoryDao = categoryDao,
                        tagDao = tagDao,
                        transactionDao = dao,
                        categories = categories.value,
                        onNavigateBack = {
                            showManageCategories = false
                        }
                    )
                }
                transactionToEdit != null -> {
                    BackHandler {
                        transactionToEdit = null
                        selectedTab = 0
                    }
                    val scope = rememberCoroutineScope()
                    AddTransactionScreen(
                        categoryDao = categoryDao,
                        tagDao = tagDao,
                        transactionToEdit = transactionToEdit!!.first,
                        existingTagId = transactionToEdit!!.second,
                        onSave = { transaction, tagId ->
                            scope.launch {
                                dao.update(transaction)

                                tagDao.deleteTransactionTagCrossRefsForTransaction(transaction.id)

                                tagId?.let { id ->
                                    tagDao.insertTransactionTagCrossRef(
                                        TransactionTagCrossRef(transaction.id, id)
                                    )
                                }

                                transactionToEdit = null
                                selectedTab = 0
                            }
                        }
                    )
                }
                selectedTab == 0 -> {
                    TransactionListScreen(
                        transactionsWithTags = transactionsWithTags.value,
                        categories = categories.value,
                        onDelete = { transaction ->
                            scope.launch {
                                dao.delete(transaction)
                            }
                        },
                        onEdit = { transaction, tagId ->
                            transactionToEdit = Pair(transaction, tagId)
                        }
                    )
                }
                selectedTab == 1 -> {
                    BackHandler {
                        selectedTab = 0
                    }
                    SummaryScreen(
                        transactionDao = dao,
                        categoryDao = categoryDao,
                        transactions = transactionsWithTags.value,
                        categories = categories.value
                    )
                }
                selectedTab == 2 -> {
                    BackHandler {
                        selectedTab = 0
                    }
                    val scope = rememberCoroutineScope()
                    AddTransactionScreen(
                        categoryDao = categoryDao,
                        tagDao = tagDao,
                        onSave = { transaction, tagId ->
                            scope.launch {
                                val transactionId = dao.insert(transaction)
                                tagId?.let { id ->
                                    tagDao.insertTransactionTagCrossRef(
                                        TransactionTagCrossRef(transactionId, id)
                                    )
                                }
                                selectedTab = 0
                            }
                        }
                    )
                }
                selectedTab == 3 -> {
                    BackHandler {
                        selectedTab = 0
                    }
                    SettingsScreen(
                        onNavigateToCategories = {
                            showManageCategories = true
                        }
                    )
                }
            }
        }
    }
}