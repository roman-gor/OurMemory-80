package com.gorman.ourmemoryapp.ui.fonts

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.gorman.ourmemoryapp.R

fun inriaFont(): FontFamily
{
    val inria = FontFamily(
        Font(R.font.inriaserif_bolditalic)
    )
    return inria
}