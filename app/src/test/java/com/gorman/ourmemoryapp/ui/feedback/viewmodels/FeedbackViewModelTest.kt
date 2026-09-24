package com.gorman.ourmemoryapp.ui.feedback.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.gorman.ourmemoryapp.domain.models.FeedbackType
import com.gorman.ourmemoryapp.domain.models.Screen
import com.gorman.ourmemoryapp.domain.models.Veteran
import com.gorman.ourmemoryapp.testutil.FakeFeedbackRepository
import com.gorman.ourmemoryapp.testutil.FakeVeteransRepository
import com.gorman.ourmemoryapp.testutil.MainDispatcherRule
import com.gorman.ourmemoryapp.ui.feedback.models.FeedbackFormStatus
import com.gorman.ourmemoryapp.ui.feedback.models.FeedbackUiIntent
import com.gorman.ourmemoryapp.ui.feedback.models.FeedbackUiState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FeedbackViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(
        veteranId: String? = null,
        repository: FakeFeedbackRepository = FakeFeedbackRepository()
    ) = FeedbackViewModel(
        savedStateHandle = SavedStateHandle(
            veteranId?.let { mapOf(Screen.FeedbackScreen.VETERAN_ID_ARG to it) }.orEmpty()
        ),
        feedbackRepository = repository,
        veteransRepository = FakeVeteransRepository(listOf(Veteran(id = VETERAN_ID, name = "Иванов Иван")))
    )

    private suspend fun FeedbackViewModel.awaitState(predicate: (FeedbackUiState) -> Boolean) =
        uiState.first(predicate)

    @Test
    fun sendingRequiresText() = runTest {
        val viewModel = viewModel()

        assertFalse(viewModel.uiState.value.canSend)
        viewModel.onUiIntent(FeedbackUiIntent.OnTextChange("   "))
        assertFalse(viewModel.awaitState { it.text.isNotEmpty() }.canSend)
        viewModel.onUiIntent(FeedbackUiIntent.OnTextChange("Спасибо за приложение"))
        assertTrue(viewModel.awaitState { it.text.isNotBlank() }.canSend)
    }

    @Test
    fun generalFeedbackDefaultsToOtherWithoutVeteran() = runTest {
        val repository = FakeFeedbackRepository()
        val viewModel = viewModel(repository = repository)
        viewModel.onUiIntent(FeedbackUiIntent.OnTextChange("Добавьте карту парковки"))

        viewModel.onUiIntent(FeedbackUiIntent.OnSendClick)

        viewModel.awaitState { it.status == FeedbackFormStatus.SENT }
        val draft = repository.drafts.single()
        assertEquals(FeedbackType.OTHER, draft.type)
        assertEquals("", draft.veteranId)
    }

    @Test
    fun errorReportCarriesVeteranTypeAndTrimmedFields() = runTest {
        val repository = FakeFeedbackRepository()
        val viewModel = viewModel(veteranId = VETERAN_ID, repository = repository)
        assertEquals("Иванов Иван", viewModel.awaitState { it.veteranName.isNotBlank() }.veteranName)
        viewModel.onUiIntent(FeedbackUiIntent.OnTypeChange(FeedbackType.SUGGESTION))
        viewModel.onUiIntent(FeedbackUiIntent.OnTextChange("  Неверный год рождения  "))
        viewModel.onUiIntent(FeedbackUiIntent.OnContactChange(" +375 29 000-00-00 "))

        viewModel.onUiIntent(FeedbackUiIntent.OnSendClick)

        viewModel.awaitState { it.status == FeedbackFormStatus.SENT }
        val draft = repository.drafts.single()
        assertEquals(FeedbackType.SUGGESTION, draft.type)
        assertEquals(VETERAN_ID, draft.veteranId)
        assertEquals("Неверный год рождения", draft.text)
        assertEquals("+375 29 000-00-00", draft.contact)
    }

    @Test
    fun errorReportDefaultsToDataError() = runTest {
        assertEquals(FeedbackType.DATA_ERROR, viewModel(veteranId = VETERAN_ID).uiState.value.type)
    }

    @Test
    fun failedSendKeepsTextForRetry() = runTest {
        val viewModel = viewModel(repository = FakeFeedbackRepository(error = IllegalStateException("offline")))
        viewModel.onUiIntent(FeedbackUiIntent.OnTextChange("Текст"))

        viewModel.onUiIntent(FeedbackUiIntent.OnSendClick)

        val state = viewModel.awaitState { it.status == FeedbackFormStatus.FAILED }
        assertEquals("Текст", state.text)
        assertTrue(state.canSend)
    }

    private companion object {
        const val VETERAN_ID = "10"
    }
}
