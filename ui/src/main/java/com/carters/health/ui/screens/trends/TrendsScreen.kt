package com.carters.health.ui.screens.trends

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Psychology
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.carters.health.data.model.HeartRateSample
import com.carters.health.data.repo.HealthRepository
import com.carters.health.data.repo.InMemoryHealthRepository
import com.carters.health.data.repo.SampleData
import com.carters.health.ui.components.BarStrip
import com.carters.health.ui.components.ChartSeries
import com.carters.health.ui.components.DeltaBadge
import com.carters.health.ui.components.DistributionBar
import com.carters.health.ui.components.Eyebrow
import com.carters.health.ui.components.GlowCard
import com.carters.health.ui.components.Pill
import com.carters.health.ui.components.ScrubbableLineChart
import com.carters.health.ui.components.SectionHeader
import com.carters.health.ui.components.Sparkline
import com.carters.health.ui.theme.HealthColors
import com.carters.health.ui.theme.HealthTheme
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TrendsScreen(repository: HealthRepository, modifier: Modifier = Modifier) {
    val hrDay by repository.heartRateDay.collectAsState()
    val spo2 by repository.spo2Day.collectAsState()
    val stress by repository.stressDay.collectAsState()
    val rhr by repository.restingHr.collectAsState()
    val hrv by repository.hrv.collectAsState()
    val zone = remember { ZoneId.systemDefault() }
    val hourFmt = remember { DateTimeFormatter.ofPattern("h a").withZone(zone) }
    val fullFmt = remember { DateTimeFormatter.ofPattern("h:mm a").withZone(zone) }
    var scrubbed by remember { mutableStateOf<HeartRateSample?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(HealthColors.Obsidian),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column {
                Text("Trends", style = MaterialTheme.typography.headlineMedium, color = HealthColors.OnSurface)
                Text("Continuous biometrics from your watch, last 24 hours.", style = MaterialTheme.typography.bodySmall, color = HealthColors.Clay)
            }
        }
        item {
            GlowCard(accent = HealthColors.Coral, contentPadding = PaddingValues(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MonitorHeart, contentDescription = null, tint = HealthColors.Coral, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Eyebrow("Heart rate · 24h", HealthColors.Coral)
                    Spacer(Modifier.weight(1f))
                    val s = scrubbed
                    if (s != null) {
                        Text("${s.bpm} bpm · ${fullFmt.format(s.time)}", style = MaterialTheme.typography.labelMedium, color = HealthColors.OnSurface)
                    } else {
                        Text("min ${hrDay.minOfOrNull { it.bpm } ?: 0} · max ${hrDay.maxOfOrNull { it.bpm } ?: 0}", style = MaterialTheme.typography.labelMedium, color = HealthColors.Clay)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("${hrDay.lastOrNull()?.bpm ?: 0}", style = MaterialTheme.typography.displaySmall, color = HealthColors.OnSurface, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(4.dp))
                    Text("bpm now", style = MaterialTheme.typography.labelLarge, color = HealthColors.Clay, modifier = Modifier.padding(bottom = 6.dp))
                    Spacer(Modifier.weight(1f))
                    Text("Drag to scrub", style = MaterialTheme.typography.labelSmall, color = HealthColors.ClayDim)
                }
                ScrubbableLineChart(
                    series = listOf(ChartSeries(hrDay.map { it.bpm.toFloat() }, HealthColors.Coral, listOf(HealthColors.Rose, HealthColors.Coral, HealthColors.Amber))),
                    modifier = Modifier.fillMaxWidth().height(240.dp),
                    xLabel = { i -> hrDay.getOrNull(i)?.let { hourFmt.format(it.time) } ?: "" },
                    xLabelCount = 5,
                    formatValue = { String.format("%.0f", it) },
                    scrubLabel = { i -> hrDay.getOrNull(i)?.let { fullFmt.format(it.time) } ?: "" },
                    minValue = 40f,
                    strokeWidth = 2.dp,
                    onScrub = { i -> scrubbed = i?.let { hrDay.getOrNull(it) } },
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BaselineTile("Resting HR", rhr.current, "bpm", rhr.sevenDayAverage, true, HealthColors.Coral, hrDay.filter { it.bpm < 70 }.takeLast(40).map { it.bpm.toFloat() }, Modifier.weight(1f))
                BaselineTile("HRV", hrv.current, "ms", hrv.sevenDayAverage, false, HealthColors.Mint, listOf(52f, 55f, 51f, 58f, 60f, 57f, 62f), Modifier.weight(1f))
            }
        }
        item { SectionHeader("Blood oxygen", subtitle = "Spot checks + overnight SpO₂") }
        item {
            val zones = remember(spo2) {
                listOf(
                    spo2.count { it.percent >= 98 }.toFloat() to HealthColors.Mint,
                    spo2.count { it.percent in 95..97 }.toFloat() to HealthColors.Emerald,
                    spo2.count { it.percent in 90..94 }.toFloat() to HealthColors.Amber,
                    spo2.count { it.percent < 90 }.toFloat() to HealthColors.Coral,
                )
            }
            GlowCard(accent = HealthColors.Mint, glow = false, contentPadding = PaddingValues(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Bloodtype, contentDescription = null, tint = HealthColors.Mint, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Eyebrow("SpO₂ range", HealthColors.Mint)
                    Spacer(Modifier.weight(1f))
                    Text("avg ${String.format("%.1f", spo2.map { it.percent }.average())}%", style = MaterialTheme.typography.labelMedium, color = HealthColors.Sand)
                }
                Spacer(Modifier.height(10.dp))
                BarStrip(
                    spo2.map { it.percent.toFloat() - 88f }, Modifier.fillMaxWidth().height(70.dp),
                    colorFor = { v -> if (v + 88 >= 98) HealthColors.Mint else if (v + 88 >= 95) HealthColors.Emerald else HealthColors.Amber },
                    maxValue = 12f,
                )
                Spacer(Modifier.height(10.dp))
                DistributionBar(zones, height = 8.dp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Pill("98–100 optimal", color = HealthColors.Mint)
                    Pill("95–97 normal", color = HealthColors.Emerald)
                    Pill("<95 low", color = HealthColors.Amber)
                }
            }
        }
        item { SectionHeader("Stress", subtitle = "Zepp stress index across the day") }
        item {
            val z = remember(stress) { SampleData.stressZones(stress) }
            GlowCard(accent = HealthColors.Lavender, glow = false, contentPadding = PaddingValues(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = HealthColors.Lavender, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Eyebrow("Stress zones", HealthColors.Lavender)
                    Spacer(Modifier.weight(1f))
                    Text("now ${stress.lastOrNull()?.level ?: 0}", style = MaterialTheme.typography.labelMedium, color = HealthColors.Sand)
                }
                Spacer(Modifier.height(10.dp))
                BarStrip(
                    stress.map { it.level.toFloat() }, Modifier.fillMaxWidth().height(80.dp),
                    colorFor = { v -> stressColor(v) },
                    maxValue = 100f,
                )
                Spacer(Modifier.height(12.dp))
                DistributionBar(
                    listOf(z.relaxed.toFloat() to HealthColors.Mint, z.normal.toFloat() to HealthColors.Emerald, z.medium.toFloat() to HealthColors.Amber, z.high.toFloat() to HealthColors.Coral),
                    height = 8.dp,
                )
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    ZoneStat("Relaxed", z.relaxed, HealthColors.Mint)
                    ZoneStat("Normal", z.normal, HealthColors.Emerald)
                    ZoneStat("Medium", z.medium, HealthColors.Amber)
                    ZoneStat("High", z.high, HealthColors.Coral)
                }
            }
        }
    }
}

private fun stressColor(v: Float): Color = when {
    v < 25 -> HealthColors.Mint
    v < 50 -> HealthColors.Emerald
    v < 75 -> HealthColors.Amber
    else -> HealthColors.Coral
}

@Composable
private fun ZoneStat(label: String, minutes: Int, color: Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(5.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = HealthColors.Clay)
        }
        Text("${minutes / 60}h ${minutes % 60}m", style = MaterialTheme.typography.titleSmall, color = HealthColors.OnSurface)
    }
}

@Composable
private fun BaselineTile(label: String, current: Int, unit: String, baseline: Double, lowerIsBetter: Boolean, accent: Color, spark: List<Float>, modifier: Modifier = Modifier) {
    GlowCard(modifier = modifier, accent = accent, glow = false, contentPadding = PaddingValues(14.dp)) {
        Eyebrow(label)
        Row(verticalAlignment = Alignment.Bottom) {
            Text("$current", style = MaterialTheme.typography.headlineMedium, color = HealthColors.OnSurface)
            Spacer(Modifier.width(4.dp))
            Text(unit, style = MaterialTheme.typography.labelMedium, color = HealthColors.Clay, modifier = Modifier.padding(bottom = 5.dp))
        }
        Spacer(Modifier.height(6.dp))
        DeltaBadge(current - baseline, unit, lowerIsBetter, suffix = " · 7D")
        Spacer(Modifier.height(8.dp))
        Sparkline(spark, Modifier.fillMaxWidth().height(36.dp), color = accent, showEndDot = false)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, heightDp = 1400)
@Composable
private fun TrendsPreview() {
    HealthTheme { TrendsScreen(InMemoryHealthRepository()) }
}
