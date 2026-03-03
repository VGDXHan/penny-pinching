package com.example.voicebill.domain.usecase

import com.example.voicebill.data.local.dao.CategoryTotal
import com.example.voicebill.data.local.dao.TransactionDao
import com.example.voicebill.domain.model.CategorySummary
import com.example.voicebill.domain.model.DailySummary
import com.example.voicebill.domain.model.StatisticsQuery
import com.example.voicebill.domain.model.StatisticsResult
import com.example.voicebill.domain.model.TransactionType
import javax.inject.Inject

class GetStatisticsUseCase @Inject constructor(
    private val transactionDao: TransactionDao,
    private val statisticsDateRangeResolver: StatisticsDateRangeResolver
) {

    suspend fun getStatistics(query: StatisticsQuery): StatisticsResult {
        val dateRange = statisticsDateRangeResolver.resolve(query)
        val startDate = dateRange.startDate
        val endDate = dateRange.endDate

        val totalIncome = transactionDao.getTotalAmountByTypeAndDateRange(
            TransactionType.INCOME.name,
            startDate,
            endDate
        ) ?: 0L

        val totalExpense = transactionDao.getTotalAmountByTypeAndDateRange(
            TransactionType.EXPENSE.name,
            startDate,
            endDate
        ) ?: 0L

        val incomeCategoryTotals = transactionDao.getCategoryTotalsByTypeAndDateRange(
            TransactionType.INCOME.name,
            startDate,
            endDate
        )

        val expenseCategoryTotals = transactionDao.getCategoryTotalsByTypeAndDateRange(
            TransactionType.EXPENSE.name,
            startDate,
            endDate
        )

        val incomeCategorySummaries = buildCategorySummaries(incomeCategoryTotals)
        val expenseCategorySummaries = buildCategorySummaries(expenseCategoryTotals)

        val dailyTotals = transactionDao.getDailyTotals(startDate, endDate, DAY_MILLIS)
        val dailySummaries = dailyTotals.map { dt ->
            DailySummary(
                date = dt.dayStart,
                incomeCents = dt.income,
                expenseCents = dt.expense
            )
        }

        return StatisticsResult(
            period = query.period,
            startDate = startDate,
            endDate = endDate,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            balance = totalIncome - totalExpense,
            incomeCategorySummaries = incomeCategorySummaries,
            expenseCategorySummaries = expenseCategorySummaries,
            dailySummaries = dailySummaries
        )
    }

    private fun buildCategorySummaries(categoryTotals: List<CategoryTotal>): List<CategorySummary> {
        val positiveCategoryTotals = categoryTotals.filter { it.total > 0 }
        val positiveTotalAmount = positiveCategoryTotals.sumOf { it.total }

        if (positiveTotalAmount <= 0L) return emptyList()

        return positiveCategoryTotals.map { ct ->
            val rawPercentage = ct.total.toFloat() / positiveTotalAmount.toFloat()
            val safePercentage = if (rawPercentage.isFinite()) {
                rawPercentage.coerceIn(0f, 1f)
            } else {
                0f
            }
            CategorySummary(
                categoryId = ct.categoryId ?: 0,
                categoryName = ct.categoryNameSnapshot,
                amountCents = ct.total,
                percentage = safePercentage
            )
        }
    }

    companion object {
        private const val DAY_MILLIS = 24 * 60 * 60 * 1000L
    }
}
