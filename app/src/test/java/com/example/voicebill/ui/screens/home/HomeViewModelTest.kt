package com.example.voicebill.ui.screens.home

import com.example.voicebill.di.SecurePrefs
import com.example.voicebill.domain.model.BillInfo
import com.example.voicebill.domain.model.Category
import com.example.voicebill.domain.model.TransactionType
import com.example.voicebill.domain.repository.BillParserRepository
import com.example.voicebill.domain.repository.CategoryRepository
import com.example.voicebill.domain.repository.TransactionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class HomeViewModelTest {

    // 统一替换 Main dispatcher，确保 viewModelScope 在测试线程执行。
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val billParserRepository: BillParserRepository = mockk()
    private val transactionRepository: TransactionRepository = mockk(relaxed = true)
    private val categoryRepository: CategoryRepository = mockk()
    private val securePrefs: SecurePrefs = mockk()

    private val expenseUncategorized = Category(
        id = 100L,
        name = "未分类",
        icon = "help_outline",
        color = "#9E9E9E",
        isIncome = false,
        isDefault = true,
        isUncategorized = true
    )

    private val incomeUncategorized = Category(
        id = 200L,
        name = "未分类",
        icon = "help_outline",
        color = "#9E9E9E",
        isIncome = true,
        isDefault = true,
        isUncategorized = true
    )

    private val transportCategory = Category(
        id = 1L,
        name = "交通",
        icon = "directions_bus",
        color = "#2196F3",
        isIncome = false
    )

    @Before
    fun setUp() {
        every { securePrefs.hasApiKey() } returns true
    }

    @Test
    fun `parseText 在支出且无分类时应回退到支出未分类`() = runTest {
        val viewModel = createViewModel(
            listOf(expenseUncategorized, incomeUncategorized, transportCategory)
        )
        coEvery { billParserRepository.parseBillText(any()) } returns BillInfo(
            amountCents = 4580L,
            categoryName = null,
            type = TransactionType.EXPENSE,
            date = 1_700_000_000_000L,
            note = "早餐云吞",
            parseSuccess = true
        )
        coEvery { categoryRepository.getUncategorizedCategory(false) } returns expenseUncategorized

        viewModel.onInputTextChanged("早饭 45.8")
        viewModel.parseText()
        advanceUntilIdle()

        assertEquals(expenseUncategorized.id, viewModel.uiState.value.selectedCategoryId)
    }

    @Test
    fun `parseText 在收入且无分类时应回退到收入未分类`() = runTest {
        val viewModel = createViewModel(
            listOf(expenseUncategorized, incomeUncategorized, transportCategory)
        )
        coEvery { billParserRepository.parseBillText(any()) } returns BillInfo(
            amountCents = 120000L,
            categoryName = null,
            type = TransactionType.INCOME,
            date = 1_700_000_000_000L,
            note = "兼职收入",
            parseSuccess = true
        )
        coEvery { categoryRepository.getUncategorizedCategory(true) } returns incomeUncategorized

        viewModel.onInputTextChanged("兼职赚了1200")
        viewModel.parseText()
        advanceUntilIdle()

        assertEquals(incomeUncategorized.id, viewModel.uiState.value.selectedCategoryId)
    }

    @Test
    fun `parseText 在分类可匹配时应优先使用匹配分类`() = runTest {
        val viewModel = createViewModel(
            listOf(expenseUncategorized, incomeUncategorized, transportCategory)
        )
        coEvery { billParserRepository.parseBillText(any()) } returns BillInfo(
            amountCents = 3000L,
            categoryName = "交通",
            type = TransactionType.EXPENSE,
            date = 1_700_000_000_000L,
            note = "打车",
            parseSuccess = true
        )

        viewModel.onInputTextChanged("打车 30")
        viewModel.parseText()
        advanceUntilIdle()

        assertEquals(transportCategory.id, viewModel.uiState.value.selectedCategoryId)
        coVerify(exactly = 0) { categoryRepository.getUncategorizedCategory(any()) }
    }

    @Test
    fun `parseText 在分类名无法匹配时应回退到未分类`() = runTest {
        val viewModel = createViewModel(
            listOf(expenseUncategorized, incomeUncategorized, transportCategory)
        )
        coEvery { billParserRepository.parseBillText(any()) } returns BillInfo(
            amountCents = 9800L,
            categoryName = "不存在分类",
            type = TransactionType.EXPENSE,
            date = 1_700_000_000_000L,
            note = "未知消费",
            parseSuccess = true
        )
        coEvery { categoryRepository.getUncategorizedCategory(false) } returns expenseUncategorized

        viewModel.onInputTextChanged("消费 98")
        viewModel.parseText()
        advanceUntilIdle()

        assertEquals(expenseUncategorized.id, viewModel.uiState.value.selectedCategoryId)
    }

    @Test
    fun `parseText 在未分类缺失时应保持未选中`() = runTest {
        val viewModel = createViewModel(
            listOf(expenseUncategorized, incomeUncategorized, transportCategory)
        )
        coEvery { billParserRepository.parseBillText(any()) } returns BillInfo(
            amountCents = 9800L,
            categoryName = null,
            type = TransactionType.EXPENSE,
            date = 1_700_000_000_000L,
            note = "未知消费",
            parseSuccess = true
        )
        coEvery { categoryRepository.getUncategorizedCategory(false) } returns null

        viewModel.onInputTextChanged("消费 98")
        viewModel.parseText()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.selectedCategoryId)
    }

    private fun createViewModel(categories: List<Category>): HomeViewModel {
        every { categoryRepository.getAllCategories() } returns MutableStateFlow(categories)

        return HomeViewModel(
            billParserRepository = billParserRepository,
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            securePrefs = securePrefs
        )
    }
}

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
