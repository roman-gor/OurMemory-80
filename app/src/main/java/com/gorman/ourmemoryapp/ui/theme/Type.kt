package com.gorman.ourmemoryapp.ui.theme

import androidx.compose.material3.Typography
import com.gorman.ourmemoryapp.ui.fonts.mulishFont

private val Mulish = mulishFont()

val Typography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = Mulish),
        displayMedium = displayMedium.copy(fontFamily = Mulish),
        displaySmall = displaySmall.copy(fontFamily = Mulish),
        headlineLarge = headlineLarge.copy(fontFamily = Mulish),
        headlineMedium = headlineMedium.copy(fontFamily = Mulish),
        headlineSmall = headlineSmall.copy(fontFamily = Mulish),
        titleLarge = titleLarge.copy(fontFamily = Mulish),
        titleMedium = titleMedium.copy(fontFamily = Mulish),
        titleSmall = titleSmall.copy(fontFamily = Mulish),
        bodyLarge = bodyLarge.copy(fontFamily = Mulish),
        bodyMedium = bodyMedium.copy(fontFamily = Mulish),
        bodySmall = bodySmall.copy(fontFamily = Mulish),
        labelLarge = labelLarge.copy(fontFamily = Mulish),
        labelMedium = labelMedium.copy(fontFamily = Mulish),
        labelSmall = labelSmall.copy(fontFamily = Mulish)
    )
}
