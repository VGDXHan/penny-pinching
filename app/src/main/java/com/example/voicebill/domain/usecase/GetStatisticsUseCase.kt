package com.example.voicebill.domain.usecase

import com.example.voicebill.data.local.dao.CategoryDao
import com.example.voicebill.data.local.dao.CategoryTotal
import com.example.voicebill.data.local.dao.TransactionDao
import com.example.voicebill.domain.model.CategorySummary
import com.example.voicebill.domain.model.DailySummary
import com.example.voicebill.domain.model.StatisticsPeriod
import com.example.voicebill.domain.model.StatisticsResult
import com.example.voicebill.domain.model.TransactionType
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class GetStatisticsUseCase @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val clock: Clock = Clock.systemDefaultZone()
) {

    companion object {
        private const val DAY_MILLIS = 24 * 60 * 60 * 1000L
        private const val WEEK_MILLIS = 7 * DAY_MILLIS
    }

    suspend fun getStatistics(period: StatisticsPeriod): StatisticsResult {
        val (startDate, endDate) = getDateRange(period)

        // 获取总收入和总支出
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

        // 获取分类汇总
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

        // 获取每日汇总
        val dailyTotals = transactionDao.getDailyTotals(startDate, endDate, DAY_MILLIS)
        val dailySummaries = dailyTotals.map { dt ->
            DailySummary(
                date = dt.dayStart,
                incomeCents = dt.income,
                expenseCents = dt.expense
            )
        }

        return StatisticsResult(
            period = period,
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

    private fun getDateRange(period: StatisticsPeriod): Pair<Long, Long> {
        val now = LocalDate.now(clock.withZone(ZoneId.systemDefault()))
        val zone = ZoneId.systemDefault()

        return when (period) {
            StatisticsPeriod.DAILY -> {
                val startOfDay = now.atStartOfDay(zone).toInstant().toEpochMilli()
                val endOfDay = now.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
                Pair(startOfDay, endOfDay)
            }
            StatisticsPeriod.WEEKLY -> {
                // 本周从周一开始
                val startOfWeek = now.minusDays(now.dayOfWeek.value.toLong() - 1)
                    .atStartOfDay(zone).toInstant().toEpochMilli()
                val endOfWeek = startOfWeek + WEEK_MILLIS
                Pair(startOfWeek, endOfWeek)
            }
            StatisticsPeriod.MONTHLY -> {
                val startOfMonth = now.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
                val endOfMonth = now.plusMonths(1).withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
                Pair(startOfMonth, endOfMonth)
            }
            StatisticsPeriod.YEARLY -> {
                val startOfYear = now.withDayOfYear(1).atStartOfDay(zone).toInstant().toEpochMilli()
                val endOfYear = now.plusYears(1).withDayOfYear(1).atStartOfDay(zone).toInstant().toEpochMilli()
                Pair(startOfYear, endOfYear)
            }
        }
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
}
