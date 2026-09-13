package com.carters.health.data.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

/** Connection state of the paired Amazfit / ZeppOS watch. */
data class WatchStatus(
    val connected: Boolean,
    val deviceName: String,
    val batteryPercent: Int,
    val lastSync: Instant,
)

/** The three "pillars" rendered on the dashboard triple-arc dial. */
data class ReadinessSnapshot(
    val readinessPercent: Int,        // 0..100
    val strain: Double,               // 0.0..21.0
    val sleepPerformancePercent: Int, // 0..100
    val sleepDurationMinutes: Int,
    val hrvMs: Int,
    val restingHr: Int,
)

data class HeartRateSample(val time: Instant, val bpm: Int)

data class DailyActivity(
    val steps: Int,
    val stepGoal: Int,
    val distanceKm: Double,
    val activeCalories: Int,
    val totalCalories: Int,
)

enum class WeightSource(val label: String) {
    FITBIT_SCALE("Fitbit Scale"),
    BLE_SCALE("BLE Scale"),
    MANUAL("Manual"),
}

data class WeightEntry(
    val id: Long,
    val time: LocalDateTime,
    val weightLbs: Double,
    val bodyFatPercent: Double?,
    val source: WeightSource,
)

data class BodyComposition(
    val weightLbs: Double,
    val bodyFatPercent: Double,
    val heightCm: Double,
) {
    val bmi: Double get() = Units.bmi(weightLbs, heightCm)
    val leanMassLbs: Double get() = weightLbs * (1 - bodyFatPercent / 100.0)
    val bmiCategory: String
        get() = when {
            bmi < 18.5 -> "Underweight"
            bmi < 25.0 -> "Normal"
            bmi < 30.0 -> "Overweight"
            else -> "Obese"
        }
    val bodyFatCategory: String
        get() = when {
            bodyFatPercent < 6 -> "Essential"
            bodyFatPercent < 14 -> "Athletic"
            bodyFatPercent < 18 -> "Fitness Range"
            bodyFatPercent < 25 -> "Average"
            else -> "Above Average"
        }
}

data class BaselineMetric(
    val current: Int,
    val sevenDayAverage: Double,
)

enum class SleepStage(val label: String) {
    DEEP("Deep"), REM("REM"), LIGHT("Light"), AWAKE("Awake");
}

data class SleepSegment(
    val start: LocalDateTime,
    val end: LocalDateTime,
    val stage: SleepStage,
) {
    val minutes: Long get() = java.time.Duration.between(start, end).toMinutes()
}

/** A consolidated night: fragmented Sleep as Android records merged into one. */
data class SleepNight(
    val date: LocalDate,
    val segments: List<SleepSegment>,
    val score: Int,
    val sleepDebtMinutes: Int,
    val sourceRecordCount: Int,
) {
    val start: LocalDateTime get() = segments.first().start
    val end: LocalDateTime get() = segments.last().end
    val totalMinutes: Long get() = segments.filter { it.stage != SleepStage.AWAKE }.sumOf { it.minutes }
    val timeInBedMinutes: Long get() = java.time.Duration.between(start, end).toMinutes()
    fun minutesIn(stage: SleepStage): Long = segments.filter { it.stage == stage }.sumOf { it.minutes }
}

data class SpO2Sample(val time: Instant, val percent: Int)
data class StressSample(val time: Instant, val level: Int) // 0..100

data class StressZones(val relaxed: Int, val normal: Int, val medium: Int, val high: Int) {
    val total: Int get() = relaxed + normal + medium + high
}
