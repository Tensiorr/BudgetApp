package com.tensiorr.budgetapp.data.repository

import android.content.Context
import android.util.Log
import com.tensiorr.budgetapp.data.database.AppDatabase
import com.tensiorr.budgetapp.data.entity.*
import com.tensiorr.budgetapp.data.preferences.UserPreferences
import com.tensiorr.budgetapp.util.SyncState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SyncRepository(context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val firestoreRepo = FirestoreRepository()
    private val preferences = UserPreferences(context)

    private val transactionDao = database.transactionDao()
    private val categoryDao = database.categoryDao()
    private val tagDao = database.tagDao()
    private val savingsGoalDao = database.savingsGoalDao()

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    companion object {
        private const val TAG = "SyncRepository"
    }

    suspend fun performFullSync(userId: String): Result<Unit> {
        return try {
            _syncState.value = SyncState.Syncing(0f, "Rozpoczynanie synchronizacji...")
            _syncState.value = SyncState.Syncing(0.1f, "Wysyłanie kategorii...")
            uploadLocalData(userId)

            _syncState.value = SyncState.Syncing(0.5f, "Pobieranie danych z chmury...")
            downloadCloudData(userId)

            _syncState.value = SyncState.Syncing(0.9f, "Finalizowanie...")
            firestoreRepo.updateLastSyncTimestamp(userId)
            preferences.setLastSyncTimestamp(System.currentTimeMillis())

            _syncState.value = SyncState.Success()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Full sync failed", e)
            _syncState.value = SyncState.Error(e.message ?: "Nieznany błąd synchronizacji")
            Result.failure(e)
        }
    }

    private suspend fun uploadLocalData(userId: String) {
        val localCategories = categoryDao.getAllCategoriesOnce()
        firestoreRepo.uploadCategories(userId, localCategories).getOrThrow()

        val localTags = tagDao.getAllTagsOnce()
        firestoreRepo.uploadTags(userId, localTags).getOrThrow()

        val localGoals = savingsGoalDao.getAllGoalsOnce()
        firestoreRepo.uploadSavingsGoals(userId, localGoals).getOrThrow()

        val localTransactions = transactionDao.getAllTransactionsOnce()
        firestoreRepo.uploadTransactions(userId, localTransactions).getOrThrow()

        val localRefs = tagDao.getAllTransactionTagCrossRefsOnce()
        firestoreRepo.uploadTransactionTagRefs(userId, localRefs).getOrThrow()
    }

    private suspend fun downloadCloudData(userId: String) {
        _syncState.value = SyncState.Syncing(0.5f, "Synchronizacja kategorii...")
        val cloudCategories = firestoreRepo.downloadCategories(userId).getOrThrow()
        mergeCategories(cloudCategories)

        _syncState.value = SyncState.Syncing(0.6f, "Synchronizacja tagów...")
        val cloudTags = firestoreRepo.downloadTags(userId).getOrThrow()
        mergeTags(cloudTags)

        _syncState.value = SyncState.Syncing(0.7f, "Synchronizacja celów oszczędnościowych...")
        val cloudGoals = firestoreRepo.downloadSavingsGoals(userId).getOrThrow()
        mergeSavingsGoals(cloudGoals)

        _syncState.value = SyncState.Syncing(0.8f, "Synchronizacja transakcji...")
        val cloudTransactions = firestoreRepo.downloadTransactions(userId).getOrThrow()
        mergeTransactions(cloudTransactions)

        _syncState.value = SyncState.Syncing(0.85f, "Synchronizacja powiązań...")
        val cloudRefs = firestoreRepo.downloadTransactionTagRefs(userId).getOrThrow()
        mergeTransactionTagRefs(cloudRefs)
    }

    private suspend fun mergeCategories(cloudCategories: List<Category>) {
        val localCategories = categoryDao.getAllCategoriesOnce()
        val localMap = localCategories.associateBy { it.id }

        val toInsert = mutableListOf<Category>()
        val toUpdate = mutableListOf<Category>()

        cloudCategories.forEach { cloudCategory ->
            val localCategory = localMap[cloudCategory.id]

            if (localCategory == null) {
                toInsert.add(cloudCategory)
            } else {
                if (cloudCategory.updatedAt > localCategory.updatedAt) {
                    toUpdate.add(cloudCategory)
                }
            }
        }

        toInsert.forEach { categoryDao.insert(it) }
        toUpdate.forEach { categoryDao.update(it) }
    }

    private suspend fun mergeTags(cloudTags: List<Tag>) {
        val localTags = tagDao.getAllTagsOnce()
        val localMap = localTags.associateBy { it.id }

        val toInsert = mutableListOf<Tag>()
        val toUpdate = mutableListOf<Tag>()

        cloudTags.forEach { cloudTag ->
            val localTag = localMap[cloudTag.id]

            if (localTag == null) {
                toInsert.add(cloudTag)
            } else {
                if (cloudTag.updatedAt > localTag.updatedAt) {
                    toUpdate.add(cloudTag)
                }
            }
        }

        toInsert.forEach { tagDao.insertTag(it) }
        toUpdate.forEach { tagDao.updateTag(it) }
    }

    private suspend fun mergeSavingsGoals(cloudGoals: List<SavingsGoal>) {
        val localGoals = savingsGoalDao.getAllGoalsOnce()
        val localMap = localGoals.associateBy { it.id }

        val toInsert = mutableListOf<SavingsGoal>()
        val toUpdate = mutableListOf<SavingsGoal>()

        cloudGoals.forEach { cloudGoal ->
            val localGoal = localMap[cloudGoal.id]

            if (localGoal == null) {
                toInsert.add(cloudGoal)
            } else {
                if (cloudGoal.updatedAt > localGoal.updatedAt) {
                    toUpdate.add(cloudGoal)
                }
            }
        }

        toInsert.forEach { savingsGoalDao.insert(it) }
        toUpdate.forEach { savingsGoalDao.update(it) }
    }

    private suspend fun mergeTransactions(cloudTransactions: List<Transaction>) {
        val localTransactions = transactionDao.getAllTransactionsOnce()
        val localMap = localTransactions.associateBy { it.id }

        val toInsert = mutableListOf<Transaction>()
        val toUpdate = mutableListOf<Transaction>()

        cloudTransactions.forEach { cloudTransaction ->
            val localTransaction = localMap[cloudTransaction.id]

            if (localTransaction == null) {
                toInsert.add(cloudTransaction)
            } else {
                if (cloudTransaction.updatedAt > localTransaction.updatedAt) {
                    toUpdate.add(cloudTransaction)
                }
            }
        }

        toInsert.forEach { transactionDao.insert(it) }
        toUpdate.forEach { transactionDao.update(it) }
    }

    private suspend fun mergeTransactionTagRefs(cloudRefs: List<TransactionTagCrossRef>) {
        val localRefs = tagDao.getAllTransactionTagCrossRefsOnce()
        val localSet = localRefs.map { "${it.transactionId}_${it.tagId}" }.toSet()

        val toInsert = cloudRefs.filter { ref ->
            "${ref.transactionId}_${ref.tagId}" !in localSet
        }

        toInsert.forEach { tagDao.insertTransactionTagCrossRef(it) }
    }

    suspend fun performQuickSync(userId: String, lastSyncTimestamp: Long): Result<Unit> {
        return try {
            _syncState.value = SyncState.Syncing(0f, "Synchronizacja zmian...")

            val changedCategories = categoryDao.getChangedSince(lastSyncTimestamp)
            val changedTags = tagDao.getChangedSince(lastSyncTimestamp)
            val changedGoals = savingsGoalDao.getChangedSince(lastSyncTimestamp)
            val changedTransactions = transactionDao.getChangedSince(lastSyncTimestamp)

            if (changedCategories.isNotEmpty()) {
                firestoreRepo.uploadCategories(userId, changedCategories)
            }
            if (changedTags.isNotEmpty()) {
                firestoreRepo.uploadTags(userId, changedTags)
            }
            if (changedGoals.isNotEmpty()) {
                firestoreRepo.uploadSavingsGoals(userId, changedGoals)
            }
            if (changedTransactions.isNotEmpty()) {
                firestoreRepo.uploadTransactions(userId, changedTransactions)
            }

            firestoreRepo.updateLastSyncTimestamp(userId)
            preferences.setLastSyncTimestamp(System.currentTimeMillis())

            _syncState.value = SyncState.Success()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Quick sync failed", e)
            _syncState.value = SyncState.Error(e.message ?: "Nieznany błąd synchronizacji")
            Result.failure(e)
        }
    }

    fun resetSyncState() {
        _syncState.value = SyncState.Idle
    }

    suspend fun deleteTransaction(userId: String, transaction: Transaction): Result<Unit> {
        return try {
            firestoreRepo.deleteTransaction(userId, transaction.id).getOrThrow()
            database.transactionDao().delete(transaction)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete transaction", e)
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(userId: String, category: Category): Result<Unit> {
        return try {
            val tags = database.tagDao().getTagsForCategoryOnce(category.id)

            tags.forEach { tag ->
                firestoreRepo.deleteTag(userId, tag.id).getOrThrow()
            }

            firestoreRepo.deleteCategory(userId, category.id).getOrThrow()
            database.categoryDao().deleteCategoryWithTags(category)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete category", e)
            Result.failure(e)
        }
    }

    suspend fun deleteTag(userId: String, tag: Tag): Result<Unit> {
        return try {
            firestoreRepo.deleteTag(userId, tag.id).getOrThrow()
            database.tagDao().deleteTag(tag)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete tag", e)
            Result.failure(e)
        }
    }

    suspend fun deleteSavingsGoal(userId: String, goal: SavingsGoal): Result<Unit> {
        return try {
            firestoreRepo.deleteSavingsGoal(userId, goal.id).getOrThrow()
            database.savingsGoalDao().delete(goal)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete savings goal", e)
            Result.failure(e)
        }
    }
}