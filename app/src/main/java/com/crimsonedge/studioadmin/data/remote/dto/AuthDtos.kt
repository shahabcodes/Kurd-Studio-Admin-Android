package com.crimsonedge.studioadmin.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(val username: String, val password: String)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val username: String,
    val displayName: String?,
    val expiresAt: String
)

@JsonClass(generateAdapter = true)
data class RefreshRequest(val refreshToken: String)
