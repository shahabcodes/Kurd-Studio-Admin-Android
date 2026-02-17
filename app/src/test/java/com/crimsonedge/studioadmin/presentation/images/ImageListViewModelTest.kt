package com.crimsonedge.studioadmin.presentation.images

import com.crimsonedge.studioadmin.MainDispatcherRule
import com.crimsonedge.studioadmin.data.remote.dto.MessageResponse
import com.crimsonedge.studioadmin.domain.model.ImageMeta
import com.crimsonedge.studioadmin.domain.repository.ImageRepository
import com.crimsonedge.studioadmin.domain.util.Resource
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ImageListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sampleImage = ImageMeta(
        id = 1, fileName = "photo.jpg", contentType = "image/jpeg",
        altText = "Photo", fileSize = 1024, width = 800, height = 600,
        imageUrl = "http://example.com/photo.jpg",
        thumbnailUrl = "http://example.com/thumb.jpg",
        createdAt = "2024-01-01T00:00:00Z"
    )

    private fun createViewModel(
        repository: ImageRepository = mockk()
    ): ImageListViewModel {
        return ImageListViewModel(repository)
    }

    @Test
    fun `init loads images`() = runTest {
        val repository = mockk<ImageRepository> {
            every { getAll() } returns flowOf(Resource.Success(listOf(sampleImage)))
        }

        val viewModel = createViewModel(repository)

        assertTrue(viewModel.uiState.value.images is Resource.Success)
        assertEquals(1, (viewModel.uiState.value.images as Resource.Success).data.size)
    }

    @Test
    fun `loadImages reloads data`() = runTest {
        val repository = mockk<ImageRepository> {
            every { getAll() } returns flowOf(Resource.Success(listOf(sampleImage)))
        }

        val viewModel = createViewModel(repository)
        viewModel.loadImages()

        assertTrue(viewModel.uiState.value.images is Resource.Success)
    }

    @Test
    fun `uploadImage success reloads list`() = runTest {
        val part = mockk<MultipartBody.Part>()
        val repository = mockk<ImageRepository> {
            every { getAll() } returns flowOf(Resource.Success(listOf(sampleImage)))
            every { upload(part) } returns flowOf(Resource.Loading, Resource.Success(sampleImage))
        }

        val viewModel = createViewModel(repository)
        viewModel.uploadImage(part)

        assertFalse(viewModel.uiState.value.isUploading)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `uploadImage error sets error`() = runTest {
        val part = mockk<MultipartBody.Part>()
        val repository = mockk<ImageRepository> {
            every { getAll() } returns flowOf(Resource.Success(emptyList()))
            every { upload(part) } returns flowOf(Resource.Loading, Resource.Error("Too large"))
        }

        val viewModel = createViewModel(repository)
        viewModel.uploadImage(part)

        assertEquals("Too large", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isUploading)
    }

    @Test
    fun `selectImage sets selectedImage`() = runTest {
        val repository = mockk<ImageRepository> {
            every { getAll() } returns flowOf(Resource.Success(listOf(sampleImage)))
        }

        val viewModel = createViewModel(repository)
        viewModel.selectImage(sampleImage)

        assertEquals(sampleImage, viewModel.uiState.value.selectedImage)
    }

    @Test
    fun `selectImage with null clears selection`() = runTest {
        val repository = mockk<ImageRepository> {
            every { getAll() } returns flowOf(Resource.Success(listOf(sampleImage)))
        }

        val viewModel = createViewModel(repository)
        viewModel.selectImage(sampleImage)
        viewModel.selectImage(null)

        assertNull(viewModel.uiState.value.selectedImage)
    }

    @Test
    fun `deleteImage success clears selectedImage and reloads`() = runTest {
        val repository = mockk<ImageRepository> {
            every { getAll() } returns flowOf(Resource.Success(listOf(sampleImage)))
            every { delete(1) } returns flowOf(Resource.Loading, Resource.Success(MessageResponse("Deleted")))
        }

        val viewModel = createViewModel(repository)
        viewModel.selectImage(sampleImage)
        viewModel.deleteImage(1)

        assertFalse(viewModel.uiState.value.isDeleting)
        assertNull(viewModel.uiState.value.selectedImage)
    }

    @Test
    fun `deleteImage error sets error`() = runTest {
        val repository = mockk<ImageRepository> {
            every { getAll() } returns flowOf(Resource.Success(emptyList()))
            every { delete(1) } returns flowOf(Resource.Loading, Resource.Error("Not found"))
        }

        val viewModel = createViewModel(repository)
        viewModel.deleteImage(1)

        assertEquals("Not found", viewModel.uiState.value.error)
    }

    @Test
    fun `clearError clears the error`() = runTest {
        val repository = mockk<ImageRepository> {
            every { getAll() } returns flowOf(Resource.Success(emptyList()))
            every { delete(1) } returns flowOf(Resource.Error("Error"))
        }

        val viewModel = createViewModel(repository)
        viewModel.deleteImage(1)
        viewModel.clearError()

        assertNull(viewModel.uiState.value.error)
    }
}
