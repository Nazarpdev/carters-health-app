package com.carters.health.ui.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Top-level destinations reachable from the bottom bar. */
enum class HealthTab(val label: String) {
    HOME("Home"), TRAIN("Train"), SLEEP("Sleep"), TRENDS("Trends");
}

/** Detail destinations pushed on top of a tab. */
sealed interface HealthRoute {
    data object WeightTrend : HealthRoute
}

/**
 * Minimal, dependency-free navigation state: a selected tab plus a detail back stack.
 * The :app module wires system back to [popDetail]; this keeps :ui free of Android-only deps.
 */
@Stable
class HealthNavState(initialTab: HealthTab = HealthTab.HOME) {
    var tab by mutableStateOf(initialTab)
        private set
    private val stack = mutableStateListOf<HealthRoute>()

    val current: HealthRoute? get() = stack.lastOrNull()
    val canGoBack: Boolean get() = stack.isNotEmpty()

    fun selectTab(target: HealthTab) {
        stack.clear()
        tab = target
    }

    fun push(route: HealthRoute) {
        if (stack.lastOrNull() != route) stack.add(route)
    }

    fun popDetail(): Boolean {
        if (stack.isEmpty()) return false
        stack.removeAt(stack.lastIndex)
        return true
    }
}
