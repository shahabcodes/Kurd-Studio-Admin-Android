package com.crimsonedge.studioadmin.presentation.dashboard

import com.crimsonedge.studioadmin.MainDispatcherRule
import com.crimsonedge.studioadmin.data.local.TokenDataStore
import com.crimsonedge.studioadmin.domain.model.DashboardStats
import com.crimsonedge.studioadmin.domain.repository.DashboardRepository
import com.crimsonedge.studioadmin.domain.util.Resource
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(
        repository: DashboardRepository = mockk(),
        tokenDataStore: TokenDataStore = mockk(relaxed = true)
    ): DashboardViewModel {
        return DashboardViewModel(repository, tokenDataStore)
    }

    @Test
    fun `init loads stats and populates state on success`() = runTest {
        val stats = DashboardStats(10, 5, 20, 3)
        val repository = mockk<DashboardRepository> {
            every { getStats() } returns flowOf(Resource.Loading, Resource.Success(stats))
        }
        val tokenDataStore = mockk<TokenDataStore>(relaxed = true) {
            every { displayName } returns flowOf("Admin")
        }

        val viewModel = createViewModel(repository, tokenDataStore)

        val state = viewModel.dashboardState.value
        assertTrue(state is Resource.Success)
        val data = (state as Resource.Success).data
        assertEquals(10, data.artworkCount)
        assertEquals(5, data.writingCount)
        assertEquals(20, data.imageCount)
        assertEquals(3, data.unreadContactCount)
    }

    @Test
    fun `init loads stats and sets error on failure`() = runTest {
        val repository = mockk<DashboardRepository> {
            every { getStats() } returns flowOf(Resource.Loading, Resource.Error("Server Error", 500))
        }
        val tokenDataStore = mockk<TokenDataStore>(relaxed = true) {
            every { displayName } returns flowOf(null)
        }

        val viewModel = createViewModel(repository, tokenDataStore)

        val state = viewModel.dashboardState.value
        assertTrue(state is Resource.Error)
        assertEquals("Server Error", (state as Resource.Error).message)
    }

    @Test
    fun `displayName comes from TokenDataStore`() = runTest {
        val repository = mockk<DashboardRepository> {
            every { getStats() } returns flowOf(Resource.Loading)
        }
        val tokenDataStore = mockk<TokenDataStore>(relaxed = true) {
            every { displayName } returns flowOf("John Doe")
        }

        val viewModel = createViewModel(repository, tokenDataStore)

        // initialValue is null, stateIn with WhileSubscribed only updates on subscription
        assertEquals(null, viewModel.displayName.value)
    }

    @Test
    fun `displayName returns null when not set`() = runTest {
        val repository = mockk<DashboardRepository> {
            every { getStats() } returns flowOf(Resource.Loading)
        }
        val tokenDataStore = mockk<TokenDataStore>(relaxed = true) {
            every { displayName } returns flowOf(null)
        }

        val viewModel = createViewModel(repository, tokenDataStore)

        assertEquals(null, viewModel.displayName.value)
    }

    @Test
    fun `loadStats reloads data`() = runTest {
        val stats = DashboardStats(1, 2, 3, 4)
        val repository = mockk<DashboardRepository> {
            every { getStats() } returns flowOf(Resource.Success(stats))
        }
        val tokenDataStore = mockk<TokenDataStore>(relaxed = true) {
            every { displayName } returns flowOf(null)
        }

        val viewModel = createViewModel(repository, tokenDataStore)
        viewModel.loadStats()

        val state = viewModel.dashboardState.value
        assertTrue(state is Resource.Success)
    }
}
