package com.tensiorr.budgetapp.data.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * Transaction with its associated tags (many-to-many relation).
 *
 * @property transaction The transaction entity
 * @property tags List of tags associated with this transaction
 */
data class TransactionWithTags(
    @Embedded val transaction: Transaction,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            TransactionTagCrossRef::class,
            parentColumn = "transactionId",
            entityColumn = "tagId"
        )
    )
    val tags: List<Tag>
)