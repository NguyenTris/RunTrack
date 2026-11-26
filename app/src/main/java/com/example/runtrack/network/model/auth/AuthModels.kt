package com.example.runtrack.network.model.auth

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val access_token: String,
    val token_type: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val full_name: String
)

data class RegisterResponse(
    val message: String,
    val user: String
)

data class MessageResponse(
    val message: String
)

data class ChangePasswordRequest(
    val old_password: String,
    val new_password: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ForgotPasswordResponse(
    val message: String = "",
    val demo_otp: String? = null
)

data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    val new_password: String
)
