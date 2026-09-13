package com.carters.health.ui.settings

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.carters.health.data.model.WeightUnit

enum class ThemeMode(val label: String) { SYSTEM("System"), LIGHT("Light"), DARK("Dark") }

/** Plain snapshot used for persistence. */
data class SettingsSnapshot(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val animations: Boolean = true,
    val haptics: Boolean = true,
    val unit: WeightUnit = WeightUnit.LBS,
    val stepGoal: Int = 10_000,
    val autoRestTimer: Boolean = true,
)

/** Storage contract; the :app module backs it with SharedPreferences. */
interface SettingsStore {
    fun load(): SettingsSnapshot?
    fun save(snapshot: SettingsSnapshot)

    object InMemory : SettingsStore {
        private var current: SettingsSnapshot? = null
        override fun load(): SettingsSnapshot? = current
        override fun save(snapshot: SettingsSnapshot) { current = snapshot }
    }
}

/** Observable app settings; every write persists through [store]. */
@Stable
class AppSettings(private val store: SettingsStore = SettingsStore.InMemory) {
    private val initial = store.load() ?: SettingsSnapshot()

    var theme by mutableStateOf(initial.theme)
        private set
    var animations by mutableStateOf(initial.animations)
        private set
    var haptics by mutableStateOf(initial.haptics)
        private set
    var unit by mutableStateOf(initial.unit)
        private set
    var stepGoal by mutableStateOf(initial.stepGoal)
        private set
    var autoRestTimer by mutableStateOf(initial.autoRestTimer)
        private set

    fun updateTheme(value: ThemeMode) { theme = value; persist() }
    fun updateAnimations(value: Boolean) { animations = value; persist() }
    fun updateHaptics(value: Boolean) { haptics = value; persist() }
    fun updateUnit(value: WeightUnit) { unit = value; persist() }
    fun updateStepGoal(value: Int) { stepGoal = value; persist() }
    fun updateAutoRestTimer(value: Boolean) { autoRestTimer = value; persist() }

    fun isDark(systemDark: Boolean): Boolean = when (theme) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    private fun persist() = store.save(SettingsSnapshot(theme, animations, haptics, unit, stepGoal, autoRestTimer))
}

val LocalAppSettings = staticCompositionLocalOf { AppSettings() }
