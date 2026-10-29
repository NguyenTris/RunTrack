package com.example.runtrack

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterValidatorTest {

    @Test
    fun testUsernameValidation() {
        // Empty or blank
        val emptyRes = RegisterValidator.validateUsername("")
        assertFalse(emptyRes.isValid)
        assertEquals("Tên định danh không được để trống", emptyRes.errorMessage)

        val blankRes = RegisterValidator.validateUsername("   ")
        assertFalse(blankRes.isValid)
        assertEquals("Tên định danh không được để trống", blankRes.errorMessage)

        // Too short (< 3 chars)
        val shortRes = RegisterValidator.validateUsername("AB")
        assertFalse(shortRes.isValid)
        assertEquals("Tên định danh phải có ít nhất 3 ký tự", shortRes.errorMessage)

        // Valid username
        val validRes = RegisterValidator.validateUsername("NIGHTRUNNER_07")
        assertTrue(validRes.isValid)
        assertNull(validRes.errorMessage)
    }

    @Test
    fun testEmailValidation() {
        // Empty
        val emptyRes = RegisterValidator.validateEmail("")
        assertFalse(emptyRes.isValid)
        assertEquals("Địa chỉ email không được để trống", emptyRes.errorMessage)

        // Invalid email formats
        assertFalse(RegisterValidator.validateEmail("notanemail").isValid)
        assertFalse(RegisterValidator.validateEmail("runner@").isValid)
        assertFalse(RegisterValidator.validateEmail("runner@domain").isValid)
        assertFalse(RegisterValidator.validateEmail("@domain.com").isValid)

        // Valid email formats
        val validRes1 = RegisterValidator.validateEmail("runner@pacepulse.io")
        assertTrue(validRes1.isValid)
        assertNull(validRes1.errorMessage)

        val validRes2 = RegisterValidator.validateEmail("user.name+tag@example.com.vn")
        assertTrue(validRes2.isValid)
    }

    @Test
    fun testPasswordValidation() {
        // Empty
        val emptyRes = RegisterValidator.validatePassword("")
        assertFalse(emptyRes.isValid)
        assertEquals("Mật khẩu không được để trống", emptyRes.errorMessage)

        // Length < 8
        val shortRes = RegisterValidator.validatePassword("P@ss1")
        assertFalse(shortRes.isValid)
        assertEquals("Mật khẩu phải chứa ít nhất 8 ký tự", shortRes.errorMessage)

        // Missing uppercase
        val noUpperRes = RegisterValidator.validatePassword("cyberpace123@")
        assertFalse(noUpperRes.isValid)
        assertEquals("Mật khẩu phải chứa ít nhất 1 chữ in hoa", noUpperRes.errorMessage)

        // Missing lowercase
        val noLowerRes = RegisterValidator.validatePassword("CYBERPACE123@")
        assertFalse(noLowerRes.isValid)
        assertEquals("Mật khẩu phải chứa ít nhất 1 chữ thường", noLowerRes.errorMessage)

        // Missing digit or special char
        val noDigitSpecialRes = RegisterValidator.validatePassword("CyberPaceRunner")
        assertFalse(noDigitSpecialRes.isValid)
        assertEquals("Mật khẩu phải chứa ít nhất 1 số hoặc ký tự đặc biệt", noDigitSpecialRes.errorMessage)

        // Strong valid password
        val validRes = RegisterValidator.validatePassword("RunTrack@2026")
        assertTrue(validRes.isValid)
        assertNull(validRes.errorMessage)
    }
}
