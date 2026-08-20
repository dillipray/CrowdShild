package com.crowdshield.stampede.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ════════════════════════════════════════════════════════════════════════════════
// COLOR TOKENS (Matching CrowdShield Command Center Light Theme)
// ════════════════════════════════════════════════════════════════════════════════
private val MapBgCard           = Color(0xFFFFFFFF)
private val MapBgSubtle         = Color(0xFFF1F5F9)
private val MapBorderSubtle     = Color(0xFFE2E8F0)
private val MapBorderStrong     = Color(0xFFCBD5E1)

private val MapTextPrimary      = Color(0xFF0F172A)
private val MapTextSecondary    = Color(0xFF475569)
private val MapTextMuted        = Color(0xFF94A3B8)

private val MapAiBlue           = Color(0xFF2563EB)
private val MapAiBlueBg         = Color(0xFFEFF6FF)
private val MapAiBlueBorder     = Color(0xFFBFDBFE)

private val MapSafeGreen        = Color(0xFF10B981)
private val MapCautionOrange    = Color(0xFFF59E0B)
private val MapHighRed          = Color(0xFFEF4444)
private val MapCriticalRed      = Color(0xFFB91C1C)

// ════════════════════════════════════════════════════════════════════════════════
// MAP DATA MODELS (Ready for future Mappls SDK & Live GPS Integration)
// ════════════════════════════════════════════════════════════════════════════════

/**
 * Geographic coordinate model.
 * Connect to android.location.Location or Mappls LatLng in the future.
 */
data class MapLocation(
    val latitude: Double,
    val longitude: Double,
    val label: String,
    val accuracyMeters: Float = 5.0f
)

/**
 * Marker types supported by the CrowdShield spatial engine.
 */
enum class MapMarkerType {
    CURRENT_LOCATION,
    CAMERA,
    CROWD,
    INCIDENT,
    SAFE_ZONE,
    RISK_ZONE
}

/**
 * Risk classification for zones and markers.
 */
enum class MapRiskLevel(
    val label: String,
    val color: Color,
    val bgColor: Color
) {
    SAFE("SAFE", MapSafeGreen, Color(0xFFECFDF5)),
    CAUTION("CAUTION", MapCautionOrange, Color(0xFFFFFBEB)),
    HIGH("HIGH", MapHighRed, Color(0xFFFEF2F2)),
    CRITICAL("CRITICAL", MapCriticalRed, Color(0xFF450A0A))
}

/**
 * Spatial marker entity.
 */
data class MapMarker(
    val id: String,
    val location: MapLocation,
    val type: MapMarkerType,
    val title: String,
    val riskLevel: MapRiskLevel
)

/**
 * Sector boundary definition for venue layouts.
 */
data class MapSectorBoundary(
    val id: Int,
    val name: String,
    val subLocation: String,
    val riskLevel: MapRiskLevel,
    val color: Color
)

// ════════════════════════════════════════════════════════════════════════════════
// MAP PROVIDER ABSTRACTION LAYER
// ════════════════════════════════════════════════════════════════════════════════

/**
 * Interface representing a map rendering provider.
 * Currently backed by [MockCurrentLocationMapProvider].
 * When Mappls is ready, implement [MapplsLocationMapProvider] without changing the UI layer!
 */
interface MapProvider {
    @Composable
    fun RenderMap(
        modifier: Modifier,
        currentLocation: MapLocation,
        markers: List<MapMarker>,
        sectors: List<MapSectorBoundary>,
        zoomLevel: Float,
        onRecenter: () -> Unit
    )
}

/**
 * Mock/Placeholder map provider creating a realistic operational command map canvas.
 */
class MockCurrentLocationMapProvider : MapProvider {
    @Composable
    override fun RenderMap(
        modifier: Modifier,
        currentLocation: MapLocation,
        markers: List<MapMarker>,
        sectors: List<MapSectorBoundary>,
        zoomLevel: Float,
        onRecenter: () -> Unit
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "gpsPulse")
        val pulseRing by infiniteTransition.animateFloat(
            initialValue = 12f,
            targetValue = 28f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "gpsPulseRadius"
        )
        val pulseAlpha by infiniteTransition.animateFloat(
            initialValue = 0.7f,
            targetValue = 0.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "gpsPulseAlpha"
        )

        Box(modifier = modifier) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // 1. Map Base Surface & Grid Lines (Simulating Geographic Tile Grid)
                drawRect(color = Color(0xFFF8FAFC))

                val gridStep = (36 * zoomLevel).dp.toPx()
                var gx = 0f
                while (gx < w) {
                    drawLine(Color(0xFFE2E8F0), Offset(gx, 0f), Offset(gx, h), strokeWidth = 0.8f)
                    gx += gridStep
                }
                var gy = 0f
                while (gy < h) {
                    drawLine(Color(0xFFE2E8F0), Offset(0f, gy), Offset(w, gy), strokeWidth = 0.8f)
                    gy += gridStep
                }

                // 2. Arterial Roads & Concourse Avenues (Map Topology)
                val mainRoadColor = Color(0xFFFFFFFF)
                val mainRoadBorder = Color(0xFFCBD5E1)

                // Perimeter Concourse Ring Road
                drawRoundRect(
                    color = mainRoadBorder,
                    topLeft = Offset(w * 0.06f, h * 0.08f),
                    size = Size(w * 0.88f, h * 0.84f),
                    cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                )
                drawRoundRect(
                    color = mainRoadColor,
                    topLeft = Offset(w * 0.06f, h * 0.08f),
                    size = Size(w * 0.88f, h * 0.84f),
                    cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                    style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                )

                // Central Concourse Cross Avenues
                drawLine(mainRoadBorder, Offset(w * 0.50f, h * 0.08f), Offset(w * 0.50f, h * 0.92f), strokeWidth = 8.dp.toPx())
                drawLine(mainRoadColor, Offset(w * 0.50f, h * 0.08f), Offset(w * 0.50f, h * 0.92f), strokeWidth = 5.dp.toPx())

                drawLine(mainRoadBorder, Offset(w * 0.06f, h * 0.50f), Offset(w * 0.94f, h * 0.50f), strokeWidth = 8.dp.toPx())
                drawLine(mainRoadColor, Offset(w * 0.06f, h * 0.50f), Offset(w * 0.94f, h * 0.50f), strokeWidth = 5.dp.toPx())

                // 3. Sector Overlay Boundaries
                val s1Pos = Offset(w * 0.10f, h * 0.12f)
                val s2Pos = Offset(w * 0.10f, h * 0.54f)
                val s3Pos = Offset(w * 0.54f, h * 0.12f)
                val s4Pos = Offset(w * 0.54f, h * 0.54f)
                val sSize = Size(w * 0.36f, h * 0.34f)

                fun drawSectorZone(pos: Offset, color: Color) {
                    drawRoundRect(
                        color = color.copy(alpha = 0.12f),
                        topLeft = pos,
                        size = sSize,
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )
                    drawRoundRect(
                        color = color.copy(alpha = 0.45f),
                        topLeft = pos,
                        size = sSize,
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                        style = Stroke(width = 1.2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
                    )
                }

                drawSectorZone(s1Pos, MapSafeGreen)
                drawSectorZone(s2Pos, MapSafeGreen)
                drawSectorZone(s3Pos, MapCautionOrange)
                drawSectorZone(s4Pos, MapHighRed)

                // 4. Incident Marker (Sector 4)
                val incPos = Offset(w * 0.72f, h * 0.70f)
                drawCircle(color = MapHighRed.copy(alpha = 0.3f), radius = 10.dp.toPx(), center = incPos)
                drawCircle(color = MapHighRed, radius = 5.dp.toPx(), center = incPos)
                drawCircle(color = Color.White, radius = 2.dp.toPx(), center = incPos)

                // 5. CURRENT LOCATION GPS MARKER (Pulsing Beacon)
                val currentGpsOffset = Offset(w * 0.44f, h * 0.46f)

                // Outer radar pulse wave
                drawCircle(
                    color = MapAiBlue.copy(alpha = pulseAlpha),
                    radius = pulseRing.dp.toPx(),
                    center = currentGpsOffset
                )

                // Accuracy circle
                drawCircle(
                    color = MapAiBlue.copy(alpha = 0.18f),
                    radius = 16.dp.toPx(),
                    center = currentGpsOffset
                )

                // Outer white halo
                drawCircle(
                    color = Color.White,
                    radius = 7.5.dp.toPx(),
                    center = currentGpsOffset
                )

                // Core GPS Location Pin
                drawCircle(
                    color = MapAiBlue,
                    radius = 5.5.dp.toPx(),
                    center = currentGpsOffset
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = currentGpsOffset
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// REUSABLE CURRENT LOCATION MAP COMPOSABLE (For Dashboard Top)
// ════════════════════════════════════════════════════════════════════════════════
@Composable
fun CurrentLocationMapCard(
    modifier: Modifier = Modifier,
    mapProvider: MapProvider = remember { MockCurrentLocationMapProvider() },
    onOpenFullMap: () -> Unit = {}
) {
    // Mock Default GPS Coordinates (Can be updated via GPS listener / ViewModel later)
    val defaultLocation = remember {
        MapLocation(
            latitude = 28.6139,
            longitude = 77.2090,
            label = "HQ Command Unit · Central Gate",
            accuracyMeters = 4.2f
        )
    }

    var currentLocation by remember { mutableStateOf(defaultLocation) }
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }

    // Mock Sectors
    val sectors = remember {
        listOf(
            MapSectorBoundary(1, "Sector 1", "North Gate Entry", MapRiskLevel.SAFE, MapSafeGreen),
            MapSectorBoundary(2, "Sector 2", "West Concourse", MapRiskLevel.SAFE, MapSafeGreen),
            MapSectorBoundary(3, "Sector 3", "Arena Stage", MapRiskLevel.CAUTION, MapCautionOrange),
            MapSectorBoundary(4, "Sector 4", "East Concourse", MapRiskLevel.HIGH, MapHighRed)
        )
    }

    // Mock Markers
    val markers = remember {
        listOf(
            MapMarker("M1", currentLocation, MapMarkerType.CURRENT_LOCATION, "Staff Officer (You)", MapRiskLevel.SAFE),
            MapMarker("M2", MapLocation(28.6145, 77.2098, "Sector 4 Corridor"), MapMarkerType.INCIDENT, "Incident #409", MapRiskLevel.HIGH)
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MapBgCard),
        border = BorderStroke(1.dp, MapBorderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── TOP HEADER BAR ───────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(MapAiBlueBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            tint = MapAiBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "CURRENT LOCATION & FIELD MAP",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black,
                            color = MapTextPrimary,
                            letterSpacing = 0.6.sp
                        )
                        Text(
                            text = "Staff Field Post · Zone Alpha",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MapTextSecondary
                        )
                    }
                }

                // Ready Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MapAiBlueBg)
                        .border(1.dp, MapAiBlueBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MapAiBlue)
                        )
                        Text(
                            text = "MAP READY",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MapAiBlue
                        )
                    }
                }
            }

            // ── MAP CONTAINER (Responsive Map Surface with Overlay Controls) ─
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MapBgSubtle)
                    .border(1.dp, MapBorderSubtle, RoundedCornerShape(12.dp))
            ) {
                // Map Rendering (Via Abstraction Provider)
                mapProvider.RenderMap(
                    modifier = Modifier.fillMaxSize(),
                    currentLocation = currentLocation,
                    markers = markers,
                    sectors = sectors,
                    zoomLevel = zoomLevel,
                    onRecenter = {
                        currentLocation = defaultLocation
                        zoomLevel = 1.0f
                    }
                )

                // Current Location Floating Pill (Top-Left)
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White.copy(alpha = 0.94f),
                    border = BorderStroke(1.dp, MapBorderSubtle),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(MapAiBlue)
                        )
                        Text(
                            text = "Current Location: Sector 1/2 Hub",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MapTextPrimary
                        )
                    }
                }

                // Floating Zoom Controls (Top-Right)
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White.copy(alpha = 0.94f),
                    border = BorderStroke(1.dp, MapBorderSubtle),
                    shadowElevation = 2.dp
                ) {
                    Column {
                        IconButton(
                            onClick = { zoomLevel = (zoomLevel + 0.2f).coerceAtMost(2.0f) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Zoom In",
                                tint = MapTextPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Box(modifier = Modifier.width(28.dp).height(1.dp).background(MapBorderSubtle))
                        IconButton(
                            onClick = { zoomLevel = (zoomLevel - 0.2f).coerceAtLeast(0.6f) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Zoom Out",
                                tint = MapTextPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Floating Recenter Button (Bottom-Right)
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clickable {
                            currentLocation = defaultLocation
                            zoomLevel = 1.0f
                        },
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(1.dp, MapAiBlueBorder),
                    shadowElevation = 3.dp
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Recenter Map",
                            tint = MapAiBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Sector Mini Legend (Bottom-Left)
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White.copy(alpha = 0.94f),
                    border = BorderStroke(1.dp, MapBorderSubtle),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MiniSectorTag("S1", MapSafeGreen)
                        MiniSectorTag("S2", MapSafeGreen)
                        MiniSectorTag("S3", MapCautionOrange)
                        MiniSectorTag("S4", MapHighRed)
                    }
                }
            }

            // ── COORDINATE & STATUS FOOTER ───────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NearMe,
                        contentDescription = null,
                        tint = MapTextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "GPS: ${currentLocation.latitude}° N, ${currentLocation.longitude}° E (±${currentLocation.accuracyMeters}m)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MapTextSecondary
                    )
                }

                Text(
                    text = "Mappls SDK Ready",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MapAiBlue
                )
            }
        }
    }
}

@Composable
private fun MiniSectorTag(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MapTextPrimary
        )
    }
}
