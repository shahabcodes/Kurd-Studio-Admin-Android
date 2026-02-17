package com.crimsonedge.studioadmin.domain.model

data class Artwork(
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

data class ArtworkType(
    val id: Int,
    val typeName: String,
    val displayName: String,
    val displayOrder: Int
)
