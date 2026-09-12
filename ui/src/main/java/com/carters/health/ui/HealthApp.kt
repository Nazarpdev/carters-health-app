package com.carters.health.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.carters.health.data.model.WorkoutSession
import com.carters.health.data.model.WorkoutSummary
import com.carters.health.data.model.format0
import com.carters.health.data.repo.HealthRepository
import com.carters.health.ui.components.Motion
import com.carters.health.ui.navigation.HealthNavState
import com.carters.health.ui.navigation.HealthRoute
import com.carters.health.ui.navigation.HealthTab
import com.carters.health.ui.screens.dashboard.DashboardScreen
import com.carters.health.ui.screens.sleep.SleepScreen
import com.carters.health.ui.screens.trends.TrendsScreen
import com.carters.health.ui.screens.weight.WeightTrendScreen
import com.carters.health.ui.screens.workout.CelebrationBanner
import com.carters.health.ui.screens.workout.WorkoutScreen
import com.carters.health.ui.screens.workout.WorkoutSessionState
import com.carters.health.ui.theme.HealthColors
import com.carters.health.ui.theme.LocalHealthHaptics
import kotlinx.coroutines.delay
import java.time.LocalDateTime

/**
 * Root of the UI: tab content, detail routes and the floating bottom bar.
 * Everything below here is platform-agnostic Compose so the whole tree renders in previews.
 */
@Composable
fun HealthApp(
    repository: HealthRepository,
    nav: HealthNavState,
    session: WorkoutSessionState,
    onWorkoutFinished: (WorkoutSummary) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHealthHaptics.current
    var toast by remember { mutableStateOf<WorkoutSummary?>(null) }
    LaunchedEffect(toast) { if (toast != null) { delay(3600); toast = null } }

    Box(modifier.fillMaxSize().background(HealthColors.Obsidian)) {
        AnimatedContent(
            targetState = nav.current to nav.tab,
            transitionSpec = {
                if (targetState.first != null) {
                    (slideInHorizontally { it / 3 } + fadeIn()) togetherWith (fadeOut())
                } else if (initialState.first != null) {
                    fadeIn() togetherWith (slideOutHorizontally { it / 3 } + fadeOut())
                } else {
                    fadeIn() togetherWith fadeOut()
                }
            },
            label = "root",
            modifier = Modifier.statusBarsPadding(),
        ) { (route, tab) ->
            when (route) {
                HealthRoute.WeightTrend -> WeightTrendScreen(repository, onBack = { nav.popDetail() })
                null -> when (tab) {
                    HealthTab.HOME -> DashboardScreen(
                        repository,
                        onOpenWeight = { haptics.tick(); nav.push(HealthRoute.WeightTrend) },
                        onStartWorkout = { haptics.confirm(); nav.selectTab(HealthTab.TRAIN) },
                    )
                    HealthTab.TRAIN -> WorkoutScreen(
                        repository, session,
                        onFinished = { summary ->
                            haptics.celebrate()
                            toast = summary
                            onWorkoutFinished(summary)
                            nav.selectTab(HealthTab.HOME)
                        },
                    )
                    HealthTab.SLEEP -> SleepScreen(repository)
                    HealthTab.TRENDS -> TrendsScreen(repository)
                }
            }
        }

        AnimatedVisibility(
            visible = toast != null,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(horizontal = 20.dp),
        ) {
            val s = toast
            if (s != null) {
                CelebrationBanner(
                    title = if (s.prs.isEmpty()) "Workout saved" else "${s.prs.size} new PR${if (s.prs.size > 1) "s" else ""}!",
                    subtitle = "${s.totalSets} sets · ${s.totalReps} reps · ${s.totalVolumeLbs.format0()} lbs moved",
                )
            }
        }

        AnimatedVisibility(
            visible = nav.current == null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            FloatingTabBar(
                selected = nav.tab,
                liveWorkout = session.completedSets > 0 || session.restTimer.active,
                onSelect = { haptics.tick(); nav.selectTab(it) },
                modifier = Modifier.navigationBarsPadding().padding(start = 20.dp, end = 20.dp, bottom = 14.dp),
            )
        }
    }
}

private fun HealthTab.icon(): ImageVector = when (this) {
    HealthTab.HOME -> Icons.Default.Home
    HealthTab.TRAIN -> Icons.Default.FitnessCenter
    HealthTab.SLEEP -> Icons.Default.Bedtime
    HealthTab.TRENDS -> Icons.Default.ShowChart
}

private fun HealthTab.accent(): Color = when (this) {
    HealthTab.HOME -> HealthColors.Amber
    HealthTab.TRAIN -> HealthColors.Coral
    HealthTab.SLEEP -> HealthColors.Lavender
    HealthTab.TRENDS -> HealthColors.Mint
}

/** Floating capsule tab bar with per-tab accent glow and springy selection. */
@Composable
fun FloatingTabBar(selected: HealthTab, liveWorkout: Boolean, onSelect: (HealthTab) -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(28.dp)
    val accent by animateColorAsState(selected.accent(), label = "tabAccent")
    Row(
        modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    brush = Brush.radialGradient(listOf(accent.copy(alpha = 0.25f), Color.Transparent), center = Offset(size.width / 2, size.height), radius = size.width * 0.5f),
                    topLeft = Offset(0f, size.height * 0.3f),
                    size = size.copy(height = size.height * 1.1f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(60f),
                )
            }
            .clip(shape)
            .background(HealthColors.Espresso.copy(alpha = 0.96f))
            .border(1.dp, HealthColors.rim(accent, 0.45f), shape)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HealthTab.entries.forEach { tab ->
            val on = tab == selected
            val scale by animateFloatAsState(if (on) 1f else 0.92f, Motion.softSpring, label = "tabScale")
            val tint by animateColorAsState(if (on) tab.accent() else HealthColors.Clay, label = "tint")
            val interaction = remember { MutableInteractionSource() }
            Column(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (on) tab.accent().copy(alpha = 0.14f) else Color.Transparent)
                    .clickable(interactionSource = interaction, indication = null) { onSelect(tab) }
                    .padding(vertical = 8.dp)
                    .scale(scale),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box {
                    Icon(tab.icon(), contentDescription = tab.label, tint = tint, modifier = Modifier.size(22.dp))
                    if (tab == HealthTab.TRAIN && liveWorkout) {
                        Box(Modifier.align(Alignment.TopEnd).size(7.dp).clip(CircleShape).background(HealthColors.Emerald))
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(tab.label, style = MaterialTheme.typography.labelSmall, color = tint)
            }
        }
    }
}

/** A blank session to start after one is saved. */
fun freshSession(): WorkoutSession = WorkoutSession(
    id = System.currentTimeMillis(),
    name = "New Workout",
    startedAt = LocalDateTime.now(),
    exercises = emptyList(),
)
