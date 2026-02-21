package com.tensiorr.budgetapp.data.dao

import androidx.room.*
import com.tensiorr.budgetapp.data.entity.Tag
import com.tensiorr.budgetapp.data.entity.TransactionTagCrossRef
import com.tensiorr.budgetapp.data.entity.TransactionType
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for tags.
 *
 * Manages tags, their relationships with categories and transactions,
 * including many-to-many cross-reference operations.
 */
@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: Tag): Long

    @Query("SELECT * FROM tags WHERE name = :name AND transactionType = :type")
    suspend fun getTagByNameAndType(name: String, type: TransactionType): Tag?

    @Query("SELECT * FROM tags WHERE name = :name AND categoryId = :categoryId")
    suspend fun getTagByNameAndCategory(name: String, categoryId: Long): Tag?

    @Query("SELECT * FROM tags WHERE categoryId = :categoryId ORDER BY name ASC")
    fun getTagsForCategory(categoryId: Long): Flow<List<Tag>>

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<Tag>>

    @Query("SELECT * FROM tags WHERE id = :tagId LIMIT 1")
    suspend fun getTagById(tagId: Long): Tag?

    @Query("SELECT * FROM tags WHERE id IN (SELECT tagId FROM transaction_tag_cross_ref WHERE transactionId = :transactionId)")
    suspend fun getTagsForTransaction(transactionId: Long): List<Tag>

    @Query("""
    SELECT COUNT(*) FROM transactions 
    WHERE id IN (
        SELECT transactionId FROM transaction_tag_cross_ref 
        WHERE tagId = :tagId
    )
    """)
    suspend fun getTransactionCountForTag(tagId: Long): Int

    @Query("SELECT COUNT(*) FROM tags")
    suspend fun getTagCount(): Int

    @Query("DELETE FROM transaction_tag_cross_ref WHERE transactionId = :transactionId")
    suspend fun deleteTransactionTagCrossRefsForTransaction(transactionId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransactionTagCrossRef(crossRef: TransactionTagCrossRef)

    @Update
    suspend fun updateTag(tag: Tag)

    @Delete
    suspend fun deleteTag(tag: Tag)
}