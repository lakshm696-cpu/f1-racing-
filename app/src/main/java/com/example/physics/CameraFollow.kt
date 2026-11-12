package com.example.physics

import kotlin.math.abs

/**
 * Direct implementation of the user's Unity CameraFollow MonoBehaviour.
 * Provides smooth position interpolation (Vector3.Lerp) and rotation (Quaternion.Slerp)
 * following behind the car with offset (0, 5, -9) and smoothSpeed = 5f.
 */
class CameraFollow(
    val car: CarController
) {
    enum class CameraMode {
        CHASE,   // Unity CameraFollow with offset (0, 5, -9)
        NOSE_CAM,// F1 Onboard nose-cone camera
        TOP_DOWN // Fixed aerial broadcast map view
    }

    var mode: CameraMode = CameraMode.CHASE

    // public Vector3 offset = new Vector3(0, 4, -8); (Advanced) or (0, 5, -9) (Standard)
    var offsetLateral: Float = 0f
    var offsetHeight: Float = 4f
    var offsetDistance: Float = 8f // behind car

    // Decoupled smooth speeds (AdvancedCameraFollow.cs)
    var followSmooth: Float = 6f
    var rotationSmooth: Float = 5f

    // Field Of View parameters (AdvancedCameraFollow.cs)
    var normalFOV: Float = 65f
    var maxFOV: Float = 78f
    var fieldOfView: Float = 65f

    // Toggle between Advanced and Classic CameraFollow
    var isAdvancedMode: Boolean = true

    // public float smoothSpeed = 5f; (classic alias)
    var smoothSpeed: Float
        get() = followSmooth
        set(value) {
            followSmooth = value
            rotationSmooth = (value * 0.83f).coerceAtLeast(1f)
        }

    // Current camera world position and rotation
    var position: Vector2D = Vector2D.Zero
    var rotationDegrees: Float = 0f
    val currentFov: Float
        get() = fieldOfView

    fun reset() {
        val carPos = car.rb.position
        val carHeading = car.rb.headingDegrees
        position = carPos
        rotationDegrees = carHeading
        fieldOfView = normalFOV
    }

    /**
     * Equivalent to Unity's LateUpdate()
     */
    fun LateUpdate(dt: Float) {
        val targetPos = car.rb.position
        val targetHeading = car.rb.headingDegrees
        val speedKmh = car.Speed

        when (mode) {
            CameraMode.CHASE -> {
                // Vector3 desiredPosition = target.TransformPoint(offset);
                // Behind the car by offsetDistance and offsetLateral
                val fwd = car.rb.forward()
                val rgt = car.rb.right()
                val desiredPosition = targetPos - (fwd * offsetDistance) + (rgt * offsetLateral)

                // transform.position = Vector3.Lerp(transform.position, desiredPosition, followSmooth * Time.deltaTime);
                val posLerpFactor = ((if (isAdvancedMode) followSmooth else smoothSpeed) * dt).coerceIn(0f, 1f)
                position = position + (desiredPosition - position) * posLerpFactor

                // Quaternion lookRotation = Quaternion.LookRotation(target.position + Vector3.up * 1.3f - transform.position);
                // transform.rotation = Quaternion.Slerp(transform.rotation, lookRotation, rotationSmooth * Time.deltaTime);
                val diffAngle = shortestAngleDistance(rotationDegrees, targetHeading)
                val rotLerpFactor = ((if (isAdvancedMode) rotationSmooth else smoothSpeed) * dt).coerceIn(0f, 1f)
                rotationDegrees = (rotationDegrees + diffAngle * rotLerpFactor) % 360f

                // Dynamic FOV (AdvancedCameraFollow.cs):
                // float targetFOV = Mathf.Lerp(normalFOV, maxFOV, Mathf.Clamp01(speed / 180f));
                // cam.fieldOfView = Mathf.Lerp(cam.fieldOfView, targetFOV, 3f * Time.deltaTime);
                val speedRatio = (speedKmh / 180f).coerceIn(0f, 1f)
                val targetFOV = normalFOV + (maxFOV - normalFOV) * speedRatio
                val fovLerpFactor = (3f * dt).coerceIn(0f, 1f)
                fieldOfView += (targetFOV - fieldOfView) * fovLerpFactor
            }
            CameraMode.NOSE_CAM -> {
                // Follow tight to nose
                val fwd = car.rb.forward()
                position = targetPos + (fwd * 1.8f)
                rotationDegrees = targetHeading
                fieldOfView = 75f
            }
            CameraMode.TOP_DOWN -> {
                // Smooth position tracking with fixed north orientation
                val lerpFactor = (smoothSpeed * dt).coerceIn(0f, 1f)
                position = position + (targetPos - position) * lerpFactor
                rotationDegrees = -90f // North facing
                fieldOfView = 50f
            }
        }
    }

    fun toggleCameraMode() {
        mode = when (mode) {
            CameraMode.CHASE -> CameraMode.NOSE_CAM
            CameraMode.NOSE_CAM -> CameraMode.TOP_DOWN
            CameraMode.TOP_DOWN -> CameraMode.CHASE
        }
    }

    private fun shortestAngleDistance(from: Float, to: Float): Float {
        var diff = (to - from) % 360f
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f
        return diff
    }
}
