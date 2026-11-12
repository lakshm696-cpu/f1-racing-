package com.example.audio

import com.example.physics.AdvancedCarController
import com.example.physics.CarController

/**
 * Direct implementation of the user's Unity CarAudio MonoBehaviour.
 *
 * Controls real-time engine pitch and volume based on vehicle velocity:
 * - speedFactor = Mathf.Clamp01(car.Speed / 180f)
 * - source.pitch = Mathf.Lerp(idlePitch, maxPitch, speedFactor)
 * - source.volume = Mathf.Lerp(0.3f, 1f, speedFactor)
 */
class CarAudio(
    var car: AdvancedCarController? = null
) {
    // Secondary constructor accepting CarController
    constructor(classicCar: CarController) : this(null) {
        this.classicCar = classicCar
    }

    private var classicCar: CarController? = null

    // Unity AudioClip placeholder representation
    var hasEngineSoundClip: Boolean = true

    // [Header("Pitch Settings")]
    var idlePitch: Float = 0.8f
    var maxPitch: Float = 2.0f

    // Simulated Unity AudioSource component
    val source: SimulatedAudioSource = SimulatedAudioSource()

    // Helper property to fetch speed from either vehicle controller
    val carSpeed: Float
        get() = car?.Speed ?: classicCar?.Speed ?: 0f

    init {
        Start()
    }

    fun Start() {
        source.loop = true
        source.playOnAwake = false
        source.spatialBlend = 1f
        source.pitch = idlePitch
        source.volume = 0.3f

        if (hasEngineSoundClip) {
            source.Play()
        }
    }

    /**
     * Unity Update() method
     */
    fun Update() {
        if (!hasEngineSoundClip) return

        // float speedFactor = Mathf.Clamp01(car.Speed / 180f);
        val speedFactor = (carSpeed / 180f).coerceIn(0f, 1f)

        // source.pitch = Mathf.Lerp(idlePitch, maxPitch, speedFactor);
        source.pitch = idlePitch + (maxPitch - idlePitch) * speedFactor

        // source.volume = Mathf.Lerp(0.3f, 1f, speedFactor);
        source.volume = 0.3f + (1.0f - 0.3f) * speedFactor
    }

    class SimulatedAudioSource {
        var loop: Boolean = true
        var playOnAwake: Boolean = false
        var spatialBlend: Float = 1f
        var isPlaying: Boolean = false
        var pitch: Float = 0.8f
        var volume: Float = 0.3f

        fun Play() {
            isPlaying = true
        }

        fun Stop() {
            isPlaying = false
        }

        fun Pause() {
            isPlaying = false
        }
    }
}
