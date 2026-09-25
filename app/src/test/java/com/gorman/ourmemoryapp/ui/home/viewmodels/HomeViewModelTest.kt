package com.gorman.ourmemoryapp.ui.home.viewmodels

import com.gorman.ourmemoryapp.domain.models.Veteran
import com.gorman.ourmemoryapp.testutil.FakeVeteransRepository
import com.gorman.ourmemoryapp.testutil.MainDispatcherRule
import com.gorman.ourmemoryapp.ui.home.models.HomeUiIntent
import com.gorman.ourmemoryapp.ui.home.models.HomeUiState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clock = Clock.fixed(Instant.parse("2026-05-09T08:00:00Z"), ZoneOffset.UTC)

    private val veterans = listOf(
        Veteran(id = "1", name = "Иванов Иван", category = "War", birthDate = "1921-05-09"),
        Veteran(id = "2", name = "Петров Пётр", category = "Art", deathDate = "1985-05-09", birthDate = "1910-01-01")
    )

    @Test
    fun loadingFailureShowsError() = runTest {
        val viewModel = HomeViewModel(FakeVeteransRepository(error = IllegalStateException("offline")), clock)

        assertTrue(viewModel.uiState.first { it !is HomeUiState.Loading } is HomeUiState.Error)
    }

    @Test
    fun searchFiltersVeteransByName() = runTest {
        val viewModel = HomeViewModel(FakeVeteransRepository(veterans), clock)
        viewModel.onUiIntent(HomeUiIntent.OnSearchChange("петр"))

        val state = viewModel.uiState.first { it is HomeUiState.Success && it.search == "петр" } as HomeUiState.Success

        assertEquals(listOf("2"), state.veterans.map { it.id })
    }

    @Test
    fun anniversariesMatchTodayBirthAndDeathDates() = runTest {
        val viewModel = HomeViewModel(FakeVeteransRepository(veterans), clock)

        val state = viewModel.uiState.first { it is HomeUiState.Success } as HomeUiState.Success

        assertEquals(
            listOf("1" to true, "2" to false),
            state.anniversaries.map { it.veteranId to it.isBirthday }
        )
        assertEquals(listOf(1921, 1985), state.anniversaries.map { it.year })
    }

    @Test
    fun anniversariesAreHiddenWhileSearching() = runTest {
        val viewModel = HomeViewModel(FakeVeteransRepository(veterans), clock)
        viewModel.onUiIntent(HomeUiIntent.OnSearchChange("иван"))

        val state = viewModel.uiState.first { it is HomeUiState.Success && it.search == "иван" } as HomeUiState.Success

        assertTrue(state.anniversaries.isEmpty())
    }

    @Test
    fun refreshInvalidatesCacheAndReloadsVeterans() = runTest {
        val repository = FakeVeteransRepository(veterans)
        val viewModel = HomeViewModel(repository, clock)
        viewModel.uiState.first { it is HomeUiState.Success }

        viewModel.onUiIntent(HomeUiIntent.OnRefresh)

        val state = viewModel.uiState.first {
            it is HomeUiState.Success && !it.isRefreshing && repository.invalidations == 1
        } as HomeUiState.Success
        assertEquals(listOf("1", "2"), state.veterans.map { it.id })
    }
}
