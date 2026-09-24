package com.gorman.ourmemoryapp.ui.admin.veterans.models

sealed interface InfoBlock {
    data class Paragraph(val text: String) : InfoBlock
    data class Media(val url: String, val caption: String) : InfoBlock
}
