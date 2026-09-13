package com.carters.health

import android.content.Context
import com.carters.health.data.model.WeightUnit
import com.carters.health.ui.settings.SettingsSnapshot
import com.carters.health.ui.settings.SettingsStore
import com.carters.health.ui.settings.ThemeMode

/** SharedPreferences-backed settings persistence. */
class PreferencesSettingsStore(context: Context) : SettingsStore {
    private val prefs = context.getSharedPreferences("carters_health_settings", Context.MODE_PRIVATE)

    override fun load(): SettingsSnapshot? {
        if (!prefs.contains("theme")) return null
        val defaults = SettingsSnapshot()
        return SettingsSnapshot(
            theme = prefs.getString("theme", null)?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: defaults.theme,
            animations = prefs.getBoolean("animations", defaults.animations),
            haptics = prefs.getBoolean("haptics", defaults.haptics),
            unit = prefs.getString("unit", null)?.let { runCatching { WeightUnit.valueOf(it) }.getOrNull() } ?: defaults.unit,
            stepGoal = prefs.getInt("stepGoal", defaults.stepGoal),
            autoRestTimer = prefs.getBoolean("autoRestTimer", defaults.autoRestTimer),
        )
    }

    override fun save(snapshot: SettingsSnapshot) {
        prefs.edit()
            .putString("theme", snapshot.theme.name)
            .putBoolean("animations", snapshot.animations)
            .putBoolean("haptics", snapshot.haptics)
            .putString("unit", snapshot.unit.name)
            .putInt("stepGoal", snapshot.stepGoal)
            .putBoolean("autoRestTimer", snapshot.autoRestTimer)
            .apply()
    }
}
