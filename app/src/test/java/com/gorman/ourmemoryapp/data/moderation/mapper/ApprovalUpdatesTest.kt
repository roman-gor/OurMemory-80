package com.gorman.ourmemoryapp.data.moderation.mapper

import com.gorman.ourmemoryapp.domain.models.ModerationStatus
import com.gorman.ourmemoryapp.domain.models.Submission
import com.gorman.ourmemoryapp.domain.models.SubmissionApproval
import org.junit.Assert.assertEquals
import org.junit.Test

class ApprovalUpdatesTest {

    private fun approval(editedText: String, photos: List<String>) = SubmissionApproval(
        submission = Submission(
            id = "s1",
            veteranId = "10",
            text = "",
            contact = "",
            photoPaths = emptyList(),
            status = ModerationStatus.PENDING,
            createdAt = 0L
        ),
        editedText = editedText,
        approvedPhotoUrls = photos,
        photoCaption = "Из семейного архива"
    )

    @Test
    fun approvalAppendsTextAndSelectedPhotosAndMarksApproved() {
        val updates = approval(editedText = "  Письмо с фронта ", photos = listOf("https://a")).toUpdates(
            currentInfo = listOf("Абзац"),
            reviewer = "admin@memory.by",
            reviewedAt = REVIEWED_AT
        )

        assertEquals(
            listOf("Абзац", "Письмо с фронта", "https://a|Из семейного архива"),
            updates["Veterans/veteran10/veteransInfo"]
        )
        assertEquals("approved", updates["Submissions/s1/status"])
        assertEquals("admin@memory.by", updates["Submissions/s1/reviewedBy"])
        assertEquals(REVIEWED_AT, updates["Submissions/s1/reviewedAt"])
    }

    @Test
    fun blankTextIsSkipped() {
        val updates = approval(editedText = "   ", photos = listOf("https://a", "https://b")).toUpdates(
            currentInfo = emptyList(),
            reviewer = "",
            reviewedAt = REVIEWED_AT
        )

        assertEquals(
            listOf("https://a|Из семейного архива", "https://b|Из семейного архива"),
            updates["Veterans/veteran10/veteransInfo"]
        )
    }

    private companion object {
        const val REVIEWED_AT = 1_700_000_000_000L
    }
}
