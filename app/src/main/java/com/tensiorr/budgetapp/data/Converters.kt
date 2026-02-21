package com.tensiorr.budgetapp.data

import androidx.room.TypeConverter
import com.tensiorr.budgetapp.data.entity.TransactionType
import java.time.LocalDate

/**
 * Room database type converters for custom data types.
 *
 * Converts between Kotlin types and SQLite-compatible types.
 */
class Converters {
    /**
     * Converts epoch day to LocalDate.
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

    /**
     * Converts LocalDate to epoch day.
     */
    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    /**
     * Converts TransactionType to String.
     */
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String {
        return value.name
    }

    /**
     * Converts String to TransactionType.
     */
    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value)
    }
}