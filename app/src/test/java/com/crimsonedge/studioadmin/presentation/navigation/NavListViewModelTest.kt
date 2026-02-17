package com.crimsonedge.studioadmin.presentation.navigation

import com.crimsonedge.studioadmin.MainDispatcherRule
import com.crimsonedge.studioadmin.data.remote.dto.CreatedResponse
import com.crimsonedge.studioadmin.data.remote.dto.MessageResponse
import com.crimsonedge.studioadmin.data.remote.dto.NavigationItemRequest
import com.crimsonedge.studioadmin.domain.model.NavigationItem
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
class NavListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sampleNavItem = NavigationItem(
        id = 1, label = "Home", link = "/",
        iconSvg = "<svg/>", displayOrder = 1, isActive = true
    )

    private fun createViewModel(
        repository: NavigationRepository = mockk()
    ): NavListViewModel {
        return NavListViewModel(repository)
    }

    @Test
    fun `init loads nav items`() = runTest {
        val repository = mockk<NavigationRepository> {
            every { getNavItems() } returns flowOf(Resource.Success(listOf(sampleNavItem)))
        }

        val viewModel = createViewModel(repository)

        assertTrue(viewModel.uiState.value.navItems is Resource.Success)
        assertEquals(1, (viewModel.uiState.value.navItems as Resource.Success).data.size)
    }

    @Test
    fun `addNavItem success closes dialog and reloads`() = runTest {
        val request = NavigationItemRequest("About", "/about", null, 2, true)
        val repository = mockk<NavigationRepository> {
            every { getNavItems() } returns flowOf(Resource.Success(listOf(sampleNavItem)))
            every { createNavItem(request) } returns flowOf(
                Resource.Loading, Resource.Success(CreatedResponse(2))
            )
        }

        val viewModel = createViewModel(repository)
        viewModel.showAddDialog()
        viewModel.addNavItem(request)

        assertFalse(viewModel.uiState.value.showAddDialog)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `addNavItem error sets error`() = runTest {
        val request = NavigationItemRequest("About", "/about", null, 2, true)
        val repository = mockk<NavigationRepository> {
            every { getNavItems() } returns flowOf(Resource.Success(emptyList()))
            every { createNavItem(request) } returns flowOf(
                Resource.Loading, Resource.Error("Failed")
            )
        }

        val viewModel = createViewModel(repository)
        viewModel.addNavItem(request)

        assertEquals("Failed", viewModel.uiState.value.error)
    }

    @Test
    fun `updateNavItem success clears editingItem and reloads`() = runTest {
        val request = NavigationItemRequest("Home Updated", "/", null, 1, true)
        val repository = mockk<NavigationRepository> {
            every { getNavItems() } returns flowOf(Resource.Success(listOf(sampleNavItem)))
            every { updateNavItem(1, request) } returns flowOf(
                Resource.Loading, Resource.Success(MessageResponse("Updated"))
            )
        }

        val viewModel = createViewModel(repository)
        viewModel.showEditDialog(sampleNavItem)
        viewModel.updateNavItem(1, request)

        assertNull(viewModel.uiState.value.editingItem)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `updateNavItem error sets error`() = runTest {
        val request = NavigationItemRequest("Home Updated", "/", null, 1, true)
        val repository = mockk<NavigationRepository> {
            every { getNavItems() } returns flowOf(Resource.Success(emptyList()))
            every { updateNavItem(1, request) } returns flowOf(
                Resource.Loading, Resource.Error("Server error")
            )
        }

        val viewModel = createViewModel(repository)
        viewModel.updateNavItem(1, request)

        assertEquals("Server error", viewModel.uiState.value.error)
    }

    @Test
    fun `deleteNavItem success clears deletingItem and reloads`() = runTest {
        val repository = mockk<NavigationRepository> {
            every { getNavItems() } returns flowOf(Resource.Success(listOf(sampleNavItem)))
            every { deleteNavItem(1) } returns flowOf(
                Resource.Loading, Resource.Success(MessageResponse("Deleted"))
            )
        }

        val viewModel = createViewModel(repository)
        viewModel.showDeleteConfirmation(sampleNavItem)
        viewModel.deleteNavItem(1)

        assertNull(viewModel.uiState.value.deletingItem)
        assertFalse(viewModel.uiState.value.isDeleting)
    }

    @Test
    fun `deleteNavItem error sets error`() = runTest {
        val repository = mockk<NavigationRepository> {
            every { getNavItems() } returns flowOf(Resource.Success(emptyList()))
            every { deleteNavItem(1) } returns flowOf(
                Resource.Loading, Resource.Error("Not found")
            )
        }

        val viewModel = createViewModel(repository)
        viewModel.deleteNavItem(1)

        assertEquals("Not found", viewModel.uiState.value.error)
    }

    @Test
    fun `showAddDialog sets showAddDialog true`() = runTest {
        val repository = mockk<NavigationRepository> {
            every { getNavItems() } returns flowOf(Resource.Success(emptyList()))
        }

        val viewModel = createViewModel(repository)
        viewModel.showAddDialog()

        assertTrue(viewModel.uiState.value.showAddDialog)
    }

    @Test
    fun `dismissAddDialog sets showAddDialog false`() = runTest {
        val repository = mockk<NavigationRepository> {
            every { getNavItems() } returns flowOf(Resource.Success(emptyList()))
        }

        val viewModel = createViewModel(repository)
        viewModel.showAddDialog()
        viewModel.dismissAddDialog()

        assertFalse(viewModel.uiState.value.showAddDialog)
    }

    @Test
    fun `showEditDialog sets editingItem`() = runTest {
        val repository = mockk<NavigationRepository> {
            every { getNavItems() } returns flowOf(Resource.Success(emptyList()))
        }

        val viewModel = createViewModel(repository)
        viewModel.showEditDialog(sampleNavItem)

        assertEquals(sampleNavItem, viewModel.uiState.value.editingItem)
    }

    @Test
    fun `dismissEditDialog clears editingItem`() = runTest {
        val repository = mockk<NavigationRepository> {
            every { getNavItems() } returns flowOf(Resource.Success(emptyList()))
        }

        val viewModel = createViewModel(repository)
        viewModel.showEditDialog(sampleNavItem)
        viewModel.dismissEditDialog()

        assertNull(viewModel.uiState.value.editingItem)
    }

    @Test
    fun `showDeleteConfirmation sets deletingItem`() = runTest {
        val repository = mockk<NavigationRepository> {
            every { getNavItems() } returns flowOf(Resource.Success(emptyList()))
        }

        val viewModel = createViewModel(repository)
        viewModel.showDeleteConfirmation(sampleNavItem)

        assertEquals(sampleNavItem, viewModel.uiState.value.deletingItem)
    }

    @Test
    fun `dismissDeleteConfirmation clears deletingItem`() = runTest {
        val repository = mockk<NavigationRepository> {
            every { getNavItems() } returns flowOf(Resource.Success(emptyList()))
        }

        val viewModel = createViewModel(repository)
        viewModel.showDeleteConfirmation(sampleNavItem)
        viewModel.dismissDeleteConfirmation()

        assertNull(viewModel.uiState.value.deletingItem)
    }

    @Test
    fun `clearError clears error`() = runTest {
        val repository = mockk<NavigationRepository> {
            every { getNavItems() } returns flowOf(Resource.Success(emptyList()))
            every { deleteNavItem(1) } returns flowOf(Resource.Error("Error"))
        }

        val viewModel = createViewModel(repository)
        viewModel.deleteNavItem(1)
        viewModel.clearError()

        assertNull(viewModel.uiState.value.error)
    }
}
