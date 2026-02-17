package com.crimsonedge.studioadmin.domain.model

data class Contact(
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
