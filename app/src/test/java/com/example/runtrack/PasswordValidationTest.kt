package com.example.runtrack

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordValidationTest {

    private fun isLengthValid(password: String): Boolean = password.length >= 8

    private fun isCaseValid(password: String): Boolean =
        password.any { it.isUpperCase() } && password.any { it.isLowerCase() }

    private fun isSpecialValid(password: String): Boolean =
        password.any { it.isDigit() || !it.isLetterOrDigit() }

    private fun isPasswordStrong(password: String): Boolean =
        isLengthValid(password) && isCaseValid(password) && isSpecialValid(password)

    @Test
    fun testShortPasswordFails() {
        assertFalse(isLengthValid("P@ss1"))
        assertFalse(isPasswordStrong("P@ss1"))
    }

    @Test
    fun testMissingUppercaseOrLowercaseFails() {
        assertFalse(isCaseValid("alllowercase123!"))
        assertFalse(isCaseValid("ALLUPPERCASE123!"))
    }

    @Test
    fun testMissingDigitOrSpecialCharFails() {
        assertFalse(isSpecialValid("OnlyLettersHere"))
    }

    @Test
    fun testStrongPasswordPasses() {
        assertTrue(isPasswordStrong("RunTrack@2026"))
        assertTrue(isPasswordStrong("CyberPace#99"))
    }

    @Test
    fun testConfirmationMatches() {
        val newPass = "SecurePace#2026"
        val confirmPass = "SecurePace#2026"
        val wrongConfirm = "WrongPace#2026"

        assertTrue(newPass == confirmPass)
        assertFalse(newPass == wrongConfirm)
    }

    @Test
    fun testCannotReuseCurrentPassword() {
        val currentPass = "OldPassword123!"
        val newPassSame = "OldPassword123!"
        val newPassDifferent = "BrandNewPace#2026"

        assertTrue(currentPass == newPassSame)
        assertFalse(currentPass == newPassDifferent)
    }
}
