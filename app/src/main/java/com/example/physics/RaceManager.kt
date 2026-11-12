package com.example.physics

import kotlin.math.roundToInt

/**
 * Direct Kotlin implementation of the user's Unity RaceManager MonoBehaviour.
 * Manages playerCar, speedText, lapText, timerText, statusText, totalLaps (default 3),
 * currentLap, raceTime, raceFinished, Start(), Update(), CompleteLap(), and UpdateLap().
 */
class RaceManager(
    val playerCar: CarController,
    var totalLaps: Int = 3
) {
    var currentLap: Int = 1
        private set
    var raceTime: Float = 0f
        private set
    var raceFinished: Boolean = false
        private set

    // Display strings equivalent to TextMeshProUGUI
    var speedText: String = "0 km/h"
        private set
    var lapText: String = "LAP 1/3"
        private set
    var timerText: String = "0.00 s"
        private set
    var statusText: String = "GO!"
        private set

    // Lap split history
    val lapSplitTimes = mutableListOf<Float>()
    var bestLapTime: Float? = null
        private set
    private var currentLapStartTime: Float = 0f

    init {
        Start()
    }

    /**
     * Equivalent to Unity Start()
     */
    fun Start() {
        currentLap = 1
        raceTime = 0f
        raceFinished = false
        currentLapStartTime = 0f
        lapSplitTimes.clear()
        bestLapTime = null
        UpdateLap()
        statusText = "GO!"
    }

    fun reset(laps: Int = totalLaps) {
        totalLaps = laps
        Start()
    }

    /**
     * Equivalent to Unity Update()
     */
    fun Update(dt: Float) {
        if (raceFinished) return

        raceTime += dt

        // speedText.text = Mathf.RoundToInt(playerCar.Speed) + " km/h";
        speedText = "${playerCar.Speed.roundToInt()} km/h"

        // timerText.text = raceTime.ToString("F2") + " s";
        timerText = String.format("%.2f s", raceTime)

        // Reset temporary "GO!" status after 3.5 seconds
        if (statusText == "GO!" && raceTime > 3.5f) {
            statusText = ""
        }
    }

    /**
     * Equivalent to Unity CompleteLap()
     */
    fun CompleteLap() {
        if (raceFinished) return

        val lapDuration = raceTime - currentLapStartTime
        currentLapStartTime = raceTime
        lapSplitTimes.add(lapDuration)

        if (bestLapTime == null || lapDuration < bestLapTime!!) {
            bestLapTime = lapDuration
        }

        if (currentLap >= totalLaps) {
            raceFinished = true
            statusText = "RACE FINISHED! Time: " + String.format("%.2f s", raceTime)
        } else {
            currentLap++
            UpdateLap()
            statusText = "LAP $currentLap!"
        }
    }

    /**
     * Equivalent to Unity UpdateLap()
     */
    private fun UpdateLap() {
        lapText = "LAP $currentLap/$totalLaps"
    }

    fun currentLapDuration(): Float {
        return if (raceFinished) {
            lapSplitTimes.lastOrNull() ?: 0f
        } else {
            (raceTime - currentLapStartTime).coerceAtLeast(0f)
        }
    }
}
