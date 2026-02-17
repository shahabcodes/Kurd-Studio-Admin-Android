package com.crimsonedge.studioadmin.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DashboardStatsDto(
    val artworkCount: Int,
    val writingCount: Int,
    val imageCount: Int,
    val unreadContactCount: Int
)
