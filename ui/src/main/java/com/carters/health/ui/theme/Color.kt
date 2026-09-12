package com.carters.health.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Warm Tech palette: an espresso-tinted OLED canvas with warm bioluminescent accents.
 */
object HealthColors {
    // Canvas
    val Obsidian = Color(0xFF0A090C)
    val Espresso = Color(0xFF131216)
    val Surface = Color(0xFF1B1A20)
    val SurfaceHigh = Color(0xFF232129)
    val Border = Color(0xFF2C2C34)
    val TrueBlack = Color(0xFF000000)

    // Sunset Amber & Solar Gold — energy, calories, milestones
    val Amber = Color(0xFFFF9F1C)
    val Gold = Color(0xFFFFD166)

    // Cyber Mint & Living Emerald — recovery, readiness, completion
    val Emerald = Color(0xFF00E676)
    val Mint = Color(0xFF00F5D4)

    // Radiant Coral & Plasma Rose — live HR, max effort
    val Coral = Color(0xFFFF6B6B)
    val Rose = Color(0xFFFF3366)

    // Warm Lavender & Nocturnal Violet — sleep, wind-down
    val Lavender = Color(0xFFA370F7)
    val Violet = Color(0xFF7C4DFF)

    // Warm Sand & Soft Clay — secondary text
    val Sand = Color(0xFFE0D6C8)
    val Clay = Color(0xFFA59E92)
    val ClayDim = Color(0xFF6F6960)

    // Sleep stage colours
    val StageDeep = Color(0xFF3D5AFE)
    val StageRem = Color(0xFF7C4DFF)
    val StageLight = Color(0xFF80D8FF)
    val StageAwake = Color(0xFFFF8A80)

    val OnSurface = Color(0xFFF7F2EC)

    val amberGold = Brush.linearGradient(listOf(Amber, Gold))
    val emeraldMint = Brush.linearGradient(listOf(Emerald, Mint))
    val coralRose = Brush.linearGradient(listOf(Rose, Coral))
    val lavenderViolet = Brush.linearGradient(listOf(Violet, Lavender))
    val sunrise = Brush.linearGradient(listOf(Rose, Amber, Gold))

    fun rim(color: Color, strength: Float = 0.55f) = Brush.linearGradient(
        listOf(color.copy(alpha = strength), color.copy(alpha = 0.08f), Color.White.copy(alpha = 0.05f)),
    )
}

fun stageColor(stage: com.carters.health.data.model.SleepStage): Color = when (stage) {
    com.carters.health.data.model.SleepStage.DEEP -> HealthColors.StageDeep
    com.carters.health.data.model.SleepStage.REM -> HealthColors.StageRem
    com.carters.health.data.model.SleepStage.LIGHT -> HealthColors.StageLight
    com.carters.health.data.model.SleepStage.AWAKE -> HealthColors.StageAwake
}
