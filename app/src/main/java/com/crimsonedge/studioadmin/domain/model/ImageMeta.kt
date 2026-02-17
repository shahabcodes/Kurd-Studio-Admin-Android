package com.crimsonedge.studioadmin.domain.model

data class ImageMeta(
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
