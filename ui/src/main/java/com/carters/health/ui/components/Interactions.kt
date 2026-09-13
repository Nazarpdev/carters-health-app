package com.carters.health.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.carters.health.ui.theme.HealthColors
import com.carters.health.ui.theme.LocalHealthHaptics

/** A clickable that squeezes while pressed. */
fun Modifier.pressableClick(enabled: Boolean = true, pressed: Float = 0.97f, onClick: () -> Unit): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed && enabled) pressed else 1f, Motion.snappy(), label = "press")
    this
        .scale(scale)
        .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick)
}

/** Flat toggle: track tints, thumb slides with a spring. */
@Composable
fun SoftSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier, accent: Color = HealthColors.Green) {
    val haptics = LocalHealthHaptics.current
    val track by animateColorAsState(if (checked) accent else HealthColors.Field, Motion.quick(), label = "track")
    val thumbOffset by animateFloatAsState(if (checked) 1f else 0f, Motion.snappy(), label = "thumb")
    val width = 46.dp
    val height = 26.dp
    val thumb = 20.dp
    Box(
        modifier
            .width(width)
            .height(height)
            .clip(CircleShape)
            .background(track)
            .pressableClick(pressed = 0.94f) { haptics.tick(); onCheckedChange(!checked) }
            .padding(3.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .offset(x = (width - 6.dp - thumb) * thumbOffset)
                .size(thumb)
                .clip(CircleShape)
                .background(HealthColors.Card),
        )
    }
}

/** A number that counts up to its value when it first appears, then eases to any new value. */
@Composable
fun AnimatedNumber(value: Int, style: TextStyle, color: Color = HealthColors.Ink, modifier: Modifier = Modifier, suffix: String = "") {
    var started by remember { mutableStateOf(!Motion.enabled) }
    LaunchedEffect(Unit) { started = true }
    val shown by animateIntAsState(if (started) value else 0, if (started) Motion.reveal() else Motion.quick(), label = "count")
    Text("$shown$suffix", style = style, color = color, modifier = modifier)
}

/**
 * Staggered entrance for list content: each item fades and drifts up a few dp, one after another.
 * Subtle enough to read as "settling", not as a slideshow.
 */
@Composable
fun Staggered(index: Int, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    var shown by remember { mutableStateOf(!Motion.enabled) }
    LaunchedEffect(Unit) { shown = true }
    val alpha by animateFloatAsState(if (shown) 1f else 0f, Motion.finite(320, delay = (index * 45).coerceAtMost(360)), label = "stagger")
    Box(
        modifier.graphicsLayer {
            this.alpha = alpha
            translationY = (1f - alpha) * 14.dp.toPx()
        },
    ) { content() }
}
