package com.example.voicebill.domain.usecase

import com.example.voicebill.domain.model.StatisticsPeriod
import com.example.voicebill.domain.model.StatisticsQuery
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import javax.inject.Inject

data class StatisticsDateRange(
    val startDate: Long,
    val endDate: Long
)

class StatisticsDateRangeResolver @Inject constructor(
    private val clock: Clock
) {

    fun resolve(query: StatisticsQuery): StatisticsDateRange {
        val zone = clock.zone
        val now = LocalDate.now(clock.withZone(zone))

        return when (query.period) {
            StatisticsPeriod.DAILY -> resolveDaily(now, zone, query.offset)
            StatisticsPeriod.WEEKLY -> resolveWeekly(now, zone, query.offset)
            StatisticsPeriod.MONTHLY -> resolveMonthly(now, zone, query.offset)
            StatisticsPeriod.YEARLY -> resolveYearly(now, zone, query.offset)
            StatisticsPeriod.CUSTOM -> resolveCustom(zone, query)
        }
    }

    private fun resolveDaily(now: LocalDate, zone: ZoneId, offset: Int): StatisticsDateRange {
        val start = now.minusDays(offset.toLong())
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()
        val end = now.minusDays(offset.toLong() - 1)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()
        return StatisticsDateRange(startDate = start, endDate = end)
    }

    private fun resolveWeekly(now: LocalDate, zone: ZoneId, offset: Int): StatisticsDateRange {
        val startOfCurrentWeek = now.minusDays(now.dayOfWeek.value.toLong() - 1)
        val startOfWeek = startOfCurrentWeek.minusWeeks(offset.toLong())
        val start = startOfWeek.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = startOfWeek.plusWeeks(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return StatisticsDateRange(startDate = start, endDate = end)
    }

    private fun resolveMonthly(now: LocalDate, zone: ZoneId, offset: Int): StatisticsDateRange {
        val startOfMonth = now.withDayOfMonth(1).minusMonths(offset.toLong())
        val start = startOfMonth.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = startOfMonth.plusMonths(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return StatisticsDateRange(startDate = start, endDate = end)
    }

    private fun resolveYearly(now: LocalDate, zone: ZoneId, offset: Int): StatisticsDateRange {
        val startOfYear = now.withDayOfYear(1).minusYears(offset.toLong())
        val start = startOfYear.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = startOfYear.plusYears(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return StatisticsDateRange(startDate = start, endDate = end)
    }

    private fun resolveCustom(zone: ZoneId, query: StatisticsQuery): StatisticsDateRange {
        val customRange = requireNotNull(query.customRange) {
            "customRange is required when period is CUSTOM"
        }
        val startLocalDate = utcDateMillisToLocalDate(customRange.startUtcDateMillis)
        val endLocalDate = utcDateMillisToLocalDate(customRange.endUtcDateMillis)

        val start = startLocalDate.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = endLocalDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return StatisticsDateRange(startDate = start, endDate = end)
    }

    private fun utcDateMillisToLocalDate(utcDateMillis: Long): LocalDate {
        return Instant.ofEpochMilli(utcDateMillis)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
    }
}
