package com.crimsonedge.studioadmin.presentation.siteconfig.sections

import com.crimsonedge.studioadmin.MainDispatcherRule
import com.crimsonedge.studioadmin.data.remote.dto.MessageResponse
import com.crimsonedge.studioadmin.domain.model.Section
import com.crimsonedge.studioadmin.domain.repository.SiteRepository
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
class SectionsListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sampleSection = Section(
        id = 1, sectionKey = "hero", tag = "Featured",
        title = "Hero Section", subtitle = "Welcome",
        displayOrder = 1, isActive = true,
        updatedAt = "2024-01-01T00:00:00Z"
    )

    private fun createViewModel(
        repository: SiteRepository = mockk()
    ): SectionsListViewModel {
        return SectionsListViewModel(repository)
    }

    @Test
    fun `init loads sections`() = runTest {
        val repository = mockk<SiteRepository> {
            every { getSections() } returns flowOf(Resource.Success(listOf(sampleSection)))
        }

        val viewModel = createViewModel(repository)

        assertTrue(viewModel.uiState.value.sections is Resource.Success)
        assertEquals(1, (viewModel.uiState.value.sections as Resource.Success).data.size)
    }

    @Test
    fun `startEdit populates editState from section`() = runTest {
        val repository = mockk<SiteRepository> {
            every { getSections() } returns flowOf(Resource.Success(listOf(sampleSection)))
        }

        val viewModel = createViewModel(repository)
        viewModel.startEdit(sampleSection)

        val state = viewModel.uiState.value
        assertEquals(1, state.editingSectionId)
        assertEquals("Featured", state.editState.tag)
        assertEquals("Hero Section", state.editState.title)
        assertEquals("Welcome", state.editState.subtitle)
        assertEquals("1", state.editState.displayOrder)
        assertTrue(state.editState.isActive)
    }

    @Test
    fun `startEdit with null fields maps to empty strings`() = runTest {
        val nullSection = Section(
            id = 2, sectionKey = "about", tag = null,
            title = null, subtitle = null, displayOrder = 0,
            isActive = false, updatedAt = "2024-01-01T00:00:00Z"
        )
        val repository = mockk<SiteRepository> {
            every { getSections() } returns flowOf(Resource.Success(listOf(nullSection)))
        }

        val viewModel = createViewModel(repository)
        viewModel.startEdit(nullSection)

        assertEquals("", viewModel.uiState.value.editState.tag)
        assertEquals("", viewModel.uiState.value.editState.title)
        assertEquals("", viewModel.uiState.value.editState.subtitle)
    }

    @Test
    fun `cancelEdit clears editing state`() = runTest {
        val repository = mockk<SiteRepository> {
            every { getSections() } returns flowOf(Resource.Success(listOf(sampleSection)))
        }

        val viewModel = createViewModel(repository)
        viewModel.startEdit(sampleSection)
        viewModel.cancelEdit()

        assertNull(viewModel.uiState.value.editingSectionId)
        assertEquals(SectionEditState(), viewModel.uiState.value.editState)
    }

    @Test
    fun `updateTag updates editState tag`() = runTest {
        val repository = mockk<SiteRepository> {
            every { getSections() } returns flowOf(Resource.Success(emptyList()))
        }

        val viewModel = createViewModel(repository)
        viewModel.updateTag("New Tag")

        assertEquals("New Tag", viewModel.uiState.value.editState.tag)
    }

    @Test
    fun `updateTitle updates editState title`() = runTest {
        val repository = mockk<SiteRepository> {
            every { getSections() } returns flowOf(Resource.Success(emptyList()))
        }

        val viewModel = createViewModel(repository)
        viewModel.updateTitle("New Title")

        assertEquals("New Title", viewModel.uiState.value.editState.title)
    }

    @Test
    fun `updateIsActive updates editState isActive`() = runTest {
        val repository = mockk<SiteRepository> {
            every { getSections() } returns flowOf(Resource.Success(emptyList()))
        }

        val viewModel = createViewModel(repository)
        viewModel.updateIsActive(false)

        assertFalse(viewModel.uiState.value.editState.isActive)
    }

    @Test
    fun `save sends SectionRequest with blank fields as null`() = runTest {
        val repository = mockk<SiteRepository> {
            every { getSections() } returns flowOf(Resource.Success(listOf(sampleSection)))
            every { updateSection(1, any()) } returns flowOf(
                Resource.Loading, Resource.Success(MessageResponse("Updated"))
            )
        }

        val viewModel = createViewModel(repository)
        viewModel.startEdit(sampleSection)
        viewModel.updateTag("")
        viewModel.save()

        verify {
            repository.updateSection(1, match { request ->
                request.tag == null && request.title == "Hero Section"
            })
        }
    }

    @Test
    fun `save success clears editing state and sets saveSuccess`() = runTest {
        val repository = mockk<SiteRepository> {
            every { getSections() } returns flowOf(Resource.Success(listOf(sampleSection)))
            every { updateSection(1, any()) } returns flowOf(
                Resource.Loading, Resource.Success(MessageResponse("Updated"))
            )
        }

        val viewModel = createViewModel(repository)
        viewModel.startEdit(sampleSection)
        viewModel.save()

        assertNull(viewModel.uiState.value.editingSectionId)
        assertTrue(viewModel.uiState.value.saveSuccess)
    }

    @Test
    fun `save error sets error message`() = runTest {
        val repository = mockk<SiteRepository> {
            every { getSections() } returns flowOf(Resource.Success(listOf(sampleSection)))
            every { updateSection(1, any()) } returns flowOf(
                Resource.Loading, Resource.Error("Server error")
            )
        }

        val viewModel = createViewModel(repository)
        viewModel.startEdit(sampleSection)
        viewModel.save()

        assertEquals("Server error", viewModel.uiState.value.error)
    }

    @Test
    fun `save without editing section does nothing`() = runTest {
        val repository = mockk<SiteRepository> {
            every { getSections() } returns flowOf(Resource.Success(listOf(sampleSection)))
        }

        val viewModel = createViewModel(repository)
        viewModel.save()

        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `clearSaveSuccess clears flag`() = runTest {
        val repository = mockk<SiteRepository> {
            every { getSections() } returns flowOf(Resource.Success(listOf(sampleSection)))
            every { updateSection(1, any()) } returns flowOf(Resource.Success(MessageResponse("OK")))
        }

        val viewModel = createViewModel(repository)
        viewModel.startEdit(sampleSection)
        viewModel.save()
        viewModel.clearSaveSuccess()

        assertFalse(viewModel.uiState.value.saveSuccess)
    }
}
