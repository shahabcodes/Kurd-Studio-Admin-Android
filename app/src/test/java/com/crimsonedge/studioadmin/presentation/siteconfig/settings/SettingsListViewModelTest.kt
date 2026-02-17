package com.crimsonedge.studioadmin.presentation.siteconfig.settings

import com.crimsonedge.studioadmin.MainDispatcherRule
import com.crimsonedge.studioadmin.data.remote.dto.MessageResponse
import com.crimsonedge.studioadmin.domain.model.SiteSetting
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
class SettingsListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sampleSettings = listOf(
        SiteSetting(1, "theme", "dark", "string", "2024-01-01T00:00:00Z"),
        SiteSetting(2, "language", "en", "string", "2024-01-01T00:00:00Z")
    )

    private fun createViewModel(
        repository: SiteRepository = mockk()
    ): SettingsListViewModel {
        return SettingsListViewModel(repository)
    }

    @Test
    fun `init loads settings`() = runTest {
        val repository = mockk<SiteRepository> {
            every { getSettings() } returns flowOf(Resource.Success(sampleSettings))
        }

        val viewModel = createViewModel(repository)

        assertTrue(viewModel.uiState.value.settings is Resource.Success)
        assertEquals(2, (viewModel.uiState.value.settings as Resource.Success).data.size)
    }

    @Test
    fun `loadSettings sets Loading then Success`() = runTest {
        val repository = mockk<SiteRepository> {
            every { getSettings() } returns flowOf(Resource.Success(sampleSettings))
        }

        val viewModel = createViewModel(repository)
        viewModel.loadSettings()

        assertTrue(viewModel.uiState.value.settings is Resource.Success)
    }

    @Test
    fun `startEdit sets editingKey and editingValue`() = runTest {
        val repository = mockk<SiteRepository> {
            every { getSettings() } returns flowOf(Resource.Success(sampleSettings))
        }

        val viewModel = createViewModel(repository)
        viewModel.startEdit(sampleSettings[0])

        assertEquals("theme", viewModel.uiState.value.editingKey)
        assertEquals("dark", viewModel.uiState.value.editingValue)
    }

    @Test
    fun `startEdit with null settingValue sets empty editingValue`() = runTest {
        val settingWithNull = SiteSetting(3, "feature", null, "string", "2024-01-01T00:00:00Z")
        val repository = mockk<SiteRepository> {
            every { getSettings() } returns flowOf(Resource.Success(listOf(settingWithNull)))
        }

        val viewModel = createViewModel(repository)
        viewModel.startEdit(settingWithNull)

        assertEquals("feature", viewModel.uiState.value.editingKey)
        assertEquals("", viewModel.uiState.value.editingValue)
    }

    @Test
    fun `updateEditValue updates the editing value`() = runTest {
        val repository = mockk<SiteRepository> {
            every { getSettings() } returns flowOf(Resource.Success(sampleSettings))
        }

        val viewModel = createViewModel(repository)
        viewModel.startEdit(sampleSettings[0])
        viewModel.updateEditValue("light")

        assertEquals("light", viewModel.uiState.value.editingValue)
    }

    @Test
    fun `cancelEdit clears editing state`() = runTest {
        val repository = mockk<SiteRepository> {
            every { getSettings() } returns flowOf(Resource.Success(sampleSettings))
        }

        val viewModel = createViewModel(repository)
        viewModel.startEdit(sampleSettings[0])
        viewModel.cancelEdit()

        assertNull(viewModel.uiState.value.editingKey)
        assertEquals("", viewModel.uiState.value.editingValue)
    }

    @Test
    fun `saveEdit success clears editing state and sets saveSuccess`() = runTest {
        val repository = mockk<SiteRepository> {
            every { getSettings() } returns flowOf(Resource.Success(sampleSettings))
            every { updateSetting("theme", any()) } returns flowOf(
                Resource.Loading, Resource.Success(MessageResponse("Updated"))
            )
        }

        val viewModel = createViewModel(repository)
        viewModel.startEdit(sampleSettings[0])
        viewModel.updateEditValue("light")
        viewModel.saveEdit()

        assertNull(viewModel.uiState.value.editingKey)
        assertTrue(viewModel.uiState.value.saveSuccess)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `saveEdit sends blank value as null`() = runTest {
        val repository = mockk<SiteRepository> {
            every { getSettings() } returns flowOf(Resource.Success(sampleSettings))
            every { updateSetting("theme", any()) } returns flowOf(
                Resource.Success(MessageResponse("Updated"))
            )
        }

        val viewModel = createViewModel(repository)
        viewModel.startEdit(sampleSettings[0])
        viewModel.updateEditValue("   ")
        viewModel.saveEdit()

        verify {
            repository.updateSetting("theme", match { it.settingValue == null })
        }
    }

    @Test
    fun `saveEdit error sets error message`() = runTest {
        val repository = mockk<SiteRepository> {
            every { getSettings() } returns flowOf(Resource.Success(sampleSettings))
            every { updateSetting("theme", any()) } returns flowOf(
                Resource.Loading, Resource.Error("Server error")
            )
        }

        val viewModel = createViewModel(repository)
        viewModel.startEdit(sampleSettings[0])
        viewModel.saveEdit()

        assertEquals("Server error", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `clearSaveSuccess clears flag`() = runTest {
        val repository = mockk<SiteRepository> {
            every { getSettings() } returns flowOf(Resource.Success(sampleSettings))
            every { updateSetting(any(), any()) } returns flowOf(Resource.Success(MessageResponse("OK")))
        }

        val viewModel = createViewModel(repository)
        viewModel.startEdit(sampleSettings[0])
        viewModel.saveEdit()
        viewModel.clearSaveSuccess()

        assertFalse(viewModel.uiState.value.saveSuccess)
    }
}
