package com.example.ui.racing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import com.example.physics.CarController
import com.example.physics.Vector2D
import com.example.physics.WheelCollider
import com.example.track.Circuit
import com.example.track.SkidMark
import com.example.ui.theme.F1CarbonDark
import com.example.ui.theme.F1Red
import com.example.ui.theme.RacingWhite
import com.example.ui.theme.SectorGreen
import com.example.ui.theme.TireHardWhite
import com.example.ui.theme.TireMediumYellow
import com.example.ui.theme.TireSoftRed
import com.example.ui.theme.TireWetBlue
import kotlin.math.cos
import kotlin.math.sin

import com.example.physics.CameraFollow
import com.example.physics.CarEffects
import com.example.physics.CarLighting
import com.example.physics.OpponentAI

@Composable
fun RacingCanvas(
    car: CarController,
    circuit: Circuit,
    skidMarks: List<SkidMark>,
    cameraFollow: CameraFollow,
    opponents: List<OpponentAI> = emptyList(),
    carEffects: CarEffects? = null,
    carLighting: CarLighting? = null,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(F1CarbonDark)
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)

        // Pixels per meter scale driven dynamically by AdvancedCameraFollow FOV (normalFOV 65° to maxFOV 78°)
        val baseScale = 9.2f
        val fovFactor = (cameraFollow.normalFOV / cameraFollow.currentFov.coerceAtLeast(35f))
        val dynamicScale = (baseScale * fovFactor).coerceIn(6.0f, 11.5f)

        // Camera position and rotation from Unity CameraFollow / AdvancedCameraFollow
        val camPos = cameraFollow.position
        val camRotation = if (cameraFollow.mode == CameraFollow.CameraMode.TOP_DOWN) 0f else -cameraFollow.rotationDegrees - 90f

        translate(center.x, center.y) {
            rotate(camRotation, Offset.Zero) {
                // Background grass/run-off terrain
                drawRect(
                    color = Color(0xFF131A13),
                    topLeft = Offset(-size.width * 2, -size.height * 2),
                    size = Size(size.width * 4, size.height * 4)
                )

                // Draw Track Asphalt and Kerbs
                drawTrackCircuit(circuit, dynamicScale, camPos)

                // Draw Skid Marks
                drawSkidMarks(skidMarks, dynamicScale, camPos)

                // Draw Tire Smoke Particles (from Unity CarEffects script)
                if (carEffects != null) {
                    drawTireSmoke(carEffects, dynamicScale, camPos)
                }

                // Draw Opponent AI Cars (from Unity OpponentAI script)
                for (opponent in opponents) {
                    drawOpponentCar(opponent, dynamicScale, camPos)
                }

                // Draw Player F1 Car
                drawF1Car(car, dynamicScale, camPos, carLighting)
            }
        }
    }
}

private fun DrawScope.drawTrackCircuit(
    circuit: Circuit,
    scale: Float,
    carPos: Vector2D
) {
    val waypoints = circuit.waypoints
    if (waypoints.size < 2) return

    val asphaltColor = Color(0xFF26262F)
    val kerbRed = Color(0xFFD61A1A)
    val kerbWhite = Color(0xFFF2F2F2)
    val boundaryLineColor = Color(0xFF888896)
    val drsColor = Color(0x3300FF88)

    // Draw asphalt track ribbon
    for (i in 0 until waypoints.size) {
        val curr = waypoints[i]
        val next = waypoints[(i + 1) % waypoints.size]

        val p1 = toScreen(curr.pos, carPos, scale)
        val p2 = toScreen(next.pos, carPos, scale)

        // Asphalt road surface
        val trackWidthPx = curr.width * scale
        drawLine(
            color = asphaltColor,
            start = p1,
            end = p2,
            strokeWidth = trackWidthPx,
            cap = StrokeCap.Round
        )

        // DRS zone highlight on asphalt
        if (curr.isDrsZone) {
            drawLine(
                color = drsColor,
                start = p1,
                end = p2,
                strokeWidth = trackWidthPx * 0.9f,
                cap = StrokeCap.Round
            )
        }

        // Kerbs (alternating red and white rumble strips)
        val dir = (next.pos - curr.pos).normalized()
        val normal = Vector2D(-dir.y, dir.x)

        val kerbOffsetDist = (curr.width / 2f) * scale

        if (curr.hasRightKerb) {
            val kStart = p1 + Offset(normal.x * kerbOffsetDist, normal.y * kerbOffsetDist)
            val kEnd = p2 + Offset(normal.x * kerbOffsetDist, normal.y * kerbOffsetDist)
            val isEven = (i % 2 == 0)
            drawLine(
                color = if (isEven) kerbRed else kerbWhite,
                start = kStart,
                end = kEnd,
                strokeWidth = 3f * scale,
                cap = StrokeCap.Butt
            )
        }

        if (curr.hasLeftKerb) {
            val kStart = p1 - Offset(normal.x * kerbOffsetDist, normal.y * kerbOffsetDist)
            val kEnd = p2 - Offset(normal.x * kerbOffsetDist, normal.y * kerbOffsetDist)
            val isEven = (i % 2 == 0)
            drawLine(
                color = if (isEven) kerbRed else kerbWhite,
                start = kStart,
                end = kEnd,
                strokeWidth = 3f * scale,
                cap = StrokeCap.Butt
            )
        }

        // White track boundaries
        val bLeftStart = p1 - Offset(normal.x * kerbOffsetDist, normal.y * kerbOffsetDist)
        val bLeftEnd = p2 - Offset(normal.x * kerbOffsetDist, normal.y * kerbOffsetDist)
        drawLine(color = boundaryLineColor, start = bLeftStart, end = bLeftEnd, strokeWidth = 1.2f)

        val bRightStart = p1 + Offset(normal.x * kerbOffsetDist, normal.y * kerbOffsetDist)
        val bRightEnd = p2 + Offset(normal.x * kerbOffsetDist, normal.y * kerbOffsetDist)
        drawLine(color = boundaryLineColor, start = bRightStart, end = bRightEnd, strokeWidth = 1.2f)
    }

    // Start / Finish Line
    val startWp = waypoints.first()
    val nextWp = waypoints[1]
    val startP = toScreen(startWp.pos, carPos, scale)
    val dir = (nextWp.pos - startWp.pos).normalized()
    val normal = Vector2D(-dir.y, dir.x)
    val halfW = (startWp.width / 2f) * scale
    val sfLeft = startP - Offset(normal.x * halfW, normal.y * halfW)
    val sfRight = startP + Offset(normal.x * halfW, normal.y * halfW)

    // Checkered line
    drawLine(color = RacingWhite, start = sfLeft, end = sfRight, strokeWidth = 3.5f)
}

private fun DrawScope.drawSkidMarks(
    skidMarks: List<SkidMark>,
    scale: Float,
    carPos: Vector2D
) {
    for (sm in skidMarks) {
        val p1 = toScreen(sm.start, carPos, scale)
        val p2 = toScreen(sm.end, carPos, scale)
        val alpha = (sm.intensity * (1f - (sm.age / 12f))).coerceIn(0f, 0.85f)
        drawLine(
            color = Color.Black.copy(alpha = alpha),
            start = p1,
            end = p2,
            strokeWidth = 0.5f * scale,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawTireSmoke(
    carEffects: CarEffects,
    scale: Float,
    camPos: Vector2D
) {
    val allParticles = carEffects.leftSmoke.particles + carEffects.rightSmoke.particles
    for (p in allParticles) {
        val center = toScreen(p.position, camPos, scale)
        val radiusPx = p.radius * scale
        drawCircle(
            color = Color(0xFFEEEEEE).copy(alpha = p.alpha.coerceIn(0f, 0.75f)),
            radius = radiusPx,
            center = center
        )
    }
}

private fun DrawScope.drawF1Car(
    car: CarController,
    scale: Float,
    carPos: Vector2D,
    carLighting: CarLighting? = null
) {
    // Relative to car position (car is at origin of screen transform)
    val carHeading = car.rb.headingDegrees
    val carScreenPos = toScreen(car.rb.position, carPos, scale)

    translate(carScreenPos.x, carScreenPos.y) {
        rotate(carHeading, Offset.Zero) {
            // Formula 1 Car dimensions (approx 5.5m length, 2.0m width)
            val carLengthPx = 5.2f * scale
            val carWidthPx = 1.9f * scale

            val liveryRed = F1Red
            val carbonBlack = Color(0xFF141418)
            val haloGold = Color(0xFFE5A93C)
            val driverHelmet = Color(0xFFFFD700)

            // 1. Draw 4 Wheel Models (FL, FR, RL, RR)
            drawWheelModel(car.frontLeft, scale, car.tireCompound)
            drawWheelModel(car.frontRight, scale, car.tireCompound)
            drawWheelModel(car.rearLeft, scale, car.tireCompound)
            drawWheelModel(car.rearRight, scale, car.tireCompound)

            // 2. Front Suspension Wishbones (Carbon fiber rods connecting chassis to wheels)
            val frontAxleY = -1.55f * scale
            val chassisFrontHalfW = 0.35f * scale
            val wheelFrontX = 0.85f * scale
            drawLine(color = carbonBlack, start = Offset(-chassisFrontHalfW, frontAxleY), end = Offset(-wheelFrontX, frontAxleY), strokeWidth = 2f)
            drawLine(color = carbonBlack, start = Offset(chassisFrontHalfW, frontAxleY), end = Offset(wheelFrontX, frontAxleY), strokeWidth = 2f)

            // Rear Suspension Wishbones
            val rearAxleY = 1.55f * scale
            val chassisRearHalfW = 0.45f * scale
            val wheelRearX = 0.85f * scale
            drawLine(color = carbonBlack, start = Offset(-chassisRearHalfW, rearAxleY), end = Offset(-wheelRearX, rearAxleY), strokeWidth = 2.5f)
            drawLine(color = carbonBlack, start = Offset(chassisRearHalfW, rearAxleY), end = Offset(wheelRearX, rearAxleY), strokeWidth = 2.5f)

            // 3. Aerodynamic Front Wing with endplates
            val frontWingY = -2.4f * scale
            val frontWingWidth = 1.95f * scale
            val frontWingDepth = 0.5f * scale

            val frontWingPath = Path().apply {
                moveTo(-frontWingWidth / 2f, frontWingY)
                lineTo(frontWingWidth / 2f, frontWingY)
                lineTo(frontWingWidth / 2f - 0.2f * scale, frontWingY + frontWingDepth)
                lineTo(-frontWingWidth / 2f + 0.2f * scale, frontWingY + frontWingDepth)
                close()
            }
            drawPath(frontWingPath, color = liveryRed)
            // Endplates
            drawRect(color = carbonBlack, topLeft = Offset(-frontWingWidth / 2f - 1f, frontWingY - 2f), size = Size(3f, frontWingDepth + 4f))
            drawRect(color = carbonBlack, topLeft = Offset(frontWingWidth / 2f - 2f, frontWingY - 2f), size = Size(3f, frontWingDepth + 4f))

            // Front Headlights Beams (Unity CarLighting leftHeadlight, rightHeadlight)
            if (carLighting?.isHeadlightsEnabled != false) {
                val beamLength = 8.5f * scale
                val beamSpread = 2.2f * scale
                val leftBeamPath = Path().apply {
                    moveTo(-0.45f * scale, frontWingY)
                    lineTo(-0.45f * scale - beamSpread, frontWingY - beamLength)
                    lineTo(-0.45f * scale + beamSpread * 0.4f, frontWingY - beamLength)
                    close()
                }
                val rightBeamPath = Path().apply {
                    moveTo(0.45f * scale, frontWingY)
                    lineTo(0.45f * scale - beamSpread * 0.4f, frontWingY - beamLength)
                    lineTo(0.45f * scale + beamSpread, frontWingY - beamLength)
                    close()
                }
                drawPath(leftBeamPath, color = Color(0x38FFF8E7))
                drawPath(rightBeamPath, color = Color(0x38FFF8E7))
            }

            // 4. Main F1 Chassis Body (Nosecone, Cockpit, Sidepods, Engine Cover)
            val bodyPath = Path().apply {
                // Nose tip
                moveTo(0f, -2.4f * scale)
                // Nose widening to front bulkhead
                lineTo(0.3f * scale, -1.6f * scale)
                // Right sidepod bulge
                lineTo(0.82f * scale, -0.6f * scale)
                lineTo(0.85f * scale, 0.6f * scale)
                // Coca-cola bottle narrowing towards rear gearbox
                lineTo(0.45f * scale, 1.6f * scale)
                lineTo(0.35f * scale, 2.3f * scale)
                // Rear end
                lineTo(-0.35f * scale, 2.3f * scale)
                lineTo(-0.45f * scale, 1.6f * scale)
                // Left sidepod bulge
                lineTo(-0.85f * scale, 0.6f * scale)
                lineTo(-0.82f * scale, -0.6f * scale)
                lineTo(-0.3f * scale, -1.6f * scale)
                close()
            }
            drawPath(bodyPath, color = liveryRed)

            // Carbon sidepod floor / aerodynamic underfloor edge
            drawRect(
                color = carbonBlack,
                topLeft = Offset(-0.95f * scale, -0.7f * scale),
                size = Size(0.12f * scale, 1.5f * scale)
            )
            drawRect(
                color = carbonBlack,
                topLeft = Offset(0.83f * scale, -0.7f * scale),
                size = Size(0.12f * scale, 1.5f * scale)
            )

            // 5. Cockpit opening & Halo safety structure
            val cockpitY = -0.5f * scale
            // Cockpit tub (dark interior)
            drawOval(
                color = carbonBlack,
                topLeft = Offset(-0.24f * scale, cockpitY),
                size = Size(0.48f * scale, 0.95f * scale)
            )

            // Driver Helmet (Yellow/Gold)
            drawCircle(
                color = driverHelmet,
                radius = 0.18f * scale,
                center = Offset(0f, cockpitY + 0.5f * scale)
            )
            // Visor (dark tint)
            drawRect(
                color = Color.Black,
                topLeft = Offset(-0.12f * scale, cockpitY + 0.38f * scale),
                size = Size(0.24f * scale, 0.08f * scale)
            )

            // Halo Wishbone Safety Hoop
            val haloPath = Path().apply {
                moveTo(0f, cockpitY - 0.05f * scale)
                lineTo(-0.25f * scale, cockpitY + 0.5f * scale)
                moveTo(0f, cockpitY - 0.05f * scale)
                lineTo(0.25f * scale, cockpitY + 0.5f * scale)
                // Central vertical pylon
                moveTo(0f, cockpitY - 0.05f * scale)
                lineTo(0f, cockpitY - 0.25f * scale)
            }
            drawPath(haloPath, color = haloGold, style = Stroke(width = 2.5f, cap = StrokeCap.Round))

            // 6. Airbox & Engine Cover Shark Fin
            drawLine(
                color = carbonBlack,
                start = Offset(0f, cockpitY + 0.7f * scale),
                end = Offset(0f, 2.0f * scale),
                strokeWidth = 3f
            )

            // 7. Rear Wing & DRS Flap
            val rearWingY = 2.25f * scale
            val rearWingW = 1.7f * scale
            val rearWingD = 0.45f * scale

            // Main Rear Wing Plane
            drawRect(
                color = carbonBlack,
                topLeft = Offset(-rearWingW / 2f, rearWingY),
                size = Size(rearWingW, rearWingD)
            )

            // DRS Flap (open/closed indicator)
            if (car.isDrsActive) {
                // Open wing flap - slotted gap
                drawRect(
                    color = SectorGreen,
                    topLeft = Offset(-rearWingW / 2f + 4f, rearWingY + 2f),
                    size = Size(rearWingW - 8f, 2.5f)
                )
            } else {
                // Closed flap
                drawRect(
                    color = liveryRed,
                    topLeft = Offset(-rearWingW / 2f + 4f, rearWingY + 2f),
                    size = Size(rearWingW - 8f, rearWingD - 4f)
                )
            }

            // Rear Endplates
            drawRect(color = liveryRed, topLeft = Offset(-rearWingW / 2f - 2f, rearWingY - 4f), size = Size(3f, rearWingD + 8f))
            drawRect(color = liveryRed, topLeft = Offset(rearWingW / 2f - 1f, rearWingY - 4f), size = Size(3f, rearWingD + 8f))

            // Left and Right Brake Light Renderers (leftBrakeLight, rightBrakeLight from CarLighting)
            val brakeActive = carLighting?.isBraking ?: (car.BrakeAmount > 0.05f)
            val brakeEmissionColor = carLighting?.currentEmissionColor ?: (if (brakeActive) Color(0xFFFF1801) else Color(0xFF1A0505))
            val brakeGlowAlpha = if (brakeActive) 0.95f else 0.25f

            // Left Brake Light
            val leftLightCenter = Offset(-rearWingW / 2f + 4f, rearWingY + rearWingD)
            drawCircle(
                color = brakeEmissionColor.copy(alpha = brakeGlowAlpha),
                radius = if (brakeActive) 4.5f else 2.5f,
                center = leftLightCenter
            )
            if (brakeActive) {
                drawCircle(color = Color(0x55FF0000), radius = 9f, center = leftLightCenter)
            }

            // Right Brake Light
            val rightLightCenter = Offset(rearWingW / 2f - 4f, rearWingY + rearWingD)
            drawCircle(
                color = brakeEmissionColor.copy(alpha = brakeGlowAlpha),
                radius = if (brakeActive) 4.5f else 2.5f,
                center = rightLightCenter
            )
            if (brakeActive) {
                drawCircle(color = Color(0x55FF0000), radius = 9f, center = rightLightCenter)
            }

            // Central FIA Rain / Braking LED light
            val centralLedColor = if (brakeActive) Color(0xFFFF2222) else Color(0x44FF2222)
            drawCircle(
                color = centralLedColor,
                radius = if (brakeActive) 4.0f else 2.0f,
                center = Offset(0f, rearWingY + rearWingD + 2f)
            )
        }
    }
}

private fun DrawScope.drawWheelModel(
    wheel: WheelCollider,
    scale: Float,
    compound: CarController.TireCompound
) {
    // Wheel dimensions (approx 0.7m diameter, 0.4m tread width)
    val wheelDiamPx = 0.72f * scale
    val wheelWidthPx = 0.42f * scale

    val localX = wheel.localOffset.x * scale
    val localY = wheel.localOffset.y * scale

    val compoundColor = when (compound) {
        CarController.TireCompound.SOFT -> TireSoftRed
        CarController.TireCompound.MEDIUM -> TireMediumYellow
        CarController.TireCompound.HARD -> TireHardWhite
        CarController.TireCompound.WET -> TireWetBlue
    }

    translate(localX, localY) {
        // Rotate front wheels according to steerAngle
        val steerRot = if (wheel.isFront) wheel.steerAngle else 0f
        rotate(steerRot, Offset.Zero) {
            // Black rubber tire
            drawRoundRect(
                color = Color(0xFF111114),
                topLeft = Offset(-wheelWidthPx / 2f, -wheelDiamPx / 2f),
                size = Size(wheelWidthPx, wheelDiamPx),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
            )

            // Tire sidewall compound stripe (Pirelli colors: Red, Yellow, White, Blue)
            drawRoundRect(
                color = compoundColor,
                topLeft = Offset(-wheelWidthPx / 2f, -wheelDiamPx / 2f),
                size = Size(wheelWidthPx, wheelDiamPx),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f),
                style = Stroke(width = 1.8f)
            )

            // 18-inch Rim / Wheel Cover Center
            val rimRadius = wheelDiamPx * 0.28f
            drawCircle(
                color = Color(0xFF2E2E38),
                radius = rimRadius,
                center = Offset.Zero
            )

            // Wheel rotation spin line to visually show wheel rolling!
            rotate(wheel.rotationAngleDegrees, Offset.Zero) {
                drawLine(
                    color = RacingWhite.copy(alpha = 0.7f),
                    start = Offset(0f, -rimRadius + 1f),
                    end = Offset(0f, rimRadius - 1f),
                    strokeWidth = 1.5f
                )
            }

            // Tire Smoke / lockup highlight
            if (wheel.isSkidding) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.45f),
                    radius = wheelDiamPx * 0.65f,
                    center = Offset.Zero
                )
            }
        }
    }
}

private fun DrawScope.drawOpponentCar(
    opponent: OpponentAI,
    scale: Float,
    camPos: Vector2D
) {
    val screenPos = toScreen(opponent.position, camPos, scale)

    translate(screenPos.x, screenPos.y) {
        rotate(opponent.headingDegrees, Offset.Zero) {
            val liveryColor = opponent.teamColor
            val carbonBlack = Color(0xFF141418)

            // Aerodynamic Front Wing
            val frontWingY = -2.3f * scale
            val frontWingWidth = 1.9f * scale
            drawRect(
                color = liveryColor,
                topLeft = Offset(-frontWingWidth / 2f, frontWingY),
                size = Size(frontWingWidth, 0.45f * scale)
            )

            // 4 Wheels
            val wheelDiamPx = 0.7f * scale
            val wheelWidthPx = 0.4f * scale
            val wheelOffX = 0.85f * scale
            val frontAxleY = -1.55f * scale
            val rearAxleY = 1.55f * scale

            // FL, FR, RL, RR
            drawRoundRect(color = Color(0xFF16161A), topLeft = Offset(-wheelOffX - wheelWidthPx / 2f, frontAxleY - wheelDiamPx / 2f), size = Size(wheelWidthPx, wheelDiamPx))
            drawRoundRect(color = Color(0xFF16161A), topLeft = Offset(wheelOffX - wheelWidthPx / 2f, frontAxleY - wheelDiamPx / 2f), size = Size(wheelWidthPx, wheelDiamPx))
            drawRoundRect(color = Color(0xFF16161A), topLeft = Offset(-wheelOffX - wheelWidthPx / 2f, rearAxleY - wheelDiamPx / 2f), size = Size(wheelWidthPx, wheelDiamPx))
            drawRoundRect(color = Color(0xFF16161A), topLeft = Offset(wheelOffX - wheelWidthPx / 2f, rearAxleY - wheelDiamPx / 2f), size = Size(wheelWidthPx, wheelDiamPx))

            // Body
            val bodyPath = Path().apply {
                moveTo(0f, -2.3f * scale)
                lineTo(0.3f * scale, -1.6f * scale)
                lineTo(0.8f * scale, -0.6f * scale)
                lineTo(0.8f * scale, 0.6f * scale)
                lineTo(0.45f * scale, 1.6f * scale)
                lineTo(0.35f * scale, 2.3f * scale)
                lineTo(-0.35f * scale, 2.3f * scale)
                lineTo(-0.45f * scale, 1.6f * scale)
                lineTo(-0.8f * scale, 0.6f * scale)
                lineTo(-0.8f * scale, -0.6f * scale)
                lineTo(-0.3f * scale, -1.6f * scale)
                close()
            }
            drawPath(bodyPath, color = liveryColor)

            // Cockpit opening
            drawOval(
                color = carbonBlack,
                topLeft = Offset(-0.24f * scale, -0.5f * scale),
                size = Size(0.48f * scale, 0.95f * scale)
            )

            // Driver Helmet
            drawCircle(
                color = opponent.helmetColor,
                radius = 0.18f * scale,
                center = Offset(0f, 0f)
            )

            // Rear Wing
            drawRect(
                color = liveryColor,
                topLeft = Offset(-1.6f * scale / 2f, 2.2f * scale),
                size = Size(1.6f * scale, 0.45f * scale)
            )
        }
    }
}

private fun toScreen(worldPos: Vector2D, carPos: Vector2D, scale: Float): Offset {
    val relX = (worldPos.x - carPos.x) * scale
    val relY = (worldPos.y - carPos.y) * scale
    return Offset(relX, relY)
}

