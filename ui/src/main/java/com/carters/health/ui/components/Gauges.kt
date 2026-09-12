package com.carters.health.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.carters.health.ui.theme.HealthColors
import com.carters.health.ui.theme.HealthTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** One ring of a multi-ring dial. */
data class DialRing(
    val progress: Float,          // 0..1
    val colors: List<Color>,
    val label: String,
)

private const val ARC_SWEEP = 270f
private const val ARC_START = 135f

/**
 * Draws a 270° arc with a gradient sweep, a dim track underneath and a soft outer glow.
 * Progress is expected to already be animated by the caller.
 */
fun DrawScope.drawGlowArc(
    center: Offset,
    radius: Float,
    stroke: Float,
    progress: Float,
    colors: List<Color>,
    trackAlpha: Float = 0.12f,
    glow: Boolean = true,
) {
    val topLeft = Offset(center.x - radius, center.y - radius)
    val size = Size(radius * 2, radius * 2)
    // track
    drawArc(
        color = colors.last().copy(alpha = trackAlpha),
        startAngle = ARC_START,
        sweepAngle = ARC_SWEEP,
        useCenter = false,
        topLeft = topLeft,
        size = size,
        style = Stroke(stroke, cap = StrokeCap.Round),
    )
    val sweep = (ARC_SWEEP * progress.coerceIn(0f, 1f))
    if (sweep <= 0.5f) return
    val brush = Brush.sweepGradient(
        colorStops = arrayOf(0f to colors.first(), 0.75f to colors.last(), 1f to colors.first()),
        center = center,
    )
    rotate(ARC_START, pivot = center) {
        if (glow) {
            drawArc(
                brush = Brush.sweepGradient(
                    colorStops = arrayOf(0f to colors.first().copy(alpha = 0.0f), 0.75f to colors.last().copy(alpha = 0.35f), 1f to colors.first().copy(alpha = 0f)),
                    center = center,
                ),
                startAngle = 0f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = size,
                style = Stroke(stroke * 2.2f, cap = StrokeCap.Round),
            )
        }
        drawArc(
            brush = brush,
            startAngle = 0f,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = topLeft,
            size = size,
            style = Stroke(stroke, cap = StrokeCap.Round),
        )
        // bright tip
        val tipAngle = sweep * PI / 180
        val tip = Offset(center.x + radius * cos(tipAngle).toFloat(), center.y + radius * sin(tipAngle).toFloat())
        drawCircle(Color.White.copy(alpha = 0.85f), radius = stroke * 0.28f, center = tip)
    }
}

/**
 * Zepp-style triple-pillar dial: three concentric 270° arcs (readiness, strain, sleep) with a
 * free-form centre slot for the hero number.
 */
@Composable
fun ZeppArcDial(
    rings: List<DialRing>,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 14.dp,
    gap: Dp = 8.dp,
    center: @Composable () -> Unit,
) {
    val animated = rings.map { rememberRevealedProgress(it.progress, it.label) }
    Box(modifier = modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val step = stroke + gap.toPx()
            val c = Offset(size.width / 2, size.height / 2)
            var radius = (size.minDimension / 2) - stroke
            rings.forEachIndexed { i, ring ->
                drawGlowArc(c, radius, stroke, animated[i].value, ring.colors, glow = i == 0)
                radius -= step
            }
        }
        center()
    }
}

/** Single ring gauge — sleep score, readiness on detail screens. */
@Composable
fun RadialGauge(
    progress: Float,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(HealthColors.Emerald, HealthColors.Mint),
    strokeWidth: Dp = 12.dp,
    center: @Composable () -> Unit,
) {
    val animated by rememberRevealedProgress(progress, "gauge")
    Box(modifier = modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            drawGlowArc(Offset(size.width / 2, size.height / 2), size.minDimension / 2 - stroke, stroke, animated, colors)
        }
        center()
    }
}

/**
 * Full-circle ring split into segments (e.g. 10 × 1,000 steps), each filling as progress passes it.
 */
@Composable
fun SegmentedRing(
    progress: Float,
    modifier: Modifier = Modifier,
    segments: Int = 10,
    colors: List<Color> = listOf(HealthColors.Amber, HealthColors.Gold),
    strokeWidth: Dp = 10.dp,
    center: @Composable () -> Unit = {},
) {
    val animated by rememberRevealedProgress(progress, "segmented")
    Box(modifier = modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val radius = size.minDimension / 2 - stroke
            val c = Offset(size.width / 2, size.height / 2)
            val gapDeg = 6f
            val segDeg = 360f / segments - gapDeg
            val filled = animated.coerceIn(0f, 1f) * segments
            for (i in 0 until segments) {
                val start = -90f + i * (segDeg + gapDeg)
                val portion = (filled - i).coerceIn(0f, 1f)
                drawArc(
                    color = colors.last().copy(alpha = 0.14f),
                    startAngle = start, sweepAngle = segDeg, useCenter = false,
                    topLeft = Offset(c.x - radius, c.y - radius), size = Size(radius * 2, radius * 2),
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
                if (portion > 0f) {
                    drawArc(
                        brush = Brush.linearGradient(colors),
                        startAngle = start, sweepAngle = segDeg * portion, useCenter = false,
                        topLeft = Offset(c.x - radius, c.y - radius), size = Size(radius * 2, radius * 2),
                        style = Stroke(stroke, cap = StrokeCap.Round),
                    )
                }
            }
        }
        center()
    }
}

/**
 * Countdown disc for the rest timer: remaining fraction sweeps down clockwise with a glowing
 * gradient, and the whole ring gently breathes while running.
 */
@Composable
fun CountdownRing(
    fraction: Float,          // remaining 0..1
    running: Boolean,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(HealthColors.Mint, HealthColors.Emerald),
    strokeWidth: Dp = 10.dp,
    center: @Composable () -> Unit,
) {
    val breathe = rememberInfiniteTransition(label = "breathe")
    val pulse by breathe.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse",
    )
    Box(modifier = modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val radius = size.minDimension / 2 - stroke * 1.5f
            val c = Offset(size.width / 2, size.height / 2)
            val alpha = if (running) pulse else 0.6f
            drawCircle(colors.last().copy(alpha = 0.12f), radius, c, style = Stroke(stroke))
            if (running) {
                drawCircle(
                    brush = Brush.radialGradient(listOf(colors.first().copy(alpha = 0.18f * pulse), Color.Transparent), c, radius * 1.15f),
                    radius = radius * 1.15f, center = c,
                )
            }
            val sweep = 360f * fraction.coerceIn(0f, 1f)
            drawArc(
                brush = Brush.sweepGradient(colorStops = arrayOf(0f to colors.first(), 1f to colors.last()), center = c),
                startAngle = -90f, sweepAngle = sweep, useCenter = false,
                topLeft = Offset(c.x - radius, c.y - radius), size = Size(radius * 2, radius * 2),
                style = Stroke(stroke, cap = StrokeCap.Round), alpha = alpha,
            )
        }
        center()
    }
}

/** Pulsating beacon dot — watch connection status, live-stream indicator. */
@Composable
fun PulsingDot(color: Color = HealthColors.Mint, size: Dp = 10.dp, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "beacon")
    val ripple by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing)), label = "ripple",
    )
    Canvas(modifier.size(size * 2.6f)) {
        val c = Offset(this.size.width / 2, this.size.height / 2)
        val r = size.toPx() / 2
        drawCircle(color.copy(alpha = (1f - ripple) * 0.5f), radius = r + r * 1.6f * ripple, center = c)
        drawCircle(color, radius = r, center = c)
        drawCircle(Color.White.copy(alpha = 0.5f), radius = r * 0.4f, center = Offset(c.x - r * 0.25f, c.y - r * 0.25f))
    }
}

/** A heart that beats at the given BPM — scale keyframes shaped like a real systole/diastole. */
@Composable
fun BeatingHeart(bpm: Int, modifier: Modifier = Modifier, color: Color = HealthColors.Coral, size: Dp = 28.dp) {
    val period = (60_000 / bpm.coerceIn(30, 220))
    val transition = rememberInfiniteTransition(label = "heart")
    val scale by transition.animateFloat(
        initialValue = 1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = period
                1f at 0
                1.28f at (period * 0.12f).toInt()
                1.05f at (period * 0.24f).toInt()
                1.2f at (period * 0.34f).toInt()
                1f at (period * 0.55f).toInt()
            },
        ),
        label = "beat",
    )
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val s = scale
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(w / 2, h * 0.9f)
            cubicTo(w * 0.05f, h * 0.55f, w * 0.05f, h * 0.05f, w * 0.5f, h * 0.3f)
            cubicTo(w * 0.95f, h * 0.05f, w * 0.95f, h * 0.55f, w / 2, h * 0.9f)
            close()
        }
        drawCircle(color.copy(alpha = 0.25f * (s - 1f) / 0.28f + 0.05f), radius = w * 0.7f * s, center = Offset(w / 2, h / 2))
        scale(s, s, pivot = Offset(w / 2, h / 2)) {
            drawPath(path, Brush.linearGradient(listOf(HealthColors.Rose, color)))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun DialPreview() {
    HealthTheme {
        Column(Modifier.padding(24.dp)) {
            ZeppArcDial(
                rings = listOf(
                    DialRing(0.84f, listOf(HealthColors.Emerald, HealthColors.Mint), "Readiness"),
                    DialRing(11.4f / 21f, listOf(HealthColors.Amber, HealthColors.Gold), "Strain"),
                    DialRing(0.71f, listOf(HealthColors.Violet, HealthColors.Lavender), "Sleep"),
                ),
                modifier = Modifier.size(220.dp),
            ) {
                Text("84", style = MaterialTheme.typography.displayMedium, color = HealthColors.OnSurface)
            }
            Spacer(Modifier.height(16.dp))
            CountdownRing(fraction = 0.6f, running = true, modifier = Modifier.size(120.dp)) {
                Text("01:30", style = MaterialTheme.typography.headlineSmall, color = HealthColors.OnSurface)
            }
        }
    }
}
