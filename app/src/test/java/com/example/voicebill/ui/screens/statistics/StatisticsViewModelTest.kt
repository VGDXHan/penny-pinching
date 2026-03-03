package com.example.voicebill.ui.screens.statistics

import com.example.voicebill.domain.model.StatisticsPeriod
import com.example.voicebill.domain.model.StatisticsResult
import com.example.voicebill.domain.usecase.GetStatisticsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getStatisticsUseCase: GetStatisticsUseCase = mockk()

    @Test
    fun `init should load statistics with loading indicator`() = runTest {
        val monthlyStats = createStatistics(StatisticsPeriod.MONTHLY, totalIncome = 50000L)
        coEvery { getStatisticsUseCase.getStatistics(StatisticsPeriod.MONTHLY) } returns monthlyStats

        val viewModel = StatisticsViewModel(getStatisticsUseCase)

        assertTrue(viewModel.uiState.value.isLoading)
        advanceUntilIdle()

        assertEquals(monthlyStats, viewModel.uiState.value.statistics)
        assertFalse(viewModel.uiState.value.isLoading)
        coVerify(exactly = 1) { getStatisticsUseCase.getStatistics(StatisticsPeriod.MONTHLY) }
    }

    @Test
    fun `onScreenEntered should refresh silently`() = runTest {
        val firstStats = createStatistics(StatisticsPeriod.MONTHLY, totalIncome = 10000L)
        val secondStats = createStatistics(StatisticsPeriod.MONTHLY, totalIncome = 20000L)
        coEvery { getStatisticsUseCase.getStatistics(StatisticsPeriod.MONTHLY) } returnsMany listOf(
            firstStats,
            secondStats
        )

        val viewModel = StatisticsViewModel(getStatisticsUseCase)
        advanceUntilIdle()

        viewModel.onScreenEntered()
        assertFalse(viewModel.uiState.value.isLoading)
        advanceUntilIdle()

        assertEquals(secondStats, viewModel.uiState.value.statistics)
        assertFalse(viewModel.uiState.value.isLoading)
        coVerify(exactly = 2) { getStatisticsUseCase.getStatistics(StatisticsPeriod.MONTHLY) }
    }

    @Test
    fun `onScreenEntered should skip when loading`() = runTest {
        val gate = CompletableDeferred<Unit>()
        val monthlyStats = createStatistics(StatisticsPeriod.MONTHLY)
        coEvery { getStatisticsUseCase.getStatistics(StatisticsPeriod.MONTHLY) } coAnswers {
            gate.await()
            monthlyStats
        }

        val viewModel = StatisticsViewModel(getStatisticsUseCase)

        assertTrue(viewModel.uiState.value.isLoading)
        viewModel.onScreenEntered()

        gate.complete(Unit)
        advanceUntilIdle()

        coVerify(exactly = 1) { getStatisticsUseCase.getStatistics(StatisticsPeriod.MONTHLY) }
        assertEquals(monthlyStats, viewModel.uiState.value.statistics)
    }

    @Test
    fun `onPeriodSelected should clear selected categories and refresh silently`() = runTest {
        val monthlyStats = createStatistics(StatisticsPeriod.MONTHLY)
        val dailyStats = createStatistics(StatisticsPeriod.DAILY)
        coEvery { getStatisticsUseCase.getStatistics(StatisticsPeriod.MONTHLY) } returns monthlyStats
        coEvery { getStatisticsUseCase.getStatistics(StatisticsPeriod.DAILY) } returns dailyStats

        val viewModel = StatisticsViewModel(getStatisticsUseCase)
        advanceUntilIdle()

        viewModel.onExpenseCategorySelected(1L)
        viewModel.onIncomeCategorySelected(2L)

        viewModel.onPeriodSelected(StatisticsPeriod.DAILY)

        assertEquals(StatisticsPeriod.DAILY, viewModel.uiState.value.selectedPeriod)
        assertNull(viewModel.uiState.value.selectedExpenseCategoryId)
        assertNull(viewModel.uiState.value.selectedIncomeCategoryId)
        assertFalse(viewModel.uiState.value.isLoading)

        advanceUntilIdle()

        assertEquals(dailyStats, viewModel.uiState.value.statistics)
        coVerify(exactly = 1) { getStatisticsUseCase.getStatistics(StatisticsPeriod.MONTHLY) }
        coVerify(exactly = 1) { getStatisticsUseCase.getStatistics(StatisticsPeriod.DAILY) }
    }

    private fun createStatistics(
        period: StatisticsPeriod,
        totalIncome: Long = 10000L,
        totalExpense: Long = 2000L
    ): StatisticsResult {
        return StatisticsResult(
            period = period,
            startDate = 1_700_000_000_000L,
            endDate = 1_700_086_400_000L,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            balance = totalIncome - totalExpense,
            incomeCategorySummaries = emptyList(),
            expenseCategorySummaries = emptyList(),
            dailySummaries = emptyList()
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
