package com.carters.health.data.model

import kotlin.math.roundToInt

enum class WeightUnit(val label: String) {
    LBS("lbs"), KG("kg");

    fun other(): WeightUnit = if (this == LBS) KG else LBS
}

object Units {
    const val LBS_PER_KG = 2.2046226218

    fun lbsToKg(lbs: Double): Double = lbs / LBS_PER_KG
    fun kgToLbs(kg: Double): Double = kg * LBS_PER_KG

    /** Converts a weight stored in lbs into the requested display unit. */
    fun displayWeight(lbs: Double, unit: WeightUnit): Double =
        if (unit == WeightUnit.LBS) lbs else lbsToKg(lbs)

    /** Converts a user-entered value in [unit] back to canonical lbs. */
    fun toLbs(value: Double, unit: WeightUnit): Double =
        if (unit == WeightUnit.LBS) value else kgToLbs(value)

    /** Epley estimated one-rep max: weight × (1 + reps / 30). */
    fun estimatedOneRepMax(weightLbs: Double, reps: Int): Double =
        if (reps <= 1) weightLbs else weightLbs * (1.0 + reps / 30.0)

    fun bmi(weightLbs: Double, heightCm: Double): Double {
        val kg = lbsToKg(weightLbs)
        val m = heightCm / 100.0
        return kg / (m * m)
    }

    fun roundTo(value: Double, decimals: Int): Double {
        var factor = 1.0
        repeat(decimals) { factor *= 10.0 }
        return (value * factor).roundToInt() / factor
    }
}

fun Double.format1(): String = String.format("%.1f", this)
fun Double.format0(): String = String.format("%,d", this.roundToInt())
