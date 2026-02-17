package com.crimsonedge.studioadmin.presentation.social

import com.crimsonedge.studioadmin.MainDispatcherRule
import com.crimsonedge.studioadmin.data.remote.dto.CreatedResponse
import com.crimsonedge.studioadmin.data.remote.dto.MessageResponse
import com.crimsonedge.studioadmin.data.remote.dto.SocialLinkRequest
import com.crimsonedge.studioadmin.domain.model.SocialLink
import com.crimsonedge.studioadmin.domain.repository.NavigationRepository
import com.crimsonedge.studioadmin.domain.util.Resource
import io.mockk.every
import io.mockk.mockk
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
class SocialLinksViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sampleLink = SocialLink(
        id = 1, platform = "Instagram", url = "https://ig.com/artist",
        iconSvg = "<svg/>", displayOrder = 1, isActive = true
    )

    private fun createViewModel(
        repository: NavigationRepository = mockk()
    ): SocialLinksViewModel {
        return SocialLinksViewModel(repository)
    }

    @Test
    fun `init loads social links`() = runTest {
        val repository = mockk<NavigationRepository> {
            every { getSocialLinks() } returns flowOf(Resource.Success(listOf(sampleLink)))
        }

        val viewModel = createViewModel(repository)

        assertTrue(viewModel.uiState.value.socialLinks is Resource.Success)
        assertEquals(1, (viewModel.uiState.value.socialLinks as Resource.Success).data.size)
    }

    @Test
    fun `addSocialLink success closes dialog and reloads`() = runTest {
        val request = SocialLinkRequest("Twitter", "https://twitter.com/artist", null, 2, true)
        val repository = mockk<NavigationRepository> {
            every { getSocialLinks() } returns flowOf(Resource.Success(listOf(sampleLink)))
            every { createSocialLink(request) } returns flowOf(
                Resource.Loading, Resource.Success(CreatedResponse(2))
            )
        }

        val viewModel = createViewModel(repository)
        viewModel.showAddDialog()
        viewModel.addSocialLink(request)

        assertFalse(viewModel.uiState.value.showAddDialog)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `addSocialLink error sets error`() = runTest {
        val request = SocialLinkRequest("Twitter", "https://twitter.com", null, 2, true)
        val repository = mockk<NavigationRepository> {
            every { getSocialLinks() } returns flowOf(Resource.Success(emptyList()))
            every { createSocialLink(request) } returns flowOf(
                Resource.Loading, Resource.Error("Failed")
            )
        }

        val viewModel = createViewModel(repository)
        viewModel.addSocialLink(request)

        assertEquals("Failed", viewModel.uiState.value.error)
    }

    @Test
    fun `updateSocialLink success clears editingLink and reloads`() = runTest {
        val request = SocialLinkRequest("Instagram", "https://ig.com/new", null, 1, true)
        val repository = mockk<NavigationRepository> {
            every { getSocialLinks() } returns flowOf(Resource.Success(listOf(sampleLink)))
            every { updateSocialLink(1, request) } returns flowOf(
                Resource.Loading, Resource.Success(MessageResponse("Updated"))
            )
        }

        val viewModel = createViewModel(repository)
        viewModel.showEditDialog(sampleLink)
        viewModel.updateSocialLink(1, request)

        assertNull(viewModel.uiState.value.editingLink)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `updateSocialLink error sets error`() = runTest {
        val request = SocialLinkRequest("Instagram", "https://ig.com/new", null, 1, true)
        val repository = mockk<NavigationRepository> {
            every { getSocialLinks() } returns flowOf(Resource.Success(emptyList()))
            every { updateSocialLink(1, request) } returns flowOf(
                Resource.Loading, Resource.Error("Server error")
            )
        }

        val viewModel = createViewModel(repository)
        viewModel.updateSocialLink(1, request)

        assertEquals("Server error", viewModel.uiState.value.error)
    }

    @Test
    fun `deleteSocialLink success clears deletingLink and reloads`() = runTest {
        val repository = mockk<NavigationRepository> {
            every { getSocialLinks() } returns flowOf(Resource.Success(listOf(sampleLink)))
            every { deleteSocialLink(1) } returns flowOf(
                Resource.Loading, Resource.Success(MessageResponse("Deleted"))
            )
        }

        val viewModel = createViewModel(repository)
        viewModel.showDeleteConfirmation(sampleLink)
        viewModel.deleteSocialLink(1)

        assertNull(viewModel.uiState.value.deletingLink)
        assertFalse(viewModel.uiState.value.isDeleting)
    }

    @Test
    fun `deleteSocialLink error sets error`() = runTest {
        val repository = mockk<NavigationRepository> {
            every { getSocialLinks() } returns flowOf(Resource.Success(emptyList()))
            every { deleteSocialLink(1) } returns flowOf(
                Resource.Loading, Resource.Error("Not found")
            )
        }

        val viewModel = createViewModel(repository)
        viewModel.deleteSocialLink(1)

        assertEquals("Not found", viewModel.uiState.value.error)
    }

    @Test
    fun `showAddDialog sets flag true`() = runTest {
        val repository = mockk<NavigationRepository> {
            every { getSocialLinks() } returns flowOf(Resource.Success(emptyList()))
        }

        val viewModel = createViewModel(repository)
        viewModel.showAddDialog()

        assertTrue(viewModel.uiState.value.showAddDialog)
    }

    @Test
    fun `dismissAddDialog sets flag false`() = runTest {
        val repository = mockk<NavigationRepository> {
            every { getSocialLinks() } returns flowOf(Resource.Success(emptyList()))
        }

        val viewModel = createViewModel(repository)
        viewModel.showAddDialog()
        viewModel.dismissAddDialog()

        assertFalse(viewModel.uiState.value.showAddDialog)
    }

    @Test
    fun `showEditDialog sets editingLink`() = runTest {
        val repository = mockk<NavigationRepository> {
            every { getSocialLinks() } returns flowOf(Resource.Success(emptyList()))
        }

        val viewModel = createViewModel(repository)
        viewModel.showEditDialog(sampleLink)

        assertEquals(sampleLink, viewModel.uiState.value.editingLink)
    }

    @Test
    fun `dismissEditDialog clears editingLink`() = runTest {
        val repository = mockk<NavigationRepository> {
            every { getSocialLinks() } returns flowOf(Resource.Success(emptyList()))
        }

        val viewModel = createViewModel(repository)
        viewModel.showEditDialog(sampleLink)
        viewModel.dismissEditDialog()

        assertNull(viewModel.uiState.value.editingLink)
    }

    @Test
    fun `showDeleteConfirmation sets deletingLink`() = runTest {
        val repository = mockk<NavigationRepository> {
            every { getSocialLinks() } returns flowOf(Resource.Success(emptyList()))
        }

        val viewModel = createViewModel(repository)
        viewModel.showDeleteConfirmation(sampleLink)

        assertEquals(sampleLink, viewModel.uiState.value.deletingLink)
    }

    @Test
    fun `dismissDeleteConfirmation clears deletingLink`() = runTest {
        val repository = mockk<NavigationRepository> {
            every { getSocialLinks() } returns flowOf(Resource.Success(emptyList()))
        }

        val viewModel = createViewModel(repository)
        viewModel.showDeleteConfirmation(sampleLink)
        viewModel.dismissDeleteConfirmation()

        assertNull(viewModel.uiState.value.deletingLink)
    }

    @Test
    fun `clearError clears error`() = runTest {
        val repository = mockk<NavigationRepository> {
            every { getSocialLinks() } returns flowOf(Resource.Success(emptyList()))
            every { deleteSocialLink(1) } returns flowOf(Resource.Error("Error"))
        }

        val viewModel = createViewModel(repository)
        viewModel.deleteSocialLink(1)
        viewModel.clearError()

        assertNull(viewModel.uiState.value.error)
    }
}
