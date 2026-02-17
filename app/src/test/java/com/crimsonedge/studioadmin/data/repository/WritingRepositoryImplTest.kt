package com.crimsonedge.studioadmin.data.repository

import com.crimsonedge.studioadmin.data.remote.api.WritingApi
import com.crimsonedge.studioadmin.data.remote.dto.CreatedResponse
import com.crimsonedge.studioadmin.data.remote.dto.MessageResponse
import com.crimsonedge.studioadmin.data.remote.dto.WritingDto
import com.crimsonedge.studioadmin.data.remote.dto.WritingRequest
import com.crimsonedge.studioadmin.data.remote.dto.WritingTypeDto
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

class WritingRepositoryImplTest {

    private lateinit var api: WritingApi
    private lateinit var repository: WritingRepositoryImpl

    private val sampleDto = WritingDto(
        id = 1,
        title = "Test Poem",
        slug = "test-poem",
        writingTypeId = 1,
        typeName = "poem",
        typeDisplayName = "Poem",
        subtitle = "A subtitle",
        excerpt = "An excerpt",
        fullContent = "Full content here",
        datePublished = "2024-01-01",
        novelName = null,
        chapterNumber = null,
        displayOrder = 1,
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-02T00:00:00Z"
    )

    private val sampleDto2 = sampleDto.copy(id = 2, typeName = "novel", typeDisplayName = "Novel")

    @Before
    fun setup() {
        api = mockk()
        repository = WritingRepositoryImpl(api)
    }

    // region getAll

    @Test
    fun `getAll success emits Loading then Success with mapped writings`() = runTest {
        coEvery { api.getAll() } returns listOf(sampleDto, sampleDto2)

        val results = repository.getAll().toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
        val writings = (results[1] as Resource.Success).data
        assertEquals(2, writings.size)
        assertEquals("Test Poem", writings[0].title)
        assertEquals("A subtitle", writings[0].subtitle)
    }

    @Test
    fun `getAll with type filter returns only matching writings`() = runTest {
        coEvery { api.getAll() } returns listOf(sampleDto, sampleDto2)

        val results = repository.getAll(type = "poem").toList()

        assertTrue(results[1] is Resource.Success)
        val writings = (results[1] as Resource.Success).data
        assertEquals(1, writings.size)
        assertEquals("poem", writings[0].typeName)
    }

    @Test
    fun `getAll HTTP error emits Loading then Error`() = runTest {
        coEvery { api.getAll() } throws HttpException(
            Response.error<List<WritingDto>>(403, "Forbidden".toResponseBody())
        )

        val results = repository.getAll().toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Error)
        assertEquals(403, (results[1] as Resource.Error).code)
    }

    @Test
    fun `getAll network error emits Loading then Error`() = runTest {
        coEvery { api.getAll() } throws IOException("Timeout")

        val results = repository.getAll().toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Error)
        assertEquals("Timeout", (results[1] as Resource.Error).message)
    }

    // endregion

    // region getById

    @Test
    fun `getById success emits Loading then Success`() = runTest {
        coEvery { api.getById(1) } returns sampleDto

        val results = repository.getById(1).toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
        val writing = (results[1] as Resource.Success).data
        assertEquals(1, writing.id)
        assertEquals("test-poem", writing.slug)
    }

    @Test
    fun `getById HTTP error emits Error`() = runTest {
        coEvery { api.getById(999) } throws HttpException(
            Response.error<WritingDto>(404, "Not Found".toResponseBody())
        )

        val results = repository.getById(999).toList()

        assertTrue(results[1] is Resource.Error)
        assertEquals(404, (results[1] as Resource.Error).code)
    }

    // endregion

    // region create

    @Test
    fun `create success emits Loading then Success`() = runTest {
        val request = WritingRequest("New", "new", 1, null, null, null, null, null, null, 0)
        coEvery { api.create(request) } returns CreatedResponse(5)

        val results = repository.create(request).toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
        assertEquals(5, (results[1] as Resource.Success).data.id)
    }

    @Test
    fun `create HTTP error emits Error`() = runTest {
        val request = WritingRequest("New", "new", 1, null, null, null, null, null, null, 0)
        coEvery { api.create(request) } throws HttpException(
            Response.error<CreatedResponse>(400, "Bad".toResponseBody())
        )

        val results = repository.create(request).toList()

        assertTrue(results[1] is Resource.Error)
    }

    // endregion

    // region update

    @Test
    fun `update success emits Loading then Success`() = runTest {
        val request = WritingRequest("Updated", "updated", 1, null, null, null, null, null, null, 0)
        coEvery { api.update(1, request) } returns MessageResponse("Updated")

        val results = repository.update(1, request).toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
    }

    @Test
    fun `update network error emits Error`() = runTest {
        val request = WritingRequest("Updated", "updated", 1, null, null, null, null, null, null, 0)
        coEvery { api.update(1, request) } throws IOException("Fail")

        val results = repository.update(1, request).toList()

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
    }

    // endregion

    // region getTypes

    @Test
    fun `getTypes success emits Loading then Success with mapped types`() = runTest {
        val typeDtos = listOf(
            WritingTypeDto(1, "poem", "Poem", 1),
            WritingTypeDto(2, "novel", "Novel", 2)
        )
        coEvery { api.getTypes() } returns typeDtos

        val results = repository.getTypes().toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
        val types = (results[1] as Resource.Success).data
        assertEquals(2, types.size)
        assertEquals("poem", types[0].typeName)
        assertEquals("Novel", types[1].displayName)
    }

    @Test
    fun `getTypes HTTP error emits Error`() = runTest {
        coEvery { api.getTypes() } throws HttpException(
            Response.error<List<WritingTypeDto>>(500, "Error".toResponseBody())
        )

        val results = repository.getTypes().toList()

        assertTrue(results[1] is Resource.Error)
    }

    // endregion
}
