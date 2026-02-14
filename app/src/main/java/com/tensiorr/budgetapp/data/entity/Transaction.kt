package com.tensiorr.budgetapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amountInCents: Int,
    val type: TransactionType,
    val date: LocalDate,
    val comment: String?
)