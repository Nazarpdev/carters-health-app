package com.carters.health

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.carters.health.ui.theme.HealthHaptics

/** Vibrator-backed haptic choreography for set completion and rest-timer celebrations. */
class AndroidHaptics(context: Context) : HealthHaptics {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    override fun tick() = play(VibrationEffect.createOneShot(12, 80))

    override fun confirm() = play(VibrationEffect.createOneShot(28, VibrationEffect.DEFAULT_AMPLITUDE))

    override fun celebrate() = play(
        VibrationEffect.createWaveform(longArrayOf(0, 60, 70, 60, 70, 140), intArrayOf(0, 160, 0, 200, 0, 255), -1),
    )

    private fun play(effect: VibrationEffect) {
        val v = vibrator ?: return
        if (v.hasVibrator()) v.vibrate(effect)
    }
}
