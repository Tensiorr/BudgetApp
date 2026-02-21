package com.tensiorr.budgetapp.data.entity

/**
 * Types of transactions supported by the app.
 *
 * @property EXPENSE Money spent
 * @property INCOME Money received
 * @property SAVING Money saved towards a savings goal
 */
enum class TransactionType {
    EXPENSE,
    INCOME,
    SAVING
}