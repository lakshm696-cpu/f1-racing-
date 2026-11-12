package com.example.physics

import androidx.compose.ui.graphics.Color
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Direct Kotlin implementation of user's Unity OpponentAI MonoBehaviour.
 * Drives AI Formula 1 opponents along circuit waypoints with smooth Slerp steering.
 */
class OpponentAI(
    val name: String,
    val driverCode: String,
    val driverNumber: Int,
    val teamColor: Color,
    val helmetColor: Color,
    var waypoints: List<Vector2D> = emptyList(),
    var speed: Float = 25f,
    var turnSpeed: Float = 3f,
    var reachDistance: Float = 5f
) {
    var currentWaypoint: Int = 0
    var position: Vector2D = Vector2D.Zero
    var headingDegrees: Float = -90f

    val speedKmh: Float
        get() = speed * 3.6f

    fun reset(startPos: Vector2D, startHeading: Float, startingWaypoint: Int = 0) {
        position = startPos
        headingDegrees = startHeading
        currentWaypoint = startingWaypoint
    }

    /**
     * Equivalent to Unity Update()
     */
    fun Update(dt: Float) {
        if (waypoints.isEmpty()) return

        val target = waypoints[currentWaypoint]
        val direction = target - position

        if (direction.magnitude < reachDistance) {
            currentWaypoint = (currentWaypoint + 1) % waypoints.size
            return
        }

        // Quaternion targetRotation = Quaternion.LookRotation(direction);
        val targetRotation = Math.toDegrees(atan2(direction.y.toDouble(), direction.x.toDouble())).toFloat()

        // transform.rotation = Quaternion.Slerp(transform.rotation, targetRotation, turnSpeed * Time.deltaTime);
        val diffAngle = shortestAngleDistance(headingDegrees, targetRotation)
        val slerpFactor = (turnSpeed * dt).coerceIn(0f, 1f)
        headingDegrees = (headingDegrees + diffAngle * slerpFactor) % 360f

        // transform.position += transform.forward * speed * Time.deltaTime;
        val rad = Math.toRadians(headingDegrees.toDouble())
        val forward = Vector2D(cos(rad).toFloat(), sin(rad).toFloat())
        position += forward * speed * dt
    }

    private fun shortestAngleDistance(from: Float, to: Float): Float {
        var diff = (to - from) % 360f
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f
        return diff
    }
}
