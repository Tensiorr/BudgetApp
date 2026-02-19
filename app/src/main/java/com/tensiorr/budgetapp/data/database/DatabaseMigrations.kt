package com.tensiorr.budgetapp.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


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
}