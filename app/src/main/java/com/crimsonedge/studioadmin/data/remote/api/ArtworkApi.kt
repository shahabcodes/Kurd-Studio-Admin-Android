package com.crimsonedge.studioadmin.data.remote.api

import com.crimsonedge.studioadmin.data.remote.dto.ArtworkDto
import com.crimsonedge.studioadmin.data.remote.dto.ArtworkRequest
import com.crimsonedge.studioadmin.data.remote.dto.ArtworkTypeDto
import com.crimsonedge.studioadmin.data.remote.dto.CreatedResponse
import com.crimsonedge.studioadmin.data.remote.dto.MessageResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ArtworkApi {

    @GET("artworks")
    suspend fun getAll(): List<ArtworkDto>

    @GET("artworks/types")
    suspend fun getTypes(): List<ArtworkTypeDto>

    @GET("artworks/{id}")
    suspend fun getById(@Path("id") id: Int): ArtworkDto

    @POST("artworks")
    suspend fun create(@Body request: ArtworkRequest): CreatedResponse

    @PUT("artworks/{id}")
    suspend fun update(@Path("id") id: Int, @Body request: ArtworkRequest): MessageResponse

    @DELETE("artworks/{id}")
    suspend fun delete(@Path("id") id: Int): MessageResponse
}
