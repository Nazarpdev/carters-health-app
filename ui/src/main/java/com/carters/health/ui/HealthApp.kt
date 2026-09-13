package com.carters.health.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.carters.health.data.model.WorkoutSession
import com.carters.health.data.model.WorkoutSummary
import com.carters.health.data.model.format0
import com.carters.health.data.repo.HealthRepository
import com.carters.health.ui.components.Motion
import com.carters.health.ui.components.pressableClick
import com.carters.health.ui.screens.settings.SettingsScreen
import com.carters.health.ui.settings.AppSettings
import com.carters.health.ui.settings.LocalAppSettings
import com.carters.health.ui.theme.HealthHaptics
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
    settings: AppSettings = LocalAppSettings.current,
) {
    // Settings drive the global motion switch, the haptics gate and the session's unit.
    if (Motion.enabled != settings.animations) Motion.enabled = settings.animations
    val outerHaptics = LocalHealthHaptics.current
    val haptics = if (settings.haptics) outerHaptics else HealthHaptics.None
    LaunchedEffect(settings.unit) { session.unit = settings.unit }
    LaunchedEffect(settings.autoRestTimer) { session.autoStartRest = settings.autoRestTimer }

    var toast by remember { mutableStateOf<WorkoutSummary?>(null) }
    LaunchedEffect(toast) { if (toast != null) { delay(3600); toast = null } }
    var lastTab by remember { mutableStateOf(nav.tab) }

    CompositionLocalProvider(LocalAppSettings provides settings, LocalHealthHaptics provides haptics) {
        Box(modifier.fillMaxSize().background(HealthColors.Canvas)) {
            AnimatedContent(
                targetState = nav.current to nav.tab,
                transitionSpec = {
                    val (toRoute, toTab) = targetState
                    val (fromRoute, fromTab) = initialState
                    when {
                        // Detail pushed: it slides in from the right while the page behind settles back.
                        toRoute != null && fromRoute == null -> Motion.pushIn() togetherWith Motion.pushParentOut()
                        // Detail popped: the page behind comes forward while the detail slides away.
                        toRoute == null && fromRoute != null -> Motion.popParentIn() togetherWith Motion.popOut()
                        // Sibling tabs: travel in the direction of the tab bar.
                        else -> {
                            val forward = toTab.ordinal >= fromTab.ordinal
                            Motion.slideIn(forward) togetherWith Motion.slideOut(forward)
                        }
                    }.using(SizeTransform(clip = false))
                },
                label = "root",
                modifier = Modifier.statusBarsPadding(),
            ) { (route, tab) ->
                when (route) {
                    HealthRoute.WeightTrend -> WeightTrendScreen(repository, onBack = { nav.popDetail() })
                    HealthRoute.Settings -> SettingsScreen(onBack = { nav.popDetail() })
                    null -> when (tab) {
                        HealthTab.HOME -> DashboardScreen(
                            repository,
                            onOpenWeight = { haptics.tick(); nav.push(HealthRoute.WeightTrend) },
                            onOpenSettings = { haptics.tick(); nav.push(HealthRoute.Settings) },
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
                enter = Motion.dropIn(),
                exit = Motion.liftOut(),
                modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(horizontal = 20.dp),
            ) {
                val s = toast
                if (s != null) {
                    CelebrationBanner(
                        title = if (s.prs.isEmpty()) "Workout saved" else "${s.prs.size} new PR${if (s.prs.size > 1) "s" else ""}!",
                        subtitle = "${s.totalSets} sets · ${s.totalVolumeLbs.format0()} lbs",
                    )
                }
            }

            AnimatedVisibility(
                visible = nav.current == null,
                enter = Motion.riseIn(),
                exit = Motion.sinkOut(),
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                FloatingTabBar(
                    selected = nav.tab,
                    liveWorkout = session.completedSets > 0 || session.restTimer.active,
                    onSelect = { haptics.tick(); lastTab = nav.tab; nav.selectTab(it) },
                    modifier = Modifier.navigationBarsPadding().padding(start = 20.dp, end = 20.dp, bottom = 14.dp),
                )
            }
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
    HealthTab.HOME -> HealthColors.GreenDeep
    HealthTab.TRAIN -> HealthColors.Terracotta
    HealthTab.SLEEP -> HealthColors.LavenderDeep
    HealthTab.TRENDS -> HealthColors.Sky
}

/** Floating tab bar: flat off-white capsule; a tinted thumb glides to the selected tab. */
@Composable
fun FloatingTabBar(selected: HealthTab, liveWorkout: Boolean, onSelect: (HealthTab) -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(28.dp)
    val accent = selected.accent()
    val thumbColor by animateColorAsState(HealthColors.tint(accent), Motion.quick(), label = "thumbColor")
    val thumbIndex by animateFloatAsState(selected.ordinal.toFloat(), Motion.snappy(), label = "thumbIndex")
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .shadow(14.dp, shape, ambientColor = HealthColors.Ink.copy(alpha = 0.10f), spotColor = HealthColors.Ink.copy(alpha = 0.10f))
            .clip(shape)
            .background(HealthColors.Card)
            .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        val segment = maxWidth / HealthTab.entries.size
        Box(
            Modifier
                .offset(x = segment * thumbIndex)
                .width(segment)
                .height(54.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(thumbColor),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            HealthTab.entries.forEach { tab ->
                val on = tab == selected
                val scale by animateFloatAsState(if (on) 1f else 0.94f, Motion.soft(), label = "tabScale")
                val tint by animateColorAsState(if (on) tab.accent() else HealthColors.Muted, Motion.quick(), label = "tint")
                Column(
                    Modifier
                        .weight(1f)
                        .height(54.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .pressableClick(pressed = 0.92f) { onSelect(tab) }
                        .scale(scale),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box {
                        Icon(tab.icon(), contentDescription = tab.label, tint = tint, modifier = Modifier.size(22.dp))
                        if (tab == HealthTab.TRAIN && liveWorkout) {
                            Box(Modifier.align(Alignment.TopEnd).size(7.dp).clip(CircleShape).background(HealthColors.Terracotta))
                        }
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(tab.label, style = MaterialTheme.typography.labelSmall, color = tint)
                }
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
