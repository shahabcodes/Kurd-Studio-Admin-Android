package com.crimsonedge.studioadmin.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NavigationItemDto(
    val id: Int,
    val label: String,
    val link: String,
    val iconSvg: String?,
    val displayOrder: Int,
    val isActive: Boolean
)

@JsonClass(generateAdapter = true)
data class NavigationItemRequest(
    val label: String,
    val link: String,
    val iconSvg: String?,
    val displayOrder: Int,
    val isActive: Boolean
)

@JsonClass(generateAdapter = true)
data class SocialLinkDto(
    val id: Int,
    val platform: String,
    val url: String,
    val iconSvg: String?,
    val displayOrder: Int,
    val isActive: Boolean
)

@JsonClass(generateAdapter = true)
data class SocialLinkRequest(
    val platform: String,
    val url: String,
    val iconSvg: String?,
    val displayOrder: Int,
    val isActive: Boolean
)
