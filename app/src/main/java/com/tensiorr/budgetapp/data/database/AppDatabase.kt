package com.tensiorr.budgetapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tensiorr.budgetapp.data.Converters
import com.tensiorr.budgetapp.data.dao.CategoryDao
import com.tensiorr.budgetapp.data.dao.SavingsGoalDao
import com.tensiorr.budgetapp.data.dao.TagDao
import com.tensiorr.budgetapp.data.dao.TransactionDao
import com.tensiorr.budgetapp.data.entity.Tag
import com.tensiorr.budgetapp.data.entity.Transaction
import com.tensiorr.budgetapp.data.entity.TransactionTagCrossRef
import com.tensiorr.budgetapp.data.entity.Category
import com.tensiorr.budgetapp.data.entity.SavingsGoal

@Database(
    entities = [
        Transaction::class,
        Tag::class,
        TransactionTagCrossRef::class,
        Category::class,
        SavingsGoal::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun tagDao(): TagDao
    abstract fun categoryDao(): CategoryDao
    abstract fun savingsGoalDao(): SavingsGoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budget_database"
                )
                    .addMigrations(
                        DatabaseMigrations.MIGRATION_3_4,
                        DatabaseMigrations.MIGRATION_4_5
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}