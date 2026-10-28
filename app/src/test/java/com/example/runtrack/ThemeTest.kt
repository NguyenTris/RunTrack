package com.example.runtrack

import com.example.runtrack.settings.HudThemeMode
import com.example.runtrack.settings.ThemeConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeTest {

    @Test
    fun testDefaultThemeConfiguration() {
        val config = ThemeConfig()
        assertEquals(HudThemeMode.AEROSPACE_DARK, config.themeMode)
        assertTrue(config.autoSwitchSunset)
        assertTrue(config.highContrastNeon)
        assertFalse(config.reduceMotion)
    }

    @Test
    fun testThemeModeTransitions() {
        val config = ThemeConfig()

        config.themeMode = HudThemeMode.AMOLED_BLACK
        assertEquals(HudThemeMode.AMOLED_BLACK, config.themeMode)

        config.themeMode = HudThemeMode.CYAN_STEALTH
        assertEquals(HudThemeMode.CYAN_STEALTH, config.themeMode)

        config.themeMode = HudThemeMode.SOLAR_LIGHT
        assertEquals(HudThemeMode.SOLAR_LIGHT, config.themeMode)
    }

    @Test
    fun testAdaptiveDisplayToggles() {
        val config = ThemeConfig(
            themeMode = HudThemeMode.AMOLED_BLACK,
            autoSwitchSunset = false,
            highContrastNeon = false,
            reduceMotion = true
        )

        assertFalse(config.autoSwitchSunset)
        assertFalse(config.highContrastNeon)
        assertTrue(config.reduceMotion)
    }

    @Test
    fun testThemeParsingFallback() {
        val validTheme = try {
            HudThemeMode.valueOf("AMOLED_BLACK")
        } catch (_: Exception) {
            HudThemeMode.AEROSPACE_DARK
        }
        assertEquals(HudThemeMode.AMOLED_BLACK, validTheme)

        val invalidTheme = try {
            HudThemeMode.valueOf("INVALID_THEME")
        } catch (_: Exception) {
            HudThemeMode.AEROSPACE_DARK
        }
        assertEquals(HudThemeMode.AEROSPACE_DARK, invalidTheme)
    }
}
