package com.example.runtrack

import com.example.runtrack.settings.ActivityPrivacyConfig
import com.example.runtrack.settings.ActivityPrivacyScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActivityPrivacyTest {

    @Test
    fun testDefaultPrivacyConfiguration() {
        val config = ActivityPrivacyConfig()
        assertEquals(ActivityPrivacyScope.FRIENDS_ONLY, config.scope)
        assertTrue(config.hideStartEndRadius)
        assertFalse(config.hideHeartRateData)
    }

    @Test
    fun testScopeTransitions() {
        val config = ActivityPrivacyConfig()

        config.scope = ActivityPrivacyScope.PUBLIC
        assertEquals(ActivityPrivacyScope.PUBLIC, config.scope)

        config.scope = ActivityPrivacyScope.ONLY_ME
        assertEquals(ActivityPrivacyScope.ONLY_ME, config.scope)
    }

    @Test
    fun testPrivacyShieldToggles() {
        val config = ActivityPrivacyConfig(
            scope = ActivityPrivacyScope.FRIENDS_ONLY,
            hideStartEndRadius = true,
            hideHeartRateData = true
        )

        assertTrue(config.hideStartEndRadius)
        assertTrue(config.hideHeartRateData)

        config.hideStartEndRadius = false
        assertFalse(config.hideStartEndRadius)
    }

    @Test
    fun testScopeParsingFallback() {
        val valid = try {
            ActivityPrivacyScope.valueOf("PUBLIC")
        } catch (_: Exception) {
            ActivityPrivacyScope.FRIENDS_ONLY
        }
        assertEquals(ActivityPrivacyScope.PUBLIC, valid)

        val invalid = try {
            ActivityPrivacyScope.valueOf("UNKNOWN_INVALID_SCOPE")
        } catch (_: Exception) {
            ActivityPrivacyScope.FRIENDS_ONLY
        }
        assertEquals(ActivityPrivacyScope.FRIENDS_ONLY, invalid)
    }
}
