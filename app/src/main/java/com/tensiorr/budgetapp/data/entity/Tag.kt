package com.tensiorr.budgetapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tag entity for detailed transaction classification within categories.
 *
 * @property id Unique identifier
 * @property name Tag name
 * @property transactionType Type of transactions this tag applies to
 * @property categoryId Parent category ID
 */
@Entity(
    tableName = "tags",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId")]
)
data class Tag(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val transactionType: TransactionType,
    val categoryId: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)