package com.crimsonedge.studioadmin.data.repository

import com.crimsonedge.studioadmin.data.remote.api.NavigationApi
import com.crimsonedge.studioadmin.data.remote.dto.CreatedResponse
import com.crimsonedge.studioadmin.data.remote.dto.MessageResponse
import com.crimsonedge.studioadmin.data.remote.dto.NavigationItemDto
import com.crimsonedge.studioadmin.data.remote.dto.NavigationItemRequest
import com.crimsonedge.studioadmin.data.remote.dto.SocialLinkDto
import com.crimsonedge.studioadmin.data.remote.dto.SocialLinkRequest
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

class NavigationRepositoryImplTest {

    private lateinit var api: NavigationApi
    private lateinit var repository: NavigationRepositoryImpl

    @Before
    fun setup() {
        api = mockk()
        repository = NavigationRepositoryImpl(api)
    }

    // region getNavItems

    @Test
    fun `getNavItems success emits Loading then Success with mapped items`() = runTest {
        val dtos = listOf(
            NavigationItemDto(1, "Home", "/", "<svg/>", 1, true),
            NavigationItemDto(2, "About", "/about", null, 2, true)
        )
        coEvery { api.getNavigationItems() } returns dtos

        val results = repository.getNavItems().toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
        val items = (results[1] as Resource.Success).data
        assertEquals(2, items.size)
        assertEquals("Home", items[0].label)
        assertEquals("/", items[0].link)
    }

    @Test
    fun `getNavItems HTTP error emits Error`() = runTest {
        coEvery { api.getNavigationItems() } throws HttpException(
            Response.error<List<NavigationItemDto>>(500, "Error".toResponseBody())
        )

        val results = repository.getNavItems().toList()

        assertTrue(results[1] is Resource.Error)
        assertEquals(500, (results[1] as Resource.Error).code)
    }

    @Test
    fun `getNavItems network error emits Error`() = runTest {
        coEvery { api.getNavigationItems() } throws IOException("Timeout")

        val results = repository.getNavItems().toList()

        assertTrue(results[1] is Resource.Error)
        assertEquals("Timeout", (results[1] as Resource.Error).message)
    }

    // endregion

    // region createNavItem

    @Test
    fun `createNavItem success emits Loading then Success`() = runTest {
        val request = NavigationItemRequest("Contact", "/contact", null, 3, true)
        coEvery { api.createNavigationItem(request) } returns CreatedResponse(3)

        val results = repository.createNavItem(request).toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
        assertEquals(3, (results[1] as Resource.Success).data.id)
    }

    @Test
    fun `createNavItem HTTP error emits Error`() = runTest {
        val request = NavigationItemRequest("Contact", "/contact", null, 3, true)
        coEvery { api.createNavigationItem(request) } throws HttpException(
            Response.error<CreatedResponse>(400, "Bad".toResponseBody())
        )

        val results = repository.createNavItem(request).toList()

        assertTrue(results[1] is Resource.Error)
    }

    // endregion

    // region updateNavItem

    @Test
    fun `updateNavItem success emits Loading then Success`() = runTest {
        val request = NavigationItemRequest("Home Updated", "/", null, 1, true)
        coEvery { api.updateNavigationItem(1, request) } returns MessageResponse("Updated")

        val results = repository.updateNavItem(1, request).toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
    }

    @Test
    fun `updateNavItem network error emits Error`() = runTest {
        val request = NavigationItemRequest("Home Updated", "/", null, 1, true)
        coEvery { api.updateNavigationItem(1, request) } throws IOException("Fail")

        val results = repository.updateNavItem(1, request).toList()

        assertTrue(results[1] is Resource.Error)
    }

    // endregion

    // region deleteNavItem

    @Test
    fun `deleteNavItem success emits Loading then Success`() = runTest {
        coEvery { api.deleteNavigationItem(1) } returns MessageResponse("Deleted")

        val results = repository.deleteNavItem(1).toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
    }

    @Test
    fun `deleteNavItem HTTP error emits Error`() = runTest {
        coEvery { api.deleteNavigationItem(1) } throws HttpException(
            Response.error<MessageResponse>(404, "Not Found".toResponseBody())
        )

        val results = repository.deleteNavItem(1).toList()

        assertTrue(results[1] is Resource.Error)
    }

    // endregion

    // region getSocialLinks

    @Test
    fun `getSocialLinks success emits Loading then Success with mapped links`() = runTest {
        val dtos = listOf(
            SocialLinkDto(1, "Instagram", "https://ig.com", "<svg/>", 1, true),
            SocialLinkDto(2, "Twitter", "https://twitter.com", null, 2, true)
        )
        coEvery { api.getSocialLinks() } returns dtos

        val results = repository.getSocialLinks().toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
        val links = (results[1] as Resource.Success).data
        assertEquals(2, links.size)
        assertEquals("Instagram", links[0].platform)
        assertEquals("https://ig.com", links[0].url)
    }

    @Test
    fun `getSocialLinks HTTP error emits Error`() = runTest {
        coEvery { api.getSocialLinks() } throws HttpException(
            Response.error<List<SocialLinkDto>>(500, "Error".toResponseBody())
        )

        val results = repository.getSocialLinks().toList()

        assertTrue(results[1] is Resource.Error)
    }

    // endregion

    // region createSocialLink

    @Test
    fun `createSocialLink success emits Loading then Success`() = runTest {
        val request = SocialLinkRequest("GitHub", "https://github.com", null, 3, true)
        coEvery { api.createSocialLink(request) } returns CreatedResponse(3)

        val results = repository.createSocialLink(request).toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
        assertEquals(3, (results[1] as Resource.Success).data.id)
    }

    @Test
    fun `createSocialLink HTTP error emits Error`() = runTest {
        val request = SocialLinkRequest("GitHub", "https://github.com", null, 3, true)
        coEvery { api.createSocialLink(request) } throws HttpException(
            Response.error<CreatedResponse>(400, "Bad".toResponseBody())
        )

        val results = repository.createSocialLink(request).toList()

        assertTrue(results[1] is Resource.Error)
    }

    // endregion

    // region updateSocialLink

    @Test
    fun `updateSocialLink success emits Loading then Success`() = runTest {
        val request = SocialLinkRequest("Instagram", "https://ig.com/new", null, 1, true)
        coEvery { api.updateSocialLink(1, request) } returns MessageResponse("Updated")

        val results = repository.updateSocialLink(1, request).toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
    }

    @Test
    fun `updateSocialLink network error emits Error`() = runTest {
        val request = SocialLinkRequest("Instagram", "https://ig.com/new", null, 1, true)
        coEvery { api.updateSocialLink(1, request) } throws IOException("Fail")

        val results = repository.updateSocialLink(1, request).toList()

        assertTrue(results[1] is Resource.Error)
    }

    // endregion

    // region deleteSocialLink

    @Test
    fun `deleteSocialLink success emits Loading then Success`() = runTest {
        coEvery { api.deleteSocialLink(1) } returns MessageResponse("Deleted")

        val results = repository.deleteSocialLink(1).toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
    }

    @Test
    fun `deleteSocialLink HTTP error emits Error`() = runTest {
        coEvery { api.deleteSocialLink(1) } throws HttpException(
            Response.error<MessageResponse>(404, "Not Found".toResponseBody())
        )

        val results = repository.deleteSocialLink(1).toList()

        assertTrue(results[1] is Resource.Error)
    }

    // endregion
}
