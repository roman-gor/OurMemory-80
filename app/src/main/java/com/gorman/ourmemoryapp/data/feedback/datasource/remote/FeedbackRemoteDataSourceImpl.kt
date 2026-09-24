package com.gorman.ourmemoryapp.data.feedback.datasource.remote

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ServerValue
import com.gorman.ourmemoryapp.data.auth.datasource.remote.AnonymousSession
import com.gorman.ourmemoryapp.data.feedback.model.FeedbackDto
import com.gorman.ourmemoryapp.data.feedback.model.FeedbackStatus
import com.gorman.ourmemoryapp.data.firebase.DatabaseNodes
import com.gorman.ourmemoryapp.data.firebase.childrenAs
import com.gorman.ourmemoryapp.data.firebase.observeValue
import com.gorman.ourmemoryapp.di.annotation.MemoryRoot
import com.gorman.ourmemoryapp.domain.models.FeedbackDraft
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FeedbackRemoteDataSourceImpl @Inject constructor(
    private val anonymousSession: AnonymousSession,
    @param:MemoryRoot private val root: DatabaseReference
) : FeedbackRemoteDataSource {

    private val feedbackReference = root.child(DatabaseNodes.FEEDBACK)

    override suspend fun send(draft: FeedbackDraft) {
        val authorUid = anonymousSession.ensureSignedIn()
        val reference = feedbackReference.push()
        reference.setValue(
            mapOf(
                "id" to reference.key,
                "authorUid" to authorUid,
                "type" to draft.type.name,
                "text" to draft.text,
                "contact" to draft.contact,
                "veteranId" to draft.veteranId,
                "status" to FeedbackStatus.NEW,
                "createdAt" to ServerValue.TIMESTAMP
            )
        ).await()
    }

    override fun observeFeedback() = feedbackReference.observeValue().map { it.childrenAs<FeedbackDto>() }

    override suspend fun markReviewed(feedbackId: String) {
        feedbackReference.child(feedbackId).updateChildren(
            mapOf("status" to FeedbackStatus.DONE, "reviewedAt" to ServerValue.TIMESTAMP)
        ).await()
    }

    override suspend fun reply(feedbackId: String, text: String) {
        feedbackReference.child(feedbackId).updateChildren(
            mapOf(
                "status" to FeedbackStatus.DONE,
                "reply" to text,
                "reviewedAt" to ServerValue.TIMESTAMP
            )
        ).await()
    }
}
