package com.example.track

import com.example.physics.Vector2D
import kotlin.math.sqrt

data class TrackWaypoint(
    val pos: Vector2D,
    val width: Float = 16f, // Track width in meters
    val hasLeftKerb: Boolean = false,
    val hasRightKerb: Boolean = false,
    val isDrsZone: Boolean = false,
    val sector: Int = 1,
    val name: String = ""
)

data class SkidMark(
    val start: Vector2D,
    val end: Vector2D,
    val intensity: Float, // 0f to 1f
    var age: Float = 0f
)

class Circuit(
    val id: String,
    val name: String,
    val country: String,
    val flag: String,
    val lengthKm: Float,
    val turns: Int,
    val startPosition: Vector2D,
    val startHeading: Float,
    val waypoints: List<TrackWaypoint>,
    val description: String
)

object TrackCatalog {

    val MONZA: Circuit by lazy {
        val pts = mutableListOf<TrackWaypoint>()
        val w = 18f

        // Main Straight (DRS 1) - Sector 1
        pts.add(TrackWaypoint(Vector2D(0f, 0f), w, isDrsZone = true, sector = 1, name = "Start / Finish"))
        pts.add(TrackWaypoint(Vector2D(0f, -80f), w, isDrsZone = true, sector = 1, name = "Pit Straight"))
        pts.add(TrackWaypoint(Vector2D(0f, -160f), w, isDrsZone = true, sector = 1, name = "DRS Zone 1"))
        pts.add(TrackWaypoint(Vector2D(0f, -240f), w, isDrsZone = true, sector = 1, name = "Braking Zone"))

        // Variante del Rettifilo Chicane (Turns 1 & 2)
        pts.add(TrackWaypoint(Vector2D(10f, -270f), 14f, hasRightKerb = true, sector = 1, name = "Rettifilo (T1)"))
        pts.add(TrackWaypoint(Vector2D(-5f, -295f), 14f, hasLeftKerb = true, sector = 1, name = "Prima Variante (T2)"))

        // Curva Grande (Turn 3)
        pts.add(TrackWaypoint(Vector2D(25f, -340f), w, hasRightKerb = true, sector = 1, name = "Curva Grande Entry"))
        pts.add(TrackWaypoint(Vector2D(70f, -380f), w, hasRightKerb = true, sector = 1, name = "Curva Grande Apex"))
        pts.add(TrackWaypoint(Vector2D(120f, -400f), w, sector = 1, name = "Curva Grande Exit"))

        // Sector 2: Variante della Roggia (Turns 4 & 5)
        pts.add(TrackWaypoint(Vector2D(180f, -400f), w, sector = 2, name = "Sector 2 Split"))
        pts.add(TrackWaypoint(Vector2D(230f, -395f), 14f, hasLeftKerb = true, sector = 2, name = "Variante Roggia (T4)"))
        pts.add(TrackWaypoint(Vector2D(250f, -370f), 14f, hasRightKerb = true, sector = 2, name = "Roggia Exit (T5)"))

        // Curva di Lesmo 1 & 2 (Turns 6 & 7)
        pts.add(TrackWaypoint(Vector2D(260f, -310f), w, hasRightKerb = true, sector = 2, name = "Lesmo 1 (T6)"))
        pts.add(TrackWaypoint(Vector2D(250f, -250f), w, sector = 2, name = "Lesmo Straight"))
        pts.add(TrackWaypoint(Vector2D(230f, -190f), w, hasRightKerb = true, sector = 2, name = "Lesmo 2 (T7)"))

        // Serraglio Straight (DRS 2)
        pts.add(TrackWaypoint(Vector2D(200f, -120f), w, isDrsZone = true, sector = 2, name = "Serraglio DRS 2"))
        pts.add(TrackWaypoint(Vector2D(160f, -50f), w, isDrsZone = true, sector = 2, name = "Under Bridge"))

        // Sector 3: Variante Ascari (Turns 8, 9, 10)
        pts.add(TrackWaypoint(Vector2D(130f, 15f), 14f, hasLeftKerb = true, sector = 3, name = "Ascari Entry (T8)"))
        pts.add(TrackWaypoint(TrackWaypoint(Vector2D(110f, 35f), 14f, hasRightKerb = true, sector = 3, name = "Ascari Apex (T9)").pos, 14f, sector = 3, hasRightKerb = true))
        pts.add(TrackWaypoint(Vector2D(90f, 40f), 14f, hasLeftKerb = true, sector = 3, name = "Ascari Exit (T10)"))

        // Back Straight towards Parabolica
        pts.add(TrackWaypoint(Vector2D(40f, 40f), w, sector = 3, name = "Back Straight"))
        pts.add(TrackWaypoint(Vector2D(0f, 40f), w, sector = 3, name = "Parabolica Approach"))

        // Curva Parabolica / Curva Alboreto (Turn 11)
        pts.add(TrackWaypoint(Vector2D(-40f, 45f), w, hasRightKerb = true, sector = 3, name = "Parabolica Entry (T11)"))
        pts.add(TrackWaypoint(Vector2D(-55f, 25f), w, hasRightKerb = true, sector = 3, name = "Parabolica Apex"))
        pts.add(TrackWaypoint(Vector2D(-35f, 5f), w, sector = 3, name = "Parabolica Exit"))

        Circuit(
            id = "monza",
            name = "Autodromo Nazionale Monza",
            country = "Italy",
            flag = "🇮🇹",
            lengthKm = 5.793f,
            turns = 11,
            startPosition = Vector2D(0f, 10f),
            startHeading = -90f, // Facing north along straight
            waypoints = pts,
            description = "The legendary Temple of Speed. Long straights, violent braking zones into tight chicanes, and the iconic Parabolica."
        )
    }

    val SILVERSTONE: Circuit by lazy {
        val pts = mutableListOf<TrackWaypoint>()
        val w = 17f

        // Hamilton Straight
        pts.add(TrackWaypoint(Vector2D(0f, 0f), w, isDrsZone = true, sector = 1, name = "Hamilton Straight"))
        pts.add(TrackWaypoint(Vector2D(0f, -80f), w, isDrsZone = true, sector = 1, name = "Abbey Approach"))

        // Abbey (T1) & Farm (T2)
        pts.add(TrackWaypoint(Vector2D(30f, -120f), w, hasRightKerb = true, sector = 1, name = "Abbey (T1)"))
        pts.add(TrackWaypoint(Vector2D(40f, -160f), w, sector = 1, name = "Farm Curve"))

        // Village & The Loop (T3, T4)
        pts.add(TrackWaypoint(Vector2D(15f, -200f), 13f, hasRightKerb = true, sector = 1, name = "Village (T3)"))
        pts.add(TrackWaypoint(Vector2D(-25f, -210f), 13f, hasLeftKerb = true, sector = 1, name = "The Loop (T4)"))
        pts.add(TrackWaypoint(Vector2D(-30f, -170f), w, sector = 1, name = "Aintree (T5)"))

        // Wellington Straight (DRS 2) - Sector 2
        pts.add(TrackWaypoint(Vector2D(-30f, -110f), w, isDrsZone = true, sector = 2, name = "Wellington Straight"))
        pts.add(TrackWaypoint(Vector2D(-30f, -40f), w, isDrsZone = true, sector = 2, name = "Brooklands Braking"))

        // Brooklands & Luffield (T6, T7)
        pts.add(TrackWaypoint(Vector2D(-65f, 0f), 14f, hasLeftKerb = true, sector = 2, name = "Brooklands (T6)"))
        pts.add(TrackWaypoint(Vector2D(-80f, 40f), 14f, hasRightKerb = true, sector = 2, name = "Luffield (T7)"))
        pts.add(TrackWaypoint(Vector2D(-55f, 65f), w, sector = 2, name = "Woodcote (T8)"))

        // Copse Corner (T9) - High Speed!
        pts.add(TrackWaypoint(Vector2D(20f, 75f), w, sector = 2, name = "National Straight"))
        pts.add(TrackWaypoint(Vector2D(80f, 75f), w, hasRightKerb = true, sector = 2, name = "Copse (T9)"))

        // Sector 3: Maggotts, Becketts & Chapel (T10 - T14)
        pts.add(TrackWaypoint(Vector2D(125f, 50f), 15f, hasLeftKerb = true, sector = 3, name = "Maggotts (T10)"))
        pts.add(TrackWaypoint(Vector2D(145f, 20f), 14f, hasRightKerb = true, sector = 3, name = "Becketts (T12)"))
        pts.add(TrackWaypoint(Vector2D(145f, -20f), 15f, hasLeftKerb = true, sector = 3, name = "Chapel (T14)"))

        // Hangar Straight
        pts.add(TrackWaypoint(Vector2D(120f, -70f), w, isDrsZone = true, sector = 3, name = "Hangar Straight"))
        pts.add(TrackWaypoint(Vector2D(80f, -50f), 14f, hasRightKerb = true, sector = 3, name = "Stowe (T15)"))

        // Vale & Club (T16, T17)
        pts.add(TrackWaypoint(Vector2D(40f, -20f), 13f, hasLeftKerb = true, sector = 3, name = "Vale (T16)"))
        pts.add(TrackWaypoint(Vector2D(15f, -5f), 14f, hasRightKerb = true, sector = 3, name = "Club (T17)"))

        Circuit(
            id = "silverstone",
            name = "Silverstone Circuit",
            country = "United Kingdom",
            flag = "🇬🇧",
            lengthKm = 5.891f,
            turns = 18,
            startPosition = Vector2D(0f, 20f),
            startHeading = -90f,
            waypoints = pts,
            description = "The home of British motorsport. Renowned for blisteringly fast corner sequences like Copse and the Maggotts-Becketts complex."
        )
    }

    val PROVING_GROUNDS: Circuit by lazy {
        val pts = mutableListOf<TrackWaypoint>()
        val w = 24f

        // Drag Strip (0 to 600m)
        pts.add(TrackWaypoint(Vector2D(0f, 100f), w, sector = 1, name = "Drag Strip Start"))
        pts.add(TrackWaypoint(Vector2D(0f, 0f), w, isDrsZone = true, sector = 1, name = "0-100 km/h Trap"))
        pts.add(TrackWaypoint(Vector2D(0f, -150f), w, isDrsZone = true, sector = 1, name = "Quarter Mile (400m)"))
        pts.add(TrackWaypoint(Vector2D(0f, -300f), w, isDrsZone = true, sector = 1, name = "Top Speed Trap (600m)"))
        pts.add(TrackWaypoint(Vector2D(0f, -420f), w, sector = 1, name = "Braking Benchmark 100-0"))

        // Hairpin Turnaround
        pts.add(TrackWaypoint(Vector2D(40f, -460f), 20f, hasRightKerb = true, sector = 2, name = "Test Loop North"))
        pts.add(TrackWaypoint(Vector2D(80f, -420f), 20f, hasRightKerb = true, sector = 2, name = "Loop Apex"))

        // Slalom & Skidpad Section
        pts.add(TrackWaypoint(Vector2D(90f, -300f), 22f, sector = 2, name = "Slalom Entry"))
        pts.add(TrackWaypoint(Vector2D(65f, -220f), 20f, hasLeftKerb = true, sector = 2, name = "Slalom Gate 1"))
        pts.add(TrackWaypoint(Vector2D(105f, -140f), 20f, hasRightKerb = true, sector = 2, name = "Slalom Gate 2"))
        pts.add(TrackWaypoint(Vector2D(75f, -60f), 20f, hasLeftKerb = true, sector = 2, name = "Slalom Gate 3"))

        // South Skidpad Loop
        pts.add(TrackWaypoint(Vector2D(60f, 30f), 22f, hasRightKerb = true, sector = 3, name = "Skidpad Entry"))
        pts.add(TrackWaypoint(Vector2D(40f, 90f), 22f, hasRightKerb = true, sector = 3, name = "Skidpad Lateral G Test"))
        pts.add(TrackWaypoint(Vector2D(10f, 100f), w, sector = 3, name = "Return to Launch"))

        Circuit(
            id = "proving_grounds",
            name = "Apex F1 Proving Grounds",
            country = "Aerodynamics Center",
            flag = "🏁",
            lengthKm = 3.200f,
            turns = 6,
            startPosition = Vector2D(0f, 80f),
            startHeading = -90f,
            waypoints = pts,
            description = "High-speed engineering testing facility. Includes a 1/2 mile drag strip with 0-100 km/h and braking traps, plus a dynamic steering slalom."
        )
    }

    val ALL_CIRCUITS = listOf(MONZA, SILVERSTONE, PROVING_GROUNDS)
}
