package com.crimsonedge.studioadmin.data.remote.api

import com.crimsonedge.studioadmin.data.remote.dto.CreatedResponse
import com.crimsonedge.studioadmin.data.remote.dto.MessageResponse
import com.crimsonedge.studioadmin.data.remote.dto.NavigationItemDto
import com.crimsonedge.studioadmin.data.remote.dto.NavigationItemRequest
import com.crimsonedge.studioadmin.data.remote.dto.SocialLinkDto
import com.crimsonedge.studioadmin.data.remote.dto.SocialLinkRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface NavigationApi {

    @GET("navigation")
    suspend fun getNavigationItems(): List<NavigationItemDto>

    @POST("navigation")
    suspend fun createNavigationItem(@Body request: NavigationItemRequest): CreatedResponse

    @PUT("navigation/{id}")
    suspend fun updateNavigationItem(
        @Path("id") id: Int,
        @Body request: NavigationItemRequest
    ): MessageResponse

    @DELETE("navigation/{id}")
    suspend fun deleteNavigationItem(@Path("id") id: Int): MessageResponse

    @GET("social")
    suspend fun getSocialLinks(): List<SocialLinkDto>

    @POST("social")
    suspend fun createSocialLink(@Body request: SocialLinkRequest): CreatedResponse

    @PUT("social/{id}")
    suspend fun updateSocialLink(
        @Path("id") id: Int,
        @Body request: SocialLinkRequest
    ): MessageResponse

    @DELETE("social/{id}")
    suspend fun deleteSocialLink(@Path("id") id: Int): MessageResponse
}
