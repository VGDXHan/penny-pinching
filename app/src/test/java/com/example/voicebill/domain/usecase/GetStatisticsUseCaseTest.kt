package com.example.voicebill.domain.usecase

import com.example.voicebill.data.local.dao.CategoryTotal
import com.example.voicebill.data.local.dao.DailyTotal
import com.example.voicebill.data.local.dao.TransactionDao
import com.example.voicebill.domain.model.CustomDateRange
import com.example.voicebill.domain.model.StatisticsPeriod
import com.example.voicebill.domain.model.StatisticsQuery
import com.example.voicebill.domain.model.TransactionType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class GetStatisticsUseCaseTest {

    private val transactionDao: TransactionDao = mockk()
    private val zone = ZoneId.of("Asia/Shanghai")
    private val fixedClock = Clock.fixed(Instant.parse("2026-03-03T10:15:30Z"), zone)

    private lateinit var useCase: GetStatisticsUseCase

    @Before
    fun setUp() {
        stubDefaultDaoResponses()
        useCase = GetStatisticsUseCase(
            transactionDao = transactionDao,
            statisticsDateRangeResolver = StatisticsDateRangeResolver(fixedClock)
        )
    }

    @Test
    fun `DAILY offset=2 should query two days before current day`() = runTest {
        useCase.getStatistics(
            StatisticsQuery(
                period = StatisticsPeriod.DAILY,
                offset = 2
            )
        )

        val expectedStart = LocalDate.of(2026, 3, 1)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()
        val expectedEnd = LocalDate.of(2026, 3, 2)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

        verifyRange(expectedStart, expectedEnd)
    }

    @Test
    fun `WEEKLY offset=1 should use previous week Monday to next Monday`() = runTest {
        useCase.getStatistics(
            StatisticsQuery(
                period = StatisticsPeriod.WEEKLY,
                offset = 1
            )
        )

        val expectedStart = LocalDate.of(2026, 2, 23)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()
        val expectedEnd = LocalDate.of(2026, 3, 2)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

        verifyRange(expectedStart, expectedEnd)
    }

    @Test
    fun `MONTHLY offset=2 should cross year correctly`() = runTest {
        useCase.getStatistics(
            StatisticsQuery(
                period = StatisticsPeriod.MONTHLY,
                offset = 2
            )
        )

        val expectedStart = LocalDate.of(2026, 1, 1)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()
        val expectedEnd = LocalDate.of(2026, 2, 1)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

        verifyRange(expectedStart, expectedEnd)
    }

    @Test
    fun `YEARLY offset=1 should query previous year`() = runTest {
        useCase.getStatistics(
            StatisticsQuery(
                period = StatisticsPeriod.YEARLY,
                offset = 1
            )
        )

        val expectedStart = LocalDate.of(2025, 1, 1)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()
        val expectedEnd = LocalDate.of(2026, 1, 1)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

        verifyRange(expectedStart, expectedEnd)
    }

    @Test
    fun `CUSTOM should convert utc date millis to local day boundaries`() = runTest {
        val customStartUtc = LocalDate.of(2026, 2, 10)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
        val customEndUtc = LocalDate.of(2026, 2, 12)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()

        useCase.getStatistics(
            StatisticsQuery(
                period = StatisticsPeriod.CUSTOM,
                customRange = CustomDateRange(
                    startUtcDateMillis = customStartUtc,
                    endUtcDateMillis = customEndUtc
                )
            )
        )

        val expectedStart = LocalDate.of(2026, 2, 10)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()
        val expectedEnd = LocalDate.of(2026, 2, 13)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

        verifyRange(expectedStart, expectedEnd)
    }

    @Test
    fun `should map totals categories and daily summaries correctly`() = runTest {
        coEvery {
            transactionDao.getTotalAmountByTypeAndDateRange(TransactionType.INCOME.name, any(), any())
        } returns 8888L
        coEvery {
            transactionDao.getTotalAmountByTypeAndDateRange(TransactionType.EXPENSE.name, any(), any())
        } returns 3000L
        coEvery {
            transactionDao.getCategoryTotalsByTypeAndDateRange(TransactionType.INCOME.name, any(), any())
        } returns listOf(
            CategoryTotal(categoryId = 10L, categoryNameSnapshot = "工资", total = 8000L),
            CategoryTotal(categoryId = 11L, categoryNameSnapshot = "红包", total = 888L)
        )
        coEvery {
            transactionDao.getCategoryTotalsByTypeAndDateRange(TransactionType.EXPENSE.name, any(), any())
        } returns listOf(
            CategoryTotal(categoryId = 20L, categoryNameSnapshot = "餐饮", total = 2500L),
            CategoryTotal(categoryId = 21L, categoryNameSnapshot = "交通", total = 500L)
        )
        coEvery { transactionDao.getDailyTotals(any(), any(), any()) } returns listOf(
            DailyTotal(dayStart = 1L, income = 500L, expense = 100L),
            DailyTotal(dayStart = 2L, income = 300L, expense = 200L)
        )

        val result = useCase.getStatistics(
            StatisticsQuery(period = StatisticsPeriod.MONTHLY)
        )

        assertEquals(StatisticsPeriod.MONTHLY, result.period)
        assertEquals(8888L, result.totalIncome)
        assertEquals(3000L, result.totalExpense)
        assertEquals(5888L, result.balance)
        assertEquals(2, result.incomeCategorySummaries.size)
        assertEquals(2, result.expenseCategorySummaries.size)
        assertEquals(2, result.dailySummaries.size)
        assertEquals(0.90009004f, result.incomeCategorySummaries.first().percentage, 0.0001f)
        assertEquals(0.8333333f, result.expenseCategorySummaries.first().percentage, 0.0001f)
    }

    private fun verifyRange(expectedStart: Long, expectedEnd: Long) {
        coVerify {
            transactionDao.getDailyTotals(expectedStart, expectedEnd, 24 * 60 * 60 * 1000L)
        }
        coVerify {
            transactionDao.getTotalAmountByTypeAndDateRange(TransactionType.INCOME.name, expectedStart, expectedEnd)
        }
        coVerify {
            transactionDao.getTotalAmountByTypeAndDateRange(TransactionType.EXPENSE.name, expectedStart, expectedEnd)
        }
    }

    private fun stubDefaultDaoResponses() {
        coEvery {
            transactionDao.getTotalAmountByTypeAndDateRange(any(), any(), any())
        } returns 0L
        coEvery {
            transactionDao.getCategoryTotalsByTypeAndDateRange(any(), any(), any())
        } returns emptyList()
        coEvery {
            transactionDao.getDailyTotals(any(), any(), any())
        } returns emptyList()
    }
}
