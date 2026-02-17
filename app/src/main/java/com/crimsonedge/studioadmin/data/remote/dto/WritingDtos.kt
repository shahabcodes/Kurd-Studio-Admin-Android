package com.crimsonedge.studioadmin.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WritingDto(
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

@JsonClass(generateAdapter = true)
data class WritingTypeDto(
    val id: Int,
    val typeName: String,
    val displayName: String,
    val displayOrder: Int
)

@JsonClass(generateAdapter = true)
data class WritingRequest(
    val title: String,
    val slug: String,
    val writingTypeId: Int,
    val subtitle: String?,
    val excerpt: String?,
    val fullContent: String?,
    val datePublished: String?,
    val novelName: String?,
    val chapterNumber: Int?,
    val displayOrder: Int
)
