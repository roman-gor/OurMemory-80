package com.gorman.ourmemoryapp.data.moderation.repository

import com.gorman.ourmemoryapp.data.moderation.model.SubmissionDto
import com.gorman.ourmemoryapp.data.moderation.model.SubmissionStatusValues
import com.gorman.ourmemoryapp.domain.models.ModerationStatus
import com.gorman.ourmemoryapp.domain.models.Submission
import com.gorman.ourmemoryapp.domain.models.SubmissionApproval
import com.gorman.ourmemoryapp.testutil.FakeModerationRemoteDataSource
import com.gorman.ourmemoryapp.testutil.FakeVeteransRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ModerationRepositoryImplTest {

    @Test
    fun pendingGoFirstNewestOnTopAndBrokenRecordsAreDropped() = runTest {
        val repository = ModerationRepositoryImpl(
            remoteDataSource = FakeModerationRemoteDataSource(
                listOf(
                    SubmissionDto(id = "approved", status = SubmissionStatusValues.APPROVED, createdAt = 5),
                    SubmissionDto(id = "", status = SubmissionStatusValues.PENDING, createdAt = 9),
                    SubmissionDto(id = "old", status = SubmissionStatusValues.PENDING, createdAt = 1),
                    SubmissionDto(id = "new", status = SubmissionStatusValues.PENDING, createdAt = 3)
                )
            ),
            veteransRepository = FakeVeteransRepository()
        )

        val submissions = repository.observeSubmissions().first()

        assertEquals(listOf("new", "old", "approved"), submissions.map { it.id })
        assertEquals(ModerationStatus.APPROVED, submissions.last().status)
    }

    @Test
    fun approvalRefreshesVeteransCache() = runTest {
        val remote = FakeModerationRemoteDataSource()
        val veterans = FakeVeteransRepository()
        val repository = ModerationRepositoryImpl(remote, veterans)
        val approval = SubmissionApproval(
            submission = Submission("s1", "10", "", "", emptyList(), ModerationStatus.PENDING, 0L),
            editedText = "Текст",
            approvedPhotoUrls = emptyList(),
            photoCaption = ""
        )

        repository.approve(approval)

        assertEquals(listOf(approval), remote.approvals)
        assertEquals(1, veterans.invalidations)
    }

    @Test
    fun photoPathsResolveInOrder() = runTest {
        val repository = ModerationRepositoryImpl(FakeModerationRemoteDataSource(), FakeVeteransRepository())

        assertEquals(
            listOf("https://storage/a.jpg", "https://storage/b.jpg"),
            repository.resolvePhotoUrls(listOf("a.jpg", "b.jpg"))
        )
    }
}
