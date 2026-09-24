package com.gorman.ourmemoryapp.ui.tours.models

fun resumeStopIndex(stopCount: Int, visitedStops: Set<Int>): Int? =
    if (visitedStops.isEmpty()) null else (0 until stopCount).firstOrNull { it !in visitedStops }
