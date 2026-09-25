package com.gorman.ourmemoryapp.ui.admin.tours.models

import com.gorman.ourmemoryapp.domain.models.ContentLanguage
import com.gorman.ourmemoryapp.domain.models.TourStop
import com.gorman.ourmemoryapp.domain.models.TourStopTranslation
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentMap

data class TourStopForm(
    val burialId: String,
    val text: String = "",
    val audioUrl: String = "",
    val translations: ImmutableMap<ContentLanguage, TourStopTranslation> = persistentMapOf()
) {
    fun textIn(language: ContentLanguage?): TourStopTranslation =
        language?.let { translations[it] ?: TourStopTranslation() } ?: TourStopTranslation(text, audioUrl)

    fun withText(language: ContentLanguage?, transform: (TourStopTranslation) -> TourStopTranslation): TourStopForm {
        val updated = transform(textIn(language))
        val key = language ?: return copy(text = updated.text, audioUrl = updated.audioUrl)
        return copy(translations = (translations + (key to updated)).toPersistentMap())
    }
}

fun TourStop.toForm() = TourStopForm(
    burialId = burialId,
    text = text,
    audioUrl = audioUrl,
    translations = translations.toLanguageMap()
)

fun TourStopForm.toTourStop() = TourStop(
    burialId = burialId,
    text = text.trim(),
    audioUrl = audioUrl,
    translations = translations
        .mapValues { (_, stop) -> stop.copy(text = stop.text.trim()) }
        .filterValues { it.text.isNotEmpty() || it.audioUrl.isNotEmpty() }
        .entries
        .associate { (language, stop) -> language.key to stop }
)
