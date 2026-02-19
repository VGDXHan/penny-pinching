package com.example.voicebill.data.repository

import androidx.room.withTransaction
import com.example.voicebill.data.local.VoiceBillDatabase
import com.example.voicebill.data.local.dao.CategoryDao
import com.example.voicebill.data.local.dao.TransactionDao
import com.example.voicebill.data.local.entity.CategoryEntity
import com.example.voicebill.domain.model.Category
import com.example.voicebill.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
    private val database: VoiceBillDatabase
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getExpenseCategories(): Flow<List<Category>> {
        return categoryDao.getCategoriesByType(isIncome = false).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getIncomeCategories(): Flow<List<Category>> {
        return categoryDao.getCategoriesByType(isIncome = true).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCategoryById(id: Long): Category? {
        return categoryDao.getCategoryById(id)?.toDomain()
    }

    override suspend fun getUncategorizedCategory(isIncome: Boolean): Category? {
        return categoryDao.getUncategorizedCategory(isIncome)?.toDomain()
    }

    override suspend fun insertCategory(category: Category): Long {
        return categoryDao.insertCategory(CategoryEntity.fromDomain(category))
    }

    override suspend fun updateCategory(category: Category) {
        val oldCategory = categoryDao.getCategoryById(category.id)

        database.withTransaction {
            categoryDao.updateCategory(CategoryEntity.fromDomain(category))

            // 如果名称变化，同步更新账单的 categoryNameSnapshot
            if (oldCategory != null && oldCategory.name != category.name) {
                transactionDao.updateCategoryNameSnapshot(category.id, category.name)
            }
        }
    }

    override suspend fun deleteCategory(id: Long) {
        categoryDao.softDeleteCategory(id)
    }

    override suspend fun deleteCategoryWithMigration(id: Long) {
        val category = categoryDao.getCategoryById(id) ?: return
        val uncategorized = categoryDao.getUncategorizedCategory(category.isIncome)
            ?: throw IllegalStateException("未分类不存在")

        database.withTransaction {
            transactionDao.migrateCategoryTransactions(
                oldCategoryId = id,
                newCategoryId = uncategorized.id,
                newCategoryName = uncategorized.name
            )
            categoryDao.softDeleteCategory(id)
        }
    }

    override suspend fun restoreCategory(id: Long) {
        categoryDao.restoreCategory(id)
    }
}
