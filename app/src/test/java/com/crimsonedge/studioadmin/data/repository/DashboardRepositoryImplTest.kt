package com.crimsonedge.studioadmin.data.repository

import com.crimsonedge.studioadmin.data.remote.api.DashboardApi
import com.crimsonedge.studioadmin.data.remote.dto.DashboardStatsDto
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

class DashboardRepositoryImplTest {

    private lateinit var api: DashboardApi
    private lateinit var repository: DashboardRepositoryImpl

    @Before
    fun setup() {
        api = mockk()
        repository = DashboardRepositoryImpl(api)
    }

    @Test
    fun `getStats success emits Loading then Success with mapped stats`() = runTest {
        val dto = DashboardStatsDto(
            artworkCount = 10,
            writingCount = 5,
            imageCount = 20,
            unreadContactCount = 3
        )
        coEvery { api.getStats() } returns dto

        val results = repository.getStats().toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
        val stats = (results[1] as Resource.Success).data
        assertEquals(10, stats.artworkCount)
        assertEquals(5, stats.writingCount)
        assertEquals(20, stats.imageCount)
        assertEquals(3, stats.unreadContactCount)
    }

    @Test
    fun `getStats HTTP error emits Loading then Error with code`() = runTest {
        coEvery { api.getStats() } throws HttpException(
            Response.error<DashboardStatsDto>(500, "Server Error".toResponseBody())
        )

        val results = repository.getStats().toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Error)
        assertEquals(500, (results[1] as Resource.Error).code)
    }

    @Test
    fun `getStats network error emits Loading then Error`() = runTest {
        coEvery { api.getStats() } throws IOException("Connection refused")

        val results = repository.getStats().toList()

        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Error)
        assertEquals("Connection refused", (results[1] as Resource.Error).message)
    }
}
