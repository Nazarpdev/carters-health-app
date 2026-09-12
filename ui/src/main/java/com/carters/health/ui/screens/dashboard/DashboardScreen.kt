package com.carters.health.ui.screens.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.carters.health.data.model.format0
import com.carters.health.data.repo.HealthRepository
import com.carters.health.data.repo.InMemoryHealthRepository
import com.carters.health.ui.components.BeatingHeart
import com.carters.health.ui.components.DeltaBadge
import com.carters.health.ui.components.DialRing
import com.carters.health.ui.components.Eyebrow
import com.carters.health.ui.components.SoftCard
import com.carters.health.ui.components.PrimaryButton
import com.carters.health.ui.components.Pill
import com.carters.health.ui.components.PulsingDot
import com.carters.health.ui.components.RadialGauge
import com.carters.health.ui.components.SectionHeader
import com.carters.health.ui.components.SegmentedRing
import com.carters.health.ui.components.Sparkline
import com.carters.health.ui.components.ZeppArcDial
import com.carters.health.ui.theme.HealthColors
import com.carters.health.ui.theme.HealthTheme
import com.carters.health.ui.theme.LocalHealthHaptics
import kotlinx.coroutines.delay
import java.time.LocalTime

/** Circadian copy: the greeting and the tone shift with the hour of day. */
internal data class Greeting(val title: String, val body: String, val accent: Color)

internal fun greetingFor(hour: Int, readiness: Int, strain: Double): Greeting {
    val recoveryLine = when {
        readiness >= 80 -> "your recovery is primed for high intensity"
        readiness >= 60 -> "you're recovered enough for a solid session"
        else -> "keep today light — your body is still rebuilding"
    }
    return when (hour) {
        in 5..11 -> Greeting("Good morning", "$recoveryLine.", HealthColors.Green)
        in 12..16 -> Greeting("Good afternoon", if (strain < 8) "plenty of room left for strain today." else "$recoveryLine.", HealthColors.Green)
        in 17..20 -> Greeting("Good evening", if (strain >= 14) "big day — start winding down." else "$recoveryLine.", HealthColors.Terracotta)
        else -> Greeting("Rest & recharge tonight", "dim the lights — deep sleep builds tomorrow's readiness.", HealthColors.Sage)
    }
}

@Composable
fun DashboardScreen(
    repository: HealthRepository,
    onOpenWeight: () -> Unit,
    onStartWorkout: () -> Unit,
    modifier: Modifier = Modifier,
    hourOfDay: Int = LocalTime.now().hour,
) {
    val watch by repository.watchStatus.collectAsState()
    val readiness by repository.readiness.collectAsState()
    val activity by repository.activity.collectAsState()
    val liveHr by repository.liveHeartRate.collectAsState()
    val latestWeight by repository.latestWeight.collectAsState(initial = null)
    val weights by repository.weightHistory.collectAsState()
    val rhr by repository.restingHr.collectAsState()
    val hrv by repository.hrv.collectAsState()
    val greeting = remember(hourOfDay, readiness) { greetingFor(hourOfDay, readiness.readinessPercent, readiness.strain) }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(HealthColors.Canvas),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val edge = Modifier.padding(horizontal = 20.dp)
        item {
            GreetingHeader(greeting, watch.connected, watch.deviceName, watch.batteryPercent, edge)
        }
        item {
            PillarDialCard(readiness, edge)
        }
        item {
            LiveBiometricsStrip(
                bpm = liveHr.lastOrNull()?.bpm ?: 0,
                sparkline = liveHr.map { it.bpm.toFloat() },
                modifier = edge,
            )
        }
        item {
            SectionHeader("Body & activity", subtitle = "Swipe for more", modifier = edge)
        }
        item {
            val baseline30 = remember(weights) {
                val cutoff = weights.lastOrNull()?.time?.minusDays(30)
                weights.filter { cutoff != null && it.time >= cutoff }.map { it.weightLbs }.average().takeIf { !it.isNaN() }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(horizontal = 20.dp)) {
                item {
                    WeightCard(
                        weightLbs = latestWeight?.weightLbs ?: 0.0,
                        delta = if (latestWeight != null && baseline30 != null) latestWeight!!.weightLbs - baseline30 else 0.0,
                        source = latestWeight?.source?.label ?: "—",
                        recent = weights.takeLast(14).map { it.weightLbs.toFloat() },
                        onClick = onOpenWeight,
                    )
                }
                item {
                    StepsCard(activity.steps, activity.stepGoal, activity.distanceKm, activity.activeCalories)
                }
                item {
                    BaselineCard("Resting HR", rhr.current, "bpm", rhr.sevenDayAverage, lowerIsBetter = true, accent = HealthColors.Terracotta)
                }
                item {
                    BaselineCard("HRV", hrv.current, "ms", hrv.sevenDayAverage, lowerIsBetter = false, accent = HealthColors.Green)
                }
            }
        }
        item {
            Row(edge, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BurnTile(activity.activeCalories, activity.totalCalories, Modifier.weight(1f))
                WindDownTile(hourOfDay, readiness.sleepDurationMinutes, Modifier.weight(1f))
            }
        }
        item {
            PrimaryButton(
                text = "Start Strength Workout",
                icon = Icons.Default.FitnessCenter,
                color = HealthColors.Green,
                modifier = edge.fillMaxWidth(),
                onClick = onStartWorkout,
            )
        }
    }
}

/** Calories: active burn ring over total, warm amber. */
@Composable
private fun BurnTile(active: Int, total: Int, modifier: Modifier = Modifier) {
    SoftCard(modifier = modifier, accent = HealthColors.Green, contentPadding = PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = HealthColors.Green, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Eyebrow("Burn")
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadialGauge(progress = active / total.toFloat().coerceAtLeast(1f), modifier = Modifier.size(58.dp), colors = listOf(HealthColors.Green, HealthColors.Green), strokeWidth = 6.dp) {
                Text("${(active * 100 / total.coerceAtLeast(1))}%", style = MaterialTheme.typography.labelSmall, color = HealthColors.Green)
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(active.toDouble().format0(), style = MaterialTheme.typography.titleLarge, color = HealthColors.Ink)
                Text("active kcal", style = MaterialTheme.typography.labelSmall, color = HealthColors.Muted)
                Text("${total.toDouble().format0()} total", style = MaterialTheme.typography.bodySmall, color = HealthColors.Faint)
            }
        }
    }
}

/** Circadian nudge: a suggested wind-down window based on last night's rest. */
@Composable
private fun WindDownTile(hour: Int, lastSleepMinutes: Int, modifier: Modifier = Modifier) {
    val debt = (480 - lastSleepMinutes).coerceAtLeast(0)
    val bedtime = if (debt > 60) "9:45 PM" else "10:30 PM"
    val copy = when {
        hour >= 21 -> "Screens down. Lights warm."
        debt > 60 -> "Repay ${debt / 60}h ${debt % 60}m of sleep debt tonight."
        else -> "Rhythm is steady. Keep the routine."
    }
    SoftCard(modifier = modifier, accent = HealthColors.Sage, contentPadding = PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Bedtime, contentDescription = null, tint = HealthColors.Sage, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Eyebrow("Wind-down")
        }
        Spacer(Modifier.height(8.dp))
        Text(bedtime, style = MaterialTheme.typography.titleLarge, color = HealthColors.Ink)
        Text("target bedtime", style = MaterialTheme.typography.labelSmall, color = HealthColors.Muted)
        Spacer(Modifier.height(6.dp))
        Text(copy, style = MaterialTheme.typography.bodySmall, color = HealthColors.Sage)
    }
}

@Composable
private fun GreetingHeader(greeting: Greeting, connected: Boolean, deviceName: String, battery: Int, modifier: Modifier = Modifier) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(HealthColors.tint(greeting.accent))
                    .padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            ) {
                Text(greeting.title, style = MaterialTheme.typography.labelLarge, color = greeting.accent)
                Spacer(Modifier.width(6.dp))
                Box(Modifier.size(7.dp).clip(CircleShape).background(greeting.accent))
                Spacer(Modifier.width(6.dp))
            }
            Spacer(Modifier.weight(1f))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(HealthColors.Card)
                    .padding(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
            ) {
                PulsingDot(color = if (connected) HealthColors.Green else HealthColors.Faint, size = 8.dp)
                Icon(Icons.Default.Watch, contentDescription = "Watch", tint = HealthColors.InkSoft, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = if (battery > 20) HealthColors.Green else HealthColors.Terracotta, modifier = Modifier.size(14.dp))
                Text("$battery%", style = MaterialTheme.typography.labelMedium, color = HealthColors.InkSoft)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            greeting.body.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.headlineMedium,
            color = HealthColors.Ink,
        )
        Text(deviceName, style = MaterialTheme.typography.bodySmall, color = HealthColors.Faint)
    }
}

@Composable
private fun PillarDialCard(readiness: com.carters.health.data.model.ReadinessSnapshot, modifier: Modifier = Modifier) {
    val sleepH = readiness.sleepDurationMinutes / 60
    val sleepM = readiness.sleepDurationMinutes % 60
    SoftCard(modifier = modifier, accent = HealthColors.Green, contentPadding = PaddingValues(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ZeppArcDial(
                rings = listOf(
                    DialRing(readiness.readinessPercent / 100f, listOf(HealthColors.GreenDeep), "Readiness"),
                    DialRing((readiness.strain / 21.0).toFloat(), listOf(HealthColors.Terracotta), "Strain"),
                    DialRing(readiness.sleepPerformancePercent / 100f, listOf(HealthColors.Sage), "Sleep"),
                ),
                modifier = Modifier.size(172.dp),
                strokeWidth = 11.dp,
                gap = 6.dp,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${readiness.readinessPercent}", style = MaterialTheme.typography.displaySmall, color = HealthColors.Ink)
                    Eyebrow("Ready", HealthColors.Green)
                }
            }
            Spacer(Modifier.width(18.dp))
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                PillarRow(HealthColors.GreenDeep, "Recovery", "${readiness.readinessPercent}%", "HRV ${readiness.hrvMs} ms")
                PillarRow(HealthColors.Terracotta, "Strain", String.format("%.1f", readiness.strain), "of 21.0")
                PillarRow(HealthColors.Sage, "Sleep", "${readiness.sleepPerformancePercent}%", "${sleepH}h ${sleepM}m rest")
            }
        }
    }
}

@Composable
private fun PillarRow(color: Color, label: String, value: String, support: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(8.dp))
        Column {
            Eyebrow(label)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, style = MaterialTheme.typography.titleLarge, color = HealthColors.Ink)
                Spacer(Modifier.width(6.dp))
                Text(support, style = MaterialTheme.typography.bodySmall, color = HealthColors.Muted, modifier = Modifier.padding(bottom = 2.dp))
            }
        }
    }
}

@Composable
private fun LiveBiometricsStrip(bpm: Int, sparkline: List<Float>, modifier: Modifier = Modifier) {
    val haptics = LocalHealthHaptics.current
    var spotCheck by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(spotCheck) {
        if (spotCheck != null) { delay(2600); spotCheck = null }
    }
    SoftCard(modifier = modifier, accent = HealthColors.Terracotta, contentPadding = PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BeatingHeart(bpm = bpm.coerceAtLeast(40), size = 30.dp)
            Spacer(Modifier.width(12.dp))
            Column {
                Eyebrow("Live heart rate", HealthColors.Terracotta)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("$bpm", style = MaterialTheme.typography.displaySmall, color = HealthColors.Ink)
                    Spacer(Modifier.width(4.dp))
                    Text("bpm", style = MaterialTheme.typography.labelLarge, color = HealthColors.Muted, modifier = Modifier.padding(bottom = 6.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Sparkline(sparkline, Modifier.weight(1f).height(52.dp), color = HealthColors.Terracotta)
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SpotCheckButton(
                label = if (spotCheck == "spo2") "Measuring…" else "Spot SpO₂",
                icon = Icons.Default.Bloodtype,
                accent = HealthColors.Green,
                active = spotCheck == "spo2",
                modifier = Modifier.weight(1f),
            ) { haptics.confirm(); spotCheck = "spo2" }
            SpotCheckButton(
                label = if (spotCheck == "stress") "Measuring…" else "Spot Stress",
                icon = Icons.Default.Psychology,
                accent = HealthColors.Sage,
                active = spotCheck == "stress",
                modifier = Modifier.weight(1f),
            ) { haptics.confirm(); spotCheck = "stress" }
        }
    }
}

@Composable
private fun SpotCheckButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, accent: Color, active: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(if (active) HealthColors.tint(accent) else HealthColors.Field)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (active) PulsingDot(accent, 6.dp) else Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = if (active) accent else HealthColors.InkSoft)
    }
}

@Composable
private fun WeightCard(weightLbs: Double, delta: Double, source: String, recent: List<Float>, onClick: () -> Unit) {
    SoftCard(modifier = Modifier.width(210.dp), accent = HealthColors.Green, contentPadding = PaddingValues(16.dp), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Scale, contentDescription = null, tint = HealthColors.Green, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Eyebrow("Body weight")
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(String.format("%.1f", weightLbs), style = MaterialTheme.typography.headlineMedium, color = HealthColors.Ink)
            Spacer(Modifier.width(4.dp))
            Text("lbs", style = MaterialTheme.typography.labelLarge, color = HealthColors.Muted, modifier = Modifier.padding(bottom = 4.dp))
        }
        Spacer(Modifier.height(6.dp))
        DeltaBadge(delta, "lbs", lowerIsBetter = true, suffix = " · 30D")
        Spacer(Modifier.height(10.dp))
        Sparkline(recent, Modifier.fillMaxWidth().height(34.dp), color = HealthColors.Green, showEndDot = false)
        Spacer(Modifier.height(8.dp))
        Pill(source, color = HealthColors.Green)
    }
}

@Composable
private fun StepsCard(steps: Int, goal: Int, km: Double, activeKcal: Int) {
    SoftCard(modifier = Modifier.width(236.dp), accent = HealthColors.Green, contentPadding = PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DirectionsWalk, contentDescription = null, tint = HealthColors.Green, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Eyebrow("Daily steps")
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            SegmentedRing(progress = steps / goal.toFloat(), modifier = Modifier.size(78.dp), strokeWidth = 7.dp) {
                Text("${(steps * 100 / goal)}%", style = MaterialTheme.typography.labelLarge, color = HealthColors.Ink)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(steps.toDouble().format0(), style = MaterialTheme.typography.headlineSmall, color = HealthColors.Ink)
                Text("of ${goal.toDouble().format0()}", style = MaterialTheme.typography.bodySmall, color = HealthColors.Muted)
                Spacer(Modifier.height(6.dp))
                Text(String.format("%.1f km · %d kcal", km, activeKcal), style = MaterialTheme.typography.labelMedium, color = HealthColors.Green)
            }
        }
    }
}

@Composable
private fun BaselineCard(label: String, current: Int, unit: String, baseline: Double, lowerIsBetter: Boolean, accent: Color) {
    SoftCard(modifier = Modifier.width(170.dp), accent = accent, contentPadding = PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.MonitorHeart, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Eyebrow(label)
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text("$current", style = MaterialTheme.typography.headlineMedium, color = HealthColors.Ink)
            Spacer(Modifier.width(4.dp))
            Text(unit, style = MaterialTheme.typography.labelLarge, color = HealthColors.Muted, modifier = Modifier.padding(bottom = 4.dp))
        }
        Spacer(Modifier.height(6.dp))
        DeltaBadge(current - baseline, unit, lowerIsBetter = lowerIsBetter, suffix = " · 7D")
        Spacer(Modifier.height(6.dp))
        Text("7-day avg ${String.format("%.1f", baseline)}", style = MaterialTheme.typography.bodySmall, color = HealthColors.Muted)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F1EA, heightDp = 1200)
@Composable
private fun DashboardPreview() {
    HealthTheme {
        DashboardScreen(repository = InMemoryHealthRepository(), onOpenWeight = {}, onStartWorkout = {}, hourOfDay = 8)
    }
}
