package com.tensiorr.budgetapp.ui

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Available date format options.
 */
enum class DateFormatOption(
    val displayName: String,
    val pattern: String,
    val separator: String
) {
    DD_MM_YYYY("DD.MM.YYYY", "dd.MM.yyyy", "."),
    MM_DD_YYYY("MM.DD.YYYY", "MM.dd.yyyy", "."),
    YYYY_MM_DD("YYYY-MM-DD", "yyyy-MM-dd", "-"),
    DD_MM_YYYY_SLASH("DD/MM/YYYY", "dd/MM/yyyy", "/"),
    MM_DD_YYYY_SLASH("MM/DD/YYYY", "MM/dd/yyyy", "/");

    fun format(date: LocalDate): String {
        return date.format(DateTimeFormatter.ofPattern(pattern))
    }

    companion object {
        fun fromPattern(pattern: String): DateFormatOption {
            return values().find { it.displayName == pattern } ?: DD_MM_YYYY
        }
    }
}