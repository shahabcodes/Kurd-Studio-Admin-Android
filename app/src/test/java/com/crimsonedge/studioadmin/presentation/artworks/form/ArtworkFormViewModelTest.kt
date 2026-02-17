package com.crimsonedge.studioadmin.presentation.artworks.form

import androidx.lifecycle.SavedStateHandle
import com.crimsonedge.studioadmin.MainDispatcherRule
import com.crimsonedge.studioadmin.data.remote.dto.ArtworkRequest
import com.crimsonedge.studioadmin.data.remote.dto.CreatedResponse
import com.crimsonedge.studioadmin.data.remote.dto.MessageResponse
import com.crimsonedge.studioadmin.domain.model.Artwork
import com.crimsonedge.studioadmin.domain.model.ArtworkType
import com.crimsonedge.studioadmin.domain.repository.ArtworkRepository
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
class ArtworkFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sampleTypes = listOf(
        ArtworkType(1, "painting", "Painting", 1),
        ArtworkType(2, "digital", "Digital", 2)
    )

    private val sampleArtwork = Artwork(
        id = 1, title = "Existing Art", slug = "existing-art", artworkTypeId = 2,
        typeName = "digital", typeDisplayName = "Digital", imageId = 10,
        description = "A description", isFeatured = true, displayOrder = 5,
        createdAt = "2024-01-01T00:00:00Z", updatedAt = "2024-01-01T00:00:00Z"
    )

    private fun createViewModel(
        id: String = "new",
        repository: ArtworkRepository = mockk()
    ): ArtworkFormViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("id" to id))
        return ArtworkFormViewModel(repository, savedStateHandle)
    }

    @Test
    fun `create mode has isEditing false and isLoading false`() = runTest {
        val repository = mockk<ArtworkRepository> {
            every { getTypes() } returns flowOf(Resource.Success(sampleTypes))
        }

        val viewModel = createViewModel("new", repository)

        assertFalse(viewModel.uiState.value.isEditing)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `edit mode has isEditing true and loads existing artwork`() = runTest {
        val repository = mockk<ArtworkRepository> {
            every { getTypes() } returns flowOf(Resource.Success(sampleTypes))
            every { getById(1) } returns flowOf(Resource.Success(sampleArtwork))
        }

        val viewModel = createViewModel("1", repository)

        assertTrue(viewModel.uiState.value.isEditing)
        assertEquals("Existing Art", viewModel.uiState.value.title)
        assertEquals("existing-art", viewModel.uiState.value.slug)
        assertEquals(2, viewModel.uiState.value.artworkTypeId)
        assertEquals(10, viewModel.uiState.value.imageId)
        assertEquals("A description", viewModel.uiState.value.description)
        assertTrue(viewModel.uiState.value.isFeatured)
        assertEquals(5, viewModel.uiState.value.displayOrder)
    }

    @Test
    fun `updateTitle updates title and clears titleError`() {
        val repository = mockk<ArtworkRepository> {
            every { getTypes() } returns flowOf(Resource.Success(emptyList()))
        }
        val viewModel = createViewModel("new", repository)

        viewModel.updateTitle("New Title")

        assertEquals("New Title", viewModel.uiState.value.title)
        assertNull(viewModel.uiState.value.titleError)
    }

    @Test
    fun `updateSlug updates slug and clears slugError`() {
        val repository = mockk<ArtworkRepository> {
            every { getTypes() } returns flowOf(Resource.Success(emptyList()))
        }
        val viewModel = createViewModel("new", repository)

        viewModel.updateSlug("new-slug")

        assertEquals("new-slug", viewModel.uiState.value.slug)
        assertNull(viewModel.uiState.value.slugError)
    }

    @Test
    fun `generateSlug creates slug from title`() {
        val repository = mockk<ArtworkRepository> {
            every { getTypes() } returns flowOf(Resource.Success(emptyList()))
        }
        val viewModel = createViewModel("new", repository)

        viewModel.updateTitle("My Cool Art Piece!")
        viewModel.generateSlug()

        assertEquals("my-cool-art-piece", viewModel.uiState.value.slug)
    }

    @Test
    fun `save validates title is required`() {
        val repository = mockk<ArtworkRepository> {
            every { getTypes() } returns flowOf(Resource.Success(emptyList()))
        }
        val viewModel = createViewModel("new", repository)

        viewModel.updateSlug("has-slug")
        viewModel.save()

        assertEquals("Title is required", viewModel.uiState.value.titleError)
    }

    @Test
    fun `save validates slug is required`() {
        val repository = mockk<ArtworkRepository> {
            every { getTypes() } returns flowOf(Resource.Success(emptyList()))
        }
        val viewModel = createViewModel("new", repository)

        viewModel.updateTitle("Has Title")
        viewModel.save()

        assertEquals("Slug is required", viewModel.uiState.value.slugError)
    }

    @Test
    fun `save creates new artwork in create mode`() = runTest {
        val repository = mockk<ArtworkRepository> {
            every { getTypes() } returns flowOf(Resource.Success(sampleTypes))
            every { create(any()) } returns flowOf(Resource.Loading, Resource.Success(CreatedResponse(5)))
        }

        val viewModel = createViewModel("new", repository)
        viewModel.updateTitle("New Art")
        viewModel.updateSlug("new-art")
        viewModel.save()

        assertTrue(viewModel.uiState.value.saveSuccess)
        verify { repository.create(any()) }
    }

    @Test
    fun `save updates existing artwork in edit mode`() = runTest {
        val repository = mockk<ArtworkRepository> {
            every { getTypes() } returns flowOf(Resource.Success(sampleTypes))
            every { getById(1) } returns flowOf(Resource.Success(sampleArtwork))
            every { update(1, any()) } returns flowOf(Resource.Loading, Resource.Success(MessageResponse("Updated")))
        }

        val viewModel = createViewModel("1", repository)
        viewModel.save()

        assertTrue(viewModel.uiState.value.saveSuccess)
        verify { repository.update(1, any()) }
    }

    @Test
    fun `save error sets error message`() = runTest {
        val repository = mockk<ArtworkRepository> {
            every { getTypes() } returns flowOf(Resource.Success(emptyList()))
            every { create(any()) } returns flowOf(Resource.Loading, Resource.Error("Server error"))
        }

        val viewModel = createViewModel("new", repository)
        viewModel.updateTitle("Title")
        viewModel.updateSlug("slug")
        viewModel.save()

        assertEquals("Server error", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `types are loaded and default typeId set from first type`() = runTest {
        val repository = mockk<ArtworkRepository> {
            every { getTypes() } returns flowOf(Resource.Success(sampleTypes))
        }

        val viewModel = createViewModel("new", repository)

        assertEquals(2, viewModel.uiState.value.types.size)
        assertEquals(1, viewModel.uiState.value.artworkTypeId)
    }
}
