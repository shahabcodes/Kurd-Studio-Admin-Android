package com.crimsonedge.studioadmin.data.repository

import com.crimsonedge.studioadmin.data.remote.api.ImageApi
import com.crimsonedge.studioadmin.data.remote.dto.ImageMetaDto
import com.crimsonedge.studioadmin.data.remote.dto.ImageMetaUpdateRequest
import com.crimsonedge.studioadmin.data.remote.dto.MessageResponse
import com.crimsonedge.studioadmin.domain.util.Resource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ImageRepositoryImplTest {

    private lateinit var api: ImageApi
    private lateinit var repository: ImageRepositoryImpl

    private val sampleDto = ImageMetaDto(
        id = 1,
        fileName = "photo.jpg",
        contentType = "image/jpeg",
        altText = "A photo",
        fileSize = 1024,
        width = 800,
        height = 600,
        imageUrl = "http://example.com/photo.jpg",
        thumbnailUrl = "http://example.com/photo_thumb.jpg",
        createdAt = "2024-01-01T00:00:00Z"
    )

    @Before
    fun setup() {
        api = mockk()
        repository = ImageRepositoryImpl(api)
    }

    // region getAll

    @Test
    fun `getAll success emits Loading then Success with mapped images`() = runTest {
        coEvery { api.getAll() } returns listOf(sampleDto)

        val results = repository.getAll().toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
        val images = (results[1] as Resource.Success).data
        assertEquals(1, images.size)
        assertEquals("photo.jpg", images[0].fileName)
        assertEquals("image/jpeg", images[0].contentType)
        assertEquals(1024L, images[0].fileSize)
        assertEquals(800, images[0].width)
    }

    @Test
    fun `getAll HTTP error emits Error`() = runTest {
        coEvery { api.getAll() } throws HttpException(
            Response.error<List<ImageMetaDto>>(500, "Error".toResponseBody())
        )

        val results = repository.getAll().toList()

        assertTrue(results[1] is Resource.Error)
        assertEquals(500, (results[1] as Resource.Error).code)
    }

    @Test
    fun `getAll network error emits Error`() = runTest {
        coEvery { api.getAll() } throws IOException("No internet")

        val results = repository.getAll().toList()

        assertTrue(results[1] is Resource.Error)
        assertEquals("No internet", (results[1] as Resource.Error).message)
    }

    // endregion

    // region upload

    @Test
    fun `upload success emits Loading then Success`() = runTest {
        val part = mockk<MultipartBody.Part>()
        coEvery { api.upload(part) } returns sampleDto

        val results = repository.upload(part).toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
        val image = (results[1] as Resource.Success).data
        assertEquals("photo.jpg", image.fileName)
    }

    @Test
    fun `upload HTTP error emits Error`() = runTest {
        val part = mockk<MultipartBody.Part>()
        coEvery { api.upload(part) } throws HttpException(
            Response.error<ImageMetaDto>(413, "Too Large".toResponseBody())
        )

        val results = repository.upload(part).toList()

        assertTrue(results[1] is Resource.Error)
        assertEquals(413, (results[1] as Resource.Error).code)
    }

    // endregion

    // region updateMeta

    @Test
    fun `updateMeta success emits Loading then Success`() = runTest {
        val request = ImageMetaUpdateRequest("renamed.jpg", "alt text")
        coEvery { api.update(1, request) } returns MessageResponse("Updated")

        val results = repository.updateMeta(1, request).toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
    }

    @Test
    fun `updateMeta network error emits Error`() = runTest {
        val request = ImageMetaUpdateRequest("renamed.jpg", "alt text")
        coEvery { api.update(1, request) } throws IOException("Timeout")

        val results = repository.updateMeta(1, request).toList()

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
}
