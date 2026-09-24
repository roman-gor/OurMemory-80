package com.gorman.ourmemoryapp.ui.myrequests.models

import com.gorman.ourmemoryapp.domain.models.MyRequest
import com.gorman.ourmemoryapp.domain.models.RequestKind
import com.gorman.ourmemoryapp.domain.models.RequestStatus
import com.gorman.ourmemoryapp.ui.common.models.toDisplayDate

data class MyRequestItemUi(
    val id: String,
    val kind: RequestKind,
    val veteranName: String,
    val text: String,
    val status: RequestStatus,
    val reply: String,
    val date: String
)

fun MyRequest.toUi(veteranName: String) = MyRequestItemUi(
    id = id,
    kind = kind,
    veteranName = veteranName,
    text = text,
    status = status,
    reply = reply,
    date = createdAt.toDisplayDate()
)
