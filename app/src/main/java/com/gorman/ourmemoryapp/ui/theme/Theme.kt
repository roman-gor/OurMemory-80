package com.gorman.ourmemoryapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val MemoryColorScheme = lightColorScheme(
    primary = MemoryRed,
    onPrimary = MemoryPaper,
    primaryContainer = MemoryRedContainer,
    onPrimaryContainer = MemoryOnRedContainer,
    secondary = MemoryRedBright,
    onSecondary = MemoryPaper,
    background = MemoryPaper,
    onBackground = MemoryInk,
    surface = MemoryPaper,
    onSurface = MemoryInk,
    surfaceVariant = MemoryPaperHigh,
    onSurfaceVariant = MemoryInkMuted,
    surfaceContainerLowest = MemoryPaper,
    surfaceContainerLow = MemoryPaperLow,
    surfaceContainer = MemoryPaperMid,
    surfaceContainerHigh = MemoryPaperHigh,
    surfaceContainerHighest = MemoryPaperHighest,
    outline = MemoryOutline,
    outlineVariant = MemoryOutlineVariant
)

@Composable
fun OurMemoryAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MemoryColorScheme,
        typography = Typography,
        content = content
    )
}
