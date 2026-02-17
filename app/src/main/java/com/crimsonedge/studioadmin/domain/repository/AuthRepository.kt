package com.crimsonedge.studioadmin.domain.repository

import com.crimsonedge.studioadmin.domain.model.UserSession
import com.crimsonedge.studioadmin.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun login(username: String, password: String): Flow<Resource<UserSession>>
    suspend fun logout()
    val isLoggedIn: Flow<Boolean>
    val displayName: Flow<String?>
}
