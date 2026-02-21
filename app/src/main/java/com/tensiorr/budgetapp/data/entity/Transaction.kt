package com.tensiorr.budgetapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

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
    val savingsGoalId: Long? = null
)