package com.crimsonedge.studioadmin.data.remote.api

import com.crimsonedge.studioadmin.data.remote.dto.CreatedResponse
import com.crimsonedge.studioadmin.data.remote.dto.MessageResponse
import com.crimsonedge.studioadmin.data.remote.dto.WritingDto
import com.crimsonedge.studioadmin.data.remote.dto.WritingRequest
import com.crimsonedge.studioadmin.data.remote.dto.WritingTypeDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface WritingApi {

    @GET("writings")
    suspend fun getAll(): List<WritingDto>

    @GET("writings/types")
    suspend fun getTypes(): List<WritingTypeDto>

    @GET("writings/{id}")
    suspend fun getById(@Path("id") id: Int): WritingDto

    @POST("writings")
    suspend fun create(@Body request: WritingRequest): CreatedResponse

    @PUT("writings/{id}")
    suspend fun update(@Path("id") id: Int, @Body request: WritingRequest): MessageResponse

    @DELETE("writings/{id}")
    suspend fun delete(@Path("id") id: Int): MessageResponse
}
