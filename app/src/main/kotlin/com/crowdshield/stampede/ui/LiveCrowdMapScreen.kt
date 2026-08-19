package com.crowdshield.stampede.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoorBack
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

enum class MapSector(
    val id: String,
    val title: String,
    val riskLabel: String,
    val densityText: String,
    val riskColor: Color,
    val containerColor: Color,
    val borderColor: Color,
    val isUserHere: Boolean = false
) {
    SECTOR_1(
        id = "Sector 1",
        title = "North Concourse",
        riskLabel = "Safe",
        densityText = "1.1 p/m²",
        riskColor = SafetyThemeColors.SafeAccent,
        containerColor = Color(0xFFE8F5E9),
        borderColor = SafetyThemeColors.SafeBorder
    ),
    SECTOR_2(
        id = "Sector 2",
        title = "Central Plaza",
        riskLabel = "Caution",
        densityText = "3.4 p/m²",
        riskColor = SafetyThemeColors.CautionAccent,
        containerColor = Color(0xFFFFF3E0),
        borderColor = SafetyThemeColors.CautionBorder
    ),
    SECTOR_3(
        id = "Sector 3",
        title = "West Walkway",
        riskLabel = "Safe",
        densityText = "1.4 p/m²",
        riskColor = SafetyThemeColors.SafeAccent,
        containerColor = Color(0xFFE8F5E9),
        borderColor = SafetyThemeColors.SafeBorder
    ),
    SECTOR_4(
        id = "Sector 4",
        title = "East Concourse",
        riskLabel = "Moderate / High Risk",
        densityText = "6.2 p/m²",
        riskColor = SafetyThemeColors.HighRiskAccent,
        containerColor = Color(0xFFFFEBEE),
        borderColor = SafetyThemeColors.HighRiskBorder,
        isUserHere = true
    )
}

data class ExitLocation(
    val id: String,
    val name: String,
    val distance: String,
    val status: String,
    val isRecommended: Boolean = false
)

val defaultExits = listOf(
    ExitLocation("A", "Exit A (North)", "210 m", "Normal Flow", false),
    ExitLocation("B", "Exit B (East)", "120 m", "Low Congestion", true),
    ExitLocation("C", "Exit C (West)", "340 m", "Clear Path", false)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveCrowdMapScreen(
    onBack: (() -> Unit)? = null
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Floating Map Control Filter States (UI toggles)
    var showMyLocation by remember { mutableStateOf(true) }
    var showRiskOverlay by remember { mutableStateOf(true) }
    var showDensityOverlay by remember { mutableStateOf(true) }
    var showExitsOverlay by remember { mutableStateOf(true) }

    var selectedSector by remember { mutableStateOf<MapSector?>(MapSector.SECTOR_4) }
    var isRouteHighlighted by remember { mutableStateOf(false) }

    // Pulse animation for LIVE indicator & user marker
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Live Crowd Map",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Black,
                                color = SafetyThemeColors.TextPrimaryDark
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Home",
                                tint = SafetyThemeColors.TextPrimaryDark
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SafetyThemeColors.SurfaceWhite
                )
            )
        },
        containerColor = SafetyThemeColors.ScreenBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. TOP STATUS & LOCATION BAR
            MapTopStatusBar(pulseScale = pulseScale)

            // 2. FLOATING MAP CONTROLS ROW
            FloatingMapControlsRow(
                showMyLocation = showMyLocation,
                onToggleMyLocation = { showMyLocation = !showMyLocation },
                showRisk = showRiskOverlay,
                onToggleRisk = { showRiskOverlay = !showRiskOverlay },
                showDensity = showDensityOverlay,
                onToggleDensity = { showDensityOverlay = !showDensityOverlay },
                showExits = showExitsOverlay,
                onToggleExits = { showExitsOverlay = !showExitsOverlay }
            )

            // 3. MAIN MAP VISUALIZATION AREA
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                MapCanvasVisualizer(
                    showMyLocation = showMyLocation,
                    showRisk = showRiskOverlay,
                    showDensity = showDensityOverlay,
                    showExits = showExitsOverlay,
                    pulseScale = pulseScale,
                    pulseAlpha = pulseAlpha,
                    selectedSector = selectedSector,
                    onSelectSector = { sector -> selectedSector = sector },
                    isRouteHighlighted = isRouteHighlighted
                )
            }

            // 4. BOTTOM INFORMATION CARD: RECOMMENDED EXIT
            RecommendedExitBottomCard(
                onViewRouteClick = {
                    isRouteHighlighted = !isRouteHighlighted
                    scope.launch {
                        val msg = if (isRouteHighlighted) {
                            "Safe evacuation route to Exit B highlighted (120m)."
                        } else {
                            "Evacuation route preview cleared."
                        }
                        snackbarHostState.showSnackbar(msg)
                    }
                },
                isRouteHighlighted = isRouteHighlighted
            )
        }
    }
}

/**
 * Top Status Header with LIVE pulsing status and Current Sector/Location
 */
@Composable
private fun MapTopStatusBar(pulseScale: Float) {
    Surface(
        color = SafetyThemeColors.SurfaceWhite,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Live Status Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(SafetyThemeColors.SafeAccent)
                )
                Text(
                    text = "LIVE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SafetyThemeColors.SafePrimary
                )
                Text(
                    text = "• Updated just now",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = SafetyThemeColors.TextMuted
                )
            }

            // Current Sector Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF1F5F9))
                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = SafetyThemeColors.HighRiskAccent,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Sector 4 • East Concourse",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SafetyThemeColors.TextPrimaryDark
                )
            }
        }
    }
}

/**
 * Floating Map Controls (UI-only filter and focus toggles)
 */
@Composable
private fun FloatingMapControlsRow(
    showMyLocation: Boolean,
    onToggleMyLocation: () -> Unit,
    showRisk: Boolean,
    onToggleRisk: () -> Unit,
    showDensity: Boolean,
    onToggleDensity: () -> Unit,
    showExits: Boolean,
    onToggleExits: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            selected = showMyLocation,
            onClick = onToggleMyLocation,
            label = { Text("My Location", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFFE0F2FE),
                selectedLabelColor = Color(0xFF0369A1),
                selectedLeadingIconColor = Color(0xFF0284C7)
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = showMyLocation,
                borderColor = Color(0xFF7DD3FC)
            )
        )

        FilterChip(
            selected = showRisk,
            onClick = onToggleRisk,
            label = { Text("Risk", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = SafetyThemeColors.HighRiskContainer,
                selectedLabelColor = SafetyThemeColors.HighRiskText,
                selectedLeadingIconColor = SafetyThemeColors.HighRiskAccent
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = showRisk,
                borderColor = SafetyThemeColors.HighRiskBorder
            )
        )

        FilterChip(
            selected = showDensity,
            onClick = onToggleDensity,
            label = { Text("Density", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = SafetyThemeColors.CautionContainer,
                selectedLabelColor = SafetyThemeColors.CautionText,
                selectedLeadingIconColor = SafetyThemeColors.CautionAccent
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = showDensity,
                borderColor = SafetyThemeColors.CautionBorder
            )
        )

        FilterChip(
            selected = showExits,
            onClick = onToggleExits,
            label = { Text("Exits", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.DoorBack,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = SafetyThemeColors.SafeContainer,
                selectedLabelColor = SafetyThemeColors.SafeText,
                selectedLeadingIconColor = SafetyThemeColors.SafeAccent
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = showExits,
                borderColor = SafetyThemeColors.SafeBorder
            )
        )
    }
}

/**
 * Clean architectural Concourse Map Visualizer with sectors, exits, user location, and evacuation route.
 */
@Composable
private fun MapCanvasVisualizer(
    showMyLocation: Boolean,
    showRisk: Boolean,
    showDensity: Boolean,
    showExits: Boolean,
    pulseScale: Float,
    pulseAlpha: Float,
    selectedSector: MapSector?,
    onSelectSector: (MapSector) -> Unit,
    isRouteHighlighted: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .shadow(4.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFCFF)),
        border = BorderStroke(1.5.dp, Color(0xFFDDE3EA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Top Legend Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STADIUM CONCOURSE MAP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SafetyThemeColors.TextMuted,
                    letterSpacing = 1.sp
                )

                // Compact Legend
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = SafetyThemeColors.SafeAccent, label = "Safe")
                    LegendItem(color = SafetyThemeColors.CautionAccent, label = "Caution")
                    LegendItem(color = SafetyThemeColors.HighRiskAccent, label = "High Risk")
                }
            }

            // Interactive Map Grid
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF1F5F9))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
            ) {
                // Background Grid Canvas & Path Drawings
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArchitecturalGrid()

                    if (isRouteHighlighted && showExits) {
                        drawEvacuationPath()
                    }
                }

                // 2x2 Sector Interactive Zone Layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Top Row: Sector 1 & Sector 2
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SectorCard(
                            sector = MapSector.SECTOR_1,
                            showRisk = showRisk,
                            showDensity = showDensity,
                            showMyLocation = showMyLocation,
                            pulseScale = pulseScale,
                            pulseAlpha = pulseAlpha,
                            isSelected = selectedSector == MapSector.SECTOR_1,
                            onClick = { onSelectSector(MapSector.SECTOR_1) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                        )
                        SectorCard(
                            sector = MapSector.SECTOR_2,
                            showRisk = showRisk,
                            showDensity = showDensity,
                            showMyLocation = showMyLocation,
                            pulseScale = pulseScale,
                            pulseAlpha = pulseAlpha,
                            isSelected = selectedSector == MapSector.SECTOR_2,
                            onClick = { onSelectSector(MapSector.SECTOR_2) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                        )
                    }

                    // Bottom Row: Sector 3 & Sector 4
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SectorCard(
                            sector = MapSector.SECTOR_3,
                            showRisk = showRisk,
                            showDensity = showDensity,
                            showMyLocation = showMyLocation,
                            pulseScale = pulseScale,
                            pulseAlpha = pulseAlpha,
                            isSelected = selectedSector == MapSector.SECTOR_3,
                            onClick = { onSelectSector(MapSector.SECTOR_3) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                        )
                        SectorCard(
                            sector = MapSector.SECTOR_4,
                            showRisk = showRisk,
                            showDensity = showDensity,
                            showMyLocation = showMyLocation,
                            pulseScale = pulseScale,
                            pulseAlpha = pulseAlpha,
                            isSelected = selectedSector == MapSector.SECTOR_4,
                            onClick = { onSelectSector(MapSector.SECTOR_4) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                        )
                    }
                }

                // Exit Gate Badges (Floating on map edges)
                if (showExits) {
                    // Exit A (North - Top Center)
                    ExitMarkerBadge(
                        label = "🚪 Exit A",
                        isRecommended = false,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 2.dp)
                    )

                    // Exit B (East - Middle Right)
                    ExitMarkerBadge(
                        label = "🚪 Exit B (Recommended)",
                        isRecommended = true,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 2.dp)
                    )

                    // Exit C (West - Middle Left)
                    ExitMarkerBadge(
                        label = "🚪 Exit C",
                        isRecommended = false,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Sector Card representation on the Map
 */
@Composable
private fun SectorCard(
    sector: MapSector,
    showRisk: Boolean,
    showDensity: Boolean,
    showMyLocation: Boolean,
    pulseScale: Float,
    pulseAlpha: Float,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val effectiveContainerColor = if (showRisk) sector.containerColor else Color.White
    val effectiveBorderColor = if (isSelected) {
        sector.riskColor
    } else if (showRisk) {
        sector.borderColor
    } else {
        Color(0xFFCBD5E1)
    }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = effectiveContainerColor),
        border = BorderStroke(if (isSelected) 2.5.dp else 1.dp, effectiveBorderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Sector Header & Risk Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = sector.id,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = SafetyThemeColors.TextPrimaryDark
                        )
                        Text(
                            text = sector.title,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = SafetyThemeColors.TextSecondaryDark
                        )
                    }

                    if (showRisk) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(sector.riskColor)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = sector.riskLabel,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Sector Density & User Location Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    if (showDensity) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = SafetyThemeColors.TextSecondaryDark,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = sector.densityText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SafetyThemeColors.TextPrimaryDark
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    // User Location Radar Marker in Sector 4
                    if (sector.isUserHere && showMyLocation) {
                        UserLocationPin(pulseScale = pulseScale, pulseAlpha = pulseAlpha)
                    }
                }
            }
        }
    }
}

/**
 * Pulsing User Location Beacon
 */
@Composable
private fun UserLocationPin(pulseScale: Float, pulseAlpha: Float) {
    Box(
        modifier = Modifier.size(36.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing ring
        Box(
            modifier = Modifier
                .size((32 * pulseScale).dp)
                .clip(CircleShape)
                .background(Color(0xFF0284C7).copy(alpha = pulseAlpha))
        )
        // Center Pin with You badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0284C7))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
                Text(
                    text = "YOU",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Exit Marker Badge for the Map
 */
@Composable
private fun ExitMarkerBadge(
    label: String,
    isRecommended: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isRecommended) SafetyThemeColors.SafePrimary else Color(0xFF1E293B)
    Box(
        modifier = modifier
            .shadow(3.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(
                1.dp,
                if (isRecommended) SafetyThemeColors.SafeBorder else Color(0xFF475569),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/**
 * Compact Legend Dot + Label
 */
@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = SafetyThemeColors.TextSecondaryDark
        )
    }
}

/**
 * Bottom Information Card: RECOMMENDED EXIT with distance and View Route button
 */
@Composable
private fun RecommendedExitBottomCard(
    onViewRouteClick: () -> Unit,
    isRouteHighlighted: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SafetyThemeColors.SurfaceWhite),
        border = BorderStroke(1.5.dp, SafetyThemeColors.SafeBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SafetyThemeColors.SafeContainer)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SafetyThemeColors.SafePrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "RECOMMENDED EXIT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = SafetyThemeColors.SafeText
                        )
                    }
                }

                Text(
                    text = "AI Path Active",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SafetyThemeColors.TextMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Info & Action Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Exit Details
                Column {
                    Text(
                        text = "Exit B",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = SafetyThemeColors.TextPrimaryDark
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "120 m",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SafetyThemeColors.SafePrimary
                        )
                        Text(
                            text = "•",
                            fontSize = 14.sp,
                            color = SafetyThemeColors.TextMuted
                        )
                        Text(
                            text = "Low congestion",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = SafetyThemeColors.TextSecondaryDark
                        )
                    }
                }

                // View Route Action Button
                Button(
                    onClick = onViewRouteClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRouteHighlighted) Color(0xFF0F172A) else SafetyThemeColors.SafePrimary
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isRouteHighlighted) "CLEAR ROUTE" else "VIEW ROUTE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Draws architectural background blueprint grid lines
 */
private fun DrawScope.drawArchitecturalGrid() {
    val step = 30.dp.toPx()
    val gridColor = Color(0xFFE2E8F0).copy(alpha = 0.6f)

    var x = 0f
    while (x < size.width) {
        drawLine(
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1f
        )
        x += step
    }

    var y = 0f
    while (y < size.height) {
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1f
        )
        y += step
    }
}

/**
 * Draws safe evacuation trajectory path from Sector 4 to Exit B
 */
private fun DrawScope.drawEvacuationPath() {
    val path = Path().apply {
        // Start near user location (Sector 4: bottom right quadrant)
        val startX = size.width * 0.75f
        val startY = size.height * 0.75f
        // Exit B is at middle right
        val targetX = size.width * 0.96f
        val targetY = size.height * 0.50f

        moveTo(startX, startY)
        cubicTo(
            startX + 20f, startY - 40f,
            targetX - 30f, targetY + 30f,
            targetX, targetY
        )
    }

    drawPath(
        path = path,
        color = Color(0xFF1B5E20),
        style = Stroke(
            width = 5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 12f), 0f)
        )
    )
}
