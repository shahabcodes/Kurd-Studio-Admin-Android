package com.crimsonedge.studioadmin.data.repository

import com.crimsonedge.studioadmin.data.remote.api.WritingApi
import com.crimsonedge.studioadmin.data.remote.dto.CreatedResponse
import com.crimsonedge.studioadmin.data.remote.dto.MessageResponse
import com.crimsonedge.studioadmin.data.remote.dto.WritingDto
import com.crimsonedge.studioadmin.data.remote.dto.WritingRequest
import com.crimsonedge.studioadmin.domain.model.Writing
import com.crimsonedge.studioadmin.domain.model.WritingType
import com.crimsonedge.studioadmin.domain.repository.WritingRepository
import com.crimsonedge.studioadmin.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WritingRepositoryImpl @Inject constructor(
    private val api: WritingApi
) : WritingRepository {

    override fun getAll(type: String?): Flow<Resource<List<Writing>>> = flow {
        emit(Resource.Loading)
        try {
            val dtos = api.getAll()
            val writings = dtos
                .let { list ->
                    if (type != null) list.filter { it.typeName == type } else list
                }
                .map { it.toDomain() }
            emit(Resource.Success(writings))
        } catch (e: HttpException) {
            emit(Resource.Error(
                message = e.message() ?: "HTTP error ${e.code()}",
                code = e.code()
            ))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message ?: "Unknown network error"))
        }
    }

    override fun getById(id: Int): Flow<Resource<Writing>> = flow {
        emit(Resource.Loading)
        try {
            val dto = api.getById(id)
            emit(Resource.Success(dto.toDomain()))
        } catch (e: HttpException) {
            emit(Resource.Error(
                message = e.message() ?: "HTTP error ${e.code()}",
                code = e.code()
            ))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message ?: "Unknown network error"))
        }
    }

    override fun create(request: WritingRequest): Flow<Resource<CreatedResponse>> = flow {
        emit(Resource.Loading)
        try {
            val response = api.create(request)
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

    override fun update(id: Int, request: WritingRequest): Flow<Resource<MessageResponse>> = flow {
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

    override fun getTypes(): Flow<Resource<List<WritingType>>> = flow {
        emit(Resource.Loading)
        try {
            val dtos = api.getTypes()
            val types = dtos.map { dto ->
                WritingType(
                    id = dto.id,
                    typeName = dto.typeName,
                    displayName = dto.displayName,
                    displayOrder = dto.displayOrder
                )
            }
            emit(Resource.Success(types))
        } catch (e: HttpException) {
            emit(Resource.Error(
                message = e.message() ?: "HTTP error ${e.code()}",
                code = e.code()
            ))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message ?: "Unknown network error"))
        }
    }

    private fun WritingDto.toDomain() = Writing(
        id = id,
        title = title,
        slug = slug,
        writingTypeId = writingTypeId,
        typeName = typeName,
        typeDisplayName = typeDisplayName,
        subtitle = subtitle,
        excerpt = excerpt,
        fullContent = fullContent,
        datePublished = datePublished,
        novelName = novelName,
        chapterNumber = chapterNumber,
        displayOrder = displayOrder,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
