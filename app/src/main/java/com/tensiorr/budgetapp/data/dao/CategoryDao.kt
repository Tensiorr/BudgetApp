package com.tensiorr.budgetapp.data.dao

import androidx.room.*
import com.tensiorr.budgetapp.data.entity.Category
import com.tensiorr.budgetapp.data.entity.TransactionType
import kotlinx.coroutines.flow.Flow

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

    @Delete
    suspend fun delete(category: Category)
}