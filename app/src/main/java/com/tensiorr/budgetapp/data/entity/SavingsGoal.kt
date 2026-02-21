package com.tensiorr.budgetapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val targetAmount: Int,
    val currentAmount: Int = 0,
    val deadline: LocalDate? = null,
    val isArchived: Boolean = false,
    val createdAt: LocalDate
)