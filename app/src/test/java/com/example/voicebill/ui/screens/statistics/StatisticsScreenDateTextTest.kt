package com.example.voicebill.ui.screens.statistics

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class StatisticsScreenDateTextTest {

    private val testZone: ZoneId = ZoneId.of("Asia/Shanghai")

    @Test
    fun formatDateRangePickerHeadline_whenBothNull_shouldReturnHint() {
        val result = formatDateRangePickerHeadline(
            startUtcMillis = null,
            endUtcMillis = null,
            zoneId = testZone
        )

        assertEquals("开始 - 结束", result)
    }

    @Test
    fun formatDateRangePickerHeadline_whenOnlyStartSelected_shouldReturnStartAndPlaceholder() {
        val result = formatDateRangePickerHeadline(
            startUtcMillis = utcDateMillis(2026, 2, 28),
            endUtcMillis = null,
            zoneId = testZone
        )

        assertEquals("2026/2/28 - 结束", result)
    }

    @Test
    fun formatDateRangePickerHeadline_whenBothSelected_shouldReturnCompactDateRange() {
        val result = formatDateRangePickerHeadline(
            startUtcMillis = utcDateMillis(2026, 2, 28),
            endUtcMillis = utcDateMillis(2026, 3, 3),
            zoneId = testZone
        )

        assertEquals("2026/2/28 - 2026/3/3", result)
    }

    @Test
    fun formatDateRangePickerHeadline_whenOnlyEndSelected_shouldReturnPlaceholderAndEnd() {
        val result = formatDateRangePickerHeadline(
            startUtcMillis = null,
            endUtcMillis = utcDateMillis(2026, 3, 3),
            zoneId = testZone
        )

        assertEquals("开始 - 2026/3/3", result)
    }

    @Test
    fun formatDateRangePickerHeadline_shouldUseProvidedZoneForDateConversion() {
        val result = formatDateRangePickerHeadline(
            startUtcMillis = utcDateMillis(2026, 3, 1),
            endUtcMillis = utcDateMillis(2026, 3, 31),
            zoneId = ZoneId.of("UTC")
        )

        assertEquals("2026/3/1 - 2026/3/31", result)
    }

    private fun utcDateMillis(year: Int, month: Int, day: Int): Long {
        return LocalDate.of(year, month, day)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    }
}
