package com.example.physics

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 2D Vector representation representing positions, velocities, and forces
 * in the physics simulation.
 */
data class Vector2D(
    val x: Float = 0f,
    val y: Float = 0f
) {
    val magnitude: Float
        get() = sqrt(x * x + y * y)

    val sqrMagnitude: Float
        get() = x * x + y * y

    fun normalized(): Vector2D {
        val mag = magnitude
        return if (mag > 0.0001f) Vector2D(x / mag, y / mag) else Vector2D(0f, 0f)
    }

    operator fun plus(other: Vector2D): Vector2D = Vector2D(x + other.x, y + other.y)
    operator fun minus(other: Vector2D): Vector2D = Vector2D(x - other.x, y - other.y)
    operator fun times(scalar: Float): Vector2D = Vector2D(x * scalar, y * scalar)
    operator fun div(scalar: Float): Vector2D = if (scalar != 0f) Vector2D(x / scalar, y / scalar) else Vector2D(0f, 0f)
    operator fun unaryMinus(): Vector2D = Vector2D(-x, -y)

    fun dot(other: Vector2D): Float = x * other.x + y * other.y
    fun cross(other: Vector2D): Float = x * other.y - y * other.x

    fun rotated(degrees: Float): Vector2D {
        val rad = Math.toRadians(degrees.toDouble()).toFloat()
        val c = cos(rad)
        val s = sin(rad)
        return Vector2D(x * c - y * s, x * s + y * c)
    }

    fun angleDegrees(): Float {
        return Math.toDegrees(atan2(y.toDouble(), x.toDouble())).toFloat()
    }

    companion object {
        val Zero = Vector2D(0f, 0f)
        val Up = Vector2D(0f, -1f) // Canvas Y points down, so Up is (0, -1)
        val Down = Vector2D(0f, 1f)
        val Right = Vector2D(1f, 0f)
        val Left = Vector2D(-1f, 0f)

        fun fromAngle(degrees: Float): Vector2D {
            val rad = Math.toRadians(degrees.toDouble())
            return Vector2D(cos(rad).toFloat(), sin(rad).toFloat())
        }
    }
}
