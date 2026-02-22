package com.tensiorr.budgetapp

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tensiorr.budgetapp.data.database.AppDatabase
import com.tensiorr.budgetapp.data.entity.Transaction
import com.tensiorr.budgetapp.data.entity.TransactionTagCrossRef
import com.tensiorr.budgetapp.data.entity.TransactionType
import com.tensiorr.budgetapp.data.preferences.UserPreferences
import com.tensiorr.budgetapp.ui.dialogs.UpdateDialog
import com.tensiorr.budgetapp.ui.models.DateFormatOption
import com.tensiorr.budgetapp.ui.screens.*
import com.tensiorr.budgetapp.ui.theme.BudgetAppTheme
import com.tensiorr.budgetapp.ui.theme.ThemeMode
import com.tensiorr.budgetapp.ui.viewmodel.UpdateUiState
import com.tensiorr.budgetapp.ui.viewmodel.UpdateViewModel
import com.tensiorr.budgetapp.util.ApkInstaller
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

/**
 * Main activity for the Budget App.
 *
 * Manages:
 * - Splash screen during initialization
 * - Theme configuration
 * - Navigation between screens
 * - App update flow
 */
class MainActivity : ComponentActivity() {

    private var pendingApkInstall: File? = null

    private val installSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        pendingApkInstall?.let { apkFile ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (packageManager.canRequestPackageInstalls()) {
                    ApkInstaller.installApk(this, apkFile)
                    pendingApkInstall = null
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(this)
        val preferences = UserPreferences(this)

        var isReady = false

        splashScreen.setKeepOnScreenCondition { !isReady }

        lifecycleScope.launch {
            preferences.themeModeFlow.first()
            db.transactionDao().getAllTransactionsWithTags().first()
            db.categoryDao().getAllCategories().first()
            isReady = true
        }

        setContent {
            val themeMode by preferences.themeModeEnumFlow
                .collectAsState(initial = ThemeMode.SYSTEM)

            val updateViewModel: UpdateViewModel = viewModel()
            val updateUiState by updateViewModel.uiState.collectAsState()

            LaunchedEffect(updateUiState) {
                if (updateUiState is UpdateUiState.NeedsInstallPermission) {
                    pendingApkInstall = (updateUiState as UpdateUiState.NeedsInstallPermission).apkFile
                }
            }

            BudgetAppTheme(themeMode = themeMode) {
                BudgetAppNavigation(
                    db = db,
                    preferences = preferences,
                    updateViewModel = updateViewModel
                )

                UpdateDialog(
                    uiState = updateUiState,
                    onDownload = {
                        if (updateUiState is UpdateUiState.UpdateAvailable) {
                            updateViewModel.downloadAndInstall(
                                (updateUiState as UpdateUiState.UpdateAvailable).update
                            )
                        }
                    },
                    onInstall = {
                        if (updateUiState is UpdateUiState.ReadyToInstall) {
                            updateViewModel.installApk(
                                (updateUiState as UpdateUiState.ReadyToInstall).apkFile
                            )
                        }
                    },
                    onDismiss = {
                        when (updateUiState) {
                            is UpdateUiState.UpdateAvailable -> {
                                updateViewModel.dismissUpdate(
                                    (updateUiState as UpdateUiState.UpdateAvailable).update
                                )
                            }
                            else -> updateViewModel.resetState()
                        }
                    },
                    onRequestInstallPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                                data = Uri.parse("package:$packageName")
                            }
                            installSettingsLauncher.launch(intent)
                        }
                    }
                )
            }

            LaunchedEffect(Unit) {
                updateViewModel.checkForUpdateSilently()
            }
        }
    }
}

/**
 * Main navigation composable managing tab navigation and sub-screens.
 */
@Composable
fun BudgetAppNavigation(
    db: AppDatabase,
    preferences: UserPreferences,
    updateViewModel: UpdateViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var transactionToEdit by remember { mutableStateOf<Pair<Transaction, Long?>?>(null) }
    var showManageCategories by remember { mutableStateOf(false) }
    var showThemeSelection by remember { mutableStateOf(false) }
    var showStatistics by remember { mutableStateOf(false) }
    var showDateFormatSelection by remember { mutableStateOf(false) }
    var showSavings by remember { mutableStateOf(false) }

    val dao = db.transactionDao()
    val tagDao = db.tagDao()
    val categoryDao = db.categoryDao()
    val savingsGoalDao = db.savingsGoalDao()

    val scope = rememberCoroutineScope()

    val transactionsWithTags = dao.getAllTransactionsWithTags()
        .collectAsState(initial = emptyList())
    val categories = categoryDao.getAllCategories()
        .collectAsState(initial = emptyList())
    val currentThemeMode by preferences.themeModeFlow
        .collectAsState(initial = "SYSTEM")
    val currentDateFormatString by preferences.dateFormatFlow
        .collectAsState(initial = "DD.MM.YYYY")
    val dateFormat = DateFormatOption.fromPattern(currentDateFormatString)
    val activeSavingsGoals by savingsGoalDao.getActiveGoals()
        .collectAsState(initial = emptyList())
    val archivedSavingsGoals by savingsGoalDao.getArchivedGoals()
        .collectAsState(initial = emptyList())

    val themeDisplayText = when (currentThemeMode) {
        "LIGHT" -> "Jasny"
        "DARK" -> "Ciemny"
        "SYSTEM" -> "Systemowy"
        else -> "Systemowy"
    }

    fun navigateToTab(tab: Int) {
        transactionToEdit = null
        showManageCategories = false
        showThemeSelection = false
        showStatistics = false
        showDateFormatSelection = false
        showSavings = false
        selectedTab = tab
    }

    fun navigateToSettings() {
        transactionToEdit = null
        showManageCategories = false
        showThemeSelection = false
        showStatistics = false
        showDateFormatSelection = false
        showSavings = false
        selectedTab = 4
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
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Bilans") },
                    label = { Text("Bilans") },
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
                    icon = { Icon(Icons.Default.Savings, contentDescription = "Skarbonki") },
                    label = { Text("Skarbonki") },
                    selected = selectedTab == 3,
                    onClick = { navigateToTab(3) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Ustawienia") },
                    label = { Text("Ustawienia") },
                    selected = selectedTab == 4,
                    onClick = { navigateToTab(4) }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when {
                showDateFormatSelection -> {
                    BackHandler {
                        showDateFormatSelection = false
                        navigateToSettings()
                    }
                    DateFormatSelectionScreen(
                        preferences = preferences,
                        currentDateFormat = currentDateFormatString,
                        onNavigateBack = { showDateFormatSelection = false }
                    )
                }

                showManageCategories -> {
                    BackHandler {
                        showManageCategories = false
                        navigateToSettings()
                    }
                    ManageCategoriesScreen(
                        categoryDao = categoryDao,
                        tagDao = tagDao,
                        transactionDao = dao,
                        categories = categories.value,
                        onNavigateBack = { showManageCategories = false }
                    )
                }

                showThemeSelection -> {
                    BackHandler {
                        showThemeSelection = false
                        navigateToSettings()
                    }
                    ThemeSelectionScreen(
                        preferences = preferences,
                        currentThemeMode = currentThemeMode,
                        onNavigateBack = { showThemeSelection = false }
                    )
                }

                showStatistics -> {
                    BackHandler {
                        showStatistics = false
                        navigateToSettings()
                    }
                    StatisticsScreen(
                        transactionDao = dao,
                        categoryDao = categoryDao,
                        tagDao = tagDao,
                        onNavigateBack = { showStatistics = false }
                    )
                }

                showSavings -> {
                    BackHandler {
                        showSavings = false
                        selectedTab = 0
                    }
                    SavingsScreen(
                        savingsGoalDao = savingsGoalDao,
                        dateFormat = dateFormat,
                        activeGoals = activeSavingsGoals,
                        archivedGoals = archivedSavingsGoals,
                        onNavigateBack = { showSavings = false }
                    )
                }

                transactionToEdit != null -> {
                    BackHandler {
                        transactionToEdit = null
                        selectedTab = 0
                    }
                    AddTransactionScreen(
                        categoryDao = categoryDao,
                        tagDao = tagDao,
                        savingsGoalDao = savingsGoalDao,
                        transactionToEdit = transactionToEdit!!.first,
                        existingTagId = transactionToEdit!!.second,
                        dateFormat = dateFormat,
                        onSave = { transaction, tagId ->
                            scope.launch {
                                val oldTransaction = transactionToEdit!!.first

                                if (oldTransaction.type == TransactionType.SAVING &&
                                    oldTransaction.savingsGoalId != null) {
                                    savingsGoalDao.addToGoal(
                                        oldTransaction.savingsGoalId!!,
                                        -oldTransaction.amountInCents
                                    )
                                }

                                dao.update(transaction)

                                if (transaction.type == TransactionType.SAVING &&
                                    transaction.savingsGoalId != null) {
                                    savingsGoalDao.addToGoal(
                                        transaction.savingsGoalId!!,
                                        transaction.amountInCents
                                    )
                                }

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
                        tagDao = tagDao,
                        savingsGoalDao = savingsGoalDao,
                        dateFormat = dateFormat,
                        onDelete = { transaction ->
                            scope.launch {
                                if (transaction.type == TransactionType.SAVING &&
                                    transaction.savingsGoalId != null) {
                                    savingsGoalDao.addToGoal(
                                        transaction.savingsGoalId!!,
                                        -transaction.amountInCents
                                    )
                                }
                                dao.delete(transaction)
                            }
                        },
                        onEdit = { transaction, tagId ->
                            transactionToEdit = Pair(transaction, tagId)
                        }
                    )
                }

                selectedTab == 1 -> {
                    BackHandler { navigateToTab(0) }
                    SummaryScreen(
                        transactions = transactionsWithTags.value,
                        categories = categories.value,
                        savingsGoalDao = savingsGoalDao,
                        dateFormat = dateFormat
                    )
                }

                selectedTab == 2 -> {
                    BackHandler { navigateToTab(0) }
                    AddTransactionScreen(
                        categoryDao = categoryDao,
                        tagDao = tagDao,
                        savingsGoalDao = savingsGoalDao,
                        dateFormat = dateFormat,
                        onSave = { transaction, tagId ->
                            scope.launch {
                                val transactionId = dao.insert(transaction)

                                if (transaction.type == TransactionType.SAVING &&
                                    transaction.savingsGoalId != null) {
                                    savingsGoalDao.addToGoal(
                                        transaction.savingsGoalId!!,
                                        transaction.amountInCents
                                    )
                                }

                                tagId?.let { id ->
                                    tagDao.insertTransactionTagCrossRef(
                                        TransactionTagCrossRef(transactionId, id)
                                    )
                                }

                                navigateToTab(0)
                            }
                        }
                    )
                }

                selectedTab == 3 -> {
                    BackHandler { navigateToTab(0) }
                    SavingsScreen(
                        savingsGoalDao = savingsGoalDao,
                        dateFormat = dateFormat,
                        activeGoals = activeSavingsGoals,
                        archivedGoals = archivedSavingsGoals
                    )
                }

                selectedTab == 4 -> {
                    BackHandler { navigateToTab(0) }
                    SettingsScreen(
                        onNavigateToCategories = { showManageCategories = true },
                        onNavigateToTheme = { showThemeSelection = true },
                        onNavigateToStatistics = { showStatistics = true },
                        onNavigateToDateFormat = { showDateFormatSelection = true },
                        onCheckForUpdates = { updateViewModel.checkForUpdate() },
                        themeDisplayText = themeDisplayText,
                        dateFormatDisplayText = currentDateFormatString
                    )
                }
            }
        }
    }
}