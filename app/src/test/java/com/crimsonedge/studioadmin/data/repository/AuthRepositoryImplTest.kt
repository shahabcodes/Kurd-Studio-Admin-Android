package com.crimsonedge.studioadmin.data.repository

import com.crimsonedge.studioadmin.data.local.TokenDataStore
import com.crimsonedge.studioadmin.data.remote.api.AuthApi
import com.crimsonedge.studioadmin.data.remote.dto.LoginRequest
import com.crimsonedge.studioadmin.data.remote.dto.LoginResponse
import com.crimsonedge.studioadmin.data.remote.dto.MessageResponse
import com.crimsonedge.studioadmin.data.remote.dto.RefreshRequest
import com.crimsonedge.studioadmin.domain.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class AuthRepositoryImplTest {

    private lateinit var api: AuthApi
    private lateinit var tokenDataStore: TokenDataStore
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setup() {
        api = mockk()
        tokenDataStore = mockk(relaxed = true)
        repository = AuthRepositoryImpl(api, tokenDataStore)
    }

    // region login

    @Test
    fun `login success emits Loading then Success with UserSession`() = runTest {
        val response = LoginResponse(
            accessToken = "access123",
            refreshToken = "refresh123",
            username = "admin",
            displayName = "Admin User",
            expiresAt = "2025-12-31T23:59:59Z"
        )
        coEvery { api.login(LoginRequest("admin", "pass")) } returns response
        coEvery {
            tokenDataStore.saveTokens(any(), any(), any(), any(), any())
        } returns Unit

        val results = repository.login("admin", "pass").toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
        val session = (results[1] as Resource.Success).data
        assertEquals("access123", session.accessToken)
        assertEquals("refresh123", session.refreshToken)
        assertEquals("admin", session.username)
        assertEquals("Admin User", session.displayName)
    }

    @Test
    fun `login success saves tokens to datastore`() = runTest {
        val response = LoginResponse(
            accessToken = "access123",
            refreshToken = "refresh123",
            username = "admin",
            displayName = "Admin User",
            expiresAt = "2025-12-31T23:59:59Z"
        )
        coEvery { api.login(any()) } returns response
        coEvery {
            tokenDataStore.saveTokens(any(), any(), any(), any(), any())
        } returns Unit

        repository.login("admin", "pass").toList()

        coVerify {
            tokenDataStore.saveTokens(
                accessToken = "access123",
                refreshToken = "refresh123",
                username = "admin",
                displayName = "Admin User",
                expiresAt = any()
            )
        }
    }

    @Test
    fun `login HTTP error emits Loading then Error with code`() = runTest {
        val httpException = HttpException(
            Response.error<LoginResponse>(401, "Unauthorized".toResponseBody())
        )
        coEvery { api.login(any()) } throws httpException

        val results = repository.login("admin", "wrong").toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Error)
        assertEquals(401, (results[1] as Resource.Error).code)
    }

    @Test
    fun `login network error emits Loading then Error`() = runTest {
        coEvery { api.login(any()) } throws IOException("Network error")

        val results = repository.login("admin", "pass").toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Error)
        assertEquals("Network error", (results[1] as Resource.Error).message)
    }

    // endregion

    // region logout

    @Test
    fun `logout clears tokens even on API error`() = runTest {
        every { tokenDataStore.refreshToken } returns flowOf("refresh123")
        coEvery { api.logout(RefreshRequest("refresh123")) } throws IOException("fail")
        coEvery { tokenDataStore.clear() } returns Unit

        repository.logout()

        coVerify { tokenDataStore.clear() }
    }

    @Test
    fun `logout calls api then clears tokens on success`() = runTest {
        every { tokenDataStore.refreshToken } returns flowOf("refresh123")
        coEvery { api.logout(RefreshRequest("refresh123")) } returns MessageResponse("ok")
        coEvery { tokenDataStore.clear() } returns Unit

        repository.logout()

        coVerify { api.logout(RefreshRequest("refresh123")) }
        coVerify { tokenDataStore.clear() }
    }

    @Test
    fun `logout skips api call when no refresh token`() = runTest {
        every { tokenDataStore.refreshToken } returns flowOf(null)
        coEvery { tokenDataStore.clear() } returns Unit

        repository.logout()

        coVerify(exactly = 0) { api.logout(any()) }
        coVerify { tokenDataStore.clear() }
    }

    // endregion

    // region isLoggedIn / displayName

    @Test
    fun `isLoggedIn delegates to tokenDataStore`() {
        every { tokenDataStore.isLoggedIn } returns flowOf(true)

        val flow = repository.isLoggedIn
        assertEquals(tokenDataStore.isLoggedIn, flow)
    }

    @Test
    fun `displayName delegates to tokenDataStore`() {
        every { tokenDataStore.displayName } returns flowOf("Test")

        val flow = repository.displayName
        assertEquals(tokenDataStore.displayName, flow)
    }

    // endregion
}
