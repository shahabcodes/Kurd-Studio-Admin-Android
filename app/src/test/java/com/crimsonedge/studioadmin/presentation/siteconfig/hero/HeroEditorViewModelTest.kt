package com.crimsonedge.studioadmin.presentation.siteconfig.hero

import com.crimsonedge.studioadmin.MainDispatcherRule
import com.crimsonedge.studioadmin.data.remote.dto.MessageResponse
import com.crimsonedge.studioadmin.domain.model.HeroContent
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
class HeroEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sampleHero = HeroContent(
        id = 1, quote = "A quote", quoteAttribution = "Author",
        headline = "Headline", subheading = "Subheading",
        featuredImageId = 3, badgeText = "Badge",
        primaryCtaText = "CTA1", primaryCtaLink = "/link1",
        secondaryCtaText = "CTA2", secondaryCtaLink = "/link2",
        isActive = true, updatedAt = "2024-01-01T00:00:00Z"
    )

    private fun createViewModel(
        repository: SiteRepository = mockk()
    ): HeroEditorViewModel {
        return HeroEditorViewModel(repository)
    }

    @Test
    fun `init loads hero and maps fields correctly`() = runTest {
        val repository = mockk<SiteRepository> {
            every { getHero() } returns flowOf(Resource.Success(sampleHero))
        }

        val viewModel = createViewModel(repository)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("A quote", state.quote)
        assertEquals("Author", state.quoteAttribution)
        assertEquals("Headline", state.headline)
        assertEquals("Subheading", state.subheading)
        assertEquals(3, state.featuredImageId)
        assertEquals("Badge", state.badgeText)
        assertEquals("CTA1", state.primaryCtaText)
        assertEquals("/link1", state.primaryCtaLink)
        assertEquals("CTA2", state.secondaryCtaText)
        assertEquals("/link2", state.secondaryCtaLink)
        assertTrue(state.isActive)
    }

    @Test
    fun `loadHero maps null fields to empty strings`() = runTest {
        val nullHero = HeroContent(
            id = 1, quote = null, quoteAttribution = null,
            headline = null, subheading = null, featuredImageId = null,
            badgeText = null, primaryCtaText = null, primaryCtaLink = null,
            secondaryCtaText = null, secondaryCtaLink = null,
            isActive = false, updatedAt = "2024-01-01T00:00:00Z"
        )
        val repository = mockk<SiteRepository> {
            every { getHero() } returns flowOf(Resource.Success(nullHero))
        }

        val viewModel = createViewModel(repository)

        assertEquals("", viewModel.uiState.value.quote)
        assertEquals("", viewModel.uiState.value.headline)
        assertNull(viewModel.uiState.value.featuredImageId)
        assertFalse(viewModel.uiState.value.isActive)
    }

    @Test
    fun `loadHero error sets error`() = runTest {
        val repository = mockk<SiteRepository> {
            every { getHero() } returns flowOf(Resource.Error("Server error"))
        }

        val viewModel = createViewModel(repository)

        assertEquals("Server error", viewModel.uiState.value.error)
    }

    @Test
    fun `updateQuote updates quote and clears saveSuccess`() {
        val repository = mockk<SiteRepository> {
            every { getHero() } returns flowOf(Resource.Loading)
        }
        val viewModel = createViewModel(repository)

        viewModel.updateQuote("New quote")

        assertEquals("New quote", viewModel.uiState.value.quote)
        assertFalse(viewModel.uiState.value.saveSuccess)
    }

    @Test
    fun `updateHeadline updates headline`() {
        val repository = mockk<SiteRepository> {
            every { getHero() } returns flowOf(Resource.Loading)
        }
        val viewModel = createViewModel(repository)

        viewModel.updateHeadline("New Headline")

        assertEquals("New Headline", viewModel.uiState.value.headline)
    }

    @Test
    fun `updateIsActive updates isActive`() {
        val repository = mockk<SiteRepository> {
            every { getHero() } returns flowOf(Resource.Loading)
        }
        val viewModel = createViewModel(repository)

        viewModel.updateIsActive(false)

        assertFalse(viewModel.uiState.value.isActive)
    }

    @Test
    fun `updateFeaturedImageId updates the id`() {
        val repository = mockk<SiteRepository> {
            every { getHero() } returns flowOf(Resource.Loading)
        }
        val viewModel = createViewModel(repository)

        viewModel.updateFeaturedImageId(42)

        assertEquals(42, viewModel.uiState.value.featuredImageId)
    }

    @Test
    fun `save sends HeroRequest with blank fields as null`() = runTest {
        val repository = mockk<SiteRepository> {
            every { getHero() } returns flowOf(Resource.Success(sampleHero))
            every { updateHero(any()) } returns flowOf(Resource.Loading, Resource.Success(MessageResponse("Updated")))
        }

        val viewModel = createViewModel(repository)
        viewModel.updateQuote("")
        viewModel.save()

        verify {
            repository.updateHero(match { request ->
                request.quote == null && request.headline == "Headline"
            })
        }
    }

    @Test
    fun `save success sets saveSuccess true`() = runTest {
        val repository = mockk<SiteRepository> {
            every { getHero() } returns flowOf(Resource.Success(sampleHero))
            every { updateHero(any()) } returns flowOf(Resource.Loading, Resource.Success(MessageResponse("Updated")))
        }

        val viewModel = createViewModel(repository)
        viewModel.save()

        assertTrue(viewModel.uiState.value.saveSuccess)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `save error sets error message`() = runTest {
        val repository = mockk<SiteRepository> {
            every { getHero() } returns flowOf(Resource.Success(sampleHero))
            every { updateHero(any()) } returns flowOf(Resource.Loading, Resource.Error("Failed"))
        }

        val viewModel = createViewModel(repository)
        viewModel.save()

        assertEquals("Failed", viewModel.uiState.value.error)
    }

    @Test
    fun `clearSaveSuccess clears flag`() = runTest {
        val repository = mockk<SiteRepository> {
            every { getHero() } returns flowOf(Resource.Success(sampleHero))
            every { updateHero(any()) } returns flowOf(Resource.Success(MessageResponse("OK")))
        }

        val viewModel = createViewModel(repository)
        viewModel.save()
        viewModel.clearSaveSuccess()

        assertFalse(viewModel.uiState.value.saveSuccess)
    }
}
