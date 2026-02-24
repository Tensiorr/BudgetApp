package com.tensiorr.budgetapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Savings goal entity representing a piggy bank.
 *
 * @property id Unique identifier
 * @property name Goal name (e.g., "Holiday", "New notebook")
 * @property targetAmount Target amount in cents
 * @property currentAmount Current saved amount in cents
 * @property deadline Optional deadline date
 * @property isArchived Whether goal is archived
 * @property createdAt Date when goal was created
 */
@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val targetAmount: Int,
    val currentAmount: Int = 0,
    val deadline: LocalDate? = null,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)