package com.tensiorr.budgetapp.data.entity

import androidx.room.Entity

@Entity(
    tableName = "transaction_tag_cross_ref",
    primaryKeys = ["transactionId", "tagId"]
)
data class TransactionTagCrossRef(
    val transactionId: Long,
    val tagId: Long
)