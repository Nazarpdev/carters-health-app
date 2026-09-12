package com.carters.health.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/** Shared motion vocabulary so every surface breathes the same way. */
object Motion {
    val softSpring = spring<Float>(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
    val gentleSpring = spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)
    val snappySpring = spring<Float>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
    val reveal = tween<Float>(durationMillis = 900)
}

/**
 * Animates from 0 to [target] on first composition (a "reveal"), then springs to any new target.
 * Used by every dial, ring and chart so screens feel alive when they appear.
 */
@Composable
fun rememberRevealedProgress(target: Float, label: String = "reveal"): State<Float> {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    return animateFloatAsState(
        targetValue = if (started) target else 0f,
        animationSpec = if (started) Motion.gentleSpring else Motion.reveal,
        label = label,
    )
}
