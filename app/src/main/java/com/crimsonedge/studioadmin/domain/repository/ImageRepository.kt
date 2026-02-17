package com.crimsonedge.studioadmin.domain.repository

import com.crimsonedge.studioadmin.data.remote.dto.ImageMetaUpdateRequest
import com.crimsonedge.studioadmin.data.remote.dto.MessageResponse
import com.crimsonedge.studioadmin.domain.model.ImageMeta
import com.crimsonedge.studioadmin.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody

interface ImageRepository {
    fun getAll(): Flow<Resource<List<ImageMeta>>>
    fun upload(file: MultipartBody.Part): Flow<Resource<ImageMeta>>
    fun updateMeta(id: Int, request: ImageMetaUpdateRequest): Flow<Resource<MessageResponse>>
    fun delete(id: Int): Flow<Resource<MessageResponse>>
}
