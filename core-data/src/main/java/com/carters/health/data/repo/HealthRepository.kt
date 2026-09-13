package com.carters.health.data.repo

import com.carters.health.data.model.BaselineMetric
import com.carters.health.data.model.DailyActivity
import com.carters.health.data.model.Exercise
import com.carters.health.data.model.ExerciseLibrary
import com.carters.health.data.model.HeartRateSample
import com.carters.health.data.model.OverloadPoint
import com.carters.health.data.model.PersonalRecord
import com.carters.health.data.model.ReadinessSnapshot
import com.carters.health.data.model.SleepNight
import com.carters.health.data.model.SpO2Sample
import com.carters.health.data.model.StressSample
import com.carters.health.data.model.TrainingTotals
import com.carters.health.data.model.WatchStatus
import com.carters.health.data.model.WeightEntry
import com.carters.health.data.model.WeightSource
import com.carters.health.data.model.WorkoutHistoryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime

/**
 * The reactive contract the UI collects from. Today it is backed by [InMemoryHealthRepository];
 * the Room DAOs, BLE watch stream, Sleep as Android provider and scale pipelines will implement
 * this same surface so no screen has to change when real telemetry arrives.
 */
interface HealthRepository {
    val watchStatus: StateFlow<WatchStatus>
    val readiness: StateFlow<ReadinessSnapshot>
    val activity: StateFlow<DailyActivity>
    val liveHeartRate: StateFlow<List<HeartRateSample>>
    val restingHr: StateFlow<BaselineMetric>
    val hrv: StateFlow<BaselineMetric>

    val heartRateDay: StateFlow<List<HeartRateSample>>
    val spo2Day: StateFlow<List<SpO2Sample>>
    val stressDay: StateFlow<List<StressSample>>

    val weightHistory: StateFlow<List<WeightEntry>>
    val latestWeight: Flow<WeightEntry?>

    val sleepNights: StateFlow<List<SleepNight>>

    val exercises: StateFlow<List<Exercise>>
    val workoutHistory: StateFlow<List<WorkoutHistoryEntry>>
    val personalRecords: StateFlow<List<PersonalRecord>>
    val trainingTotals: Flow<TrainingTotals>

    fun overloadCurve(exercise: Exercise): List<OverloadPoint>

    fun logWeight(weightLbs: Double, bodyFatPercent: Double?, source: WeightSource = WeightSource.MANUAL)
    fun addCustomExercise(exercise: Exercise)
    fun pushLiveHeartRate(sample: HeartRateSample)
}

class InMemoryHealthRepository : HealthRepository {
    private val _watchStatus = MutableStateFlow(SampleData.watchStatus())
    override val watchStatus: StateFlow<WatchStatus> = _watchStatus.asStateFlow()

    private val _readiness = MutableStateFlow(SampleData.readiness())
    override val readiness: StateFlow<ReadinessSnapshot> = _readiness.asStateFlow()

    private val _activity = MutableStateFlow(SampleData.activity())
    override val activity: StateFlow<DailyActivity> = _activity.asStateFlow()

    private val _liveHr = MutableStateFlow(SampleData.liveHeartRate())
    override val liveHeartRate: StateFlow<List<HeartRateSample>> = _liveHr.asStateFlow()

    override val restingHr: StateFlow<BaselineMetric> = MutableStateFlow(SampleData.restingHr()).asStateFlow()
    override val hrv: StateFlow<BaselineMetric> = MutableStateFlow(SampleData.hrv()).asStateFlow()

    override val heartRateDay: StateFlow<List<HeartRateSample>> = MutableStateFlow(SampleData.heartRateDay()).asStateFlow()
    override val spo2Day: StateFlow<List<SpO2Sample>> = MutableStateFlow(SampleData.spo2Day()).asStateFlow()
    override val stressDay: StateFlow<List<StressSample>> = MutableStateFlow(SampleData.stressDay()).asStateFlow()

    private val _weights = MutableStateFlow(SampleData.weightHistory())
    override val weightHistory: StateFlow<List<WeightEntry>> = _weights.asStateFlow()
    override val latestWeight: Flow<WeightEntry?> = _weights.map { it.maxByOrNull { e -> e.time } }

    override val sleepNights: StateFlow<List<SleepNight>> = MutableStateFlow(SampleData.sleepNights()).asStateFlow()

    private val _exercises = MutableStateFlow(ExerciseLibrary.defaults)
    override val exercises: StateFlow<List<Exercise>> = _exercises.asStateFlow()

    private val _history = MutableStateFlow(SampleData.workoutHistory())
    override val workoutHistory: StateFlow<List<WorkoutHistoryEntry>> = _history.asStateFlow()
    override val personalRecords: StateFlow<List<PersonalRecord>> = MutableStateFlow(SampleData.personalRecords()).asStateFlow()
    override val trainingTotals: Flow<TrainingTotals> = _history.map { list ->
        TrainingTotals(
            totalVolumeLbs = list.sumOf { it.totalVolumeLbs } + 1_284_300.0,
            totalWorkouts = list.size + 118,
            totalSets = list.sumOf { it.totalSets } + 1_960,
        )
    }

    override fun overloadCurve(exercise: Exercise): List<OverloadPoint> = SampleData.overloadCurve(exercise)

    override fun logWeight(weightLbs: Double, bodyFatPercent: Double?, source: WeightSource) {
        _weights.update { list ->
            val nextId = (list.maxOfOrNull { it.id } ?: 0L) + 1
            list + WeightEntry(nextId, LocalDateTime.now(), weightLbs, bodyFatPercent, source)
        }
    }

    override fun addCustomExercise(exercise: Exercise) {
        _exercises.update { list ->
            val nextId = (list.maxOfOrNull { it.id } ?: 0L) + 1
            list + exercise.copy(id = nextId, isCustom = true)
        }
    }

    override fun pushLiveHeartRate(sample: HeartRateSample) {
        _liveHr.update { (it + sample).takeLast(60) }
    }
}
