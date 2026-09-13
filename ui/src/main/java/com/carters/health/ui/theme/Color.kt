package com.carters.health.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.carters.health.data.model.SleepStage

/**
 * One calm palette in two moods. Light: off-white canvas with beige / off-grey cards.
 * Dark: warm charcoal canvas with slightly lifted cards. The accents keep their meaning in both
 * (green = recovery, terracotta = effort, sky = body, lavender = sleep, ochre = energy).
 */
@Immutable
data class HealthPalette(
    val canvas: Color, val card: Color, val cardAlt: Color, val field: Color, val hairline: Color,
    val ink: Color, val inkSoft: Color, val muted: Color, val faint: Color,
    val green: Color, val greenDeep: Color, val greenSoft: Color, val sage: Color,
    val terracotta: Color, val terracottaSoft: Color,
    val sky: Color, val skySoft: Color,
    val lavender: Color, val lavenderDeep: Color, val lavenderSoft: Color,
    val ochre: Color, val ochreSoft: Color,
    val stageDeep: Color, val stageRem: Color, val stageLight: Color, val stageAwake: Color,
    val isDark: Boolean,
)

val LightPalette = HealthPalette(
    canvas = Color(0xFFF4F1EA), card = Color(0xFFFCFAF6), cardAlt = Color(0xFFECE9E2), field = Color(0xFFEFECE5), hairline = Color(0xFFE4DFD5),
    ink = Color(0xFF2B2925), inkSoft = Color(0xFF4F4B44), muted = Color(0xFF7D786F), faint = Color(0xFFAAA498),
    green = Color(0xFF4F7C5C), greenDeep = Color(0xFF3A5E46), greenSoft = Color(0xFFDCE7D9), sage = Color(0xFF86A98F),
    terracotta = Color(0xFFC4735A), terracottaSoft = Color(0xFFF2DED4),
    sky = Color(0xFF6C8EA6), skySoft = Color(0xFFDCE6EC),
    lavender = Color(0xFF8B7CA6), lavenderDeep = Color(0xFF6A5C86), lavenderSoft = Color(0xFFE6E1EE),
    ochre = Color(0xFFC59B4E), ochreSoft = Color(0xFFF3E7CC),
    stageDeep = Color(0xFF6A5C86), stageRem = Color(0xFF8B7CA6), stageLight = Color(0xFFCFD9E3), stageAwake = Color(0xFFE2B48F),
    isDark = false,
)

val DarkPalette = HealthPalette(
    canvas = Color(0xFF1B1A18), card = Color(0xFF252422), cardAlt = Color(0xFF2E2D2A), field = Color(0xFF343330), hairline = Color(0xFF3D3B37),
    ink = Color(0xFFF1EDE6), inkSoft = Color(0xFFD8D3CA), muted = Color(0xFFA39D92), faint = Color(0xFF77726B),
    green = Color(0xFF86AF93), greenDeep = Color(0xFFA9CBB2), greenSoft = Color(0xFF2C3A31), sage = Color(0xFF9DBBA6),
    terracotta = Color(0xFFD99078), terracottaSoft = Color(0xFF45302A),
    sky = Color(0xFF8DACC3), skySoft = Color(0xFF2A353E),
    lavender = Color(0xFFAFA1C8), lavenderDeep = Color(0xFFC8BDDC), lavenderSoft = Color(0xFF342F40),
    ochre = Color(0xFFD5B06A), ochreSoft = Color(0xFF3F3623),
    stageDeep = Color(0xFF8574A8), stageRem = Color(0xFFAFA1C8), stageLight = Color(0xFF4B5966), stageAwake = Color(0xFFD9A67D),
    isDark = true,
)

/**
 * Static accessors used throughout the UI. [palette] is swapped by [HealthTheme]; it is snapshot
 * state so composables and draw lambdas that read it update when the theme changes.
 */
object HealthColors {
    var palette: HealthPalette by mutableStateOf(LightPalette)

    val Canvas: Color get() = palette.canvas
    val Card: Color get() = palette.card
    val CardAlt: Color get() = palette.cardAlt
    val Field: Color get() = palette.field
    val Hairline: Color get() = palette.hairline

    val Ink: Color get() = palette.ink
    val InkSoft: Color get() = palette.inkSoft
    val Muted: Color get() = palette.muted
    val Faint: Color get() = palette.faint

    val Green: Color get() = palette.green
    val GreenDeep: Color get() = palette.greenDeep
    val GreenSoft: Color get() = palette.greenSoft
    val Sage: Color get() = palette.sage

    val Terracotta: Color get() = palette.terracotta
    val TerracottaSoft: Color get() = palette.terracottaSoft

    val Sky: Color get() = palette.sky
    val SkySoft: Color get() = palette.skySoft
    val Lavender: Color get() = palette.lavender
    val LavenderDeep: Color get() = palette.lavenderDeep
    val LavenderSoft: Color get() = palette.lavenderSoft
    val Ochre: Color get() = palette.ochre
    val OchreSoft: Color get() = palette.ochreSoft

    val StageDeep: Color get() = palette.stageDeep
    val StageRem: Color get() = palette.stageRem
    val StageLight: Color get() = palette.stageLight
    val StageAwake: Color get() = palette.stageAwake

    /** Soft tonal tint of an accent for chips and badges. */
    fun tint(color: Color): Color = when (color) {
        Green, GreenDeep -> GreenSoft
        Terracotta -> TerracottaSoft
        Sage -> GreenSoft
        Sky -> SkySoft
        Lavender, LavenderDeep -> LavenderSoft
        Ochre -> OchreSoft
        else -> CardAlt
    }
}

fun stageColor(stage: SleepStage): Color = when (stage) {
    SleepStage.DEEP -> HealthColors.StageDeep
    SleepStage.REM -> HealthColors.StageRem
    SleepStage.LIGHT -> HealthColors.StageLight
    SleepStage.AWAKE -> HealthColors.StageAwake
}
