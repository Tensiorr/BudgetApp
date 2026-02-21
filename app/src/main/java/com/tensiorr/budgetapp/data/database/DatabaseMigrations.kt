package com.tensiorr.budgetapp.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database schema migrations.
 *
 * Contains migration strategies for database version updates.
 */
object DatabaseMigrations {
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS tags_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    transactionType TEXT NOT NULL,
                    categoryId INTEGER NOT NULL,
                    FOREIGN KEY(categoryId) REFERENCES categories(id) ON DELETE CASCADE
                )
            """)

            database.execSQL("""
                INSERT INTO tags_new (id, name, transactionType, categoryId)
                SELECT id, name, transactionType, categoryId FROM tags
            """)

            database.execSQL("DROP TABLE tags")

            database.execSQL("ALTER TABLE tags_new RENAME TO tags")

            database.execSQL("CREATE INDEX IF NOT EXISTS index_tags_categoryId ON tags(categoryId)")

            database.execSQL("""
                CREATE TABLE IF NOT EXISTS transaction_tag_cross_ref_new (
                    transactionId INTEGER NOT NULL,
                    tagId INTEGER NOT NULL,
                    PRIMARY KEY(transactionId, tagId),
                    FOREIGN KEY(transactionId) REFERENCES transactions(id) ON DELETE CASCADE,
                    FOREIGN KEY(tagId) REFERENCES tags(id) ON DELETE CASCADE
                )
            """)

            database.execSQL("""
                INSERT INTO transaction_tag_cross_ref_new (transactionId, tagId)
                SELECT transactionId, tagId FROM transaction_tag_cross_ref
            """)

            database.execSQL("DROP TABLE transaction_tag_cross_ref")

            database.execSQL("ALTER TABLE transaction_tag_cross_ref_new RENAME TO transaction_tag_cross_ref")

            database.execSQL("CREATE INDEX IF NOT EXISTS index_transaction_tag_cross_ref_transactionId ON transaction_tag_cross_ref(transactionId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_transaction_tag_cross_ref_tagId ON transaction_tag_cross_ref(tagId)")
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""
            CREATE TABLE IF NOT EXISTS savings_goals (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                targetAmount INTEGER NOT NULL,
                currentAmount INTEGER NOT NULL DEFAULT 0,
                deadline INTEGER,
                isArchived INTEGER NOT NULL DEFAULT 0,
                createdAt INTEGER NOT NULL
            )
        """)

            database.execSQL("""
            CREATE TABLE IF NOT EXISTS transactions_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                amountInCents INTEGER NOT NULL,
                type TEXT NOT NULL,
                date INTEGER NOT NULL,
                comment TEXT,
                savingsGoalId INTEGER,
                FOREIGN KEY(savingsGoalId) REFERENCES savings_goals(id) ON DELETE SET NULL
            )
        """)

            database.execSQL("""
            INSERT INTO transactions_new (id, amountInCents, type, date, comment, savingsGoalId)
            SELECT id, amountInCents, type, date, comment, NULL as savingsGoalId
            FROM transactions
        """)

            database.execSQL("DROP TABLE transactions")

            database.execSQL("ALTER TABLE transactions_new RENAME TO transactions")

            database.execSQL("""
            CREATE TEMPORARY TABLE transaction_tag_cross_ref_backup AS 
            SELECT * FROM transaction_tag_cross_ref
        """)

            database.execSQL("DROP TABLE transaction_tag_cross_ref")

            database.execSQL("""
            CREATE TABLE IF NOT EXISTS transaction_tag_cross_ref (
                transactionId INTEGER NOT NULL,
                tagId INTEGER NOT NULL,
                PRIMARY KEY(transactionId, tagId),
                FOREIGN KEY(transactionId) REFERENCES transactions(id) ON DELETE CASCADE,
                FOREIGN KEY(tagId) REFERENCES tags(id) ON DELETE CASCADE
            )
        """)

            database.execSQL("""
            INSERT INTO transaction_tag_cross_ref (transactionId, tagId)
            SELECT transactionId, tagId FROM transaction_tag_cross_ref_backup
        """)

            database.execSQL("DROP TABLE transaction_tag_cross_ref_backup")

            database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_transactions_savingsGoalId 
            ON transactions(savingsGoalId)
        """)

            database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_transaction_tag_cross_ref_transactionId 
            ON transaction_tag_cross_ref(transactionId)
        """)

            database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_transaction_tag_cross_ref_tagId 
            ON transaction_tag_cross_ref(tagId)
        """)
        }
    }
}