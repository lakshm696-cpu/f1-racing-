package com.example

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsMotorsports
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.CarAudio
import com.example.audio.F1SoundEngine
import com.example.physics.CameraFollow
import com.example.physics.CarController
import com.example.physics.CarEffects
import com.example.physics.CarLighting
import com.example.physics.LapTrigger
import com.example.physics.MobileButton
import com.example.physics.MobileInput
import com.example.physics.OpponentAI
import com.example.physics.RaceManager
import com.example.physics.Vector2D
import com.example.track.Circuit
import com.example.track.SkidMark
import com.example.track.TrackCatalog
import com.example.ui.racing.CockpitHud
import com.example.ui.racing.RacingCanvas
import com.example.ui.reaction.LightsOutScreen
import com.example.ui.standings.StandingsScreen
import com.example.ui.theme.F1CarbonDark
import com.example.ui.theme.F1CardSurface
import com.example.ui.theme.F1DarkRed
import com.example.ui.theme.F1Red
import com.example.ui.theme.F1SurfaceDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RacingTextMuted
import com.example.ui.theme.RacingWhite
import com.example.ui.theme.SectorGreen
import com.example.ui.theme.TelemetryCyan
import com.example.ui.tuning.PhysicsTuningScreen
import kotlinx.coroutines.isActive
import kotlin.math.abs
import kotlin.math.max

enum class ScreenTab {
    RACE,
    LIGHTS_OUT,
    TUNING,
    STANDINGS
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppRoot()
            }
        }
    }
}

@Composable
fun MainAppRoot() {
    val context = LocalContext.current

    // Core Vehicle Physics and Inputs
    val car = remember {
        CarController().apply {
            Start()
            reset(TrackCatalog.MONZA.startPosition, TrackCatalog.MONZA.startHeading)
        }
    }
    val mobileInput = remember(car) { MobileInput(car) }
    val cameraFollow = remember(car) {
        CameraFollow(car).apply {
            reset()
        }
    }
    val raceManager = remember(car) {
        RaceManager(playerCar = car, totalLaps = 3)
    }

    // Audio Engine & Unity CarAudio
    val carAudio = remember(car) {
        CarAudio(car)
    }
    // Unity CarEffects Particle Systems
    val carEffects = remember(car) {
        CarEffects(car.rearLeft, car.rearRight)
    }
    // Unity CarLighting Headlights & Brake Lights
    val carLighting = remember(car) {
        CarLighting(car)
    }
    val soundEngine = remember {
        F1SoundEngine(context).apply { start() }
    }
    DisposableEffect(soundEngine) {
        onDispose { soundEngine.stop() }
    }

    // Circuit and State
    var currentCircuit by remember { mutableStateOf(TrackCatalog.MONZA) }
    val lapTrigger = remember(raceManager, currentCircuit) {
        val startWp = currentCircuit.waypoints.first()
        val nextWp = currentCircuit.waypoints[1]
        val dir = (nextWp.pos - startWp.pos).normalized()
        LapTrigger(
            raceManager = raceManager,
            position = startWp.pos,
            trackWidth = startWp.width,
            normalDirection = dir
        )
    }
    var currentTab by remember { mutableStateOf(ScreenTab.RACE) }
    var isCircuitDialogVisible by remember { mutableStateOf(false) }
    var isSoundMuted by remember { mutableStateOf(false) }

    // Lap Timing
    var lapTimeSeconds by remember { mutableFloatStateOf(0f) }
    var bestLapSeconds by remember { mutableStateOf<Float?>(null) }
    var currentLap by remember { mutableIntStateOf(1) }
    var currentSector by remember { mutableIntStateOf(1) }
    var lastWaypointIndex by remember { mutableIntStateOf(0) }

    // Skid marks list
    val skidMarks = remember { mutableStateListOf<SkidMark>() }

    // Opponent AI Racers (Unity OpponentAI script integration)
    val opponents = remember(currentCircuit) {
        val wpCoords = currentCircuit.waypoints.map { it.pos }
        listOf(
            OpponentAI(
                name = "Max Verstappen",
                driverCode = "VER",
                driverNumber = 1,
                teamColor = Color(0xFF1E41FF),
                helmetColor = Color(0xFFFFD700),
                waypoints = wpCoords,
                speed = 34f,
                turnSpeed = 3.6f,
                reachDistance = 12f
            ).apply {
                if (wpCoords.size > 5) {
                    reset(wpCoords[3], -90f, 4)
                }
            },
            OpponentAI(
                name = "Charles Leclerc",
                driverCode = "LEC",
                driverNumber = 16,
                teamColor = Color(0xFFE8002D),
                helmetColor = Color(0xFFFF2222),
                waypoints = wpCoords,
                speed = 32f,
                turnSpeed = 3.4f,
                reachDistance = 12f
            ).apply {
                if (wpCoords.size > 8) {
                    reset(wpCoords[7], -90f, 8)
                }
            },
            OpponentAI(
                name = "Lando Norris",
                driverCode = "NOR",
                driverNumber = 4,
                teamColor = Color(0xFFFF8700),
                helmetColor = Color(0xFFFFFF00),
                waypoints = wpCoords,
                speed = 33f,
                turnSpeed = 3.5f,
                reachDistance = 12f
            ).apply {
                if (wpCoords.size > 12) {
                    reset(wpCoords[11], -90f, 12)
                }
            }
        )
    }

    // Handle Back Press to return to RACE tab
    if (currentTab != ScreenTab.RACE) {
        BackHandler { currentTab = ScreenTab.RACE }
    }

    // 60 FPS FixedUpdate and LateUpdate Physics Game Loop
    LaunchedEffect(currentCircuit) {
        var lastTimeNanos = SystemClock.elapsedRealtimeNanos()

        while (isActive) {
            withFrameNanos { nowNanos ->
                val dt = ((nowNanos - lastTimeNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastTimeNanos = nowNanos

                // 1. Step vehicle physics (FixedUpdate)
                val prevPos = car.rb.position
                car.FixedUpdate(dt)

                // 2. Step smooth chase camera (LateUpdate)
                cameraFollow.LateUpdate(dt)

                // 3. Step Opponent AI cars
                for (opponent in opponents) {
                    opponent.Update(dt)
                }

                // 4. Step Unity RaceManager
                raceManager.Update(dt)

                // 5. Track progress and Lap timing
                lapTimeSeconds += dt
                val carPos = car.rb.position

                // Find closest waypoint
                val waypoints = currentCircuit.waypoints
                var minDistSq = Float.MAX_VALUE
                var closestIndex = 0
                for (i in waypoints.indices) {
                    val dSq = (carPos - waypoints[i].pos).sqrMagnitude
                    if (dSq < minDistSq) {
                        minDistSq = dSq
                        closestIndex = i
                    }
                }

                val closestWp = waypoints[closestIndex]
                currentSector = closestWp.sector
                car.isDrsAvailable = closestWp.isDrsZone

                // Check for kerb rumble haptic
                val distToCenterline = kotlin.math.sqrt(minDistSq)
                val halfTrack = closestWp.width / 2f
                if (distToCenterline > halfTrack * 0.78f && distToCenterline < halfTrack * 1.15f) {
                    if (closestWp.hasLeftKerb || closestWp.hasRightKerb) {
                        soundEngine.vibrateKerbRumble()
                    }
                }

                // Lap completion check via LapTrigger (OnTriggerEnter when tagged 'Player')
                lapTrigger.checkCrossing(carPos, isPlayerTag = true)

                if (lastWaypointIndex >= waypoints.size - 2 && closestIndex <= 1) {
                    if (lapTimeSeconds > 20f) { // Valid lap threshold
                        if (bestLapSeconds == null || lapTimeSeconds < bestLapSeconds!!) {
                            bestLapSeconds = lapTimeSeconds
                        }
                        currentLap++
                        lapTimeSeconds = 0f
                        lapTrigger.onTriggerEnter()
                    }
                }
                lastWaypointIndex = closestIndex

                // 4. Generate Skid Marks when tires lock or spin
                val isAnyTireSkidding = car.frontLeft.isSkidding || car.frontRight.isSkidding ||
                        car.rearLeft.isSkidding || car.rearRight.isSkidding

                if (isAnyTireSkidding && car.Speed > 15f) {
                    val maxSlip = max(
                        max(car.frontLeft.slipRatio, car.frontRight.slipRatio),
                        max(car.rearLeft.slipRatio, car.rearRight.slipRatio)
                    )
                    skidMarks.add(
                        SkidMark(
                            start = prevPos,
                            end = carPos,
                            intensity = maxSlip.coerceIn(0.2f, 1f)
                        )
                    )
                    if (skidMarks.size > 70) {
                        skidMarks.removeAt(0)
                    }
                }

                // Age skid marks
                for (sm in skidMarks) {
                    sm.age += dt
                }

                // 5. Procedural Engine Audio Sync with Unity CarAudio
                carAudio.Update()
                soundEngine.targetRpm = car.engineRpm
                soundEngine.throttleLoad = car.acceleration
                soundEngine.tireScreechIntensity = if (isAnyTireSkidding) 0.65f else 0f
                soundEngine.pitchMultiplier = carAudio.source.pitch
                soundEngine.engineVolume = carAudio.source.volume

                // 6. Step Unity CarEffects (rear tire slip smoke particles)
                carEffects.Update(dt)

                // 7. Step Unity CarLighting (headlights and dynamic brake lights)
                carLighting.Update()
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = F1CarbonDark,
        bottomBar = {
            NavigationBar(
                containerColor = F1SurfaceDark,
                contentColor = RacingWhite
            ) {
                NavigationBarItem(
                    selected = currentTab == ScreenTab.RACE,
                    onClick = { currentTab = ScreenTab.RACE },
                    icon = {
                        Icon(
                            Icons.Default.SportsMotorsports,
                            contentDescription = "Race Track",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Race", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = F1CarbonDark,
                        selectedTextColor = F1Red,
                        indicatorColor = F1Red,
                        unselectedIconColor = RacingTextMuted,
                        unselectedTextColor = RacingTextMuted
                    ),
                    modifier = Modifier.testTag("nav_race")
                )

                NavigationBarItem(
                    selected = currentTab == ScreenTab.LIGHTS_OUT,
                    onClick = { currentTab = ScreenTab.LIGHTS_OUT },
                    icon = {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = "Lights Out",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Lights Out", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = F1CarbonDark,
                        selectedTextColor = F1Red,
                        indicatorColor = F1Red,
                        unselectedIconColor = RacingTextMuted,
                        unselectedTextColor = RacingTextMuted
                    ),
                    modifier = Modifier.testTag("nav_lights_out")
                )

                NavigationBarItem(
                    selected = currentTab == ScreenTab.TUNING,
                    onClick = { currentTab = ScreenTab.TUNING },
                    icon = {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "Physics Tuning",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Physics & Code", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = F1CarbonDark,
                        selectedTextColor = F1Red,
                        indicatorColor = F1Red,
                        unselectedIconColor = RacingTextMuted,
                        unselectedTextColor = RacingTextMuted
                    ),
                    modifier = Modifier.testTag("nav_tuning")
                )

                NavigationBarItem(
                    selected = currentTab == ScreenTab.STANDINGS,
                    onClick = { currentTab = ScreenTab.STANDINGS },
                    icon = {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = "Standings",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Standings", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = F1CarbonDark,
                        selectedTextColor = F1Red,
                        indicatorColor = F1Red,
                        unselectedIconColor = RacingTextMuted,
                        unselectedTextColor = RacingTextMuted
                    ),
                    modifier = Modifier.testTag("nav_standings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                ScreenTab.RACE -> {
                    // Full Screen Interactive 2D Racing Simulation
                    RacingCanvas(
                        car = car,
                        circuit = currentCircuit,
                        skidMarks = skidMarks,
                        cameraFollow = cameraFollow,
                        opponents = opponents,
                        carEffects = carEffects,
                        carLighting = carLighting,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Cockpit HUD and Touch Controls Overlay
                    CockpitHud(
                        car = car,
                        mobileInput = mobileInput,
                        raceManager = raceManager,
                        lapTimeSeconds = lapTimeSeconds,
                        bestLapSeconds = bestLapSeconds,
                        currentLap = currentLap,
                        currentSector = currentSector,
                        isSoundMuted = isSoundMuted,
                        onToggleSound = {
                            isSoundMuted = !isSoundMuted
                            soundEngine.isMuted = isSoundMuted
                        },
                        onResetCar = {
                            car.reset(currentCircuit.startPosition, currentCircuit.startHeading)
                            cameraFollow.reset()
                            skidMarks.clear()
                            lapTimeSeconds = 0f
                            raceManager.reset()
                            val sWp = currentCircuit.waypoints.first()
                            val nWp = currentCircuit.waypoints[1]
                            lapTrigger.reset(sWp.pos, sWp.width, (nWp.pos - sWp.pos).normalized())
                            for (i in opponents.indices) {
                                val opp = opponents[i]
                                val startWp = ((i + 1) * 4) % currentCircuit.waypoints.size
                                opp.reset(currentCircuit.waypoints[startWp].pos, currentCircuit.startHeading, startWp)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Circuit Selector & Camera View quick action floating bar
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 110.dp, end = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Track Switcher Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xCC1E1E28))
                                .border(1.dp, Color(0xFF383848), RoundedCornerShape(8.dp))
                                .clickable { isCircuitDialogVisible = true }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(currentCircuit.flag, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    currentCircuit.name.split(" ").first(),
                                    color = RacingWhite,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Camera View Switcher (Chase / Nose / TopDown)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xCC1E1E28))
                                .border(1.dp, Color(0xFF383848), RoundedCornerShape(8.dp))
                                .clickable { cameraFollow.toggleCameraMode() }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Videocam, contentDescription = "Cam", tint = TelemetryCyan, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    when (cameraFollow.mode) {
                                        CameraFollow.CameraMode.CHASE -> "CHASE"
                                        CameraFollow.CameraMode.NOSE_CAM -> "NOSE"
                                        CameraFollow.CameraMode.TOP_DOWN -> "MAP"
                                    },
                                    color = TelemetryCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                ScreenTab.LIGHTS_OUT -> {
                    LightsOutScreen(
                        onKerbVibrate = { soundEngine.vibrateKerbRumble() },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                ScreenTab.TUNING -> {
                    PhysicsTuningScreen(
                        car = car,
                        cameraFollow = cameraFollow,
                        carAudio = carAudio,
                        carEffects = carEffects,
                        carLighting = carLighting,
                        onResetDefaults = {
                            car.motorPower = 1800f
                            car.brakePower = 4000f
                            car.maxSpeed = 180f
                            car.steeringAngle = 30f
                            car.steeringResponse = 5f
                            car.tractionLimit = 0.9f
                            car.springForce = 35000f
                            car.damperForce = 4500f
                            car.rb.mass = 1200f
                            car.tireCompound = CarController.TireCompound.SOFT
                            cameraFollow.smoothSpeed = 5f
                            cameraFollow.offsetDistance = 9f
                            cameraFollow.offsetHeight = 5f
                            carAudio.idlePitch = 0.8f
                            carAudio.maxPitch = 2.0f
                            carEffects.smokeThreshold = 0.4f
                            carLighting.SetHeadlights(true)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                ScreenTab.STANDINGS -> {
                    StandingsScreen(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }

    // Circuit Selection Dialog
    if (isCircuitDialogVisible) {
        AlertDialog(
            onDismissRequest = { isCircuitDialogVisible = false },
            containerColor = F1SurfaceDark,
            title = {
                Text(
                    text = "SELECT GRAND PRIX CIRCUIT",
                    color = RacingWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TrackCatalog.ALL_CIRCUITS.forEach { circuit ->
                        val isSelected = circuit.id == currentCircuit.id
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) F1CardSurface else Color(0xFF14141C)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) F1Red else Color(0xFF2C2C3C)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentCircuit = circuit
                                    car.reset(circuit.startPosition, circuit.startHeading)
                                    cameraFollow.reset()
                                    skidMarks.clear()
                                    lapTimeSeconds = 0f
                                    raceManager.reset()
                                    val sWp = circuit.waypoints.first()
                                    val nWp = circuit.waypoints[1]
                                    lapTrigger.reset(sWp.pos, sWp.width, (nWp.pos - sWp.pos).normalized())
                                    isCircuitDialogVisible = false
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(circuit.flag, fontSize = 18.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            circuit.name,
                                            color = RacingWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Text(
                                        "${circuit.lengthKm} km",
                                        color = TelemetryCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    circuit.description,
                                    color = RacingTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { isCircuitDialogVisible = false }) {
                    Text("CLOSE", color = TelemetryCyan, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
