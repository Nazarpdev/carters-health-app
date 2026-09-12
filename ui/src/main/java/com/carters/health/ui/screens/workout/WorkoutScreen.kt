package com.carters.health.ui.screens.workout

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.carters.health.data.model.WorkoutSummary
import com.carters.health.data.repo.HealthRepository
import com.carters.health.data.repo.InMemoryHealthRepository
import com.carters.health.data.repo.SampleData
import com.carters.health.ui.components.SegmentedControl
import com.carters.health.ui.components.UnitToggle
import com.carters.health.ui.theme.HealthColors
import com.carters.health.ui.theme.HealthTheme
import com.carters.health.ui.theme.LocalHealthHaptics

enum class WorkoutMode(val label: String) { ACTIVE("Active"), ANALYTICS("Analytics & PRs"), LIBRARY("Library") }

/** Keeps the live session alive across tab switches; created once at app scope. */
@Composable
fun rememberWorkoutSession(): WorkoutSessionState = remember { WorkoutSessionState(SampleData.activeSession()) }

@Composable
fun WorkoutScreen(
    repository: HealthRepository,
    session: WorkoutSessionState,
    onFinished: (WorkoutSummary) -> Unit,
    modifier: Modifier = Modifier,
    initialMode: WorkoutMode = WorkoutMode.ACTIVE,
) {
    var mode by rememberSaveable { mutableStateOf(initialMode) }
    val library by repository.exercises.collectAsState()
    val haptics = LocalHealthHaptics.current

    Column(modifier.fillMaxSize().background(HealthColors.Canvas)) {
        Row(Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Strength Studio", style = MaterialTheme.typography.headlineMedium, color = HealthColors.Ink, modifier = Modifier.weight(1f))
            UnitToggle(session.unit, { session.unit = it })
        }
        Spacer(Modifier.height(12.dp))
        SegmentedControl(
            options = WorkoutMode.entries.map { it.label },
            selected = mode.ordinal,
            onSelect = { haptics.tick(); mode = WorkoutMode.entries[it] },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            accent = HealthColors.Green,
        )
        Spacer(Modifier.height(6.dp))
        AnimatedContent(
            targetState = mode,
            transitionSpec = {
                val forward = targetState.ordinal > initialState.ordinal
                (slideInHorizontally { if (forward) it / 6 else -it / 6 } + fadeIn()) togetherWith
                    (slideOutHorizontally { if (forward) -it / 6 else it / 6 } + fadeOut())
            },
            label = "workoutMode",
        ) { m ->
            when (m) {
                WorkoutMode.ACTIVE -> ActiveWorkoutTab(session, library, onFinished, Modifier.fillMaxSize())
                WorkoutMode.ANALYTICS -> AnalyticsTab(repository, session.unit, Modifier.fillMaxSize())
                WorkoutMode.LIBRARY -> LibraryTab(repository, onAddToWorkout = { session.addExercise(it); mode = WorkoutMode.ACTIVE }, Modifier.fillMaxSize())
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F1EA, heightDp = 1400)
@Composable
private fun WorkoutPreview() {
    HealthTheme {
        WorkoutScreen(InMemoryHealthRepository(), rememberWorkoutSession(), onFinished = {})
    }
}
