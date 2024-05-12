package com.example.agrotracker.api.responses

data class AuthInfoResponse(
    val id: Int,
    val login: String,
    val name: String,
    val password: String,
    val role: String,
    val token: String,
)