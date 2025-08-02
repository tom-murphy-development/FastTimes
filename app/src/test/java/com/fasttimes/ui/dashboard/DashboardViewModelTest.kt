package com.fasttimes.ui.dashboard

import app.cash.turbine.test
import com.fasttimes.data.fast.Fast
import com.fasttimes.data.fast.FastRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {
    private lateinit var repository: FastRepository
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setup() {
        repository = mock()
        whenever(repository.getAllFasts()).thenReturn(flowOf(emptyList()))
        viewModel = DashboardViewModel(repository)
    }

    @Test
    fun `startFast inserts new fast`() = runTest {
        viewModel.startFast()
        verify(repository).insertFast(org.mockito.kotlin.any())
    }

    @Test
    fun `endCurrentFast calls repository`() = runTest {
        val fast = Fast(1, 1000, null, 1000, null)
        whenever(repository.getAllFasts()).thenReturn(flowOf(listOf(fast)))
        viewModel = DashboardViewModel(repository)
        viewModel.endCurrentFast()
        verify(repository).endFast(fast.id, org.mockito.kotlin.any())
    }

    @Test
    fun `stats are calculated correctly`() = runTest {
        val fasts = listOf(
            Fast(1, 1000, 2000, 1000, null),
            Fast(2, 2000, 4000, 2000, null)
        )
        whenever(repository.getAllFasts()).thenReturn(flowOf(fasts))
        viewModel = DashboardViewModel(repository)
        viewModel.stats.test {
            val stats = awaitItem()
            assertEquals(2, stats.totalFasts)
            assertEquals(0, stats.longestFast) // duration in hours (mocked data)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

