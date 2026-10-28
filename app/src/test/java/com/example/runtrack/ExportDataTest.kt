package com.example.runtrack

import com.example.runtrack.settings.ExportDateRange
import com.example.runtrack.settings.ExportFormat
import com.example.runtrack.settings.ExportDataConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportDataTest {

    @Test
    fun testDefaultExportConfiguration() {
        val config = ExportDataConfig()
        assertEquals(ExportFormat.GPX, config.format)
        assertEquals(ExportDateRange.LAST_30_DAYS, config.dateRange)
        assertTrue(config.includeHeartRate)
        assertTrue(config.includeCadence)
        assertTrue(config.includeElevation)
    }

    @Test
    fun testFormatTransitions() {
        val config = ExportDataConfig()

        config.format = ExportFormat.TCX
        assertEquals(ExportFormat.TCX, config.format)

        config.format = ExportFormat.CSV
        assertEquals(ExportFormat.CSV, config.format)

        config.format = ExportFormat.FIT
        assertEquals(ExportFormat.FIT, config.format)
    }

    @Test
    fun testDateRangeTransitions() {
        val config = ExportDataConfig()

        config.dateRange = ExportDateRange.THIS_MONTH
        assertEquals(ExportDateRange.THIS_MONTH, config.dateRange)

        config.dateRange = ExportDateRange.ALL_TIME
        assertEquals(ExportDateRange.ALL_TIME, config.dateRange)
    }

    @Test
    fun testTelemetryToggles() {
        val config = ExportDataConfig(
            format = ExportFormat.CSV,
            dateRange = ExportDateRange.ALL_TIME,
            includeHeartRate = false,
            includeCadence = false,
            includeElevation = false
        )

        assertFalse(config.includeHeartRate)
        assertFalse(config.includeCadence)
        assertFalse(config.includeElevation)
    }

    @Test
    fun testEnumFallbackParsing() {
        val validFormat = try {
            ExportFormat.valueOf("TCX")
        } catch (_: Exception) {
            ExportFormat.GPX
        }
        assertEquals(ExportFormat.TCX, validFormat)

        val invalidFormat = try {
            ExportFormat.valueOf("INVALID_FORMAT")
        } catch (_: Exception) {
            ExportFormat.GPX
        }
        assertEquals(ExportFormat.GPX, invalidFormat)

        val validRange = try {
            ExportDateRange.valueOf("THIS_MONTH")
        } catch (_: Exception) {
            ExportDateRange.LAST_30_DAYS
        }
        assertEquals(ExportDateRange.THIS_MONTH, validRange)

        val invalidRange = try {
            ExportDateRange.valueOf("UNKNOWN_RANGE")
        } catch (_: Exception) {
            ExportDateRange.LAST_30_DAYS
        }
        assertEquals(ExportDateRange.LAST_30_DAYS, invalidRange)
    }
}
