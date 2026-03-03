package com.example.voicebill.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicebill.domain.model.CustomDateRange
import com.example.voicebill.domain.model.StatisticsPeriod
import com.example.voicebill.domain.model.StatisticsQuery
import com.example.voicebill.domain.model.StatisticsResult
import com.example.voicebill.domain.usecase.GetStatisticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val showCustomRangePicker: Boolean = false
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getStatisticsUseCase: GetStatisticsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics(showLoading = true)
    }

    fun onPeriodSelected(period: StatisticsPeriod) {
        _uiState.value = _uiState.value.copy(
            selectedPeriod = period,
            periodOffset = 0,
            customRange = null,
            selectedExpenseCategoryId = null,
            selectedIncomeCategoryId = null,
            error = null
        )
        loadStatistics(showLoading = false)
    }

    fun onPreviousPeriod() {
        if (_uiState.value.selectedPeriod == StatisticsPeriod.CUSTOM) return

        _uiState.value = _uiState.value.copy(
            periodOffset = _uiState.value.periodOffset + 1,
            selectedExpenseCategoryId = null,
            selectedIncomeCategoryId = null,
            error = null
        )
        loadStatistics(showLoading = false)
    }

    fun onNextPeriod() {
        val currentState = _uiState.value
        if (currentState.selectedPeriod == StatisticsPeriod.CUSTOM || currentState.periodOffset == 0) {
            return
        }

        _uiState.value = _uiState.value.copy(
            periodOffset = currentState.periodOffset - 1,
            selectedExpenseCategoryId = null,
            selectedIncomeCategoryId = null,
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
            error = null
        )
        loadStatistics(showLoading = false)
    }

    fun onExpenseCategorySelected(categoryId: Long?) {
        val currentSelected = _uiState.value.selectedExpenseCategoryId
        _uiState.value = _uiState.value.copy(
            selectedExpenseCategoryId = if (currentSelected == categoryId) null else categoryId
        )
    }

    fun onIncomeCategorySelected(categoryId: Long?) {
        val currentSelected = _uiState.value.selectedIncomeCategoryId
        _uiState.value = _uiState.value.copy(
            selectedIncomeCategoryId = if (currentSelected == categoryId) null else categoryId
        )
    }

    fun refresh() {
        loadStatistics(showLoading = true)
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
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
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
}
