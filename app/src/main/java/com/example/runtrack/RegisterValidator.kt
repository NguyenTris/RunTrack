package com.example.runtrack

object RegisterValidator {

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validateUsername(username: String?): ValidationResult {
        val trimmed = username?.trim().orEmpty()
        return when {
            trimmed.isEmpty() -> ValidationResult(false, "Tên định danh không được để trống")
            trimmed.length < 3 -> ValidationResult(false, "Tên định danh phải có ít nhất 3 ký tự")
            else -> ValidationResult(true)
        }
    }

    fun validateEmail(email: String?): ValidationResult {
        val trimmed = email?.trim().orEmpty()
        return when {
            trimmed.isEmpty() -> ValidationResult(false, "Địa chỉ email không được để trống")
            !EMAIL_REGEX.matches(trimmed) -> ValidationResult(false, "Định dạng email không hợp lệ (ví dụ: runner@pacepulse.io)")
            else -> ValidationResult(true)
        }
    }

    fun validatePassword(password: String?): ValidationResult {
        val pass = password.orEmpty()
        return when {
            pass.isEmpty() -> ValidationResult(false, "Mật khẩu không được để trống")
            pass.length < 8 -> ValidationResult(false, "Mật khẩu phải chứa ít nhất 8 ký tự")
            !pass.any { it.isUpperCase() } -> ValidationResult(false, "Mật khẩu phải chứa ít nhất 1 chữ in hoa")
            !pass.any { it.isLowerCase() } -> ValidationResult(false, "Mật khẩu phải chứa ít nhất 1 chữ thường")
            !pass.any { it.isDigit() || !it.isLetterOrDigit() } -> ValidationResult(false, "Mật khẩu phải chứa ít nhất 1 số hoặc ký tự đặc biệt")
            else -> ValidationResult(true)
        }
    }
}
