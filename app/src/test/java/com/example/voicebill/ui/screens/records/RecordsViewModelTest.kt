package com.example.voicebill.ui.screens.records

import com.example.voicebill.domain.model.Category
import com.example.voicebill.domain.model.Transaction
import com.example.voicebill.domain.model.TransactionType
import com.example.voicebill.domain.repository.CategoryRepository
import com.example.voicebill.domain.repository.TransactionRepository
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
class RecordsViewModelTest {

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

    private val sampleTransaction = Transaction(
        id = 10L,
        amountCents = 4580L,
        categoryId = expenseCategory.id,
        categoryNameSnapshot = expenseCategory.name,
        type = TransactionType.EXPENSE,
        date = 1_700_000_000_000L,
        note = "早餐云吞",
        rawText = "早餐云吞"
    )

    @Test
    fun startEditing_shouldFillEditFields() = runTest {
        val transactionRepository = FakeTransactionRepository(listOf(sampleTransaction))
        val categoryRepository = FakeCategoryRepository(listOf(expenseCategory, incomeCategory))
        val viewModel = RecordsViewModel(transactionRepository, categoryRepository)
        advanceUntilIdle()

        viewModel.startEditing(sampleTransaction)

        val state = viewModel.uiState.value
        assertEquals(sampleTransaction.id, state.editingTransaction?.id)
        assertEquals(sampleTransaction.amountCents, state.editAmount)
        assertEquals(sampleTransaction.categoryId, state.editCategoryId)
        assertEquals(sampleTransaction.type, state.editType)
    }

    @Test
    fun onEditTypeSelected_shouldClearMismatchedCategory() = runTest {
        val transactionRepository = FakeTransactionRepository(listOf(sampleTransaction))
        val categoryRepository = FakeCategoryRepository(listOf(expenseCategory, incomeCategory))
        val viewModel = RecordsViewModel(transactionRepository, categoryRepository)
        advanceUntilIdle()
        viewModel.startEditing(sampleTransaction)

        viewModel.onEditTypeSelected(TransactionType.INCOME)

        assertNull(viewModel.uiState.value.editCategoryId)
    }

    @Test
    fun saveEditedTransaction_whenAmountInvalid_shouldSetError() = runTest {
        val transactionRepository = FakeTransactionRepository(listOf(sampleTransaction))
        val categoryRepository = FakeCategoryRepository(listOf(expenseCategory, incomeCategory))
        val viewModel = RecordsViewModel(transactionRepository, categoryRepository)
        advanceUntilIdle()
        viewModel.startEditing(sampleTransaction)
        viewModel.onEditAmountChanged(0L)

        viewModel.saveEditedTransaction()

        assertEquals("请输入有效金额", viewModel.uiState.value.error)
        assertTrue(transactionRepository.updatedTransactions.isEmpty())
    }

    @Test
    fun saveEditedTransaction_whenCategoryMissing_shouldSetError() = runTest {
        val transactionRepository = FakeTransactionRepository(listOf(sampleTransaction))
        val categoryRepository = FakeCategoryRepository(listOf(expenseCategory, incomeCategory))
        val viewModel = RecordsViewModel(transactionRepository, categoryRepository)
        advanceUntilIdle()
        viewModel.startEditing(sampleTransaction)
        viewModel.onEditCategorySelected(expenseCategory.id)
        viewModel.onEditTypeSelected(TransactionType.INCOME)

        viewModel.saveEditedTransaction()

        assertEquals("请选择分类", viewModel.uiState.value.error)
        assertTrue(transactionRepository.updatedTransactions.isEmpty())
    }

    @Test
    fun saveEditedTransaction_withValidInput_shouldUpdateAndCloseEditing() = runTest {
        val transactionRepository = FakeTransactionRepository(listOf(sampleTransaction))
        val categoryRepository = FakeCategoryRepository(listOf(expenseCategory, incomeCategory))
        val viewModel = RecordsViewModel(transactionRepository, categoryRepository)
        advanceUntilIdle()
        viewModel.startEditing(sampleTransaction)
        viewModel.onEditAmountChanged(5200L)
        viewModel.onEditCategorySelected(incomeCategory.id)
        viewModel.onEditTypeSelected(TransactionType.INCOME)
        viewModel.onEditNoteChanged("工资补发")

        viewModel.saveEditedTransaction()
        advanceUntilIdle()

        val updated = transactionRepository.updatedTransactions.single()
        assertEquals(5200L, updated.amountCents)
        assertEquals(incomeCategory.id, updated.categoryId)
        assertEquals(incomeCategory.name, updated.categoryNameSnapshot)
        assertEquals(TransactionType.INCOME, updated.type)
        assertEquals("工资补发", updated.note)
        assertNull(viewModel.uiState.value.editingTransaction)
    }

    @Test
    fun deleteTransaction_shouldCallRepositoryDelete() = runTest {
        val transactionRepository = FakeTransactionRepository(listOf(sampleTransaction))
        val categoryRepository = FakeCategoryRepository(listOf(expenseCategory, incomeCategory))
        val viewModel = RecordsViewModel(transactionRepository, categoryRepository)
        advanceUntilIdle()

        viewModel.deleteTransaction(sampleTransaction)
        advanceUntilIdle()

        assertEquals(1, transactionRepository.deleteCallCount)
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

private class FakeTransactionRepository(
    initialTransactions: List<Transaction>
) : TransactionRepository {
    private val transactionsFlow = MutableStateFlow(initialTransactions)
    val updatedTransactions = mutableListOf<Transaction>()
    var deleteCallCount: Int = 0

    override fun getAllTransactions(): Flow<List<Transaction>> = transactionsFlow

    override fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> =
        flowOf(emptyList())

    override fun getTransactionsByCategory(categoryId: Long): Flow<List<Transaction>> = flowOf(emptyList())

    override fun searchTransactions(keyword: String): Flow<List<Transaction>> = flowOf(emptyList())

    override suspend fun getTransactionById(id: Long): Transaction? =
        transactionsFlow.value.find { it.id == id }

    override suspend fun insertTransaction(transaction: Transaction): Long = 1L

    override suspend fun updateTransaction(transaction: Transaction) {
        updatedTransactions += transaction
        transactionsFlow.value = transactionsFlow.value.map {
            if (it.id == transaction.id) transaction else it
        }
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        deleteCallCount += 1
        transactionsFlow.value = transactionsFlow.value.filterNot { it.id == transaction.id }
    }

    override suspend fun deleteAllTransactions() = Unit
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
