package com.crimsonedge.studioadmin.presentation.contacts.list

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
class ContactListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sampleContact = Contact(
        id = 1, name = "John", email = "john@example.com",
        subject = "Hello", budget = "$500", message = "Message text",
        submittedAt = "2024-01-01T00:00:00Z", isRead = false, isResponded = false
    )

    private fun createViewModel(
        repository: ContactRepository = mockk()
    ): ContactListViewModel {
        return ContactListViewModel(repository)
    }

    @Test
    fun `init loads contacts with null filter`() = runTest {
        val repository = mockk<ContactRepository> {
            every { getAll(null) } returns flowOf(Resource.Success(listOf(sampleContact)))
        }

        val viewModel = createViewModel(repository)

        assertTrue(viewModel.uiState.value.contacts is Resource.Success)
        assertEquals(1, (viewModel.uiState.value.contacts as Resource.Success).data.size)
    }

    @Test
    fun `loadContacts reloads data`() = runTest {
        val repository = mockk<ContactRepository> {
            every { getAll(null) } returns flowOf(Resource.Success(listOf(sampleContact)))
        }

        val viewModel = createViewModel(repository)
        viewModel.loadContacts()

        assertTrue(viewModel.uiState.value.contacts is Resource.Success)
    }

    @Test
    fun `setUnreadFilter true reloads with unreadOnly true`() = runTest {
        val repository = mockk<ContactRepository> {
            every { getAll(null) } returns flowOf(Resource.Success(listOf(sampleContact)))
            every { getAll(true) } returns flowOf(Resource.Success(listOf(sampleContact)))
        }

        val viewModel = createViewModel(repository)
        viewModel.setUnreadFilter(true)

        assertTrue(viewModel.uiState.value.unreadOnly)
        verify { repository.getAll(true) }
    }

    @Test
    fun `setUnreadFilter false reloads with null filter`() = runTest {
        val repository = mockk<ContactRepository> {
            every { getAll(any()) } returns flowOf(Resource.Success(emptyList()))
        }

        val viewModel = createViewModel(repository)
        viewModel.setUnreadFilter(true)
        viewModel.setUnreadFilter(false)

        assertFalse(viewModel.uiState.value.unreadOnly)
    }

    @Test
    fun `deleteContact success reloads list`() = runTest {
        val repository = mockk<ContactRepository> {
            every { getAll(null) } returns flowOf(Resource.Success(listOf(sampleContact)))
            every { delete(1) } returns flowOf(
                Resource.Loading, Resource.Success(MessageResponse("Deleted"))
            )
        }

        val viewModel = createViewModel(repository)
        viewModel.deleteContact(1)

        assertFalse(viewModel.uiState.value.isDeleting)
        assertNull(viewModel.uiState.value.deleteError)
    }

    @Test
    fun `deleteContact error sets deleteError`() = runTest {
        val repository = mockk<ContactRepository> {
            every { getAll(null) } returns flowOf(Resource.Success(emptyList()))
            every { delete(1) } returns flowOf(
                Resource.Loading, Resource.Error("Not found")
            )
        }

        val viewModel = createViewModel(repository)
        viewModel.deleteContact(1)

        assertEquals("Not found", viewModel.uiState.value.deleteError)
        assertFalse(viewModel.uiState.value.isDeleting)
    }

    @Test
    fun `clearDeleteError clears the error`() = runTest {
        val repository = mockk<ContactRepository> {
            every { getAll(null) } returns flowOf(Resource.Success(emptyList()))
            every { delete(1) } returns flowOf(Resource.Error("Error"))
        }

        val viewModel = createViewModel(repository)
        viewModel.deleteContact(1)
        viewModel.clearDeleteError()

        assertNull(viewModel.uiState.value.deleteError)
    }

    @Test
    fun `contacts error state is propagated`() = runTest {
        val repository = mockk<ContactRepository> {
            every { getAll(null) } returns flowOf(Resource.Error("Server error"))
        }

        val viewModel = createViewModel(repository)

        assertTrue(viewModel.uiState.value.contacts is Resource.Error)
    }
}
