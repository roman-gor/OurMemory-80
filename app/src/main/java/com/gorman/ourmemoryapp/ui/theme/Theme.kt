package com.gorman.ourmemoryapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

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

private val MemoryDarkColorScheme = darkColorScheme(
    primary = MemoryRedOnDark,
    onPrimary = MemoryNight,
    primaryContainer = MemoryRedContainerDark,
    onPrimaryContainer = MemoryOnRedContainerDark,
    secondary = MemoryRedOnDark,
    onSecondary = MemoryNight,
    background = MemoryNight,
    onBackground = MemoryNightInk,
    surface = MemoryNight,
    onSurface = MemoryNightInk,
    surfaceVariant = MemoryNightHigh,
    onSurfaceVariant = MemoryNightInkMuted,
    surfaceContainerLowest = MemoryNightLowest,
    surfaceContainerLow = MemoryNightLow,
    surfaceContainer = MemoryNightMid,
    surfaceContainerHigh = MemoryNightHigh,
    surfaceContainerHighest = MemoryNightHighest,
    outline = MemoryNightOutline,
    outlineVariant = MemoryNightOutlineVariant
)

@Composable
fun OurMemoryAppTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = if (darkTheme) MemoryDarkColorScheme else MemoryColorScheme,
            typography = Typography,
            content = content
        )
    }
}
