package com.carters.health.ui.screens.weight

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.carters.health.data.model.Units
import com.carters.health.data.model.WeightEntry
import com.carters.health.data.model.WeightSource
import com.carters.health.data.model.WeightUnit
import com.carters.health.data.repo.HealthRepository
import com.carters.health.data.repo.InMemoryHealthRepository
import com.carters.health.data.repo.SampleData
import com.carters.health.ui.components.ChartSeries
import com.carters.health.ui.components.DeltaBadge
import com.carters.health.ui.components.Eyebrow
import com.carters.health.ui.components.GlowCard
import com.carters.health.ui.components.GradientButton
import com.carters.health.ui.components.NumberField
import com.carters.health.ui.components.Pill
import com.carters.health.ui.components.PulsingDot
import com.carters.health.ui.components.ScrubbableLineChart
import com.carters.health.ui.components.SectionHeader
import com.carters.health.ui.components.SegmentedControl
import com.carters.health.ui.components.UnitToggle
import com.carters.health.ui.theme.HealthColors
import com.carters.health.ui.theme.HealthTheme
import com.carters.health.ui.theme.LocalHealthHaptics
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private enum class Timeframe(val label: String, val days: Long) { D7("7D", 7), D30("30D", 30), D90("90D", 90), Y1("1Y", 365) }

@Composable
fun WeightTrendScreen(
    repository: HealthRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val history by repository.weightHistory.collectAsState()
    var unit by rememberSaveable { mutableStateOf(WeightUnit.LBS) }
    var timeframe by rememberSaveable { mutableStateOf(Timeframe.D30) }
    var showLog by remember { mutableStateOf(false) }
    var scanning by remember { mutableStateOf(false) }
    val haptics = LocalHealthHaptics.current

    val sorted = remember(history) { history.sortedBy { it.time } }
    val latest = sorted.lastOrNull()
    val baseline30 = remember(sorted) {
        val cutoff = latest?.time?.minusDays(30)
        sorted.filter { cutoff != null && it.time >= cutoff }.map { it.weightLbs }.average().takeIf { !it.isNaN() } ?: latest?.weightLbs ?: 0.0
    }
    val window = remember(sorted, timeframe) {
        val cutoff = latest?.time?.minusDays(timeframe.days)
        sorted.filter { cutoff != null && it.time >= cutoff }
    }
    val composition = remember(latest) { latest?.let { SampleData.bodyComposition(it) } }
    val timeFmt = remember { DateTimeFormatter.ofPattern("EEE h:mm a") }

    LaunchedEffect(scanning) { if (scanning) { delay(4000); scanning = false } }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(HealthColors.Obsidian),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.ArrowBack, contentDescription = "Back", tint = HealthColors.Sand,
                    modifier = Modifier.clip(CircleShape).clickable(onClick = onBack).padding(6.dp),
                )
                Spacer(Modifier.width(6.dp))
                Column(Modifier.weight(1f)) {
                    Text("Weight & body", style = MaterialTheme.typography.headlineMedium, color = HealthColors.OnSurface)
                    Text("Fitbit Aria + BLE scale, unified", style = MaterialTheme.typography.bodySmall, color = HealthColors.Clay)
                }
                UnitToggle(unit, { unit = it })
            }
        }
        item {
            HeroWeightCard(latest, unit, latest?.weightLbs?.minus(baseline30) ?: 0.0, timeFmt)
        }
        item {
            GlowCard(accent = HealthColors.Gold, glow = false, contentPadding = PaddingValues(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Eyebrow("Trend", HealthColors.Gold)
                        Text("${window.size} weigh-ins", style = MaterialTheme.typography.bodySmall, color = HealthColors.Clay)
                    }
                    SegmentedControl(
                        Timeframe.entries.map { it.label }, timeframe.ordinal, { haptics.tick(); timeframe = Timeframe.entries[it] },
                        Modifier.width(200.dp), accent = HealthColors.Gold, height = 32.dp,
                    )
                }
                Spacer(Modifier.height(8.dp))
                val fmt = if (timeframe == Timeframe.D7) DateTimeFormatter.ofPattern("EEE") else DateTimeFormatter.ofPattern("MMM d")
                ScrubbableLineChart(
                    series = listOf(ChartSeries(window.map { Units.displayWeight(it.weightLbs, unit).toFloat() }, HealthColors.Amber, listOf(HealthColors.Amber, HealthColors.Gold))),
                    modifier = Modifier.fillMaxWidth().height(230.dp),
                    baseline = Units.displayWeight(baseline30, unit).toFloat(),
                    xLabel = { i -> window.getOrNull(i)?.time?.format(fmt) ?: "" },
                    xLabelCount = if (timeframe == Timeframe.D7) window.size.coerceIn(2, 7) else 4,
                    formatValue = { String.format("%.1f", it) },
                    scrubLabel = { i -> window.getOrNull(i)?.let { "${it.time.format(DateTimeFormatter.ofPattern("MMM d, h:mm a"))} · ${it.source.label}" } ?: "" },
                )
            }
        }
        if (composition != null) {
            item {
                SectionHeader("Body composition", subtitle = "From latest smart-scale reading")
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CompositionTile("Body fat", String.format("%.1f", composition.bodyFatPercent), "%", composition.bodyFatCategory, HealthColors.Coral, Modifier.weight(1f))
                        CompositionTile("BMI", String.format("%.1f", composition.bmi), "", composition.bmiCategory, HealthColors.Mint, Modifier.weight(1f))
                    }
                    CompositionTile(
                        "Lean body mass", String.format("%.1f", Units.displayWeight(composition.leanMassLbs, unit)), unit.label,
                        "muscle · bone · water", HealthColors.Lavender, Modifier.fillMaxWidth(),
                        support = "${String.format("%.1f", 100 - composition.bodyFatPercent)}% of body weight is lean tissue",
                    )
                }
            }
        }
        item { SectionHeader("Scale sync & log") }
        item {
            ScaleSyncCard(scanning) { haptics.confirm(); scanning = true }
        }
        item {
            GradientButton("Log Weight", icon = Icons.Default.Add, brush = HealthColors.amberGold, modifier = Modifier.fillMaxWidth()) { showLog = true }
        }
        item { SectionHeader("Weigh-in history", subtitle = "${sorted.size} entries") }
        items(sorted.asReversed().take(30).size, key = { sorted.asReversed()[it].id }) { i ->
            val entry = sorted.asReversed()[i]
            val prev = sorted.asReversed().getOrNull(i + 1)
            HistoryRow(entry, prev, unit)
        }
    }

    if (showLog) {
        LogWeightDialog(unit, onDismiss = { showLog = false }) { lbs, fat ->
            repository.logWeight(lbs, fat)
            haptics.celebrate()
            showLog = false
        }
    }
}

@Composable
private fun HeroWeightCard(latest: WeightEntry?, unit: WeightUnit, delta: Double, fmt: DateTimeFormatter) {
    GlowCard(accent = HealthColors.Amber, contentPadding = PaddingValues(22.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Scale, contentDescription = null, tint = HealthColors.Amber, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Eyebrow("Current weight", HealthColors.Amber)
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                String.format("%.1f", Units.displayWeight(latest?.weightLbs ?: 0.0, unit)),
                style = MaterialTheme.typography.displayLarge, color = HealthColors.OnSurface, fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.width(8.dp))
            Text(unit.label, style = MaterialTheme.typography.titleLarge, color = HealthColors.Clay, modifier = Modifier.padding(bottom = 10.dp))
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DeltaBadge(Units.displayWeight(delta, unit), unit.label, lowerIsBetter = true, suffix = " vs 30D")
            if (latest != null) Pill(latest.source.name, color = if (latest.source == WeightSource.BLE_SCALE) HealthColors.Mint else HealthColors.Gold, filled = true)
        }
        Spacer(Modifier.height(10.dp))
        Text(
            if (latest != null) "Last synced ${latest.time.format(fmt)}" else "No readings yet",
            style = MaterialTheme.typography.bodySmall, color = HealthColors.Clay,
        )
    }
}

@Composable
private fun CompositionTile(label: String, value: String, unit: String, category: String, accent: Color, modifier: Modifier = Modifier, support: String? = null) {
    GlowCard(modifier = modifier, accent = accent, glow = false, contentPadding = PaddingValues(14.dp)) {
        Eyebrow(label)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, style = MaterialTheme.typography.headlineSmall, color = HealthColors.OnSurface)
            if (unit.isNotEmpty()) { Spacer(Modifier.width(3.dp)); Text(unit, style = MaterialTheme.typography.labelSmall, color = HealthColors.Clay, modifier = Modifier.padding(bottom = 4.dp)) }
            if (support != null) {
                Spacer(Modifier.weight(1f))
                Text(support, style = MaterialTheme.typography.bodySmall, color = HealthColors.Clay, modifier = Modifier.padding(bottom = 3.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        Pill(category, color = accent, filled = true)
    }
}

@Composable
private fun ScaleSyncCard(scanning: Boolean, onScan: () -> Unit) {
    GlowCard(accent = HealthColors.Emerald, glow = scanning, contentPadding = PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadarBeacon(active = scanning, modifier = Modifier.size(64.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(if (scanning) "Scanning for scales…" else "Connect Scale", style = MaterialTheme.typography.titleMedium, color = HealthColors.OnSurface)
                Text(
                    if (scanning) "Step on the scale to broadcast a reading (GATT 0x181D)" else "Pair a Bluetooth smart scale for automatic weigh-ins",
                    style = MaterialTheme.typography.bodySmall, color = HealthColors.Clay,
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(HealthColors.emeraldMint)
                        .clickable(enabled = !scanning, onClick = onScan)
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(if (scanning) Icons.Default.BluetoothSearching else Icons.Default.Bluetooth, contentDescription = null, tint = HealthColors.Obsidian, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (scanning) "Listening" else "Scan", style = MaterialTheme.typography.labelLarge, color = HealthColors.Obsidian, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/** Bluetooth radar: expanding rings while scanning, calm dot when idle. */
@Composable
private fun RadarBeacon(active: Boolean, modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "radar")
    val sweep by t.animateFloat(0f, 1f, infiniteRepeatable(tween(1800, easing = LinearEasing)), label = "sweep")
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val c = Offset(size.width / 2, size.height / 2)
            val r = size.minDimension / 2
            if (active) {
                for (k in 0 until 3) {
                    val p = ((sweep + k / 3f) % 1f)
                    drawCircle(HealthColors.Mint.copy(alpha = (1 - p) * 0.5f), r * p, c, style = Stroke(2.dp.toPx()))
                }
            } else {
                drawCircle(HealthColors.Emerald.copy(alpha = 0.12f), r, c)
                drawCircle(HealthColors.Emerald.copy(alpha = 0.3f), r, c, style = Stroke(1.dp.toPx()))
            }
        }
        if (active) PulsingDot(HealthColors.Mint, 10.dp) else Icon(Icons.Default.Bluetooth, contentDescription = null, tint = HealthColors.Emerald)
    }
}

@Composable
private fun HistoryRow(entry: WeightEntry, previous: WeightEntry?, unit: WeightUnit) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier.fillMaxWidth().clip(shape).background(HealthColors.Espresso).border(1.dp, HealthColors.Border, shape).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(entry.time.format(DateTimeFormatter.ofPattern("EEE, MMM d · h:mm a")), style = MaterialTheme.typography.titleSmall, color = HealthColors.Sand)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Pill(entry.source.label, color = when (entry.source) { WeightSource.BLE_SCALE -> HealthColors.Mint; WeightSource.FITBIT_SCALE -> HealthColors.Gold; WeightSource.MANUAL -> HealthColors.Clay })
                entry.bodyFatPercent?.let { Pill(String.format("%.1f%% fat", it), color = HealthColors.Coral) }
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("${String.format("%.1f", Units.displayWeight(entry.weightLbs, unit))} ${unit.label}", style = MaterialTheme.typography.titleMedium, color = HealthColors.OnSurface)
            if (previous != null) {
                val d = Units.displayWeight(entry.weightLbs - previous.weightLbs, unit)
                Text(String.format("%+.1f", d), style = MaterialTheme.typography.labelSmall, color = if (d <= 0) HealthColors.Emerald else HealthColors.Coral)
            }
        }
    }
}

@Composable
private fun LogWeightDialog(unit: WeightUnit, onDismiss: () -> Unit, onSave: (lbs: Double, fat: Double?) -> Unit) {
    var weight by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        GlowCard(accent = HealthColors.Amber, contentPadding = PaddingValues(20.dp)) {
            Text("Manual weigh-in", style = MaterialTheme.typography.headlineSmall, color = HealthColors.OnSurface)
            Text(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d · h:mm a")), style = MaterialTheme.typography.bodySmall, color = HealthColors.Clay)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(Modifier.weight(1f)) {
                    Eyebrow("Weight (${unit.label})")
                    Spacer(Modifier.height(6.dp))
                    NumberField(weight, { weight = it }, Modifier.fillMaxWidth(), placeholder = "0.0")
                }
                Column(Modifier.weight(1f)) {
                    Eyebrow("Body fat %")
                    Spacer(Modifier.height(6.dp))
                    NumberField(fat, { fat = it }, Modifier.fillMaxWidth(), placeholder = "optional", accent = HealthColors.Coral)
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge, color = HealthColors.Clay, modifier = Modifier.clickable(onClick = onDismiss).padding(8.dp))
                Spacer(Modifier.width(8.dp))
                GradientButton("Save", modifier = Modifier.weight(1f), height = 46.dp) {
                    val w = weight.toDoubleOrNull() ?: return@GradientButton
                    onSave(Units.toLbs(w, unit), fat.toDoubleOrNull())
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, heightDp = 1400)
@Composable
private fun WeightPreview() {
    HealthTheme { WeightTrendScreen(InMemoryHealthRepository(), onBack = {}) }
}
