package com.carters.health.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.carters.health.ui.theme.HealthColors
import com.carters.health.ui.theme.HealthTheme

/**
 * The signature surface: warm espresso card, 1px gradient rim light and a soft ambient glow
 * that pools underneath the card in the accent colour.
 */
@Composable
fun GlowCard(
    modifier: Modifier = Modifier,
    accent: Color = HealthColors.Amber,
    glow: Boolean = true,
    shape: Shape = RoundedCornerShape(24.dp),
    container: Color = HealthColors.Espresso,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val corner = 24.dp
    Column(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                if (glow) {
                    val radius = size.width * 0.75f
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(accent.copy(alpha = 0.22f), accent.copy(alpha = 0.05f), Color.Transparent),
                            center = Offset(size.width * 0.5f, size.height * 0.95f),
                            radius = radius,
                        ),
                        topLeft = Offset(-radius * 0.15f, 0f),
                        size = Size(size.width + radius * 0.3f, size.height + radius * 0.35f),
                        cornerRadius = CornerRadius(corner.toPx() * 2),
                    )
                }
            }
            .clip(shape)
            .background(container)
            .border(1.dp, HealthColors.rim(accent), shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(contentPadding),
        content = content,
    )
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    action: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = HealthColors.OnSurface)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = HealthColors.Clay)
            }
        }
        if (action != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(enabled = onAction != null) { onAction?.invoke() }
                    .padding(start = 10.dp, top = 4.dp, bottom = 4.dp, end = 2.dp),
            ) {
                Text(action, style = MaterialTheme.typography.labelLarge, color = HealthColors.Amber)
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = HealthColors.Amber, modifier = Modifier.size(18.dp))
            }
        }
    }
}

/** Small, capsule-shaped tag: muscle group, unit, source badge, category. */
@Composable
fun Pill(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = HealthColors.Clay,
    filled: Boolean = false,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(if (filled) color.copy(alpha = 0.18f) else HealthColors.SurfaceHigh.copy(alpha = 0.6f))
            .border(1.dp, color.copy(alpha = if (filled) 0.5f else 0.25f), CircleShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (icon != null) Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** ▼ -2.5 lbs / ▲ +4 bpm style delta badge; colour conveys whether the direction is good. */
@Composable
fun DeltaBadge(delta: Double, unit: String, lowerIsBetter: Boolean = true, decimals: Int = 1, suffix: String = "") {
    val improving = if (lowerIsBetter) delta <= 0 else delta >= 0
    val color = if (improving) HealthColors.Emerald else HealthColors.Coral
    val icon = if (delta < 0) Icons.Default.ArrowDropDown else Icons.Default.ArrowDropUp
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.14f))
            .padding(start = 4.dp, end = 10.dp, top = 3.dp, bottom = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        val magnitude = kotlin.math.abs(delta)
        val text = if (decimals == 0) String.format("%.0f", magnitude) else String.format("%.${decimals}f", magnitude)
        Text(
            "${if (delta < 0) "-" else "+"}$text $unit$suffix",
            style = MaterialTheme.typography.labelMedium,
            color = color,
        )
    }
}

/** Full-width gradient call-to-action with a soft glow. */
@Composable
fun GradientButton(
    text: String,
    modifier: Modifier = Modifier,
    brush: Brush = HealthColors.amberGold,
    glowColor: Color = HealthColors.Amber,
    icon: ImageVector? = null,
    textColor: Color = HealthColors.Obsidian,
    height: Dp = 54.dp,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .drawBehind {
                drawRoundRect(
                    brush = Brush.radialGradient(
                        listOf(glowColor.copy(alpha = 0.35f), Color.Transparent),
                        center = Offset(size.width / 2, size.height),
                        radius = size.width * 0.6f,
                    ),
                    topLeft = Offset(0f, size.height * 0.2f),
                    size = Size(size.width, size.height * 1.2f),
                    cornerRadius = CornerRadius(40f),
                )
            }
            .height(height)
            .clip(shape)
            .background(brush)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (icon != null) Icon(icon, contentDescription = null, tint = textColor)
            Text(text, style = MaterialTheme.typography.titleMedium, color = textColor, fontWeight = FontWeight.Bold)
        }
    }
}

/** Compact bento stat tile: eyebrow label, hero value, supporting line. */
@Composable
fun StatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color = HealthColors.Amber,
    unit: String? = null,
    support: String? = null,
    icon: ImageVector? = null,
    glow: Boolean = false,
    compact: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    GlowCard(modifier = modifier, accent = accent, glow = glow, contentPadding = PaddingValues(if (compact) 12.dp else 16.dp), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = HealthColors.Clay)
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, style = if (compact) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineMedium, color = HealthColors.OnSurface, maxLines = 1, softWrap = false)
            if (unit != null) {
                Spacer(Modifier.width(4.dp))
                Text(unit, style = MaterialTheme.typography.labelMedium, color = HealthColors.Clay, maxLines = 1, softWrap = false, modifier = Modifier.padding(bottom = 3.dp))
            }
            if (trailing != null) {
                Spacer(Modifier.weight(1f))
                trailing()
            }
        }
        if (support != null) {
            Spacer(Modifier.height(4.dp))
            Text(support, style = MaterialTheme.typography.bodySmall, color = HealthColors.Clay)
        }
    }
}

@Composable
fun Eyebrow(text: String, color: Color = HealthColors.Clay, modifier: Modifier = Modifier) {
    Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, color = color, modifier = modifier)
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun GlowCardPreview() {
    HealthTheme {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            GlowCard(accent = HealthColors.Emerald) {
                Eyebrow("Recovery")
                Text("84%", style = MaterialTheme.typography.displaySmall, color = HealthColors.OnSurface)
                DeltaBadge(4.0, "pts", lowerIsBetter = false, decimals = 0)
            }
            StatTile("Body weight", "168.9", unit = "lbs", support = "Fitbit Scale · 7:12 AM", accent = HealthColors.Amber)
            GradientButton("Start Strength Workout") {}
        }
    }
}
