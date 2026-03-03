package com.example.voicebill.ui.screens.statistics

import com.example.voicebill.domain.model.Category
import com.example.voicebill.domain.model.CategorySummary
import com.example.voicebill.domain.model.StatisticsPeriod
import com.example.voicebill.domain.model.StatisticsResult
import com.example.voicebill.domain.model.Transaction
import com.example.voicebill.domain.model.TransactionType
import com.example.voicebill.domain.repository.CategoryRepository
import com.example.voicebill.domain.repository.TransactionRepository
import com.example.voicebill.domain.usecase.GetStatisticsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
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

    private val expenseCategory = Category(
        id = 1L,
        name = "餐饮",
        icon = "food",
        color = "#FF0000",
        isIncome = false
    )

    private val incomeCategory = Category(
        id = 2L,
        name = "工资",
        icon = "salary",
        color = "#00FF00",
        isIncome = true
    )

    private val sampleExpenseTransaction = Transaction(
        id = 101L,
        amountCents = 1800L,
        categoryId = expenseCategory.id,
        categoryNameSnapshot = expenseCategory.name,
        type = TransactionType.EXPENSE,
        date = 1500L,
        note = "午餐",
        rawText = "午餐"
    )

    private val monthlyStats = StatisticsResult(
        period = StatisticsPeriod.MONTHLY,
        startDate = 1000L,
        endDate = 2000L,
        totalIncome = 500000L,
        totalExpense = 200000L,
        balance = 300000L,
        incomeCategorySummaries = listOf(
            CategorySummary(
                categoryId = incomeCategory.id,
                categoryName = incomeCategory.name,
                amountCents = 500000L,
                percentage = 1f
            )
        ),
        expenseCategorySummaries = listOf(
            CategorySummary(
                categoryId = expenseCategory.id,
                categoryName = expenseCategory.name,
                amountCents = 200000L,
                percentage = 1f
            )
        ),
        dailySummaries = emptyList()
    )

    private val weeklyStats = monthlyStats.copy(
        period = StatisticsPeriod.WEEKLY,
        startDate = 2000L,
        endDate = 3000L
    )

    @Test
    fun onExpenseCategorySelected_shouldLoadCategoryTransactionsByCurrentPeriod() = runTest {
        val useCase = mockk<GetStatisticsUseCase>()
        coEvery { useCase.getStatistics(any()) } returns monthlyStats

        val transactionRepository = FakeTransactionRepository()
        transactionRepository.setCategoryRangeTransactions(
            categoryId = expenseCategory.id,
            startDate = monthlyStats.startDate,
            endDate = monthlyStats.endDate,
            transactions = listOf(sampleExpenseTransaction)
        )
        val categoryRepository = FakeCategoryRepository(listOf(expenseCategory, incomeCategory))

        val viewModel = StatisticsViewModel(useCase, transactionRepository, categoryRepository)
        advanceUntilIdle()

        viewModel.onExpenseCategorySelected(expenseCategory.id)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(expenseCategory.id, state.selectedExpenseCategoryId)
        assertEquals(1, state.expenseCategoryTransactions.size)
        assertEquals(
            Triple(expenseCategory.id, monthlyStats.startDate, monthlyStats.endDate),
            transactionRepository.lastCategoryRangeRequest
        )
    }

    @Test
    fun onExpenseCategorySelected_whenClickSameCategory_shouldCollapseDetails() = runTest {
        val useCase = mockk<GetStatisticsUseCase>()
        coEvery { useCase.getStatistics(any()) } returns monthlyStats

        val transactionRepository = FakeTransactionRepository()
        val categoryRepository = FakeCategoryRepository(listOf(expenseCategory, incomeCategory))
        val viewModel = StatisticsViewModel(useCase, transactionRepository, categoryRepository)
        advanceUntilIdle()

        viewModel.onExpenseCategorySelected(expenseCategory.id)
        advanceUntilIdle()
        viewModel.onExpenseCategorySelected(expenseCategory.id)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.selectedExpenseCategoryId)
        assertTrue(state.expenseCategoryTransactions.isEmpty())
    }

    @Test
    fun onPeriodSelected_shouldResetExpandedCategoryAndDetails() = runTest {
        val useCase = mockk<GetStatisticsUseCase>()
        coEvery { useCase.getStatistics(StatisticsPeriod.MONTHLY) } returns monthlyStats
        coEvery { useCase.getStatistics(StatisticsPeriod.WEEKLY) } returns weeklyStats

        val transactionRepository = FakeTransactionRepository()
        transactionRepository.setCategoryRangeTransactions(
            categoryId = expenseCategory.id,
            startDate = monthlyStats.startDate,
            endDate = monthlyStats.endDate,
            transactions = listOf(sampleExpenseTransaction)
        )
        val categoryRepository = FakeCategoryRepository(listOf(expenseCategory, incomeCategory))
        val viewModel = StatisticsViewModel(useCase, transactionRepository, categoryRepository)
        advanceUntilIdle()

        viewModel.onExpenseCategorySelected(expenseCategory.id)
        advanceUntilIdle()

        viewModel.onPeriodSelected(StatisticsPeriod.WEEKLY)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(StatisticsPeriod.WEEKLY, state.selectedPeriod)
        assertEquals(StatisticsPeriod.WEEKLY, state.statistics?.period)
        assertNull(state.selectedExpenseCategoryId)
        assertTrue(state.expenseCategoryTransactions.isEmpty())
    }

    @Test
    fun saveEditedTransaction_withValidInput_shouldUpdateTransactionAndRefreshStatistics() = runTest {
        val useCase = mockk<GetStatisticsUseCase>()
        coEvery { useCase.getStatistics(any()) } returns monthlyStats

        val transactionRepository = FakeTransactionRepository()
        val categoryRepository = FakeCategoryRepository(listOf(expenseCategory, incomeCategory))
        val viewModel = StatisticsViewModel(useCase, transactionRepository, categoryRepository)
        advanceUntilIdle()

        viewModel.startEditing(sampleExpenseTransaction)
        viewModel.onEditAmountChanged(5200L)
        viewModel.onEditCategorySelected(expenseCategory.id)
        viewModel.onEditNoteChanged("晚餐")

        viewModel.saveEditedTransaction()
        advanceUntilIdle()

        val updated = transactionRepository.updatedTransactions.single()
        assertEquals(5200L, updated.amountCents)
        assertEquals("晚餐", updated.note)
        assertNull(viewModel.uiState.value.editingTransaction)
        coVerify(atLeast = 2) { useCase.getStatistics(StatisticsPeriod.MONTHLY) }
    }

    @Test
    fun saveEditedTransaction_whenAmountInvalid_shouldSetError() = runTest {
        val useCase = mockk<GetStatisticsUseCase>()
        coEvery { useCase.getStatistics(any()) } returns monthlyStats

        val transactionRepository = FakeTransactionRepository()
        val categoryRepository = FakeCategoryRepository(listOf(expenseCategory, incomeCategory))
        val viewModel = StatisticsViewModel(useCase, transactionRepository, categoryRepository)
        advanceUntilIdle()

        viewModel.startEditing(sampleExpenseTransaction)
        viewModel.onEditAmountChanged(0L)

        viewModel.saveEditedTransaction()

        assertEquals("请输入有效金额", viewModel.uiState.value.error)
        assertTrue(transactionRepository.updatedTransactions.isEmpty())
    }

    @Test
    fun deleteTransaction_shouldCallRepositoryAndRefreshStatistics() = runTest {
        val useCase = mockk<GetStatisticsUseCase>()
        coEvery { useCase.getStatistics(any()) } returns monthlyStats

        val transactionRepository = FakeTransactionRepository()
        val categoryRepository = FakeCategoryRepository(listOf(expenseCategory, incomeCategory))
        val viewModel = StatisticsViewModel(useCase, transactionRepository, categoryRepository)
        advanceUntilIdle()

        viewModel.deleteTransaction(sampleExpenseTransaction)
        advanceUntilIdle()

        assertEquals(1, transactionRepository.deleteCallCount)
        coVerify(atLeast = 2) { useCase.getStatistics(StatisticsPeriod.MONTHLY) }
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

private class FakeTransactionRepository : TransactionRepository {
    private val allTransactionsFlow = MutableStateFlow(emptyList<Transaction>())
    private val categoryRangeFlows = mutableMapOf<String, MutableStateFlow<List<Transaction>>>()

    val updatedTransactions = mutableListOf<Transaction>()
    var deleteCallCount: Int = 0
    var lastCategoryRangeRequest: Triple<Long, Long, Long>? = null

    fun setCategoryRangeTransactions(
        categoryId: Long,
        startDate: Long,
        endDate: Long,
        transactions: List<Transaction>
    ) {
        categoryRangeFlows[buildRangeKey(categoryId, startDate, endDate)] = MutableStateFlow(transactions)
    }

    override fun getAllTransactions(): Flow<List<Transaction>> = allTransactionsFlow

    override fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> =
        flowOf(emptyList())

    override fun getTransactionsByCategory(categoryId: Long): Flow<List<Transaction>> = flowOf(emptyList())

    override fun getTransactionsByCategoryAndDateRange(
        categoryId: Long,
        startDate: Long,
        endDate: Long
    ): Flow<List<Transaction>> {
        lastCategoryRangeRequest = Triple(categoryId, startDate, endDate)
        return categoryRangeFlows.getOrPut(buildRangeKey(categoryId, startDate, endDate)) {
            MutableStateFlow(emptyList())
        }
    }

    override fun searchTransactions(keyword: String): Flow<List<Transaction>> = flowOf(emptyList())

    override suspend fun getTransactionById(id: Long): Transaction? =
        allTransactionsFlow.value.find { it.id == id }

    override suspend fun insertTransaction(transaction: Transaction): Long = transaction.id

    override suspend fun updateTransaction(transaction: Transaction) {
        updatedTransactions += transaction
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        deleteCallCount += 1
    }

    override suspend fun deleteAllTransactions() = Unit

    private fun buildRangeKey(categoryId: Long, startDate: Long, endDate: Long): String {
        return "$categoryId-$startDate-$endDate"
    }
}

private class FakeCategoryRepository(
    initialCategories: List<Category>
) : CategoryRepository {
    private val categoriesFlow = MutableStateFlow(initialCategories)

    override fun getAllCategories(): Flow<List<Category>> = categoriesFlow

    override fun getExpenseCategories(): Flow<List<Category>> =
        flowOf(categoriesFlow.value.filter { !it.isIncome })

    override fun getIncomeCategories(): Flow<List<Category>> =
        flowOf(categoriesFlow.value.filter { it.isIncome })

    override suspend fun getCategoryById(id: Long): Category? = categoriesFlow.value.find { it.id == id }

    override suspend fun getUncategorizedCategory(isIncome: Boolean): Category? =
        categoriesFlow.value.find { it.isIncome == isIncome && it.isUncategorized }

    override suspend fun insertCategory(category: Category): Long {
        categoriesFlow.value = categoriesFlow.value + category
        return category.id
    }

    override suspend fun updateCategory(category: Category) = Unit

    override suspend fun deleteCategory(id: Long) = Unit

    override suspend fun deleteCategoryWithMigration(id: Long) = Unit

    override suspend fun restoreCategory(id: Long) = Unit
}

