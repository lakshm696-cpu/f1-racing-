package com.example.physics

import kotlin.math.abs

/**
 * Direct implementation of the user's Unity AdvancedCameraFollow MonoBehaviour.
 *
 * Features:
 * - Decoupled position follow smoothing (followSmooth = 6f) and rotational smoothing (rotationSmooth = 5f)
 * - Closer F1 low-angle offset: new Vector3(0, 4, -8)
 * - Dynamic velocity-driven Field Of View (FOV): normalFOV (65°) expands to maxFOV (78°) at 180+ km/h
 *   giving a high-speed peripheral tunnel-vision zoom effect.
 */
class AdvancedCameraFollow(
    var target: CarController? = null,
    var targetBody: F1Rigidbody? = target?.rb
) {
    enum class CameraMode {
        CHASE,    // Advanced dynamic chase camera with speed-scaled FOV
        NOSE_CAM, // Cockpit / nose-cone camera
        TOP_DOWN  // Aerial broadcast track map view
    }

    var mode: CameraMode = CameraMode.CHASE

    // public Vector3 offset = new Vector3(0, 4, -8);
    var offsetLateral: Float = 0f  // x
    var offsetHeight: Float = 4f   // y (height above vehicle)
    var offsetDistance: Float = 8f // z (behind vehicle, -8)

    // public float followSmooth = 6f;
    var followSmooth: Float = 6f

    // public float rotationSmooth = 5f;
    var rotationSmooth: Float = 5f

    // public float normalFOV = 65f;
    var normalFOV: Float = 65f

    // public float maxFOV = 78f;
    var maxFOV: Float = 78f

    // Simulated Camera component
    var fieldOfView: Float = 65f
    val currentFov: Float
        get() = fieldOfView

    // Camera transform position and heading in world coordinates
    var position: Vector2D = Vector2D.Zero
    var rotationDegrees: Float = 0f

    // Compatibility aliases with CameraFollow
    var smoothSpeed: Float
        get() = followSmooth
        set(value) {
            followSmooth = value
            rotationSmooth = (value * 0.83f).coerceAtLeast(1f)
        }

    init {
        Start()
    }

    fun Start() {
        if (target != null && targetBody == null) {
            targetBody = target?.rb
        }
        fieldOfView = normalFOV
        reset()
    }

    fun reset() {
        val car = target
        if (car != null) {
            val carPos = car.rb.position
            val carHeading = car.rb.headingDegrees
            position = carPos
            rotationDegrees = carHeading
        } else {
            position = Vector2D.Zero
            rotationDegrees = 0f
        }
        fieldOfView = normalFOV
    }

    /**
     * Unity LateUpdate() implementation
     */
    fun LateUpdate(deltaTime: Float) {
        val car = target ?: return
        val rb = targetBody ?: car.rb
        val dt = deltaTime.coerceIn(0.001f, 0.1f)

        when (mode) {
            CameraMode.CHASE -> {
                // Vector3 desiredPosition = target.TransformPoint(offset);
                // In world 2D plane: car forward & right vectors
                val fwd = rb.forward()
                val rgt = rb.right()
                val desiredPosition = rb.position - (fwd * offsetDistance) + (rgt * offsetLateral)

                // transform.position = Vector3.Lerp(transform.position, desiredPosition, followSmooth * Time.deltaTime);
                val followFactor = (followSmooth * dt).coerceIn(0f, 1f)
                position = position + (desiredPosition - position) * followFactor

                // Vector3 lookPoint = target.position + Vector3.up * 1.3f;
                // Quaternion desiredRotation = Quaternion.LookRotation(lookPoint - transform.position);
                // transform.rotation = Quaternion.Slerp(transform.rotation, desiredRotation, rotationSmooth * Time.deltaTime);
                val targetHeading = rb.headingDegrees
                val diffAngle = shortestAngleDistance(rotationDegrees, targetHeading)
                val rotationFactor = (rotationSmooth * dt).coerceIn(0f, 1f)
                rotationDegrees = (rotationDegrees + diffAngle * rotationFactor) % 360f

                // float speed = targetBody != null ? targetBody.linearVelocity.magnitude * 3.6f : 0f;
                val speed = rb.linearVelocity.magnitude * 3.6f

                // float targetFOV = Mathf.Lerp(normalFOV, maxFOV, Mathf.Clamp01(speed / 180f));
                val speedNormalized = (speed / 180f).coerceIn(0f, 1f)
                val targetFOV = normalFOV + (maxFOV - normalFOV) * speedNormalized

                // cam.fieldOfView = Mathf.Lerp(cam.fieldOfView, targetFOV, 3f * Time.deltaTime);
                val fovLerpFactor = (3f * dt).coerceIn(0f, 1f)
                fieldOfView += (targetFOV - fieldOfView) * fovLerpFactor
            }
            CameraMode.NOSE_CAM -> {
                val fwd = rb.forward()
                position = rb.position + (fwd * 1.8f)
                rotationDegrees = rb.headingDegrees
                fieldOfView = 75f
            }
            CameraMode.TOP_DOWN -> {
                val followFactor = (followSmooth * dt).coerceIn(0f, 1f)
                position = position + (rb.position - position) * followFactor
                rotationDegrees = -90f // North locked
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
