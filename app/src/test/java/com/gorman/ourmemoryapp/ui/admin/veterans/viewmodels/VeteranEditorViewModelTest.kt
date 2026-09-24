package com.gorman.ourmemoryapp.ui.admin.veterans.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.gorman.ourmemoryapp.domain.models.Screen
import com.gorman.ourmemoryapp.domain.models.Veteran
import com.gorman.ourmemoryapp.testutil.FakeBurialsRepository
import com.gorman.ourmemoryapp.testutil.FakeContentEditorRepository
import com.gorman.ourmemoryapp.testutil.FakeMediaRepository
import com.gorman.ourmemoryapp.testutil.FakeVeteransRepository
import com.gorman.ourmemoryapp.testutil.MainDispatcherRule
import com.gorman.ourmemoryapp.ui.admin.veterans.models.InfoBlock
import com.gorman.ourmemoryapp.ui.admin.veterans.models.VeteranEditorUiIntent
import com.gorman.ourmemoryapp.ui.admin.veterans.models.VeteranEditorUiState
import com.gorman.ourmemoryapp.ui.details.models.Reward
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class VeteranEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val veteran = Veteran(
        id = "10",
        name = "Окрестин Борис Семёнович",
        category = "War",
        rewards = "9,9",
        veteransInfo = listOf("Абзац", "https://a|Фото")
    )
    private val content = FakeContentEditorRepository()
    private val media = FakeMediaRepository()

    private fun viewModel(veteranId: String) = VeteranEditorViewModel(
        savedStateHandle = SavedStateHandle(mapOf(Screen.AdminVeteranEditorScreen.VETERAN_ID_ARG to veteranId)),
        veteransRepository = FakeVeteransRepository(listOf(veteran, Veteran(id = "12", name = "Другой"))),
        burialsRepository = FakeBurialsRepository(),
        contentEditorRepository = content,
        mediaRepository = media
    )

    private suspend fun VeteranEditorViewModel.awaitEditing(
        predicate: (VeteranEditorUiState.Editing) -> Boolean = { true }
    ) = uiState.first { it is VeteranEditorUiState.Editing && predicate(it) } as VeteranEditorUiState.Editing

    @Test
    fun newVeteranGetsNextIdAndCannotBeSavedWithoutName() = runTest {
        val viewModel = viewModel("")
        val state = viewModel.awaitEditing()

        assertTrue(state.isNew)
        assertEquals("13", state.form.id)
        assertFalse(state.canSave)

        viewModel.onUiIntent(VeteranEditorUiIntent.OnNameChange("Новиков Пётр"))
        assertTrue(viewModel.awaitEditing { it.form.name.isNotEmpty() }.canSave)
    }

    @Test
    fun invalidDateBlocksSaving() = runTest {
        val viewModel = viewModel("10")
        viewModel.awaitEditing()

        viewModel.onUiIntent(VeteranEditorUiIntent.OnBirthDateChange("03.08.1905"))

        assertFalse(viewModel.awaitEditing { it.form.birthDate.isNotEmpty() }.canSave)
        viewModel.onUiIntent(VeteranEditorUiIntent.OnSaveClick)
        assertTrue(content.savedVeterans.isEmpty())
    }

    @Test
    fun editsAccumulateAndSaveInDatabaseFormat() = runTest {
        val viewModel = viewModel("10")
        viewModel.awaitEditing()

        viewModel.onUiIntent(VeteranEditorUiIntent.OnYearsChange("1905–1966"))
        viewModel.onUiIntent(VeteranEditorUiIntent.OnRewardCountChange(Reward.RED_BANNER, 1))
        viewModel.onUiIntent(VeteranEditorUiIntent.OnRewardCountChange(Reward.RED_STAR, -1))
        viewModel.onUiIntent(VeteranEditorUiIntent.OnAddParagraph)
        viewModel.onUiIntent(VeteranEditorUiIntent.OnBlockChange(2, InfoBlock.Paragraph("Новый абзац")))
        viewModel.onUiIntent(VeteranEditorUiIntent.OnBlockMove(2, -2))
        viewModel.onUiIntent(VeteranEditorUiIntent.OnBirthDateChange("1905-08-03"))
        viewModel.onUiIntent(VeteranEditorUiIntent.OnSaveClick)

        assertTrue(viewModel.awaitEditing { it.isClosed }.isClosed)
        val saved = content.savedVeterans.single()
        assertEquals("10", saved.id)
        assertEquals("1905–1966", saved.years)
        assertEquals("1,9", saved.rewards)
        assertEquals("1905-08-03", saved.birthDate)
        assertEquals(listOf("Новый абзац", "Абзац", "https://a|Фото"), saved.veteransInfo)
    }

    @Test
    fun uploadedPortraitAndPhotoGoToVeteranFolder() = runTest {
        val viewModel = viewModel("10")
        viewModel.awaitEditing()

        viewModel.onUiIntent(VeteranEditorUiIntent.OnPortraitPicked("content://portrait"))
        viewModel.onUiIntent(VeteranEditorUiIntent.OnMediaPicked("content://photo"))

        val state = viewModel.awaitEditing { it.form.portrait.isNotEmpty() && it.form.blocks.size == 3 }
        assertEquals("https://media/veterans/10/photo", state.form.portrait)
        assertEquals(InfoBlock.Media("https://media/veterans/10/photo", ""), state.form.blocks.last())
        assertEquals(listOf("content://portrait", "content://photo"), media.uploads.map { it.first })
        assertFalse(state.isUploading)
    }

    @Test
    fun deleteRemovesExistingVeteran() = runTest {
        val viewModel = viewModel("10")
        viewModel.awaitEditing()

        viewModel.onUiIntent(VeteranEditorUiIntent.OnDeleteConfirm)

        viewModel.awaitEditing { it.isClosed }
        assertEquals(listOf("10"), content.deletedVeteranIds)
    }

    @Test
    fun unknownVeteranShowsError() = runTest {
        assertEquals(
            VeteranEditorUiState.Error,
            viewModel("404").uiState.first { it is VeteranEditorUiState.Error }
        )
    }
}
