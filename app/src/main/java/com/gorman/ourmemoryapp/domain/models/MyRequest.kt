package com.gorman.ourmemoryapp.domain.models

data class MyRequest(
    val id: String,
    val kind: RequestKind,
    val veteranId: String,
    val text: String,
    val status: RequestStatus,
    val reply: String,
    val createdAt: Long,
    val reviewedAt: Long
)
