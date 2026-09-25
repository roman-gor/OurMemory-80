package com.gorman.ourmemoryapp.ui.admin.burials.models

import com.gorman.ourmemoryapp.domain.models.Burial
import com.gorman.ourmemoryapp.domain.models.BurialTranslation
import com.gorman.ourmemoryapp.domain.models.ContentLanguage
import com.gorman.ourmemoryapp.ui.common.models.BurialType
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentMap

data class BurialForm(
    val id: String = "",
    val type: BurialType = BurialType.GRAVE,
    val section: String = "",
    val row: String = "",
    val place: String = "",
    val description: String = "",
    val photo: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val translatedDescriptions: ImmutableMap<ContentLanguage, String> = persistentMapOf(),
    val language: ContentLanguage? = null
) {
    val latitudeValue = latitude.toLatitudeOrNull()
    val longitudeValue = longitude.toLongitudeOrNull()
    val isValid = latitudeValue != null && longitudeValue != null
    val descriptionText = language?.let { translatedDescriptions[it].orEmpty() } ?: description
}

fun BurialForm.withDescription(text: String): BurialForm {
    val language = language ?: return copy(description = text)
    return copy(translatedDescriptions = (translatedDescriptions + (language to text)).toPersistentMap())
}

fun Burial.toForm() = BurialForm(
    id = id,
    type = BurialType.fromValue(type),
    section = section,
    row = row,
    place = place,
    description = description,
    photo = photo,
    latitude = latitude.toCoordinateText(),
    longitude = longitude.toCoordinateText(),
    translatedDescriptions = translations.entries
        .mapNotNull { (key, translation) -> ContentLanguage.fromLanguage(key)?.let { it to translation.description } }
        .toMap()
        .toPersistentMap()
)

fun BurialForm.toBurial() = Burial(
    id = id,
    latitude = latitudeValue ?: 0.0,
    longitude = longitudeValue ?: 0.0,
    section = section.trim(),
    row = row.trim(),
    place = place.trim(),
    type = type.value,
    photo = photo,
    description = description.trim(),
    translations = translatedDescriptions
        .filterValues { it.isNotBlank() }
        .entries
        .associate { (language, text) -> language.key to BurialTranslation(description = text.trim()) }
)
