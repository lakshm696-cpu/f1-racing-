package com.example.physics

import kotlin.math.abs
import kotlin.random.Random

/**
 * Direct implementation of the user's Unity CarEffects MonoBehaviour.
 *
 * Monitors rear wheel sideways slip via WheelHit. If sidewaysSlip > smokeThreshold (0.4f),
 * triggers simulated tire smoke ParticleSystems billowing behind the rear wheels.
 */
class CarEffects(
    var rearLeft: WheelCollider? = null,
    var rearRight: WheelCollider? = null
) {
    // Simulated Unity ParticleSystems
    val leftSmoke: SimulatedParticleSystem = SimulatedParticleSystem("RearLeftSmoke")
    val rightSmoke: SimulatedParticleSystem = SimulatedParticleSystem("RearRightSmoke")

    // public float smokeThreshold = 0.4f;
    var smokeThreshold: Float = 0.4f

    // Total active particles across both rear wheels
    val totalActiveParticles: Int
        get() = leftSmoke.particles.size + rightSmoke.particles.size

    val isAnySmokeActive: Boolean
        get() = leftSmoke.isPlaying || rightSmoke.isPlaying

    init {
        Start()
    }

    fun Start() {
        leftSmoke.stop()
        rightSmoke.stop()
    }

    /**
     * Unity Update()
     */
    fun Update(dt: Float = 1f / 60f) {
        UpdateSmoke(rearLeft, leftSmoke)
        UpdateSmoke(rearRight, rightSmoke)

        // Step physics simulation for smoke particle drift & fading
        rearLeft?.let { leftSmoke.step(it.worldPosition, dt) }
        rearRight?.let { rightSmoke.step(it.worldPosition, dt) }
    }

    fun UpdateSmoke(
        wheel: WheelCollider?,
        smoke: SimulatedParticleSystem?
    ) {
        if (wheel == null || smoke == null)
            return

        val hit = WheelHit()
        val grounded = wheel.GetGroundHit(hit)

        val slipping = grounded &&
            abs(hit.sidewaysSlip) > smokeThreshold

        if (slipping && !smoke.isPlaying)
            smoke.Play()

        if (!slipping && smoke.isPlaying)
            smoke.Stop()
    }

    /**
     * Simulated Unity ParticleSystem emitting billowing tire smoke puffs.
     */
    class SimulatedParticleSystem(val name: String) {
        var isPlaying: Boolean = false
            private set

        val particles = mutableListOf<SmokeParticle>()
        private var emitTimer = 0f

        data class SmokeParticle(
            var position: Vector2D,
            var velocity: Vector2D,
            var radius: Float,
            var alpha: Float,
            var age: Float = 0f,
            val maxLife: Float = 0.65f
        )

        fun Play() {
            isPlaying = true
        }

        fun Stop() {
            isPlaying = false
        }

        fun stop() = Stop()
        fun play() = Play()

        fun clear() {
            particles.clear()
            isPlaying = false
        }

        fun step(emissionOrigin: Vector2D, dt: Float) {
            // Emit new particles while playing
            if (isPlaying) {
                emitTimer += dt
                if (emitTimer >= 0.035f) { // ~30 particles/sec
                    emitTimer = 0f
                    val driftX = (Random.nextFloat() - 0.5f) * 1.5f
                    val driftY = (Random.nextFloat() - 0.5f) * 1.5f
                    if (particles.size < 40) {
                        particles.add(
                            SmokeParticle(
                                position = emissionOrigin + Vector2D((Random.nextFloat() - 0.5f) * 0.4f, (Random.nextFloat() - 0.5f) * 0.4f),
                                velocity = Vector2D(driftX, driftY),
                                radius = 0.45f,
                                alpha = 0.65f,
                                maxLife = 0.5f + Random.nextFloat() * 0.3f
                            )
                        )
                    }
                }
            }

            // Update existing particles
            val iterator = particles.iterator()
            while (iterator.hasNext()) {
                val p = iterator.next()
                p.age += dt
                if (p.age >= p.maxLife) {
                    iterator.remove()
                } else {
                    p.position = p.position + (p.velocity * dt)
                    val progress = p.age / p.maxLife
                    // Smoke expands as it billows and dissipates
                    p.radius = 0.45f + progress * 1.4f
                    p.alpha = (1f - progress) * 0.6f
                }
            }
        }
    }
}
