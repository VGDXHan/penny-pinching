package com.example.voicebill.ui.screens.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicebill.domain.model.Category
import com.example.voicebill.domain.model.Transaction
import com.example.voicebill.domain.model.TransactionType
import com.example.voicebill.domain.repository.CategoryRepository
import com.example.voicebill.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecordsUiState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val editingTransaction: Transaction? = null,
    val error: String? = null,
    val categories: List<Category> = emptyList(),
    val editAmount: Long = 0,
    val editCategoryId: Long? = null,
    val editType: TransactionType = TransactionType.EXPENSE,
    val editDate: Long = System.currentTimeMillis(),
    val editNote: String = ""
)

@HiltViewModel
class RecordsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordsUiState())
    val uiState: StateFlow<RecordsUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { categories ->
                _uiState.value = _uiState.value.copy(categories = categories)
            }
        }
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            transactionRepository.getAllTransactions().collect { transactions ->
                _uiState.value = _uiState.value.copy(transactions = transactions)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        if (query.isNotBlank()) {
            searchTransactions(query)
        } else {
            loadTransactions()
        }
    }

    private fun searchTransactions(query: String) {
        viewModelScope.launch {
            transactionRepository.searchTransactions(query).collect { transactions ->
                _uiState.value = _uiState.value.copy(transactions = transactions)
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                transactionRepository.deleteTransaction(transaction)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun startEditing(transaction: Transaction) {
        _uiState.value = _uiState.value.copy(
            editingTransaction = transaction,
            editAmount = transaction.amountCents,
            editCategoryId = transaction.categoryId,
            editType = transaction.type,
            editDate = transaction.date,
            editNote = transaction.note ?: ""
        )
    }

    fun onEditAmountChanged(amount: Long) {
        _uiState.value = _uiState.value.copy(editAmount = amount)
    }

    fun onEditCategorySelected(categoryId: Long) {
        _uiState.value = _uiState.value.copy(editCategoryId = categoryId)
    }

    fun onEditTypeSelected(type: TransactionType) {
        val state = _uiState.value
        val newCategoryId = keepCategoryIfTypeMatched(
            categoryId = state.editCategoryId,
            type = type,
            categories = state.categories
        )
        _uiState.value = state.copy(editType = type, editCategoryId = newCategoryId)
    }

    fun onEditDateSelected(date: Long) {
        _uiState.value = _uiState.value.copy(editDate = date)
    }

    fun onEditNoteChanged(note: String) {
        _uiState.value = _uiState.value.copy(editNote = note)
    }

    fun saveEditedTransaction() {
        val state = _uiState.value
        val editingTransaction = state.editingTransaction ?: return

        if (state.editAmount <= 0) {
            _uiState.value = state.copy(error = "请输入有效金额")
            return
        }
        if (state.editCategoryId == null) {
            _uiState.value = state.copy(error = "请选择分类")
            return
        }

        viewModelScope.launch {
            val category = categoryRepository.getCategoryById(state.editCategoryId)
            val categoryName = category?.name
                ?: state.categories.find { it.id == state.editCategoryId }?.name
                ?: editingTransaction.categoryNameSnapshot

            val updatedTransaction = editingTransaction.copy(
                amountCents = state.editAmount,
                categoryId = state.editCategoryId,
                categoryNameSnapshot = categoryName,
                type = state.editType,
                date = state.editDate,
                note = state.editNote.ifBlank { null }
            )
            transactionRepository.updateTransaction(updatedTransaction)
            cancelEditing()
        }
    }

    fun cancelEditing() {
        _uiState.value = _uiState.value.copy(
            editingTransaction = null,
            editAmount = 0,
            editCategoryId = null,
            editType = TransactionType.EXPENSE,
            editDate = System.currentTimeMillis(),
            editNote = ""
        )
    }

    private fun keepCategoryIfTypeMatched(
        categoryId: Long?,
        type: TransactionType,
        categories: List<Category>
    ): Long? {
        val category = categories.find { it.id == categoryId } ?: return null
        val isIncome = type == TransactionType.INCOME
        return if (category.isIncome == isIncome) categoryId else null
    }
}
