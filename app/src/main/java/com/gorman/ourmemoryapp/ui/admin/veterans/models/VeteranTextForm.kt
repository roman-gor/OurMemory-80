package com.gorman.ourmemoryapp.ui.admin.veterans.models

import com.gorman.ourmemoryapp.domain.models.VeteranTranslation
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

data class VeteranTextForm(
    val name: String = "",
    val baseInfo: String = "",
    val allInfo: String = "",
    val blocks: ImmutableList<InfoBlock> = persistentListOf()
) {
    val isEmpty = name.isBlank() && baseInfo.isBlank() && allInfo.isBlank() && blocks.toVeteransInfo().isEmpty()
}

fun VeteranTranslation.toTextForm() = VeteranTextForm(
    name = name,
    baseInfo = baseInfo,
    allInfo = allInfo,
    blocks = veteransInfo.toInfoBlocks().toPersistentList()
)

fun VeteranTextForm.toTranslation() = VeteranTranslation(
    name = name.trim(),
    baseInfo = baseInfo.trim(),
    allInfo = allInfo.trim(),
    veteransInfo = blocks.toVeteransInfo()
)
