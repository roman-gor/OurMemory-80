package com.gorman.ourmemoryapp.data.submissions.datasource.local

import kotlin.math.max
import kotlin.math.roundToInt

fun scaledSize(width: Int, height: Int, maxSide: Int): Pair<Int, Int> {
    val longestSide = max(width, height)
    if (longestSide <= maxSide) return width to height
    val scale = maxSide.toFloat() / longestSide
    return (width * scale).roundToInt().coerceAtLeast(1) to (height * scale).roundToInt().coerceAtLeast(1)
}
