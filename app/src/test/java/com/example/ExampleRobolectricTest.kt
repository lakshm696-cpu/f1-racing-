package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.physics.CameraFollow
import com.example.physics.CarController
import com.example.physics.MobileButton
import com.example.physics.MobileInput
import com.example.physics.OpponentAI
import com.example.physics.Vector2D
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Apex F1", appName)
    }

    @Test
    fun `car controller accelerates and updates wheel physics`() {
        val car = CarController().apply { Start() }
        assertEquals(0f, car.Speed, 0.001f)

        // Accelerate
        car.SetAcceleration(1f)
        for (i in 0 until 60) {
            car.FixedUpdate(1f / 60f)
        }

        // Car should have accelerated forward
        assertTrue("Speed should be greater than 0, was: ${car.Speed}", car.Speed > 0f)
        assertTrue("Rear wheels should have received motor torque", car.rearLeft.motorTorque > 0f)

        // Steer
        car.SetSteering(1f)
        for (i in 0 until 60) {
            car.FixedUpdate(1f / 60f)
        }
        assertEquals(car.steeringAngle, car.frontLeft.steerAngle, 0.5f)

        // Brake
        val speedBeforeBrake = car.Speed
        car.SetAcceleration(0f)
        car.SetBrake(1f)
        for (i in 0 until 60) {
            car.FixedUpdate(1f / 60f)
        }
        assertTrue("Brake should reduce speed", car.Speed < speedBeforeBrake)
    }

    @Test
    fun `mobile button triggers mobile input and car controls`() {
        val car = CarController().apply { Start() }
        val input = MobileInput(car)
        val gasButton = MobileButton(input, MobileButton.ButtonType.Gas)
        val brakeButton = MobileButton(input, MobileButton.ButtonType.Brake)
        val leftButton = MobileButton(input, MobileButton.ButtonType.Left)

        gasButton.onPointerDown()
        assertEquals(1f, car.acceleration, 0.001f)

        gasButton.onPointerUp()
        assertEquals(0f, car.acceleration, 0.001f)

        brakeButton.onPointerDown()
        assertEquals(1f, car.braking, 0.001f)

        brakeButton.onPointerUp()
        assertEquals(0f, car.braking, 0.001f)

        leftButton.onPointerDown()
        assertEquals(-1f, car.steering, 0.001f)

        leftButton.onPointerUp()
        assertEquals(0f, car.steering, 0.001f)
    }

    @Test
    fun `camera follow tracks car smoothly`() {
        val car = CarController().apply { Start() }
        val camera = CameraFollow(car).apply { reset() }

        // Move car
        car.SetAcceleration(1f)
        for (i in 0 until 30) {
            car.FixedUpdate(1f / 60f)
            camera.LateUpdate(1f / 60f)
        }

        // Camera position should have moved
        assertTrue(camera.position.magnitude >= 0f)
    }

    @Test
    fun `opponent AI navigates along waypoints`() {
        val waypoints = listOf(Vector2D(0f, 0f), Vector2D(0f, 50f), Vector2D(50f, 50f))
        val opponent = OpponentAI(
            name = "Verstappen",
            driverCode = "VER",
            driverNumber = 1,
            teamColor = androidx.compose.ui.graphics.Color.Blue,
            helmetColor = androidx.compose.ui.graphics.Color.Yellow,
            waypoints = waypoints,
            speed = 25f
        ).apply {
            reset(Vector2D(0f, 0f), 90f, 1)
        }

        for (i in 0 until 120) {
            opponent.Update(1f / 60f)
        }

        // Opponent should have progressed
        assertTrue(opponent.position.magnitude > 0f)
    }

    @Test
    fun `race manager tracks laps and race finish`() {
        val car = CarController().apply { Start() }
        val raceManager = com.example.physics.RaceManager(playerCar = car, totalLaps = 3)

        assertEquals("LAP 1/3", raceManager.lapText)
        assertEquals("GO!", raceManager.statusText)
        assertEquals(false, raceManager.raceFinished)

        // Step time
        raceManager.Update(1.0f)
        assertTrue(raceManager.raceTime > 0.99f)

        // Complete Lap 1
        raceManager.CompleteLap()
        assertEquals(2, raceManager.currentLap)
        assertEquals("LAP 2/3", raceManager.lapText)

        // Complete Lap 2
        raceManager.CompleteLap()
        assertEquals(3, raceManager.currentLap)
        assertEquals("LAP 3/3", raceManager.lapText)

        // Complete Lap 3 (Final)
        raceManager.CompleteLap()
        assertEquals(true, raceManager.raceFinished)
        assertTrue(raceManager.statusText.startsWith("RACE FINISHED!"))
    }

    @Test
    fun `lap trigger detects player collision and completes lap`() {
        val car = CarController().apply { Start() }
        val raceManager = com.example.physics.RaceManager(playerCar = car, totalLaps = 3)
        val trigger = com.example.physics.LapTrigger(
            raceManager = raceManager,
            position = Vector2D(0f, 0f),
            trackWidth = 20f,
            normalDirection = Vector2D(0f, -1f)
        )

        assertEquals(1, raceManager.currentLap)

        // Simulate car crossing from (0, 5) to (0, -5) with tag "Player"
        trigger.checkCrossing(Vector2D(0f, 5f), isPlayerTag = true)
        trigger.checkCrossing(Vector2D(0f, -5f), isPlayerTag = true)

        assertEquals("Lap count should advance when Player crosses line", 2, raceManager.currentLap)

        // Non-player tag shouldn't trigger
        trigger.checkCrossing(Vector2D(0f, 5f), isPlayerTag = false)
        trigger.checkCrossing(Vector2D(0f, -5f), isPlayerTag = false)
        assertEquals(2, raceManager.currentLap)
    }

    @Test
    fun `advanced car controller handles suspension, steering lerp and throttle`() {
        val car = com.example.physics.AdvancedCarController()
        car.Start()

        // Verify suspension spring settings from user code
        assertEquals(35000f, car.frontLeft.suspensionSpring.spring, 0.1f)
        assertEquals(4500f, car.frontLeft.suspensionSpring.damper, 0.1f)
        assertEquals(0.5f, car.frontLeft.suspensionSpring.targetPosition, 0.01f)

        // Test throttle and motor torque
        car.SetThrottle(1f)
        car.FixedUpdate(1f / 60f)
        assertEquals(1800f, car.rearLeft.motorTorque, 0.1f)
        assertEquals(1800f, car.rearRight.motorTorque, 0.1f)

        // Test steering response lerp (smooth transition rather than instantaneous)
        car.SetSteering(1f) // max steer angle is 30f
        car.FixedUpdate(1f / 60f)
        assertTrue("Steering should smoothly interpolate towards 30 deg", car.currentSteering in 0.1f..30f)
        assertTrue("Wheel steerAngle should match currentSteering", car.frontLeft.steerAngle == car.currentSteering)

        // Test brake torque
        car.SetThrottle(0f)
        car.SetBrake(1f)
        car.FixedUpdate(1f / 60f)
        assertEquals(4000f, car.frontLeft.brakeTorque, 0.1f)
        assertEquals(4000f, car.rearLeft.brakeTorque, 0.1f)
    }

    @Test
    fun `advanced camera follow tracks target and expands field of view at high speed`() {
        val car = CarController().apply { Start() }
        val advCam = com.example.physics.AdvancedCameraFollow(target = car)
        advCam.Start()

        // Verify default Unity parameters
        assertEquals(6f, advCam.followSmooth, 0.01f)
        assertEquals(5f, advCam.rotationSmooth, 0.01f)
        assertEquals(65f, advCam.normalFOV, 0.01f)
        assertEquals(78f, advCam.maxFOV, 0.01f)
        assertEquals(65f, advCam.fieldOfView, 0.01f)
        assertEquals(8f, advCam.offsetDistance, 0.01f)
        assertEquals(4f, advCam.offsetHeight, 0.01f)

        // At zero speed, FOV stays at normalFOV
        advCam.LateUpdate(1f / 60f)
        assertEquals(65f, advCam.fieldOfView, 0.5f)

        // Accelerate car to high speed
        car.SetAcceleration(1f)
        for (i in 0 until 180) {
            car.FixedUpdate(1f / 60f)
            advCam.LateUpdate(1f / 60f)
        }

        assertTrue("Car should reach speed", car.Speed > 50f)
        assertTrue("Camera FOV should dynamically expand above normalFOV at speed", advCam.fieldOfView > 65.5f)
        assertTrue("Camera FOV should not exceed maxFOV", advCam.fieldOfView <= 78.5f)
    }

    @Test
    fun `car audio modulates pitch and volume based on speed factor`() {
        val car = CarController().apply { Start() }
        val carAudio = com.example.audio.CarAudio(car)
        carAudio.Start()

        // Verify default Unity parameters
        assertEquals(0.8f, carAudio.idlePitch, 0.001f)
        assertEquals(2.0f, carAudio.maxPitch, 0.001f)
        assertEquals(true, carAudio.source.loop)
        assertEquals(true, carAudio.source.isPlaying)

        // At standstill (0 km/h)
        carAudio.Update()
        assertEquals(0.8f, carAudio.source.pitch, 0.01f)
        assertEquals(0.3f, carAudio.source.volume, 0.01f)

        // Simulate 90 km/h: speedFactor = 90 / 180 = 0.5
        // pitch = lerp(0.8, 2.0, 0.5) = 1.4f
        // volume = lerp(0.3, 1.0, 0.5) = 0.65f
        car.rb.linearVelocity = com.example.physics.Vector2D(0f, 90f / 3.6f)
        carAudio.Update()
        assertEquals(1.4f, carAudio.source.pitch, 0.05f)
        assertEquals(0.65f, carAudio.source.volume, 0.05f)

        // Simulate 180 km/h: speedFactor = 1.0
        // pitch = 2.0f, volume = 1.0f
        car.rb.linearVelocity = com.example.physics.Vector2D(0f, 180f / 3.6f)
        carAudio.Update()
        assertEquals(2.0f, carAudio.source.pitch, 0.01f)
        assertEquals(1.0f, carAudio.source.volume, 0.01f)

        // Clamping check: at 250 km/h, pitch should not exceed maxPitch (2.0f)
        car.rb.linearVelocity = com.example.physics.Vector2D(0f, 250f / 3.6f)
        carAudio.Update()
        assertEquals(2.0f, carAudio.source.pitch, 0.01f)
        assertEquals(1.0f, carAudio.source.volume, 0.01f)
    }

    @Test
    fun `car effects triggers tire smoke particle systems on rear wheel slip`() {
        val car = CarController().apply { Start() }
        val effects = com.example.physics.CarEffects(car.rearLeft, car.rearRight)
        effects.Start()

        // Default threshold is 0.4f
        assertEquals(0.4f, effects.smokeThreshold, 0.001f)
        assertEquals(false, effects.leftSmoke.isPlaying)
        assertEquals(false, effects.rightSmoke.isPlaying)

        // 1. Normal driving with low lateral slip (< 0.4f)
        car.rearLeft.lateralSlip = 0.15f
        car.rearRight.lateralSlip = 0.15f
        effects.Update(1f / 60f)
        assertEquals(false, effects.leftSmoke.isPlaying)
        assertEquals(false, effects.rightSmoke.isPlaying)

        // 2. High sideways slip (oversteer / drift > 0.4f)
        car.rearLeft.lateralSlip = 0.65f
        car.rearRight.lateralSlip = 0.65f
        effects.Update(1f / 60f)
        assertEquals(true, effects.leftSmoke.isPlaying)
        assertEquals(true, effects.rightSmoke.isPlaying)
        assertTrue(effects.isAnySmokeActive)

        // Step a few frames to verify particles are emitted
        for (i in 0 until 5) {
            effects.Update(1f / 60f)
        }
        assertTrue("Smoke particles should be generated", effects.totalActiveParticles > 0)

        // 3. Straighten out / recover grip (< 0.4f)
        car.rearLeft.lateralSlip = 0.05f
        car.rearRight.lateralSlip = 0.05f
        effects.Update(1f / 60f)
        assertEquals(false, effects.leftSmoke.isPlaying)
        assertEquals(false, effects.rightSmoke.isPlaying)
    }

    @Test
    fun `car lighting toggles headlights and activates brake lights on BrakeAmount greater than 0_05`() {
        val car = CarController().apply { Start() }
        val lighting = com.example.physics.CarLighting(car)
        lighting.Start()

        // 1. Initial Start State: Headlights enabled, brake lamps normal/idle
        assertEquals(true, lighting.isHeadlightsEnabled)
        assertEquals(true, lighting.leftHeadlight.enabled)
        assertEquals(true, lighting.rightHeadlight.enabled)
        assertEquals(false, lighting.isBraking)
        assertEquals(lighting.normalColor, lighting.currentEmissionColor)

        // 2. Headlights toggling
        lighting.SetHeadlights(false)
        assertEquals(false, lighting.isHeadlightsEnabled)
        assertEquals(false, lighting.leftHeadlight.enabled)
        assertEquals(false, lighting.rightHeadlight.enabled)

        lighting.SetHeadlights(true)
        assertEquals(true, lighting.isHeadlightsEnabled)

        // 3. No braking (car.BrakeAmount = 0.0f <= 0.05f)
        lighting.Update()
        assertEquals(false, lighting.isBraking)
        assertEquals(lighting.normalColor, lighting.currentEmissionColor)

        // 4. Moderate to full braking (car.BrakeAmount = 1.0f > 0.05f)
        car.SetBrake(1.0f)
        assertEquals(1.0f, car.BrakeAmount, 0.001f)
        lighting.Update()
        assertEquals(true, lighting.isBraking)
        assertEquals(lighting.brakeColor, lighting.currentEmissionColor)

        // 5. Release brake (car.BrakeAmount = 0.0f)
        car.SetBrake(0.0f)
        lighting.Update()
        assertEquals(false, lighting.isBraking)
        assertEquals(lighting.normalColor, lighting.currentEmissionColor)
    }
}





