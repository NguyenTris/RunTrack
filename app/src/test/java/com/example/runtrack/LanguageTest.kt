package com.example.runtrack

import com.example.runtrack.settings.AppLanguage
import com.example.runtrack.settings.HudFontScale
import com.example.runtrack.settings.LanguageFontConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LanguageTest {

    @Test
    fun testDefaultLanguageConfiguration() {
        val config = LanguageFontConfig()
        assertEquals(AppLanguage.VIETNAMESE, config.language)
        assertEquals(HudFontScale.STANDARD, config.fontScale)
        assertTrue(config.useTechMonospace)
        assertTrue(config.boldNumbersOnDashboard)
    }

    @Test
    fun testLanguageTransitions() {
        val config = LanguageFontConfig()

        config.language = AppLanguage.ENGLISH
        assertEquals(AppLanguage.ENGLISH, config.language)

        config.language = AppLanguage.JAPANESE
        assertEquals(AppLanguage.JAPANESE, config.language)

        config.language = AppLanguage.FRENCH
        assertEquals(AppLanguage.FRENCH, config.language)
    }

    @Test
    fun testFontScaleTransitions() {
        val config = LanguageFontConfig()

        config.fontScale = HudFontScale.LARGE
        assertEquals(HudFontScale.LARGE, config.fontScale)

        config.fontScale = HudFontScale.EXTRA_LARGE
        assertEquals(HudFontScale.EXTRA_LARGE, config.fontScale)

        config.fontScale = HudFontScale.STANDARD
        assertEquals(HudFontScale.STANDARD, config.fontScale)
    }

    @Test
    fun testStylingToggles() {
        val config = LanguageFontConfig(
            language = AppLanguage.ENGLISH,
            fontScale = HudFontScale.LARGE,
            useTechMonospace = false,
            boldNumbersOnDashboard = false
        )

        assertFalse(config.useTechMonospace)
        assertFalse(config.boldNumbersOnDashboard)
    }

    @Test
    fun testEnumFallbackParsing() {
        val validLang = try {
            AppLanguage.valueOf("ENGLISH")
        } catch (_: Exception) {
            AppLanguage.VIETNAMESE
        }
        assertEquals(AppLanguage.ENGLISH, validLang)

        val invalidLang = try {
            AppLanguage.valueOf("INVALID_LANG")
        } catch (_: Exception) {
            AppLanguage.VIETNAMESE
        }
        assertEquals(AppLanguage.VIETNAMESE, invalidLang)

        val validScale = try {
            HudFontScale.valueOf("LARGE")
        } catch (_: Exception) {
            HudFontScale.STANDARD
        }
        assertEquals(HudFontScale.LARGE, validScale)

        val invalidScale = try {
            HudFontScale.valueOf("UNKNOWN_SCALE")
        } catch (_: Exception) {
            HudFontScale.STANDARD
        }
        assertEquals(HudFontScale.STANDARD, invalidScale)
    }
}
