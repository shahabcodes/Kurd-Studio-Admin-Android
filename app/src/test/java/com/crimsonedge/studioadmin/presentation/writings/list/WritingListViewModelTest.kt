package com.crimsonedge.studioadmin.presentation.writings.list

import com.crimsonedge.studioadmin.MainDispatcherRule
import com.crimsonedge.studioadmin.data.remote.dto.MessageResponse
import com.crimsonedge.studioadmin.domain.model.Writing
import com.crimsonedge.studioadmin.domain.model.WritingType
import com.crimsonedge.studioadmin.domain.repository.WritingRepository
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
class WritingListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sampleWriting = Writing(
        id = 1, title = "Poem", slug = "poem", writingTypeId = 1,
        typeName = "poem", typeDisplayName = "Poem", subtitle = null,
        excerpt = null, fullContent = null, datePublished = null,
        novelName = null, chapterNumber = null, displayOrder = 1,
        createdAt = "2024-01-01T00:00:00Z", updatedAt = "2024-01-01T00:00:00Z"
    )

    private val sampleTypes = listOf(
        WritingType(1, "poem", "Poem", 1),
        WritingType(2, "novel", "Novel", 2)
    )

    private fun createViewModel(
        repository: WritingRepository = mockk()
    ): WritingListViewModel {
        return WritingListViewModel(repository)
    }

    @Test
    fun `init loads writings and types`() = runTest {
        val repository = mockk<WritingRepository> {
            every { getAll(null) } returns flowOf(Resource.Success(listOf(sampleWriting)))
            every { getTypes() } returns flowOf(Resource.Success(sampleTypes))
        }

        val viewModel = createViewModel(repository)

        val state = viewModel.uiState.value
        assertTrue(state.writings is Resource.Success)
        assertEquals(2, state.types.size)
    }

    @Test
    fun `setTypeFilter updates selectedType and reloads`() = runTest {
        val repository = mockk<WritingRepository> {
            every { getAll(any()) } returns flowOf(Resource.Success(listOf(sampleWriting)))
            every { getTypes() } returns flowOf(Resource.Success(sampleTypes))
        }

        val viewModel = createViewModel(repository)
        viewModel.setTypeFilter("poem")

        assertEquals("poem", viewModel.uiState.value.selectedType)
        verify { repository.getAll("poem") }
    }

    @Test
    fun `deleteWriting success reloads list`() = runTest {
        val repository = mockk<WritingRepository> {
            every { getAll(null) } returns flowOf(Resource.Success(listOf(sampleWriting)))
            every { getTypes() } returns flowOf(Resource.Success(emptyList()))
            every { delete(1) } returns flowOf(Resource.Loading, Resource.Success(MessageResponse("Deleted")))
        }

        val viewModel = createViewModel(repository)
        viewModel.deleteWriting(1)

        assertEquals(false, viewModel.uiState.value.isDeleting)
        assertNull(viewModel.uiState.value.deleteError)
    }

    @Test
    fun `deleteWriting error sets deleteError`() = runTest {
        val repository = mockk<WritingRepository> {
            every { getAll(null) } returns flowOf(Resource.Success(emptyList()))
            every { getTypes() } returns flowOf(Resource.Success(emptyList()))
            every { delete(1) } returns flowOf(Resource.Loading, Resource.Error("Not found", 404))
        }

        val viewModel = createViewModel(repository)
        viewModel.deleteWriting(1)

        assertEquals("Not found", viewModel.uiState.value.deleteError)
    }

    @Test
    fun `clearDeleteError clears the error`() = runTest {
        val repository = mockk<WritingRepository> {
            every { getAll(null) } returns flowOf(Resource.Success(emptyList()))
            every { getTypes() } returns flowOf(Resource.Success(emptyList()))
            every { delete(1) } returns flowOf(Resource.Error("Error"))
        }

        val viewModel = createViewModel(repository)
        viewModel.deleteWriting(1)
        viewModel.clearDeleteError()

        assertNull(viewModel.uiState.value.deleteError)
    }

    @Test
    fun `setTypeFilter to null clears filter`() = runTest {
        val repository = mockk<WritingRepository> {
            every { getAll(any()) } returns flowOf(Resource.Success(emptyList()))
            every { getTypes() } returns flowOf(Resource.Success(emptyList()))
        }

        val viewModel = createViewModel(repository)
        viewModel.setTypeFilter("poem")
        viewModel.setTypeFilter(null)

        assertNull(viewModel.uiState.value.selectedType)
    }
}
