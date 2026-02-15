package com.example.voicebill.ui.screens.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicebill.domain.model.Category
import com.example.voicebill.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoriesUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val showAddDialog: Boolean = false,
    val editingCategory: Category? = null,
    val error: String? = null
)

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { categories ->
                _uiState.value = _uiState.value.copy(categories = categories)
            }
        }
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true, editingCategory = null)
    }

    fun showEditDialog(category: Category) {
        _uiState.value = _uiState.value.copy(showAddDialog = true, editingCategory = category)
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false, editingCategory = null)
    }

    fun addCategory(name: String, icon: String, color: String, isIncome: Boolean) {
        viewModelScope.launch {
            try {
                val category = Category(
                    name = name,
                    icon = icon,
                    color = color,
                    isIncome = isIncome
                )
                categoryRepository.insertCategory(category)
                dismissDialog()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateCategory(id: Long, name: String, icon: String, color: String) {
        viewModelScope.launch {
            try {
                val existingCategory = categoryRepository.getCategoryById(id)
                if (existingCategory != null) {
                    categoryRepository.updateCategory(
                        existingCategory.copy(name = name, icon = icon, color = color)
                    )
                }
                dismissDialog()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            try {
                categoryRepository.deleteCategory(id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
