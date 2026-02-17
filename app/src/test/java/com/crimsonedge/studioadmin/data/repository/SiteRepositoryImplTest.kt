package com.crimsonedge.studioadmin.data.repository

import com.crimsonedge.studioadmin.data.remote.api.SiteApi
import com.crimsonedge.studioadmin.data.remote.dto.HeroContentDto
import com.crimsonedge.studioadmin.data.remote.dto.HeroRequest
import com.crimsonedge.studioadmin.data.remote.dto.MessageResponse
import com.crimsonedge.studioadmin.data.remote.dto.ProfileDto
import com.crimsonedge.studioadmin.data.remote.dto.ProfileRequest
import com.crimsonedge.studioadmin.data.remote.dto.SectionDto
import com.crimsonedge.studioadmin.data.remote.dto.SectionRequest
import com.crimsonedge.studioadmin.data.remote.dto.SiteSettingDto
import com.crimsonedge.studioadmin.data.remote.dto.SiteSettingRequest
import com.crimsonedge.studioadmin.domain.util.Resource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class SiteRepositoryImplTest {

    private lateinit var api: SiteApi
    private lateinit var repository: SiteRepositoryImpl

    @Before
    fun setup() {
        api = mockk()
        repository = SiteRepositoryImpl(api)
    }

    // region getProfile

    @Test
    fun `getProfile success emits Loading then Success with mapped profile`() = runTest {
        val dto = ProfileDto(
            id = 1, name = "Artist", tagline = "Creative", bio = "Bio text",
            avatarImageId = 5, email = "a@b.com", instagramUrl = "ig",
            twitterUrl = "tw", artworksCount = "50", poemsCount = "20",
            yearsExperience = "10", updatedAt = "2024-01-01T00:00:00Z"
        )
        coEvery { api.getProfile() } returns dto

        val results = repository.getProfile().toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
        val profile = (results[1] as Resource.Success).data
        assertEquals("Artist", profile.name)
        assertEquals("Creative", profile.tagline)
        assertEquals(5, profile.avatarImageId)
    }

    @Test
    fun `getProfile HTTP error emits Error`() = runTest {
        coEvery { api.getProfile() } throws HttpException(
            Response.error<ProfileDto>(500, "Error".toResponseBody())
        )

        val results = repository.getProfile().toList()

        assertTrue(results[1] is Resource.Error)
        assertEquals(500, (results[1] as Resource.Error).code)
    }

    @Test
    fun `getProfile network error emits Error`() = runTest {
        coEvery { api.getProfile() } throws IOException("Network fail")

        val results = repository.getProfile().toList()

        assertTrue(results[1] is Resource.Error)
        assertEquals("Network fail", (results[1] as Resource.Error).message)
    }

    // endregion

    // region updateProfile

    @Test
    fun `updateProfile success emits Loading then Success`() = runTest {
        val request = ProfileRequest("Name", null, null, null, null, null, null, null, null, null)
        coEvery { api.updateProfile(request) } returns MessageResponse("Updated")

        val results = repository.updateProfile(request).toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
    }

    @Test
    fun `updateProfile HTTP error emits Error`() = runTest {
        val request = ProfileRequest("Name", null, null, null, null, null, null, null, null, null)
        coEvery { api.updateProfile(request) } throws HttpException(
            Response.error<MessageResponse>(400, "Bad".toResponseBody())
        )

        val results = repository.updateProfile(request).toList()

        assertTrue(results[1] is Resource.Error)
    }

    // endregion

    // region getHero

    @Test
    fun `getHero success emits Loading then Success with mapped hero`() = runTest {
        val dto = HeroContentDto(
            id = 1, quote = "A quote", quoteAttribution = "Author",
            headline = "Headline", subheading = "Sub", featuredImageId = 3,
            badgeText = "Badge", primaryCtaText = "CTA1", primaryCtaLink = "/link1",
            secondaryCtaText = "CTA2", secondaryCtaLink = "/link2",
            isActive = true, updatedAt = "2024-01-01T00:00:00Z"
        )
        coEvery { api.getHero() } returns dto

        val results = repository.getHero().toList()

        assertTrue(results[1] is Resource.Success)
        val hero = (results[1] as Resource.Success).data
        assertEquals("A quote", hero.quote)
        assertEquals(true, hero.isActive)
    }

    @Test
    fun `getHero HTTP error emits Error`() = runTest {
        coEvery { api.getHero() } throws HttpException(
            Response.error<HeroContentDto>(500, "Error".toResponseBody())
        )

        val results = repository.getHero().toList()

        assertTrue(results[1] is Resource.Error)
    }

    // endregion

    // region updateHero

    @Test
    fun `updateHero success emits Loading then Success`() = runTest {
        val request = HeroRequest(null, null, null, null, null, null, null, null, null, null, true)
        coEvery { api.updateHero(request) } returns MessageResponse("Updated")

        val results = repository.updateHero(request).toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
    }

    @Test
    fun `updateHero network error emits Error`() = runTest {
        val request = HeroRequest(null, null, null, null, null, null, null, null, null, null, true)
        coEvery { api.updateHero(request) } throws IOException("Fail")

        val results = repository.updateHero(request).toList()

        assertTrue(results[1] is Resource.Error)
    }

    // endregion

    // region getSettings

    @Test
    fun `getSettings success emits Loading then Success with mapped settings`() = runTest {
        val dtos = listOf(
            SiteSettingDto(1, "theme", "dark", "string", "2024-01-01T00:00:00Z"),
            SiteSettingDto(2, "language", "en", "string", "2024-01-01T00:00:00Z")
        )
        coEvery { api.getSettings() } returns dtos

        val results = repository.getSettings().toList()

        assertTrue(results[1] is Resource.Success)
        val settings = (results[1] as Resource.Success).data
        assertEquals(2, settings.size)
        assertEquals("theme", settings[0].settingKey)
        assertEquals("dark", settings[0].settingValue)
    }

    @Test
    fun `getSettings HTTP error emits Error`() = runTest {
        coEvery { api.getSettings() } throws HttpException(
            Response.error<List<SiteSettingDto>>(500, "Error".toResponseBody())
        )

        val results = repository.getSettings().toList()

        assertTrue(results[1] is Resource.Error)
    }

    // endregion

    // region updateSetting

    @Test
    fun `updateSetting success emits Loading then Success`() = runTest {
        val request = SiteSettingRequest("newValue")
        coEvery { api.updateSetting("theme", request) } returns MessageResponse("Updated")

        val results = repository.updateSetting("theme", request).toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
    }

    @Test
    fun `updateSetting HTTP error emits Error`() = runTest {
        val request = SiteSettingRequest("newValue")
        coEvery { api.updateSetting("theme", request) } throws HttpException(
            Response.error<MessageResponse>(400, "Bad".toResponseBody())
        )

        val results = repository.updateSetting("theme", request).toList()

        assertTrue(results[1] is Resource.Error)
    }

    // endregion

    // region getSections

    @Test
    fun `getSections success emits Loading then Success with mapped sections`() = runTest {
        val dtos = listOf(
            SectionDto(1, "hero", "Featured", "Hero Section", "Subtitle", 1, true, "2024-01-01T00:00:00Z")
        )
        coEvery { api.getSections() } returns dtos

        val results = repository.getSections().toList()

        assertTrue(results[1] is Resource.Success)
        val sections = (results[1] as Resource.Success).data
        assertEquals(1, sections.size)
        assertEquals("hero", sections[0].sectionKey)
        assertEquals("Featured", sections[0].tag)
    }

    @Test
    fun `getSections network error emits Error`() = runTest {
        coEvery { api.getSections() } throws IOException("Fail")

        val results = repository.getSections().toList()

        assertTrue(results[1] is Resource.Error)
    }

    // endregion

    // region updateSection

    @Test
    fun `updateSection success emits Loading then Success`() = runTest {
        val request = SectionRequest("tag", "title", "sub", 1, true)
        coEvery { api.updateSection(1, request) } returns MessageResponse("Updated")

        val results = repository.updateSection(1, request).toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
    }

    @Test
    fun `updateSection HTTP error emits Error`() = runTest {
        val request = SectionRequest("tag", "title", "sub", 1, true)
        coEvery { api.updateSection(1, request) } throws HttpException(
            Response.error<MessageResponse>(404, "Not Found".toResponseBody())
        )

        val results = repository.updateSection(1, request).toList()

        assertTrue(results[1] is Resource.Error)
    }

    // endregion
}
