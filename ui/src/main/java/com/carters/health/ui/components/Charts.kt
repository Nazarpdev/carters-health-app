package com.carters.health.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carters.health.ui.theme.HealthColors
import com.carters.health.ui.theme.HealthTheme
import kotlin.math.roundToInt
import kotlin.math.sin

/** Builds a smooth Catmull-Rom → cubic Bézier path through the given points. */
fun smoothPath(points: List<Offset>, tension: Float = 1f): Path {
    val path = Path()
    if (points.isEmpty()) return path
    path.moveTo(points[0].x, points[0].y)
    if (points.size == 1) return path
    for (i in 0 until points.size - 1) {
        val p0 = points[(i - 1).coerceAtLeast(0)]
        val p1 = points[i]
        val p2 = points[i + 1]
        val p3 = points[(i + 2).coerceAtMost(points.size - 1)]
        val c1 = Offset(p1.x + (p2.x - p0.x) / 6f * tension, p1.y + (p2.y - p0.y) / 6f * tension)
        val c2 = Offset(p2.x - (p3.x - p1.x) / 6f * tension, p2.y - (p3.y - p1.y) / 6f * tension)
        path.cubicTo(c1.x, c1.y, c2.x, c2.y, p2.x, p2.y)
    }
    return path
}

/** A series to plot; values are evenly spaced along the x axis. */
data class ChartSeries(
    val values: List<Float>,
    val color: Color,
    val gradient: List<Color> = listOf(color, color),
    val fill: Boolean = true,
    val dashed: Boolean = false,
    val label: String = "",
)

/** Tiny fill-under-curve sparkline (live HR strip, tile decorations). */
@Composable
fun Sparkline(
    values: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = HealthColors.Coral,
    strokeWidth: Dp = 2.dp,
    fill: Boolean = true,
    reveal: Boolean = true,
    showEndDot: Boolean = true,
) {
    val progress by rememberRevealedProgress(1f, "spark")
    Canvas(modifier) {
        if (values.size < 2) return@Canvas
        val min = values.min()
        val max = values.max()
        val span = (max - min).takeIf { it > 0f } ?: 1f
        val pad = strokeWidth.toPx() * 2
        val pts = values.mapIndexed { i, v ->
            Offset(i / (values.size - 1f) * size.width, pad + (1f - (v - min) / span) * (size.height - pad * 2))
        }
        val path = smoothPath(pts)
        val visible = if (reveal) progress else 1f
        val drawn = partialPath(path, visible)
        if (fill) {
            val fillPath = Path().apply {
                addPath(drawn)
                lineTo(pts.last().x * visible, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(fillPath, Brush.verticalGradient(listOf(color.copy(alpha = 0.35f), Color.Transparent)))
        }
        drawPath(drawn, color, style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round))
        if (showEndDot && visible >= 0.999f) {
            drawCircle(color.copy(alpha = 0.3f), strokeWidth.toPx() * 3.5f, pts.last())
            drawCircle(color, strokeWidth.toPx() * 1.6f, pts.last())
        }
    }
}

private fun partialPath(path: Path, fraction: Float): Path {
    if (fraction >= 0.999f) return path
    val measure = PathMeasure()
    measure.setPath(path, false)
    val out = Path()
    measure.getSegment(0f, measure.length * fraction.coerceIn(0f, 1f), out, true)
    return out
}

/**
 * Hero trend chart: multi-series Bézier curves, optional baseline reference, x-axis labels and
 * touch scrubbing with a floating readout. Drag anywhere on the chart to inspect values.
 */
@Composable
fun ScrubbableLineChart(
    series: List<ChartSeries>,
    modifier: Modifier = Modifier,
    xLabel: (index: Int) -> String = { "" },
    xLabelCount: Int = 5,
    baseline: Float? = null,
    baselineLabel: String = "30D baseline",
    formatValue: (Float) -> String = { String.format("%.1f", it) },
    scrubLabel: (index: Int) -> String = { "" },
    minValue: Float? = null,
    maxValue: Float? = null,
    gridLines: Int = 3,
    strokeWidth: Dp = 2.5.dp,
    onScrub: ((index: Int?) -> Unit)? = null,
) {
    val measurer = rememberTextMeasurer()
    val haptics = LocalHapticFeedback.current
    var scrubX by remember { mutableStateOf<Float?>(null) }
    var lastIndex by remember { mutableStateOf(-1) }
    val reveal by rememberRevealedProgress(1f, "chartReveal")
    val labelStyle = TextStyle(color = HealthColors.ClayDim, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    val readoutStyle = TextStyle(color = HealthColors.OnSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    val readoutSub = TextStyle(color = HealthColors.Clay, fontSize = 10.sp, fontWeight = FontWeight.Medium)

    val count = series.maxOfOrNull { it.values.size } ?: 0
    Canvas(
        modifier
            .pointerInput(count) {
                detectDragGestures(
                    onDragStart = { scrubX = it.x },
                    onDrag = { change, _ -> change.consume(); scrubX = change.position.x },
                    onDragEnd = { scrubX = null; lastIndex = -1; onScrub?.invoke(null) },
                    onDragCancel = { scrubX = null; lastIndex = -1; onScrub?.invoke(null) },
                )
            }
            .pointerInput(count) {
                detectTapGestures(onPress = { scrubX = it.x; tryAwaitRelease(); scrubX = null; lastIndex = -1; onScrub?.invoke(null) })
            },
    ) {
        if (count < 2) return@Canvas
        val leftPad = 8.dp.toPx()
        val rightPad = 8.dp.toPx()
        val topPad = 34.dp.toPx()
        val bottomPad = 22.dp.toPx()
        val plotW = size.width - leftPad - rightPad
        val plotH = size.height - topPad - bottomPad

        val all = series.flatMap { it.values } + listOfNotNull(baseline)
        val lo = minValue ?: (all.min() - (all.max() - all.min()) * 0.15f)
        val hi = maxValue ?: (all.max() + (all.max() - all.min()) * 0.15f)
        val span = (hi - lo).takeIf { it > 0f } ?: 1f
        fun yOf(v: Float) = topPad + (1f - (v - lo) / span) * plotH
        fun xOf(i: Int, n: Int) = leftPad + i / (n - 1f) * plotW

        // grid
        for (g in 0..gridLines) {
            val y = topPad + plotH * g / gridLines
            drawLine(HealthColors.Border.copy(alpha = 0.6f), Offset(leftPad, y), Offset(size.width - rightPad, y), 1f)
            val v = hi - span * g / gridLines
            drawText(measurer, formatValue(v), Offset(leftPad, y - 12.sp.toPx()), labelStyle)
        }
        // x labels
        for (k in 0 until xLabelCount) {
            val idx = (k / (xLabelCount - 1f) * (count - 1)).roundToInt()
            val text = xLabel(idx)
            if (text.isEmpty()) continue
            val layout = measurer.measure(text, labelStyle)
            val x = (xOf(idx, count) - layout.size.width / 2f).coerceIn(0f, size.width - layout.size.width)
            drawText(layout, topLeft = Offset(x, size.height - bottomPad + 6.dp.toPx()))
        }
        // baseline
        if (baseline != null) {
            val y = yOf(baseline)
            drawLine(
                HealthColors.Sand.copy(alpha = 0.5f), Offset(leftPad, y), Offset(size.width - rightPad, y), 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
            )
            val layout = measurer.measure(baselineLabel, labelStyle.copy(color = HealthColors.Sand))
            drawText(layout, topLeft = Offset(size.width - rightPad - layout.size.width, y - layout.size.height - 2.dp.toPx()))
        }
        // series
        series.forEach { s ->
            val n = s.values.size
            if (n < 2) return@forEach
            val pts = s.values.mapIndexed { i, v -> Offset(xOf(i, n), yOf(v)) }
            val full = smoothPath(pts)
            val path = partialPath(full, reveal)
            if (s.fill && !s.dashed) {
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(leftPad + plotW * reveal, topPad + plotH)
                    lineTo(leftPad, topPad + plotH)
                    close()
                }
                drawPath(fillPath, Brush.verticalGradient(listOf(s.color.copy(alpha = 0.28f), Color.Transparent), startY = topPad, endY = topPad + plotH))
            }
            // glow pass
            drawPath(path, s.color.copy(alpha = 0.25f), style = Stroke(strokeWidth.toPx() * 3f, cap = StrokeCap.Round))
            drawPath(
                path,
                brush = Brush.horizontalGradient(s.gradient),
                style = Stroke(
                    strokeWidth.toPx(), cap = StrokeCap.Round,
                    pathEffect = if (s.dashed) PathEffect.dashPathEffect(floatArrayOf(10f, 8f)) else null,
                ),
            )
        }
        // scrub readout
        val sx = scrubX
        if (sx != null) {
            val idx = (((sx - leftPad) / plotW) * (count - 1)).roundToInt().coerceIn(0, count - 1)
            if (idx != lastIndex) {
                lastIndex = idx
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onScrub?.invoke(idx)
            }
            val x = xOf(idx, count)
            drawLine(HealthColors.Sand.copy(alpha = 0.6f), Offset(x, topPad - 6.dp.toPx()), Offset(x, topPad + plotH), 1.5f)
            val lines = series.mapNotNull { s -> s.values.getOrNull(idx)?.let { v -> Triple(s, v, yOf(v)) } }
            lines.forEach { (s, _, y) ->
                drawCircle(s.color.copy(alpha = 0.35f), 12.dp.toPx() / 2, Offset(x, y))
                drawCircle(s.color, 4.dp.toPx(), Offset(x, y))
                drawCircle(Color.White, 1.5.dp.toPx(), Offset(x, y))
            }
            val primary = lines.firstOrNull() ?: return@Canvas
            val valueText = buildString {
                append(formatValue(primary.second))
                if (lines.size > 1) append("  ·  ").append(formatValue(lines[1].second))
            }
            val sub = scrubLabel(idx)
            val vLayout = measurer.measure(valueText, readoutStyle)
            val sLayout = measurer.measure(sub, readoutSub)
            val w = maxOf(vLayout.size.width, sLayout.size.width) + 24.dp.toPx()
            val h = vLayout.size.height + (if (sub.isEmpty()) 0 else sLayout.size.height) + 14.dp.toPx()
            val bx = (x - w / 2).coerceIn(0f, size.width - w)
            drawRoundRect(HealthColors.SurfaceHigh, Offset(bx, 0f), Size(w, h), CornerRadius(12.dp.toPx()))
            drawRoundRect(primary.first.color.copy(alpha = 0.6f), Offset(bx, 0f), Size(w, h), CornerRadius(12.dp.toPx()), style = Stroke(1.5f))
            drawText(vLayout, topLeft = Offset(bx + 12.dp.toPx(), 7.dp.toPx()))
            if (sub.isNotEmpty()) drawText(sLayout, topLeft = Offset(bx + 12.dp.toPx(), 7.dp.toPx() + vLayout.size.height))
        }
    }
}

/** Horizontal stacked distribution bar (sleep stages, stress zones). */
@Composable
fun DistributionBar(
    parts: List<Pair<Float, Color>>,
    modifier: Modifier = Modifier,
    height: Dp = 14.dp,
) {
    val reveal by rememberRevealedProgress(1f, "dist")
    Canvas(modifier.fillMaxWidth().height(height)) {
        val total = parts.sumOf { it.first.toDouble() }.toFloat().takeIf { it > 0 } ?: 1f
        var x = 0f
        val gap = 3.dp.toPx()
        val usable = size.width - gap * (parts.size - 1)
        parts.forEach { (v, c) ->
            val w = usable * (v / total) * reveal
            if (w > 0) drawRoundRect(c, Offset(x, 0f), Size(w, size.height), CornerRadius(size.height / 2))
            x += w + gap
        }
    }
}

/** Compact vertical bar strip (24h stress / SpO2 distribution). */
@Composable
fun BarStrip(
    values: List<Float>,
    modifier: Modifier = Modifier,
    colorFor: (Float) -> Color,
    maxValue: Float = 100f,
    barGap: Dp = 2.dp,
) {
    val reveal by rememberRevealedProgress(1f, "bars")
    Canvas(modifier) {
        if (values.isEmpty()) return@Canvas
        val gap = barGap.toPx()
        val w = (size.width - gap * (values.size - 1)) / values.size
        values.forEachIndexed { i, v ->
            val h = (v / maxValue).coerceIn(0.04f, 1f) * size.height * reveal
            val x = i * (w + gap)
            drawRoundRect(colorFor(v), Offset(x, size.height - h), Size(w, h), CornerRadius(w / 2))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun ChartPreview() {
    HealthTheme {
        val values = (0 until 30).map { 170f - it * 0.1f + (2 * sin(it / 3f)) }
        Column(Modifier.padding(16.dp)) {
            ScrubbableLineChart(
                series = listOf(ChartSeries(values, HealthColors.Amber, listOf(HealthColors.Amber, HealthColors.Gold))),
                modifier = Modifier.fillMaxWidth().height(220.dp),
                baseline = 169.2f,
                xLabel = { "D$it" },
            )
            Sparkline(values, Modifier.fillMaxWidth().height(48.dp))
        }
    }
}
