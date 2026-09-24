package com.gorman.ourmemoryapp.ui.admin.veterans.models

import com.gorman.ourmemoryapp.domain.models.Veteran
import com.gorman.ourmemoryapp.ui.details.models.Reward
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toPersistentList

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
    val blocks: ImmutableList<InfoBlock> = persistentListOf()
) {
    val isNameValid = name.isNotBlank()
    val isBirthDateValid = birthDate.isBlankOrIsoDate()
    val isDeathDateValid = deathDate.isBlankOrIsoDate()
    val isValid = isNameValid && isBirthDateValid && isDeathDateValid
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
    blocks = veteransInfo.toInfoBlocks().toPersistentList()
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
    deathDate = deathDate
)
