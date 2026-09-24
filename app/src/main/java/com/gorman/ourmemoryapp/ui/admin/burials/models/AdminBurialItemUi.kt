package com.gorman.ourmemoryapp.ui.admin.burials.models

import com.gorman.ourmemoryapp.domain.models.Burial
import com.gorman.ourmemoryapp.ui.common.models.BurialType
import com.gorman.ourmemoryapp.ui.common.models.BurialUi
import com.gorman.ourmemoryapp.ui.common.models.toExternalModel

data class AdminBurialItemUi(
    val burial: BurialUi,
    val type: BurialType,
    val veteranNames: String
)

fun Burial.toAdminItemUi(veteranNames: String) = AdminBurialItemUi(
    burial = toExternalModel(),
    type = BurialType.fromValue(type),
    veteranNames = veteranNames
)
