package com.crimsonedge.studioadmin.presentation.writings.form

import androidx.lifecycle.SavedStateHandle
import com.crimsonedge.studioadmin.MainDispatcherRule
import com.crimsonedge.studioadmin.data.remote.dto.CreatedResponse
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WritingFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sampleTypes = listOf(
        WritingType(1, "poem", "Poem", 1),
        WritingType(2, "novel", "Novel", 2)
    )

    private val sampleWriting = Writing(
        id = 1, title = "Existing Poem", slug = "existing-poem", writingTypeId = 2,
        typeName = "novel", typeDisplayName = "Novel", subtitle = "A subtitle",
        excerpt = "An excerpt", fullContent = "Full content", datePublished = "2024-06-15",
        novelName = "My Novel", chapterNumber = 3, displayOrder = 5,
        createdAt = "2024-01-01T00:00:00Z", updatedAt = "2024-01-01T00:00:00Z"
    )

    private fun createViewModel(
        id: String = "new",
        repository: WritingRepository = mockk()
    ): WritingFormViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("id" to id))
        return WritingFormViewModel(savedStateHandle, repository)
    }

    @Test
    fun `create mode has isEditing false`() = runTest {
        val repository = mockk<WritingRepository> {
            every { getTypes() } returns flowOf(Resource.Success(sampleTypes))
        }

        val viewModel = createViewModel("new", repository)

        assertFalse(viewModel.uiState.value.isEditing)
    }

    @Test
    fun `edit mode has isEditing true and loads existing writing`() = runTest {
        val repository = mockk<WritingRepository> {
            every { getTypes() } returns flowOf(Resource.Success(sampleTypes))
            every { getById(1) } returns flowOf(Resource.Success(sampleWriting))
        }

        val viewModel = createViewModel("1", repository)

        assertTrue(viewModel.uiState.value.isEditing)
        assertEquals("Existing Poem", viewModel.uiState.value.title)
        assertEquals("existing-poem", viewModel.uiState.value.slug)
        assertEquals(2, viewModel.uiState.value.writingTypeId)
        assertEquals("A subtitle", viewModel.uiState.value.subtitle)
        assertEquals("An excerpt", viewModel.uiState.value.excerpt)
        assertEquals("Full content", viewModel.uiState.value.fullContent)
        assertEquals("2024-06-15", viewModel.uiState.value.datePublished)
        assertEquals("My Novel", viewModel.uiState.value.novelName)
        assertEquals("3", viewModel.uiState.value.chapterNumber)
        assertEquals("5", viewModel.uiState.value.displayOrder)
    }

    @Test
    fun `updateTitle updates title and clears titleError`() {
        val repository = mockk<WritingRepository> {
            every { getTypes() } returns flowOf(Resource.Success(emptyList()))
        }
        val viewModel = createViewModel("new", repository)

        viewModel.updateTitle("New Title")

        assertEquals("New Title", viewModel.uiState.value.title)
        assertNull(viewModel.uiState.value.titleError)
    }

    @Test
    fun `updateSlug updates slug and clears slugError`() {
        val repository = mockk<WritingRepository> {
            every { getTypes() } returns flowOf(Resource.Success(emptyList()))
        }
        val viewModel = createViewModel("new", repository)

        viewModel.updateSlug("new-slug")

        assertEquals("new-slug", viewModel.uiState.value.slug)
        assertNull(viewModel.uiState.value.slugError)
    }

    @Test
    fun `generateSlug creates slug from title`() {
        val repository = mockk<WritingRepository> {
            every { getTypes() } returns flowOf(Resource.Success(emptyList()))
        }
        val viewModel = createViewModel("new", repository)

        viewModel.updateTitle("My Cool Poem!")
        viewModel.generateSlug()

        assertEquals("my-cool-poem", viewModel.uiState.value.slug)
    }

    @Test
    fun `updateSubtitle updates subtitle`() {
        val repository = mockk<WritingRepository> {
            every { getTypes() } returns flowOf(Resource.Success(emptyList()))
        }
        val viewModel = createViewModel("new", repository)

        viewModel.updateSubtitle("Sub")

        assertEquals("Sub", viewModel.uiState.value.subtitle)
    }

    @Test
    fun `save validates title is required`() {
        val repository = mockk<WritingRepository> {
            every { getTypes() } returns flowOf(Resource.Success(emptyList()))
        }
        val viewModel = createViewModel("new", repository)

        viewModel.updateSlug("has-slug")
        viewModel.save()

        assertEquals("Title is required", viewModel.uiState.value.titleError)
    }

    @Test
    fun `save validates slug is required`() {
        val repository = mockk<WritingRepository> {
            every { getTypes() } returns flowOf(Resource.Success(emptyList()))
        }
        val viewModel = createViewModel("new", repository)

        viewModel.updateTitle("Has Title")
        viewModel.save()

        assertEquals("Slug is required", viewModel.uiState.value.slugError)
    }

    @Test
    fun `save creates new writing in create mode`() = runTest {
        val repository = mockk<WritingRepository> {
            every { getTypes() } returns flowOf(Resource.Success(sampleTypes))
            every { create(any()) } returns flowOf(Resource.Loading, Resource.Success(CreatedResponse(5)))
        }

        val viewModel = createViewModel("new", repository)
        viewModel.updateTitle("New Poem")
        viewModel.updateSlug("new-poem")
        viewModel.save()

        assertTrue(viewModel.uiState.value.saveSuccess)
        verify { repository.create(any()) }
    }

    @Test
    fun `save updates existing writing in edit mode`() = runTest {
        val repository = mockk<WritingRepository> {
            every { getTypes() } returns flowOf(Resource.Success(sampleTypes))
            every { getById(1) } returns flowOf(Resource.Success(sampleWriting))
            every { update(1, any()) } returns flowOf(Resource.Loading, Resource.Success(MessageResponse("Updated")))
        }

        val viewModel = createViewModel("1", repository)
        viewModel.save()

        assertTrue(viewModel.uiState.value.saveSuccess)
        verify { repository.update(1, any()) }
    }

    @Test
    fun `save error sets error message`() = runTest {
        val repository = mockk<WritingRepository> {
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
    fun `clearError clears the error`() {
        val repository = mockk<WritingRepository> {
            every { getTypes() } returns flowOf(Resource.Error("Error"))
        }
        val viewModel = createViewModel("new", repository)
        viewModel.clearError()

        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `types are loaded and default typeId set from first type`() = runTest {
        val repository = mockk<WritingRepository> {
            every { getTypes() } returns flowOf(Resource.Success(sampleTypes))
        }

        val viewModel = createViewModel("new", repository)

        assertEquals(2, viewModel.uiState.value.types.size)
        assertEquals(1, viewModel.uiState.value.writingTypeId)
    }
}
