package com.crimsonedge.studioadmin.data.repository

import com.crimsonedge.studioadmin.data.remote.api.ContactApi
import com.crimsonedge.studioadmin.data.remote.dto.MessageResponse
import com.crimsonedge.studioadmin.domain.model.Contact
import com.crimsonedge.studioadmin.domain.repository.ContactRepository
import com.crimsonedge.studioadmin.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepositoryImpl @Inject constructor(
    private val api: ContactApi
) : ContactRepository {

    override fun getAll(unreadOnly: Boolean?): Flow<Resource<List<Contact>>> = flow {
        emit(Resource.Loading)
        try {
            val dtos = api.getAll()
            val contacts = dtos
                .let { list ->
                    when (unreadOnly) {
                        true -> list.filter { !it.isRead }
                        false -> list.filter { it.isRead }
                        null -> list
                    }
                }
                .map { dto ->
                    Contact(
                        id = dto.id,
                        name = dto.name,
                        email = dto.email,
                        subject = dto.subject,
                        budget = dto.budget,
                        message = dto.message,
                        submittedAt = dto.submittedAt,
                        isRead = dto.isRead,
                        isResponded = dto.isResponded
                    )
                }
            emit(Resource.Success(contacts))
        } catch (e: HttpException) {
            emit(Resource.Error(
                message = e.message() ?: "HTTP error ${e.code()}",
                code = e.code()
            ))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message ?: "Unknown network error"))
        }
    }

    override fun markAsRead(id: Int): Flow<Resource<MessageResponse>> = flow {
        emit(Resource.Loading)
        try {
            val response = api.markAsRead(id)
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
}
