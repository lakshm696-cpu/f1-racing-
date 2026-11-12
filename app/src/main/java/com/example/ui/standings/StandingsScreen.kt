package com.example.ui.standings

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.ui.theme.TelemetryCyan

data class DriverStanding(
    val pos: Int,
    val name: String,
    val code: String,
    val number: Int,
    val team: String,
    val points: Int,
    val wins: Int,
    val teamColor: Color
)

data class ConstructorStanding(
    val pos: Int,
    val team: String,
    val engine: String,
    val points: Int,
    val wins: Int,
    val teamColor: Color
)

@Composable
fun StandingsScreen(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val drivers = remember {
        listOf(
            DriverStanding(1, "Max Verstappen", "VER", 1, "Red Bull Racing", 437, 9, Color(0xFF1E41FF)),
            DriverStanding(2, "Lando Norris", "NOR", 4, "McLaren F1 Team", 374, 4, Color(0xFFFF8700)),
            DriverStanding(3, "Charles Leclerc", "LEC", 16, "Scuderia Ferrari", 356, 3, Color(0xFFE8002D)),
            DriverStanding(4, "Oscar Piastri", "PIA", 81, "McLaren F1 Team", 292, 2, Color(0xFFFF8700)),
            DriverStanding(5, "Carlos Sainz", "SAI", 55, "Williams Racing", 290, 2, Color(0xFF005AFF)),
            DriverStanding(6, "George Russell", "RUS", 63, "Mercedes-AMG Petronas", 245, 2, Color(0xFF00A19B)),
            DriverStanding(7, "Lewis Hamilton", "HAM", 44, "Scuderia Ferrari", 223, 2, Color(0xFFE8002D)),
            DriverStanding(8, "Sergio Perez", "PER", 11, "Red Bull Racing", 152, 0, Color(0xFF1E41FF)),
            DriverStanding(9, "Fernando Alonso", "ALO", 14, "Aston Martin Aramco", 70, 0, Color(0xFF229971)),
            DriverStanding(10, "Pierre Gasly", "GAS", 10, "Alpine F1 Team", 42, 0, Color(0xFF0093CC))
        )
    }

    val constructors = remember {
        listOf(
            ConstructorStanding(1, "McLaren F1 Team", "Mercedes-AMG", 666, 6, Color(0xFFFF8700)),
            ConstructorStanding(2, "Scuderia Ferrari", "Ferrari 066/12", 652, 5, Color(0xFFE8002D)),
            ConstructorStanding(3, "Red Bull Racing", "Honda RBPT", 589, 9, Color(0xFF1E41FF)),
            ConstructorStanding(4, "Mercedes-AMG Petronas", "Mercedes-AMG", 468, 4, Color(0xFF00A19B)),
            ConstructorStanding(5, "Aston Martin Aramco", "Mercedes-AMG", 94, 0, Color(0xFF229971)),
            ConstructorStanding(6, "Alpine F1 Team", "Renault", 65, 0, Color(0xFF0093CC)),
            ConstructorStanding(7, "Haas F1 Team", "Ferrari", 58, 0, Color(0xFFB6BABD)),
            ConstructorStanding(8, "Visa Cash App RB", "Honda RBPT", 46, 0, Color(0xFF6692FF)),
            ConstructorStanding(9, "Williams Racing", "Mercedes-AMG", 17, 0, Color(0xFF005AFF)),
            ConstructorStanding(10, "Stake F1 Team Kick Sauber", "Ferrari", 4, 0, Color(0xFF52E252))
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(F1CarbonDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(F1Red)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "WORLD CHAMPIONSHIP STANDINGS",
                    color = RacingWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "FIA Formula One Official Points",
                    color = RacingTextMuted,
                    fontSize = 12.sp
                )
            }
        }

        // Tabs
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
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("DRIVERS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("CONSTRUCTORS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (selectedTab == 0) {
                items(drivers) { driver ->
                    DriverCard(driver)
                }
            } else {
                items(constructors) { team ->
                    ConstructorCard(team)
                }
            }
        }
    }
}

@Composable
private fun DriverCard(driver: DriverStanding) {
    Card(
        colors = CardDefaults.cardColors(containerColor = F1CardSurface),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Position Box
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (driver.pos) {
                                1 -> Color(0xFFFFD700)
                                2 -> Color(0xFFC0C0C0)
                                3 -> Color(0xFFCD7F32)
                                else -> Color(0xFF2E2E3C)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = driver.pos.toString(),
                        color = if (driver.pos <= 3) F1CarbonDark else RacingWhite,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Team color vertical strip
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(34.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(driver.teamColor)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = driver.name,
                            color = RacingWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "#${driver.number}",
                            color = RacingTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = driver.team,
                        color = RacingTextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // Points
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${driver.points} PTS",
                    color = TelemetryCyan,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace
                )
                if (driver.wins > 0) {
                    Text(
                        text = "${driver.wins} ${if (driver.wins == 1) "WIN" else "WINS"}",
                        color = SectorGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ConstructorCard(team: ConstructorStanding) {
    Card(
        colors = CardDefaults.cardColors(containerColor = F1CardSurface),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Position Box
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (team.pos) {
                                1 -> Color(0xFFFFD700)
                                2 -> Color(0xFFC0C0C0)
                                3 -> Color(0xFFCD7F32)
                                else -> Color(0xFF2E2E3C)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = team.pos.toString(),
                        color = if (team.pos <= 3) F1CarbonDark else RacingWhite,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Team color vertical strip
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(34.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(team.teamColor)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = team.team,
                        color = RacingWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "PU: ${team.engine}",
                        color = RacingTextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // Points
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${team.points} PTS",
                    color = TelemetryCyan,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace
                )
                if (team.wins > 0) {
                    Text(
                        text = "${team.wins} ${if (team.wins == 1) "WIN" else "WINS"}",
                        color = SectorGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
