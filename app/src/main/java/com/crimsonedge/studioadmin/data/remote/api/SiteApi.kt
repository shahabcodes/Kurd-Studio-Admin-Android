package com.crimsonedge.studioadmin.data.remote.api

import com.crimsonedge.studioadmin.data.remote.dto.HeroContentDto
import com.crimsonedge.studioadmin.data.remote.dto.HeroRequest
import com.crimsonedge.studioadmin.data.remote.dto.MessageResponse
import com.crimsonedge.studioadmin.data.remote.dto.ProfileDto
import com.crimsonedge.studioadmin.data.remote.dto.ProfileRequest
import com.crimsonedge.studioadmin.data.remote.dto.SectionDto
import com.crimsonedge.studioadmin.data.remote.dto.SectionRequest
import com.crimsonedge.studioadmin.data.remote.dto.SiteSettingDto
import com.crimsonedge.studioadmin.data.remote.dto.SiteSettingRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface SiteApi {

    @GET("site/profile")
    suspend fun getProfile(): ProfileDto

    @PUT("site/profile")
    suspend fun updateProfile(@Body request: ProfileRequest): MessageResponse

    @GET("site/hero")
    suspend fun getHero(): HeroContentDto

    @PUT("site/hero")
    suspend fun updateHero(@Body request: HeroRequest): MessageResponse

    @GET("site/settings")
    suspend fun getSettings(): List<SiteSettingDto>

    @PUT("site/settings/{key}")
    suspend fun updateSetting(
        @Path("key") key: String,
        @Body request: SiteSettingRequest
    ): MessageResponse

    @GET("site/sections")
    suspend fun getSections(): List<SectionDto>

    @PUT("site/sections/{id}")
    suspend fun updateSection(
        @Path("id") id: Int,
        @Body request: SectionRequest
    ): MessageResponse
}
