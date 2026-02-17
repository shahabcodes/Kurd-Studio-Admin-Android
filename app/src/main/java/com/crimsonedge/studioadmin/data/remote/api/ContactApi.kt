package com.crimsonedge.studioadmin.data.remote.api

import com.crimsonedge.studioadmin.data.remote.dto.ContactSubmissionDto
import com.crimsonedge.studioadmin.data.remote.dto.MessageResponse
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface ContactApi {

    @GET("contacts")
    suspend fun getAll(): List<ContactSubmissionDto>

    @PUT("contacts/{id}/read")
    suspend fun markAsRead(@Path("id") id: Int): MessageResponse

    @DELETE("contacts/{id}")
    suspend fun delete(@Path("id") id: Int): MessageResponse
}
