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
