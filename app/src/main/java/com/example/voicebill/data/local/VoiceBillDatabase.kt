package com.example.voicebill.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.voicebill.data.local.dao.CategoryDao
import com.example.voicebill.data.local.dao.TransactionDao
import com.example.voicebill.data.local.entity.CategoryEntity
import com.example.voicebill.data.local.entity.TransactionEntity

@Database(
    entities = [CategoryEntity::class, TransactionEntity::class],
    version = 2,
    exportSchema = false
)
abstract class VoiceBillDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val DATABASE_NAME = "voicebill_db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. 添加 isUncategorized 列
                database.execSQL("ALTER TABLE categories ADD COLUMN isUncategorized INTEGER NOT NULL DEFAULT 0")

                // 2. 插入支出"未分类"
                database.execSQL("""
                    INSERT INTO categories (name, icon, color, isIncome, isDefault, isDeleted, isUncategorized)
                    VALUES ('未分类', 'help_outline', '#9E9E9E', 0, 1, 0, 1)
                """)

                // 3. 插入收入"未分类"
                database.execSQL("""
                    INSERT INTO categories (name, icon, color, isIncome, isDefault, isDeleted, isUncategorized)
                    VALUES ('未分类', 'help_outline', '#9E9E9E', 1, 1, 0, 1)
                """)

                // 4. 获取支出"未分类"的 ID 并迁移交易记录
                val expenseCursor = database.query(
                    "SELECT id FROM categories WHERE isUncategorized = 1 AND isIncome = 0"
                )
                if (expenseCursor.moveToFirst()) {
                    val expenseUncategorizedId = expenseCursor.getLong(0)
                    database.execSQL("""
                        UPDATE transactions
                        SET categoryId = $expenseUncategorizedId,
                            categoryNameSnapshot = '未分类'
                        WHERE categoryId IS NULL AND type = 'EXPENSE'
                    """)
                }
                expenseCursor.close()

                // 5. 获取收入"未分类"的 ID 并迁移交易记录
                val incomeCursor = database.query(
                    "SELECT id FROM categories WHERE isUncategorized = 1 AND isIncome = 1"
                )
                if (incomeCursor.moveToFirst()) {
                    val incomeUncategorizedId = incomeCursor.getLong(0)
                    database.execSQL("""
                        UPDATE transactions
                        SET categoryId = $incomeUncategorizedId,
                            categoryNameSnapshot = '未分类'
                        WHERE categoryId IS NULL AND type = 'INCOME'
                    """)
                }
                incomeCursor.close()
            }
        }
    }
}
