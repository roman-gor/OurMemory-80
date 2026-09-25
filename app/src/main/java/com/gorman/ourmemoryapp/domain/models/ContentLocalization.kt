package com.gorman.ourmemoryapp.domain.models

fun Veteran.localized(language: ContentLanguage?): Veteran {
    val translation = language?.let { translations[it.key] } ?: return this
    return copy(
        name = translation.name.ifBlank { name },
        baseInfo = translation.baseInfo.ifBlank { baseInfo },
        allInfo = translation.allInfo.ifBlank { allInfo },
        veteransInfo = translation.veteransInfo.ifEmpty { veteransInfo }
    )
}

fun Burial.localized(language: ContentLanguage?): Burial {
    val translation = language?.let { translations[it.key] } ?: return this
    return copy(description = translation.description.ifBlank { description })
}

fun Tour.localized(language: ContentLanguage?): Tour {
    val translation = language?.let { translations[it.key] }
    return copy(
        title = translation?.title?.ifBlank { null } ?: title,
        description = translation?.description?.ifBlank { null } ?: description,
        stops = stops.map { it.localized(language) }
    )
}

fun TourStop.localized(language: ContentLanguage?): TourStop {
    val translation = language?.let { translations[it.key] } ?: return this
    return copy(
        text = translation.text.ifBlank { text },
        audioUrl = translation.audioUrl.ifBlank { audioUrl }
    )
}

fun Veteran.allNames(): List<String> = listOf(name) + translations.values.map { it.name }.filter { it.isNotBlank() }
