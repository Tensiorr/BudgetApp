package com.tensiorr.budgetapp.data.dao

import androidx.room.*
import com.tensiorr.budgetapp.data.entity.SavingsGoal
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for savings goals.
 *
 * Provides CRUD operations and queries for managing savings goals,
 * including active/archived filtering and transaction counting.
 */
@Dao
interface SavingsGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(savingsGoal: SavingsGoal): Long

    @Query("SELECT * FROM savings_goals WHERE id = :id")
    suspend fun getById(id: Long): SavingsGoal?

    @Query("SELECT * FROM savings_goals WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<SavingsGoal?>

    @Query("SELECT * FROM savings_goals WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getActiveGoals(): Flow<List<SavingsGoal>>

    @Query("SELECT * FROM savings_goals WHERE isArchived = 1 ORDER BY createdAt DESC")
    fun getArchivedGoals(): Flow<List<SavingsGoal>>

    @Query("SELECT * FROM savings_goals ORDER BY createdAt DESC")
    fun getAllGoals(): Flow<List<SavingsGoal>>

    @Query("SELECT COUNT(*) FROM savings_goals WHERE isArchived = 0")
    suspend fun getActiveGoalsCount(): Int

    @Query("UPDATE savings_goals SET currentAmount = currentAmount + :amount WHERE id = :goalId")
    suspend fun addToGoal(goalId: Long, amount: Int)

    @Query("UPDATE savings_goals SET isArchived = :archived WHERE id = :goalId")
    suspend fun setArchived(goalId: Long, archived: Boolean)

    @Query("SELECT COUNT(*) FROM transactions WHERE savingsGoalId = :goalId")
    suspend fun getTransactionCount(goalId: Long): Int

    @Update
    suspend fun update(savingsGoal: SavingsGoal)

    @Delete
    suspend fun delete(savingsGoal: SavingsGoal)

    suspend fun canDelete(goalId: Long): Boolean {
        return getTransactionCount(goalId) == 0
    }
}