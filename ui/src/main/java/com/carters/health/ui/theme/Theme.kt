package com.carters.health.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val WarmDarkScheme = darkColorScheme(
    primary = HealthColors.Amber,
    onPrimary = HealthColors.Obsidian,
    primaryContainer = HealthColors.SurfaceHigh,
    onPrimaryContainer = HealthColors.Gold,
    secondary = HealthColors.Emerald,
    onSecondary = HealthColors.Obsidian,
    secondaryContainer = HealthColors.SurfaceHigh,
    onSecondaryContainer = HealthColors.Mint,
    tertiary = HealthColors.Lavender,
    onTertiary = HealthColors.Obsidian,
    tertiaryContainer = HealthColors.SurfaceHigh,
    onTertiaryContainer = HealthColors.Lavender,
    error = HealthColors.Rose,
    onError = HealthColors.OnSurface,
    background = HealthColors.Obsidian,
    onBackground = HealthColors.OnSurface,
    surface = HealthColors.Obsidian,
    onSurface = HealthColors.OnSurface,
    surfaceVariant = HealthColors.Surface,
    onSurfaceVariant = HealthColors.Clay,
    surfaceContainer = HealthColors.Espresso,
    surfaceContainerHigh = HealthColors.Surface,
    surfaceContainerHighest = HealthColors.SurfaceHigh,
    surfaceContainerLow = HealthColors.Espresso,
    surfaceContainerLowest = HealthColors.TrueBlack,
    outline = HealthColors.Border,
    outlineVariant = HealthColors.Border,
)

val HealthShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun HealthTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WarmDarkScheme,
        typography = HealthTypography,
        shapes = HealthShapes,
        content = content,
    )
}
