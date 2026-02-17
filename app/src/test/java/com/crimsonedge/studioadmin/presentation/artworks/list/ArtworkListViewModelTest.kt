package com.crimsonedge.studioadmin.presentation.artworks.list

import com.crimsonedge.studioadmin.MainDispatcherRule
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArtworkListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sampleArtwork = Artwork(
        id = 1, title = "Art", slug = "art", artworkTypeId = 1,
        typeName = "painting", typeDisplayName = "Painting", imageId = 10,
        description = "Desc", isFeatured = false, displayOrder = 1,
        createdAt = "2024-01-01T00:00:00Z", updatedAt = "2024-01-01T00:00:00Z"
    )

    private val sampleTypes = listOf(
        ArtworkType(1, "painting", "Painting", 1),
        ArtworkType(2, "digital", "Digital", 2)
    )

    private fun createViewModel(
        artworkRepository: ArtworkRepository = mockk()
    ): ArtworkListViewModel {
        return ArtworkListViewModel(artworkRepository)
    }

    @Test
    fun `init loads artworks and types`() = runTest {
        val repository = mockk<ArtworkRepository> {
            every { getAll(null) } returns flowOf(Resource.Success(listOf(sampleArtwork)))
            every { getTypes() } returns flowOf(Resource.Success(sampleTypes))
        }

        val viewModel = createViewModel(repository)

        val state = viewModel.uiState.value
        assertTrue(state.artworks is Resource.Success)
        assertEquals(1, (state.artworks as Resource.Success).data.size)
        assertEquals(2, state.types.size)
    }

    @Test
    fun `setTypeFilter updates selectedType and reloads`() = runTest {
        val repository = mockk<ArtworkRepository> {
            every { getAll(null) } returns flowOf(Resource.Success(listOf(sampleArtwork)))
            every { getAll("painting") } returns flowOf(Resource.Success(listOf(sampleArtwork)))
            every { getTypes() } returns flowOf(Resource.Success(sampleTypes))
        }

        val viewModel = createViewModel(repository)
        viewModel.setTypeFilter("painting")

        assertEquals("painting", viewModel.uiState.value.selectedType)
        verify { repository.getAll("painting") }
    }

    @Test
    fun `deleteArtwork success reloads list`() = runTest {
        val repository = mockk<ArtworkRepository> {
            every { getAll(null) } returns flowOf(Resource.Success(listOf(sampleArtwork)))
            every { getTypes() } returns flowOf(Resource.Success(emptyList()))
            every { delete(1) } returns flowOf(Resource.Loading, Resource.Success(MessageResponse("Deleted")))
        }

        val viewModel = createViewModel(repository)
        viewModel.deleteArtwork(1)

        val state = viewModel.uiState.value
        assertEquals(false, state.isDeleting)
        assertNull(state.deleteError)
    }

    @Test
    fun `deleteArtwork error sets deleteError`() = runTest {
        val repository = mockk<ArtworkRepository> {
            every { getAll(null) } returns flowOf(Resource.Success(listOf(sampleArtwork)))
            every { getTypes() } returns flowOf(Resource.Success(emptyList()))
            every { delete(1) } returns flowOf(Resource.Loading, Resource.Error("Not found", 404))
        }

        val viewModel = createViewModel(repository)
        viewModel.deleteArtwork(1)

        assertEquals("Not found", viewModel.uiState.value.deleteError)
    }

    @Test
    fun `clearDeleteError clears the error`() = runTest {
        val repository = mockk<ArtworkRepository> {
            every { getAll(null) } returns flowOf(Resource.Success(emptyList()))
            every { getTypes() } returns flowOf(Resource.Success(emptyList()))
            every { delete(1) } returns flowOf(Resource.Error("Error"))
        }

        val viewModel = createViewModel(repository)
        viewModel.deleteArtwork(1)
        viewModel.clearDeleteError()

        assertNull(viewModel.uiState.value.deleteError)
    }

    @Test
    fun `init with error state sets artworks to Error`() = runTest {
        val repository = mockk<ArtworkRepository> {
            every { getAll(null) } returns flowOf(Resource.Error("Server error"))
            every { getTypes() } returns flowOf(Resource.Success(emptyList()))
        }

        val viewModel = createViewModel(repository)

        assertTrue(viewModel.uiState.value.artworks is Resource.Error)
    }

    @Test
    fun `setTypeFilter to null clears filter and reloads all`() = runTest {
        val repository = mockk<ArtworkRepository> {
            every { getAll(any()) } returns flowOf(Resource.Success(listOf(sampleArtwork)))
            every { getTypes() } returns flowOf(Resource.Success(emptyList()))
        }

        val viewModel = createViewModel(repository)
        viewModel.setTypeFilter("painting")
        viewModel.setTypeFilter(null)

        assertNull(viewModel.uiState.value.selectedType)
    }
}
