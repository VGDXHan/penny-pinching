package com.example.voicebill.domain.repository

import com.example.voicebill.domain.model.BillInfo
import com.example.voicebill.domain.model.Category
import com.example.voicebill.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface BillParserRepository {
    suspend fun parseBillText(text: String): BillInfo
}

interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
    fun getExpenseCategories(): Flow<List<Category>>
    fun getIncomeCategories(): Flow<List<Category>>
    suspend fun getCategoryById(id: Long): Category?
    suspend fun insertCategory(category: Category): Long
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(id: Long)
    suspend fun restoreCategory(id: Long)
}

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>>
    fun getTransactionsByCategory(categoryId: Long): Flow<List<Transaction>>
    fun searchTransactions(keyword: String): Flow<List<Transaction>>
    suspend fun getTransactionById(id: Long): Transaction?
    suspend fun insertTransaction(transaction: Transaction): Long
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
    suspend fun deleteAllTransactions()
}
