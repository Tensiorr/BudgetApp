package com.tensiorr.budgetapp.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.tensiorr.budgetapp.data.entity.*
import kotlinx.coroutines.tasks.await

/**
 * Repository for Firestore operations.
 *
 * Handles:
 * - Uploading local data to Firestore
 * - Downloading data from Firestore
 * - Conflict resolution (last-write-wins based on updatedAt timestamp)
 */
class FirestoreRepository {

    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "FirestoreRepository"
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_TRANSACTIONS = "transactions"
        private const val COLLECTION_CATEGORIES = "categories"
        private const val COLLECTION_TAGS = "tags"
        private const val COLLECTION_SAVINGS_GOALS = "savings_goals"
        private const val COLLECTION_TRANSACTION_TAG_REFS = "transaction_tag_refs"
        private const val COLLECTION_METADATA = "metadata"
        private const val DOC_SYNC_INFO = "sync_info"
    }

    /**
     * Gets reference to user's collection.
     */
    private fun getUserCollection(userId: String, collection: String) =
        firestore.collection(COLLECTION_USERS)
            .document(userId)
            .collection(collection)

    /**
     * Upload transactions to Firestore.
     */
    suspend fun uploadTransactions(userId: String, transactions: List<Transaction>): Result<Unit> {
        return try {
            val batch = firestore.batch()
            val collection = getUserCollection(userId, COLLECTION_TRANSACTIONS)

            transactions.forEach { transaction ->
                val docRef = collection.document(transaction.id.toString())
                batch.set(docRef, transaction.toFirestoreMap(), SetOptions.merge())
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload transactions", e)
            Result.failure(e)
        }
    }

    /**
     * Download all transactions from Firestore.
     */
    suspend fun downloadTransactions(userId: String): Result<List<Transaction>> {
        return try {
            val snapshot = getUserCollection(userId, COLLECTION_TRANSACTIONS).get().await()
            val transactions = snapshot.documents.mapNotNull { doc ->
                doc.toTransaction()
            }
            Result.success(transactions)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download transactions", e)
            Result.failure(e)
        }
    }

    /**
     * Upload categories to Firestore.
     */
    suspend fun uploadCategories(userId: String, categories: List<Category>): Result<Unit> {
        return try {
            val batch = firestore.batch()
            val collection = getUserCollection(userId, COLLECTION_CATEGORIES)

            categories.forEach { category ->
                val docRef = collection.document(category.id.toString())
                batch.set(docRef, category.toFirestoreMap(), SetOptions.merge())
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload categories", e)
            Result.failure(e)
        }
    }

    /**
     * Download all categories from Firestore.
     */
    suspend fun downloadCategories(userId: String): Result<List<Category>> {
        return try {
            val snapshot = getUserCollection(userId, COLLECTION_CATEGORIES).get().await()
            val categories = snapshot.documents.mapNotNull { doc ->
                doc.toCategory()
            }
            Result.success(categories)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download categories", e)
            Result.failure(e)
        }
    }

    /**
     * Upload tags to Firestore.
     */
    suspend fun uploadTags(userId: String, tags: List<Tag>): Result<Unit> {
        return try {
            val batch = firestore.batch()
            val collection = getUserCollection(userId, COLLECTION_TAGS)

            tags.forEach { tag ->
                val docRef = collection.document(tag.id.toString())
                batch.set(docRef, tag.toFirestoreMap(), SetOptions.merge())
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload tags", e)
            Result.failure(e)
        }
    }

    /**
     * Download all tags from Firestore.
     */
    suspend fun downloadTags(userId: String): Result<List<Tag>> {
        return try {
            val snapshot = getUserCollection(userId, COLLECTION_TAGS).get().await()
            val tags = snapshot.documents.mapNotNull { doc ->
                doc.toTag()
            }
            Result.success(tags)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download tags", e)
            Result.failure(e)
        }
    }

    /**
     * Upload savings goals to Firestore.
     */
    suspend fun uploadSavingsGoals(userId: String, goals: List<SavingsGoal>): Result<Unit> {
        return try {
            val batch = firestore.batch()
            val collection = getUserCollection(userId, COLLECTION_SAVINGS_GOALS)

            goals.forEach { goal ->
                val docRef = collection.document(goal.id.toString())
                batch.set(docRef, goal.toFirestoreMap(), SetOptions.merge())
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload savings goals", e)
            Result.failure(e)
        }
    }

    /**
     * Download all savings goals from Firestore.
     */
    suspend fun downloadSavingsGoals(userId: String): Result<List<SavingsGoal>> {
        return try {
            val snapshot = getUserCollection(userId, COLLECTION_SAVINGS_GOALS).get().await()
            val goals = snapshot.documents.mapNotNull { doc ->
                doc.toSavingsGoal()
            }
            Result.success(goals)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download savings goals", e)
            Result.failure(e)
        }
    }

    /**
     * Upload transaction-tag cross references to Firestore.
     */
    suspend fun uploadTransactionTagRefs(
        userId: String,
        refs: List<TransactionTagCrossRef>
    ): Result<Unit> {
        return try {
            val batch = firestore.batch()
            val collection = getUserCollection(userId, COLLECTION_TRANSACTION_TAG_REFS)

            refs.forEach { ref ->
                val docRef = collection.document("${ref.transactionId}_${ref.tagId}")
                batch.set(docRef, ref.toFirestoreMap(), SetOptions.merge())
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload transaction-tag refs", e)
            Result.failure(e)
        }
    }

    /**
     * Download all transaction-tag cross references from Firestore.
     */
    suspend fun downloadTransactionTagRefs(userId: String): Result<List<TransactionTagCrossRef>> {
        return try {
            val snapshot = getUserCollection(userId, COLLECTION_TRANSACTION_TAG_REFS).get().await()
            val refs = snapshot.documents.mapNotNull { doc ->
                doc.toTransactionTagCrossRef()
            }
            Result.success(refs)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download transaction-tag refs", e)
            Result.failure(e)
        }
    }

    /**
     * Update last sync timestamp.
     */
    suspend fun updateLastSyncTimestamp(userId: String): Result<Unit> {
        return try {
            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_METADATA)
                .document(DOC_SYNC_INFO)
                .set(mapOf("lastSyncTimestamp" to System.currentTimeMillis()))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update last sync timestamp", e)
            Result.failure(e)
        }
    }

    /**
     * Get last sync timestamp.
     */
    suspend fun getLastSyncTimestamp(userId: String): Result<Long?> {
        return try {
            val doc = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_METADATA)
                .document(DOC_SYNC_INFO)
                .get()
                .await()

            val timestamp = doc.getLong("lastSyncTimestamp")
            Result.success(timestamp)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get last sync timestamp", e)
            Result.failure(e)
        }
    }

    /**
     * Delete transaction from Firestore.
     */
    suspend fun deleteTransaction(userId: String, transactionId: Long): Result<Unit> {
        return try {
            getUserCollection(userId, COLLECTION_TRANSACTIONS)
                .document(transactionId.toString())
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete transaction", e)
            Result.failure(e)
        }
    }

    /**
     * Delete category from Firestore.
     */
    suspend fun deleteCategory(userId: String, categoryId: Long): Result<Unit> {
        return try {
            getUserCollection(userId, COLLECTION_CATEGORIES)
                .document(categoryId.toString())
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete category", e)
            Result.failure(e)
        }
    }

    /**
     * Delete tag from Firestore.
     */
    suspend fun deleteTag(userId: String, tagId: Long): Result<Unit> {
        return try {
            getUserCollection(userId, COLLECTION_TAGS)
                .document(tagId.toString())
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete tag", e)
            Result.failure(e)
        }
    }

    /**
     * Delete savings goal from Firestore.
     */
    suspend fun deleteSavingsGoal(userId: String, goalId: Long): Result<Unit> {
        return try {
            getUserCollection(userId, COLLECTION_SAVINGS_GOALS)
                .document(goalId.toString())
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete savings goal", e)
            Result.failure(e)
        }
    }
}

/**
 * Convert Transaction to Firestore map.
 */
private fun Transaction.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "amountInCents" to amountInCents,
    "type" to type.name,
    "date" to date.toEpochDay(),
    "comment" to comment,
    "savingsGoalId" to savingsGoalId,
    "createdAt" to createdAt,
    "updatedAt" to updatedAt
)

/**
 * Convert Firestore document to Transaction.
 */
private fun com.google.firebase.firestore.DocumentSnapshot.toTransaction(): Transaction? {
    return try {
        Transaction(
            id = getLong("id") ?: return null,
            amountInCents = getLong("amountInCents")?.toInt() ?: return null,
            type = TransactionType.valueOf(getString("type") ?: return null),
            date = java.time.LocalDate.ofEpochDay(getLong("date") ?: return null),
            comment = getString("comment"),
            savingsGoalId = getLong("savingsGoalId"),
            createdAt = getLong("createdAt") ?: System.currentTimeMillis(),
            updatedAt = getLong("updatedAt") ?: System.currentTimeMillis()
        )
    } catch (e: Exception) {
        Log.e("FirestoreRepository", "Failed to parse transaction from document $id", e)
        null
    }
}

/**
 * Convert Category to Firestore map.
 */
private fun Category.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "name" to name,
    "transactionType" to transactionType.name,
    "createdAt" to createdAt,
    "updatedAt" to updatedAt
)

/**
 * Convert Firestore document to Category.
 */
private fun com.google.firebase.firestore.DocumentSnapshot.toCategory(): Category? {
    return try {
        Category(
            id = getLong("id") ?: return null,
            name = getString("name") ?: return null,
            transactionType = TransactionType.valueOf(getString("transactionType") ?: return null),
            createdAt = getLong("createdAt") ?: System.currentTimeMillis(),
            updatedAt = getLong("updatedAt") ?: System.currentTimeMillis()
        )
    } catch (e: Exception) {
        Log.e("FirestoreRepository", "Failed to parse category from document $id", e)
        null
    }
}

/**
 * Convert Tag to Firestore map.
 */
private fun Tag.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "name" to name,
    "categoryId" to categoryId,
    "transactionType" to transactionType.name,
    "createdAt" to createdAt,
    "updatedAt" to updatedAt
)

/**
 * Convert Firestore document to Tag.
 */
private fun com.google.firebase.firestore.DocumentSnapshot.toTag(): Tag? {
    return try {
        Tag(
            id = getLong("id") ?: return null,
            name = getString("name") ?: return null,
            categoryId = getLong("categoryId") ?: return null,
            transactionType = TransactionType.valueOf(getString("transactionType") ?: return null),
            createdAt = getLong("createdAt") ?: System.currentTimeMillis(),
            updatedAt = getLong("updatedAt") ?: System.currentTimeMillis()
        )
    } catch (e: Exception) {
        Log.e("FirestoreRepository", "Failed to parse tag from document $id", e)
        null
    }
}

/**
 * Convert SavingsGoal to Firestore map.
 */
private fun SavingsGoal.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "name" to name,
    "targetAmount" to targetAmount,
    "currentAmount" to currentAmount,
    "deadline" to deadline?.toEpochDay(),
    "isArchived" to isArchived,
    "createdAt" to createdAt,
    "updatedAt" to updatedAt
)

/**
 * Convert Firestore document to SavingsGoal.
 */
private fun com.google.firebase.firestore.DocumentSnapshot.toSavingsGoal(): SavingsGoal? {
    return try {
        SavingsGoal(
            id = getLong("id") ?: return null,
            name = getString("name") ?: return null,
            targetAmount = getLong("targetAmount")?.toInt() ?: return null,
            currentAmount = getLong("currentAmount")?.toInt() ?: 0,
            deadline = getLong("deadline")?.let { java.time.LocalDate.ofEpochDay(it) },
            isArchived = getBoolean("isArchived") ?: false,
            createdAt = getLong("createdAt") ?: System.currentTimeMillis(),
            updatedAt = getLong("updatedAt") ?: System.currentTimeMillis()
        )
    } catch (e: Exception) {
        Log.e("FirestoreRepository", "Failed to parse savings goal from document $id", e)
        null
    }
}

/**
 * Convert TransactionTagCrossRef to Firestore map.
 */
private fun TransactionTagCrossRef.toFirestoreMap(): Map<String, Any> = mapOf(
    "transactionId" to transactionId,
    "tagId" to tagId
)

/**
 * Convert Firestore document to TransactionTagCrossRef.
 */
private fun com.google.firebase.firestore.DocumentSnapshot.toTransactionTagCrossRef(): TransactionTagCrossRef? {
    return try {
        TransactionTagCrossRef(
            transactionId = getLong("transactionId") ?: return null,
            tagId = getLong("tagId") ?: return null
        )
    } catch (e: Exception) {
        Log.e("FirestoreRepository", "Failed to parse transaction-tag ref from document $id", e)
        null
    }
}
