package com.example.physics

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Models the Unity Rigidbody component for vehicle dynamics.
 * rb.mass = 1200f
 * rb.centerOfMass = new Vector3(0, -0.5f, 0)
 */
class F1Rigidbody(
    var mass: Float = 1200f,
    var centerOfMassY: Float = -0.5f
) {
    var position: Vector2D = Vector2D.Zero
    var linearVelocity: Vector2D = Vector2D.Zero
    var headingDegrees: Float = 0f
    var angularVelocityDegrees: Float = 0f

    val angularVelocity: Float
        get() = Math.toRadians(angularVelocityDegrees.toDouble()).toFloat()

    // Aerodynamic constants
    var dragCoefficient: Float = 0.42f
    var downforceCoefficient: Float = 1.25f

    // Telemetry tracking
    var longitudinalG: Float = 0f
    var lateralG: Float = 0f
    private var lastLinearVelocity: Vector2D = Vector2D.Zero

    val speedMs: Float
        get() = linearVelocity.magnitude

    val speedKmh: Float
        get() = speedMs * 3.6f

    fun reset(startPos: Vector2D = Vector2D.Zero, startHeading: Float = 0f) {
        position = startPos
        headingDegrees = startHeading
        linearVelocity = Vector2D.Zero
        angularVelocityDegrees = 0f
        longitudinalG = 0f
        lateralG = 0f
        lastLinearVelocity = Vector2D.Zero
    }

    /**
     * Compute forward unit vector based on car heading
     */
    fun forward(): Vector2D {
        val rad = Math.toRadians(headingDegrees.toDouble())
        return Vector2D(cos(rad).toFloat(), sin(rad).toFloat())
    }

    /**
     * Compute right lateral unit vector based on car heading
     */
    fun right(): Vector2D {
        val rad = Math.toRadians((headingDegrees + 90f).toDouble())
        return Vector2D(cos(rad).toFloat(), sin(rad).toFloat())
    }

    /**
     * Step the rigidbody physics integrating forces and velocities
     */
    fun step(
        driveForceForward: Float,
        brakeForceOpposing: Float,
        steeringInfluence: Float,
        gripMultiplier: Float,
        dt: Float
    ) {
        val fwd = forward()
        val rgt = right()

        // Decompose velocity into forward and lateral components
        val vForward = linearVelocity.dot(fwd)
        val vLateral = linearVelocity.dot(rgt)

        // Aerodynamic drag force: F_drag = 0.5 * rho * Cd * A * v^2
        val airDrag = 0.5f * 1.225f * dragCoefficient * 1.6f * (vForward * vForward) * (if (vForward >= 0f) 1f else -1f)

        // Net forward force
        var netForwardForce = driveForceForward - airDrag
        if (brakeForceOpposing > 0f) {
            val brakeSign = if (vForward > 0.05f) 1f else if (vForward < -0.05f) -1f else 0f
            netForwardForce -= brakeForceOpposing * brakeSign
        }

        // Apply rolling resistance
        if (abs(vForward) > 0.1f) {
            netForwardForce -= 150f * (if (vForward > 0f) 1f else -1f)
        }

        // Forward acceleration (F = m * a)
        val forwardAcc = netForwardForce / mass
        var newVForward = vForward + forwardAcc * dt

        // Prevent oscillating or reversing due to brake
        if (brakeForceOpposing > 0f && (vForward > 0 && newVForward < 0 || vForward < 0 && newVForward > 0)) {
            newVForward = 0f
        }

        // Downforce increases cornering grip at speed
        val aerodynamicGripBonus = (vForward * vForward * 0.0006f * downforceCoefficient).coerceAtMost(0.85f)
        val totalLateralFriction = (8.5f * gripMultiplier * (1f + aerodynamicGripBonus))

        // Lateral dampening (tire grip counteracting sideways slide)
        val newVLateral = vLateral * (1f - (totalLateralFriction * dt).coerceIn(0f, 0.95f))

        // Reconstruct world linear velocity
        val newLinearVelocity = fwd * newVForward + rgt * newVLateral

        // Angular velocity calculation based on steering, forward speed, and wheelbase
        // Only steer effectively when moving
        val speedFactor = (abs(newVForward) / 12f).coerceIn(0f, 1f)
        val targetAngularVelocity = steeringInfluence * speedFactor * (if (newVForward >= 0f) 1f else -1f)
        angularVelocityDegrees += (targetAngularVelocity - angularVelocityDegrees) * 12f * dt

        // Heading integration
        headingDegrees = (headingDegrees + angularVelocityDegrees * dt) % 360f
        if (headingDegrees < 0f) headingDegrees += 360f

        // Position integration
        position += newLinearVelocity * dt

        // Calculate G-Forces for telemetry
        val accelerationVector = (newLinearVelocity - lastLinearVelocity) / (dt.coerceAtLeast(0.001f))
        val gConst = 9.81f
        longitudinalG = (accelerationVector.dot(fwd) / gConst).coerceIn(-6f, 4f)
        lateralG = (accelerationVector.dot(rgt) / gConst).coerceIn(-6f, 6f)

        lastLinearVelocity = linearVelocity
        linearVelocity = newLinearVelocity
    }
}
