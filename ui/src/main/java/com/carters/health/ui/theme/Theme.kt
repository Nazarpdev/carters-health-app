package com.carters.health.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp

private fun schemeFor(p: HealthPalette): ColorScheme {
    val base = if (p.isDark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = p.green,
        onPrimary = p.card,
        primaryContainer = p.greenSoft,
        onPrimaryContainer = p.greenDeep,
        secondary = p.terracotta,
        onSecondary = p.card,
        secondaryContainer = p.terracottaSoft,
        onSecondaryContainer = p.ink,
        tertiary = p.lavender,
        onTertiary = p.ink,
        tertiaryContainer = p.lavenderSoft,
        onTertiaryContainer = p.ink,
        background = p.canvas,
        onBackground = p.ink,
        surface = p.canvas,
        onSurface = p.ink,
        surfaceVariant = p.cardAlt,
        onSurfaceVariant = p.muted,
        surfaceContainer = p.card,
        surfaceContainerHigh = p.card,
        surfaceContainerHighest = p.cardAlt,
        surfaceContainerLow = p.card,
        surfaceContainerLowest = p.card,
        outline = p.hairline,
        outlineVariant = p.hairline,
        error = p.terracotta,
    )
}

val HealthShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun HealthTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val palette = if (darkTheme) DarkPalette else LightPalette
    // Swap synchronously so everything composed below reads the right mood on the first frame.
    if (HealthColors.palette !== palette) HealthColors.palette = palette
    val scheme = remember(palette) { schemeFor(palette) }
    MaterialTheme(
        colorScheme = scheme,
        typography = HealthTypography,
        shapes = HealthShapes,
        content = content,
    )
}
