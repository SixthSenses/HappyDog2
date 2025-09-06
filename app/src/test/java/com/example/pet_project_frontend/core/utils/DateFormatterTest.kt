package com.example.pet_project_frontend.core.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class DateFormatterTest {
    @Test
    fun weekRangeUtc_includesTodayAndSixDaysBefore() {
        val today = LocalDate.of(2024, 3, 1) // non-leap year reference
        val (start, end) = DateFormatter.weekRangeUtc(today)
        assertEquals("2024-02-24", start)
        assertEquals("2024-03-01", end)
    }

    @Test
    fun monthRangeUtc_fromFirstDayToToday() {
        val today = LocalDate.of(2024, 12, 31)
        val (start, end) = DateFormatter.monthRangeUtc(today)
        assertEquals("2024-12-01", start)
        assertEquals("2024-12-31", end)
    }

    @Test
    fun weekRangeUtc_handlesLeapYear() {
        val today = LocalDate.of(2024, 3, 1) // 2024 is leap year
        val (start, end) = DateFormatter.weekRangeUtc(today)
        assertEquals("2024-02-24", start) // includes Feb 29 within range implicitly
        assertEquals("2024-03-01", end)
    }
}
