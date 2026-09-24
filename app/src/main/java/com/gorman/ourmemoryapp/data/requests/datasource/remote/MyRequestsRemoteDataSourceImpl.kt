package com.gorman.ourmemoryapp.data.requests.datasource.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.gorman.ourmemoryapp.data.auth.datasource.remote.observeCurrentUser
import com.gorman.ourmemoryapp.data.feedback.model.FeedbackDto
import com.gorman.ourmemoryapp.data.firebase.DatabaseNodes
import com.gorman.ourmemoryapp.data.firebase.childrenAs
import com.gorman.ourmemoryapp.data.firebase.observeValue
import com.gorman.ourmemoryapp.data.moderation.model.SubmissionDto
import com.gorman.ourmemoryapp.di.annotation.MemoryRoot
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MyRequestsRemoteDataSourceImpl @Inject constructor(
    private val auth: FirebaseAuth,
    @param:MemoryRoot private val root: DatabaseReference
) : MyRequestsRemoteDataSource {

    override fun observeCurrentUid() = auth.observeCurrentUser().map { it?.uid }

    override fun observeSubmissions(authorUid: String) = authoredBy(DatabaseNodes.SUBMISSIONS, authorUid)
        .map { it.childrenAs<SubmissionDto>() }

    override fun observeFeedback(authorUid: String) = authoredBy(DatabaseNodes.FEEDBACK, authorUid)
        .map { it.childrenAs<FeedbackDto>() }

    private fun authoredBy(node: String, authorUid: String) =
        root.child(node).orderByChild(AUTHOR_UID).equalTo(authorUid).observeValue()

    companion object {
        private const val AUTHOR_UID = "authorUid"
    }
}
