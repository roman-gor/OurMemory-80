package com.gorman.ourmemoryapp.ui.admin.burials.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.gorman.ourmemoryapp.domain.models.Burial
import com.gorman.ourmemoryapp.domain.models.Screen
import com.gorman.ourmemoryapp.testutil.FakeBurialsRepository
import com.gorman.ourmemoryapp.testutil.FakeContentEditorRepository
import com.gorman.ourmemoryapp.testutil.FakeMediaRepository
import com.gorman.ourmemoryapp.testutil.MainDispatcherRule
import com.gorman.ourmemoryapp.ui.admin.burials.models.BurialEditorUiIntent
import com.gorman.ourmemoryapp.ui.admin.burials.models.BurialEditorUiState
import com.gorman.ourmemoryapp.ui.common.models.BurialType
import com.gorman.ourmemoryapp.ui.common.models.CemeteryLocation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class BurialEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val content = FakeContentEditorRepository()
    private val media = FakeMediaRepository()

    private fun viewModel(burialId: String) = BurialEditorViewModel(
        savedStateHandle = SavedStateHandle(mapOf(Screen.AdminBurialEditorScreen.BURIAL_ID_ARG to burialId)),
        burialsRepository = FakeBurialsRepository(listOf(Burial(id = "b_001", latitude = 53.9, longitude = 27.5))),
        contentEditorRepository = content,
        mediaRepository = media
    )

    private suspend fun BurialEditorViewModel.awaitEditing(
        predicate: (BurialEditorUiState.Editing) -> Boolean = { true }
    ) = uiState.first { it is BurialEditorUiState.Editing && predicate(it) } as BurialEditorUiState.Editing

    @Test
    fun newBurialGetsGeneratedIdAndStartsAtCemetery() = runTest {
        val state = viewModel("").awaitEditing()

        assertTrue(state.isNew)
        assertEquals(FakeContentEditorRepository.NEW_BURIAL_ID, state.form.id)
        assertEquals(CemeteryLocation.LATITUDE, state.form.latitudeValue)
        assertTrue(state.canSave)
    }

    @Test
    fun pickedPointAndFieldsAreSaved() = runTest {
        val viewModel = viewModel("b_001")
        viewModel.awaitEditing()

        viewModel.onUiIntent(BurialEditorUiIntent.OnTypeChange(BurialType.MONUMENT))
        viewModel.onUiIntent(BurialEditorUiIntent.OnSectionChange("3"))
        viewModel.onUiIntent(BurialEditorUiIntent.OnPointPicked(53.1234567, 27.7654321))
        viewModel.onUiIntent(BurialEditorUiIntent.OnSaveClick)

        viewModel.awaitEditing { it.isClosed }
        val saved = content.savedBurials.single()
        assertEquals("b_001", saved.id)
        assertEquals("MONUMENT", saved.type)
        assertEquals("3", saved.section)
        assertEquals(53.123457, saved.latitude, DELTA)
        assertEquals(27.765432, saved.longitude, DELTA)
    }

    @Test
    fun invalidLatitudeBlocksSaving() = runTest {
        val viewModel = viewModel("b_001")
        viewModel.awaitEditing()

        viewModel.onUiIntent(BurialEditorUiIntent.OnLatitudeChange("north"))
        assertFalse(viewModel.awaitEditing { it.form.latitude == "north" }.canSave)

        viewModel.onUiIntent(BurialEditorUiIntent.OnSaveClick)
        assertTrue(content.savedBurials.isEmpty())
    }

    @Test
    fun photoUploadsToBurialFolder() = runTest {
        val viewModel = viewModel("b_001")
        viewModel.awaitEditing()

        viewModel.onUiIntent(BurialEditorUiIntent.OnPhotoPicked("content://grave"))

        val state = viewModel.awaitEditing { it.form.photo.isNotEmpty() }
        assertEquals("https://media/burials/b_001/photo", state.form.photo)
    }

    private companion object {
        const val DELTA = 1e-9
    }
}
