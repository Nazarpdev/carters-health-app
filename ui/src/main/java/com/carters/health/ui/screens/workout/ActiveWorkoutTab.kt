package com.carters.health.ui.screens.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.carters.health.data.model.Exercise
import com.carters.health.data.model.MuscleGroup
import com.carters.health.data.model.Units
import com.carters.health.data.model.WeightUnit
import com.carters.health.data.model.WorkoutSummary
import com.carters.health.data.model.format0
import com.carters.health.ui.components.CountdownRing
import com.carters.health.ui.components.Eyebrow
import com.carters.health.ui.components.GlowCard
import com.carters.health.ui.components.GradientButton
import com.carters.health.ui.components.NumberField
import com.carters.health.ui.components.Pill
import com.carters.health.ui.theme.HealthColors
import com.carters.health.ui.theme.LocalHealthHaptics
import kotlinx.coroutines.delay

private val REST_PRESETS = listOf(30, 60, 90, 120, 180)

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun ActiveWorkoutTab(
    session: WorkoutSessionState,
    library: List<Exercise>,
    onFinished: (WorkoutSummary) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHealthHaptics.current
    val timer = session.restTimer
    var showAddExercise by remember { mutableStateOf(false) }
    var showFinish by remember { mutableStateOf(false) }
    var celebrate by remember { mutableStateOf(false) }

    // Frame-accurate countdown while running.
    LaunchedEffect(timer.running) {
        while (timer.running) {
            withFrameMillis { }
            timer.tick()
        }
    }
    // Vibrate + celebrate when the timer hits zero.
    LaunchedEffect(timer.finishedAt) {
        if (timer.finishedAt != null) {
            haptics.celebrate()
            celebrate = true
            delay(3200)
            celebrate = false
        }
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { ChronometerHeader(session, onFinish = { showFinish = true }) }
        stickyHeader {
            Column(Modifier.background(HealthColors.Obsidian).padding(bottom = 4.dp)) {
                RestTimerCard(timer, exerciseName = session.lastCompletedExerciseName)
                AnimatedVisibility(visible = celebrate, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                    CelebrationBanner("Catch your breath — explosive set!", "Rest complete. Load the bar.")
                }
            }
        }
        items(session.exercises, key = { it.id }) { ex ->
            ExerciseCard(session, ex)
        }
        item {
            AddButton("Add Exercise", accent = HealthColors.Amber) { showAddExercise = true }
        }
    }

    if (showAddExercise) {
        AddExerciseSheet(library = library, onDismiss = { showAddExercise = false }) {
            session.addExercise(it)
            haptics.confirm()
            showAddExercise = false
        }
    }
    if (showFinish) {
        FinishWorkoutDialog(session, onDismiss = { showFinish = false }) {
            showFinish = false
            onFinished(session.summary())
        }
    }
}

@Composable
private fun ChronometerHeader(session: WorkoutSessionState, onFinish: () -> Unit) {
    var elapsed by remember { mutableStateOf(session.elapsed.seconds) }
    LaunchedEffect(Unit) {
        while (true) {
            elapsed = session.elapsed.seconds
            delay(1000)
        }
    }
    GlowCard(accent = HealthColors.Amber, contentPadding = PaddingValues(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = HealthColors.Gold, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(formatClock(elapsed), style = MaterialTheme.typography.displaySmall, color = HealthColors.OnSurface, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BasicTextField(
                        value = session.name,
                        onValueChange = { session.name = it },
                        singleLine = true,
                        textStyle = TextStyle(color = HealthColors.Sand, fontSize = MaterialTheme.typography.titleMedium.fontSize, fontWeight = FontWeight.SemiBold),
                        cursorBrush = SolidColor(HealthColors.Amber),
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Default.Edit, contentDescription = "Rename", tint = HealthColors.ClayDim, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Box(
                Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(HealthColors.emeraldMint)
                    .clickable(onClick = onFinish)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text("Finish", style = MaterialTheme.typography.labelLarge, color = HealthColors.Obsidian, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pill("${session.completedSets} ${if (session.completedSets == 1) "set" else "sets"}", color = HealthColors.Emerald, filled = true)
            Pill("${session.totalReps} reps", color = HealthColors.Mint)
            Pill("${Units.displayWeight(session.totalVolumeLbs, session.unit).format0()} ${session.unit.label}", color = HealthColors.Gold)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RestTimerCard(timer: RestTimerState, exerciseName: String?, modifier: Modifier = Modifier) {
    val haptics = LocalHealthHaptics.current
    val accent = if (timer.active) HealthColors.Mint else HealthColors.Clay
    GlowCard(modifier = modifier, accent = accent, glow = timer.active, contentPadding = PaddingValues(14.dp), container = HealthColors.Surface) {
        if (timer.active) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CountdownRing(fraction = timer.fraction, running = timer.running, modifier = Modifier.size(92.dp), strokeWidth = 8.dp) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(formatClock(timer.remainingSeconds.toLong()), style = MaterialTheme.typography.titleLarge, color = HealthColors.OnSurface, fontWeight = FontWeight.Bold)
                        Text(if (timer.paused) "PAUSED" else "REST", style = MaterialTheme.typography.labelSmall, color = HealthColors.Mint)
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Eyebrow("Resting after", HealthColors.Mint)
                    Text(exerciseName ?: "Set complete", style = MaterialTheme.typography.titleSmall, color = HealthColors.OnSurface, maxLines = 1)
                    Spacer(Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        TimerChip("+30s") { haptics.tick(); timer.adjust(30) }
                        TimerChip("-15s") { haptics.tick(); timer.adjust(-15) }
                        TimerChip(if (timer.paused) "Resume" else "Pause", icon = if (timer.paused) Icons.Default.PlayArrow else Icons.Default.Pause) { haptics.tick(); timer.togglePause() }
                        TimerChip("Skip", icon = Icons.Default.SkipNext, color = HealthColors.Coral) { haptics.tick(); timer.skip() }
                    }
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Timer, contentDescription = null, tint = HealthColors.Clay, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Rest timer", style = MaterialTheme.typography.labelLarge, color = HealthColors.Sand)
                Spacer(Modifier.weight(1f))
                Text("auto-starts on ✓", style = MaterialTheme.typography.labelSmall, color = HealthColors.ClayDim)
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                REST_PRESETS.forEach { s ->
                    TimerChip("${s}s", modifier = Modifier.weight(1f)) { haptics.confirm(); timer.start(s) }
                }
            }
        }
    }
}

@Composable
private fun TimerChip(
    text: String,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    color: Color = HealthColors.Mint,
    onClick: () -> Unit,
) {
    Row(
        modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.4f), CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(3.dp))
        }
        Text(text, style = MaterialTheme.typography.labelMedium, color = color, maxLines = 1)
    }
}

@Composable
fun CelebrationBanner(title: String, subtitle: String, accent: Color = HealthColors.Gold) {
    Row(
        Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(listOf(accent.copy(alpha = 0.3f), HealthColors.Coral.copy(alpha = 0.18f))))
            .border(1.dp, accent.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = accent)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, color = HealthColors.OnSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = HealthColors.Sand)
        }
    }
}

@Composable
private fun ExerciseCard(session: WorkoutSessionState, ex: ExerciseState) {
    var menu by remember { mutableStateOf(false) }
    val haptics = LocalHealthHaptics.current
    GlowCard(accent = HealthColors.Amber, glow = false, contentPadding = PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(ex.exercise.name, style = MaterialTheme.typography.titleMedium, color = HealthColors.OnSurface)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Pill(ex.exercise.muscleGroup.label.uppercase(), color = HealthColors.Amber, filled = true)
                    Pill(ex.exercise.equipment.label, color = HealthColors.Clay)
                    Pill("rest ${ex.exercise.defaultRestSeconds}s", color = HealthColors.Mint)
                }
            }
            Box {
                Icon(
                    Icons.Default.MoreVert, contentDescription = "Options", tint = HealthColors.Clay,
                    modifier = Modifier.clip(CircleShape).clickable { menu = true }.padding(6.dp),
                )
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(text = { Text("Add warm-up set") }, onClick = {
                        menu = false
                        session.addSet(ex)
                        ex.sets.last().isWarmup = true
                    })
                    DropdownMenuItem(text = { Text("Remove exercise") }, onClick = { menu = false; session.removeExercise(ex) })
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        TableHeader(session.unit)
        Spacer(Modifier.height(4.dp))
        ex.sets.forEach { set ->
            key(set.id) {
                SetRow(
                    label = ex.indexLabel(set),
                    set = set,
                    unit = session.unit,
                    onToggleWarmup = { haptics.tick(); set.isWarmup = !set.isWarmup },
                    onToggleDone = { haptics.confirm(); session.toggleSet(ex, set) },
                    onRemove = { session.removeSet(ex, set) },
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        AddButton("Add Set", accent = HealthColors.Clay, compact = true) { haptics.tick(); session.addSet(ex) }
    }
}

private val COL_SET = 0.55f
private val COL_PREV = 1.15f
private val COL_WEIGHT = 1f
private val COL_REPS = 0.8f
private val COL_RPE = 0.8f
private val COL_DONE = 0.7f

@Composable
private fun TableHeader(unit: WeightUnit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        HeaderCell("SET", COL_SET)
        HeaderCell("PREVIOUS", COL_PREV)
        HeaderCell(unit.label.uppercase(), COL_WEIGHT)
        HeaderCell("REPS", COL_REPS)
        HeaderCell("RPE", COL_RPE)
        HeaderCell("DONE", COL_DONE)
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.HeaderCell(text: String, weight: Float) {
    Text(text, style = MaterialTheme.typography.labelSmall, color = HealthColors.ClayDim, textAlign = TextAlign.Center, modifier = Modifier.weight(weight))
}

@Composable
private fun SetRow(
    label: String,
    set: SetState,
    unit: WeightUnit,
    onToggleWarmup: () -> Unit,
    onToggleDone: () -> Unit,
    onRemove: () -> Unit,
) {
    val rowColor by animateColorAsState(
        if (set.completed) HealthColors.Emerald.copy(alpha = 0.10f) else Color.Transparent, label = "row",
    )
    var weightText by remember(set.id, unit) { mutableStateOf(formatWeight(set.weightLbs, unit)) }
    var repsText by remember(set.id) { mutableStateOf(set.reps?.toString() ?: "") }
    var rpeText by remember(set.id) { mutableStateOf(set.rpe?.let { String.format("%.1f", it).removeSuffix(".0") } ?: "") }
    // Keep the field in sync when the model changes underneath (e.g. auto-adopt on complete).
    LaunchedEffect(set.weightLbs, unit) { weightText = formatWeight(set.weightLbs, unit) }
    LaunchedEffect(set.reps) { repsText = set.reps?.toString() ?: "" }

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(rowColor)
            .padding(horizontal = 2.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Set index / warm-up toggle chip
        Box(Modifier.weight(COL_SET), contentAlignment = Alignment.Center) {
            val warm = set.isWarmup
            Box(
                Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(if (warm) HealthColors.Amber.copy(alpha = 0.2f) else HealthColors.SurfaceHigh)
                    .border(1.dp, if (warm) HealthColors.Amber.copy(alpha = 0.7f) else HealthColors.Border, CircleShape)
                    .clickable(onClick = onToggleWarmup),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, style = MaterialTheme.typography.labelMedium, color = if (warm) HealthColors.Amber else HealthColors.Sand)
            }
        }
        // Previous benchmark
        Box(Modifier.weight(COL_PREV), contentAlignment = Alignment.Center) {
            val p = set.previous
            Text(
                if (p == null) "—" else "${formatWeight(p.weightLbs, unit)} × ${p.reps}",
                style = MaterialTheme.typography.bodySmall, color = HealthColors.Clay, textAlign = TextAlign.Center,
            )
        }
        NumberField(
            value = weightText,
            onValueChange = { t -> weightText = t; set.weightLbs = t.toDoubleOrNull()?.let { Units.toLbs(it, unit) } },
            modifier = Modifier.weight(COL_WEIGHT).padding(horizontal = 3.dp),
            enabled = !set.completed,
        )
        NumberField(
            value = repsText,
            onValueChange = { t -> repsText = t.filter { it.isDigit() }; set.reps = repsText.toIntOrNull() },
            modifier = Modifier.weight(COL_REPS).padding(horizontal = 3.dp),
            enabled = !set.completed,
        )
        NumberField(
            value = rpeText,
            onValueChange = { t -> rpeText = t; set.rpe = t.toDoubleOrNull()?.coerceIn(1.0, 10.0) },
            modifier = Modifier.weight(COL_RPE).padding(horizontal = 3.dp),
            placeholder = "RPE",
            accent = HealthColors.Lavender,
            enabled = !set.completed,
        )
        Box(Modifier.weight(COL_DONE), contentAlignment = Alignment.Center) {
            DoneButton(set.completed, onToggleDone)
        }
    }
    AnimatedVisibility(visible = set.completed, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
        val e1rm = set.estimatedOneRepMax
        Row(Modifier.padding(start = 8.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (e1rm != null) "est. 1RM ${formatWeight(e1rm, unit)} ${unit.label}" else "logged",
                style = MaterialTheme.typography.labelSmall, color = HealthColors.Emerald,
            )
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.Default.Close, contentDescription = "Remove set", tint = HealthColors.ClayDim,
                modifier = Modifier.size(20.dp).clip(CircleShape).clickable(onClick = onRemove).padding(3.dp),
            )
        }
    }
}

@Composable
private fun DoneButton(done: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (done) 1.08f else 1f, com.carters.health.ui.components.Motion.softSpring, label = "doneScale")
    val bg by animateColorAsState(if (done) HealthColors.Emerald else HealthColors.SurfaceHigh, label = "doneBg")
    val border by animateColorAsState(if (done) HealthColors.Mint else HealthColors.Border, label = "doneBorder")
    Box(
        Modifier
            .size(36.dp)
            .scale(scale)
            .clip(RoundedCornerShape(11.dp))
            .background(bg)
            .border(1.5.dp, border, RoundedCornerShape(11.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Default.Check, contentDescription = "Complete set", tint = if (done) HealthColors.Obsidian else HealthColors.Clay, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun AddButton(text: String, accent: Color, compact: Boolean = false, onClick: () -> Unit) {
    val shape = RoundedCornerShape(if (compact) 12.dp else 16.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(accent.copy(alpha = if (compact) 0.06f else 0.12f))
            .border(1.dp, accent.copy(alpha = if (compact) 0.25f else 0.5f), shape)
            .clickable(onClick = onClick)
            .padding(vertical = if (compact) 9.dp else 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.Add, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelLarge, color = accent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseSheet(library: List<Exercise>, onDismiss: () -> Unit, onPick: (Exercise) -> Unit) {
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }
    var group by remember { mutableStateOf<MuscleGroup?>(null) }
    val results = remember(query, group, library) {
        library.filter { (group == null || it.muscleGroup == group) && it.name.contains(query, ignoreCase = true) }
    }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheet, containerColor = HealthColors.Espresso, dragHandle = null) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text("Add exercise", style = MaterialTheme.typography.headlineSmall, color = HealthColors.OnSurface)
            Spacer(Modifier.height(12.dp))
            SearchField(query, { query = it }, "Search ${library.size} exercises")
            Spacer(Modifier.height(10.dp))
            MuscleFilterRow(group) { group = it }
            Spacer(Modifier.height(10.dp))
            LazyColumn(Modifier.height(420.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(results, key = { it.id }) { ex -> ExerciseListRow(ex) { onPick(ex) } }
            }
        }
    }
}

@Composable
fun SearchField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(HealthColors.Surface)
            .border(1.dp, HealthColors.Border, shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.Search, contentDescription = null, tint = HealthColors.Clay, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(color = HealthColors.OnSurface, fontSize = MaterialTheme.typography.bodyLarge.fontSize),
            cursorBrush = SolidColor(HealthColors.Amber),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                if (value.isEmpty()) Text(placeholder, color = HealthColors.ClayDim, style = MaterialTheme.typography.bodyLarge)
                inner()
            },
        )
        if (value.isNotEmpty()) {
            Icon(Icons.Default.Close, contentDescription = "Clear", tint = HealthColors.Clay, modifier = Modifier.size(16.dp).clickable { onValueChange("") })
        }
    }
}

@Composable
fun MuscleFilterRow(selected: MuscleGroup?, onSelect: (MuscleGroup?) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        item { Pill("All", color = if (selected == null) HealthColors.Amber else HealthColors.Clay, filled = selected == null) { onSelect(null) } }
        items(MuscleGroup.entries) { g ->
            val on = selected == g
            Pill(g.label, color = if (on) HealthColors.Amber else HealthColors.Clay, filled = on) { onSelect(if (on) null else g) }
        }
    }
}

@Composable
fun ExerciseListRow(ex: Exercise, trailing: (@Composable () -> Unit)? = null, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(HealthColors.Surface)
            .border(1.dp, HealthColors.Border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(ex.name, style = MaterialTheme.typography.titleSmall, color = HealthColors.OnSurface)
                if (ex.isCustom) { Spacer(Modifier.width(6.dp)); Pill("Custom", color = HealthColors.Lavender, filled = true) }
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Pill(ex.muscleGroup.label, color = HealthColors.Amber)
                Pill(ex.equipment.label, color = HealthColors.Clay)
                Pill("${ex.defaultRestSeconds}s rest", color = HealthColors.Mint)
            }
        }
        if (trailing != null) trailing() else Icon(Icons.Default.Add, contentDescription = null, tint = HealthColors.Amber)
    }
}

@Composable
private fun FinishWorkoutDialog(session: WorkoutSessionState, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val summary = remember { session.summary() }
    val unit = session.unit
    Dialog(onDismissRequest = onDismiss) {
        GlowCard(accent = HealthColors.Gold, contentPadding = PaddingValues(22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = HealthColors.Gold, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Workout complete", style = MaterialTheme.typography.headlineSmall, color = HealthColors.OnSurface)
                    Text(session.name, style = MaterialTheme.typography.bodySmall, color = HealthColors.Clay)
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryTile("Volume", Units.displayWeight(summary.totalVolumeLbs, unit).format0(), unit.label, HealthColors.Gold, Modifier.weight(1f))
                SummaryTile("Duration", formatClock(summary.durationSeconds), "", HealthColors.Mint, Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryTile("Sets", summary.totalSets.toString(), "", HealthColors.Emerald, Modifier.weight(1f))
                SummaryTile("Reps", summary.totalReps.toString(), "", HealthColors.Coral, Modifier.weight(1f))
            }
            if (summary.prs.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                Eyebrow("Personal records", HealthColors.Gold)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    summary.prs.take(3).forEach { Pill("PR · $it", color = HealthColors.Gold, filled = true, icon = Icons.Default.EmojiEvents) }
                }
            } else {
                Spacer(Modifier.height(14.dp))
                Text("Solid work. Recovery starts now — hydrate and eat.", style = MaterialTheme.typography.bodyMedium, color = HealthColors.Sand)
            }
            Spacer(Modifier.height(18.dp))
            GradientButton("Save Workout", brush = HealthColors.amberGold, modifier = Modifier.fillMaxWidth(), onClick = onConfirm)
            Spacer(Modifier.height(8.dp))
            Text(
                "Keep training", style = MaterialTheme.typography.labelLarge, color = HealthColors.Clay,
                modifier = Modifier.align(Alignment.CenterHorizontally).clip(CircleShape).clickable(onClick = onDismiss).padding(8.dp),
            )
        }
    }
}

@Composable
private fun SummaryTile(label: String, value: String, unit: String, accent: Color, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(HealthColors.Surface)
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(12.dp),
    ) {
        Eyebrow(label, accent)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, style = MaterialTheme.typography.headlineSmall, color = HealthColors.OnSurface)
            if (unit.isNotEmpty()) { Spacer(Modifier.width(4.dp)); Text(unit, style = MaterialTheme.typography.labelMedium, color = HealthColors.Clay, modifier = Modifier.padding(bottom = 3.dp)) }
        }
    }
}
