package com.example.voicebill.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicebill.domain.model.StatisticsPeriod
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
    val statistics: StatisticsResult? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getStatisticsUseCase: GetStatisticsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    fun onPeriodSelected(period: StatisticsPeriod) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val result = getStatisticsUseCase.getStatistics(_uiState.value.selectedPeriod)
                _uiState.value = _uiState.value.copy(
                    statistics = result,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}
