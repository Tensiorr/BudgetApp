package com.tensiorr.budgetapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Text
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Icon
import com.tensiorr.budgetapp.data.dao.TagDao
import com.tensiorr.budgetapp.data.dao.TransactionDao
import com.tensiorr.budgetapp.data.database.AppDatabase
import com.tensiorr.budgetapp.data.entity.Tag
import com.tensiorr.budgetapp.data.entity.TransactionTagCrossRef
import com.tensiorr.budgetapp.ui.AddTransactionScreen
import com.tensiorr.budgetapp.ui.TransactionListScreen
import com.tensiorr.budgetapp.ui.theme.BudgetAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val db = AppDatabase.getDatabase(this)
        val dao = db.transactionDao()
        val tagDao = db.tagDao()
        setContent {
            BudgetAppTheme {
                BudgetAppNavigation(dao = dao, tagDao = tagDao)
            }
        }
    }
}


@Composable
fun BudgetAppNavigation(dao: TransactionDao, tagDao: TagDao) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Lista") },
                    label = { Text("Lista") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Add, contentDescription = "Dodaj") },
                    label = { Text("Dodaj") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> {
                    val transactionsWithTags = dao.getAllTransactionsWithTags()
                        .collectAsState(initial = emptyList())
                    TransactionListScreen(transactionsWithTags = transactionsWithTags.value)
                }
                1 -> {
                    val scope = rememberCoroutineScope()
                    AddTransactionScreen(onSave = { transaction, tagNames ->
                        scope.launch {
                            val transactionId = dao.insert(transaction)

                            tagNames.forEach { tagName ->
                                var tag = tagDao.getTagByNameAndType(tagName, transaction.type)

                                if (tag == null) {
                                    val tagId = tagDao.insertTag(
                                        Tag(name = tagName, transactionType = transaction.type)
                                    )
                                    tag = Tag(id = tagId, name = tagName, transactionType = transaction.type)
                                }

                                tagDao.insertTransactionTagCrossRef(
                                    TransactionTagCrossRef(transactionId, tag.id)
                                )
                            }
                            selectedTab = 0
                        }
                    })
                }
            }
        }
    }
}