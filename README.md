# Carter's Health

A warm, data-dense Android health tracker built with Jetpack Compose. The interface pairs the
density of Zepp / Whoop with a calm, friendly aesthetic: an off-white canvas, flat beige and
off-grey cards with no borders or gradients, and a small pastel family assigned by meaning:
sage green for recovery and training, terracotta for heart rate and effort, dusty sky for weight,
HRV and blood oxygen, soft lavender for sleep and stress, warm ochre for energy, steps and records.
Two typefaces only: Instrument Serif for headlines and hero numbers,
Nunito for everything else. Fonts are bundled under `ui/src/main/res/font` (SIL OFL).

## Modules

| Module | Purpose |
| --- | --- |
| `:app` | Android shell: `MainActivity`, edge-to-edge, system back, `Vibrator`-backed haptics, process-scoped repository. |
| `:ui` | Every screen and custom Canvas component. Pure Compose with no Android-only APIs so the whole tree renders in previews and on the JVM. |
| `:core-data` | Plain Kotlin models, unit math (lbs ↔ kg, Epley 1RM, BMI), the 36-exercise library, deterministic sample data and the `HealthRepository` contract with an in-memory implementation. |

`:connectivity` (BLE watch / scale, Sleep as Android provider) and `:export` are not yet created;
they will implement `HealthRepository` so no screen changes when real telemetry arrives.

## Screens

- **Home** (`DashboardScreen`) — circadian greeting pill, watch beacon + battery, Zepp triple-pillar dial
  (readiness / strain / sleep), live HR strip with beating heart and 60 s sparkline, spot SpO₂ / stress
  actuation, edge-to-edge bento carousel (weight, steps ring, resting HR, HRV), burn + wind-down tiles.
- **Strength Studio** (`WorkoutScreen`) — three modes:
  - *Active*: chronometer, editable name, sticky rest timer (auto-starts on set completion, +30s / −15s /
    pause / skip, presets, vibration + celebration at zero), exercise tables (warm-up toggle, previous
    benchmark, lbs ↔ kg conversion, reps, RPE, glowing done button that logs est. 1RM), add set /
    exercise bottom sheet, finish modal with volume, sets, reps, duration and PR badges.
  - *Analytics & PRs*: all-time totals, exercise chips, scrubbable overload curve (est. 1RM + heaviest
    set), PR cards, workout history.
  - *Library*: search, muscle-group filters, grouped list, custom exercise dialog.
- **Weight & body** (`WeightTrendScreen`) — hero weight with 30-day delta and source badge, unit
  toggle, 7D / 30D / 90D / 1Y Bézier trend with baseline and scrub tooltip, body-composition bento,
  BLE radar scanner, manual log dialog, weigh-in history.
- **Sleep** (`SleepScreen`) — sleep score gauge, debt, bedtime / wake pills, stage-stepped hypnogram
  from consolidated multi-record nights, stage distribution, date navigation and history.
- **Trends** (`TrendsScreen`) — scrubbable 24 h heart-rate timeline with live readout, RHR / HRV
  baselines, SpO₂ range strip and zones, stress index strip and zone breakdown.

## Component library (`:ui/components`)

`SoftCard`, `ZeppArcDial`, `RadialGauge`, `SegmentedRing`, `CountdownRing`, `PulsingDot`,
`BeatingHeart`, `Sparkline`, `ScrubbableLineChart` (Catmull-Rom → cubic Bézier, drag scrubbing with
haptic ticks), `DistributionBar`, `BarStrip`, `SegmentedControl`, `UnitToggle`, `NumberField`,
`StatTile`, `Pill`, `DeltaBadge`, `PrimaryButton`, `TonalButton`.

## Building

Requires the Android SDK (compileSdk 35) and JDK 17+.

```
./gradlew :app:assembleDebug
```

## Status

UI only. Data is served by `InMemoryHealthRepository` with realistic generated samples. Room,
BLE streaming, Sleep as Android sync and Health Connect are the next layers.
