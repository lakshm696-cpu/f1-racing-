package com.example.physics

/**
 * Direct implementation of the user's Unity MobileInput MonoBehaviour.
 *
 * public AdvancedCarController car;
 *
 * Controls vehicle inputs:
 * - LeftDown() / RightDown() / SteeringUp() -> car.SetSteering()
 * - GasDown() / GasUp() -> car.SetThrottle()
 * - BrakeDown() / BrakeUp() -> car.SetBrake()
 */
class MobileInput(
    var car: AdvancedCarController? = null
) {
    // Secondary constructor for CarController compatibility
    constructor(classicCar: CarController) : this(null) {
        this.classicCar = classicCar
    }

    var classicCar: CarController? = null

    enum class SteeringMode {
        BUTTONS,
        ROTARY_WHEEL
    }

    var steeringMode: SteeringMode = SteeringMode.BUTTONS

    // Tracking pressed states for buttons
    var isLeftPressed: Boolean = false
        private set
    var isRightPressed: Boolean = false
        private set
    var isGasPressed: Boolean = false
        private set
    var isBrakePressed: Boolean = false
        private set

    // Discrete Unity-like button events
    fun LeftDown() {
        isLeftPressed = true
        isRightPressed = false
        car?.SetSteering(-1f)
        classicCar?.SetSteering(-1f)
    }

    fun RightDown() {
        isRightPressed = true
        isLeftPressed = false
        car?.SetSteering(1f)
        classicCar?.SetSteering(1f)
    }

    fun SteeringUp() {
        isLeftPressed = false
        isRightPressed = false
        car?.SetSteering(0f)
        classicCar?.SetSteering(0f)
    }

    fun GasDown() {
        isGasPressed = true
        car?.SetThrottle(1f)
        classicCar?.SetThrottle(1f)
    }

    fun GasUp() {
        isGasPressed = false
        car?.SetThrottle(0f)
        classicCar?.SetThrottle(0f)
    }

    fun BrakeDown() {
        isBrakePressed = true
        car?.SetBrake(1f)
        classicCar?.SetBrake(1f)
    }

    fun BrakeUp() {
        isBrakePressed = false
        car?.SetBrake(0f)
        classicCar?.SetBrake(0f)
    }

    // Analog continuous input (e.g. from virtual steering wheel dial)
    fun setAnalogSteering(steer: Float) {
        car?.SetSteering(steer)
        classicCar?.SetSteering(steer)
    }

    fun setAnalogThrottle(gas: Float) {
        car?.SetThrottle(gas)
        classicCar?.SetThrottle(gas)
    }

    fun setAnalogBrake(brake: Float) {
        car?.SetBrake(brake)
        classicCar?.SetBrake(brake)
    }

    fun releaseAll() {
        isLeftPressed = false
        isRightPressed = false
        isGasPressed = false
        isBrakePressed = false
        car?.SetSteering(0f)
        car?.SetThrottle(0f)
        car?.SetBrake(0f)
        classicCar?.SetSteering(0f)
        classicCar?.SetThrottle(0f)
        classicCar?.SetBrake(0f)
    }
}
