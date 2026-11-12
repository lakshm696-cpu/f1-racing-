package com.example.ui.tuning

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.example.physics.CarController
import com.example.ui.theme.F1CarbonDark
import com.example.ui.theme.F1CardSurface
import com.example.ui.theme.F1Red
import com.example.ui.theme.F1SurfaceDark
import com.example.ui.theme.RacingTextMuted
import com.example.ui.theme.RacingWhite
import com.example.ui.theme.SectorGreen
import com.example.ui.theme.SectorPurple
import com.example.ui.theme.TelemetryCyan
import com.example.ui.theme.TireHardWhite
import com.example.ui.theme.TireMediumYellow
import com.example.ui.theme.TireSoftRed
import com.example.ui.theme.TireWetBlue
import com.example.ui.theme.WarningRed
import kotlin.math.roundToInt

import com.example.audio.CarAudio
import com.example.physics.CameraFollow
import com.example.physics.CarEffects
import com.example.physics.CarLighting

@Composable
fun PhysicsTuningScreen(
    car: CarController,
    cameraFollow: CameraFollow,
    onResetDefaults: () -> Unit,
    carAudio: CarAudio? = null,
    carEffects: CarEffects? = null,
    carLighting: CarLighting? = null,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(F1CarbonDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(F1Red)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CAR CONTROLLER & TUNING",
                        color = RacingWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Text(
                    text = "Unity C# Physics & Kotlin Engine Parameters",
                    color = RacingTextMuted,
                    fontSize = 12.sp
                )
            }

            OutlinedButton(
                onClick = onResetDefaults,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TelemetryCyan),
                modifier = Modifier.testTag("reset_tuning_btn")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("DEFAULTS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Navigation Tabs: Tuning Sliders vs C# Source Code
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = F1SurfaceDark,
            contentColor = F1Red,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = F1Red
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PHYSICS SETUP", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("UNITY C# CODE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            )
        }

        if (selectedTab == 0) {
            // Tuning Sliders
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Quick Summary Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = F1CardSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        TuningStat("MOTOR TORQUE", "${car.motorPower.roundToInt()} N·m", TelemetryCyan)
                        TuningStat("BRAKE POWER", "${car.brakePower.roundToInt()} N·m", WarningRed)
                        TuningStat("MAX SPEED", "${car.maxSpeed.roundToInt()} km/h", SectorGreen)
                        TuningStat("STEER ANGLE", "${car.steeringAngle.roundToInt()}°", RacingWhite)
                    }
                }

                // 1. Motor Power Slider
                TuningSliderCard(
                    title = "MOTOR POWER (motorPower)",
                    subtitle = "Drive torque sent to rear wheels (rearLeft & rearRight)",
                    valueText = "${car.motorPower.roundToInt()} N·m",
                    value = car.motorPower,
                    range = 1000f..3200f,
                    activeColor = TelemetryCyan,
                    onValueChange = { car.motorPower = it }
                )

                // 2. Brake Power Slider
                TuningSliderCard(
                    title = "BRAKE POWER (brakePower)",
                    subtitle = "Stopping torque applied across all 4 wheel colliders",
                    valueText = "${car.brakePower.roundToInt()} N·m",
                    value = car.brakePower,
                    range = 1500f..6500f,
                    activeColor = WarningRed,
                    onValueChange = { car.brakePower = it }
                )

                // 3. Max Speed Slider
                TuningSliderCard(
                    title = "MAX SPEED (maxSpeed)",
                    subtitle = "Speed threshold where motor torque cut-off occurs",
                    valueText = "${car.maxSpeed.roundToInt()} km/h",
                    value = car.maxSpeed,
                    range = 120f..370f,
                    activeColor = SectorGreen,
                    onValueChange = { car.maxSpeed = it }
                )

                // 4. Steering Angle Slider
                TuningSliderCard(
                    title = "STEERING ANGLE (steeringAngle)",
                    subtitle = "Maximum front wheel rotation angle (frontLeft & frontRight)",
                    valueText = "${car.steeringAngle.roundToInt()}°",
                    value = car.steeringAngle,
                    range = 15f..45f,
                    activeColor = RacingWhite,
                    onValueChange = { car.steeringAngle = it }
                )

                // 5. Rigidbody Mass Slider
                TuningSliderCard(
                    title = "RIGIDBODY MASS (rb.mass)",
                    subtitle = "F1 regulation car weight (798 kg minimum - 1200 kg standard)",
                    valueText = "${car.rb.mass.roundToInt()} kg",
                    value = car.rb.mass,
                    range = 798f..1500f,
                    activeColor = TelemetryCyan,
                    onValueChange = { car.rb.mass = it }
                )

                // AdvancedCarController Settings
                Text(
                    text = "ADVANCED HANDLING & SUSPENSION",
                    color = RacingWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                TuningSliderCard(
                    title = "STEERING RESPONSE (steeringResponse)",
                    subtitle = "Mathf.Lerp steering weight & wheel response time",
                    valueText = "${String.format("%.1f", car.steeringResponse)}x",
                    value = car.steeringResponse,
                    range = 1f..15f,
                    activeColor = TelemetryCyan,
                    onValueChange = { car.steeringResponse = it }
                )

                TuningSliderCard(
                    title = "TRACTION LIMIT (tractionLimit)",
                    subtitle = "WheelFrictionCurve forward & lateral stiffness",
                    valueText = "${(car.tractionLimit * 100).roundToInt()}%",
                    value = car.tractionLimit,
                    range = 0.5f..1.5f,
                    activeColor = SectorGreen,
                    onValueChange = { car.tractionLimit = it }
                )

                TuningSliderCard(
                    title = "SUSPENSION SPRING (springForce)",
                    subtitle = "JointSpring vertical stiffness for 4 wheels",
                    valueText = "${car.springForce.roundToInt()} N/m",
                    value = car.springForce,
                    range = 15000f..60000f,
                    activeColor = SectorPurple,
                    onValueChange = {
                        car.springForce = it
                        car.SetupWheel(car.frontLeft)
                        car.SetupWheel(car.frontRight)
                        car.SetupWheel(car.rearLeft)
                        car.SetupWheel(car.rearRight)
                    }
                )

                TuningSliderCard(
                    title = "SUSPENSION DAMPER (damperForce)",
                    subtitle = "JointSpring vertical shock rebound damping",
                    valueText = "${car.damperForce.roundToInt()} Ns/m",
                    value = car.damperForce,
                    range = 1500f..8000f,
                    activeColor = WarningRed,
                    onValueChange = {
                        car.damperForce = it
                        car.SetupWheel(car.frontLeft)
                        car.SetupWheel(car.frontRight)
                        car.SetupWheel(car.rearLeft)
                        car.SetupWheel(car.rearRight)
                    }
                )

                // 6. Tire Compound Selector
                Text(
                    text = "TIRE COMPOUND (Pirelli Spec)",
                    color = RacingWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CarController.TireCompound.values().forEach { compound ->
                        val isSelected = car.tireCompound == compound
                        val compoundColor = when (compound) {
                            CarController.TireCompound.SOFT -> TireSoftRed
                            CarController.TireCompound.MEDIUM -> TireMediumYellow
                            CarController.TireCompound.HARD -> TireHardWhite
                            CarController.TireCompound.WET -> TireWetBlue
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) compoundColor else F1CardSurface)
                                .border(1.dp, compoundColor, RoundedCornerShape(10.dp))
                                .clickable { car.tireCompound = compound }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = compound.name,
                                    color = if (isSelected) F1CarbonDark else RacingWhite,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "${(compound.grip * 100).toInt()}% Grip",
                                    color = if (isSelected) F1CarbonDark.copy(alpha = 0.8f) else RacingTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                // 7. Advanced Camera Follow & Dynamic FOV (AdvancedCameraFollow.cs)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ADVANCED CAMERA & FOV (AdvancedCameraFollow.cs)",
                        color = RacingWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(
                        onClick = { cameraFollow.isAdvancedMode = !cameraFollow.isAdvancedMode },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (cameraFollow.isAdvancedMode) SectorGreen else RacingTextMuted
                        ),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(
                            text = if (cameraFollow.isAdvancedMode) "ADVANCED FOV" else "CLASSIC",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Live FOV Telemetry Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = F1CardSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TuningStat("NORMAL FOV", "${cameraFollow.normalFOV.roundToInt()}°", RacingWhite)
                        TuningStat("CURRENT FOV", "${String.format("%.1f", cameraFollow.currentFov)}°", SectorPurple)
                        TuningStat("MAX FOV", "${cameraFollow.maxFOV.roundToInt()}°", TelemetryCyan)
                        TuningStat("OFFSET", "(0, ${cameraFollow.offsetHeight.roundToInt()}, -${cameraFollow.offsetDistance.roundToInt()})", SectorGreen)
                    }
                }

                TuningSliderCard(
                    title = "FOLLOW SMOOTH (followSmooth)",
                    subtitle = "Decoupled position lerp speed: Vector3.Lerp at 6f/s",
                    valueText = "${String.format("%.1f", cameraFollow.followSmooth)}x",
                    value = cameraFollow.followSmooth,
                    range = 1f..15f,
                    activeColor = SectorPurple,
                    onValueChange = { cameraFollow.followSmooth = it }
                )

                TuningSliderCard(
                    title = "ROTATION SMOOTH (rotationSmooth)",
                    subtitle = "Decoupled look rotation slerp speed: Quaternion.Slerp at 5f/s",
                    valueText = "${String.format("%.1f", cameraFollow.rotationSmooth)}x",
                    value = cameraFollow.rotationSmooth,
                    range = 1f..15f,
                    activeColor = TelemetryCyan,
                    onValueChange = { cameraFollow.rotationSmooth = it }
                )

                TuningSliderCard(
                    title = "NORMAL FOV (normalFOV)",
                    subtitle = "Base camera field of view at low speeds (default 65°)",
                    valueText = "${cameraFollow.normalFOV.roundToInt()}°",
                    value = cameraFollow.normalFOV,
                    range = 50f..80f,
                    activeColor = RacingWhite,
                    onValueChange = {
                        cameraFollow.normalFOV = it
                        if (cameraFollow.maxFOV < it) cameraFollow.maxFOV = it + 5f
                    }
                )

                TuningSliderCard(
                    title = "MAX SPEED FOV (maxFOV)",
                    subtitle = "Dynamic field of view expansion at 180+ km/h (default 78°)",
                    valueText = "${cameraFollow.maxFOV.roundToInt()}°",
                    value = cameraFollow.maxFOV,
                    range = 65f..95f,
                    activeColor = F1Red,
                    onValueChange = {
                        cameraFollow.maxFOV = it
                        if (cameraFollow.normalFOV > it) cameraFollow.normalFOV = it - 5f
                    }
                )

                TuningSliderCard(
                    title = "CAMERA OFFSET DISTANCE (offset.z)",
                    subtitle = "Distance behind target: offset = new Vector3(0, ${cameraFollow.offsetHeight.roundToInt()}, -${cameraFollow.offsetDistance.roundToInt()})",
                    valueText = "${cameraFollow.offsetDistance.roundToInt()} m",
                    value = cameraFollow.offsetDistance,
                    range = 4f..18f,
                    activeColor = SectorGreen,
                    onValueChange = { cameraFollow.offsetDistance = it }
                )

                TuningSliderCard(
                    title = "CAMERA OFFSET HEIGHT (offset.y)",
                    subtitle = "Height above target: offset = new Vector3(0, ${cameraFollow.offsetHeight.roundToInt()}, -${cameraFollow.offsetDistance.roundToInt()})",
                    valueText = "${cameraFollow.offsetHeight.roundToInt()} m",
                    value = cameraFollow.offsetHeight,
                    range = 2f..10f,
                    activeColor = TireMediumYellow,
                    onValueChange = { cameraFollow.offsetHeight = it }
                )

                // 8. Engine Audio & Pitch Modulation (CarAudio.cs)
                if (carAudio != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "ENGINE AUDIO & PITCH (CarAudio.cs)",
                        color = RacingWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Live Audio Telemetry Card
                    val speedFactor = (car.Speed / 180f).coerceIn(0f, 1f)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = F1CardSurface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TuningStat("PITCH", "${String.format("%.2f", carAudio.source.pitch)}x", SectorPurple)
                            TuningStat("VOLUME", "${(carAudio.source.volume * 100).roundToInt()}%", TelemetryCyan)
                            TuningStat("SPEED FACTOR", "${(speedFactor * 100).roundToInt()}%", SectorGreen)
                            TuningStat("STATE", if (carAudio.source.isPlaying) "PLAYING" else "PAUSED", TireMediumYellow)
                        }
                    }

                    TuningSliderCard(
                        title = "IDLE PITCH (idlePitch)",
                        subtitle = "AudioSource pitch at standstill (default 0.8x)",
                        valueText = "${String.format("%.2f", carAudio.idlePitch)}x",
                        value = carAudio.idlePitch,
                        range = 0.4f..1.5f,
                        activeColor = SectorPurple,
                        onValueChange = { carAudio.idlePitch = it }
                    )

                    TuningSliderCard(
                        title = "MAX PITCH (maxPitch)",
                        subtitle = "AudioSource pitch at 180+ km/h (default 2.0x)",
                        valueText = "${String.format("%.2f", carAudio.maxPitch)}x",
                        value = carAudio.maxPitch,
                        range = 1.2f..3.5f,
                        activeColor = F1Red,
                        onValueChange = { carAudio.maxPitch = it }
                    )
                }

                // 9. Tire Smoke Particle Systems (CarEffects.cs)
                if (carEffects != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "TIRE SMOKE & DRIFT PARTICLES (CarEffects.cs)",
                        color = RacingWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Live Particle Telemetry Card
                    val rlSlip = car.rearLeft.lateralSlip
                    val rrSlip = car.rearRight.lateralSlip
                    val maxRearSlip = maxOf(kotlin.math.abs(rlSlip), kotlin.math.abs(rrSlip))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = F1CardSurface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TuningStat("THRESHOLD", String.format("%.2f", carEffects.smokeThreshold), SectorPurple)
                            TuningStat("MAX SLIP", String.format("%.2f", maxRearSlip), if (maxRearSlip > carEffects.smokeThreshold) WarningRed else TelemetryCyan)
                            TuningStat("PARTICLES", "${carEffects.totalActiveParticles}", SectorGreen)
                            TuningStat("EMITTER", if (carEffects.isAnySmokeActive) "EMITTING" else "IDLE", if (carEffects.isAnySmokeActive) TireMediumYellow else RacingTextMuted)
                        }
                    }

                    TuningSliderCard(
                        title = "SMOKE THRESHOLD (smokeThreshold)",
                        subtitle = "Trigger particles when abs(sidewaysSlip) exceeds threshold (default 0.40)",
                        valueText = String.format("%.2f", carEffects.smokeThreshold),
                        value = carEffects.smokeThreshold,
                        range = 0.1f..0.8f,
                        activeColor = TelemetryCyan,
                        onValueChange = { carEffects.smokeThreshold = it }
                    )
                }

                // 10. Vehicle Lighting & Brake Lamps (CarLighting.cs)
                if (carLighting != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CAR LIGHTING & BRAKE LAMPS (CarLighting.cs)",
                            color = RacingWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedButton(
                            onClick = { carLighting.SetHeadlights(!carLighting.isHeadlightsEnabled) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (carLighting.isHeadlightsEnabled) SectorGreen else RacingTextMuted
                            ),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(
                                text = if (carLighting.isHeadlightsEnabled) "HEADLIGHTS ON" else "HEADLIGHTS OFF",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Live Lighting Telemetry Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = F1CardSurface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TuningStat("HEADLIGHTS", if (carLighting.isHeadlightsEnabled) "BEAMS ON" else "OFF", if (carLighting.isHeadlightsEnabled) SectorGreen else RacingTextMuted)
                            TuningStat("BRAKE LAMPS", if (carLighting.isBraking) "GLOWING" else "IDLE", if (carLighting.isBraking) WarningRed else RacingTextMuted)
                            TuningStat("BRAKE AMOUNT", "${(car.BrakeAmount * 100).roundToInt()}%", if (car.BrakeAmount > 0.05f) WarningRed else TelemetryCyan)
                            TuningStat("THRESHOLD", "> 5%", SectorPurple)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        } else {
            // Source Code Comparison Viewer
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = F1CardSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = SectorGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AdvancedCarController.cs (Suspension & Handling)", color = RacingWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F0F14))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = """
using UnityEngine;

[RequireComponent(typeof(Rigidbody))]
public class AdvancedCarController : MonoBehaviour
{
    [Header("Wheel Colliders")]
    public WheelCollider frontLeft, frontRight, rearLeft, rearRight;

    [Header("Vehicle Settings")]
    public float motorTorque = 1800f;
    public float brakeTorque = 4000f;
    public float maxSpeed = 180f;
    public float maxSteerAngle = 30f;

    [Header("Handling")]
    public float steeringResponse = 5f;
    public float tractionLimit = 0.9f;

    [Header("Suspension")]
    public float springForce = 35000f;
    public float damperForce = 4500f;

    private Rigidbody rb;
    private float steering, throttle, brake, currentSteering;

    public float Speed => rb.linearVelocity.magnitude * 3.6f;

    void FixedUpdate()
    {
        currentSteering = Mathf.Lerp(
            currentSteering,
            steering * maxSteerAngle,
            steeringResponse * Time.fixedDeltaTime
        );

        frontLeft.steerAngle = currentSteering;
        frontRight.steerAngle = currentSteering;

        float torque = Speed < maxSpeed ? throttle * motorTorque : 0f;
        rearLeft.motorTorque = torque;
        rearRight.motorTorque = torque;

        float brakingForce = brake * brakeTorque;
        frontLeft.brakeTorque = brakingForce;
        frontRight.brakeTorque = brakingForce;
        rearLeft.brakeTorque = brakingForce;
        rearRight.brakeTorque = brakingForce;

        ApplyWheelFriction(rearLeft);
        ApplyWheelFriction(rearRight);
    }
}
                                """.trimIndent(),
                                color = Color(0xFFD4D4D8),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = F1CardSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = TelemetryCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CarController.cs (Unity Engine)", color = RacingWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F0F14))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = """
using UnityEngine;

public class CarController : MonoBehaviour
{
    [Header("Wheel Colliders")]
    public WheelCollider frontLeft;
    public WheelCollider frontRight;
    public WheelCollider rearLeft;
    public WheelCollider rearRight;

    [Header("Wheel Models")]
    public Transform frontLeftMesh;
    public Transform frontRightMesh;
    public Transform rearLeftMesh;
    public Transform rearRightMesh;

    [Header("Driving")]
    public float motorPower = 1800f;
    public float brakePower = 3500f;
    public float maxSpeed = 180f;
    public float steeringAngle = 30f;

    private Rigidbody rb;
    private float steering;
    private float acceleration;
    private float braking;

    public float Speed =>
        rb == null ? 0f : rb.linearVelocity.magnitude * 3.6f;

    void Start()
    {
        rb = GetComponent<Rigidbody>();
        rb.mass = 1200f;
        rb.centerOfMass = new Vector3(0, -0.5f, 0);
        rb.interpolation = RigidbodyInterpolation.Interpolate;
    }

    void FixedUpdate()
    {
        float currentSpeed = Speed;

        float torque = currentSpeed < maxSpeed
            ? acceleration * motorPower : 0f;

        rearLeft.motorTorque = torque;
        rearRight.motorTorque = torque;

        frontLeft.steerAngle = steering * steeringAngle;
        frontRight.steerAngle = steering * steeringAngle;

        float brakeTorque = braking * brakePower;

        frontLeft.brakeTorque = brakeTorque;
        frontRight.brakeTorque = brakeTorque;
        rearLeft.brakeTorque = brakeTorque;
        rearRight.brakeTorque = brakeTorque;

        UpdateWheel(frontLeft, frontLeftMesh);
        UpdateWheel(frontRight, frontRightMesh);
        UpdateWheel(rearLeft, rearLeftMesh);
        UpdateWheel(rearRight, rearRightMesh);
    }
}
                                """.trimIndent(),
                                color = Color(0xFFD4D4D8),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = F1CardSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = SectorGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("MobileInput.cs (Touch Pedals & Steering)", color = RacingWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F0F14))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = """
using UnityEngine;

public class MobileInput : MonoBehaviour
{
    public AdvancedCarController car;

    public void LeftDown()
    {
        car.SetSteering(-1f);
    }

    public void RightDown()
    {
        car.SetSteering(1f);
    }

    public void SteeringUp()
    {
        car.SetSteering(0f);
    }

    public void GasDown()
    {
        car.SetThrottle(1f);
    }

    public void GasUp()
    {
        car.SetThrottle(0f);
    }

    public void BrakeDown()
    {
        car.SetBrake(1f);
    }

    public void BrakeUp()
    {
        car.SetBrake(0f);
    }
}
                                """.trimIndent(),
                                color = Color(0xFFD4D4D8),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = F1CardSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = WarningRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("MobileButton.cs (Pointer Down / Up Events)", color = RacingWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F0F14))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = """
using UnityEngine;
using UnityEngine.EventSystems;

public class MobileButton : MonoBehaviour,
    IPointerDownHandler, IPointerUpHandler
{
    public MobileInput input;

    public enum ButtonType
    {
        Left, Right, Gas, Brake
    }

    public ButtonType buttonType;

    public void OnPointerDown(PointerEventData eventData)
    {
        switch (buttonType)
        {
            case ButtonType.Left:
                input.LeftDown();
                break;
            case ButtonType.Right:
                input.RightDown();
                break;
            case ButtonType.Gas:
                input.GasDown();
                break;
            case ButtonType.Brake:
                input.BrakeDown();
                break;
        }
    }

    public void OnPointerUp(PointerEventData eventData)
    {
        switch (buttonType)
        {
            case ButtonType.Left:
            case ButtonType.Right:
                input.SteeringUp();
                break;
            case ButtonType.Gas:
                input.GasUp();
                break;
            case ButtonType.Brake:
                input.BrakeUp();
                break;
        }
    }
}
                                """.trimIndent(),
                                color = Color(0xFFD4D4D8),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = F1CardSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = SectorPurple, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CameraFollow.cs (Smooth Vector3.Lerp & Slerp)", color = RacingWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F0F14))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = """
using UnityEngine;

public class CameraFollow : MonoBehaviour
{
    public Transform target;
    public Vector3 offset = new Vector3(0, 5, -9);
    public float smoothSpeed = 5f;

    void LateUpdate()
    {
        if (target == null) return;

        Vector3 desiredPosition =
            target.TransformPoint(offset);

        transform.position = Vector3.Lerp(
            transform.position,
            desiredPosition,
            smoothSpeed * Time.deltaTime
        );

        Quaternion lookRotation = Quaternion.LookRotation(
            target.position + Vector3.up * 1.5f
            - transform.position
        );

        transform.rotation = Quaternion.Slerp(
            transform.rotation,
            lookRotation,
            smoothSpeed * Time.deltaTime
        );
    }
}
                                """.trimIndent(),
                                color = Color(0xFFD4D4D8),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = F1CardSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = F1Red, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AdvancedCameraFollow.cs (Decoupled Smoothing & Dynamic FOV)", color = RacingWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F0F14))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = """
using UnityEngine;

[RequireComponent(typeof(Camera))]
public class AdvancedCameraFollow : MonoBehaviour
{
    public Transform target;
    public Rigidbody targetBody;

    public Vector3 offset = new Vector3(0, 4, -8);
    public float followSmooth = 6f;
    public float rotationSmooth = 5f;

    public float normalFOV = 65f;
    public float maxFOV = 78f;

    private Camera cam;

    void Start()
    {
        cam = GetComponent<Camera>();
    }

    void LateUpdate()
    {
        if (target == null) return;

        Vector3 desiredPosition =
            target.TransformPoint(offset);

        transform.position = Vector3.Lerp(
            transform.position,
            desiredPosition,
            followSmooth * Time.deltaTime
        );

        Vector3 lookPoint =
            target.position + Vector3.up * 1.3f;

        Quaternion desiredRotation =
            Quaternion.LookRotation(
                lookPoint - transform.position
            );

        transform.rotation = Quaternion.Slerp(
            transform.rotation,
            desiredRotation,
            rotationSmooth * Time.deltaTime
        );

        float speed = targetBody != null
            ? targetBody.linearVelocity.magnitude * 3.6f
            : 0f;

        float targetFOV = Mathf.Lerp(
            normalFOV,
            maxFOV,
            Mathf.Clamp01(speed / 180f)
        );

        cam.fieldOfView = Mathf.Lerp(
            cam.fieldOfView,
            targetFOV,
            3f * Time.deltaTime
        );
    }
}
                                """.trimIndent(),
                                color = Color(0xFFD4D4D8),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = F1CardSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = TelemetryCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("OpponentAI.cs (Waypoint Slerp Navigation)", color = RacingWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F0F14))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = """
using UnityEngine;

public class OpponentAI : MonoBehaviour
{
    public Transform[] waypoints;
    public float speed = 25f;
    public float turnSpeed = 3f;
    public float reachDistance = 5f;

    private int currentWaypoint = 0;

    void Update()
    {
        if (waypoints == null || waypoints.Length == 0)
            return;

        Transform target = waypoints[currentWaypoint];

        Vector3 direction =
            target.position - transform.position;

        direction.y = 0;

        if (direction.magnitude < reachDistance)
        {
            currentWaypoint =
                (currentWaypoint + 1) % waypoints.Length;
            return;
        }

        Quaternion targetRotation =
            Quaternion.LookRotation(direction);

        transform.rotation = Quaternion.Slerp(
            transform.rotation,
            targetRotation,
            turnSpeed * Time.deltaTime
        );

        transform.position +=
            transform.forward * speed * Time.deltaTime;
    }
}
                                """.trimIndent(),
                                color = Color(0xFFD4D4D8),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = F1CardSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = SectorGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("RaceManager.cs (TextMeshProUGUI Timing & Laps)", color = RacingWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F0F14))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = """
using UnityEngine;
using TMPro;

public class RaceManager : MonoBehaviour
{
    public CarController playerCar;

    public TextMeshProUGUI speedText;
    public TextMeshProUGUI lapText;
    public TextMeshProUGUI timerText;
    public TextMeshProUGUI statusText;

    public int totalLaps = 3;

    private int currentLap = 1;
    private float raceTime = 0f;
    private bool raceFinished = false;

    void Start()
    {
        UpdateLap();
        if (statusText != null)
            statusText.text = "GO!";
    }

    void Update()
    {
        if (raceFinished) return;

        raceTime += Time.deltaTime;

        if (playerCar != null && speedText != null)
            speedText.text =
                Mathf.RoundToInt(playerCar.Speed) + " km/h";

        if (timerText != null)
            timerText.text =
                raceTime.ToString("F2") + " s";
    }

    public void CompleteLap()
    {
        if (raceFinished) return;

        if (currentLap >= totalLaps)
        {
            raceFinished = true;

            if (statusText != null)
                statusText.text =
                    "RACE FINISHED! Time: " +
                    raceTime.ToString("F2") + " s";
        }
        else
        {
            currentLap++;
            UpdateLap();
        }
    }

    void UpdateLap()
    {
        if (lapText != null)
            lapText.text =
                "LAP " + currentLap + "/" + totalLaps;
    }
}
                                """.trimIndent(),
                                color = Color(0xFFD4D4D8),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = F1CardSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = WarningRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("LapTrigger.cs (OnTriggerEnter Player Detection)", color = RacingWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F0F14))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = """
using UnityEngine;

public class LapTrigger : MonoBehaviour
{
    public RaceManager raceManager;

    private void OnTriggerEnter(Collider other)
    {
        if (other.CompareTag("Player"))
        {
            raceManager.CompleteLap();
        }
    }
}
                                """.trimIndent(),
                                color = Color(0xFFD4D4D8),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = F1CardSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = SectorPurple, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CarAudio.cs (AudioSource Speed Pitch & Volume)", color = RacingWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F0F14))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = """
using UnityEngine;

[RequireComponent(typeof(AudioSource))]
public class CarAudio : MonoBehaviour
{
    public AdvancedCarController car;
    public AudioClip engineSound;

    public float idlePitch = 0.8f;
    public float maxPitch = 2f;

    private AudioSource source;

    void Start()
    {
        source = GetComponent<AudioSource>();

        source.clip = engineSound;
        source.loop = true;
        source.playOnAwake = false;
        source.spatialBlend = 1f;

        if (engineSound != null)
            source.Play();
    }

    void Update()
    {
        if (car == null || engineSound == null)
            return;

        float speedFactor =
            Mathf.Clamp01(car.Speed / 180f);

        source.pitch = Mathf.Lerp(
            idlePitch,
            maxPitch,
            speedFactor
        );

        source.volume = Mathf.Lerp(
            0.3f,
            1f,
            speedFactor
        );
    }
}
                                """.trimIndent(),
                                color = Color(0xFFD4D4D8),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = F1CardSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = TelemetryCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CarEffects.cs (WheelHit Sideways Slip Tire Smoke)", color = RacingWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F0F14))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = """
using UnityEngine;

public class CarEffects : MonoBehaviour
{
    public WheelCollider rearLeft;
    public WheelCollider rearRight;

    public ParticleSystem leftSmoke;
    public ParticleSystem rightSmoke;

    public float smokeThreshold = 0.4f;

    void Update()
    {
        UpdateSmoke(rearLeft, leftSmoke);
        UpdateSmoke(rearRight, rightSmoke);
    }

    void UpdateSmoke(
        WheelCollider wheel,
        ParticleSystem smoke)
    {
        if (wheel == null || smoke == null)
            return;

        WheelHit hit;
        bool grounded = wheel.GetGroundHit(out hit);

        bool slipping = grounded &&
            Mathf.Abs(hit.sidewaysSlip) > smokeThreshold;

        if (slipping && !smoke.isPlaying)
            smoke.Play();

        if (!slipping && smoke.isPlaying)
            smoke.Stop();
    }
}
                                """.trimIndent(),
                                color = Color(0xFFD4D4D8),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = F1CardSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = F1Red, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CarLighting.cs (Headlights & Dynamic Brake Light Emission)", color = RacingWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F0F14))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = """
using UnityEngine;

public class CarLighting : MonoBehaviour
{
    public Light leftHeadlight;
    public Light rightHeadlight;

    public Renderer leftBrakeLight;
    public Renderer rightBrakeLight;

    public AdvancedCarController car;

    public Color brakeColor = Color.red;
    public Color normalColor = Color.black;

    private Material leftMaterial;
    private Material rightMaterial;

    void Start()
    {
        if (leftBrakeLight != null)
            leftMaterial = leftBrakeLight.material;

        if (rightBrakeLight != null)
            rightMaterial = rightBrakeLight.material;

        SetHeadlights(true);
    }

    void Update()
    {
        if (car == null) return;

        // Brake light activation is connected
        // through the public brake state below.
        UpdateBrakeLights(car.BrakeAmount > 0.05f);
    }

    public void SetHeadlights(bool enabled)
    {
        if (leftHeadlight != null)
            leftHeadlight.enabled = enabled;

        if (rightHeadlight != null)
            rightHeadlight.enabled = enabled;
    }

    void UpdateBrakeLights(bool braking)
    {
        Color color = braking
            ? brakeColor
            : normalColor;

        if (leftMaterial != null)
            leftMaterial.SetColor("_EmissionColor", color * 2f);

        if (rightMaterial != null)
            rightMaterial.SetColor("_EmissionColor", color * 2f);
    }
}
                                """.trimIndent(),
                                color = Color(0xFFD4D4D8),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TuningStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = RacingTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, color = color, fontSize = 13.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun TuningSliderCard(
    title: String,
    subtitle: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    activeColor: Color,
    onValueChange: (Float) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = F1CardSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = title, color = RacingWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(text = subtitle, color = RacingTextMuted, fontSize = 10.sp)
                }
                Text(
                    text = valueText,
                    color = activeColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = range,
                colors = SliderDefaults.colors(
                    thumbColor = activeColor,
                    activeTrackColor = activeColor,
                    inactiveTrackColor = Color(0xFF333342)
                )
            )
        }
    }
}
