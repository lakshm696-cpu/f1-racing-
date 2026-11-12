package com.example.physics

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Models Unity's WheelHit struct storing ground contact and slip telemetry.
 */
data class WheelHit(
    var sidewaysSlip: Float = 0f,
    var forwardSlip: Float = 0f,
    var point: Vector2D = Vector2D.Zero,
    var normal: Vector2D = Vector2D(0f, 1f),
    var force: Float = 0f
)

/**
 * Models the Unity WheelCollider and its attached visual Wheel Mesh Transform.
 * Keeps track of motorTorque, brakeTorque, steerAngle, and world pose.
 */
class WheelCollider(
    val name: String,
    val localOffset: Vector2D, // Offset relative to car center (x = lateral, y = longitudinal)
    val isFront: Boolean,
    val isLeft: Boolean,
    val radius: Float = 0.35f // F1 18-inch wheel approx 0.35m radius
) {
    // Inputs from CarController
    var motorTorque: Float = 0f
    var brakeTorque: Float = 0f
    var steerAngle: Float = 0f // In degrees

    // Suspension Spring & Friction (from AdvancedCarController)
    data class JointSpring(
        var spring: Float = 35000f,
        var damper: Float = 4500f,
        var targetPosition: Float = 0.5f
    )
    var suspensionSpring = JointSpring()

    data class WheelFrictionCurve(
        var stiffness: Float = 0.9f
    )
    var forwardFriction = WheelFrictionCurve()

    // Wheel State
    var angularVelocity: Float = 0f // rad/s
    var rotationAngleDegrees: Float = 0f // Wheel spin for rendering
    var slipRatio: Float = 0f
    var lateralSlip: Float = 0f
    var isSkidding: Boolean = false
    var isGrounded: Boolean = true
    var tireTemperature: Float = 90f // F1 optimal tire temp ~90-105 deg C
    var tireWear: Float = 100f // 100% down to 0%
    var suspensionCompression: Float = 0.5f

    // World pose calculated in UpdateWheel
    var worldPosition: Vector2D = Vector2D.Zero
    var worldHeadingDegrees: Float = 0f

    /**
     * Equivalent to Unity's WheelCollider.GetGroundHit(out WheelHit hit)
     */
    fun GetGroundHit(hit: WheelHit): Boolean {
        hit.sidewaysSlip = lateralSlip
        hit.forwardSlip = slipRatio
        hit.point = worldPosition
        return isGrounded
    }

    fun reset() {
        motorTorque = 0f
        brakeTorque = 0f
        steerAngle = 0f
        suspensionSpring = JointSpring()
        forwardFriction = WheelFrictionCurve()
        suspensionCompression = 0.5f
        angularVelocity = 0f
        rotationAngleDegrees = 0f
        slipRatio = 0f
        lateralSlip = 0f
        isSkidding = false
        isGrounded = true
        tireTemperature = 95f
        tireWear = 100f
    }

    /**
     * Equivalent to Unity's WheelCollider.GetWorldPose
     */
    fun getWorldPose(carPosition: Vector2D, carHeadingDegrees: Float): Pair<Vector2D, Float> {
        val rad = Math.toRadians(carHeadingDegrees.toDouble())
        val cosA = cos(rad).toFloat()
        val sinA = sin(rad).toFloat()

        // Rotate local offset by car heading
        val rotatedOffset = Vector2D(
            x = localOffset.x * cosA - localOffset.y * sinA,
            y = localOffset.x * sinA + localOffset.y * cosA
        )

        val pos = carPosition + rotatedOffset
        val heading = if (isFront) carHeadingDegrees + steerAngle else carHeadingDegrees
        return Pair(pos, heading)
    }

    /**
     * Step the wheel physics with forward speed and applied torques
     */
    fun step(groundSpeed: Float, dt: Float, compoundGripMultiplier: Float = 1.0f) {
        val driveForce = motorTorque / radius
        val maxBrakeForce = (brakeTorque / radius) * compoundGripMultiplier

        // Drive or brake influence on wheel spin
        val targetAngularVelocity = groundSpeed / radius
        
        if (brakeTorque > 100f && groundSpeed > 1f) {
            // Braking
            angularVelocity = (angularVelocity - (maxBrakeForce / 15f) * dt).coerceAtLeast(0f)
            val diff = abs(targetAngularVelocity - angularVelocity)
            slipRatio = (diff / (targetAngularVelocity + 0.1f)).coerceIn(0f, 1f)
            isSkidding = slipRatio > 0.25f
            if (isSkidding) {
                tireTemperature = (tireTemperature + 12f * dt).coerceAtMost(140f)
                tireWear = (tireWear - 0.05f * dt).coerceAtLeast(0f)
            }
        } else if (motorTorque > 100f) {
            // Acceleration
            val accelDelta = (driveForce / 20f) * dt
            angularVelocity += accelDelta
            val diff = angularVelocity - targetAngularVelocity
            slipRatio = (diff / (targetAngularVelocity + 1.0f)).coerceIn(0f, 1f)
            isSkidding = slipRatio > 0.35f
            if (isSkidding) {
                tireTemperature = (tireTemperature + 8f * dt).coerceAtMost(135f)
                tireWear = (tireWear - 0.03f * dt).coerceAtLeast(0f)
            }
        } else {
            // Rolling resistance towards ground speed
            angularVelocity += (targetAngularVelocity - angularVelocity) * 10f * dt
            slipRatio *= 0.8f
            isSkidding = false
            // Cool towards optimal
            tireTemperature += (95f - tireTemperature) * 0.1f * dt
        }

        // Update wheel spin angle for visual rolling
        rotationAngleDegrees = (rotationAngleDegrees + Math.toDegrees(angularVelocity.toDouble()).toFloat() * dt) % 360f
    }
}
