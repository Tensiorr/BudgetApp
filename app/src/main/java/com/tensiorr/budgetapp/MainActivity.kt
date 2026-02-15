package com.tensiorr.budgetapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.tensiorr.budgetapp.data.database.AppDatabase
import com.tensiorr.budgetapp.ui.TransactionListScreen
import com.tensiorr.budgetapp.ui.theme.BudgetAppTheme
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val db = AppDatabase.getDatabase(this)
        val dao = db.transactionDao()
        setContent {
            BudgetAppTheme {
                val transactions = dao.getAllTransactions().collectAsState(initial = emptyList())

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        TransactionListScreen(transactions = transactions.value)
                    }
                }
            }
        }
    }
}