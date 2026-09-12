package com.carters.health.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.carters.health.ui.theme.HealthColors
import com.carters.health.ui.theme.HealthTheme

/**
 * The only card in the app: a flat, softly rounded off-white surface on the beige canvas.
 * No borders, no gradients. [accent] is kept so callers can tint a card (`tinted = true`)
 * when it should read as "active" (a running rest timer, a scanning radar).
 */
@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    accent: Color = HealthColors.Green,
    tinted: Boolean = false,
    shape: Shape = RoundedCornerShape(22.dp),
    container: Color = HealthColors.Card,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (tinted) HealthColors.tint(accent) else container)
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
            Text(title, style = MaterialTheme.typography.headlineSmall, color = HealthColors.Ink)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = HealthColors.Muted)
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
                Text(action, style = MaterialTheme.typography.labelLarge, color = HealthColors.Green)
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = HealthColors.Green, modifier = Modifier.size(18.dp))
            }
        }
    }
}

/** Small capsule tag: muscle group, unit, source badge, category. Flat tonal fill. */
@Composable
fun Pill(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = HealthColors.Muted,
    filled: Boolean = false,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
) {
    val bg = if (filled) color else HealthColors.tint(color)
    val fg = if (filled) HealthColors.Card else if (color == HealthColors.Muted || color == HealthColors.Faint) HealthColors.InkSoft else color
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(bg)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (icon != null) Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(12.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = fg, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** ▼ -2.5 lbs / ▲ +4 bpm style delta badge; green when the direction is good, terracotta otherwise. */
@Composable
fun DeltaBadge(delta: Double, unit: String, lowerIsBetter: Boolean = true, decimals: Int = 1, suffix: String = "") {
    val improving = if (lowerIsBetter) delta <= 0 else delta >= 0
    val color = if (improving) HealthColors.Green else HealthColors.Terracotta
    val icon = if (delta < 0) Icons.Default.ArrowDropDown else Icons.Default.ArrowDropUp
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(HealthColors.tint(color))
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

/** Full-width flat call-to-action. */
@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = HealthColors.Green,
    icon: ImageVector? = null,
    textColor: Color = HealthColors.Card,
    height: Dp = 54.dp,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(color)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (icon != null) Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(20.dp))
            Text(text, style = MaterialTheme.typography.titleMedium, color = textColor)
        }
    }
}

/** Secondary flat button on a tonal fill. */
@Composable
fun TonalButton(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = HealthColors.Green,
    icon: ImageVector? = null,
    height: Dp = 46.dp,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(HealthColors.tint(color))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (icon != null) Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Text(text, style = MaterialTheme.typography.labelLarge, color = color)
        }
    }
}

/** Compact bento stat tile: small label, hero number, supporting line. */
@Composable
fun StatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color = HealthColors.Green,
    unit: String? = null,
    support: String? = null,
    icon: ImageVector? = null,
    tinted: Boolean = false,
    compact: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    SoftCard(modifier = modifier, accent = accent, tinted = tinted, contentPadding = PaddingValues(if (compact) 12.dp else 16.dp), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
            }
            Eyebrow(label)
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, style = if (compact) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineMedium, color = HealthColors.Ink, maxLines = 1, softWrap = false)
            if (unit != null) {
                Spacer(Modifier.width(4.dp))
                Text(unit, style = MaterialTheme.typography.labelMedium, color = HealthColors.Muted, maxLines = 1, softWrap = false, modifier = Modifier.padding(bottom = 4.dp))
            }
            if (trailing != null) {
                Spacer(Modifier.weight(1f))
                trailing()
            }
        }
        if (support != null) {
            Spacer(Modifier.height(4.dp))
            Text(support, style = MaterialTheme.typography.bodySmall, color = HealthColors.Muted)
        }
    }
}

/** Small sentence-case label above a value. */
@Composable
fun Eyebrow(text: String, color: Color = HealthColors.Muted, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.labelMedium, color = color, modifier = modifier)
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F1EA)
@Composable
private fun SoftCardPreview() {
    HealthTheme {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SoftCard {
                Eyebrow("Recovery")
                Text("84%", style = MaterialTheme.typography.displaySmall, color = HealthColors.Ink)
                DeltaBadge(4.0, "pts", lowerIsBetter = false, decimals = 0)
            }
            StatTile("Body weight", "168.9", unit = "lbs", support = "Fitbit Scale · 7:12 AM")
            PrimaryButton("Start strength workout") {}
        }
    }
}
