package com.tensiorr.budgetapp.data.dao

import androidx.room.*
import com.tensiorr.budgetapp.data.entity.Transaction
import com.tensiorr.budgetapp.data.entity.TransactionWithTags
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import androidx.room.Transaction as RoomTransaction

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: Transaction): Long

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @RoomTransaction
    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    suspend fun getTransactionWithTags(transactionId: Long): TransactionWithTags?

    @RoomTransaction
    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    fun getAllTransactionsWithTags(): Flow<List<TransactionWithTags>>

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTransactionCount(): Int

    @Query("SELECT MIN(date) FROM transactions")
    suspend fun getFirstTransactionDate(): LocalDate?

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)
}