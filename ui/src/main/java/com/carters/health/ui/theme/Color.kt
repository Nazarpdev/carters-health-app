package com.carters.health.ui.theme

import androidx.compose.ui.graphics.Color
import com.carters.health.data.model.SleepStage

/**
 * Calm, warm, light palette: off-white canvas, beige and off-grey surfaces, one sage green
 * accent, and a single warm terracotta reserved for heart-rate and effort.
 */
object HealthColors {
    // Surfaces
    val Canvas = Color(0xFFF4F1EA)      // warm off-white page background
    val Card = Color(0xFFFCFAF6)        // lighter off-white card
    val CardAlt = Color(0xFFECE9E2)     // beige / off-grey secondary card
    val Field = Color(0xFFEFECE5)       // input fields, tracks
    val Hairline = Color(0xFFE4DFD5)    // used very sparingly (dividers only)

    // Text
    val Ink = Color(0xFF2B2925)
    val InkSoft = Color(0xFF4F4B44)
    val Muted = Color(0xFF7D786F)
    val Faint = Color(0xFFAAA498)

    // Accent: sage / forest green
    val Green = Color(0xFF4F7C5C)
    val GreenDeep = Color(0xFF3A5E46)
    val GreenSoft = Color(0xFFDCE7D9)
    val Sage = Color(0xFF86A98F)

    // Warm accent, reserved for live heart rate and effort
    val Terracotta = Color(0xFFC4735A)
    val TerracottaSoft = Color(0xFFF2DED4)

    // Sleep stages
    val StageDeep = GreenDeep
    val StageRem = Sage
    val StageLight = Color(0xFFC9D8C5)
    val StageAwake = Color(0xFFDDA98F)

    /** Soft tonal tint of an accent for chips and badges. */
    fun tint(color: Color): Color = when (color) {
        Green, GreenDeep -> GreenSoft
        Terracotta -> TerracottaSoft
        Sage -> Color(0xFFE3ECE0)
        else -> CardAlt
    }
}

fun stageColor(stage: SleepStage): Color = when (stage) {
    SleepStage.DEEP -> HealthColors.StageDeep
    SleepStage.REM -> HealthColors.StageRem
    SleepStage.LIGHT -> HealthColors.StageLight
    SleepStage.AWAKE -> HealthColors.StageAwake
}
