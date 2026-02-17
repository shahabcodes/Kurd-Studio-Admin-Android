package com.crimsonedge.studioadmin.domain.model

data class NavigationItem(
    val id: Int,
    val label: String,
    val link: String,
    val iconSvg: String?,
    val displayOrder: Int,
    val isActive: Boolean
)

data class SocialLink(
    val id: Int,
    val platform: String,
    val url: String,
    val iconSvg: String?,
    val displayOrder: Int,
    val isActive: Boolean
)
