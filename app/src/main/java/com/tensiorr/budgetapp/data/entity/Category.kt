package com.tensiorr.budgetapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Category entity for organizing transactions.
 *
 * @property id Unique identifier
 * @property name Category name
 * @property transactionType Type of transactions this category applies to
 */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val transactionType: TransactionType
)