package com.example.voicebill.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicebill.di.SecurePrefs
import com.example.voicebill.domain.model.BillInfo
import com.example.voicebill.domain.model.Category
import com.example.voicebill.domain.model.Transaction
import com.example.voicebill.domain.model.TransactionType
import com.example.voicebill.domain.repository.BillParserRepository
import com.example.voicebill.domain.repository.CategoryRepository
import com.example.voicebill.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val inputText: String = "",
    val isLoading: Boolean = false,
    val parseResult: BillInfo? = null,
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null,
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val amount: Long = 0,
    val note: String = "",
    val error: String? = null,
    val successMessage: String? = null,
    val hasApiKey: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val billParserRepository: BillParserRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val securePrefs: SecurePrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        checkApiKey()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { categories ->
                _uiState.value = _uiState.value.copy(categories = categories)
            }
        }
    }

    private fun checkApiKey() {
        _uiState.value = _uiState.value.copy(hasApiKey = securePrefs.hasApiKey())
    }

    fun onInputTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(
            inputText = text,
            parseResult = null,
            error = null,
            successMessage = null
        )
    }

    fun parseText() {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "请输入记账内容")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = billParserRepository.parseBillText(text)

            if (result.parseSuccess && result.amountCents != null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    parseResult = result,
                    selectedType = result.type ?: TransactionType.EXPENSE,
                    amount = result.amountCents,
                    selectedCategoryId = findCategoryIdByName(result.categoryName),
                    note = result.note ?: ""
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.errorMessage ?: "解析失败，请手动填写"
                )
            }
        }
    }

    private fun findCategoryIdByName(name: String?): Long? {
        if (name == null) return null
        return _uiState.value.categories.find { it.name == name }?.id
    }

    fun onCategorySelected(categoryId: Long) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
    }

    fun onTypeSelected(type: TransactionType) {
        _uiState.value = _uiState.value.copy(selectedType = type)
    }

    fun onAmountChanged(amount: Long) {
        _uiState.value = _uiState.value.copy(amount = amount)
    }

    fun onNoteChanged(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }

    fun saveTransaction() {
        val state = _uiState.value

        if (state.amount <= 0) {
            _uiState.value = state.copy(error = "请输入有效金额")
            return
        }

        if (state.selectedCategoryId == null) {
            _uiState.value = state.copy(error = "请选择分类")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val category = categoryRepository.getCategoryById(state.selectedCategoryId)

            val transaction = Transaction(
                amountCents = state.amount,
                categoryId = state.selectedCategoryId,
                categoryNameSnapshot = category?.name ?: "未知",
                type = state.selectedType,
                date = state.parseResult?.date ?: System.currentTimeMillis(),
                note = state.note.ifBlank { null },
                rawText = state.inputText
            )

            transactionRepository.insertTransaction(transaction)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                inputText = "",
                parseResult = null,
                amount = 0,
                note = "",
                successMessage = "记账成功！"
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
