package com.crimsonedge.studioadmin.domain.repository

import com.crimsonedge.studioadmin.data.remote.dto.MessageResponse
import com.crimsonedge.studioadmin.domain.model.Contact
import com.crimsonedge.studioadmin.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface ContactRepository {
    fun getAll(unreadOnly: Boolean? = null): Flow<Resource<List<Contact>>>
    fun markAsRead(id: Int): Flow<Resource<MessageResponse>>
    fun delete(id: Int): Flow<Resource<MessageResponse>>
}
