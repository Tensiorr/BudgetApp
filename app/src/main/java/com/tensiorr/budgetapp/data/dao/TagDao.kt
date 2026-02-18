package com.tensiorr.budgetapp.data.dao

import androidx.room.*
import com.tensiorr.budgetapp.data.entity.Tag
import com.tensiorr.budgetapp.data.entity.TransactionTagCrossRef
import com.tensiorr.budgetapp.data.entity.TransactionType
import kotlinx.coroutines.flow.Flow

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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransactionTagCrossRef(crossRef: TransactionTagCrossRef)

    @Query("SELECT * FROM tags WHERE id IN (SELECT tagId FROM transaction_tag_cross_ref WHERE transactionId = :transactionId)")
    suspend fun getTagsForTransaction(transactionId: Long): List<Tag>

    @Query("DELETE FROM transaction_tag_cross_ref WHERE transactionId = :transactionId")
    suspend fun deleteTransactionTagCrossRefsForTransaction(transactionId: Long)
}