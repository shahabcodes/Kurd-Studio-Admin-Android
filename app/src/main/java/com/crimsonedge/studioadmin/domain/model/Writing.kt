package com.crimsonedge.studioadmin.domain.model

data class Writing(
    val id: Int,
    val title: String,
    val slug: String,
    val writingTypeId: Int,
    val typeName: String,
    val typeDisplayName: String,
    val subtitle: String?,
    val excerpt: String?,
    val fullContent: String?,
    val datePublished: String?,
    val novelName: String?,
    val chapterNumber: Int?,
    val displayOrder: Int,
    val createdAt: String,
    val updatedAt: String
)

data class WritingType(
    val id: Int,
    val typeName: String,
    val displayName: String,
    val displayOrder: Int
)
