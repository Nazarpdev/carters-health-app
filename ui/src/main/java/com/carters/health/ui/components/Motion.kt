package com.carters.health.ui.components

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

/**
 * Shared motion vocabulary. Every spec goes through here so the "Animations" setting can turn
 * the whole app instant: when [enabled] is false, springs become snaps and transitions become
 * immediate. Read [enabled] inside infinite animations to render their resting frame instead.
 */
object Motion {
    var enabled: Boolean by mutableStateOf(true)

    fun <T> soft(): AnimationSpec<T> = if (enabled) spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow) else snap()
    fun <T> gentle(): AnimationSpec<T> = if (enabled) spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow) else snap()
    fun <T> snappy(): AnimationSpec<T> = if (enabled) spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium) else snap()
    fun <T> quick(): AnimationSpec<T> = if (enabled) tween(180) else snap()
    fun <T> reveal(): AnimationSpec<T> = if (enabled) tween(900) else snap()
    fun <T> finite(millis: Int, delay: Int = 0): FiniteAnimationSpec<T> = if (enabled) tween(millis, delayMillis = delay) else snap()

    // ---- Transitions, named by the action they belong to ----

    /** Content that was added: grows into place. */
    fun grow(): EnterTransition = if (enabled) expandVertically(finite(260)) + fadeIn(finite(200, 60)) else EnterTransition.None
    fun shrink(): ExitTransition = if (enabled) shrinkVertically(finite(200)) + fadeOut(finite(120)) else ExitTransition.None

    /** A banner or toast dropping in from the top edge. */
    fun dropIn(): EnterTransition = if (enabled) slideInVertically(finite(320)) { -it } + fadeIn(finite(200)) else EnterTransition.None
    fun liftOut(): ExitTransition = if (enabled) slideOutVertically(finite(220)) { -it } + fadeOut(finite(160)) else ExitTransition.None

    /** The floating tab bar rising from the bottom edge. */
    fun riseIn(): EnterTransition = if (enabled) slideInVertically(finite(320)) { it } + fadeIn(finite(200)) else EnterTransition.None
    fun sinkOut(): ExitTransition = if (enabled) slideOutVertically(finite(220)) { it } + fadeOut(finite(160)) else ExitTransition.None

    /** A dialog settling into view. */
    fun settleIn(): EnterTransition = if (enabled) scaleIn(finite(220), initialScale = 0.94f) + fadeIn(finite(180)) else EnterTransition.None
    fun settleOut(): ExitTransition = if (enabled) scaleOut(finite(160), targetScale = 0.96f) + fadeOut(finite(140)) else ExitTransition.None

    /** Sibling pages: slide a short distance in the direction of travel. */
    fun slideIn(forward: Boolean, fraction: Int = 8): EnterTransition =
        if (enabled) slideInHorizontally(finite(260)) { if (forward) it / fraction else -it / fraction } + fadeIn(finite(200)) else EnterTransition.None
    fun slideOut(forward: Boolean, fraction: Int = 8): ExitTransition =
        if (enabled) slideOutHorizontally(finite(220)) { if (forward) -it / fraction else it / fraction } + fadeOut(finite(160)) else ExitTransition.None

    /** A detail page pushed on top: enters from the right edge while the parent settles back. */
    fun pushIn(): EnterTransition = if (enabled) slideInHorizontally(finite(300)) { it } else EnterTransition.None
    fun pushParentOut(): ExitTransition = if (enabled) scaleOut(finite(300), targetScale = 0.96f) + fadeOut(finite(240)) else ExitTransition.None
    fun popParentIn(): EnterTransition = if (enabled) scaleIn(finite(280), initialScale = 0.96f) + fadeIn(finite(220)) else EnterTransition.None
    fun popOut(): ExitTransition = if (enabled) slideOutHorizontally(finite(260)) { it } else ExitTransition.None

    fun offsetSpec(): FiniteAnimationSpec<IntOffset> = if (enabled) spring(stiffness = Spring.StiffnessMediumLow) else snap()
    fun sizeSpec(): FiniteAnimationSpec<IntSize> = if (enabled) spring(stiffness = Spring.StiffnessMediumLow) else snap()
}

/**
 * Animates from 0 to [target] on first composition (a "reveal"), then springs to any new target.
 * Used by every dial, ring and chart so screens feel alive when they appear.
 */
@Composable
fun rememberRevealedProgress(target: Float, label: String = "reveal"): State<Float> {
    if (!Motion.enabled) return rememberUpdatedState(target)
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    return animateFloatAsState(
        targetValue = if (started) target else 0f,
        animationSpec = if (started) Motion.gentle() else Motion.reveal(),
        label = label,
    )
}
