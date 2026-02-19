package com.example.voicebill.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.voicebill.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY date DESC, createdAt DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date >= :startDate AND date < :endDate ORDER BY date DESC, createdAt DESC")
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY date DESC, createdAt DESC")
    fun getTransactionsByCategory(categoryId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC, createdAt DESC")
    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE rawText LIKE '%' || :keyword || '%' OR note LIKE '%' || :keyword || '%' ORDER BY date DESC, createdAt DESC")
    fun searchTransactions(keyword: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    // Statistics queries
    @Query("SELECT SUM(amountCents) FROM transactions WHERE type = :type AND date >= :startDate AND date < :endDate")
    suspend fun getTotalAmountByTypeAndDateRange(type: String, startDate: Long, endDate: Long): Long?

    @Query("""
        SELECT categoryId, categoryNameSnapshot, SUM(amountCents) as total
        FROM transactions
        WHERE type = :type AND date >= :startDate AND date < :endDate
        GROUP BY categoryId
        ORDER BY total DESC
    """)
    suspend fun getCategoryTotalsByTypeAndDateRange(type: String, startDate: Long, endDate: Long): List<CategoryTotal>

    @Query("""
        SELECT
            date / :dayMillis * :dayMillis as dayStart,
            SUM(CASE WHEN type = 'INCOME' THEN amountCents ELSE 0 END) as income,
            SUM(CASE WHEN type = 'EXPENSE' THEN amountCents ELSE 0 END) as expense
        FROM transactions
        WHERE date >= :startDate AND date < :endDate
        GROUP BY dayStart
        ORDER BY dayStart ASC
    """)
    suspend fun getDailyTotals(startDate: Long, endDate: Long, dayMillis: Long): List<DailyTotal>

    @Query("""
        UPDATE transactions
        SET categoryId = :newCategoryId,
            categoryNameSnapshot = :newCategoryName
        WHERE categoryId = :oldCategoryId
    """)
    suspend fun migrateCategoryTransactions(
        oldCategoryId: Long,
        newCategoryId: Long,
        newCategoryName: String
    )

    @Query("""
        UPDATE transactions
        SET categoryNameSnapshot = :newCategoryName
        WHERE categoryId = :categoryId
    """)
    suspend fun updateCategoryNameSnapshot(categoryId: Long, newCategoryName: String)
}

data class CategoryTotal(
    val categoryId: Long?,
    val categoryNameSnapshot: String,
    val total: Long
)

data class DailyTotal(
    val dayStart: Long,
    val income: Long,
    val expense: Long
)
