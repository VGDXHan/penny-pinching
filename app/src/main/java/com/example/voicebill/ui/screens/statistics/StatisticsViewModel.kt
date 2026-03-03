package com.example.voicebill.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicebill.domain.model.Category
import com.example.voicebill.domain.model.CustomDateRange
import com.example.voicebill.domain.model.StatisticsPeriod
import com.example.voicebill.domain.model.StatisticsQuery
import com.example.voicebill.domain.model.StatisticsResult
import com.example.voicebill.domain.model.Transaction
import com.example.voicebill.domain.model.TransactionType
import com.example.voicebill.domain.repository.CategoryRepository
import com.example.voicebill.domain.repository.TransactionRepository
import com.example.voicebill.domain.usecase.GetStatisticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatisticsUiState(
    val selectedPeriod: StatisticsPeriod = StatisticsPeriod.MONTHLY,
    val periodOffset: Int = 0,
    val customRange: CustomDateRange? = null,
    val statistics: StatisticsResult? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedExpenseCategoryId: Long? = null,
    val selectedIncomeCategoryId: Long? = null,
    val expenseCategoryTransactions: List<Transaction> = emptyList(),
    val incomeCategoryTransactions: List<Transaction> = emptyList(),
    val isExpenseDetailLoading: Boolean = false,
    val isIncomeDetailLoading: Boolean = false,
    val categories: List<Category> = emptyList(),
    val editingTransaction: Transaction? = null,
    val editAmount: Long = 0,
    val editCategoryId: Long? = null,
    val editType: TransactionType = TransactionType.EXPENSE,
    val editDate: Long = System.currentTimeMillis(),
    val editNote: String = "",
    val showCustomRangePicker: Boolean = false
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getStatisticsUseCase: GetStatisticsUseCase,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    private var expenseDetailJob: Job? = null
    private var incomeDetailJob: Job? = null

    init {
        loadCategories()
        loadStatistics(showLoading = true)
    }

    fun onPeriodSelected(period: StatisticsPeriod) {
        expenseDetailJob?.cancel()
        incomeDetailJob?.cancel()
        _uiState.value = _uiState.value.copy(
            selectedPeriod = period,
            periodOffset = 0,
            customRange = null,
            showCustomRangePicker = false,
            selectedExpenseCategoryId = null,
            selectedIncomeCategoryId = null,
            expenseCategoryTransactions = emptyList(),
            incomeCategoryTransactions = emptyList(),
            isExpenseDetailLoading = false,
            isIncomeDetailLoading = false,
            error = null
        )
        loadStatistics(showLoading = false)
    }

    fun onPreviousPeriod() {
        if (_uiState.value.selectedPeriod == StatisticsPeriod.CUSTOM) return

        expenseDetailJob?.cancel()
        incomeDetailJob?.cancel()
        _uiState.value = _uiState.value.copy(
            periodOffset = _uiState.value.periodOffset + 1,
            selectedExpenseCategoryId = null,
            selectedIncomeCategoryId = null,
            expenseCategoryTransactions = emptyList(),
            incomeCategoryTransactions = emptyList(),
            isExpenseDetailLoading = false,
            isIncomeDetailLoading = false,
            error = null
        )
        loadStatistics(showLoading = false)
    }

    fun onNextPeriod() {
        val currentState = _uiState.value
        if (currentState.selectedPeriod == StatisticsPeriod.CUSTOM || currentState.periodOffset == 0) {
            return
        }

        expenseDetailJob?.cancel()
        incomeDetailJob?.cancel()
        _uiState.value = _uiState.value.copy(
            periodOffset = currentState.periodOffset - 1,
            selectedExpenseCategoryId = null,
            selectedIncomeCategoryId = null,
            expenseCategoryTransactions = emptyList(),
            incomeCategoryTransactions = emptyList(),
            isExpenseDetailLoading = false,
            isIncomeDetailLoading = false,
            error = null
        )
        loadStatistics(showLoading = false)
    }

    fun onCustomRangeClick() {
        _uiState.value = _uiState.value.copy(showCustomRangePicker = true)
    }

    fun onCustomRangeDismiss() {
        _uiState.value = _uiState.value.copy(showCustomRangePicker = false)
    }

    fun onCustomRangeConfirmed(startUtcDateMillis: Long, endUtcDateMillis: Long) {
        expenseDetailJob?.cancel()
        incomeDetailJob?.cancel()
        _uiState.value = _uiState.value.copy(
            selectedPeriod = StatisticsPeriod.CUSTOM,
            periodOffset = 0,
            customRange = CustomDateRange(
                startUtcDateMillis = startUtcDateMillis,
                endUtcDateMillis = endUtcDateMillis
            ),
            showCustomRangePicker = false,
            selectedExpenseCategoryId = null,
            selectedIncomeCategoryId = null,
            expenseCategoryTransactions = emptyList(),
            incomeCategoryTransactions = emptyList(),
            isExpenseDetailLoading = false,
            isIncomeDetailLoading = false,
            error = null
        )
        loadStatistics(showLoading = false)
    }

    fun onExpenseCategorySelected(categoryId: Long?) {
        val currentSelected = _uiState.value.selectedExpenseCategoryId
        val nextSelected = if (categoryId == null || currentSelected == categoryId) null else categoryId
        _uiState.value = _uiState.value.copy(selectedExpenseCategoryId = nextSelected)
        updateExpenseCategoryDetails(nextSelected)
    }

    fun onIncomeCategorySelected(categoryId: Long?) {
        val currentSelected = _uiState.value.selectedIncomeCategoryId
        val nextSelected = if (categoryId == null || currentSelected == categoryId) null else categoryId
        _uiState.value = _uiState.value.copy(selectedIncomeCategoryId = nextSelected)
        updateIncomeCategoryDetails(nextSelected)
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
            try {
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
                refreshStatisticsSilently()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
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

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                transactionRepository.deleteTransaction(transaction)
                refreshStatisticsSilently()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    // 页面进入时静默刷新，避免闪烁
    fun onScreenEntered() {
        if (_uiState.value.isLoading) return
        loadStatistics(showLoading = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    // 复用页面进入刷新逻辑
    fun refresh() {
        onScreenEntered()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { categories ->
                _uiState.value = _uiState.value.copy(categories = categories)
            }
        }
    }

    private fun loadStatistics(showLoading: Boolean) {
        val query = buildCurrentQuery() ?: return

        if (showLoading) {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )
        }

        viewModelScope.launch {
            try {
                val result = getStatisticsUseCase.getStatistics(query)
                _uiState.value = _uiState.value.copy(
                    statistics = result,
                    isLoading = false,
                    error = null
                )
                updateExpenseCategoryDetails(_uiState.value.selectedExpenseCategoryId)
                updateIncomeCategoryDetails(_uiState.value.selectedIncomeCategoryId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private fun refreshStatisticsSilently() {
        loadStatistics(showLoading = false)
    }

    private fun buildCurrentQuery(): StatisticsQuery? {
        val state = _uiState.value
        if (state.selectedPeriod == StatisticsPeriod.CUSTOM && state.customRange == null) {
            return null
        }
        return StatisticsQuery(
            period = state.selectedPeriod,
            offset = state.periodOffset,
            customRange = state.customRange
        )
    }

    private fun updateExpenseCategoryDetails(categoryId: Long?) {
        if (categoryId == null) {
            expenseDetailJob?.cancel()
            _uiState.value = _uiState.value.copy(
                expenseCategoryTransactions = emptyList(),
                isExpenseDetailLoading = false
            )
            return
        }

        val statistics = _uiState.value.statistics ?: return
        expenseDetailJob?.cancel()
        expenseDetailJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isExpenseDetailLoading = true,
                expenseCategoryTransactions = emptyList()
            )
            transactionRepository.getTransactionsByCategoryAndDateRange(
                categoryId = categoryId,
                startDate = statistics.startDate,
                endDate = statistics.endDate
            ).collect { transactions ->
                if (_uiState.value.selectedExpenseCategoryId == categoryId) {
                    _uiState.value = _uiState.value.copy(
                        expenseCategoryTransactions = transactions,
                        isExpenseDetailLoading = false
                    )
                }
            }
        }
    }

    private fun updateIncomeCategoryDetails(categoryId: Long?) {
        if (categoryId == null) {
            incomeDetailJob?.cancel()
            _uiState.value = _uiState.value.copy(
                incomeCategoryTransactions = emptyList(),
                isIncomeDetailLoading = false
            )
            return
        }

        val statistics = _uiState.value.statistics ?: return
        incomeDetailJob?.cancel()
        incomeDetailJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isIncomeDetailLoading = true,
                incomeCategoryTransactions = emptyList()
            )
            transactionRepository.getTransactionsByCategoryAndDateRange(
                categoryId = categoryId,
                startDate = statistics.startDate,
                endDate = statistics.endDate
            ).collect { transactions ->
                if (_uiState.value.selectedIncomeCategoryId == categoryId) {
                    _uiState.value = _uiState.value.copy(
                        incomeCategoryTransactions = transactions,
                        isIncomeDetailLoading = false
                    )
                }
            }
        }
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
