package com.example.physics

import androidx.compose.ui.graphics.Color

/**
 * Direct implementation of the user's Unity CarLighting MonoBehaviour.
 *
 * Controls front headlights (leftHeadlight, rightHeadlight) and rear brake light
 * renderers (leftBrakeLight, rightBrakeLight).
 *
 * In Update():
 * UpdateBrakeLights(car.BrakeAmount > 0.05f);
 * modulating the material emission color between normalColor (Black) and brakeColor (Red * 2f).
 */
class CarLighting(
    var car: AdvancedCarController? = null
) {
    // Secondary constructor accepting CarController
    constructor(classicCar: CarController) : this(null) {
        this.classicCar = classicCar
    }

    private var classicCar: CarController? = null

    // public Light leftHeadlight;
    // public Light rightHeadlight;
    val leftHeadlight: SimulatedLight = SimulatedLight("LeftHeadlight")
    val rightHeadlight: SimulatedLight = SimulatedLight("RightHeadlight")

    // public Renderer leftBrakeLight;
    // public Renderer rightBrakeLight;
    val leftBrakeLight: SimulatedRenderer = SimulatedRenderer("LeftBrakeLight")
    val rightBrakeLight: SimulatedRenderer = SimulatedRenderer("RightBrakeLight")

    // public Color brakeColor = Color.red;
    // public Color normalColor = Color.black;
    var brakeColor: Color = Color(0xFFFF1801) // F1 FIA Intense Brake Red
    var normalColor: Color = Color(0xFF1A0505) // Dark unlit tail lamp

    private var leftMaterial: SimulatedMaterial? = null
    private var rightMaterial: SimulatedMaterial? = null

    var isBraking: Boolean = false
        private set

    val isHeadlightsEnabled: Boolean
        get() = leftHeadlight.enabled

    // Active emission color on rear brake lights
    val currentEmissionColor: Color
        get() = if (isBraking) brakeColor else normalColor

    // Helper property to fetch brake state from either vehicle controller
    val currentBrakeAmount: Float
        get() = car?.BrakeAmount ?: classicCar?.BrakeAmount ?: 0f

    init {
        Start()
    }

    fun Start() {
        leftMaterial = leftBrakeLight.material
        rightMaterial = rightBrakeLight.material

        SetHeadlights(true)
        UpdateBrakeLights(false)
    }

    /**
     * Unity Update()
     */
    fun Update() {
        if (car == null && classicCar == null) return

        // Brake light activation is connected
        // through the public brake state below.
        UpdateBrakeLights(currentBrakeAmount > 0.05f)
    }

    public fun SetHeadlights(enabled: Boolean) {
        leftHeadlight.enabled = enabled
        rightHeadlight.enabled = enabled
    }

    fun UpdateBrakeLights(braking: Boolean) {
        isBraking = braking
        val color = if (braking) brakeColor else normalColor

        leftMaterial?.SetColor("_EmissionColor", color)
        rightMaterial?.SetColor("_EmissionColor", color)
    }

    /**
     * Simulated Unity Light component
     */
    class SimulatedLight(val name: String) {
        var enabled: Boolean = true
        var intensity: Float = 2.0f
        var color: Color = Color(0xFFFFF8E7) // Xenon beam white
        var range: Float = 35f // Forward throw in meters
        var beamWidth: Float = 6f
    }

    /**
     * Simulated Unity Renderer component
     */
    class SimulatedRenderer(val name: String) {
        val material = SimulatedMaterial()
    }

    /**
     * Simulated Unity Material component
     */
    class SimulatedMaterial {
        val properties = mutableMapOf<String, Color>()
        var emissionColor: Color = Color.Black
            private set

        fun SetColor(propertyName: String, color: Color) {
            properties[propertyName] = color
            if (propertyName == "_EmissionColor") {
                emissionColor = color
            }
        }

        fun GetColor(propertyName: String): Color {
            return properties[propertyName] ?: Color.Black
        }
    }
}
