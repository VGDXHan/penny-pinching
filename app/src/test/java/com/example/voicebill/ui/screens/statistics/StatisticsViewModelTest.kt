package com.example.voicebill.ui.screens.statistics

import com.example.voicebill.domain.model.CustomDateRange
import com.example.voicebill.domain.model.StatisticsPeriod
import com.example.voicebill.domain.model.StatisticsQuery
import com.example.voicebill.domain.model.StatisticsResult
import com.example.voicebill.domain.usecase.GetStatisticsUseCase
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getStatisticsUseCase: GetStatisticsUseCase = mockk()

    @Before
    fun setUp() {
        coEvery { getStatisticsUseCase.getStatistics(any()) } coAnswers {
            createStatisticsResult(this.invocation.args[0] as StatisticsQuery)
        }
    }

    @Test
    fun `init should load monthly statistics with zero offset`() = runTest {
        val viewModel = StatisticsViewModel(getStatisticsUseCase)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(StatisticsPeriod.MONTHLY, state.selectedPeriod)
        assertEquals(0, state.periodOffset)
        coVerify {
            getStatisticsUseCase.getStatistics(
                match {
                    it.period == StatisticsPeriod.MONTHLY &&
                        it.offset == 0 &&
                        it.customRange == null
                }
            )
        }
    }

    @Test
    fun `onPreviousPeriod should increase offset and query history period`() = runTest {
        val viewModel = StatisticsViewModel(getStatisticsUseCase)
        advanceUntilIdle()
        clearMocks(getStatisticsUseCase, answers = false)

        viewModel.onPreviousPeriod()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.periodOffset)
        coVerify {
            getStatisticsUseCase.getStatistics(
                match {
                    it.period == StatisticsPeriod.MONTHLY &&
                        it.offset == 1
                }
            )
        }
    }

    @Test
    fun `onNextPeriod should do nothing when offset is zero`() = runTest {
        val viewModel = StatisticsViewModel(getStatisticsUseCase)
        advanceUntilIdle()
        clearMocks(getStatisticsUseCase, answers = false)

        viewModel.onNextPeriod()
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.periodOffset)
        coVerify(exactly = 0) { getStatisticsUseCase.getStatistics(any()) }
    }

    @Test
    fun `onNextPeriod should decrease offset when currently in history`() = runTest {
        val viewModel = StatisticsViewModel(getStatisticsUseCase)
        advanceUntilIdle()
        viewModel.onPreviousPeriod()
        advanceUntilIdle()
        clearMocks(getStatisticsUseCase, answers = false)

        viewModel.onNextPeriod()
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.periodOffset)
        coVerify {
            getStatisticsUseCase.getStatistics(
                match {
                    it.period == StatisticsPeriod.MONTHLY &&
                        it.offset == 0
                }
            )
        }
    }

    @Test
    fun `onPeriodSelected should reset offset customRange and selected categories`() = runTest {
        val viewModel = StatisticsViewModel(getStatisticsUseCase)
        advanceUntilIdle()

        viewModel.onPreviousPeriod()
        advanceUntilIdle()
        viewModel.onExpenseCategorySelected(101L)
        viewModel.onIncomeCategorySelected(202L)
        viewModel.onCustomRangeConfirmed(10L, 20L)
        advanceUntilIdle()
        clearMocks(getStatisticsUseCase, answers = false)

        viewModel.onPeriodSelected(StatisticsPeriod.WEEKLY)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(StatisticsPeriod.WEEKLY, state.selectedPeriod)
        assertEquals(0, state.periodOffset)
        assertNull(state.customRange)
        assertNull(state.selectedExpenseCategoryId)
        assertNull(state.selectedIncomeCategoryId)
        coVerify {
            getStatisticsUseCase.getStatistics(
                match {
                    it.period == StatisticsPeriod.WEEKLY &&
                        it.offset == 0 &&
                        it.customRange == null
                }
            )
        }
    }

    @Test
    fun `onCustomRangeConfirmed should switch to custom period and request custom query`() = runTest {
        val viewModel = StatisticsViewModel(getStatisticsUseCase)
        advanceUntilIdle()
        clearMocks(getStatisticsUseCase, answers = false)

        viewModel.onCustomRangeClick()
        assertTrue(viewModel.uiState.value.showCustomRangePicker)

        viewModel.onCustomRangeConfirmed(100L, 200L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(StatisticsPeriod.CUSTOM, state.selectedPeriod)
        assertEquals(0, state.periodOffset)
        assertNotNull(state.customRange)
        assertFalse(state.showCustomRangePicker)
        assertEquals(100L, state.customRange?.startUtcDateMillis)
        assertEquals(200L, state.customRange?.endUtcDateMillis)
        coVerify {
            getStatisticsUseCase.getStatistics(
                match {
                    it.period == StatisticsPeriod.CUSTOM &&
                        it.customRange == CustomDateRange(100L, 200L)
                }
            )
        }
    }

    @Test
    fun `refresh should keep current custom query`() = runTest {
        val viewModel = StatisticsViewModel(getStatisticsUseCase)
        advanceUntilIdle()

        viewModel.onCustomRangeConfirmed(1000L, 2000L)
        advanceUntilIdle()
        clearMocks(getStatisticsUseCase, answers = false)

        viewModel.refresh()
        advanceUntilIdle()

        coVerify {
            getStatisticsUseCase.getStatistics(
                match {
                    it.period == StatisticsPeriod.CUSTOM &&
                        it.offset == 0 &&
                        it.customRange == CustomDateRange(1000L, 2000L)
                }
            )
        }
    }

    private fun createStatisticsResult(query: StatisticsQuery): StatisticsResult {
        return StatisticsResult(
            period = query.period,
            startDate = 1L,
            endDate = 2L,
            totalIncome = 0L,
            totalExpense = 0L,
            balance = 0L,
            incomeCategorySummaries = emptyList(),
            expenseCategorySummaries = emptyList(),
            dailySummaries = emptyList()
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
