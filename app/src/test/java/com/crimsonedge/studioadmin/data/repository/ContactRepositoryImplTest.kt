package com.crimsonedge.studioadmin.data.repository

import com.crimsonedge.studioadmin.data.remote.api.ContactApi
import com.crimsonedge.studioadmin.data.remote.dto.ContactSubmissionDto
import com.crimsonedge.studioadmin.data.remote.dto.MessageResponse
import com.crimsonedge.studioadmin.domain.util.Resource
import io.mockk.coEvery
import io.mockk.mockk
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

class ContactRepositoryImplTest {

    private lateinit var api: ContactApi
    private lateinit var repository: ContactRepositoryImpl

    private val readContact = ContactSubmissionDto(
        id = 1, name = "John", email = "john@example.com",
        subject = "Hello", budget = "$500", message = "Message text",
        submittedAt = "2024-01-01T00:00:00Z", isRead = true, isResponded = false
    )

    private val unreadContact = ContactSubmissionDto(
        id = 2, name = "Jane", email = "jane@example.com",
        subject = "Inquiry", budget = null, message = "Another message",
        submittedAt = "2024-01-02T00:00:00Z", isRead = false, isResponded = false
    )

    @Before
    fun setup() {
        api = mockk()
        repository = ContactRepositoryImpl(api)
    }

    // region getAll

    @Test
    fun `getAll with null filter returns all contacts`() = runTest {
        coEvery { api.getAll() } returns listOf(readContact, unreadContact)

        val results = repository.getAll(null).toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
        val contacts = (results[1] as Resource.Success).data
        assertEquals(2, contacts.size)
    }

    @Test
    fun `getAll with unreadOnly=true returns only unread contacts`() = runTest {
        coEvery { api.getAll() } returns listOf(readContact, unreadContact)

        val results = repository.getAll(unreadOnly = true).toList()

        assertTrue(results[1] is Resource.Success)
        val contacts = (results[1] as Resource.Success).data
        assertEquals(1, contacts.size)
        assertEquals("Jane", contacts[0].name)
        assertEquals(false, contacts[0].isRead)
    }

    @Test
    fun `getAll with unreadOnly=false returns only read contacts`() = runTest {
        coEvery { api.getAll() } returns listOf(readContact, unreadContact)

        val results = repository.getAll(unreadOnly = false).toList()

        assertTrue(results[1] is Resource.Success)
        val contacts = (results[1] as Resource.Success).data
        assertEquals(1, contacts.size)
        assertEquals("John", contacts[0].name)
        assertEquals(true, contacts[0].isRead)
    }

    @Test
    fun `getAll maps dto fields correctly`() = runTest {
        coEvery { api.getAll() } returns listOf(readContact)

        val results = repository.getAll(null).toList()

        val contact = (results[1] as Resource.Success).data[0]
        assertEquals(1, contact.id)
        assertEquals("John", contact.name)
        assertEquals("john@example.com", contact.email)
        assertEquals("Hello", contact.subject)
        assertEquals("$500", contact.budget)
        assertEquals("Message text", contact.message)
    }

    @Test
    fun `getAll HTTP error emits Error`() = runTest {
        coEvery { api.getAll() } throws HttpException(
            Response.error<List<ContactSubmissionDto>>(403, "Forbidden".toResponseBody())
        )

        val results = repository.getAll(null).toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Error)
        assertEquals(403, (results[1] as Resource.Error).code)
    }

    @Test
    fun `getAll network error emits Error`() = runTest {
        coEvery { api.getAll() } throws IOException("No internet")

        val results = repository.getAll(null).toList()

        assertTrue(results[1] is Resource.Error)
        assertEquals("No internet", (results[1] as Resource.Error).message)
    }

    // endregion

    // region markAsRead

    @Test
    fun `markAsRead success emits Loading then Success`() = runTest {
        coEvery { api.markAsRead(1) } returns MessageResponse("Marked as read")

        val results = repository.markAsRead(1).toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
    }

    @Test
    fun `markAsRead HTTP error emits Error`() = runTest {
        coEvery { api.markAsRead(1) } throws HttpException(
            Response.error<MessageResponse>(404, "Not Found".toResponseBody())
        )

        val results = repository.markAsRead(1).toList()

        assertTrue(results[1] is Resource.Error)
    }

    // endregion

    // region delete

    @Test
    fun `delete success emits Loading then Success`() = runTest {
        coEvery { api.delete(1) } returns MessageResponse("Deleted")

        val results = repository.delete(1).toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
    }

    @Test
    fun `delete HTTP error emits Error`() = runTest {
        coEvery { api.delete(1) } throws HttpException(
            Response.error<MessageResponse>(404, "Not Found".toResponseBody())
        )

        val results = repository.delete(1).toList()

        assertTrue(results[1] is Resource.Error)
        assertEquals(404, (results[1] as Resource.Error).code)
    }

    @Test
    fun `delete network error emits Error`() = runTest {
        coEvery { api.delete(1) } throws IOException("Timeout")

        val results = repository.delete(1).toList()

        assertTrue(results[1] is Resource.Error)
    }

    // endregion
}
