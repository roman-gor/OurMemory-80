package com.gorman.ourmemoryapp

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily

fun MulishFont(): FontFamily
{
    val mulish = FontFamily(
        Font(R.font.mulish_medium),
        Font(R.font.mulish_bold)
    )
    return mulish
}