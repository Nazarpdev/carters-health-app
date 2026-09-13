package com.carters.health.data.repo

import com.carters.health.data.model.BaselineMetric
import com.carters.health.data.model.BodyComposition
import com.carters.health.data.model.DailyActivity
import com.carters.health.data.model.Exercise
import com.carters.health.data.model.ExerciseLibrary
import com.carters.health.data.model.HeartRateSample
import com.carters.health.data.model.OverloadPoint
import com.carters.health.data.model.PersonalRecord
import com.carters.health.data.model.PreviousSet
import com.carters.health.data.model.ReadinessSnapshot
import com.carters.health.data.model.SleepNight
import com.carters.health.data.model.SleepSegment
import com.carters.health.data.model.SleepStage
import com.carters.health.data.model.SpO2Sample
import com.carters.health.data.model.StressSample
import com.carters.health.data.model.StressZones
import com.carters.health.data.model.Units
import com.carters.health.data.model.WatchStatus
import com.carters.health.data.model.WeightEntry
import com.carters.health.data.model.WeightSource
import com.carters.health.data.model.WorkoutExercise
import com.carters.health.data.model.WorkoutHistoryEntry
import com.carters.health.data.model.WorkoutSession
import com.carters.health.data.model.WorkoutSet
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * Deterministic, believable demo data so every screen is fully populated before the
 * Room / BLE / Health Connect pipelines are wired in. Everything is generated relative
 * to the supplied [now] so timelines always look "live".
 */
object SampleData {
    private val zone: ZoneId = ZoneId.systemDefault()

    fun watchStatus(now: Instant = Instant.now()) = WatchStatus(
        connected = true,
        deviceName = "Amazfit Balance",
        batteryPercent = 72,
        lastSync = now.minusSeconds(95),
    )

    fun readiness() = ReadinessSnapshot(
        readinessPercent = 84,
        strain = 11.4,
        sleepPerformancePercent = 71,
        sleepDurationMinutes = 334,
        hrvMs = 62,
        restingHr = 52,
    )

    fun activity() = DailyActivity(
        steps = 7_420,
        stepGoal = 10_000,
        distanceKm = 5.6,
        activeCalories = 486,
        totalCalories = 2_140,
    )

    fun restingHr() = BaselineMetric(current = 52, sevenDayAverage = 54.3)
    fun hrv() = BaselineMetric(current = 62, sevenDayAverage = 58.1)

    /** Last 60 seconds of live HR at 1 Hz. */
    fun liveHeartRate(now: Instant = Instant.now()): List<HeartRateSample> {
        val rnd = Random(7)
        return (59 downTo 0).map { secondsAgo ->
            val t = secondsAgo.toDouble()
            val bpm = 68 + (6 * sin(t / 9.0)).toInt() + rnd.nextInt(-2, 3)
            HeartRateSample(now.minusSeconds(secondsAgo.toLong()), bpm)
        }
    }

    /** Continuous 24-hour HR curve sampled every 5 minutes. */
    fun heartRateDay(now: Instant = Instant.now()): List<HeartRateSample> {
        val rnd = Random(21)
        val start = now.truncatedTo(ChronoUnit.HOURS).minus(23, ChronoUnit.HOURS)
        val points = 24 * 12
        return (0 until points).map { i ->
            val time = start.plusSeconds(i * 300L)
            val hour = time.atZone(zone).hour + time.atZone(zone).minute / 60.0
            val base = when {
                hour < 6.5 -> 50 + 3 * sin(hour * PI / 3)
                hour < 8.0 -> 58 + (hour - 6.5) * 12
                hour < 12.0 -> 74 + 5 * sin(hour * 1.7)
                hour < 13.5 -> 82 + 4 * sin(hour * 3)
                hour < 17.0 -> 76 + 4 * sin(hour * 2.1)
                hour < 18.2 -> 120 + 25 * sin((hour - 17.0) * PI / 1.2) // workout
                hour < 22.0 -> 72 - (hour - 18.2) * 3
                else -> 60 - (hour - 22.0) * 2
            }
            HeartRateSample(time, (base + rnd.nextInt(-3, 4)).toInt().coerceIn(44, 178))
        }
    }

    fun spo2Day(now: Instant = Instant.now()): List<SpO2Sample> {
        val rnd = Random(4)
        val start = now.minus(23, ChronoUnit.HOURS)
        return (0 until 48).map { i ->
            SpO2Sample(start.plusSeconds(i * 1800L), (96 + rnd.nextInt(0, 4)).coerceAtMost(99))
        }
    }

    fun stressDay(now: Instant = Instant.now()): List<StressSample> {
        val rnd = Random(11)
        val start = now.minus(23, ChronoUnit.HOURS)
        return (0 until 96).map { i ->
            val t = start.plusSeconds(i * 900L)
            val hour = t.atZone(zone).hour
            val base = when (hour) {
                in 0..6 -> 18
                in 7..9 -> 34
                in 10..12 -> 46
                in 13..15 -> 58
                in 16..18 -> 40
                else -> 26
            }
            StressSample(t, (base + rnd.nextInt(-8, 9)).coerceIn(5, 95))
        }
    }

    fun stressZones(samples: List<StressSample> = stressDay()): StressZones {
        var r = 0; var n = 0; var m = 0; var h = 0
        samples.forEach {
            when {
                it.level < 25 -> r++
                it.level < 50 -> n++
                it.level < 75 -> m++
                else -> h++
            }
        }
        return StressZones(r * 15, n * 15, m * 15, h * 15)
    }

    /** 365 days of weigh-ins trending gently downward, sourced from both scale pipelines. */
    fun weightHistory(today: LocalDate = LocalDate.now()): List<WeightEntry> {
        val rnd = Random(3)
        val entries = ArrayList<WeightEntry>()
        var id = 1L
        for (daysAgo in 365 downTo 0) {
            if (daysAgo % 2 == 1 && daysAgo > 30) continue // sparser older data
            val trend = 176.0 - (365 - daysAgo) * 0.0195
            val wave = 1.2 * sin(daysAgo / 11.0)
            val noise = rnd.nextDouble(-0.9, 0.9)
            val weight = Units.roundTo(trend + wave + noise, 1)
            val fat = Units.roundTo(17.9 - (365 - daysAgo) * 0.0035 + rnd.nextDouble(-0.4, 0.4), 1)
            val source = when {
                daysAgo % 7 == 0 -> WeightSource.BLE_SCALE
                daysAgo % 13 == 0 -> WeightSource.MANUAL
                else -> WeightSource.FITBIT_SCALE
            }
            entries += WeightEntry(
                id = id++,
                time = LocalDateTime.of(today.minusDays(daysAgo.toLong()), LocalTime.of(7, 5 + rnd.nextInt(0, 40))),
                weightLbs = weight,
                bodyFatPercent = fat,
                source = source,
            )
        }
        return entries
    }

    fun bodyComposition(latest: WeightEntry) = BodyComposition(
        weightLbs = latest.weightLbs,
        bodyFatPercent = latest.bodyFatPercent ?: 17.0,
        heightCm = 183.0,
    )

    /** Fourteen nights of consolidated sleep, built from 2–4 fragmented source records each. */
    fun sleepNights(today: LocalDate = LocalDate.now()): List<SleepNight> {
        val rnd = Random(9)
        return (0 until 14).map { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            val bedtime = LocalDateTime.of(date.minusDays(1), LocalTime.of(22 + rnd.nextInt(0, 2), rnd.nextInt(0, 59)))
            val segments = ArrayList<SleepSegment>()
            var cursor = bedtime
            val cycles = 4 + rnd.nextInt(0, 2)
            segments += SleepSegment(cursor, cursor.plusMinutes(8L + rnd.nextInt(0, 10)), SleepStage.AWAKE).also { cursor = it.end }
            repeat(cycles) { c ->
                val light1 = 20L + rnd.nextInt(0, 20)
                val deep = if (c < 2) 35L + rnd.nextInt(0, 25) else 8L + rnd.nextInt(0, 12)
                val light2 = 15L + rnd.nextInt(0, 15)
                val rem = if (c < 2) 10L + rnd.nextInt(0, 10) else 25L + rnd.nextInt(0, 25)
                segments += SleepSegment(cursor, cursor.plusMinutes(light1), SleepStage.LIGHT).also { cursor = it.end }
                segments += SleepSegment(cursor, cursor.plusMinutes(deep), SleepStage.DEEP).also { cursor = it.end }
                segments += SleepSegment(cursor, cursor.plusMinutes(light2), SleepStage.LIGHT).also { cursor = it.end }
                segments += SleepSegment(cursor, cursor.plusMinutes(rem), SleepStage.REM).also { cursor = it.end }
                if (rnd.nextInt(0, 3) == 0) {
                    segments += SleepSegment(cursor, cursor.plusMinutes(3L + rnd.nextInt(0, 6)), SleepStage.AWAKE).also { cursor = it.end }
                }
            }
            segments += SleepSegment(cursor, cursor.plusMinutes(5L + rnd.nextInt(0, 6)), SleepStage.AWAKE)
            val night = SleepNight(date, segments, score = 0, sleepDebtMinutes = 0, sourceRecordCount = 2 + rnd.nextInt(0, 3))
            val asleep = night.totalMinutes
            val deepShare = night.minutesIn(SleepStage.DEEP).toDouble() / asleep
            val score = (asleep / 480.0 * 70 + deepShare * 120).toInt().coerceIn(35, 98)
            night.copy(score = score, sleepDebtMinutes = (480 - asleep).toInt().coerceAtLeast(0))
        }
    }

    // ---------- Training ----------

    private val bench = ExerciseLibrary.defaults.first { it.name == "Barbell Bench Press" }
    private val incline = ExerciseLibrary.defaults.first { it.name == "Incline Dumbbell Press" }
    private val fly = ExerciseLibrary.defaults.first { it.name == "Cable Fly" }
    private val pushdown = ExerciseLibrary.defaults.first { it.name == "Triceps Pushdown" }

    /** A freshly started push session with previous-session benchmarks pre-filled. */
    fun activeSession(now: LocalDateTime = LocalDateTime.now()): WorkoutSession {
        var setId = 1L
        fun sets(prev: List<Pair<Double, Int>>, warmups: Int = 1): List<WorkoutSet> {
            val list = ArrayList<WorkoutSet>()
            var index = 1
            repeat(warmups) {
                list += WorkoutSet(id = setId++, index = 0, isWarmup = true, weightLbs = prev.first().first * 0.5, reps = 10, previous = null)
            }
            prev.forEach { (w, r) ->
                list += WorkoutSet(id = setId++, index = index++, previous = PreviousSet(w, r))
            }
            return list
        }
        return WorkoutSession(
            id = 1,
            name = "Push Day - Chest & Triceps",
            startedAt = now.minusMinutes(42).minusSeconds(15),
            exercises = listOf(
                WorkoutExercise(1, bench, sets(listOf(225.0 to 5, 225.0 to 5, 215.0 to 6, 205.0 to 8))),
                WorkoutExercise(2, incline, sets(listOf(70.0 to 10, 70.0 to 9, 65.0 to 10), warmups = 0)),
                WorkoutExercise(3, fly, sets(listOf(35.0 to 12, 35.0 to 12, 30.0 to 15), warmups = 0)),
                WorkoutExercise(4, pushdown, sets(listOf(60.0 to 12, 60.0 to 12, 55.0 to 15), warmups = 0)),
            ),
        )
    }

    /** Twelve weeks of progressive-overload points for a given exercise. */
    fun overloadCurve(exercise: Exercise, today: LocalDate = LocalDate.now()): List<OverloadPoint> {
        val rnd = Random(exercise.id.toInt())
        val startWeight = when (exercise.muscleGroup) {
            com.carters.health.data.model.MuscleGroup.LEGS -> 275.0
            com.carters.health.data.model.MuscleGroup.BACK -> if (exercise.equipment == com.carters.health.data.model.Equipment.BARBELL) 295.0 else 150.0
            com.carters.health.data.model.MuscleGroup.CHEST -> 195.0
            com.carters.health.data.model.MuscleGroup.SHOULDERS -> 105.0
            com.carters.health.data.model.MuscleGroup.ARMS -> 75.0
            com.carters.health.data.model.MuscleGroup.CORE -> 45.0
        }
        return (0 until 16).map { i ->
            val date = today.minusWeeks((15 - i).toLong())
            val heaviest = startWeight + i * 2.5 + rnd.nextInt(-1, 2) * 5
            val reps = 4 + rnd.nextInt(0, 4)
            OverloadPoint(date, Units.estimatedOneRepMax(heaviest, reps), heaviest)
        }
    }

    fun personalRecords(today: LocalDate = LocalDate.now()): List<PersonalRecord> = listOf(
        PersonalRecord("Barbell Bench Press", 235.0, 262.5, 1_125.0, today.minusDays(4)),
        PersonalRecord("Back Squat", 315.0, 346.5, 1_575.0, today.minusDays(9)),
        PersonalRecord("Conventional Deadlift", 385.0, 410.7, 1_925.0, today.minusDays(2)),
        PersonalRecord("Overhead Press", 145.0, 159.5, 725.0, today.minusDays(16)),
    )

    fun workoutHistory(today: LocalDate = LocalDate.now()): List<WorkoutHistoryEntry> {
        val rnd = Random(5)
        val names = listOf("Push Day - Chest & Triceps", "Pull Day - Back & Biceps", "Leg Day - Squat Focus", "Upper Hypertrophy", "Lower Hypertrophy")
        val exercisesFor = mapOf(
            names[0] to listOf("Barbell Bench Press", "Incline Dumbbell Press", "Cable Fly", "Triceps Pushdown"),
            names[1] to listOf("Conventional Deadlift", "Pull-Up", "Barbell Row", "Hammer Curl"),
            names[2] to listOf("Back Squat", "Romanian Deadlift", "Leg Press", "Standing Calf Raise"),
            names[3] to listOf("Overhead Press", "Lat Pulldown", "Arnold Press", "Preacher Curl"),
            names[4] to listOf("Goblet Squat", "Walking Lunge", "Leg Curl", "Hanging Leg Raise"),
        )
        return (0 until 24).map { i ->
            val name = names[i % names.size]
            val sets = 14 + rnd.nextInt(0, 8)
            val reps = sets * (6 + rnd.nextInt(0, 6))
            WorkoutHistoryEntry(
                id = (i + 1).toLong(),
                name = name,
                date = today.minusDays((i * 2 + 1).toLong()),
                durationMinutes = 48 + rnd.nextInt(0, 30),
                totalSets = sets,
                totalReps = reps,
                totalVolumeLbs = (sets * (95 + rnd.nextInt(0, 60)) * 8).toDouble(),
                exerciseNames = exercisesFor.getValue(name),
                prCount = if (rnd.nextInt(0, 4) == 0) 1 + rnd.nextInt(0, 2) else 0,
            )
        }
    }
}
