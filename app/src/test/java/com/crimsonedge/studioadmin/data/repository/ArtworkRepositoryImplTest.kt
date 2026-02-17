package com.crimsonedge.studioadmin.data.repository

import com.crimsonedge.studioadmin.data.remote.api.ArtworkApi
import com.crimsonedge.studioadmin.data.remote.dto.ArtworkDto
import com.crimsonedge.studioadmin.data.remote.dto.ArtworkRequest
import com.crimsonedge.studioadmin.data.remote.dto.ArtworkTypeDto
import com.crimsonedge.studioadmin.data.remote.dto.CreatedResponse
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

class ArtworkRepositoryImplTest {

    private lateinit var api: ArtworkApi
    private lateinit var repository: ArtworkRepositoryImpl

    private val sampleDto = ArtworkDto(
        id = 1,
        title = "Test Art",
        slug = "test-art",
        artworkTypeId = 1,
        typeName = "painting",
        typeDisplayName = "Painting",
        imageId = 10,
        description = "A description",
        isFeatured = true,
        displayOrder = 1,
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-02T00:00:00Z"
    )

    private val sampleDto2 = sampleDto.copy(id = 2, typeName = "digital", typeDisplayName = "Digital")

    @Before
    fun setup() {
        api = mockk()
        repository = ArtworkRepositoryImpl(api)
    }

    // region getAll

    @Test
    fun `getAll success emits Loading then Success with mapped artworks`() = runTest {
        coEvery { api.getAll() } returns listOf(sampleDto, sampleDto2)

        val results = repository.getAll().toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
        val artworks = (results[1] as Resource.Success).data
        assertEquals(2, artworks.size)
        assertEquals("Test Art", artworks[0].title)
        assertEquals("test-art", artworks[0].slug)
        assertEquals(1, artworks[0].artworkTypeId)
    }

    @Test
    fun `getAll with type filter returns only matching artworks`() = runTest {
        coEvery { api.getAll() } returns listOf(sampleDto, sampleDto2)

        val results = repository.getAll(type = "painting").toList()

        assertTrue(results[1] is Resource.Success)
        val artworks = (results[1] as Resource.Success).data
        assertEquals(1, artworks.size)
        assertEquals("painting", artworks[0].typeName)
    }

    @Test
    fun `getAll HTTP error emits Loading then Error`() = runTest {
        coEvery { api.getAll() } throws HttpException(
            Response.error<List<ArtworkDto>>(403, "Forbidden".toResponseBody())
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
        val artwork = (results[1] as Resource.Success).data
        assertEquals(1, artwork.id)
        assertEquals("Test Art", artwork.title)
    }

    @Test
    fun `getById HTTP error emits Error`() = runTest {
        coEvery { api.getById(999) } throws HttpException(
            Response.error<ArtworkDto>(404, "Not Found".toResponseBody())
        )

        val results = repository.getById(999).toList()

        assertTrue(results[1] is Resource.Error)
        assertEquals(404, (results[1] as Resource.Error).code)
    }

    // endregion

    // region create

    @Test
    fun `create success emits Loading then Success with CreatedResponse`() = runTest {
        val request = ArtworkRequest("New Art", "new-art", 1, 10, null, false, 0)
        coEvery { api.create(request) } returns CreatedResponse(5)

        val results = repository.create(request).toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
        assertEquals(5, (results[1] as Resource.Success).data.id)
    }

    @Test
    fun `create HTTP error emits Error`() = runTest {
        val request = ArtworkRequest("New Art", "new-art", 1, 10, null, false, 0)
        coEvery { api.create(request) } throws HttpException(
            Response.error<CreatedResponse>(400, "Bad Request".toResponseBody())
        )

        val results = repository.create(request).toList()

        assertTrue(results[1] is Resource.Error)
        assertEquals(400, (results[1] as Resource.Error).code)
    }

    // endregion

    // region update

    @Test
    fun `update success emits Loading then Success`() = runTest {
        val request = ArtworkRequest("Updated Art", "updated-art", 1, 10, null, false, 0)
        coEvery { api.update(1, request) } returns MessageResponse("Updated")

        val results = repository.update(1, request).toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
        assertEquals("Updated", (results[1] as Resource.Success).data.message)
    }

    @Test
    fun `update network error emits Error`() = runTest {
        val request = ArtworkRequest("Updated Art", "updated-art", 1, 10, null, false, 0)
        coEvery { api.update(1, request) } throws IOException("Timeout")

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
        assertEquals(404, (results[1] as Resource.Error).code)
    }

    // endregion

    // region getTypes

    @Test
    fun `getTypes success emits Loading then Success with mapped types`() = runTest {
        val typeDtos = listOf(
            ArtworkTypeDto(1, "painting", "Painting", 1),
            ArtworkTypeDto(2, "digital", "Digital Art", 2)
        )
        coEvery { api.getTypes() } returns typeDtos

        val results = repository.getTypes().toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
        val types = (results[1] as Resource.Success).data
        assertEquals(2, types.size)
        assertEquals("painting", types[0].typeName)
        assertEquals("Digital Art", types[1].displayName)
    }

    @Test
    fun `getTypes HTTP error emits Error`() = runTest {
        coEvery { api.getTypes() } throws HttpException(
            Response.error<List<ArtworkTypeDto>>(500, "Error".toResponseBody())
        )

        val results = repository.getTypes().toList()

        assertTrue(results[1] is Resource.Error)
    }

    // endregion
}
