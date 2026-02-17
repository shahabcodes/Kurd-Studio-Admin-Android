package com.crimsonedge.studioadmin.data.repository

import com.crimsonedge.studioadmin.data.remote.api.ArtworkApi
import com.crimsonedge.studioadmin.data.remote.dto.ArtworkDto
import com.crimsonedge.studioadmin.data.remote.dto.ArtworkRequest
import com.crimsonedge.studioadmin.data.remote.dto.CreatedResponse
import com.crimsonedge.studioadmin.data.remote.dto.MessageResponse
import com.crimsonedge.studioadmin.domain.model.Artwork
import com.crimsonedge.studioadmin.domain.model.ArtworkType
import com.crimsonedge.studioadmin.domain.repository.ArtworkRepository
import com.crimsonedge.studioadmin.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtworkRepositoryImpl @Inject constructor(
    private val api: ArtworkApi
) : ArtworkRepository {

    override fun getAll(type: String?): Flow<Resource<List<Artwork>>> = flow {
        emit(Resource.Loading)
        try {
            val dtos = api.getAll()
            val artworks = dtos
                .let { list ->
                    if (type != null) list.filter { it.typeName == type } else list
                }
                .map { it.toDomain() }
            emit(Resource.Success(artworks))
        } catch (e: HttpException) {
            emit(Resource.Error(
                message = e.message() ?: "HTTP error ${e.code()}",
                code = e.code()
            ))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message ?: "Unknown network error"))
        }
    }

    override fun getById(id: Int): Flow<Resource<Artwork>> = flow {
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

    override fun create(request: ArtworkRequest): Flow<Resource<CreatedResponse>> = flow {
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

    override fun update(id: Int, request: ArtworkRequest): Flow<Resource<MessageResponse>> = flow {
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

    override fun getTypes(): Flow<Resource<List<ArtworkType>>> = flow {
        emit(Resource.Loading)
        try {
            val dtos = api.getTypes()
            val types = dtos.map { dto ->
                ArtworkType(
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

    private fun ArtworkDto.toDomain() = Artwork(
        id = id,
        title = title,
        slug = slug,
        artworkTypeId = artworkTypeId,
        typeName = typeName,
        typeDisplayName = typeDisplayName,
        imageId = imageId,
        description = description,
        isFeatured = isFeatured,
        displayOrder = displayOrder,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
