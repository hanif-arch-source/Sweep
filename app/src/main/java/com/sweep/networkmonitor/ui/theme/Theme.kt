package com.sweep.networkmonitor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val SweepColorScheme = lightColorScheme(
    primary = SweepRed,
    onPrimary = SweepWhite,
    primaryContainer = SweepRedLight,
    onPrimaryContainer = SweepRedDark,
    secondary = SweepOnlineGreen,
    background = SweepWhite,
    onBackground = SweepCharcoal,
    surface = SweepWhite,
    onSurface = SweepCharcoal,
    surfaceVariant = SweepSurfaceVariant,
    onSurfaceVariant = SweepCharcoalMuted,
    outline = SweepBorder,
    error = SweepRedDark
)

/**
 * Sweep's app-wide theme: white background, red accent, dark charcoal text —
 * clean and spacious rather than a technical terminal look.
 */
@Composable
fun SweepTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SweepColorScheme,
        typography = SweepTypography,
        content = content
    )
}
