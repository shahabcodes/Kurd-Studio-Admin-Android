package com.crimsonedge.studioadmin.data.repository

import com.crimsonedge.studioadmin.data.remote.api.ImageApi
import com.crimsonedge.studioadmin.data.remote.dto.BatchDeleteRequest
import com.crimsonedge.studioadmin.data.remote.dto.ImageMetaDto
import com.crimsonedge.studioadmin.data.remote.dto.ImageMetaUpdateRequest
import com.crimsonedge.studioadmin.data.remote.dto.MessageResponse
import com.crimsonedge.studioadmin.domain.model.ImageMeta
import com.crimsonedge.studioadmin.domain.repository.ImageRepository
import com.crimsonedge.studioadmin.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRepositoryImpl @Inject constructor(
    private val api: ImageApi
) : ImageRepository {

    override fun getAll(): Flow<Resource<List<ImageMeta>>> = flow {
        emit(Resource.Loading)
        try {
            val dtos = api.getAll()
            val images = dtos.map { it.toDomain() }
            emit(Resource.Success(images))
        } catch (e: HttpException) {
            emit(Resource.Error(
                message = e.message() ?: "HTTP error ${e.code()}",
                code = e.code()
            ))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message ?: "Unknown network error"))
        }
    }

    override fun upload(file: MultipartBody.Part): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val response = api.upload(file)
            if (response.isSuccessful) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error(
                    message = "Upload failed: HTTP ${response.code()}",
                    code = response.code()
                ))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(
                message = e.message() ?: "HTTP error ${e.code()}",
                code = e.code()
            ))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message ?: "Unknown network error"))
        }
    }

    override fun updateMeta(id: Int, request: ImageMetaUpdateRequest): Flow<Resource<MessageResponse>> = flow {
        emit(Resource.Loading)
        try {
            val response = api.update(id, request)
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            emit(Resource.Error(
                message = e.message() ?: "HTTP error ${e.code()}",
                code = e.code()
            ))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message ?: "Unknown network error"))
        }
    }

    override fun delete(id: Int): Flow<Resource<MessageResponse>> = flow {
        emit(Resource.Loading)
        try {
            val response = api.delete(id)
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            emit(Resource.Error(
                message = e.message() ?: "HTTP error ${e.code()}",
                code = e.code()
            ))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message ?: "Unknown network error"))
        }
    }

    override fun deleteBatch(ids: List<Int>): Flow<Resource<MessageResponse>> = flow {
        emit(Resource.Loading)
        try {
            val response = api.deleteBatch(BatchDeleteRequest(ids))
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            emit(Resource.Error(message = e.message() ?: "HTTP error ${e.code()}", code = e.code()))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message ?: "Unknown network error"))
        }
    }

    private fun ImageMetaDto.toDomain() = ImageMeta(
        id = id,
        fileName = fileName,
        contentType = contentType,
        altText = altText,
        fileSize = fileSize,
        width = width,
        height = height,
        imageUrl = imageUrl,
        thumbnailUrl = thumbnailUrl,
        createdAt = createdAt
    )
}
