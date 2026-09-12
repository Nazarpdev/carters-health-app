package com.carters.health.ui.screens.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.carters.health.data.model.Equipment
import com.carters.health.data.model.Exercise
import com.carters.health.data.model.MuscleGroup
import com.carters.health.data.repo.HealthRepository
import com.carters.health.ui.components.Eyebrow
import com.carters.health.ui.components.GlowCard
import com.carters.health.ui.components.GradientButton
import com.carters.health.ui.components.Pill
import com.carters.health.ui.components.SectionHeader
import com.carters.health.ui.components.SegmentedControl
import com.carters.health.ui.theme.HealthColors
import com.carters.health.ui.theme.LocalHealthHaptics

@Composable
fun LibraryTab(repository: HealthRepository, onAddToWorkout: (Exercise) -> Unit, modifier: Modifier = Modifier) {
    val exercises by repository.exercises.collectAsState()
    var query by remember { mutableStateOf("") }
    var group by remember { mutableStateOf<MuscleGroup?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    val haptics = LocalHealthHaptics.current
    val results = remember(query, group, exercises) {
        exercises.filter { (group == null || it.muscleGroup == group) && it.name.contains(query, ignoreCase = true) }
    }
    val grouped = remember(results) { results.groupBy { it.muscleGroup } }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { SearchField(query, { query = it }, "Search ${exercises.size} exercises") }
        item { MuscleFilterRow(group) { group = it } }
        item {
            GradientButton("Create Custom Exercise", icon = Icons.Default.Add, brush = HealthColors.lavenderViolet, glowColor = HealthColors.Violet, modifier = Modifier.fillMaxWidth(), height = 48.dp) { showCreate = true }
        }
        grouped.forEach { (g, list) ->
            item(key = "header-${g.name}") {
                SectionHeader(g.label, subtitle = "${list.size} exercises", modifier = Modifier.padding(top = 8.dp))
            }
            items(list, key = { it.id }) { ex ->
                ExerciseListRow(ex, trailing = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = HealthColors.Clay) }) { haptics.confirm(); onAddToWorkout(ex) }
            }
        }
    }

    if (showCreate) {
        CreateExerciseDialog(onDismiss = { showCreate = false }) {
            repository.addCustomExercise(it)
            haptics.celebrate()
            showCreate = false
        }
    }
}

@Composable
private fun CreateExerciseDialog(onDismiss: () -> Unit, onCreate: (Exercise) -> Unit) {
    var name by remember { mutableStateOf("") }
    var group by remember { mutableStateOf(MuscleGroup.CHEST) }
    var equipment by remember { mutableStateOf(Equipment.BARBELL) }
    val restOptions = listOf(45, 60, 90, 120, 180)
    var restIndex by remember { mutableStateOf(2) }

    Dialog(onDismissRequest = onDismiss) {
        GlowCard(accent = HealthColors.Lavender, contentPadding = PaddingValues(20.dp)) {
            Text("Custom exercise", style = MaterialTheme.typography.headlineSmall, color = HealthColors.OnSurface)
            Spacer(Modifier.height(14.dp))
            Eyebrow("Name")
            Spacer(Modifier.height(6.dp))
            val shape = RoundedCornerShape(12.dp)
            BasicTextField(
                value = name, onValueChange = { name = it }, singleLine = true,
                textStyle = TextStyle(color = HealthColors.OnSurface, fontSize = MaterialTheme.typography.bodyLarge.fontSize),
                cursorBrush = SolidColor(HealthColors.Lavender),
                modifier = Modifier.fillMaxWidth().clip(shape).background(HealthColors.Surface).border(1.dp, HealthColors.Border, shape).padding(12.dp),
                decorationBox = { inner -> if (name.isEmpty()) Text("e.g. Landmine Press", color = HealthColors.ClayDim); inner() },
            )
            Spacer(Modifier.height(14.dp))
            Eyebrow("Muscle group")
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(MuscleGroup.entries) { g -> Pill(g.label, color = if (g == group) HealthColors.Lavender else HealthColors.Clay, filled = g == group) { group = g } }
            }
            Spacer(Modifier.height(14.dp))
            Eyebrow("Equipment")
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(Equipment.entries) { e -> Pill(e.label, color = if (e == equipment) HealthColors.Lavender else HealthColors.Clay, filled = e == equipment) { equipment = e } }
            }
            Spacer(Modifier.height(14.dp))
            Eyebrow("Default rest")
            Spacer(Modifier.height(6.dp))
            SegmentedControl(restOptions.map { "${it}s" }, restIndex, { restIndex = it }, Modifier.fillMaxWidth(), accent = HealthColors.Lavender, height = 34.dp)
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge, color = HealthColors.Clay, modifier = Modifier.clickable(onClick = onDismiss).padding(8.dp))
                Spacer(Modifier.width(8.dp))
                GradientButton(
                    "Save exercise", brush = HealthColors.lavenderViolet, glowColor = HealthColors.Violet, textColor = HealthColors.OnSurface,
                    modifier = Modifier.weight(1f), height = 46.dp,
                ) {
                    if (name.isNotBlank()) onCreate(Exercise(0, name.trim(), group, equipment, restOptions[restIndex], isCustom = true))
                }
            }
        }
    }
}
