package com.tensiorr.budgetapp.data.dao

import androidx.room.*
import com.tensiorr.budgetapp.data.entity.Tag
import com.tensiorr.budgetapp.data.entity.TransactionTagCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: Tag): Long

    @Query("SELECT * FROM tags WHERE name = :name")
    suspend fun getTagByName(name: String): Tag?

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<Tag>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransactionTagCrossRef(crossRef: TransactionTagCrossRef)

    @Query("SELECT * FROM tags WHERE id IN (SELECT tagId FROM transaction_tag_cross_ref WHERE transactionId = :transactionId)")
    suspend fun getTagsForTransaction(transactionId: Long): List<Tag>
}