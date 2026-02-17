package com.crimsonedge.studioadmin.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ArtworkDto(
    val id: Int,
    val title: String,
    val slug: String,
    val artworkTypeId: Int,
    val typeName: String,
    val typeDisplayName: String,
    val imageId: Int,
    val description: String?,
    val isFeatured: Boolean,
    val displayOrder: Int,
    val createdAt: String,
    val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class ArtworkTypeDto(
    val id: Int,
    val typeName: String,
    val displayName: String,
    val displayOrder: Int
)

@JsonClass(generateAdapter = true)
data class ArtworkRequest(
    val title: String,
    val slug: String,
    val artworkTypeId: Int,
    val imageId: Int,
    val description: String?,
    val isFeatured: Boolean,
    val displayOrder: Int
)
