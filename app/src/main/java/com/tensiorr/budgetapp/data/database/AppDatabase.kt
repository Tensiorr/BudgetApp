package com.tensiorr.budgetapp.data.database

import android.content.Context
import android.util.Log
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Database(
    entities = [
        Transaction::class,
        Tag::class,
        TransactionTagCrossRef::class,
        Category::class,
        SavingsGoal::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun tagDao(): TagDao
    abstract fun categoryDao(): CategoryDao
    abstract fun savingsGoalDao(): SavingsGoalDao

    companion object {
        private const val TAG = "AppDatabase"

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
                        DatabaseMigrations.MIGRATION_4_5,
                        DatabaseMigrations.MIGRATION_5_6,
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Clear all data from database.
         */
        suspend fun clearAllData(context: Context) {
            val db = getDatabase(context)
            withContext(Dispatchers.IO) {
                try {
                    db.clearAllTables()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to clear database", e)
                }
            }
        }
    }
}