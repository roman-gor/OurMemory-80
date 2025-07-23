package com.gorman.ourmemoryapp.ui.fonts

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.gorman.ourmemoryapp.R

fun mulishFont(): FontFamily
{
    val mulish = FontFamily(
        Font(R.font.mulish_medium),
        Font(R.font.mulish_bold)
    )
    return mulish
}