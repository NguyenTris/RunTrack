package com.example.runtrack

import com.example.runtrack.settings.GpsAccuracyConfig
import com.example.runtrack.settings.GpsSamplingMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GpsAccuracyTest {

    @Test
    fun testDefaultGpsAccuracyConfiguration() {
        val config = GpsAccuracyConfig()
        assertEquals(GpsSamplingMode.BALANCED_STREET, config.mode)
        assertTrue(config.enableKalmanFilter)
        assertTrue(config.autoPauseUnderSpeed)
        assertTrue(config.enableMultiGnss)
    }

    @Test
    fun testModeTransitions() {
        val config = GpsAccuracyConfig()

        config.mode = GpsSamplingMode.HIGH_PRECISION_ART
        assertEquals(GpsSamplingMode.HIGH_PRECISION_ART, config.mode)

        config.mode = GpsSamplingMode.BATTERY_SAVER
        assertEquals(GpsSamplingMode.BATTERY_SAVER, config.mode)
    }

    @Test
    fun testTelemetryToggles() {
        val config = GpsAccuracyConfig(
            mode = GpsSamplingMode.BATTERY_SAVER,
            enableKalmanFilter = false,
            autoPauseUnderSpeed = false,
            enableMultiGnss = false
        )

        assertFalse(config.enableKalmanFilter)
        assertFalse(config.autoPauseUnderSpeed)
        assertFalse(config.enableMultiGnss)
    }

    @Test
    fun testSamplingModeParsingFallback() {
        val valid = try {
            GpsSamplingMode.valueOf("HIGH_PRECISION_ART")
        } catch (_: Exception) {
            GpsSamplingMode.BALANCED_STREET
        }
        assertEquals(GpsSamplingMode.HIGH_PRECISION_ART, valid)

        val invalid = try {
            GpsSamplingMode.valueOf("UNKNOWN_MODE")
        } catch (_: Exception) {
            GpsSamplingMode.BALANCED_STREET
        }
        assertEquals(GpsSamplingMode.BALANCED_STREET, invalid)
    }
}
