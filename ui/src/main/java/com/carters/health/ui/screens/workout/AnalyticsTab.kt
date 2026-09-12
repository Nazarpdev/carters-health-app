package com.carters.health.ui.screens.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.carters.health.data.model.Equipment
import com.carters.health.data.model.Exercise
import com.carters.health.data.model.TrainingTotals
import com.carters.health.data.model.Units
import com.carters.health.data.model.WeightUnit
import com.carters.health.data.model.WorkoutHistoryEntry
import com.carters.health.data.model.format0
import com.carters.health.data.repo.HealthRepository
import com.carters.health.ui.components.ChartSeries
import com.carters.health.ui.components.Eyebrow
import com.carters.health.ui.components.SoftCard
import com.carters.health.ui.components.Pill
import com.carters.health.ui.components.ScrubbableLineChart
import com.carters.health.ui.components.SectionHeader
import com.carters.health.ui.components.StatTile
import com.carters.health.ui.theme.HealthColors
import com.carters.health.ui.theme.LocalHealthHaptics
import java.time.format.DateTimeFormatter

@Composable
fun AnalyticsTab(repository: HealthRepository, unit: WeightUnit, modifier: Modifier = Modifier) {
    val totals by repository.trainingTotals.collectAsState(initial = TrainingTotals(0.0, 0, 0))
    val exercises by repository.exercises.collectAsState()
    val prs by repository.personalRecords.collectAsState()
    val history by repository.workoutHistory.collectAsState()
    val haptics = LocalHealthHaptics.current

    val trackable = remember(exercises) { exercises.filter { it.equipment == Equipment.BARBELL || it.equipment == Equipment.DUMBBELL } }
    var selected by remember(trackable) { mutableStateOf(trackable.firstOrNull()) }
    val curve = remember(selected) { selected?.let { repository.overloadCurve(it) } ?: emptyList() }
    val dateFmt = remember { DateTimeFormatter.ofPattern("MMM d") }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("Volume", compactVolume(Units.displayWeight(totals.totalVolumeLbs, unit)), unit = unit.label, accent = HealthColors.Ochre, tinted = true, compact = true, modifier = Modifier.weight(1f))
                StatTile("Workouts", totals.totalWorkouts.toString(), accent = HealthColors.Green, compact = true, modifier = Modifier.weight(1f))
                StatTile("Sets", totals.totalSets.toDouble().format0(), accent = HealthColors.Terracotta, compact = true, modifier = Modifier.weight(1f))
            }
        }
        item {
            SectionHeader("Overload curve", subtitle = "Drag across the chart to inspect any session")
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(trackable, key = { it.id }) { ex ->
                    val on = ex == selected
                    Pill(ex.name, color = if (on) HealthColors.Green else HealthColors.Muted, filled = on) { haptics.tick(); selected = ex }
                }
            }
        }
        item {
            OverloadChartCard(selected, curve, unit, dateFmt)
        }
        item {
            val pr = prs.firstOrNull { it.exerciseName == selected?.name }
            val maxW = pr?.maxWeightLbs ?: curve.maxOfOrNull { it.heaviestSetLbs } ?: 0.0
            val max1rm = pr?.maxEstimatedOneRepMax ?: curve.maxOfOrNull { it.estimatedOneRepMax } ?: 0.0
            val maxVol = pr?.maxVolumeSetLbs ?: (maxW * 5)
            Column {
                SectionHeader("All-time PRs", subtitle = selected?.name ?: "")
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PrCard("Max weight", formatWeight(maxW, unit), unit.label, HealthColors.Ochre, Modifier.weight(1f))
                    PrCard("Max est. 1RM", formatWeight(max1rm, unit), unit.label, HealthColors.Ochre, Modifier.weight(1f))
                    PrCard("Max volume set", Units.displayWeight(maxVol, unit).format0(), unit.label, HealthColors.Ochre, Modifier.weight(1f))
                }
            }
        }
        item { SectionHeader("Workout history", subtitle = "${history.size} recent sessions") }
        items(history, key = { it.id }) { entry -> HistoryCard(entry, unit) }
    }
}

private fun compactVolume(v: Double): String = when {
    v >= 1_000_000 -> String.format("%.2fM", v / 1_000_000)
    v >= 10_000 -> String.format("%.0fK", v / 1_000)
    else -> v.format0()
}

@Composable
private fun OverloadChartCard(exercise: Exercise?, curve: List<com.carters.health.data.model.OverloadPoint>, unit: WeightUnit, fmt: DateTimeFormatter) {
    SoftCard(accent = HealthColors.Green, contentPadding = PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Eyebrow(exercise?.name ?: "Select an exercise", HealthColors.Green)
                if (curve.isNotEmpty()) {
                    val first = curve.first().estimatedOneRepMax
                    val last = curve.last().estimatedOneRepMax
                    Text(
                        "+${formatWeight(last - first, unit)} ${unit.label} est. 1RM over ${curve.size} sessions",
                        style = MaterialTheme.typography.bodySmall, color = HealthColors.Muted,
                    )
                }
            }
            LegendDot(HealthColors.Green, "Est. 1RM")
            Spacer(Modifier.width(10.dp))
            LegendDot(HealthColors.Sky, "Heaviest")
        }
        Spacer(Modifier.height(8.dp))
        ScrubbableLineChart(
            series = listOf(
                ChartSeries(curve.map { Units.displayWeight(it.estimatedOneRepMax, unit).toFloat() }, HealthColors.Green, label = "Est. 1RM"),
                ChartSeries(curve.map { Units.displayWeight(it.heaviestSetLbs, unit).toFloat() }, HealthColors.Sky, fill = false, dashed = true, label = "Heaviest"),
            ),
            modifier = Modifier.fillMaxWidth().height(230.dp),
            xLabel = { i -> curve.getOrNull(i)?.date?.format(fmt) ?: "" },
            xLabelCount = 4,
            formatValue = { String.format("%.0f", it) },
            scrubLabel = { i -> curve.getOrNull(i)?.date?.format(DateTimeFormatter.ofPattern("EEE, MMM d")) ?: "" },
        )
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = HealthColors.Muted)
    }
}

@Composable
private fun PrCard(label: String, value: String, unit: String, accent: Color, modifier: Modifier = Modifier) {
    SoftCard(modifier = modifier, accent = accent, contentPadding = PaddingValues(12.dp)) {
        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.headlineSmall, color = HealthColors.Ink)
        Text(unit, style = MaterialTheme.typography.labelSmall, color = HealthColors.Muted)
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = HealthColors.InkSoft)
    }
}

@Composable
private fun HistoryCard(entry: WorkoutHistoryEntry, unit: WeightUnit) {
    SoftCard(accent = HealthColors.Muted, contentPadding = PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.History, contentDescription = null, tint = HealthColors.Muted, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(entry.name, style = MaterialTheme.typography.titleSmall, color = HealthColors.Ink)
                Text("${entry.date.format(DateTimeFormatter.ofPattern("EEE, MMM d"))} · ${entry.durationMinutes} min", style = MaterialTheme.typography.bodySmall, color = HealthColors.Muted)
            }
            if (entry.prCount > 0) Pill("${entry.prCount} PR", color = HealthColors.Ochre, icon = Icons.Default.EmojiEvents)
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            MiniStat("Sets", entry.totalSets.toString())
            MiniStat("Reps", entry.totalReps.toString())
            MiniStat("Volume", "${Units.displayWeight(entry.totalVolumeLbs, unit).format0()} ${unit.label}")
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = HealthColors.Faint, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(6.dp))
            Text(entry.exerciseNames.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = HealthColors.Faint, maxLines = 1)
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String) {
    Column {
        Eyebrow(label)
        Text(value, style = MaterialTheme.typography.titleSmall, color = HealthColors.InkSoft)
    }
}
