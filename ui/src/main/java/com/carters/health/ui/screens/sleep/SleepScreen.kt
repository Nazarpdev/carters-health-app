package com.carters.health.ui.screens.sleep

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carters.health.data.model.SleepNight
import com.carters.health.data.model.SleepStage
import com.carters.health.data.repo.HealthRepository
import com.carters.health.data.repo.InMemoryHealthRepository
import com.carters.health.ui.components.DistributionBar
import com.carters.health.ui.components.Eyebrow
import com.carters.health.ui.components.SoftCard
import com.carters.health.ui.components.Pill
import com.carters.health.ui.components.RadialGauge
import com.carters.health.ui.components.SectionHeader
import com.carters.health.ui.components.rememberRevealedProgress
import com.carters.health.ui.theme.HealthColors
import com.carters.health.ui.theme.HealthTheme
import com.carters.health.ui.theme.LocalHealthHaptics
import com.carters.health.ui.theme.SansFamily
import com.carters.health.ui.theme.stageColor
import java.time.Duration
import java.time.format.DateTimeFormatter

private fun hm(minutes: Long): String = "${minutes / 60}h ${minutes % 60}m"

@Composable
fun SleepScreen(repository: HealthRepository, modifier: Modifier = Modifier) {
    val nights by repository.sleepNights.collectAsState()
    var index by rememberSaveable { mutableStateOf(0) } // 0 = most recent
    val night = nights.getOrNull(index)
    val haptics = LocalHealthHaptics.current
    val dateFmt = remember { DateTimeFormatter.ofPattern("EEEE, MMM d") }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(HealthColors.Canvas),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column {
                Text("Sleep", style = MaterialTheme.typography.headlineMedium, color = HealthColors.Ink)
                Text("Rest & recharge tonight — deep sleep builds tomorrow's readiness.", style = MaterialTheme.typography.bodySmall, color = HealthColors.Muted)
            }
        }
        item {
            DateNavigator(
                label = night?.date?.format(dateFmt) ?: "No data",
                sub = if (night != null) "${night.sourceRecordCount} records merged · Sleep as Android" else "",
                canPrev = index < nights.lastIndex,
                canNext = index > 0,
                onPrev = { haptics.tick(); index++ },
                onNext = { haptics.tick(); index-- },
            )
        }
        if (night != null) {
            item {
                AnimatedContent(
                    targetState = night,
                    transitionSpec = { (fadeIn() + slideInHorizontally { it / 8 }) togetherWith (fadeOut() + slideOutHorizontally { -it / 8 }) },
                    label = "night",
                ) { n ->
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        SleepScoreCard(n)
                        HypnogramCard(n)
                        StageBreakdown(n)
                    }
                }
            }
        }
        item { SectionHeader("Recent nights", subtitle = "Tap to revisit") }
        items(nights.size, key = { nights[it].date.toString() }) { i ->
            NightRow(nights[i], selected = i == index) { haptics.tick(); index = i }
        }
    }
}

@Composable
private fun DateNavigator(label: String, sub: String, canPrev: Boolean, canNext: Boolean, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        NavChevron(Icons.Default.ChevronLeft, canPrev, onPrev)
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.titleMedium, color = HealthColors.Ink)
            Text(sub, style = MaterialTheme.typography.labelSmall, color = HealthColors.Muted)
        }
        NavChevron(Icons.Default.ChevronRight, canNext, onNext)
    }
}

@Composable
private fun NavChevron(icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (enabled) HealthColors.GreenSoft else HealthColors.CardAlt)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = if (enabled) HealthColors.GreenDeep else HealthColors.Faint)
    }
}

@Composable
private fun SleepScoreCard(night: SleepNight) {
    val timeFmt = DateTimeFormatter.ofPattern("h:mm a")
    SoftCard(accent = HealthColors.Sage, contentPadding = PaddingValues(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadialGauge(progress = night.score / 100f, modifier = Modifier.size(150.dp), colors = listOf(HealthColors.GreenDeep, HealthColors.Sage), strokeWidth = 12.dp) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${night.score}", style = MaterialTheme.typography.displaySmall, color = HealthColors.Ink, fontWeight = FontWeight.Bold)
                    Eyebrow("Sleep score", HealthColors.Sage)
                }
            }
            Spacer(Modifier.width(18.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column {
                    Eyebrow("Total rest")
                    Text(hm(night.totalMinutes), style = MaterialTheme.typography.headlineSmall, color = HealthColors.Ink)
                }
                Column {
                    Eyebrow("Sleep debt")
                    Text(
                        if (night.sleepDebtMinutes == 0) "Cleared" else "-${hm(night.sleepDebtMinutes.toLong())}",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (night.sleepDebtMinutes == 0) HealthColors.Green else HealthColors.Terracotta,
                    )
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pill("Bed ${night.start.format(timeFmt)}", color = HealthColors.Sage, icon = Icons.Default.Bedtime)
            Pill("Wake ${night.end.format(timeFmt)}", color = HealthColors.Green, icon = Icons.Default.WbSunny)
        }
    }
}

/** Stage-stepped hypnogram: awake on top, deep at the bottom, drawn as lit rounded bands. */
@Composable
private fun HypnogramCard(night: SleepNight) {
    val measurer = rememberTextMeasurer()
    val reveal by rememberRevealedProgress(1f, "hypno")
    val labelStyle = TextStyle(fontFamily = SansFamily, color = HealthColors.Muted, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    val timeFmt = DateTimeFormatter.ofPattern("h a")
    SoftCard(accent = HealthColors.GreenDeep, contentPadding = PaddingValues(16.dp)) {
        Eyebrow("Hypnogram", HealthColors.Sage)
        Text("${night.segments.size} stage transitions · ${hm(night.timeInBedMinutes)} in bed", style = MaterialTheme.typography.bodySmall, color = HealthColors.Muted)
        Spacer(Modifier.height(10.dp))
        Canvas(Modifier.fillMaxWidth().height(170.dp)) {
            val leftPad = 44.dp.toPx()
            val bottomPad = 18.dp.toPx()
            val plotW = size.width - leftPad
            val plotH = size.height - bottomPad
            val lanes = listOf(SleepStage.AWAKE, SleepStage.REM, SleepStage.LIGHT, SleepStage.DEEP)
            val laneH = plotH / lanes.size
            lanes.forEachIndexed { i, s ->
                val y = i * laneH
                drawLine(HealthColors.Hairline, Offset(leftPad, y + laneH), Offset(size.width, y + laneH), 1f)
                drawText(measurer, s.label, Offset(0f, y + laneH / 2 - 7.sp.toPx()), labelStyle)
            }
            val total = night.timeInBedMinutes.toFloat().coerceAtLeast(1f)
            var prevEnd: Offset? = null
            night.segments.forEach { seg ->
                val startMin = Duration.between(night.start, seg.start).toMinutes()
                val x0 = leftPad + startMin / total * plotW
                val w = (seg.minutes / total * plotW) * reveal
                val lane = lanes.indexOf(seg.stage)
                val y = lane * laneH + laneH * 0.2f
                val h = laneH * 0.6f
                val c = stageColor(seg.stage)
                val r = 4.dp.toPx()
                drawRoundRect(c, Offset(x0, y), Size(w, h), CornerRadius(r))
                val mid = Offset(x0, y + h / 2)
                if (prevEnd != null) drawLine(HealthColors.Hairline, prevEnd!!, mid, 1.5f)
                prevEnd = Offset(x0 + w, y + h / 2)
            }
            // time axis
            val hours = (night.timeInBedMinutes / 60).toInt().coerceAtLeast(1)
            val step = if (hours > 6) 2 else 1
            var t = night.start.withMinute(0).plusHours(1)
            while (t.isBefore(night.end)) {
                val m = Duration.between(night.start, t).toMinutes()
                val x = leftPad + m / total * plotW
                if (t.hour % step == 0) {
                    val layout = measurer.measure(t.format(timeFmt), labelStyle)
                    drawText(layout, topLeft = Offset(x - layout.size.width / 2, plotH + 4.dp.toPx()))
                }
                t = t.plusHours(1)
            }
        }
    }
}

@Composable
private fun StageBreakdown(night: SleepNight) {
    val asleep = night.timeInBedMinutes.toFloat().coerceAtLeast(1f)
    val order = listOf(SleepStage.DEEP, SleepStage.REM, SleepStage.LIGHT, SleepStage.AWAKE)
    SoftCard(accent = HealthColors.StageDeep, contentPadding = PaddingValues(16.dp)) {
        Eyebrow("Stage distribution")
        Spacer(Modifier.height(10.dp))
        DistributionBar(order.map { night.minutesIn(it).toFloat() to stageColor(it) })
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            order.forEach { s ->
                val m = night.minutesIn(s)
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(stageColor(s)))
                        Spacer(Modifier.width(5.dp))
                        Text(s.label, style = MaterialTheme.typography.labelSmall, color = HealthColors.Muted)
                    }
                    Text(hm(m), style = MaterialTheme.typography.titleSmall, color = HealthColors.Ink)
                    Text("${(m / asleep * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = HealthColors.Muted)
                }
            }
        }
    }
}

@Composable
private fun NightRow(night: SleepNight, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    val accent = when {
        night.score >= 80 -> HealthColors.Green
        night.score >= 60 -> HealthColors.Sage
        else -> HealthColors.Terracotta
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) HealthColors.GreenSoft else HealthColors.Card)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(42.dp).clip(CircleShape).background(HealthColors.tint(accent)),
            contentAlignment = Alignment.Center,
        ) { Text("${night.score}", style = MaterialTheme.typography.titleSmall, color = accent) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(night.date.format(DateTimeFormatter.ofPattern("EEE, MMM d")), style = MaterialTheme.typography.titleSmall, color = HealthColors.Ink)
            Spacer(Modifier.height(6.dp))
            DistributionBar(
                listOf(SleepStage.DEEP, SleepStage.REM, SleepStage.LIGHT, SleepStage.AWAKE).map { night.minutesIn(it).toFloat() to stageColor(it) },
                height = 6.dp,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(hm(night.totalMinutes), style = MaterialTheme.typography.titleSmall, color = HealthColors.InkSoft)
            Pill("${night.sourceRecordCount} rec", color = HealthColors.Faint)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F1EA, heightDp = 1400)
@Composable
private fun SleepPreview() {
    HealthTheme { SleepScreen(InMemoryHealthRepository()) }
}
