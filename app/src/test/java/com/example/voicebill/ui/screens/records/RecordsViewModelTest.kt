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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import kotlin.math.abs

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

    @Test
    fun startCreating_sets_expected_defaults() = runTest {
        val transactionRepository = FakeTransactionRepository()
        val categoryRepository = FakeCategoryRepository(listOf(expenseCategory, incomeCategory))
        val viewModel = RecordsViewModel(transactionRepository, categoryRepository)
        advanceUntilIdle()

        viewModel.startCreating()

        val state = viewModel.uiState.value
        assertTrue(state.isCreating)
        assertEquals(TransactionType.EXPENSE, state.createType)
        assertNull(state.createCategoryId)
        assertEquals(0L, state.createAmount)
        assertEquals("", state.createNote)
        assertTrue(abs(System.currentTimeMillis() - state.createDate) < 5_000)
    }

    @Test
    fun saveCreatedTransaction_whenAmountInvalid_setsErrorAndDoesNotInsert() = runTest {
        val transactionRepository = FakeTransactionRepository()
        val categoryRepository = FakeCategoryRepository(listOf(expenseCategory, incomeCategory))
        val viewModel = RecordsViewModel(transactionRepository, categoryRepository)
        advanceUntilIdle()
        viewModel.startCreating()
        viewModel.onCreateCategorySelected(expenseCategory.id)

        viewModel.saveCreatedTransaction()

        assertEquals("请输入有效金额", viewModel.uiState.value.error)
        assertEquals(0, transactionRepository.insertCallCount)
    }

    @Test
    fun saveCreatedTransaction_whenCategoryMissing_setsErrorAndDoesNotInsert() = runTest {
        val transactionRepository = FakeTransactionRepository()
        val categoryRepository = FakeCategoryRepository(listOf(expenseCategory, incomeCategory))
        val viewModel = RecordsViewModel(transactionRepository, categoryRepository)
        advanceUntilIdle()
        viewModel.startCreating()
        viewModel.onCreateAmountChanged(1200L)

        viewModel.saveCreatedTransaction()

        assertEquals("请选择分类", viewModel.uiState.value.error)
        assertEquals(0, transactionRepository.insertCallCount)
    }

    @Test
    fun saveCreatedTransaction_withNote_usesNoteAsRawText() = runTest {
        val transactionRepository = FakeTransactionRepository()
        val categoryRepository = FakeCategoryRepository(listOf(expenseCategory, incomeCategory))
        val viewModel = RecordsViewModel(transactionRepository, categoryRepository)
        advanceUntilIdle()
        viewModel.startCreating()
        viewModel.onCreateTypeSelected(TransactionType.EXPENSE)
        viewModel.onCreateAmountChanged(12800L)
        viewModel.onCreateCategorySelected(expenseCategory.id)
        viewModel.onCreateNoteChanged("和同事吃火锅")

        viewModel.saveCreatedTransaction()
        advanceUntilIdle()

        assertEquals(1, transactionRepository.insertCallCount)
        val inserted = transactionRepository.insertedTransaction
        assertNotNull(inserted)
        assertEquals("和同事吃火锅", inserted?.note)
        assertEquals("和同事吃火锅", inserted?.rawText)
        assertFalse(viewModel.uiState.value.isCreating)
    }

    @Test
    fun saveCreatedTransaction_withoutNote_setsManualRawTextFallback() = runTest {
        val transactionRepository = FakeTransactionRepository()
        val categoryRepository = FakeCategoryRepository(listOf(expenseCategory, incomeCategory))
        val viewModel = RecordsViewModel(transactionRepository, categoryRepository)
        advanceUntilIdle()
        viewModel.startCreating()
        viewModel.onCreateTypeSelected(TransactionType.EXPENSE)
        viewModel.onCreateAmountChanged(5000L)
        viewModel.onCreateCategorySelected(expenseCategory.id)
        viewModel.onCreateNoteChanged("")

        viewModel.saveCreatedTransaction()
        advanceUntilIdle()

        assertEquals(1, transactionRepository.insertCallCount)
        val inserted = transactionRepository.insertedTransaction
        assertNotNull(inserted)
        assertNull(inserted?.note)
        assertEquals("手动记账", inserted?.rawText)
    }

    @Test
    fun onCreateTypeSelected_clearsCategoryWhenTypeMismatched() = runTest {
        val transactionRepository = FakeTransactionRepository()
        val categoryRepository = FakeCategoryRepository(listOf(expenseCategory, incomeCategory))
        val viewModel = RecordsViewModel(transactionRepository, categoryRepository)
        advanceUntilIdle()
        viewModel.startCreating()
        viewModel.onCreateCategorySelected(expenseCategory.id)

        viewModel.onCreateTypeSelected(TransactionType.INCOME)

        assertNull(viewModel.uiState.value.createCategoryId)
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
    private val transactionsFlow = MutableStateFlow<List<Transaction>>(emptyList())
    var insertedTransaction: Transaction? = null
    var insertCallCount: Int = 0

    override fun getAllTransactions(): Flow<List<Transaction>> = transactionsFlow

    override fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> =
        flowOf(emptyList())

    override fun getTransactionsByCategory(categoryId: Long): Flow<List<Transaction>> = flowOf(emptyList())

    override fun searchTransactions(keyword: String): Flow<List<Transaction>> = flowOf(emptyList())

    override suspend fun getTransactionById(id: Long): Transaction? = null

    override suspend fun insertTransaction(transaction: Transaction): Long {
        insertCallCount += 1
        insertedTransaction = transaction
        transactionsFlow.value = transactionsFlow.value + transaction.copy(id = insertCallCount.toLong())
        return insertCallCount.toLong()
    }

    override suspend fun updateTransaction(transaction: Transaction) = Unit

    override suspend fun deleteTransaction(transaction: Transaction) = Unit

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
