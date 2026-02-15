package com.example.voicebill.data.repository

import com.example.voicebill.data.local.dao.CategoryDao
import com.example.voicebill.data.local.entity.CategoryEntity
import com.example.voicebill.domain.model.Category
import com.example.voicebill.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
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

    override suspend fun insertCategory(category: Category): Long {
        return categoryDao.insertCategory(CategoryEntity.fromDomain(category))
    }

    override suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(CategoryEntity.fromDomain(category))
    }

    override suspend fun deleteCategory(id: Long) {
        categoryDao.softDeleteCategory(id)
    }

    override suspend fun restoreCategory(id: Long) {
        categoryDao.restoreCategory(id)
    }
}
