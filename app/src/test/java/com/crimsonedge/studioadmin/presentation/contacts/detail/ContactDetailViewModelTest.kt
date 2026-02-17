package com.crimsonedge.studioadmin.presentation.contacts.detail

import androidx.lifecycle.SavedStateHandle
import com.crimsonedge.studioadmin.MainDispatcherRule
import com.crimsonedge.studioadmin.data.remote.dto.MessageResponse
import com.crimsonedge.studioadmin.domain.model.Contact
import com.crimsonedge.studioadmin.domain.repository.ContactRepository
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
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ContactDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val unreadContact = Contact(
        id = 1, name = "John", email = "john@example.com",
        subject = "Hello", budget = "$500", message = "Message text",
        submittedAt = "2024-01-01T00:00:00Z", isRead = false, isResponded = false
    )

    private val readContact = unreadContact.copy(isRead = true)

    private fun createViewModel(
        id: Int = 1,
        repository: ContactRepository = mockk()
    ): ContactDetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("id" to id))
        return ContactDetailViewModel(repository, savedStateHandle)
    }

    @Test
    fun `init loads contact from savedStateHandle id`() = runTest {
        val repository = mockk<ContactRepository> {
            every { getAll(null) } returns flowOf(Resource.Success(listOf(unreadContact)))
            every { markAsRead(1) } returns flowOf(Resource.Success(MessageResponse("Marked")))
        }

        val viewModel = createViewModel(1, repository)

        val state = viewModel.uiState.value
        assertTrue(state.contact is Resource.Success)
        val contact = (state.contact as Resource.Success).data
        assertEquals("John", contact.name)
        assertEquals("john@example.com", contact.email)
    }

    @Test
    fun `init auto-marks unread contact as read`() = runTest {
        val repository = mockk<ContactRepository> {
            every { getAll(null) } returns flowOf(Resource.Success(listOf(unreadContact)))
            every { markAsRead(1) } returns flowOf(Resource.Success(MessageResponse("Marked")))
        }

        createViewModel(1, repository)

        verify { repository.markAsRead(1) }
    }

    @Test
    fun `init does not mark already read contact`() = runTest {
        val repository = mockk<ContactRepository> {
            every { getAll(null) } returns flowOf(Resource.Success(listOf(readContact)))
        }

        createViewModel(1, repository)

        verify(exactly = 0) { repository.markAsRead(any()) }
    }

    @Test
    fun `contact not found sets error`() = runTest {
        val repository = mockk<ContactRepository> {
            every { getAll(null) } returns flowOf(Resource.Success(emptyList()))
        }

        val viewModel = createViewModel(999, repository)

        assertTrue(viewModel.uiState.value.contact is Resource.Error)
        assertEquals("Contact not found", (viewModel.uiState.value.contact as Resource.Error).message)
    }

    @Test
    fun `getAll error propagates to contact state`() = runTest {
        val repository = mockk<ContactRepository> {
            every { getAll(null) } returns flowOf(Resource.Error("Server error"))
        }

        val viewModel = createViewModel(1, repository)

        assertTrue(viewModel.uiState.value.contact is Resource.Error)
        assertEquals("Server error", (viewModel.uiState.value.contact as Resource.Error).message)
    }

    @Test
    fun `deleteContact success sets deleteSuccess true`() = runTest {
        val repository = mockk<ContactRepository> {
            every { getAll(null) } returns flowOf(Resource.Success(listOf(readContact)))
            every { delete(1) } returns flowOf(
                Resource.Loading, Resource.Success(MessageResponse("Deleted"))
            )
        }

        val viewModel = createViewModel(1, repository)
        viewModel.deleteContact()

        assertTrue(viewModel.uiState.value.deleteSuccess)
        assertFalse(viewModel.uiState.value.isDeleting)
    }

    @Test
    fun `deleteContact error sets error`() = runTest {
        val repository = mockk<ContactRepository> {
            every { getAll(null) } returns flowOf(Resource.Success(listOf(readContact)))
            every { delete(1) } returns flowOf(
                Resource.Loading, Resource.Error("Failed to delete")
            )
        }

        val viewModel = createViewModel(1, repository)
        viewModel.deleteContact()

        assertEquals("Failed to delete", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isDeleting)
    }

    @Test
    fun `clearError clears error`() = runTest {
        val repository = mockk<ContactRepository> {
            every { getAll(null) } returns flowOf(Resource.Success(listOf(readContact)))
            every { delete(1) } returns flowOf(Resource.Error("Error"))
        }

        val viewModel = createViewModel(1, repository)
        viewModel.deleteContact()
        viewModel.clearError()

        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `initial state has loading contact`() {
        val repository = mockk<ContactRepository> {
            every { getAll(null) } returns flowOf(Resource.Loading)
        }

        val viewModel = createViewModel(1, repository)

        assertTrue(viewModel.uiState.value.contact is Resource.Loading)
    }
}
