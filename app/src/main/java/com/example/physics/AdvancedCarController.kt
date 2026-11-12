package com.example.physics

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Direct Kotlin implementation of the user's Unity AdvancedCarController MonoBehaviour.
 * Provides advanced suspension springs (35000N/m), damping (4500Ns/m), traction limit (0.9),
 * and smoothed steering response (Mathf.Lerp at 5f/s).
 */
class AdvancedCarController {

    // [Header("Wheel Colliders")]
    val frontLeft = WheelCollider("FL", Vector2D(-0.85f, -1.55f), isFront = true, isLeft = true)
    val frontRight = WheelCollider("FR", Vector2D(0.85f, -1.55f), isFront = true, isLeft = false)
    val rearLeft = WheelCollider("RL", Vector2D(-0.85f, 1.55f), isFront = false, isLeft = true)
    val rearRight = WheelCollider("RR", Vector2D(0.85f, 1.55f), isFront = false, isLeft = false)

    // [Header("Vehicle Settings")]
    var motorTorque: Float = 1800f
    var brakeTorque: Float = 4000f
    var maxSpeed: Float = 180f
    var maxSteerAngle: Float = 30f

    // [Header("Handling")]
    var steeringResponse: Float = 5f
    var tractionLimit: Float = 0.9f

    // [Header("Suspension")]
    var springForce: Float = 35000f
    var damperForce: Float = 4500f

    // Rigidbody
    val rb: F1Rigidbody = F1Rigidbody(mass = 1200f, centerOfMassY = -0.5f)

    // Private inputs
    var steering: Float = 0f
        private set
    var throttle: Float = 0f
        private set
    var brake: Float = 0f
        private set
    var currentSteering: Float = 0f
        private set

    // Extended F1 Features
    var isDrsActive: Boolean = false
    var isDrsAvailable: Boolean = false
    var isErsActive: Boolean = false
    var ersBatteryPercent: Float = 100f
    var tireCompound: CarController.TireCompound = CarController.TireCompound.SOFT

    // Gearbox and RPM
    var currentGear: Int = 1
    var engineRpm: Float = 1200f
    val idleRpm: Float = 1200f
    val maxRpm: Float = 15000f

    // Speed property: rb.linearVelocity.magnitude * 3.6f;
    val Speed: Float
        get() = rb.linearVelocity.magnitude * 3.6f

    // Compatibility aliases with CarController
    var motorPower: Float
        get() = motorTorque
        set(value) { motorTorque = value }
    var brakePower: Float
        get() = brakeTorque
        set(value) { brakeTorque = value }
    var steeringAngle: Float
        get() = maxSteerAngle
        set(value) { maxSteerAngle = value }
    val acceleration: Float
        get() = throttle
    val braking: Float
        get() = brake
    val BrakeAmount: Float
        get() = brake

    init {
        Awake()
    }

    fun Awake() {
        rb.mass = 1200f
        rb.centerOfMassY = -0.5f

        SetupWheel(frontLeft)
        SetupWheel(frontRight)
        SetupWheel(rearLeft)
        SetupWheel(rearRight)
    }

    fun Start() {
        Awake()
        reset()
    }

    fun SetupWheel(wheel: WheelCollider?) {
        if (wheel == null) return

        wheel.suspensionSpring.spring = springForce
        wheel.suspensionSpring.damper = damperForce
        wheel.suspensionSpring.targetPosition = 0.5f
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
        throttle = 0f
        brake = 0f
        currentSteering = 0f
        isDrsActive = false
        isErsActive = false
        ersBatteryPercent = 100f
        currentGear = 1
        engineRpm = idleRpm
    }

    /**
     * FixedUpdate()
     */
    fun FixedUpdate(fixedDeltaTime: Float = 1f / 60f) {
        val speed = Speed

        // currentSteering = Mathf.Lerp(currentSteering, steering * maxSteerAngle, steeringResponse * Time.fixedDeltaTime);
        val targetSteer = steering * maxSteerAngle
        val lerpFactor = (steeringResponse * fixedDeltaTime).coerceIn(0f, 1f)
        currentSteering += (targetSteer - currentSteering) * lerpFactor

        // frontLeft.steerAngle = currentSteering;
        // frontRight.steerAngle = currentSteering;
        frontLeft.steerAngle = currentSteering
        frontRight.steerAngle = currentSteering

        // DRS reduces drag and elevates max speed
        val effectiveMaxSpeed = if (isDrsActive) maxSpeed + 35f else maxSpeed
        val effectiveMotorTorque = if (isErsActive && ersBatteryPercent > 1f) motorTorque + 600f else motorTorque

        // Drain / Recharge ERS
        if (isErsActive && throttle > 0.1f) {
            ersBatteryPercent = (ersBatteryPercent - 8f * fixedDeltaTime).coerceAtLeast(0f)
            if (ersBatteryPercent <= 0f) isErsActive = false
        } else if (brake > 0.2f && speed > 30f) {
            ersBatteryPercent = (ersBatteryPercent + 5f * fixedDeltaTime).coerceAtMost(100f)
        }

        // float torque = speed < maxSpeed ? throttle * motorTorque : 0f;
        val torque = if (speed < effectiveMaxSpeed) throttle * effectiveMotorTorque else 0f

        // rearLeft.motorTorque = torque;
        // rearRight.motorTorque = torque;
        rearLeft.motorTorque = torque
        rearRight.motorTorque = torque

        // float brakingForce = brake * brakeTorque;
        val brakingForce = brake * brakeTorque

        // frontLeft.brakeTorque = brakingForce;
        // frontRight.brakeTorque = brakingForce;
        // rearLeft.brakeTorque = brakingForce;
        // rearRight.brakeTorque = brakingForce;
        frontLeft.brakeTorque = brakingForce
        frontRight.brakeTorque = brakingForce
        rearLeft.brakeTorque = brakingForce
        rearRight.brakeTorque = brakingForce

        // ApplyWheelFriction(rearLeft);
        // ApplyWheelFriction(rearRight);
        ApplyWheelFriction(rearLeft)
        ApplyWheelFriction(rearRight)
        ApplyWheelFriction(frontLeft)
        ApplyWheelFriction(frontRight)

        // Step wheel physics with tractionLimit and compound grip
        val groundSpeedMs = rb.speedMs
        val totalGrip = tireCompound.grip * tractionLimit
        frontLeft.step(groundSpeedMs, fixedDeltaTime, totalGrip)
        frontRight.step(groundSpeedMs, fixedDeltaTime, totalGrip)
        rearLeft.step(groundSpeedMs, fixedDeltaTime, totalGrip)
        rearRight.step(groundSpeedMs, fixedDeltaTime, totalGrip)

        // Compute sideways slip for WheelHit & CarEffects smoke
        val yawRateSlip = (abs(rb.angularVelocity) * 0.45f).coerceIn(0f, 1.2f)
        val steerSlip = (abs(currentSteering) / maxSteerAngle * 0.4f * (speed / 80f)).coerceIn(0f, 1.0f)
        rearLeft.lateralSlip = (yawRateSlip + rearLeft.slipRatio * 0.6f).coerceIn(-1.5f, 1.5f)
        rearRight.lateralSlip = (yawRateSlip + rearRight.slipRatio * 0.6f).coerceIn(-1.5f, 1.5f)
        frontLeft.lateralSlip = (steerSlip + frontLeft.slipRatio * 0.4f).coerceIn(-1.5f, 1.5f)
        frontRight.lateralSlip = (steerSlip + frontRight.slipRatio * 0.4f).coerceIn(-1.5f, 1.5f)

        // Integrate forces onto Rigidbody
        val totalDriveForce = (rearLeft.motorTorque + rearRight.motorTorque) / 0.35f
        val totalBrakeForce = (frontLeft.brakeTorque + frontRight.brakeTorque +
                rearLeft.brakeTorque + rearRight.brakeTorque) / 0.35f

        val steeringInfluence = currentSteering * 2.2f
        rb.dragCoefficient = if (isDrsActive) 0.28f else 0.42f

        rb.step(
            driveForceForward = totalDriveForce,
            brakeForceOpposing = totalBrakeForce,
            steeringInfluence = steeringInfluence,
            gripMultiplier = totalGrip,
            dt = fixedDeltaTime
        )

        // UpdateWheel(frontLeft, frontLeftMesh);
        // UpdateWheel(frontRight, frontRightMesh);
        // UpdateWheel(rearLeft, rearLeftMesh);
        // UpdateWheel(rearRight, rearRightMesh);
        UpdateWheel(frontLeft)
        UpdateWheel(frontRight)
        UpdateWheel(rearLeft)
        UpdateWheel(rearRight)

        updateGearboxAndRpm()
    }

    fun ApplyWheelFriction(wheel: WheelCollider?) {
        if (wheel == null) return
        wheel.forwardFriction.stiffness = tractionLimit
    }

    fun UpdateWheel(wheel: WheelCollider?) {
        if (wheel == null) return
        val (pos, heading) = wheel.getWorldPose(rb.position, rb.headingDegrees)
        wheel.worldPosition = pos
        wheel.worldHeadingDegrees = heading
    }

    public fun SetSteering(value: Float) {
        steering = value.coerceIn(-1f, 1f)
    }

    public fun SetThrottle(value: Float) {
        throttle = value.coerceIn(0f, 1f)
    }

    public fun SetAcceleration(value: Float) {
        SetThrottle(value)
    }

    public fun SetBrake(value: Float) {
        brake = value.coerceIn(0f, 1f)
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

    private fun updateGearboxAndRpm() {
        val speed = Speed
        val gearSpeeds = floatArrayOf(0f, 65f, 110f, 160f, 210f, 255f, 295f, 335f, 370f)

        var newGear = 1
        for (g in 1..8) {
            if (speed > gearSpeeds[g - 1]) {
                newGear = g
            }
        }
        currentGear = newGear

        val prevGearSpeed = gearSpeeds[currentGear - 1]
        val nextGearSpeed = gearSpeeds[currentGear]
        val ratio = ((speed - prevGearSpeed) / (nextGearSpeed - prevGearSpeed).coerceAtLeast(1f)).coerceIn(0f, 1f)

        val throttleBonus = throttle * 1500f
        engineRpm = idleRpm + ratio * (maxRpm - idleRpm) * 0.85f + throttleBonus
        engineRpm = engineRpm.coerceIn(idleRpm, maxRpm)
    }
}
