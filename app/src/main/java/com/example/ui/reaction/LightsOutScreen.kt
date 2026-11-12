package com.example.ui.reaction

import android.os.SystemClock
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
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
import com.example.ui.theme.F1CarbonDark
import com.example.ui.theme.F1CardSurface
import com.example.ui.theme.F1Red
import com.example.ui.theme.F1SurfaceDark
import com.example.ui.theme.RacingTextMuted
import com.example.ui.theme.RacingWhite
import com.example.ui.theme.SectorGreen
import com.example.ui.theme.SectorPurple
import com.example.ui.theme.TelemetryCyan
import com.example.ui.theme.WarningRed
import kotlinx.coroutines.delay
import kotlin.random.Random

enum class LightsState {
    IDLE,
    COUNTDOWN, // 1 to 5 red lights turning on
    HOLDING,   // All 5 on, waiting for random lights out
    LIGHTS_OUT,// Tap now!
    FALSE_START,
    FINISHED
}

@Composable
fun LightsOutScreen(
    onKerbVibrate: () -> Unit,
    modifier: Modifier = Modifier
) {
    var gameState by remember { mutableStateOf(LightsState.IDLE) }
    var activeRedLights by remember { mutableIntStateOf(0) }
    var lightsOutTimestamp by remember { mutableLongStateOf(0L) }
    var reactionTimeMs by remember { mutableLongStateOf(0L) }
    var bestTimeMs by remember { mutableStateOf<Long?>(null) }
    val history = remember { mutableStateListOf<Long>() }

    // Coroutine loop for countdown sequence
    LaunchedEffect(gameState) {
        if (gameState == LightsState.COUNTDOWN) {
            activeRedLights = 0
            for (i in 1..5) {
                delay(1000)
                activeRedLights = i
                onKerbVibrate()
            }
            gameState = LightsState.HOLDING
            // Random FIA lights out hold: 1.0s to 3.2s
            val holdTime = Random.nextLong(1000, 3200)
            delay(holdTime)
            if (gameState == LightsState.HOLDING) {
                activeRedLights = 0
                lightsOutTimestamp = SystemClock.elapsedRealtime()
                gameState = LightsState.LIGHTS_OUT
                onKerbVibrate()
            }
        }
    }

    fun handleTap() {
        when (gameState) {
            LightsState.IDLE, LightsState.FINISHED, LightsState.FALSE_START -> {
                // Start new round
                gameState = LightsState.COUNTDOWN
            }
            LightsState.COUNTDOWN, LightsState.HOLDING -> {
                // Jump start!
                gameState = LightsState.FALSE_START
                onKerbVibrate()
            }
            LightsState.LIGHTS_OUT -> {
                // Legitimate reaction!
                val now = SystemClock.elapsedRealtime()
                reactionTimeMs = now - lightsOutTimestamp
                if (bestTimeMs == null || reactionTimeMs < bestTimeMs!!) {
                    bestTimeMs = reactionTimeMs
                }
                history.add(0, reactionTimeMs)
                gameState = LightsState.FINISHED
                onKerbVibrate()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(F1CarbonDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "LIGHTS OUT REACTION TEST",
                    color = RacingWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "FIA Formula 1 Grand Prix Start Procedure",
                    color = RacingTextMuted,
                    fontSize = 12.sp
                )
            }
            bestTimeMs?.let { best ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${best}ms",
                        color = Color(0xFFFFD700),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // THE 5-LIGHT F1 GANTRY
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0C0C10))
                .border(2.dp, Color(0xFF2E2E3A), RoundedCornerShape(16.dp))
                .padding(vertical = 24.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (column in 1..5) {
                    val isLit = column <= activeRedLights
                    GantryColumn(isLit = isLit)
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // MAIN TAP LAUNCH PADDLE / REACTION DISPLAY
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .testTag("launch_paddle")
                .clip(RoundedCornerShape(20.dp))
                .background(
                    when (gameState) {
                        LightsState.LIGHTS_OUT -> SectorGreen
                        LightsState.FALSE_START -> WarningRed
                        LightsState.HOLDING -> Color(0xFF261010)
                        else -> F1CardSurface
                    }
                )
                .border(
                    2.dp,
                    when (gameState) {
                        LightsState.LIGHTS_OUT -> SectorGreen
                        LightsState.FALSE_START -> WarningRed
                        else -> Color(0xFF38384A)
                    },
                    RoundedCornerShape(20.dp)
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { handleTap() }
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (gameState) {
                    LightsState.IDLE -> {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = TelemetryCyan, modifier = Modifier.size(42.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("TAP TO START SEQUENCE", color = RacingWhite, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Text("Wait for all 5 lights... tap when they GO OUT!", color = RacingTextMuted, fontSize = 11.sp)
                    }
                    LightsState.COUNTDOWN -> {
                        Text("FORMING ON THE GRID...", color = TelemetryCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Lights $activeRedLights of 5 on", color = RacingTextMuted, fontSize = 12.sp)
                    }
                    LightsState.HOLDING -> {
                        Text("HOLD...", color = WarningRed, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        Text("DO NOT JUMP THE START!", color = RacingTextMuted, fontSize = 12.sp)
                    }
                    LightsState.LIGHTS_OUT -> {
                        Text("LIGHTS OUT!", color = F1CarbonDark, fontSize = 28.sp, fontWeight = FontWeight.Black)
                        Text("TAP NOW! AWAY WE GO!", color = F1CarbonDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    LightsState.FALSE_START -> {
                        Text("JUMP START! 5s PENALTY", color = RacingWhite, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Text("You launched before lights extinguished. Tap to retry.", color = RacingWhite.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                    LightsState.FINISHED -> {
                        Text(
                            text = "${reactionTimeMs}ms",
                            color = RacingWhite,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        val rating = when {
                            reactionTimeMs < 200 -> "VERSTAPPEN TIER (Alien Reflexes!)"
                            reactionTimeMs < 240 -> "HAMILTON TIER (Championship Launch!)"
                            reactionTimeMs < 280 -> "PODIUM CONTENDER"
                            reactionTimeMs < 350 -> "MIDFIELD RUNNER"
                            else -> "SLOW GETAWAY (Stalled Clutch)"
                        }
                        Text(text = rating, color = SectorGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Tap to try again", color = RacingTextMuted, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // F1 BENCHMARKS & ATTEMPTS HISTORY
        Card(
            colors = CardDefaults.cardColors(containerColor = F1CardSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("REAL F1 DRIVER BENCHMARKS", color = RacingWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))

                DriverBenchmarkRow("Max Verstappen (Red Bull)", "0.192s", SectorGreen)
                DriverBenchmarkRow("Lewis Hamilton (Ferrari)", "0.205s", TelemetryCyan)
                DriverBenchmarkRow("Charles Leclerc (Ferrari)", "0.210s", F1Red)
                DriverBenchmarkRow("Fernando Alonso (Aston Martin)", "0.188s", SectorPurple)

                if (history.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("RECENT REACTION TIMES", color = RacingWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        history.take(4).forEachIndexed { index, time ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF191922))
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("#${index + 1}", color = RacingTextMuted, fontSize = 9.sp)
                                    Text("${time}ms", color = RacingWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GantryColumn(isLit: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF16161E))
            .padding(vertical = 10.dp, horizontal = 6.dp)
    ) {
        // Dual vertically-stacked red lights per column (FIA standard)
        RedBulb(isLit = isLit)
        RedBulb(isLit = isLit)
    }
}

@Composable
private fun RedBulb(isLit: Boolean) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(if (isLit) F1Red else Color(0xFF281111))
            .border(
                1.5.dp,
                if (isLit) Color(0xFFFF6666) else Color(0xFF3B1E1E),
                CircleShape
            )
    )
}

@Composable
private fun DriverBenchmarkRow(name: String, time: String, teamColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(teamColor))
            Spacer(modifier = Modifier.width(8.dp))
            Text(name, color = RacingTextMuted, fontSize = 12.sp)
        }
        Text(time, color = RacingWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}
