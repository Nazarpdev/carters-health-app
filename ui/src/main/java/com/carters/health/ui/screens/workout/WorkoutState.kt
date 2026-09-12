package com.carters.health.ui.screens.workout

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.carters.health.data.model.Exercise
import com.carters.health.data.model.PreviousSet
import com.carters.health.data.model.Units
import com.carters.health.data.model.WeightUnit
import com.carters.health.data.model.WorkoutSession
import com.carters.health.data.model.WorkoutSummary
import java.time.Duration
import java.time.LocalDateTime

/** One editable row in the exercise table. */
@Stable
class SetState(
    val id: Long,
    isWarmup: Boolean,
    weightLbs: Double?,
    reps: Int?,
    rpe: Double?,
    val previous: PreviousSet?,
) {
    var isWarmup by mutableStateOf(isWarmup)
    var weightLbs by mutableStateOf(weightLbs)
    var reps by mutableStateOf(reps)
    var rpe by mutableStateOf(rpe)
    var completed by mutableStateOf(false)

    val estimatedOneRepMax: Double?
        get() = weightLbs?.let { w -> reps?.let { r -> Units.estimatedOneRepMax(w, r) } }
    val volumeLbs: Double get() = (weightLbs ?: 0.0) * (reps ?: 0)

    /** Autofills from the previous benchmark if the user completes an empty row. */
    fun adoptPreviousIfEmpty() {
        val p = previous ?: return
        if (weightLbs == null) weightLbs = p.weightLbs
        if (reps == null) reps = p.reps
    }
}

@Stable
class ExerciseState(val id: Long, val exercise: Exercise, initial: List<SetState>) {
    val sets: SnapshotStateList<SetState> = mutableStateListOf<SetState>().apply { addAll(initial) }

    /** Working-set index (warmups are labelled W and don't count). */
    fun indexLabel(set: SetState): String {
        if (set.isWarmup) return "W"
        var n = 0
        for (s in sets) {
            if (!s.isWarmup) n++
            if (s === set) break
        }
        return n.toString()
    }

    fun addSet(nextId: Long) {
        val last = sets.lastOrNull()
        sets += SetState(
            id = nextId,
            isWarmup = false,
            weightLbs = last?.weightLbs,
            reps = last?.reps,
            rpe = null,
            previous = last?.previous,
        )
    }
}

/**
 * Countdown for rest intervals. Ticks on wall-clock time so backgrounding the app doesn't drift.
 * Auto-started by [WorkoutSessionState.completeSet]; the UI observes [finishedAt] to celebrate.
 */
@Stable
class RestTimerState {
    var totalSeconds by mutableStateOf(0)
        private set
    var remainingMillis by mutableStateOf(0L)
        private set
    var running by mutableStateOf(false)
        private set
    var paused by mutableStateOf(false)
        private set
    /** Bumped every time the timer hits zero; the UI keys a celebration on it. */
    var finishedAt by mutableStateOf<Long?>(null)
        private set

    private var endAtMillis = 0L

    val active: Boolean get() = running || paused
    val fraction: Float get() = if (totalSeconds == 0) 0f else (remainingMillis / 1000f / totalSeconds).coerceIn(0f, 1f)
    val remainingSeconds: Int get() = ((remainingMillis + 999) / 1000).toInt().coerceAtLeast(0)

    fun start(seconds: Int, now: Long = System.currentTimeMillis()) {
        totalSeconds = seconds
        remainingMillis = seconds * 1000L
        endAtMillis = now + remainingMillis
        running = true
        paused = false
    }

    fun adjust(deltaSeconds: Int, now: Long = System.currentTimeMillis()) {
        if (!active) return
        remainingMillis = (remainingMillis + deltaSeconds * 1000L).coerceAtLeast(0L)
        totalSeconds = (totalSeconds + deltaSeconds).coerceAtLeast(1)
        endAtMillis = now + remainingMillis
        if (remainingMillis == 0L) finish(now)
    }

    fun togglePause(now: Long = System.currentTimeMillis()) {
        if (!active) return
        if (paused) {
            endAtMillis = now + remainingMillis
            paused = false
            running = true
        } else {
            paused = true
            running = false
        }
    }

    fun skip() {
        running = false
        paused = false
        remainingMillis = 0
        totalSeconds = 0
    }

    /** Called from a frame loop while [running]. */
    fun tick(now: Long = System.currentTimeMillis()) {
        if (!running) return
        remainingMillis = (endAtMillis - now).coerceAtLeast(0L)
        if (remainingMillis == 0L) finish(now)
    }

    private fun finish(now: Long) {
        running = false
        paused = false
        finishedAt = now
    }
}

/** The live workout: name, chronometer origin, exercises, unit preference and the rest timer. */
@Stable
class WorkoutSessionState(session: WorkoutSession) {
    var name by mutableStateOf(session.name)
    val startedAt: LocalDateTime = session.startedAt
    var unit by mutableStateOf(WeightUnit.LBS)
    val restTimer = RestTimerState()
    val exercises: SnapshotStateList<ExerciseState> = mutableStateListOf()
    var lastCompletedExerciseName by mutableStateOf<String?>(null)
        private set

    private var nextId = 1000L

    init {
        session.exercises.forEach { we ->
            exercises += ExerciseState(
                we.id, we.exercise,
                we.sets.map { s -> SetState(s.id, s.isWarmup, s.weightLbs, s.reps, s.rpe, s.previous) },
            )
        }
    }

    val completedSets: Int get() = exercises.sumOf { e -> e.sets.count { it.completed && !it.isWarmup } }
    val totalReps: Int get() = exercises.sumOf { e -> e.sets.filter { it.completed && !it.isWarmup }.sumOf { it.reps ?: 0 } }
    val totalVolumeLbs: Double get() = exercises.sumOf { e -> e.sets.filter { it.completed && !it.isWarmup }.sumOf { it.volumeLbs } }
    val elapsed: Duration get() = Duration.between(startedAt, LocalDateTime.now())

    /** Toggles completion; a newly completed set kicks off the exercise's rest interval. */
    fun toggleSet(exercise: ExerciseState, set: SetState) {
        if (set.completed) {
            set.completed = false
            return
        }
        set.adoptPreviousIfEmpty()
        set.completed = true
        lastCompletedExerciseName = exercise.exercise.name
        restTimer.start(exercise.exercise.defaultRestSeconds)
    }

    fun addSet(exercise: ExerciseState) = exercise.addSet(nextId++)

    fun removeSet(exercise: ExerciseState, set: SetState) {
        exercise.sets.remove(set)
    }

    fun addExercise(exercise: Exercise) {
        val id = nextId++
        exercises += ExerciseState(
            id, exercise,
            listOf(SetState(nextId++, false, null, null, null, null)),
        )
    }

    fun removeExercise(exercise: ExerciseState) {
        exercises.remove(exercise)
    }

    /** PR detection against the previous benchmark: any completed set beating its previous e1RM. */
    fun personalRecords(): List<String> = exercises.mapNotNull { e ->
        val bestPrev = e.sets.mapNotNull { it.previous }.maxOfOrNull { Units.estimatedOneRepMax(it.weightLbs, it.reps) } ?: return@mapNotNull null
        val bestNow = e.sets.filter { it.completed && !it.isWarmup }.mapNotNull { it.estimatedOneRepMax }.maxOrNull() ?: return@mapNotNull null
        if (bestNow > bestPrev + 0.01) e.exercise.name else null
    }

    fun summary(): WorkoutSummary = WorkoutSummary(
        totalVolumeLbs = totalVolumeLbs,
        totalSets = completedSets,
        totalReps = totalReps,
        durationSeconds = elapsed.seconds,
        prs = personalRecords(),
    )
}

internal fun formatClock(totalSeconds: Long): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) String.format("%02d:%02d:%02d", h, m, s) else String.format("%02d:%02d", m, s)
}

internal fun formatWeight(lbs: Double?, unit: WeightUnit): String {
    if (lbs == null) return ""
    val v = Units.displayWeight(lbs, unit)
    return if (v == Math.floor(v)) String.format("%.0f", v) else String.format("%.1f", v)
}
