package com.crimsonedge.studioadmin.presentation.auth

import com.crimsonedge.studioadmin.MainDispatcherRule
import com.crimsonedge.studioadmin.domain.model.UserSession
import com.crimsonedge.studioadmin.domain.repository.AuthRepository
import com.crimsonedge.studioadmin.domain.util.Resource
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        authRepository = mockk(relaxed = true)
        viewModel = LoginViewModel(authRepository)
    }

    @Test
    fun `initial state has empty fields and no errors`() {
        val state = viewModel.uiState.value

        assertEquals("", state.username)
        assertEquals("", state.password)
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertFalse(state.isSuccess)
    }

    @Test
    fun `onUsernameChange updates username and clears error`() {
        viewModel.onUsernameChange("admin")

        assertEquals("admin", viewModel.uiState.value.username)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `onPasswordChange updates password and clears error`() {
        viewModel.onPasswordChange("secret")

        assertEquals("secret", viewModel.uiState.value.password)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `login with empty username sets error`() {
        viewModel.onPasswordChange("pass")
        viewModel.login()

        assertEquals("Username is required", viewModel.uiState.value.error)
    }

    @Test
    fun `login with empty password sets error`() {
        viewModel.onUsernameChange("admin")
        viewModel.login()

        assertEquals("Password is required", viewModel.uiState.value.error)
    }

    @Test
    fun `login with blank username sets error`() {
        viewModel.onUsernameChange("   ")
        viewModel.onPasswordChange("pass")
        viewModel.login()

        assertEquals("Username is required", viewModel.uiState.value.error)
    }

    @Test
    fun `login success sets isSuccess true`() = runTest {
        val session = UserSession("access", "refresh", "admin", "Admin")
        every { authRepository.login("admin", "pass") } returns flowOf(
            Resource.Loading,
            Resource.Success(session)
        )

        viewModel.onUsernameChange("admin")
        viewModel.onPasswordChange("pass")
        viewModel.login()

        assertTrue(viewModel.uiState.value.isSuccess)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `login error sets error message`() = runTest {
        every { authRepository.login("admin", "wrong") } returns flowOf(
            Resource.Loading,
            Resource.Error("Invalid credentials", 401)
        )

        viewModel.onUsernameChange("admin")
        viewModel.onPasswordChange("wrong")
        viewModel.login()

        assertEquals("Invalid credentials", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isSuccess)
    }

    @Test
    fun `login trims username before sending`() = runTest {
        every { authRepository.login("admin", "pass") } returns flowOf(
            Resource.Loading,
            Resource.Success(UserSession("a", "r", "admin", null))
        )

        viewModel.onUsernameChange("  admin  ")
        viewModel.onPasswordChange("pass")
        viewModel.login()

        verify { authRepository.login("admin", "pass") }
    }
}
