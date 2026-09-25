package com.gorman.ourmemoryapp.ui.submission.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.gorman.ourmemoryapp.domain.models.Screen
import com.gorman.ourmemoryapp.domain.models.Veteran
import com.gorman.ourmemoryapp.testutil.FakeContentCheckRepository
import com.gorman.ourmemoryapp.testutil.FakeSubmissionsRepository
import com.gorman.ourmemoryapp.testutil.FakeVeteransRepository
import com.gorman.ourmemoryapp.testutil.MainDispatcherRule
import com.gorman.ourmemoryapp.ui.submission.models.SubmissionStatus
import com.gorman.ourmemoryapp.ui.submission.models.SubmissionUiIntent
import com.gorman.ourmemoryapp.ui.submission.models.SubmissionUiState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SubmissionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(repository: FakeSubmissionsRepository = FakeSubmissionsRepository()) = SubmissionViewModel(
        savedStateHandle = SavedStateHandle(mapOf(Screen.SubmissionScreen.VETERAN_ID_ARG to VETERAN_ID)),
        submissionsRepository = repository,
        veteransRepository = FakeVeteransRepository(listOf(Veteran(id = VETERAN_ID, name = "Иванов Иван"))),
        contentCheckRepository = FakeContentCheckRepository()
    )

    private suspend fun SubmissionViewModel.awaitState(predicate: (SubmissionUiState) -> Boolean) =
        uiState.first(predicate)

    private fun SubmissionViewModel.fillForm() {
        onUiIntent(SubmissionUiIntent.OnTextChange("Воспоминания внука"))
        onUiIntent(SubmissionUiIntent.OnContactChange("Пётр, +375 29 000-00-00"))
    }

    @Test
    fun sendingRequiresTextContactAndConsent() = runTest {
        val viewModel = viewModel()
        viewModel.fillForm()

        assertFalse(viewModel.awaitState { it.contact.isNotBlank() }.canSend)

        viewModel.onUiIntent(SubmissionUiIntent.OnConsentChange(true))

        assertTrue(viewModel.awaitState { it.hasConsent }.canSend)
    }

    @Test
    fun photosAreCappedAtMaximumAndDeduplicated() = runTest {
        val viewModel = viewModel()
        val uris = (1..7).map { "content://photo/$it" }
        viewModel.onUiIntent(SubmissionUiIntent.OnPhotosPicked(uris.take(2) + uris.take(2)))
        viewModel.onUiIntent(SubmissionUiIntent.OnPhotosPicked(uris))

        val state = viewModel.awaitState { it.photoUris.isNotEmpty() }

        assertEquals(uris.take(SubmissionUiState.MAX_PHOTOS), state.photoUris)
        assertFalse(state.canAddPhotos)
    }

    @Test
    fun successfulSendSubmitsTrimmedDraftAndShowsThanks() = runTest {
        val repository = FakeSubmissionsRepository()
        val viewModel = viewModel(repository)
        viewModel.fillForm()
        viewModel.onUiIntent(SubmissionUiIntent.OnContactChange("  Пётр  "))
        viewModel.onUiIntent(SubmissionUiIntent.OnPhotosPicked(listOf("content://photo/1")))
        viewModel.onUiIntent(SubmissionUiIntent.OnConsentChange(true))

        viewModel.onUiIntent(SubmissionUiIntent.OnSendClick)

        assertEquals(SubmissionStatus.SENT, viewModel.awaitState { it.status == SubmissionStatus.SENT }.status)
        val draft = repository.drafts.single()
        assertEquals(VETERAN_ID, draft.veteranId)
        assertEquals("Пётр", draft.contact)
        assertEquals(listOf("content://photo/1"), draft.photoUris)
    }

    @Test
    fun failedSendKeepsFormForRetry() = runTest {
        val viewModel = viewModel(FakeSubmissionsRepository(error = IllegalStateException("offline")))
        viewModel.fillForm()
        viewModel.onUiIntent(SubmissionUiIntent.OnConsentChange(true))

        viewModel.onUiIntent(SubmissionUiIntent.OnSendClick)

        val state = viewModel.awaitState { it.status == SubmissionStatus.FAILED }
        assertEquals("Воспоминания внука", state.text)
        assertTrue(state.canSend)
    }

    @Test
    fun veteranNameIsShownInHeader() = runTest {
        assertEquals("Иванов Иван", viewModel().awaitState { it.veteranName.isNotBlank() }.veteranName)
    }

    private companion object {
        const val VETERAN_ID = "10"
    }
}
