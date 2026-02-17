package com.crimsonedge.studioadmin.domain.repository

import com.crimsonedge.studioadmin.data.remote.dto.CreatedResponse
import com.crimsonedge.studioadmin.data.remote.dto.MessageResponse
import com.crimsonedge.studioadmin.data.remote.dto.WritingRequest
import com.crimsonedge.studioadmin.domain.model.Writing
import com.crimsonedge.studioadmin.domain.model.WritingType
import com.crimsonedge.studioadmin.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface WritingRepository {
    fun getAll(type: String? = null): Flow<Resource<List<Writing>>>
    fun getById(id: Int): Flow<Resource<Writing>>
    fun create(request: WritingRequest): Flow<Resource<CreatedResponse>>
    fun update(id: Int, request: WritingRequest): Flow<Resource<MessageResponse>>
    fun delete(id: Int): Flow<Resource<MessageResponse>>
    fun getTypes(): Flow<Resource<List<WritingType>>>
}
