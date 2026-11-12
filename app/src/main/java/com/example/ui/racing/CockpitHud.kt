package com.example.ui.racing

import android.view.MotionEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.physics.CarController
import com.example.physics.MobileInput
import com.example.ui.theme.F1CarbonDark
import com.example.ui.theme.F1Red
import com.example.ui.theme.RacingTextMuted
import com.example.ui.theme.RacingWhite
import com.example.ui.theme.SectorGreen
import com.example.ui.theme.SectorPurple
import com.example.ui.theme.SectorYellow
import com.example.ui.theme.TelemetryCyan
import com.example.ui.theme.WarningRed
import kotlin.math.roundToInt

import com.example.physics.RaceManager

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CockpitHud(
    car: CarController,
    mobileInput: MobileInput,
    raceManager: RaceManager,
    lapTimeSeconds: Float,
    bestLapSeconds: Float?,
    currentLap: Int,
    currentSector: Int,
    isSoundMuted: Boolean,
    onToggleSound: () -> Unit,
    onResetCar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var steeringWheelAngle by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // TOP HUD BAR: RPM Rev LEDs, Speedometer, Gear, Lap & Sector Timing
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // F1 RPM Shift Light LED Bar (Green -> Amber -> Red -> Flashing Purple)
            RpmLedBar(rpm = car.engineRpm, maxRpm = car.maxRpm)

            Spacer(modifier = Modifier.height(6.dp))

            // Main Telemetry Readout Dashboard
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xCC111116))
                    .border(1.dp, Color(0xFF282835), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Gear & Speed
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Gear indicator box
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(F1Red),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (car.currentGear == 0) "N" else car.currentGear.toString(),
                            color = RacingWhite,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${car.Speed.roundToInt()}",
                                color = RacingWhite,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = " KM/H",
                                color = TelemetryCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                            )
                        }
                        Text(
                            text = "${car.engineRpm.roundToInt()} RPM",
                            color = RacingTextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Middle: Lap, Sector, & Delta
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // RaceManager lapText
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(F1Red)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = raceManager.lapText,
                                color = RacingWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        SectorBadge(label = "S1", active = currentSector == 1, color = SectorPurple)
                        SectorBadge(label = "S2", active = currentSector == 2, color = SectorGreen)
                        SectorBadge(label = "S3", active = currentSector == 3, color = SectorYellow)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = raceManager.timerText,
                            color = TelemetryCyan,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${formatTime(lapTimeSeconds)})",
                            color = RacingWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    bestLapSeconds?.let { best ->
                        val delta = lapTimeSeconds - best
                        val deltaText = if (delta <= 0) String.format("-%.3fs", -delta) else String.format("+%.3fs", delta)
                        Text(
                            text = "BEST $deltaText",
                            color = if (delta <= 0) SectorGreen else WarningRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Right: DRS, ERS Battery & Audio Toggles
                Column(horizontalAlignment = Alignment.End) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // DRS Status Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when {
                                        car.isDrsActive -> SectorGreen
                                        car.isDrsAvailable -> TelemetryCyan
                                        else -> Color(0xFF282835)
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (car.isDrsActive) "DRS ON" else "DRS",
                                color = if (car.isDrsActive) F1CarbonDark else RacingWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // Mute button
                        IconButton(
                            onClick = onToggleSound,
                            modifier = Modifier.size(32.dp).testTag("sound_toggle")
                        ) {
                            Icon(
                                imageVector = if (isSoundMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = "Audio Toggle",
                                tint = if (isSoundMuted) RacingTextMuted else TelemetryCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Reset Car button
                        IconButton(
                            onClick = onResetCar,
                            modifier = Modifier.size(32.dp).testTag("reset_car")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Reset Car Position",
                                tint = RacingWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // ERS Battery Bar
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = "ERS Battery",
                            tint = if (car.isErsActive) TelemetryCyan else RacingTextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Box(
                            modifier = Modifier
                                .width(56.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF2B2B38))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(car.ersBatteryPercent / 100f)
                                    .height(6.dp)
                                    .background(if (car.isErsActive) TelemetryCyan else SectorGreen)
                            )
                        }
                    }
                }
            }
        }

        // 4-WHEEL TELEMETRY MINI-HUD (Top-Left under dashboard)
        FourWheelTelemetryWidget(
            car = car,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(y = 110.dp, x = 12.dp)
        )

        // STATUS BANNER (From RaceManager.statusText e.g. "GO!", "LAP 2!", "RACE FINISHED! Time: 84.32 s")
        if (raceManager.statusText.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (raceManager.raceFinished) Color(0xEE161622) else Color(0xDD0E0E14)
                    )
                    .border(
                        2.dp,
                        if (raceManager.raceFinished) SectorGreen else F1Red,
                        RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = raceManager.statusText,
                        color = if (raceManager.raceFinished) SectorGreen else RacingWhite,
                        fontSize = if (raceManager.raceFinished) 18.sp else 30.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    if (raceManager.raceFinished) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(F1Red)
                                .pointerInteropFilter {
                                    if (it.action == MotionEvent.ACTION_DOWN) {
                                        onResetCar()
                                    }
                                    true
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "RESTART RACE",
                                color = RacingWhite,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // BOTTOM CONTROLS (MobileInput Integration)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // LEFT SIDE: Steering Controls
            if (mobileInput.steeringMode == MobileInput.SteeringMode.BUTTONS) {
                // Discrete Touch Buttons (LeftDown / RightDown / SteeringUp)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // LEFT STEER BUTTON
                    PedalButton(
                        label = "◀",
                        subLabel = "STEER L",
                        isPressed = mobileInput.isLeftPressed,
                        activeColor = TelemetryCyan,
                        testTag = "btn_steer_left",
                        modifier = Modifier.size(76.dp),
                        onDown = { mobileInput.LeftDown() },
                        onUp = { mobileInput.SteeringUp() }
                    )

                    // RIGHT STEER BUTTON
                    PedalButton(
                        label = "▶",
                        subLabel = "STEER R",
                        isPressed = mobileInput.isRightPressed,
                        activeColor = TelemetryCyan,
                        testTag = "btn_steer_right",
                        modifier = Modifier.size(76.dp),
                        onDown = { mobileInput.RightDown() },
                        onUp = { mobileInput.SteeringUp() }
                    )
                }
            } else {
                // Interactive F1 Rotary Steering Wheel (Rotates and sets steering -1f to 1f)
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color(0xCC181822))
                        .border(2.dp, Color(0xFF383848), CircleShape)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragEnd = {
                                    steeringWheelAngle = 0f
                                    mobileInput.setAnalogSteering(0f)
                                },
                                onDragCancel = {
                                    steeringWheelAngle = 0f
                                    mobileInput.setAnalogSteering(0f)
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    steeringWheelAngle = (steeringWheelAngle + dragAmount.x * 0.8f).coerceIn(-90f, 90f)
                                    val steerVal = (steeringWheelAngle / 90f).coerceIn(-1f, 1f)
                                    mobileInput.setAnalogSteering(steerVal)
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .rotate(steeringWheelAngle),
                        contentAlignment = Alignment.Center
                    ) {
                        // F1 Steering Wheel Rim graphic
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(8.dp, F1CarbonDark, CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(F1Red),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("F1", color = RacingWhite, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                    }
                }
            }

            // CENTER: DRS & ERS BOOST & STEERING MODE TOGGLE
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Steering Mode Toggle (Buttons vs Wheel)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x99222230))
                        .border(1.dp, Color(0xFF3C3C4C), RoundedCornerShape(8.dp))
                        .pointerInteropFilter {
                            if (it.action == MotionEvent.ACTION_DOWN) {
                                mobileInput.steeringMode = if (mobileInput.steeringMode == MobileInput.SteeringMode.BUTTONS) {
                                    MobileInput.SteeringMode.ROTARY_WHEEL
                                } else {
                                    MobileInput.SteeringMode.BUTTONS
                                }
                            }
                            true
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (mobileInput.steeringMode == MobileInput.SteeringMode.BUTTONS) "MODE: BUTTONS" else "MODE: WHEEL",
                        color = TelemetryCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // DRS Toggle Button
                Box(
                    modifier = Modifier
                        .size(width = 80.dp, height = 36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (car.isDrsActive) SectorGreen else Color(0x99222230))
                        .border(1.dp, if (car.isDrsActive) SectorGreen else Color(0xFF3C3C4C), RoundedCornerShape(8.dp))
                        .pointerInteropFilter {
                            if (it.action == MotionEvent.ACTION_DOWN) {
                                car.toggleDrs()
                            }
                            true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "DRS",
                        color = if (car.isDrsActive) F1CarbonDark else RacingWhite,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }

                // ERS Overtake Boost Button
                Box(
                    modifier = Modifier
                        .size(width = 80.dp, height = 36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (car.isErsActive) TelemetryCyan else Color(0x99222230))
                        .border(1.dp, if (car.isErsActive) TelemetryCyan else Color(0xFF3C3C4C), RoundedCornerShape(8.dp))
                        .pointerInteropFilter {
                            if (it.action == MotionEvent.ACTION_DOWN) {
                                car.toggleErs()
                            }
                            true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "BOOST",
                        color = if (car.isErsActive) F1CarbonDark else RacingWhite,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            }

            // RIGHT SIDE: Brake and Gas Pedals (BrakeDown/Up, GasDown/Up)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // BRAKE PEDAL
                PedalButton(
                    label = "BRAKE",
                    subLabel = "${car.brakePower.toInt()} N",
                    isPressed = mobileInput.isBrakePressed,
                    activeColor = WarningRed,
                    testTag = "btn_brake",
                    modifier = Modifier
                        .size(width = 72.dp, height = 95.dp),
                    onDown = { mobileInput.BrakeDown() },
                    onUp = { mobileInput.BrakeUp() }
                )

                // GAS PEDAL
                PedalButton(
                    label = "GAS",
                    subLabel = "${car.motorPower.toInt()} N",
                    isPressed = mobileInput.isGasPressed,
                    activeColor = SectorGreen,
                    testTag = "btn_gas",
                    modifier = Modifier
                        .size(width = 72.dp, height = 110.dp),
                    onDown = { mobileInput.GasDown() },
                    onUp = { mobileInput.GasUp() }
                )
            }
        }
    }
}

@Composable
private fun RpmLedBar(rpm: Float, maxRpm: Float) {
    val totalLeds = 15
    val activeCount = ((rpm / maxRpm) * totalLeds).toInt().coerceIn(0, totalLeds)
    val isRevLimit = rpm >= maxRpm * 0.95f

    val infiniteTransition = rememberInfiniteTransition(label = "revFlash")
    val flashAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flash"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xDD0D0D12))
            .padding(horizontal = 6.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (i in 0 until totalLeds) {
            val isActive = i < activeCount
            val ledColor = when {
                i < 5 -> SectorGreen
                i < 10 -> SectorYellow
                i < 13 -> WarningRed
                else -> SectorPurple
            }

            val finalColor = if (isActive) {
                if (isRevLimit && i >= 13) ledColor.copy(alpha = flashAlpha) else ledColor
            } else {
                Color(0xFF22222C)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(10.dp)
                    .padding(horizontal = 2.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(finalColor)
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun PedalButton(
    label: String,
    subLabel: String,
    isPressed: Boolean,
    activeColor: Color,
    testTag: String,
    modifier: Modifier = Modifier,
    onDown: () -> Unit,
    onUp: () -> Unit
) {
    Box(
        modifier = modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isPressed) {
                    Brush.verticalGradient(listOf(activeColor, activeColor.copy(alpha = 0.8f)))
                } else {
                    Brush.verticalGradient(listOf(Color(0xDD242432), Color(0xDD181822)))
                }
            )
            .border(
                2.dp,
                if (isPressed) activeColor else Color(0xFF38384A),
                RoundedCornerShape(12.dp)
            )
            .pointerInteropFilter {
                when (it.action) {
                    MotionEvent.ACTION_DOWN -> onDown()
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> onUp()
                }
                true
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                color = if (isPressed) F1CarbonDark else RacingWhite,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subLabel,
                color = if (isPressed) F1CarbonDark.copy(alpha = 0.8f) else RacingTextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SectorBadge(label: String, active: Boolean, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (active) color else Color(0xFF282835))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = if (active) F1CarbonDark else RacingTextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FourWheelTelemetryWidget(car: CarController, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x99101016))
            .border(1.dp, Color(0xFF262635), RoundedCornerShape(8.dp))
            .padding(6.dp)
    ) {
        Text("TIRE TELEMETRY", color = RacingTextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Front Wheels
            WheelTempPill("FL", car.frontLeft.tireTemperature, car.frontLeft.isSkidding)
            WheelTempPill("FR", car.frontRight.tireTemperature, car.frontRight.isSkidding)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Rear Wheels
            WheelTempPill("RL", car.rearLeft.tireTemperature, car.rearLeft.isSkidding)
            WheelTempPill("RR", car.rearRight.tireTemperature, car.rearRight.isSkidding)
        }
    }
}

@Composable
private fun WheelTempPill(label: String, temp: Float, isSkid: Boolean) {
    val tempColor = when {
        isSkid -> WarningRed
        temp > 115f -> WarningRed
        temp > 90f -> SectorGreen
        else -> TelemetryCyan
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF1E1E28))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(label, color = RacingTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(3.dp))
        Text("${temp.toInt()}°C", color = tempColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

private fun formatTime(seconds: Float): String {
    val mins = (seconds / 60).toInt()
    val secs = (seconds % 60).toInt()
    val millis = ((seconds - mins * 60 - secs) * 1000).toInt()
    return String.format("%02d:%02d.%03d", mins, secs, millis)
}
