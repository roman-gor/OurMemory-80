package com.gorman.ourmemoryapp.ui.admin.veterans.models

private const val LINK_MARKER = "http"
private const val DESCRIPTION_SEPARATOR = "|"

fun List<String>.toInfoBlocks(): List<InfoBlock> = map { entry ->
    if (entry.contains(LINK_MARKER)) {
        InfoBlock.Media(
            url = entry.substringBefore(DESCRIPTION_SEPARATOR),
            caption = entry.substringAfter(DESCRIPTION_SEPARATOR, missingDelimiterValue = "")
        )
    } else {
        InfoBlock.Paragraph(entry)
    }
}

fun List<InfoBlock>.toVeteransInfo(): List<String> = mapNotNull { block ->
    when (block) {
        is InfoBlock.Paragraph -> block.text.takeIf { it.isNotBlank() }
        is InfoBlock.Media -> if (block.caption.isBlank()) {
            block.url
        } else {
            "${block.url}$DESCRIPTION_SEPARATOR${block.caption}"
        }
    }
}
