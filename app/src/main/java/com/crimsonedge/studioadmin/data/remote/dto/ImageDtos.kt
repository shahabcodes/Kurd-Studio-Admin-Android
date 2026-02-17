package com.crimsonedge.studioadmin.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ImageMetaDto(
    val id: Int,
    val fileName: String,
    val contentType: String,
    val altText: String?,
    val fileSize: Long,
    val width: Int,
    val height: Int,
    val imageUrl: String,
    val thumbnailUrl: String,
    val createdAt: String
)

@JsonClass(generateAdapter = true)
data class ImageMetaUpdateRequest(
    val fileName: String,
    val altText: String?
)
