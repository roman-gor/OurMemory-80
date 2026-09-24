package com.gorman.ourmemoryapp.ui.feedback.models

import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.domain.models.FeedbackType

val FeedbackType.labelRes
    get() = when (this) {
        FeedbackType.DATA_ERROR -> R.string.data_error
        FeedbackType.SUGGESTION -> R.string.suggestion
        FeedbackType.OTHER -> R.string.other
    }
