package com.crimsonedge.studioadmin.domain.model

data class Profile(
    val id: Int,
    val name: String,
    val tagline: String?,
    val bio: String?,
    val avatarImageId: Int?,
    val email: String?,
    val instagramUrl: String?,
    val twitterUrl: String?,
    val artworksCount: String?,
    val poemsCount: String?,
    val yearsExperience: String?,
    val updatedAt: String
)

data class HeroContent(
    val id: Int,
    val quote: String?,
    val quoteAttribution: String?,
    val headline: String?,
    val subheading: String?,
    val featuredImageId: Int?,
    val badgeText: String?,
    val primaryCtaText: String?,
    val primaryCtaLink: String?,
    val secondaryCtaText: String?,
    val secondaryCtaLink: String?,
    val isActive: Boolean,
    val updatedAt: String
)

data class SiteSetting(
    val id: Int,
    val settingKey: String,
    val settingValue: String?,
    val settingType: String,
    val updatedAt: String
)

data class Section(
    val id: Int,
    val sectionKey: String,
    val tag: String?,
    val title: String?,
    val subtitle: String?,
    val displayOrder: Int,
    val isActive: Boolean,
    val updatedAt: String
)
