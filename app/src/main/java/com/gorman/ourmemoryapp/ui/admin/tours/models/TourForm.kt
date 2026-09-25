package com.gorman.ourmemoryapp.ui.admin.tours.models

import com.gorman.ourmemoryapp.domain.models.ContentLanguage
import com.gorman.ourmemoryapp.domain.models.Tour
import com.gorman.ourmemoryapp.domain.models.TourTranslation
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap

data class TourForm(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val stops: ImmutableList<TourStopForm> = persistentListOf(),
    val translations: ImmutableMap<ContentLanguage, TourTranslation> = persistentMapOf(),
    val language: ContentLanguage? = null
) {
    val isValid = title.isNotBlank() && stops.isNotEmpty()
    val text = language?.let { translations[it] ?: TourTranslation() } ?: TourTranslation(title, description)
}

fun TourForm.withText(transform: (TourTranslation) -> TourTranslation): TourForm {
    val updated = transform(text)
    val language = language ?: return copy(title = updated.title, description = updated.description)
    return copy(translations = (translations + (language to updated)).toPersistentMap())
}

fun Tour.toForm() = TourForm(
    id = id,
    title = title,
    description = description,
    stops = stops.map { it.toForm() }.toPersistentList(),
    translations = translations.toLanguageMap()
)

fun TourForm.toTour() = Tour(
    id = id,
    title = title.trim(),
    description = description.trim(),
    stops = stops.map { it.toTourStop() },
    translations = translations
        .mapValues { (_, text) -> TourTranslation(title = text.title.trim(), description = text.description.trim()) }
        .filterValues { it.title.isNotEmpty() || it.description.isNotEmpty() }
        .entries
        .associate { (language, text) -> language.key to text }
)

fun <T> Map<String, T>.toLanguageMap(): ImmutableMap<ContentLanguage, T> = entries
    .mapNotNull { (key, value) -> ContentLanguage.fromLanguage(key)?.let { it to value } }
    .toMap()
    .toPersistentMap()
