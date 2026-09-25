package com.gorman.ourmemoryapp.ui.admin.moderation.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.gorman.ourmemoryapp.domain.models.ModerationDeniedException
import com.gorman.ourmemoryapp.domain.models.ModerationStatus
import com.gorman.ourmemoryapp.domain.models.Screen
import com.gorman.ourmemoryapp.domain.models.Submission
import com.gorman.ourmemoryapp.domain.models.Veteran
import com.gorman.ourmemoryapp.testutil.FakeModerationRepository
import com.gorman.ourmemoryapp.testutil.FakeVeteransRepository
import com.gorman.ourmemoryapp.testutil.MainDispatcherRule
import com.gorman.ourmemoryapp.ui.admin.moderation.models.ReviewFailure
import com.gorman.ourmemoryapp.ui.admin.moderation.models.SubmissionReviewUiIntent
import com.gorman.ourmemoryapp.ui.admin.moderation.models.SubmissionReviewUiState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SubmissionReviewViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val submission = Submission(
        id = SUBMISSION_ID,
        veteranId = "10",
        text = "Воспоминания внука",
        contact = "Пётр",
        photoPaths = listOf("0.jpg", "1.jpg"),
        status = ModerationStatus.PENDING,
        createdAt = 0L
    )

    private fun viewModel(repository: FakeModerationRepository) = SubmissionReviewViewModel(
        savedStateHandle = SavedStateHandle(mapOf(Screen.AdminSubmissionScreen.SUBMISSION_ID_ARG to SUBMISSION_ID)),
        moderationRepository = repository,
        veteransRepository = FakeVeteransRepository(listOf(Veteran(id = "10", name = "Окрестин Борис Семёнович")))
    )

    private suspend fun SubmissionReviewViewModel.awaitSuccess(
        predicate: (SubmissionReviewUiState.Success) -> Boolean = { true }
    ) = uiState.first { it is SubmissionReviewUiState.Success && predicate(it) } as SubmissionReviewUiState.Success

    @Test
    fun loadsSubmissionWithVeteranNameAndAllPhotosSelected() = runTest {
        val state = viewModel(FakeModerationRepository(listOf(submission))).awaitSuccess()

        assertEquals("Окрестин Борис Семёнович", state.veteranName)
        assertEquals("Воспоминания внука", state.text)
        assertTrue(state.photos.all { it.isSelected })
        assertTrue(state.isEditable)
    }

    @Test
    fun approvalSendsEditedTextAndOnlySelectedPhotos() = runTest {
        val repository = FakeModerationRepository(listOf(submission))
        val viewModel = viewModel(repository)
        viewModel.awaitSuccess()

        viewModel.onUiIntent(SubmissionReviewUiIntent.OnTextChange("Исправленный текст"))
        viewModel.onUiIntent(SubmissionReviewUiIntent.OnPhotoToggle("https://storage/0.jpg"))
        viewModel.onUiIntent(SubmissionReviewUiIntent.OnApproveClick("Из семейного архива"))

        assertTrue(viewModel.awaitSuccess { it.isFinished }.isFinished)
        val approval = repository.approvals.single()
        assertEquals("Исправленный текст", approval.editedText)
        assertEquals(listOf("https://storage/1.jpg"), approval.approvedPhotoUrls)
        assertEquals("Из семейного архива", approval.photoCaption)
    }

    @Test
    fun rejectionMarksSubmission() = runTest {
        val repository = FakeModerationRepository(listOf(submission))
        val viewModel = viewModel(repository)
        viewModel.awaitSuccess()

        viewModel.onUiIntent(SubmissionReviewUiIntent.OnRejectClick)

        viewModel.awaitSuccess { it.isFinished }
        assertEquals(listOf(SUBMISSION_ID), repository.rejections)
    }

    @Test
    fun failureKeepsScreenOpenWithError() = runTest {
        val viewModel = viewModel(FakeModerationRepository(listOf(submission), IllegalStateException("offline")))
        viewModel.awaitSuccess()

        viewModel.onUiIntent(SubmissionReviewUiIntent.OnApproveClick(""))

        val state = viewModel.awaitSuccess { it.failure != null }
        assertEquals(ReviewFailure.NETWORK, state.failure)
        assertFalse(state.isFinished)
        assertTrue(state.isEditable)
    }

    @Test
    fun deniedRejectionShowsPermissionError() = runTest {
        val viewModel = viewModel(FakeModerationRepository(listOf(submission), ModerationDeniedException()))
        viewModel.awaitSuccess()

        viewModel.onUiIntent(SubmissionReviewUiIntent.OnRejectClick)

        val state = viewModel.awaitSuccess { it.failure != null }
        assertEquals(ReviewFailure.NO_PERMISSION, state.failure)
        assertFalse(state.isFinished)
    }

    @Test
    fun reviewedSubmissionIsReadOnly() = runTest {
        val reviewed = submission.copy(status = ModerationStatus.APPROVED)

        assertFalse(viewModel(FakeModerationRepository(listOf(reviewed))).awaitSuccess().isEditable)
    }

    @Test
    fun missingSubmissionShowsError() = runTest {
        val viewModel = viewModel(FakeModerationRepository(emptyList()))

        assertEquals(
            SubmissionReviewUiState.Error,
            viewModel.uiState.first { it is SubmissionReviewUiState.Error }
        )
    }

    private companion object {
        const val SUBMISSION_ID = "s1"
    }
}
