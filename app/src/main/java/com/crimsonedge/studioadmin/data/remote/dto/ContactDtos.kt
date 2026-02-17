package com.crimsonedge.studioadmin.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ContactSubmissionDto(
    val id: Int,
    val name: String,
    val email: String,
    val subject: String,
    val budget: String?,
    val message: String,
    val submittedAt: String,
    val isRead: Boolean,
    val isResponded: Boolean
)
