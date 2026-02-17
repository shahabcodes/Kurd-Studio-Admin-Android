package com.crimsonedge.studioadmin.domain.model

data class UserSession(
    val accessToken: String,
    val refreshToken: String,
    val username: String,
    val displayName: String?
)
