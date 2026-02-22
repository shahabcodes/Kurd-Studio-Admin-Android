package com.crimsonedge.studioadmin.data.remote.api

import com.crimsonedge.studioadmin.data.remote.dto.ImageMetaDto
import com.crimsonedge.studioadmin.data.remote.dto.ImageMetaUpdateRequest
import com.crimsonedge.studioadmin.data.remote.dto.MessageResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ImageApi {

    @GET("images")
    suspend fun getAll(): List<ImageMetaDto>

    @Multipart
    @POST("images/upload")
    suspend fun upload(@Part file: MultipartBody.Part): Response<ResponseBody>

    @PUT("images/{id}")
    suspend fun update(@Path("id") id: Int, @Body request: ImageMetaUpdateRequest): MessageResponse

    @DELETE("images/{id}")
    suspend fun delete(@Path("id") id: Int): MessageResponse
}
