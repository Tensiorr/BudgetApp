package com.tensiorr.budgetapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Transaction entity representing a financial transaction.
 *
 * @property id Unique identifier
 * @property amountInCents Amount in cents (100 = 1 PLN)
 * @property type Transaction type (EXPENSE, INCOME, SAVING)
 * @property date Transaction date
 * @property comment Optional comment
 * @property savingsGoalId Optional savings goal ID (required for SAVING type)
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = SavingsGoal::class,
            parentColumns = ["id"],
            childColumns = ["savingsGoalId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("savingsGoalId")]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amountInCents: Int,
    val type: TransactionType,
    val date: LocalDate,
    val comment: String?,
    val savingsGoalId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)