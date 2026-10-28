package com.example.runtrack

import com.example.runtrack.settings.PaceDisplayFormat
import com.example.runtrack.settings.UnitSystem
import com.example.runtrack.settings.UnitsConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UnitsTest {

    @Test
    fun testDefaultUnitsConfiguration() {
        val config = UnitsConfig()
        assertEquals(UnitSystem.METRIC, config.unitSystem)
        assertEquals(PaceDisplayFormat.MIN_PER_KM, config.paceFormat)
        assertTrue(config.elevationUnitMeters)
        assertTrue(config.temperatureCelsius)
        assertTrue(config.weightUnitKg)
    }

    @Test
    fun testUnitSystemTransitions() {
        val config = UnitsConfig()

        config.unitSystem = UnitSystem.IMPERIAL
        config.paceFormat = PaceDisplayFormat.MIN_PER_MILE
        config.elevationUnitMeters = false
        config.temperatureCelsius = false
        config.weightUnitKg = false

        assertEquals(UnitSystem.IMPERIAL, config.unitSystem)
        assertEquals(PaceDisplayFormat.MIN_PER_MILE, config.paceFormat)
        assertFalse(config.elevationUnitMeters)
        assertFalse(config.temperatureCelsius)
        assertFalse(config.weightUnitKg)
    }

    @Test
    fun testPaceFormatTransitions() {
        val config = UnitsConfig()

        config.paceFormat = PaceDisplayFormat.SPEED_KMH
        assertEquals(PaceDisplayFormat.SPEED_KMH, config.paceFormat)

        config.paceFormat = PaceDisplayFormat.MIN_PER_MILE
        assertEquals(PaceDisplayFormat.MIN_PER_MILE, config.paceFormat)

        config.paceFormat = PaceDisplayFormat.MIN_PER_KM
        assertEquals(PaceDisplayFormat.MIN_PER_KM, config.paceFormat)
    }

    @Test
    fun testEnumFallbackParsing() {
        val validSystem = try {
            UnitSystem.valueOf("IMPERIAL")
        } catch (_: Exception) {
            UnitSystem.METRIC
        }
        assertEquals(UnitSystem.IMPERIAL, validSystem)

        val invalidSystem = try {
            UnitSystem.valueOf("UNKNOWN_SYSTEM")
        } catch (_: Exception) {
            UnitSystem.METRIC
        }
        assertEquals(UnitSystem.METRIC, invalidSystem)

        val validPace = try {
            PaceDisplayFormat.valueOf("SPEED_KMH")
        } catch (_: Exception) {
            PaceDisplayFormat.MIN_PER_KM
        }
        assertEquals(PaceDisplayFormat.SPEED_KMH, validPace)

        val invalidPace = try {
            PaceDisplayFormat.valueOf("INVALID_PACE")
        } catch (_: Exception) {
            PaceDisplayFormat.MIN_PER_KM
        }
        assertEquals(PaceDisplayFormat.MIN_PER_KM, invalidPace)
    }
}
