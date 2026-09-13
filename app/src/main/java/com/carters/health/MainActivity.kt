package com.carters.health

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.carters.health.data.repo.SampleData
import com.carters.health.ui.HealthApp
import com.carters.health.ui.freshSession
import com.carters.health.ui.navigation.HealthNavState
import com.carters.health.ui.screens.workout.WorkoutSessionState
import com.carters.health.ui.settings.LocalAppSettings
import com.carters.health.ui.theme.HealthTheme
import com.carters.health.ui.theme.LocalHealthHaptics

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as CartersHealthApplication
        val repository = app.repository
        val settings = app.settings
        val haptics = AndroidHaptics(this)
        setContent {
            HealthTheme(darkTheme = settings.isDark(isSystemInDarkTheme())) {
                CompositionLocalProvider(LocalHealthHaptics provides haptics, LocalAppSettings provides settings) {
                    val nav = remember { HealthNavState() }
                    var session by remember { mutableStateOf(WorkoutSessionState(SampleData.activeSession())) }
                    BackHandler(enabled = nav.canGoBack) { nav.popDetail() }
                    HealthApp(
                        repository = repository,
                        nav = nav,
                        session = session,
                        onWorkoutFinished = { session = WorkoutSessionState(freshSession()) },
                    )
                }
            }
        }
    }
}
