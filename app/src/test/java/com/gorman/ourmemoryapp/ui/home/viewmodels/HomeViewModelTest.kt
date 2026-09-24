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

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val veterans = listOf(
        Veteran(id = "1", name = "Иванов Иван", category = "War"),
        Veteran(id = "2", name = "Петров Пётр", category = "Art")
    )

    @Test
    fun loadingFailureShowsError() = runTest {
        val viewModel = HomeViewModel(FakeVeteransRepository(error = IllegalStateException("offline")))

        assertTrue(viewModel.uiState.first { it !is HomeUiState.Loading } is HomeUiState.Error)
    }

    @Test
    fun searchFiltersVeteransByName() = runTest {
        val viewModel = HomeViewModel(FakeVeteransRepository(veterans))
        viewModel.onUiIntent(HomeUiIntent.OnSearchChange("петр"))

        val state = viewModel.uiState.first { it is HomeUiState.Success && it.search == "петр" } as HomeUiState.Success

        assertEquals(listOf("2"), state.veterans.map { it.id })
    }
}
