package com.example.voicebill.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.voicebill.data.local.dao.CategoryDao
import com.example.voicebill.data.local.dao.TransactionDao
import com.example.voicebill.data.local.entity.CategoryEntity
import com.example.voicebill.data.local.entity.TransactionEntity

@Database(
    entities = [CategoryEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class VoiceBillDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val DATABASE_NAME = "voicebill_db"
    }
}
