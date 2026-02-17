package com.crimsonedge.studioadmin.presentation.siteconfig.profile

import com.crimsonedge.studioadmin.MainDispatcherRule
import com.crimsonedge.studioadmin.data.remote.dto.MessageResponse
import com.crimsonedge.studioadmin.domain.model.Profile
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
class ProfileEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sampleProfile = Profile(
        id = 1, name = "Artist", tagline = "Creative Mind",
        bio = "Bio text", avatarImageId = 5, email = "artist@example.com",
        instagramUrl = "https://ig.com/artist", twitterUrl = "https://twitter.com/artist",
        artworksCount = "50", poemsCount = "20", yearsExperience = "10",
        updatedAt = "2024-01-01T00:00:00Z"
    )

    private fun createViewModel(
        repository: SiteRepository = mockk()
    ): ProfileEditorViewModel {
        return ProfileEditorViewModel(repository)
    }

    @Test
    fun `init loads profile and maps fields correctly`() = runTest {
        val repository = mockk<SiteRepository> {
            every { getProfile() } returns flowOf(Resource.Success(sampleProfile))
        }

        val viewModel = createViewModel(repository)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Artist", state.name)
        assertEquals("Creative Mind", state.tagline)
        assertEquals("Bio text", state.bio)
        assertEquals(5, state.avatarImageId)
        assertEquals("artist@example.com", state.email)
        assertEquals("https://ig.com/artist", state.instagramUrl)
        assertEquals("https://twitter.com/artist", state.twitterUrl)
        assertEquals("50", state.artworksCount)
        assertEquals("20", state.poemsCount)
        assertEquals("10", state.yearsExperience)
    }

    @Test
    fun `loadProfile maps null String fields to empty string`() = runTest {
        val nullProfile = Profile(
            id = 1, name = "Name", tagline = null, bio = null,
            avatarImageId = null, email = null, instagramUrl = null,
            twitterUrl = null, artworksCount = null, poemsCount = null,
            yearsExperience = null, updatedAt = "2024-01-01T00:00:00Z"
        )
        val repository = mockk<SiteRepository> {
            every { getProfile() } returns flowOf(Resource.Success(nullProfile))
        }

        val viewModel = createViewModel(repository)

        val state = viewModel.uiState.value
        assertEquals("Name", state.name)
        assertEquals("", state.tagline)
        assertEquals("", state.bio)
        assertNull(state.avatarImageId)
        assertEquals("", state.email)
    }

    @Test
    fun `loadProfile error sets error`() = runTest {
        val repository = mockk<SiteRepository> {
            every { getProfile() } returns flowOf(Resource.Error("Server error"))
        }

        val viewModel = createViewModel(repository)

        assertEquals("Server error", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `updateName updates name and clears saveSuccess`() {
        val repository = mockk<SiteRepository> {
            every { getProfile() } returns flowOf(Resource.Loading)
        }
        val viewModel = createViewModel(repository)

        viewModel.updateName("New Name")

        assertEquals("New Name", viewModel.uiState.value.name)
        assertFalse(viewModel.uiState.value.saveSuccess)
    }

    @Test
    fun `updateTagline updates tagline`() {
        val repository = mockk<SiteRepository> {
            every { getProfile() } returns flowOf(Resource.Loading)
        }
        val viewModel = createViewModel(repository)

        viewModel.updateTagline("New Tagline")

        assertEquals("New Tagline", viewModel.uiState.value.tagline)
    }

    @Test
    fun `updateBio updates bio`() {
        val repository = mockk<SiteRepository> {
            every { getProfile() } returns flowOf(Resource.Loading)
        }
        val viewModel = createViewModel(repository)

        viewModel.updateBio("New Bio")

        assertEquals("New Bio", viewModel.uiState.value.bio)
    }

    @Test
    fun `updateEmail updates email`() {
        val repository = mockk<SiteRepository> {
            every { getProfile() } returns flowOf(Resource.Loading)
        }
        val viewModel = createViewModel(repository)

        viewModel.updateEmail("new@example.com")

        assertEquals("new@example.com", viewModel.uiState.value.email)
    }

    @Test
    fun `save validates name is required`() = runTest {
        val repository = mockk<SiteRepository> {
            every { getProfile() } returns flowOf(Resource.Success(sampleProfile))
        }

        val viewModel = createViewModel(repository)
        viewModel.updateName("")
        viewModel.save()

        assertEquals("Name is required", viewModel.uiState.value.error)
    }

    @Test
    fun `save sends ProfileRequest with blank fields as null`() = runTest {
        val repository = mockk<SiteRepository> {
            every { getProfile() } returns flowOf(Resource.Success(sampleProfile))
            every { updateProfile(any()) } returns flowOf(Resource.Loading, Resource.Success(MessageResponse("Updated")))
        }

        val viewModel = createViewModel(repository)
        viewModel.updateTagline("")
        viewModel.updateBio("")
        viewModel.save()

        assertTrue(viewModel.uiState.value.saveSuccess)
        verify {
            repository.updateProfile(match { request ->
                request.name == "Artist" && request.tagline == null && request.bio == null
            })
        }
    }

    @Test
    fun `save success sets saveSuccess true`() = runTest {
        val repository = mockk<SiteRepository> {
            every { getProfile() } returns flowOf(Resource.Success(sampleProfile))
            every { updateProfile(any()) } returns flowOf(Resource.Loading, Resource.Success(MessageResponse("Updated")))
        }

        val viewModel = createViewModel(repository)
        viewModel.save()

        assertTrue(viewModel.uiState.value.saveSuccess)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `save error sets error message`() = runTest {
        val repository = mockk<SiteRepository> {
            every { getProfile() } returns flowOf(Resource.Success(sampleProfile))
            every { updateProfile(any()) } returns flowOf(Resource.Loading, Resource.Error("Failed"))
        }

        val viewModel = createViewModel(repository)
        viewModel.save()

        assertEquals("Failed", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `clearSaveSuccess clears saveSuccess flag`() = runTest {
        val repository = mockk<SiteRepository> {
            every { getProfile() } returns flowOf(Resource.Success(sampleProfile))
            every { updateProfile(any()) } returns flowOf(Resource.Success(MessageResponse("OK")))
        }

        val viewModel = createViewModel(repository)
        viewModel.save()
        viewModel.clearSaveSuccess()

        assertFalse(viewModel.uiState.value.saveSuccess)
    }
}
