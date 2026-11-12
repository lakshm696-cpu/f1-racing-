package com.example.physics

/**
 * Direct Kotlin implementation of the user's Unity LapTrigger MonoBehaviour.
 * Detects when a collider tagged "Player" enters the trigger zone and calls raceManager.CompleteLap().
 */
class LapTrigger(
    val raceManager: RaceManager,
    var position: Vector2D = Vector2D.Zero,
    var trackWidth: Float = 18f,
    var normalDirection: Vector2D = Vector2D(0f, -1f) // Direction across the finish line
) {
    private var lastCarPosition: Vector2D? = null
    private var isArmed: Boolean = true // Prevents multi-triggers during a single crossing

    fun reset(pos: Vector2D, width: Float, normal: Vector2D) {
        position = pos
        trackWidth = width
        normalDirection = normal.normalized()
        lastCarPosition = null
        isArmed = true
    }

    /**
     * Equivalent to Unity's OnTriggerEnter(Collider other)
     * Checks if the player car passed through the start/finish trigger line segment.
     */
    fun checkCrossing(carPos: Vector2D, isPlayerTag: Boolean = true) {
        if (!isPlayerTag) return

        val prevPos = lastCarPosition
        lastCarPosition = carPos

        if (prevPos == null) return

        // Segment 1: car path from prevPos to carPos
        // Segment 2: trigger line from triggerLeft to triggerRight
        val triggerTangent = Vector2D(-normalDirection.y, normalDirection.x)
        val halfW = trackWidth / 2f
        val triggerLeft = position - triggerTangent * halfW
        val triggerRight = position + triggerTangent * halfW

        if (linesIntersect(prevPos, carPos, triggerLeft, triggerRight)) {
            if (isArmed) {
                // Equivalent to: if (other.CompareTag("Player")) { raceManager.CompleteLap(); }
                onTriggerEnter()
                isArmed = false
            }
        } else {
            // Re-arm trigger once the car moves away from the trigger zone
            val distSq = (carPos - position).sqrMagnitude
            if (distSq > (trackWidth * 1.5f) * (trackWidth * 1.5f)) {
                isArmed = true
            }
        }
    }

    /**
     * Direct equivalent of OnTriggerEnter(Collider other)
     */
    fun onTriggerEnter() {
        raceManager.CompleteLap()
    }

    private fun linesIntersect(p1: Vector2D, p2: Vector2D, p3: Vector2D, p4: Vector2D): Boolean {
        fun ccw(a: Vector2D, b: Vector2D, c: Vector2D): Boolean {
            return (c.y - a.y) * (b.x - a.x) > (b.y - a.y) * (c.x - a.x)
        }
        return (ccw(p1, p3, p4) != ccw(p2, p3, p4)) && (ccw(p1, p2, p3) != ccw(p1, p2, p4))
    }
}
