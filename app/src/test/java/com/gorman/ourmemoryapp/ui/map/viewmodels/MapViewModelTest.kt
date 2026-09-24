package com.gorman.ourmemoryapp.ui.map.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.gorman.ourmemoryapp.domain.models.Burial
import com.gorman.ourmemoryapp.domain.models.Screen
import com.gorman.ourmemoryapp.domain.models.Veteran
import com.gorman.ourmemoryapp.testutil.FakeBurialsRepository
import com.gorman.ourmemoryapp.testutil.FakeVeteransRepository
import com.gorman.ourmemoryapp.testutil.MainDispatcherRule
import com.gorman.ourmemoryapp.ui.map.models.BurialType
import com.gorman.ourmemoryapp.ui.map.models.MapUiIntent
import com.gorman.ourmemoryapp.ui.map.models.MapUiState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class MapViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val monument = Burial(id = "b_monument", latitude = 53.9, longitude = 27.5, type = "MONUMENT")
    private val warGrave = Burial(
        id = "b_war",
        latitude = 53.91,
        longitude = 27.51,
        section = "1",
        row = "2",
        place = "3"
    )
    private val artGrave = Burial(id = "b_art", latitude = 53.92, longitude = 27.52)
    private val noCoordinates = Burial(id = "b_zero")
    private val warVeteran = Veteran(id = "1", name = "Иванов", category = "War", burialId = warGrave.id)
    private val artVeteran = Veteran(id = "2", name = "Петров", category = "Art", burialId = artGrave.id)

    private fun viewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
        burialsError: Throwable? = null
    ) = MapViewModel(
        savedStateHandle = savedStateHandle,
        burialsRepository = FakeBurialsRepository(listOf(monument, warGrave, artGrave, noCoordinates), burialsError),
        veteransRepository = FakeVeteransRepository(listOf(warVeteran, artVeteran)),
        ioDispatcher = mainDispatcherRule.dispatcher
    )

    private suspend fun MapViewModel.awaitSuccess(predicate: (MapUiState.Success) -> Boolean = { true }) =
        uiState.first { it is MapUiState.Success && predicate(it) } as MapUiState.Success

    @Test
    fun allFiltersOnShowEveryBurialWithCoordinates() = runTest {
        val state = viewModel().awaitSuccess()

        assertEquals(listOf(monument.id, warGrave.id, artGrave.id), state.markers.map { it.id })
    }

    @Test
    fun warOnlyFilterHidesArtBurials() = runTest {
        val viewModel = viewModel()
        viewModel.onUiIntent(MapUiIntent.OnCheckedArtChange(false))

        val state = viewModel.awaitSuccess { !it.checkedArt }

        assertEquals(listOf(monument.id, warGrave.id), state.markers.map { it.id })
    }

    @Test
    fun monumentWithoutVeteransStaysVisibleWhenAllFiltersAreOff() = runTest {
        val viewModel = viewModel()
        viewModel.onUiIntent(MapUiIntent.OnCheckedWarChange(false))
        viewModel.onUiIntent(MapUiIntent.OnCheckedArtChange(false))

        val state = viewModel.awaitSuccess { !it.checkedWar && !it.checkedArt }

        assertEquals(listOf(monument.id), state.markers.map { it.id })
    }

    @Test
    fun burialFromRouteOpensItsDetailsWithVeterans() = runTest {
        val handle = SavedStateHandle(mapOf(Screen.BurialMapScreen.BURIAL_ID_ARG to warGrave.id))

        val state = viewModel(savedStateHandle = handle).awaitSuccess()

        assertEquals(warGrave.id, state.focusedBurialId)
        assertEquals(warGrave.id, state.selectedBurial?.burial?.id)
        assertEquals(BurialType.GRAVE, state.selectedBurial?.type)
        assertEquals(listOf(warVeteran.name), state.selectedBurial?.veterans?.map { it.name })
    }

    @Test
    fun dismissingSheetClearsSelectedBurial() = runTest {
        val viewModel = viewModel()
        viewModel.onUiIntent(MapUiIntent.OnMarkerClick(artGrave.id))
        viewModel.awaitSuccess { it.selectedBurial != null }

        viewModel.onUiIntent(MapUiIntent.OnSheetDismiss)

        assertNull(viewModel.awaitSuccess { it.selectedBurial == null }.selectedBurial)
    }

    @Test
    fun repositoryFailureShowsError() = runTest {
        val viewModel = viewModel(burialsError = IllegalStateException("offline"))

        assertEquals(MapUiState.Error, viewModel.uiState.first { it is MapUiState.Error })
    }
}
