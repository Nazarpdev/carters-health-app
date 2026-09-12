package com.carters.health.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Haptic choreography surface. The :app module provides a Vibrator-backed implementation;
 * previews and the desktop harness fall back to a silent no-op.
 */
interface HealthHaptics {
    /** Light tick — set toggled, chip pressed. */
    fun tick()
    /** Firm confirm — set completed, timer started. */
    fun confirm()
    /** Celebration pattern — rest complete, PR achieved. */
    fun celebrate()

    object None : HealthHaptics {
        override fun tick() = Unit
        override fun confirm() = Unit
        override fun celebrate() = Unit
    }
}

val LocalHealthHaptics = staticCompositionLocalOf<HealthHaptics> { HealthHaptics.None }
