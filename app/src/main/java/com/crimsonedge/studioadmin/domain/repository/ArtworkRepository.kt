package com.crimsonedge.studioadmin.domain.repository

import com.crimsonedge.studioadmin.data.remote.dto.ArtworkRequest
import com.crimsonedge.studioadmin.data.remote.dto.CreatedResponse
import com.crimsonedge.studioadmin.data.remote.dto.MessageResponse
import com.crimsonedge.studioadmin.domain.model.Artwork
import com.crimsonedge.studioadmin.domain.model.ArtworkType
import com.crimsonedge.studioadmin.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface ArtworkRepository {
    fun getAll(type: String? = null): Flow<Resource<List<Artwork>>>
    fun getById(id: Int): Flow<Resource<Artwork>>
    fun create(request: ArtworkRequest): Flow<Resource<CreatedResponse>>
    fun update(id: Int, request: ArtworkRequest): Flow<Resource<MessageResponse>>
    fun delete(id: Int): Flow<Resource<MessageResponse>>
    fun getTypes(): Flow<Resource<List<ArtworkType>>>
}
