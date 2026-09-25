package com.gorman.ourmemoryapp.ui.admin.veterans.models

import com.gorman.ourmemoryapp.domain.models.ContentLanguage
import com.gorman.ourmemoryapp.domain.models.Veteran
import com.gorman.ourmemoryapp.ui.details.models.Reward
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap

data class VeteranForm(
    val id: String = "",
    val name: String = "",
    val years: String = "",
    val category: VeteranCategory = VeteranCategory.WAR,
    val baseInfo: String = "",
    val allInfo: String = "",
    val rewards: ImmutableMap<Reward, Int> = persistentMapOf(),
    val birthDate: String = "",
    val deathDate: String = "",
    val portrait: String = "",
    val audioUrl: String = "",
    val burialId: String = "",
    val blocks: ImmutableList<InfoBlock> = persistentListOf(),
    val translations: ImmutableMap<ContentLanguage, VeteranTextForm> = persistentMapOf(),
    val language: ContentLanguage? = null
) {
    val isNameValid = name.isNotBlank()
    val isBirthDateValid = birthDate.isBlankOrIsoDate()
    val isDeathDateValid = deathDate.isBlankOrIsoDate()
    val isValid = isNameValid && isBirthDateValid && isDeathDateValid
    val original = VeteranTextForm(name = name, baseInfo = baseInfo, allInfo = allInfo, blocks = blocks)
    val text = language?.let { translations[it] ?: VeteranTextForm() } ?: original
}

fun VeteranForm.withText(transform: (VeteranTextForm) -> VeteranTextForm): VeteranForm {
    val updated = transform(text)
    val language = language ?: return copy(
        name = updated.name,
        baseInfo = updated.baseInfo,
        allInfo = updated.allInfo,
        blocks = updated.blocks
    )
    return copy(translations = (translations + (language to updated)).toPersistentMap())
}

fun Veteran.toForm() = VeteranForm(
    id = id,
    name = name,
    years = years,
    category = VeteranCategory.fromValue(category),
    baseInfo = baseInfo,
    allInfo = allInfo,
    rewards = rewards.toRewardCounts().toImmutableMap(),
    birthDate = birthDate,
    deathDate = deathDate,
    portrait = portrait,
    audioUrl = audioUrl,
    burialId = burialId,
    blocks = veteransInfo.toInfoBlocks().toPersistentList(),
    translations = translations.entries
        .mapNotNull { (key, translation) -> ContentLanguage.fromLanguage(key)?.let { it to translation.toTextForm() } }
        .toMap()
        .toPersistentMap()
)

fun VeteranForm.toVeteran() = Veteran(
    id = id,
    name = name.trim(),
    portrait = portrait,
    baseInfo = baseInfo.trim(),
    allInfo = allInfo.trim(),
    years = years.trim(),
    category = category.value,
    rewards = rewards.toRewardsString(),
    veteransInfo = blocks.toVeteransInfo(),
    burialId = burialId,
    audioUrl = audioUrl,
    birthDate = birthDate,
    deathDate = deathDate,
    translations = translations
        .filterValues { !it.isEmpty }
        .entries
        .associate { (language, text) -> language.key to text.toTranslation() }
)
