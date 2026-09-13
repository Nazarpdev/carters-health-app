package com.carters.health.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.carters.health.data.model.WeightUnit
import com.carters.health.ui.components.SectionHeader
import com.carters.health.ui.components.SegmentedControl
import com.carters.health.ui.components.SoftCard
import com.carters.health.ui.components.SoftSwitch
import com.carters.health.ui.components.Staggered
import com.carters.health.ui.settings.AppSettings
import com.carters.health.ui.settings.LocalAppSettings
import com.carters.health.ui.settings.ThemeMode
import com.carters.health.ui.theme.HealthColors
import com.carters.health.ui.theme.HealthTheme
import com.carters.health.ui.theme.LocalHealthHaptics

private val STEP_GOALS = listOf(6_000, 8_000, 10_000, 12_000)

@Composable
fun SettingsScreen(onBack: () -> Unit, modifier: Modifier = Modifier, settings: AppSettings = LocalAppSettings.current) {
    val haptics = LocalHealthHaptics.current
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.ArrowBack, contentDescription = "Back", tint = HealthColors.InkSoft,
                    modifier = Modifier.clip(CircleShape).clickable(onClick = onBack).padding(6.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text("Settings", style = MaterialTheme.typography.headlineMedium, color = HealthColors.Ink)
            }
        }
        item { Staggered(0) { SectionHeader("Appearance") } }
        item {
            Staggered(1) {
                SoftCard(contentPadding = PaddingValues(18.dp)) {
                    SettingRow("Theme", "System, light or dark") {
                        SegmentedControl(
                            options = ThemeMode.entries.map { it.label },
                            selected = settings.theme.ordinal,
                            onSelect = { haptics.tick(); settings.updateTheme(ThemeMode.entries[it]) },
                            modifier = Modifier.width(190.dp),
                            height = 32.dp,
                        )
                    }
                    Divider()
                    SettingRow("Animations", "Motion on pages, cards and controls") {
                        SoftSwitch(settings.animations, { settings.updateAnimations(it) })
                    }
                    Divider()
                    SettingRow("Haptics", "Vibration on taps and set completion") {
                        SoftSwitch(settings.haptics, { settings.updateHaptics(it) })
                    }
                }
            }
        }
        item { Staggered(2) { SectionHeader("Units & goals") } }
        item {
            Staggered(3) {
                SoftCard(contentPadding = PaddingValues(18.dp)) {
                    SettingRow("Weight unit", "Used across training and body weight") {
                        SegmentedControl(
                            options = WeightUnit.entries.map { it.label },
                            selected = settings.unit.ordinal,
                            onSelect = { haptics.tick(); settings.updateUnit(WeightUnit.entries[it]) },
                            modifier = Modifier.width(110.dp),
                            height = 32.dp,
                        )
                    }
                    Divider()
                    SettingRow("Daily step goal", "Fills the steps ring on Home", stacked = true) {
                        SegmentedControl(
                            options = STEP_GOALS.map { "${it / 1000}k" },
                            selected = STEP_GOALS.indexOf(settings.stepGoal).coerceAtLeast(0),
                            onSelect = { haptics.tick(); settings.updateStepGoal(STEP_GOALS[it]) },
                            modifier = Modifier.fillMaxWidth(),
                            accent = HealthColors.Ochre,
                            height = 34.dp,
                        )
                    }
                }
            }
        }
        item { Staggered(4) { SectionHeader("Training") } }
        item {
            Staggered(5) {
                SoftCard(contentPadding = PaddingValues(18.dp)) {
                    SettingRow("Auto rest timer", "Start resting when a set is ticked") {
                        SoftSwitch(settings.autoRestTimer, { settings.updateAutoRestTimer(it) })
                    }
                }
            }
        }
        item { Staggered(6) { SectionHeader("About") } }
        item {
            Staggered(7) {
                SoftCard(contentPadding = PaddingValues(18.dp)) {
                    SettingRow("Carter's Health", "Version 1.0.0") {}
                    Divider()
                    SettingRow("Watch", "Amazfit Balance · connected") {}
                    Divider()
                    SettingRow("Sleep source", "Sleep as Android") {}
                }
            }
        }
    }
}

@Composable
private fun SettingRow(title: String, description: String, stacked: Boolean = false, control: @Composable () -> Unit) {
    if (stacked) {
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, color = HealthColors.Ink)
            Text(description, style = MaterialTheme.typography.bodySmall, color = HealthColors.Muted)
            Spacer(Modifier.height(10.dp))
            control()
        }
    } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = HealthColors.Ink)
                Text(description, style = MaterialTheme.typography.bodySmall, color = HealthColors.Muted)
            }
            Spacer(Modifier.width(12.dp))
            control()
        }
    }
}

@Composable
private fun Divider() {
    Spacer(Modifier.height(14.dp))
    Box(Modifier.fillMaxWidth().height(1.dp).background(HealthColors.Hairline))
    Spacer(Modifier.height(14.dp))
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F1EA, heightDp = 1200)
@Composable
private fun SettingsPreview() {
    HealthTheme { SettingsScreen(onBack = {}) }
}
