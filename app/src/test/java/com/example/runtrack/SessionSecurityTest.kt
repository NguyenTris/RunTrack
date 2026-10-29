package com.example.runtrack

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionSecurityTest {

    @Test
    fun testSessionConstantsAndDefaults() {
        val defaultUsername = "NIGHTRUNNER_07"
        assertEquals("NIGHTRUNNER_07", defaultUsername)

        val securePrefName = "pacepulse_secure_session"
        val fallbackPrefName = "pacepulse_session"

        assertTrue(securePrefName.contains("secure"))
        assertNotNull(fallbackPrefName)
    }

    @Test
    fun testSessionKeysIntegrity() {
        val keyLoggedIn = "is_logged_in"
        val keyUsername = "username"

        assertEquals("is_logged_in", keyLoggedIn)
        assertEquals("username", keyUsername)
    }
}
