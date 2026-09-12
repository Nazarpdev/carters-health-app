package com.carters.health.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val CalmLightScheme = lightColorScheme(
    primary = HealthColors.Green,
    onPrimary = HealthColors.Card,
    primaryContainer = HealthColors.GreenSoft,
    onPrimaryContainer = HealthColors.GreenDeep,
    secondary = HealthColors.Terracotta,
    onSecondary = HealthColors.Card,
    secondaryContainer = HealthColors.TerracottaSoft,
    onSecondaryContainer = HealthColors.Ink,
    tertiary = HealthColors.Sage,
    onTertiary = HealthColors.Ink,
    background = HealthColors.Canvas,
    onBackground = HealthColors.Ink,
    surface = HealthColors.Canvas,
    onSurface = HealthColors.Ink,
    surfaceVariant = HealthColors.CardAlt,
    onSurfaceVariant = HealthColors.Muted,
    surfaceContainer = HealthColors.Card,
    surfaceContainerHigh = HealthColors.Card,
    surfaceContainerHighest = HealthColors.CardAlt,
    surfaceContainerLow = HealthColors.Card,
    surfaceContainerLowest = HealthColors.Card,
    outline = HealthColors.Hairline,
    outlineVariant = HealthColors.Hairline,
    error = HealthColors.Terracotta,
)

val HealthShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun HealthTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CalmLightScheme,
        typography = HealthTypography,
        shapes = HealthShapes,
        content = content,
    )
}
