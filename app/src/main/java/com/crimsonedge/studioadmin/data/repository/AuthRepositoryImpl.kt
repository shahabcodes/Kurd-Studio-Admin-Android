package com.crimsonedge.studioadmin.data.repository

import com.crimsonedge.studioadmin.data.local.TokenDataStore
import com.crimsonedge.studioadmin.data.remote.api.AuthApi
import com.crimsonedge.studioadmin.data.remote.dto.LoginRequest
import com.crimsonedge.studioadmin.data.remote.dto.RefreshRequest
import com.crimsonedge.studioadmin.domain.model.UserSession
import com.crimsonedge.studioadmin.domain.repository.AuthRepository
import com.crimsonedge.studioadmin.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val tokenDataStore: TokenDataStore
) : AuthRepository {

    override fun login(username: String, password: String): Flow<Resource<UserSession>> = flow {
        emit(Resource.Loading)
        try {
            val response = api.login(LoginRequest(username, password))
            val expiresAt = try {
                Instant.parse(response.expiresAt).toEpochMilli()
            } catch (e: Exception) {
                System.currentTimeMillis() + 3600000L
            }
            tokenDataStore.saveTokens(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                username = response.username,
                displayName = response.displayName,
                expiresAt = expiresAt
            )
            val session = UserSession(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                username = response.username,
                displayName = response.displayName
            )
            emit(Resource.Success(session))
        } catch (e: HttpException) {
            emit(Resource.Error(
                message = e.message() ?: "HTTP error ${e.code()}",
                code = e.code()
            ))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message ?: "Unknown network error"))
        }
    }

    override suspend fun logout() {
        try {
            val refreshToken = tokenDataStore.refreshToken.firstOrNull()
            if (refreshToken != null) {
                api.logout(RefreshRequest(refreshToken))
            }
        } catch (_: Exception) {
            // Ignore logout API errors
        } finally {
            tokenDataStore.clear()
        }
    }

    override val isLoggedIn: Flow<Boolean>
        get() = tokenDataStore.isLoggedIn

    override val displayName: Flow<String?>
        get() = tokenDataStore.displayName
}
