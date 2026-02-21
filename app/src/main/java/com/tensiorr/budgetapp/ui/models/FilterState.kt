package com.tensiorr.budgetapp.ui.models

import java.time.LocalDate

/**
 * Represents the current filter state for transaction list.
 *
 * @property transactionType Type of transactions to show
 * @property categoryIds Set of category IDs to filter by
 * @property tagIds Set of tag IDs to filter by
 * @property savingsGoalIds Set of savings goal IDs to filter by
 * @property dateRange Date range to filter transactions
 */
data class FilterState(
    val transactionType: TransactionTypeFilter = TransactionTypeFilter.ALL,
    val categoryIds: Set<Long> = emptySet(),
    val tagIds: Set<Long> = emptySet(),
    val savingsGoalIds: Set<Long> = emptySet(),
    val dateRange: DateRangeFilter = DateRangeFilter.AllTime
) {
    /**
     * Returns number of active filters (for badge display).
     */
    fun activeFilterCount(): Int {
        var count = 0
        if (transactionType != TransactionTypeFilter.ALL) count++
        count += categoryIds.size
        count += tagIds.size
        count += savingsGoalIds.size
        if (dateRange != DateRangeFilter.AllTime) count++
        return count
    }

    /**
     * Checks if any filters are active.
     */
    fun hasActiveFilters(): Boolean = activeFilterCount() > 0
}

/**
 * Transaction type filter options.
 *
 * @property ALL Show all transaction types
 * @property EXPENSE Show only expenses
 * @property INCOME Show only income
 * @property SAVING Show only savings
 */
enum class TransactionTypeFilter {
    ALL,
    EXPENSE,
    INCOME,
    SAVING
}

/**
 * Date range filter options.
 */
sealed class DateRangeFilter {
    object AllTime : DateRangeFilter()
    object ThisMonth : DateRangeFilter()
    object LastMonth : DateRangeFilter()
    object Last3Months : DateRangeFilter()
    data class Custom(val startDate: LocalDate, val endDate: LocalDate) : DateRangeFilter()

    fun displayName(dateFormat: DateFormatOption = DateFormatOption.DD_MM_YYYY): String = when (this) {
        is AllTime -> "Wszystkie czasy"
        is ThisMonth -> "Ten miesiąc"
        is LastMonth -> "Ostatni miesiąc"
        is Last3Months -> "Ostatnie 3 miesiące"
        is Custom -> {
            "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"
        }
    }
}