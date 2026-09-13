package com.carters.health.data.model

/** 30+ pre-populated exercises across all major muscle groups. */
object ExerciseLibrary {
    val defaults: List<Exercise> = buildList {
        var id = 1L
        fun add(name: String, group: MuscleGroup, eq: Equipment, rest: Int = 90) {
            add(Exercise(id++, name, group, eq, rest))
        }
        // Chest
        add("Barbell Bench Press", MuscleGroup.CHEST, Equipment.BARBELL, 150)
        add("Incline Dumbbell Press", MuscleGroup.CHEST, Equipment.DUMBBELL, 120)
        add("Cable Fly", MuscleGroup.CHEST, Equipment.CABLE, 60)
        add("Push-Up", MuscleGroup.CHEST, Equipment.BODYWEIGHT, 60)
        add("Machine Chest Press", MuscleGroup.CHEST, Equipment.MACHINE, 90)
        add("Dumbbell Pullover", MuscleGroup.CHEST, Equipment.DUMBBELL, 90)
        // Back
        add("Conventional Deadlift", MuscleGroup.BACK, Equipment.BARBELL, 180)
        add("Barbell Row", MuscleGroup.BACK, Equipment.BARBELL, 120)
        add("Pull-Up", MuscleGroup.BACK, Equipment.BODYWEIGHT, 120)
        add("Lat Pulldown", MuscleGroup.BACK, Equipment.CABLE, 90)
        add("Seated Cable Row", MuscleGroup.BACK, Equipment.CABLE, 90)
        add("Single-Arm Dumbbell Row", MuscleGroup.BACK, Equipment.DUMBBELL, 90)
        // Legs
        add("Back Squat", MuscleGroup.LEGS, Equipment.BARBELL, 180)
        add("Romanian Deadlift", MuscleGroup.LEGS, Equipment.BARBELL, 150)
        add("Leg Press", MuscleGroup.LEGS, Equipment.MACHINE, 120)
        add("Walking Lunge", MuscleGroup.LEGS, Equipment.DUMBBELL, 90)
        add("Leg Curl", MuscleGroup.LEGS, Equipment.MACHINE, 60)
        add("Leg Extension", MuscleGroup.LEGS, Equipment.MACHINE, 60)
        add("Standing Calf Raise", MuscleGroup.LEGS, Equipment.MACHINE, 60)
        add("Goblet Squat", MuscleGroup.LEGS, Equipment.KETTLEBELL, 90)
        // Shoulders
        add("Overhead Press", MuscleGroup.SHOULDERS, Equipment.BARBELL, 150)
        add("Dumbbell Lateral Raise", MuscleGroup.SHOULDERS, Equipment.DUMBBELL, 60)
        add("Face Pull", MuscleGroup.SHOULDERS, Equipment.CABLE, 60)
        add("Arnold Press", MuscleGroup.SHOULDERS, Equipment.DUMBBELL, 120)
        add("Rear Delt Fly", MuscleGroup.SHOULDERS, Equipment.MACHINE, 60)
        // Arms
        add("Barbell Curl", MuscleGroup.ARMS, Equipment.BARBELL, 60)
        add("Hammer Curl", MuscleGroup.ARMS, Equipment.DUMBBELL, 60)
        add("Triceps Pushdown", MuscleGroup.ARMS, Equipment.CABLE, 60)
        add("Skull Crusher", MuscleGroup.ARMS, Equipment.BARBELL, 90)
        add("Close-Grip Bench Press", MuscleGroup.ARMS, Equipment.BARBELL, 120)
        add("Preacher Curl", MuscleGroup.ARMS, Equipment.MACHINE, 60)
        // Core
        add("Plank", MuscleGroup.CORE, Equipment.BODYWEIGHT, 60)
        add("Hanging Leg Raise", MuscleGroup.CORE, Equipment.BODYWEIGHT, 60)
        add("Cable Crunch", MuscleGroup.CORE, Equipment.CABLE, 60)
        add("Ab Wheel Rollout", MuscleGroup.CORE, Equipment.BODYWEIGHT, 90)
        add("Russian Twist", MuscleGroup.CORE, Equipment.KETTLEBELL, 45)
    }
}
