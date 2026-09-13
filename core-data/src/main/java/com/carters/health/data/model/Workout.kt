package com.carters.health.data.model

import java.time.LocalDate
import java.time.LocalDateTime

enum class MuscleGroup(val label: String) {
    CHEST("Chest"), BACK("Back"), LEGS("Legs"), SHOULDERS("Shoulders"), ARMS("Arms"), CORE("Core");
}

enum class Equipment(val label: String) {
    BARBELL("Barbell"), DUMBBELL("Dumbbell"), CABLE("Cable"), MACHINE("Machine"), BODYWEIGHT("Bodyweight"), KETTLEBELL("Kettlebell");
}

data class Exercise(
    val id: Long,
    val name: String,
    val muscleGroup: MuscleGroup,
    val equipment: Equipment,
    val defaultRestSeconds: Int = 90,
    val isCustom: Boolean = false,
)

/** A benchmark from the previous time this exercise was performed. */
data class PreviousSet(val weightLbs: Double, val reps: Int)

data class WorkoutSet(
    val id: Long,
    val index: Int,
    val isWarmup: Boolean = false,
    val weightLbs: Double? = null,
    val reps: Int? = null,
    val rpe: Double? = null,
    val completed: Boolean = false,
    val previous: PreviousSet? = null,
) {
    val estimatedOneRepMax: Double?
        get() {
            val w = weightLbs ?: return null
            val r = reps ?: return null
            return Units.estimatedOneRepMax(w, r)
        }
    val volumeLbs: Double get() = (weightLbs ?: 0.0) * (reps ?: 0)
}

data class WorkoutExercise(
    val id: Long,
    val exercise: Exercise,
    val sets: List<WorkoutSet>,
)

data class WorkoutSession(
    val id: Long,
    val name: String,
    val startedAt: LocalDateTime,
    val exercises: List<WorkoutExercise>,
)

data class PersonalRecord(
    val exerciseName: String,
    val maxWeightLbs: Double,
    val maxEstimatedOneRepMax: Double,
    val maxVolumeSetLbs: Double,
    val achievedOn: LocalDate,
)

data class OverloadPoint(
    val date: LocalDate,
    val estimatedOneRepMax: Double,
    val heaviestSetLbs: Double,
)

data class WorkoutHistoryEntry(
    val id: Long,
    val name: String,
    val date: LocalDate,
    val durationMinutes: Int,
    val totalSets: Int,
    val totalReps: Int,
    val totalVolumeLbs: Double,
    val exerciseNames: List<String>,
    val prCount: Int,
)

data class WorkoutSummary(
    val totalVolumeLbs: Double,
    val totalSets: Int,
    val totalReps: Int,
    val durationSeconds: Long,
    val prs: List<String>,
)

data class TrainingTotals(
    val totalVolumeLbs: Double,
    val totalWorkouts: Int,
    val totalSets: Int,
)
