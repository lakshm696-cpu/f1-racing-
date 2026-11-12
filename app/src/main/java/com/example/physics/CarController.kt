package com.example.physics

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Direct Kotlin implementation of the user's Unity CarController MonoBehaviour.
 * Faithfully maintains the exact variables, logic, methods, and WheelCollider physics.
 */
class CarController {

    // [Header("Wheel Colliders")]
    val frontLeft = WheelCollider("FL", Vector2D(-0.85f, -1.55f), isFront = true, isLeft = true)
    val frontRight = WheelCollider("FR", Vector2D(0.85f, -1.55f), isFront = true, isLeft = false)
    val rearLeft = WheelCollider("RL", Vector2D(-0.85f, 1.55f), isFront = false, isLeft = true)
    val rearRight = WheelCollider("RR", Vector2D(0.85f, 1.55f), isFront = false, isLeft = false)

    // [Header("Driving")]
    var motorPower: Float = 1800f
    var brakePower: Float = 4000f
    var maxSpeed: Float = 180f
    var steeringAngle: Float = 30f

    // [Header("Handling & Suspension")] (from AdvancedCarController)
    var steeringResponse: Float = 5f
    var tractionLimit: Float = 0.9f
    var springForce: Float = 35000f
    var damperForce: Float = 4500f
    var currentSteering: Float = 0f
        private set

    // Rigidbody
    val rb: F1Rigidbody = F1Rigidbody(mass = 1200f, centerOfMassY = -0.5f)

    // Private inputs
    var steering: Float = 0f
        private set
    var acceleration: Float = 0f
        private set
    var braking: Float = 0f
        private set

    // Public brake state for CarLighting (car.BrakeAmount > 0.05f)
    val BrakeAmount: Float
        get() = braking

    // Aliases matching AdvancedCarController
    var motorTorque: Float
        get() = motorPower
        set(value) { motorPower = value }
    var brakeTorque: Float
        get() = brakePower
        set(value) { brakePower = value }
    var maxSteerAngle: Float
        get() = steeringAngle
        set(value) { steeringAngle = value }
    val throttle: Float
        get() = acceleration
    val brake: Float
        get() = braking

    // Extended F1 Features
    var isDrsActive: Boolean = false
    var isDrsAvailable: Boolean = false
    var isErsActive: Boolean = false
    var ersBatteryPercent: Float = 100f // ERS battery charge

    enum class TireCompound(val label: String, val grip: Float, val wearRate: Float) {
        SOFT("Soft (C5)", 1.25f, 1.5f),
        MEDIUM("Medium (C3)", 1.05f, 1.0f),
        HARD("Hard (C1)", 0.92f, 0.6f),
        WET("Wet", 0.85f, 0.8f)
    }
    var tireCompound: TireCompound = TireCompound.SOFT

    // Gearbox and RPM
    var currentGear: Int = 1
    var engineRpm: Float = 1200f
    val idleRpm: Float = 1200f
    val maxRpm: Float = 15000f

    // Speed property: rb == null ? 0f : rb.linearVelocity.magnitude * 3.6f;
    val Speed: Float
        get() = rb.linearVelocity.magnitude * 3.6f

    fun Start() {
        rb.mass = 1200f
        rb.centerOfMassY = -0.5f
        SetupWheel(frontLeft)
        SetupWheel(frontRight)
        SetupWheel(rearLeft)
        SetupWheel(rearRight)
        reset()
    }

    fun SetupWheel(wheel: WheelCollider?) {
        if (wheel == null) return
        wheel.suspensionSpring.spring = springForce
        wheel.suspensionSpring.damper = damperForce
        wheel.suspensionSpring.targetPosition = 0.5f
    }

    fun ApplyWheelFriction(wheel: WheelCollider?) {
        if (wheel == null) return
        wheel.forwardFriction.stiffness = tractionLimit
    }

    fun reset(startPos: Vector2D = Vector2D.Zero, startHeading: Float = 0f) {
        rb.reset(startPos, startHeading)
        frontLeft.reset()
        frontRight.reset()
        rearLeft.reset()
        rearRight.reset()
        SetupWheel(frontLeft)
        SetupWheel(frontRight)
        SetupWheel(rearLeft)
        SetupWheel(rearRight)
        steering = 0f
        acceleration = 0f
        braking = 0f
        currentSteering = 0f
        isDrsActive = false
        isErsActive = false
        ersBatteryPercent = 100f
        currentGear = 1
        engineRpm = idleRpm
    }

    /**
     * FixedUpdate() physics step.
     */
    fun FixedUpdate(dt: Float = 1f / 60f) {
        val currentSpeed = Speed

        // currentSteering = Mathf.Lerp(currentSteering, steering * maxSteerAngle, steeringResponse * Time.fixedDeltaTime);
        val targetSteer = steering * steeringAngle
        val lerpFactor = (steeringResponse * dt).coerceIn(0f, 1f)
        currentSteering += (targetSteer - currentSteering) * lerpFactor

        frontLeft.steerAngle = currentSteering
        frontRight.steerAngle = currentSteering

        // DRS reduces drag and elevates max speed
        val effectiveMaxSpeed = if (isDrsActive) maxSpeed + 35f else maxSpeed
        val effectiveMotorPower = if (isErsActive && ersBatteryPercent > 1f) motorPower + 600f else motorPower

        // Drain / Recharge ERS
        if (isErsActive && acceleration > 0.1f) {
            ersBatteryPercent = (ersBatteryPercent - 8f * dt).coerceAtLeast(0f)
            if (ersBatteryPercent <= 0f) isErsActive = false
        } else if (braking > 0.2f && currentSpeed > 30f) {
            // Regenerative braking (MGU-K energy harvest)
            ersBatteryPercent = (ersBatteryPercent + 5f * dt).coerceAtMost(100f)
        }

        // float torque = currentSpeed < maxSpeed ? acceleration * motorPower : 0f;
        val torque = if (currentSpeed < effectiveMaxSpeed) acceleration * effectiveMotorPower else 0f

        // rearLeft.motorTorque = torque;
        // rearRight.motorTorque = torque;
        rearLeft.motorTorque = torque
        rearRight.motorTorque = torque

        // float brakeTorque = braking * brakePower;
        val brakeTorque = braking * brakePower

        // frontLeft.brakeTorque = brakeTorque;
        // frontRight.brakeTorque = brakeTorque;
        // rearLeft.brakeTorque = brakeTorque;
        // rearRight.brakeTorque = brakeTorque;
        frontLeft.brakeTorque = brakeTorque
        frontRight.brakeTorque = brakeTorque
        rearLeft.brakeTorque = brakeTorque
        rearRight.brakeTorque = brakeTorque

        // ApplyWheelFriction
        ApplyWheelFriction(rearLeft)
        ApplyWheelFriction(rearRight)
        ApplyWheelFriction(frontLeft)
        ApplyWheelFriction(frontRight)

        // Step wheel physics with traction limit
        val groundSpeedMs = rb.speedMs
        val totalGrip = tireCompound.grip * tractionLimit
        frontLeft.step(groundSpeedMs, dt, totalGrip)
        frontRight.step(groundSpeedMs, dt, totalGrip)
        rearLeft.step(groundSpeedMs, dt, totalGrip)
        rearRight.step(groundSpeedMs, dt, totalGrip)

        // Compute sideways slip for WheelHit & CarEffects smoke
        val yawRateSlip = (abs(rb.angularVelocity) * 0.45f).coerceIn(0f, 1.2f)
        val steerSlip = (abs(currentSteering) / maxSteerAngle * 0.4f * (currentSpeed / 80f)).coerceIn(0f, 1.0f)
        rearLeft.lateralSlip = (yawRateSlip + rearLeft.slipRatio * 0.6f).coerceIn(-1.5f, 1.5f)
        rearRight.lateralSlip = (yawRateSlip + rearRight.slipRatio * 0.6f).coerceIn(-1.5f, 1.5f)
        frontLeft.lateralSlip = (steerSlip + frontLeft.slipRatio * 0.4f).coerceIn(-1.5f, 1.5f)
        frontRight.lateralSlip = (steerSlip + frontRight.slipRatio * 0.4f).coerceIn(-1.5f, 1.5f)

        // Integrate forces onto Rigidbody
        // Driving force from rear wheels
        val totalDriveForce = (rearLeft.motorTorque + rearRight.motorTorque) / 0.35f
        // Braking force from all 4 wheels
        val totalBrakeForce = (frontLeft.brakeTorque + frontRight.brakeTorque +
                rearLeft.brakeTorque + rearRight.brakeTorque) / 0.35f

        // Steering influence on angular velocity
        val steeringInfluence = currentSteering * 2.2f

        // Adjust drag if DRS is active
        rb.dragCoefficient = if (isDrsActive) 0.28f else 0.42f

        rb.step(
            driveForceForward = totalDriveForce,
            brakeForceOpposing = totalBrakeForce,
            steeringInfluence = steeringInfluence,
            gripMultiplier = totalGrip,
            dt = dt
        )

        // UpdateWheel(frontLeft, frontLeftMesh);
        // UpdateWheel(frontRight, frontRightMesh);
        // UpdateWheel(rearLeft, rearLeftMesh);
        // UpdateWheel(rearRight, rearRightMesh);
        UpdateWheel(frontLeft)
        UpdateWheel(frontRight)
        UpdateWheel(rearLeft)
        UpdateWheel(rearRight)

        // Update simulated automatic sequential gearbox & RPM
        updateGearboxAndRpm()
    }

    public fun SetSteering(value: Float) {
        steering = value.coerceIn(-1f, 1f)
    }

    public fun SetThrottle(value: Float) {
        acceleration = value.coerceIn(0f, 1f)
    }

    public fun SetAcceleration(value: Float) {
        SetThrottle(value)
    }

    public fun SetBrake(value: Float) {
        braking = value.coerceIn(0f, 1f)
    }

    fun toggleDrs() {
        if (isDrsAvailable || isDrsActive) {
            isDrsActive = !isDrsActive
        }
    }

    fun toggleErs() {
        if (ersBatteryPercent > 5f) {
            isErsActive = !isErsActive
        }
    }

    /**
     * Unity UpdateWheel helper
     */
    private fun UpdateWheel(wheel: WheelCollider) {
        val (pos, heading) = wheel.getWorldPose(rb.position, rb.headingDegrees)
        wheel.worldPosition = pos
        wheel.worldHeadingDegrees = heading
    }

    private fun updateGearboxAndRpm() {
        val speed = Speed
        // Automatic up/down shifting logic for 8 F1 gears
        val gearSpeeds = floatArrayOf(0f, 65f, 110f, 160f, 210f, 255f, 295f, 335f, 370f)

        var newGear = 1
        for (g in 1..8) {
            if (speed > gearSpeeds[g - 1]) {
                newGear = g
            }
        }
        currentGear = newGear

        // Calculate RPM within current gear
        val prevGearSpeed = gearSpeeds[currentGear - 1]
        val nextGearSpeed = gearSpeeds[currentGear]
        val ratio = ((speed - prevGearSpeed) / (nextGearSpeed - prevGearSpeed).coerceAtLeast(1f)).coerceIn(0f, 1f)

        // Throttle flare adds responsiveness
        val throttleBonus = acceleration * 1500f
        engineRpm = idleRpm + ratio * (maxRpm - idleRpm) * 0.85f + throttleBonus
        engineRpm = engineRpm.coerceIn(idleRpm, maxRpm)
    }
}
