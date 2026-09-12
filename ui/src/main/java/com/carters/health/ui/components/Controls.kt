package com.carters.health.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.carters.health.data.model.WeightUnit
import com.carters.health.ui.theme.HealthColors
import com.carters.health.ui.theme.LocalHealthHaptics

/** Two-segment lbs ↔ kg pill with a sliding thumb. */
@Composable
fun UnitToggle(unit: WeightUnit, onChange: (WeightUnit) -> Unit, modifier: Modifier = Modifier, accent: Color = HealthColors.Amber) {
    val haptics = LocalHealthHaptics.current
    SegmentedControl(
        options = WeightUnit.entries.map { it.label.uppercase() },
        selected = unit.ordinal,
        onSelect = { haptics.tick(); onChange(WeightUnit.entries[it]) },
        modifier = modifier.width(96.dp),
        accent = accent,
        height = 30.dp,
    )
}

/** Generic segmented control with a springy sliding thumb; used for tabs, units and timeframes. */
@Composable
fun SegmentedControl(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = HealthColors.Amber,
    height: Dp = 40.dp,
    thumbBrush: Brush? = null,
) {
    val shape = RoundedCornerShape(height / 2)
    BoxWithConstraints(
        modifier
            .height(height)
            .clip(shape)
            .background(HealthColors.Espresso)
            .border(1.dp, HealthColors.Border, shape)
            .padding(3.dp),
    ) {
        val segment = maxWidth / options.size
        val offset by animateFloatAsState(selected.toFloat(), Motion.snappySpring, label = "thumb")
        Box(
            Modifier
                .offset(x = segment * offset)
                .width(segment)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(thumbBrush ?: SolidColor(accent.copy(alpha = 0.22f)))
                .border(1.dp, accent.copy(alpha = 0.6f), CircleShape),
        )
        Row(Modifier.fillMaxWidth().fillMaxHeight()) {
            options.forEachIndexed { i, label ->
                val color by animateColorAsState(if (i == selected) accent else HealthColors.Clay, label = "seg")
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .clickable { onSelect(i) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(label, style = MaterialTheme.typography.labelMedium, color = color, maxLines = 1)
                }
            }
        }
    }
}

/** Numeric cell used in the set table (weight / reps / RPE). */
@Composable
fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "—",
    accent: Color = HealthColors.Amber,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(10.dp)
    BasicTextField(
        value = value,
        onValueChange = { new -> if (new.length <= 6 && new.all { it.isDigit() || it == '.' }) onValueChange(new) },
        modifier = modifier
            .height(38.dp)
            .clip(shape)
            .background(if (enabled) HealthColors.SurfaceHigh else HealthColors.Espresso)
            .border(1.dp, if (enabled) HealthColors.Border else Color.Transparent, shape),
        enabled = enabled,
        singleLine = true,
        textStyle = TextStyle(color = HealthColors.OnSurface, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, fontSize = MaterialTheme.typography.bodyLarge.fontSize),
        cursorBrush = SolidColor(accent),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        decorationBox = { inner ->
            Box(Modifier.fillMaxWidth().fillMaxHeight(), contentAlignment = Alignment.Center) {
                if (value.isEmpty()) {
                    Text(placeholder, color = HealthColors.ClayDim, style = MaterialTheme.typography.bodyMedium)
                }
                inner()
            }
        },
    )
}
