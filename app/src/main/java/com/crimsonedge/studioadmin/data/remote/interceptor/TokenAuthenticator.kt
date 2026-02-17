package com.crimsonedge.studioadmin.data.remote.interceptor

import com.crimsonedge.studioadmin.data.local.TokenDataStore
import com.crimsonedge.studioadmin.data.remote.api.AuthApi
import com.crimsonedge.studioadmin.data.remote.dto.RefreshRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenDataStore: TokenDataStore,
    @Named("auth") private val authApi: AuthApi
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        return runBlocking {
            mutex.withLock {
                val currentToken = tokenDataStore.accessToken.first()
                val requestToken = response.request.header("Authorization")
                    ?.removePrefix("Bearer ")

                // If the token has already been refreshed by another thread, retry with the new token
                if (currentToken != null && currentToken != requestToken) {
                    return@runBlocking response.request.newBuilder()
                        .header("Authorization", "Bearer $currentToken")
                        .build()
                }

                val refreshToken = tokenDataStore.refreshToken.first()

                if (refreshToken.isNullOrBlank()) {
                    tokenDataStore.clear()
                    return@runBlocking null
                }

                try {
                    val refreshResponse = authApi.refresh(RefreshRequest(refreshToken))

                    tokenDataStore.saveTokens(
                        accessToken = refreshResponse.accessToken,
                        refreshToken = refreshResponse.refreshToken,
                        username = refreshResponse.username,
                        displayName = refreshResponse.displayName,
                        expiresAt = parseExpiresAt(refreshResponse.expiresAt)
                    )

                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${refreshResponse.accessToken}")
                        .build()
                } catch (e: Exception) {
                    tokenDataStore.clear()
                    null
                }
            }
        }
    }

    private fun parseExpiresAt(expiresAt: String): Long {
        return try {
            java.time.Instant.parse(expiresAt).toEpochMilli()
        } catch (e: Exception) {
            0L
        }
    }
}
