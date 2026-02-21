package com.tensiorr.budgetapp.data.dao

import androidx.room.*
import com.tensiorr.budgetapp.data.entity.Category
import com.tensiorr.budgetapp.data.entity.TransactionType
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for categories.
 *
 * Provides operations for managing transaction categories,
 * including queries by type and transaction count tracking.
 */
@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category): Long

    @Query("SELECT * FROM categories WHERE transactionType = :type ORDER BY name ASC")
    fun getCategoriesForType(type: TransactionType): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE name = :name AND transactionType = :type")
    suspend fun getCategoryByNameAndType(name: String, type: TransactionType): Category?

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Update
    suspend fun update(category: Category)

    @Query("""
    SELECT COUNT(*) FROM transactions 
    WHERE id IN (
        SELECT transactionId FROM transaction_tag_cross_ref 
        WHERE tagId IN (
            SELECT id FROM tags WHERE categoryId = :categoryId
        )
    )
    """)
    suspend fun getTransactionCountForCategory(categoryId: Long): Int

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int

    @Delete
    suspend fun delete(category: Category)
}