package com.gorman.ourmemoryapp.data.feedback.repository

import com.gorman.ourmemoryapp.data.feedback.model.FeedbackDto
import com.gorman.ourmemoryapp.data.feedback.model.FeedbackStatus
import com.gorman.ourmemoryapp.domain.models.FeedbackType
import com.gorman.ourmemoryapp.testutil.FakeFeedbackRemoteDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FeedbackRepositoryImplTest {

    @Test
    fun newFeedbackGoesFirstAndNewestOnTop() = runTest {
        val repository = FeedbackRepositoryImpl(
            FakeFeedbackRemoteDataSource(
                listOf(
                    FeedbackDto(id = "old-done", status = FeedbackStatus.DONE, createdAt = 3),
                    FeedbackDto(id = "old-new", status = FeedbackStatus.NEW, createdAt = 1),
                    FeedbackDto(id = "fresh-new", status = FeedbackStatus.NEW, createdAt = 2)
                )
            )
        )

        assertEquals(listOf("fresh-new", "old-new", "old-done"), repository.observeFeedback().first().map { it.id })
    }

    @Test
    fun unknownTypeFallsBackToOther() = runTest {
        val repository = FeedbackRepositoryImpl(
            FakeFeedbackRemoteDataSource(
                listOf(
                    FeedbackDto(id = "a", type = FeedbackType.DATA_ERROR.name),
                    FeedbackDto(id = "b", type = "SPAM")
                )
            )
        )

        assertEquals(
            listOf(FeedbackType.DATA_ERROR, FeedbackType.OTHER),
            repository.observeFeedback().first().map { it.type }
        )
    }
}
