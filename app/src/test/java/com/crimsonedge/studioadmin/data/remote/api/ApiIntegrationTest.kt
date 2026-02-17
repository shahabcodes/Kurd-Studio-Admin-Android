package com.crimsonedge.studioadmin.data.remote.api

import com.crimsonedge.studioadmin.data.remote.dto.ArtworkRequest
import com.crimsonedge.studioadmin.data.remote.dto.HeroRequest
import com.crimsonedge.studioadmin.data.remote.dto.LoginRequest
import com.crimsonedge.studioadmin.data.remote.dto.NavigationItemRequest
import com.crimsonedge.studioadmin.data.remote.dto.ProfileRequest
import com.crimsonedge.studioadmin.data.remote.dto.RefreshRequest
import com.crimsonedge.studioadmin.data.remote.dto.SectionRequest
import com.crimsonedge.studioadmin.data.remote.dto.SiteSettingRequest
import com.crimsonedge.studioadmin.data.remote.dto.SocialLinkRequest
import com.crimsonedge.studioadmin.data.remote.dto.WritingRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class ApiIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var retrofit: Retrofit
    private lateinit var moshi: Moshi

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/api/"))
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    // region Auth API

    @Test
    fun `AuthApi login parses response correctly`() = runTest {
        val json = """
            {
                "accessToken": "access123",
                "refreshToken": "refresh123",
                "username": "admin",
                "displayName": "Admin User",
                "expiresAt": "2025-12-31T23:59:59Z"
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(json).setResponseCode(200))

        val api = retrofit.create(AuthApi::class.java)
        val result = api.login(LoginRequest("admin", "pass"))

        assertEquals("access123", result.accessToken)
        assertEquals("refresh123", result.refreshToken)
        assertEquals("admin", result.username)
        assertEquals("Admin User", result.displayName)
    }

    @Test
    fun `AuthApi login with null displayName`() = runTest {
        val json = """
            {
                "accessToken": "access123",
                "refreshToken": "refresh123",
                "username": "admin",
                "displayName": null,
                "expiresAt": "2025-12-31T23:59:59Z"
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(json).setResponseCode(200))

        val api = retrofit.create(AuthApi::class.java)
        val result = api.login(LoginRequest("admin", "pass"))

        assertNull(result.displayName)
    }

    @Test
    fun `AuthApi refresh parses response correctly`() = runTest {
        val json = """
            {
                "accessToken": "new_access",
                "refreshToken": "new_refresh",
                "username": "admin",
                "displayName": null,
                "expiresAt": "2025-12-31T23:59:59Z"
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(json).setResponseCode(200))

        val api = retrofit.create(AuthApi::class.java)
        val result = api.refresh(RefreshRequest("old_refresh"))

        assertEquals("new_access", result.accessToken)
    }

    @Test
    fun `AuthApi logout parses message response`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody("""{"message":"Logged out"}""").setResponseCode(200))

        val api = retrofit.create(AuthApi::class.java)
        val result = api.logout(RefreshRequest("token"))

        assertEquals("Logged out", result.message)
    }

    // endregion

    // region Dashboard API

    @Test
    fun `DashboardApi getStats parses response correctly`() = runTest {
        val json = """
            {
                "artworkCount": 10,
                "writingCount": 5,
                "imageCount": 20,
                "unreadContactCount": 3
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(json).setResponseCode(200))

        val api = retrofit.create(DashboardApi::class.java)
        val result = api.getStats()

        assertEquals(10, result.artworkCount)
        assertEquals(5, result.writingCount)
        assertEquals(20, result.imageCount)
        assertEquals(3, result.unreadContactCount)
    }

    // endregion

    // region Artwork API

    @Test
    fun `ArtworkApi getAll parses list correctly`() = runTest {
        val json = """
            [{
                "id": 1, "title": "Art 1", "slug": "art-1",
                "artworkTypeId": 1, "typeName": "painting", "typeDisplayName": "Painting",
                "imageId": 10, "description": "Desc", "isFeatured": true,
                "displayOrder": 1, "createdAt": "2024-01-01T00:00:00Z",
                "updatedAt": "2024-01-02T00:00:00Z"
            }]
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(json).setResponseCode(200))

        val api = retrofit.create(ArtworkApi::class.java)
        val result = api.getAll()

        assertEquals(1, result.size)
        assertEquals("Art 1", result[0].title)
        assertTrue(result[0].isFeatured)
    }

    @Test
    fun `ArtworkApi getById parses single artwork`() = runTest {
        val json = """
            {
                "id": 1, "title": "Art 1", "slug": "art-1",
                "artworkTypeId": 1, "typeName": "painting", "typeDisplayName": "Painting",
                "imageId": 10, "description": null, "isFeatured": false,
                "displayOrder": 0, "createdAt": "2024-01-01T00:00:00Z",
                "updatedAt": "2024-01-02T00:00:00Z"
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(json).setResponseCode(200))

        val api = retrofit.create(ArtworkApi::class.java)
        val result = api.getById(1)

        assertNull(result.description)
        assertFalse(result.isFeatured)
    }

    @Test
    fun `ArtworkApi create sends correct request and parses response`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody("""{"id": 5}""").setResponseCode(201))

        val api = retrofit.create(ArtworkApi::class.java)
        val request = ArtworkRequest("New", "new", 1, 10, null, false, 0)
        val result = api.create(request)

        assertEquals(5, result.id)
        val recorded = mockWebServer.takeRequest()
        assertEquals("POST", recorded.method)
        assertTrue(recorded.path!!.contains("artworks"))
    }

    @Test
    fun `ArtworkApi getTypes parses types list`() = runTest {
        val json = """
            [
                {"id": 1, "typeName": "painting", "displayName": "Painting", "displayOrder": 1},
                {"id": 2, "typeName": "digital", "displayName": "Digital", "displayOrder": 2}
            ]
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(json).setResponseCode(200))

        val api = retrofit.create(ArtworkApi::class.java)
        val result = api.getTypes()

        assertEquals(2, result.size)
        assertEquals("painting", result[0].typeName)
    }

    @Test
    fun `ArtworkApi delete sends DELETE request`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody("""{"message":"Deleted"}""").setResponseCode(200))

        val api = retrofit.create(ArtworkApi::class.java)
        val result = api.delete(1)

        assertEquals("Deleted", result.message)
        assertEquals("DELETE", mockWebServer.takeRequest().method)
    }

    // endregion

    // region Writing API

    @Test
    fun `WritingApi getAll parses list with nullable fields`() = runTest {
        val json = """
            [{
                "id": 1, "title": "Poem", "slug": "poem",
                "writingTypeId": 1, "typeName": "poem", "typeDisplayName": "Poem",
                "subtitle": null, "excerpt": null, "fullContent": "Content",
                "datePublished": null, "novelName": null, "chapterNumber": null,
                "displayOrder": 1, "createdAt": "2024-01-01T00:00:00Z",
                "updatedAt": "2024-01-02T00:00:00Z"
            }]
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(json).setResponseCode(200))

        val api = retrofit.create(WritingApi::class.java)
        val result = api.getAll()

        assertEquals(1, result.size)
        assertNull(result[0].subtitle)
        assertNull(result[0].chapterNumber)
        assertEquals("Content", result[0].fullContent)
    }

    @Test
    fun `WritingApi create sends correct JSON`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody("""{"id": 3}""").setResponseCode(201))

        val api = retrofit.create(WritingApi::class.java)
        val request = WritingRequest("Title", "slug", 1, "sub", null, "content", null, null, null, 0)
        val result = api.create(request)

        assertEquals(3, result.id)
    }

    // endregion

    // region Image API

    @Test
    fun `ImageApi getAll parses image list`() = runTest {
        val json = """
            [{
                "id": 1, "fileName": "photo.jpg", "contentType": "image/jpeg",
                "altText": "A photo", "fileSize": 1024, "width": 800, "height": 600,
                "imageUrl": "http://example.com/photo.jpg",
                "thumbnailUrl": "http://example.com/thumb.jpg",
                "createdAt": "2024-01-01T00:00:00Z"
            }]
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(json).setResponseCode(200))

        val api = retrofit.create(ImageApi::class.java)
        val result = api.getAll()

        assertEquals(1, result.size)
        assertEquals("photo.jpg", result[0].fileName)
        assertEquals(1024L, result[0].fileSize)
    }

    @Test
    fun `ImageApi delete sends correct request`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody("""{"message":"Deleted"}""").setResponseCode(200))

        val api = retrofit.create(ImageApi::class.java)
        val result = api.delete(1)

        assertEquals("Deleted", result.message)
        val recorded = mockWebServer.takeRequest()
        assertTrue(recorded.path!!.contains("images/1"))
    }

    // endregion

    // region Site API

    @Test
    fun `SiteApi getProfile parses profile with nullable fields`() = runTest {
        val json = """
            {
                "id": 1, "name": "Artist", "tagline": "Creative",
                "bio": null, "avatarImageId": null, "email": "a@b.com",
                "instagramUrl": null, "twitterUrl": null,
                "artworksCount": "50", "poemsCount": null, "yearsExperience": "10",
                "updatedAt": "2024-01-01T00:00:00Z"
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(json).setResponseCode(200))

        val api = retrofit.create(SiteApi::class.java)
        val result = api.getProfile()

        assertEquals("Artist", result.name)
        assertNull(result.bio)
        assertNull(result.avatarImageId)
        assertEquals("50", result.artworksCount)
    }

    @Test
    fun `SiteApi getHero parses hero content`() = runTest {
        val json = """
            {
                "id": 1, "quote": "A quote", "quoteAttribution": "Author",
                "headline": "Head", "subheading": "Sub",
                "featuredImageId": 3, "badgeText": "Badge",
                "primaryCtaText": "CTA1", "primaryCtaLink": "/link1",
                "secondaryCtaText": null, "secondaryCtaLink": null,
                "isActive": true, "updatedAt": "2024-01-01T00:00:00Z"
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(json).setResponseCode(200))

        val api = retrofit.create(SiteApi::class.java)
        val result = api.getHero()

        assertEquals("A quote", result.quote)
        assertTrue(result.isActive)
        assertNull(result.secondaryCtaText)
    }

    @Test
    fun `SiteApi getSettings parses settings list`() = runTest {
        val json = """
            [
                {"id": 1, "settingKey": "theme", "settingValue": "dark", "settingType": "string", "updatedAt": "2024-01-01T00:00:00Z"},
                {"id": 2, "settingKey": "lang", "settingValue": null, "settingType": "string", "updatedAt": "2024-01-01T00:00:00Z"}
            ]
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(json).setResponseCode(200))

        val api = retrofit.create(SiteApi::class.java)
        val result = api.getSettings()

        assertEquals(2, result.size)
        assertEquals("dark", result[0].settingValue)
        assertNull(result[1].settingValue)
    }

    @Test
    fun `SiteApi getSections parses sections list`() = runTest {
        val json = """
            [{
                "id": 1, "sectionKey": "hero", "tag": "Featured",
                "title": "Hero", "subtitle": null, "displayOrder": 1,
                "isActive": true, "updatedAt": "2024-01-01T00:00:00Z"
            }]
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(json).setResponseCode(200))

        val api = retrofit.create(SiteApi::class.java)
        val result = api.getSections()

        assertEquals(1, result.size)
        assertEquals("hero", result[0].sectionKey)
        assertTrue(result[0].isActive)
    }

    @Test
    fun `SiteApi updateProfile sends correct request`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody("""{"message":"Updated"}""").setResponseCode(200))

        val api = retrofit.create(SiteApi::class.java)
        val request = ProfileRequest("Name", null, null, null, null, null, null, null, null, null)
        val result = api.updateProfile(request)

        assertEquals("Updated", result.message)
        assertEquals("PUT", mockWebServer.takeRequest().method)
    }

    @Test
    fun `SiteApi updateHero sends correct request`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody("""{"message":"Updated"}""").setResponseCode(200))

        val api = retrofit.create(SiteApi::class.java)
        val request = HeroRequest(null, null, null, null, null, null, null, null, null, null, true)
        api.updateHero(request)

        val recorded = mockWebServer.takeRequest()
        assertEquals("PUT", recorded.method)
        assertTrue(recorded.path!!.contains("site/hero"))
    }

    @Test
    fun `SiteApi updateSetting sends correct path`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody("""{"message":"Updated"}""").setResponseCode(200))

        val api = retrofit.create(SiteApi::class.java)
        api.updateSetting("theme", SiteSettingRequest("light"))

        val recorded = mockWebServer.takeRequest()
        assertTrue(recorded.path!!.contains("site/settings/theme"))
    }

    @Test
    fun `SiteApi updateSection sends correct path`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody("""{"message":"Updated"}""").setResponseCode(200))

        val api = retrofit.create(SiteApi::class.java)
        api.updateSection(1, SectionRequest("tag", "title", null, 1, true))

        val recorded = mockWebServer.takeRequest()
        assertTrue(recorded.path!!.contains("site/sections/1"))
    }

    // endregion

    // region Navigation API

    @Test
    fun `NavigationApi getNavigationItems parses list`() = runTest {
        val json = """
            [
                {"id": 1, "label": "Home", "link": "/", "iconSvg": null, "displayOrder": 1, "isActive": true}
            ]
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(json).setResponseCode(200))

        val api = retrofit.create(NavigationApi::class.java)
        val result = api.getNavigationItems()

        assertEquals(1, result.size)
        assertEquals("Home", result[0].label)
        assertNull(result[0].iconSvg)
    }

    @Test
    fun `NavigationApi createNavigationItem parses created response`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody("""{"id": 3}""").setResponseCode(201))

        val api = retrofit.create(NavigationApi::class.java)
        val request = NavigationItemRequest("About", "/about", null, 2, true)
        val result = api.createNavigationItem(request)

        assertEquals(3, result.id)
    }

    @Test
    fun `NavigationApi getSocialLinks parses list`() = runTest {
        val json = """
            [
                {"id": 1, "platform": "Instagram", "url": "https://ig.com", "iconSvg": "<svg/>", "displayOrder": 1, "isActive": true}
            ]
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(json).setResponseCode(200))

        val api = retrofit.create(NavigationApi::class.java)
        val result = api.getSocialLinks()

        assertEquals(1, result.size)
        assertEquals("Instagram", result[0].platform)
        assertEquals("<svg/>", result[0].iconSvg)
    }

    @Test
    fun `NavigationApi createSocialLink parses response`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody("""{"id": 2}""").setResponseCode(201))

        val api = retrofit.create(NavigationApi::class.java)
        val request = SocialLinkRequest("Twitter", "https://twitter.com", null, 2, true)
        val result = api.createSocialLink(request)

        assertEquals(2, result.id)
    }

    // endregion

    // region Contact API

    @Test
    fun `ContactApi getAll parses contacts list`() = runTest {
        val json = """
            [{
                "id": 1, "name": "John", "email": "john@example.com",
                "subject": "Hello", "budget": null, "message": "Hi there",
                "submittedAt": "2024-01-01T00:00:00Z", "isRead": false, "isResponded": false
            }]
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(json).setResponseCode(200))

        val api = retrofit.create(ContactApi::class.java)
        val result = api.getAll()

        assertEquals(1, result.size)
        assertEquals("John", result[0].name)
        assertNull(result[0].budget)
        assertFalse(result[0].isRead)
    }

    @Test
    fun `ContactApi markAsRead sends PUT to correct path`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody("""{"message":"Marked"}""").setResponseCode(200))

        val api = retrofit.create(ContactApi::class.java)
        val result = api.markAsRead(1)

        assertEquals("Marked", result.message)
        val recorded = mockWebServer.takeRequest()
        assertEquals("PUT", recorded.method)
        assertTrue(recorded.path!!.contains("contacts/1/read"))
    }

    @Test
    fun `ContactApi delete sends DELETE to correct path`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody("""{"message":"Deleted"}""").setResponseCode(200))

        val api = retrofit.create(ContactApi::class.java)
        api.delete(1)

        val recorded = mockWebServer.takeRequest()
        assertEquals("DELETE", recorded.method)
        assertTrue(recorded.path!!.contains("contacts/1"))
    }

    // endregion
}
