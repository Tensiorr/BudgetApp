package com.tensiorr.budgetapp.ui

import com.tensiorr.budgetapp.data.entity.TransactionType
import java.time.LocalDate

/**
 * Represents the current filter state for transaction list.
 */
data class FilterState(
    val transactionType: TransactionTypeFilter = TransactionTypeFilter.ALL,
    val categoryIds: Set<Long> = emptySet(),
    val tagIds: Set<Long> = emptySet(),
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
 */
enum class TransactionTypeFilter {
    ALL,
    EXPENSE,
    INCOME
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

    /**
     * Display name for UI.
     */
    fun displayName(): String = when (this) {
        is AllTime -> "Wszystkie czasy"
        is ThisMonth -> "Ten miesiąc"
        is LastMonth -> "Ostatni miesiąc"
        is Last3Months -> "Ostatnie 3 miesiące"
        is Custom -> {
            val formatter = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")
            "${startDate.format(formatter)} - ${endDate.format(formatter)}"
        }
    }
}