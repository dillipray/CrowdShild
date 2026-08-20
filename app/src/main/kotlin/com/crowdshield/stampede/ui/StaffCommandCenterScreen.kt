package com.crowdshield.stampede.ui

import com.crowdshield.stampede.ui.components.CurrentLocationMapCard
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// ════════════════════════════════════════════════════════════════════════════════
// LIGHT UI DESIGN SYSTEM & COLOR TOKENS (Professional Command Center Theme)
// ════════════════════════════════════════════════════════════════════════════════
private val LightBgPage         = Color(0xFFF8FAFC) // Off-white neutral background
private val LightBgCard         = Color(0xFFFFFFFF) // Crisp white card surface
private val LightBgSubtle       = Color(0xFFF1F5F9) // Subtle secondary container
private val LightBorderSubtle   = Color(0xFFE2E8F0) // Subtle gray border
private val LightBorderStrong   = Color(0xFFCBD5E1) // Medium outline border

private val LightTextPrimary    = Color(0xFF0F172A) // Dark charcoal / navy for headings & main values
private val LightTextSecondary  = Color(0xFF475569) // Slate gray for subheadings & captions
private val LightTextMuted      = Color(0xFF94A3B8) // Light slate for timestamps & tertiary info

// Risk Color Tokens (Meaningful Semantics)
private val RiskSafeGreen       = Color(0xFF10B981) // Safe Green
private val RiskSafeBg          = Color(0xFFECFDF5) // Very light emerald tint
private val RiskSafeBorder      = Color(0xFFA7F3D0) // Emerald border
private val RiskSafeText        = Color(0xFF047857) // Dark emerald text

private val RiskCautionOrange   = Color(0xFFF59E0B) // Caution Orange
private val RiskCautionBg       = Color(0xFFFFFBEB) // Very light amber tint
private val RiskCautionBorder   = Color(0xFFFDE68A) // Amber border
private val RiskCautionText     = Color(0xFFB45309) // Dark amber text

private val RiskHighRed         = Color(0xFFEF4444) // High Risk Red
private val RiskHighBg          = Color(0xFFFEF2F2) // Very light red tint
private val RiskHighBorder      = Color(0xFFFECACA) // Red border
private val RiskHighText        = Color(0xFFB91C1C) // Dark red text

// AI & Command Accents
private val AiBlue              = Color(0xFF2563EB) // Royal AI Blue
private val AiBlueBg            = Color(0xFFEFF6FF) // Very light blue tint
private val AiBlueBorder        = Color(0xFFBFDBFE) // Light blue border
private val AiPurple            = Color(0xFF7C3AED) // AI Prediction Purple
private val AiPurpleBg          = Color(0xFFF5F3FF) // Very light purple tint
private val GoldShield          = Color(0xFFD97706) // Command Gold Accent
private val GoldShieldBg        = Color(0xFFFEF3C7) // Command Gold Tint

// ════════════════════════════════════════════════════════════════════════════════
// ENUMS & DATA MODELS
// ════════════════════════════════════════════════════════════════════════════════
enum class StaffTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    ANALYSIS("Analysis", Icons.Default.Psychology),
    CCTV("CCTV", Icons.Default.Videocam),
    INCIDENTS("Incidents", Icons.Default.Emergency)
}

enum class StaffRiskLevel(
    val label: String,
    val color: Color,
    val bgColor: Color,
    val borderColor: Color,
    val textColor: Color
) {
    SAFE("SAFE", RiskSafeGreen, RiskSafeBg, RiskSafeBorder, RiskSafeText),
    CAUTION("CAUTION", RiskCautionOrange, RiskCautionBg, RiskCautionBorder, RiskCautionText),
    HIGH_RISK("HIGH RISK", RiskHighRed, RiskHighBg, RiskHighBorder, RiskHighText)
}

data class StaffSectorData(
    val id: Int,
    val name: String,
    val subLocation: String,
    val riskScore: Double,
    val densityPercent: Int,
    val trend: String,
    val trendDirection: Int, // 1 = up, 0 = flat, -1 = down
    val riskLevel: StaffRiskLevel,
    val sensorCount: Int = 12,
    val flowRate: String = "1.2 m/s",
    val requiresAttention: Boolean = false,
    val statusDetail: String = "Normal Flow"
)

data class StaffIncidentItem(
    val id: String,
    val sector: String,
    val title: String,
    val timeAgo: String,
    val riskLevel: StaffRiskLevel,
    val officerAssigned: String? = null
)

data class StaffExitItem(
    val id: String,
    val name: String,
    val location: String,
    val status: String,
    val statusColor: Color,
    val congestionPercent: Int,
    val isRecommended: Boolean = false
)

// ════════════════════════════════════════════════════════════════════════════════
// MAIN STAFF COMMAND CENTER SCREEN
// ════════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffCommandCenterScreen(
    onLogout: () -> Unit
) {
    var currentTab by remember { mutableStateOf(StaffTab.DASHBOARD) }

    // Dialog & Detail states
    var selectedSectorDetail by remember { mutableStateOf<StaffSectorData?>(null) }
    var showRouteDialog by remember { mutableStateOf(false) }
    var showAllIncidentsDialog by remember { mutableStateOf(false) }
    var showStaffMapModal by remember { mutableStateOf(false) }
    var showActionModalType by remember { mutableStateOf<String?>(null) } // "CCTV", "INCIDENTS"
    var showLogoutConfirm by remember { mutableStateOf(false) }

    // Realistic Mock Sectors Data
    val sectors = remember {
        listOf(
            StaffSectorData(
                id = 1,
                name = "Sector 1",
                subLocation = "North Gate Entry",
                riskScore = 2.1,
                densityPercent = 24,
                trend = "Stable 0%",
                trendDirection = 0,
                riskLevel = StaffRiskLevel.SAFE,
                requiresAttention = false,
                statusDetail = "Nominal entry throughput"
            ),
            StaffSectorData(
                id = 2,
                name = "Sector 2",
                subLocation = "West Plaza & Food Court",
                riskScore = 2.8,
                densityPercent = 31,
                trend = "↓ -3% in 5m",
                trendDirection = -1,
                riskLevel = StaffRiskLevel.SAFE,
                requiresAttention = false,
                statusDetail = "Steady dispersing crowd"
            ),
            StaffSectorData(
                id = 3,
                name = "Sector 3",
                subLocation = "Main Arena Stage",
                riskScore = 5.4,
                densityPercent = 68,
                trend = "↑ +8% in 5m",
                trendDirection = 1,
                riskLevel = StaffRiskLevel.CAUTION,
                requiresAttention = true,
                statusDetail = "Corridor inflow increasing"
            ),
            StaffSectorData(
                id = 4,
                name = "Sector 4",
                subLocation = "East Concourse Exit Corridor",
                riskScore = 6.8,
                densityPercent = 89,
                trend = "↑ +18% in 5m",
                trendDirection = 1,
                riskLevel = StaffRiskLevel.HIGH_RISK,
                requiresAttention = true,
                statusDetail = "Major constriction point"
            )
        )
    }

    // Mock Incidents Data
    val activeIncidents = remember {
        listOf(
            StaffIncidentItem(
                id = "INC-409",
                sector = "Sector 4",
                title = "Density rapidly increasing",
                timeAgo = "2m ago",
                riskLevel = StaffRiskLevel.HIGH_RISK,
                officerAssigned = "Unit Alpha-3 Dispatched"
            ),
            StaffIncidentItem(
                id = "INC-402",
                sector = "Exit B",
                title = "Congestion detected",
                timeAgo = "5m ago",
                riskLevel = StaffRiskLevel.CAUTION,
                officerAssigned = "Turnstile #4 Cleared"
            )
        )
    }

    // Mock Exits Data
    val exits = remember {
        listOf(
            StaffExitItem(
                id = "EX-A",
                name = "Exit A",
                location = "North Perimeter",
                status = "Open",
                statusColor = RiskSafeGreen,
                congestionPercent = 22
            ),
            StaffExitItem(
                id = "EX-B",
                name = "Exit B",
                location = "West Concourse",
                status = "Low Congestion",
                statusColor = RiskSafeGreen,
                congestionPercent = 18,
                isRecommended = true
            ),
            StaffExitItem(
                id = "EX-C",
                name = "Exit C",
                location = "East Boulevard",
                status = "Busy",
                statusColor = RiskCautionOrange,
                congestionPercent = 74
            )
        )
    }

    Scaffold(
        topBar = {
            StaffDashboardHeader(
                onLogoutClick = { showLogoutConfirm = true }
            )
        },
        bottomBar = {
            StaffBottomNavigationBar(
                selectedTab = currentTab,
                onTabSelected = { tab ->
                    currentTab = tab
                    when (tab) {
                        StaffTab.DASHBOARD -> { /* Default Dashboard */ }
                        StaffTab.ANALYSIS -> { /* Analysis Tab rendered directly */ }
                        StaffTab.CCTV -> showActionModalType = "CCTV"
                        StaffTab.INCIDENTS -> showActionModalType = "INCIDENTS"
                    }
                }
            )
        },
        containerColor = LightBgPage
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentTab) {
                StaffTab.ANALYSIS -> {
                    // Dedicated Staff Image Analysis Screen (Tab 2)
                    StaffAnalysisScreen()
                }
                else -> {
                    // Default Staff Dashboard View (Tab 1)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. CURRENT LOCATION FIELD MAP VIEW (Mappls-Ready Map Architecture)
                        CurrentLocationMapCard(
                            onOpenFullMap = { showStaffMapModal = true }
                        )

                        // 2. EARLY PREDICTION PIPELINE
                        InnovationWorkflowBanner()

                        // 3. VENUE OVERVIEW
                        VenueOverviewSection()

                        // 3. IN-DASHBOARD LIVE VENUE SAFETY MAP
                        DashboardVenueMapCard(
                            sectors = sectors,
                            exits = exits,
                            onExpandMap = { showStaffMapModal = true },
                            onSelectSector = { sector ->
                                selectedSectorDetail = sector
                            }
                        )

                        // 4. AI PREDICTIVE ALERT — MAIN FOCUS
                        AiPredictiveAlertCard(
                            onViewSectorClick = {
                                selectedSectorDetail = sectors.firstOrNull { it.id == 4 }
                            }
                        )

                        // 5. SECTOR OVERVIEW
                        SectorOverviewSection(
                            sectors = sectors,
                            onViewSector = { sector ->
                                selectedSectorDetail = sector
                            }
                        )

                        // 6. RISK TREND — LAST 15 MINUTES
                        RiskTrendVisualizationCard()

                        // 7. AI RECOMMENDATION
                        AiRecommendationCard(
                            onViewRouteClick = { showRouteDialog = true }
                        )

                        // 8. ACTIVE INCIDENTS
                        ActiveIncidentsSection(
                            incidents = activeIncidents,
                            onViewAllClick = { showAllIncidentsDialog = true }
                        )

                        // 9. QUICK ACTIONS
                        QuickActionsSection(
                            onActionClick = { actionType ->
                                when (actionType) {
                                    "MAP" -> showStaffMapModal = true
                                    "ANALYSIS" -> currentTab = StaffTab.ANALYSIS
                                    else -> showActionModalType = actionType
                                }
                            }
                        )

                        // Footer Notice
                        OfficerAuthorityFooter()

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MODALS & PURPOSE-BUILT MAP OVERLAYS
    // ─────────────────────────────────────────────────────────────────────────

    // Staff Command Map (Interactive Crowd Safety Map)
    if (showStaffMapModal) {
        StaffCommandMapModal(
            sectors = sectors,
            exits = exits,
            incidents = activeIncidents,
            onDismiss = { showStaffMapModal = false },
            onSelectSector = { sector ->
                selectedSectorDetail = sector
            }
        )
    }

    // Sector Detail Dialog (Deep Telemetry with Sector Footprint Map)
    selectedSectorDetail?.let { sector ->
        SectorDetailDialog(
            sector = sector,
            onDismiss = { selectedSectorDetail = null }
        )
    }

    // Tactical Route Diversion Dialog (Tactical Route Map Canvas)
    if (showRouteDialog) {
        AiRouteRecommendationDialog(
            onDismiss = { showRouteDialog = false }
        )
    }

    // All Incidents Dialog (Spatial Incident Map)
    if (showAllIncidentsDialog) {
        AllIncidentsDialog(
            incidents = activeIncidents,
            onDismiss = { showAllIncidentsDialog = false }
        )
    }

    // Quick Action Modals (CCTV Spatial Coverage / Incidents)
    showActionModalType?.let { modalType ->
        QuickActionModal(
            actionType = modalType,
            sectors = sectors,
            incidents = activeIncidents,
            onDismiss = {
                showActionModalType = null
                currentTab = StaffTab.DASHBOARD
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirm) {
        LogoutConfirmationDialog(
            onConfirm = {
                showLogoutConfirm = false
                onLogout()
            },
            onDismiss = { showLogoutConfirm = false }
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// STAFF ANALYSIS SCREEN (Frontend UI for Image Upload & Preview)
// ════════════════════════════════════════════════════════════════════════════════
@Composable
fun StaffAnalysisScreen() {
    var hasSelectedImage by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Section
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Image Analysis",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = LightTextPrimary,
                letterSpacing = (-0.2).sp
            )
            Text(
                text = "Upload a crowd image to assess density and potential risk.",
                fontSize = 13.sp,
                color = LightTextSecondary,
                lineHeight = 18.sp
            )
        }

        // Image Upload & Preview Box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { hasSelectedImage = !hasSelectedImage },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = LightBgCard),
            border = BorderStroke(
                width = 1.5.dp,
                color = if (hasSelectedImage) AiBlueBorder else LightBorderStrong
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            if (!hasSelectedImage) {
                // ── EMPTY STATE ───────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(AiBlueBg)
                            .border(1.dp, AiBlueBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Add Crowd Image/Video",
                            tint = AiBlue,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Text(
                        text = "Add Crowd Image/Video",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightTextPrimary
                    )

                    Text(
                        text = "Upload an image of the crowd area for analysis",
                        fontSize = 12.5.sp,
                        color = LightTextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { hasSelectedImage = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AiBlue,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "ADD IMAGE/VIDEO",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            } else {
                // ── PREVIEW STATE (After Image Selection) ─────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = AiBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Crowd Image",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightTextPrimary
                            )
                        }

                        Text(
                            text = "Image Preview",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AiBlue
                        )
                    }

                    // Visual Crowd Image Preview Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(LightBgSubtle)
                            .border(1.dp, LightBorderSubtle, RoundedCornerShape(12.dp))
                    ) {
                        CrowdImageMockPreviewCanvas()

                        // Image Tag Overlay
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp),
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.95f),
                            border = BorderStroke(1.dp, LightBorderSubtle),
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(RiskCautionOrange)
                                )
                                Text(
                                    text = "Concourse_Sector4_East.jpg · 2.4 MB",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = LightTextPrimary
                                )
                            }
                        }
                    }

                    // Change Image Action
                    OutlinedButton(
                        onClick = { hasSelectedImage = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, LightBorderStrong),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LightTextPrimary)
                    ) {
                        Text(
                            text = "CHANGE IMAGE",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Analysis Action Button
        Button(
            onClick = { /* Frontend UI only: No AI execution */ },
            enabled = hasSelectedImage,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AiBlue,
                contentColor = Color.White,
                disabledContainerColor = LightBgSubtle,
                disabledContentColor = LightTextMuted
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = if (hasSelectedImage) 2.dp else 0.dp
            )
        ) {
            Text(
                text = "ANALYSIS",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun CrowdImageMockPreviewCanvas() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Simulated concourse ground
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1))
            )
        )

        // Architectural concourse lines
        drawLine(Color(0xFF94A3B8), Offset(w * 0.15f, 0f), Offset(w * 0.35f, h), strokeWidth = 1.2f)
        drawLine(Color(0xFF94A3B8), Offset(w * 0.85f, 0f), Offset(w * 0.65f, h), strokeWidth = 1.2f)

        // Cluster of crowd dots simulating overhead aerial view
        val randomOffsets = listOf(
            Offset(w * 0.45f, h * 0.35f),
            Offset(w * 0.48f, h * 0.38f),
            Offset(w * 0.52f, h * 0.32f),
            Offset(w * 0.42f, h * 0.48f),
            Offset(w * 0.50f, h * 0.50f),
            Offset(w * 0.55f, h * 0.46f),
            Offset(w * 0.58f, h * 0.52f),
            Offset(w * 0.46f, h * 0.60f),
            Offset(w * 0.53f, h * 0.62f),
            Offset(w * 0.60f, h * 0.58f),
            Offset(w * 0.40f, h * 0.65f),
            Offset(w * 0.48f, h * 0.70f),
            Offset(w * 0.55f, h * 0.72f),
            Offset(w * 0.62f, h * 0.68f)
        )

        for (pt in randomOffsets) {
            drawCircle(color = Color(0xFF1E293B).copy(alpha = 0.75f), radius = 4.5.dp.toPx(), center = pt)
            drawCircle(color = Color(0xFF64748B), radius = 2.dp.toPx(), center = pt)
        }

        // Sector 4 Focus boundary overlay
        drawRoundRect(
            color = RiskCautionOrange.copy(alpha = 0.5f),
            topLeft = Offset(w * 0.35f, h * 0.25f),
            size = Size(w * 0.35f, h * 0.55f),
            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
            style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// HEADER COMPONENT (Clean Professional Command Bar)
// ════════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StaffDashboardHeader(
    onLogoutClick: () -> Unit
) {
    Surface(
        color = LightBgCard,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, LightBorderSubtle)
    ) {
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Shield Emblem
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(GoldShieldBg)
                            .border(1.dp, GoldShield.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = GoldShield,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "CrowdShield Command",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = LightTextPrimary,
                                letterSpacing = (-0.2).sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(GoldShieldBg)
                                    .border(0.8.dp, GoldShield.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "STAFF",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GoldShield
                                )
                            }
                        }

                        Text(
                            text = "Central Control Room · Zone Alpha",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = LightTextSecondary
                        )
                    }
                }
            },
            actions = {
                // Live Operational Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(RiskSafeBg)
                        .border(1.dp, RiskSafeBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PulsingDot(color = RiskSafeGreen, size = 7)
                        Text(
                            text = "System Operational",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RiskSafeText
                        )
                    }
                }

                IconButton(onClick = onLogoutClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Logout",
                        tint = LightTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = LightBgCard
            )
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// INNOVATION PIPELINE BANNER (MONITOR → DETECT TREND → PREDICT RISK → RECOMMEND → RESPOND)
// ════════════════════════════════════════════════════════════════════════════════
@Composable
private fun InnovationWorkflowBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LightBgCard),
        border = BorderStroke(1.dp, LightBorderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = AiBlue,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "EARLY PREDICTION PIPELINE",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black,
                        color = AiBlue,
                        letterSpacing = 0.8.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(RiskCautionBg)
                        .border(0.8.dp, RiskCautionBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "PREDICTION ACTIVE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = RiskCautionText
                    )
                }
            }

            // 5 Stages workflow flow
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WorkflowStepBadge(step = "1. MONITOR", isCompleted = true)
                WorkflowArrow()
                WorkflowStepBadge(step = "2. DETECT TREND", isCompleted = true)
                WorkflowArrow()
                WorkflowStepBadge(step = "3. PREDICT RISK", isHighlight = true)
                WorkflowArrow()
                WorkflowStepBadge(step = "4. RECOMMEND", isActive = false)
                WorkflowArrow()
                WorkflowStepBadge(step = "5. RESPOND", isActive = false)
            }
        }
    }
}

@Composable
private fun WorkflowStepBadge(
    step: String,
    isActive: Boolean = false,
    isCompleted: Boolean = false,
    isHighlight: Boolean = false
) {
    val bg = when {
        isHighlight -> RiskCautionBg
        isCompleted -> RiskSafeBg
        isActive -> AiBlueBg
        else -> LightBgSubtle
    }
    val fg = when {
        isHighlight -> RiskCautionText
        isCompleted -> RiskSafeText
        isActive -> AiBlue
        else -> LightTextMuted
    }
    val border = when {
        isHighlight -> RiskCautionBorder
        isCompleted -> RiskSafeBorder
        else -> LightBorderSubtle
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(6.dp))
            .padding(horizontal = 5.dp, vertical = 3.dp)
    ) {
        Text(
            text = step,
            fontSize = 8.5.sp,
            fontWeight = if (isHighlight || isCompleted) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = fg
        )
    }
}

@Composable
private fun WorkflowArrow() {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
        contentDescription = null,
        tint = LightTextMuted.copy(alpha = 0.8f),
        modifier = Modifier.size(9.dp)
    )
}

// ════════════════════════════════════════════════════════════════════════════════
// 2. VENUE OVERVIEW SECTION (Compact White Cards)
// ════════════════════════════════════════════════════════════════════════════════
@Composable
private fun VenueOverviewSection() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "VENUE OVERVIEW",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = LightTextSecondary,
                letterSpacing = 1.1.sp
            )
            Text(
                text = "Live Telemetry · 10s refresh",
                fontSize = 10.5.sp,
                color = LightTextMuted
            )
        }

        // 2x2 Grid of White Cards with Subtle Borders & Soft Elevation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            VenueMetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.People,
                iconColor = AiBlue,
                value = "8,420",
                label = "People",
                subLabel = "Venue Capacity 70%"
            )
            VenueMetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.LocationOn,
                iconColor = RiskSafeGreen,
                value = "4",
                label = "Sectors",
                subLabel = "100% Monitored"
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            VenueMetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.DoorFront,
                iconColor = AiBlue,
                value = "6/8",
                label = "Exits Available",
                subLabel = "2 Inactive / Restricted"
            )
            VenueMetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.WarningAmber,
                iconColor = RiskCautionOrange,
                value = "2",
                label = "Need Attention",
                subLabel = "Sector 3 & Sector 4",
                isAlert = true
            )
        }
    }
}

@Composable
private fun VenueMetricCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color,
    value: String,
    label: String,
    subLabel: String,
    isAlert: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAlert) RiskCautionBg else LightBgCard
        ),
        border = BorderStroke(
            1.dp,
            if (isAlert) RiskCautionBorder else LightBorderSubtle
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(13.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isAlert) RiskCautionBorder.copy(alpha = 0.5f) else LightBgSubtle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isAlert) RiskCautionText else iconColor,
                        modifier = Modifier.size(17.dp)
                    )
                }

                if (isAlert) {
                    PulsingDot(color = RiskCautionOrange, size = 6)
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = if (isAlert) RiskCautionText else LightTextPrimary,
                letterSpacing = (-0.3).sp
            )

            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isAlert) RiskCautionText else LightTextPrimary
            )

            Text(
                text = subLabel,
                fontSize = 10.5.sp,
                color = if (isAlert) RiskCautionText.copy(alpha = 0.8f) else LightTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// 3. IN-DASHBOARD LIVE VENUE SAFETY MAP CARD (Direct Spatial Map UI)
// ════════════════════════════════════════════════════════════════════════════════
@Composable
private fun DashboardVenueMapCard(
    sectors: List<StaffSectorData>,
    exits: List<StaffExitItem>,
    onExpandMap: () -> Unit,
    onSelectSector: (StaffSectorData) -> Unit
) {
    var selectedId by remember { mutableStateOf(4) }
    var showRisk by remember { mutableStateOf(true) }
    var showDensity by remember { mutableStateOf(true) }
    var showExits by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightBgCard),
        border = BorderStroke(1.dp, LightBorderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        tint = AiBlue,
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = "LIVE VENUE SAFETY MAP",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = LightTextPrimary,
                        letterSpacing = 0.8.sp
                    )
                }

                OutlinedButton(
                    onClick = onExpandMap,
                    modifier = Modifier.height(28.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, AiBlueBorder)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = null,
                            tint = AiBlue,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "COMMAND MAP",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AiBlue
                        )
                    }
                }
            }

            // Interactive Mini Map Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(LightBgSubtle)
                    .border(1.dp, LightBorderSubtle, RoundedCornerShape(12.dp))
            ) {
                VenueMapCanvas(
                    sectors = sectors,
                    selectedSectorId = selectedId,
                    showRiskLayer = showRisk,
                    showDensityLayer = showDensity,
                    showExitLayer = showExits,
                    showIncidentLayer = true,
                    onSectorClicked = { id ->
                        selectedId = id
                        sectors.firstOrNull { it.id == id }?.let { onSelectSector(it) }
                    }
                )

                // Mini Legend (Floating Top-Left)
                MapFloatingLegend(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                )

                // Quick Sector Status Pill (Floating Bottom-Left)
                val selSector = sectors.firstOrNull { it.id == selectedId } ?: sectors[3]
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White.copy(alpha = 0.95f),
                    border = BorderStroke(1.dp, selSector.riskLevel.borderColor),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(selSector.riskLevel.color)
                        )
                        Text(
                            text = "${selSector.name} · ${selSector.riskLevel.label} (${selSector.densityPercent}%)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = selSector.riskLevel.textColor
                        )
                    }
                }
            }

            // Quick Map Layer Toggle Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MapQuickChip(
                        label = "Risk",
                        isActive = showRisk,
                        activeColor = RiskCautionText,
                        activeBg = RiskCautionBg,
                        onClick = { showRisk = !showRisk }
                    )
                    MapQuickChip(
                        label = "Density",
                        isActive = showDensity,
                        activeColor = AiBlue,
                        activeBg = AiBlueBg,
                        onClick = { showDensity = !showDensity }
                    )
                    MapQuickChip(
                        label = "Exits",
                        isActive = showExits,
                        activeColor = RiskSafeText,
                        activeBg = RiskSafeBg,
                        onClick = { showExits = !showExits }
                    )
                }

                Text(
                    text = "Tap sector to inspect",
                    fontSize = 10.sp,
                    color = LightTextMuted
                )
            }
        }
    }
}

@Composable
private fun MapQuickChip(
    label: String,
    isActive: Boolean,
    activeColor: Color,
    activeBg: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isActive) activeBg else LightBgSubtle)
            .border(
                1.dp,
                if (isActive) activeColor.copy(alpha = 0.4f) else LightBorderSubtle,
                RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = if (isActive) activeColor else LightTextSecondary
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// 4. AI PREDICTIVE ALERT — MAIN FOCUS (Highest Prominence)
// ════════════════════════════════════════════════════════════════════════════════
@Composable
private fun AiPredictiveAlertCard(
    onViewSectorClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alertBorderAlpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp), spotColor = RiskCautionOrange.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightBgCard),
        border = BorderStroke(1.8.dp, RiskCautionOrange.copy(alpha = borderAlpha))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Bar: Tag & Location
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(RiskCautionOrange)
                    )
                    Text(
                        text = "AI PREDICTIVE ALERT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = RiskCautionText,
                        letterSpacing = 1.2.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(RiskHighBg)
                        .border(1.dp, RiskHighBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "PRIORITY 1",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = RiskHighText
                    )
                }
            }

            // Sector Identifier
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Sector 4 — East Concourse",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = LightTextPrimary
                )
                Text(
                    text = "Major bottleneck corridor connecting Arena Zone to North Concourse",
                    fontSize = 11.5.sp,
                    color = LightTextSecondary,
                    lineHeight = 16.sp
                )
            }

            HorizontalDivider(color = LightBorderSubtle, thickness = 1.dp)

            // Dual Column Comparison: CURRENT CONDITION vs PREDICTED RISK
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // CURRENT CONDITION BOX
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(RiskCautionBg)
                        .border(1.dp, RiskCautionBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "CURRENT CONDITION",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = RiskCautionText,
                            letterSpacing = 0.6.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "6.8",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = RiskCautionText
                            )
                            Text(
                                text = "/ 10",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightTextMuted
                            )
                        }

                        // Caution Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White)
                                .border(1.dp, RiskCautionBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "🟠 CAUTION",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = RiskCautionText
                            )
                        }

                        // Density Trend
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = RiskHighRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "↑ 18% in last 5 min",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = RiskHighText
                            )
                        }
                    }
                }

                // PREDICTED RISK BOX (Early Prediction Focus)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(RiskHighBg)
                        .border(1.2.dp, RiskHighBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = RiskHighRed,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "PREDICTED RISK",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = RiskHighText,
                                letterSpacing = 0.6.sp
                            )
                        }

                        Text(
                            text = "\"Elevated crowd pressure predicted if current movement continues.\"",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RiskHighText,
                            lineHeight = 15.sp
                        )

                        Text(
                            text = "Projected: 8.2 (HIGH RISK)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = RiskHighText
                        )
                    }
                }
            }

            // Action Button
            Button(
                onClick = onViewSectorClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RiskCautionOrange,
                    contentColor = Color.White
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "VIEW SECTOR 4 TELEMETRY",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.4.sp
                    )
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// 5. SECTOR OVERVIEW SECTION (All 4 Sectors)
// ════════════════════════════════════════════════════════════════════════════════
@Composable
private fun SectorOverviewSection(
    sectors: List<StaffSectorData>,
    onViewSector: (StaffSectorData) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SECTOR OVERVIEW",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = LightTextSecondary,
                letterSpacing = 1.1.sp
            )
            Text(
                text = "4 ACTIVE ZONES",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = LightTextMuted
            )
        }

        sectors.forEach { sector ->
            SectorCard(
                sector = sector,
                onViewClick = { onViewSector(sector) }
            )
        }
    }
}

@Composable
private fun SectorCard(
    sector: StaffSectorData,
    onViewClick: () -> Unit
) {
    val isSector4 = sector.id == 4

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSector4) RiskHighBg.copy(alpha = 0.5f) else LightBgCard
        ),
        border = BorderStroke(
            width = if (isSector4) 1.5.dp else 1.dp,
            color = if (isSector4) RiskHighBorder else LightBorderSubtle
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSector4) 2.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(sector.riskLevel.color)
                    )
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${sector.name} — ${sector.riskLevel.label}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightTextPrimary
                            )
                            if (isSector4) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(RiskHighBg)
                                        .border(0.8.dp, RiskHighBorder, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "ATTENTION",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = RiskHighText
                                    )
                                }
                            }
                        }
                        Text(
                            text = sector.subLocation,
                            fontSize = 11.sp,
                            color = LightTextSecondary
                        )
                    }
                }

                // Risk Score Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(sector.riskLevel.bgColor)
                        .border(1.dp, sector.riskLevel.borderColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${sector.riskScore} / 10",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Black,
                        color = sector.riskLevel.textColor
                    )
                }
            }

            // Density progress bar & trend row
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Density: ${sector.densityPercent}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightTextPrimary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        val trendIcon = when (sector.trendDirection) {
                            1 -> Icons.AutoMirrored.Filled.TrendingUp
                            -1 -> Icons.AutoMirrored.Filled.TrendingDown
                            else -> Icons.AutoMirrored.Filled.TrendingFlat
                        }
                        val trendColor = when (sector.trendDirection) {
                            1 -> if (sector.riskScore > 5) RiskHighText else RiskCautionText
                            -1 -> RiskSafeText
                            else -> LightTextSecondary
                        }
                        Icon(
                            imageVector = trendIcon,
                            contentDescription = null,
                            tint = trendColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = sector.trend,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = trendColor
                        )
                    }
                }

                // Custom density bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(LightBgSubtle)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(sector.densityPercent / 100f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(sector.riskLevel.color)
                    )
                }
            }

            // View Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onViewClick,
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    border = BorderStroke(1.dp, LightBorderStrong),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AiBlue)
                ) {
                    Text(
                        text = "View Sector Details",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// 6. RISK TREND — LAST 15 MINUTES (Stable → Increasing → Rapid Increase)
// ════════════════════════════════════════════════════════════════════════════════
@Composable
private fun RiskTrendVisualizationCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightBgCard),
        border = BorderStroke(1.dp, LightBorderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Risk Trend — Last 15 Minutes",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightTextPrimary
                    )
                    Text(
                        text = "Stable → Increasing → Rapid Increase",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RiskCautionText
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AiBlueBg)
                        .border(1.dp, AiBlueBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "AI TELEMETRY",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = AiBlue
                    )
                }
            }

            // Canvas Line Chart (Light Theme)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LightBgSubtle)
                    .padding(8.dp)
            ) {
                TrendChartCanvas()
            }

            // Time & Threshold Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = RiskSafeGreen, label = "Safe (0-4)")
                    LegendItem(color = RiskCautionOrange, label = "Caution (4-7)")
                    LegendItem(color = RiskHighRed, label = "High Risk (7+)")
                }

                Text(
                    text = "+5m Prediction Horizon",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = AiPurple
                )
            }
        }
    }
}

@Composable
private fun TrendChartCanvas() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Threshold Guidelines
        val ySafe = height * (1f - 0.4f)
        val yCaution = height * (1f - 0.7f)

        // Draw light dotted grid threshold lines
        drawLine(
            color = RiskSafeGreen.copy(alpha = 0.35f),
            start = Offset(0f, ySafe),
            end = Offset(width, ySafe),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
        )
        drawLine(
            color = RiskHighRed.copy(alpha = 0.35f),
            start = Offset(0f, yCaution),
            end = Offset(width, yCaution),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
        )

        // Data points: -15m (2.0), -10m (2.4), -5m (4.2), NOW (6.8), +5m PREDICTED (8.2)
        val points = listOf(
            Offset(width * 0.05f, height * (1f - 0.20f)), // -15m: 2.0 (Stable)
            Offset(width * 0.28f, height * (1f - 0.24f)), // -10m: 2.4 (Stable)
            Offset(width * 0.52f, height * (1f - 0.42f)), // -5m: 4.2 (Increasing)
            Offset(width * 0.75f, height * (1f - 0.68f)), // NOW: 6.8 (Caution)
            Offset(width * 0.95f, height * (1f - 0.82f))  // +5m: 8.2 (Rapid Surge Predicted)
        )

        // Historical line (0 to NOW)
        val historyPath = Path().apply {
            moveTo(points[0].x, points[0].y)
            lineTo(points[1].x, points[1].y)
            lineTo(points[2].x, points[2].y)
            lineTo(points[3].x, points[3].y)
        }

        // Fill below history
        val fillPath = Path().apply {
            moveTo(points[0].x, height)
            lineTo(points[0].x, points[0].y)
            lineTo(points[1].x, points[1].y)
            lineTo(points[2].x, points[2].y)
            lineTo(points[3].x, points[3].y)
            lineTo(points[3].x, height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    RiskCautionOrange.copy(alpha = 0.25f),
                    Color.Transparent
                )
            )
        )

        drawPath(
            path = historyPath,
            color = RiskCautionOrange,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Dashed prediction line (NOW to +5m PREDICTED)
        val predictPath = Path().apply {
            moveTo(points[3].x, points[3].y)
            lineTo(points[4].x, points[4].y)
        }

        drawPath(
            path = predictPath,
            color = RiskHighRed,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
            )
        )

        // Point dots
        points.forEachIndexed { index, point ->
            val dotColor = when (index) {
                0, 1 -> RiskSafeGreen
                2, 3 -> RiskCautionOrange
                else -> RiskHighRed
            }
            drawCircle(
                color = Color.White,
                radius = 5.dp.toPx(),
                center = point
            )
            drawCircle(
                color = dotColor,
                radius = 3.5.dp.toPx(),
                center = point
            )
        }

        // Callout halo at NOW point
        val nowPoint = points[3]
        drawCircle(
            color = RiskCautionOrange.copy(alpha = 0.25f),
            radius = 9.dp.toPx(),
            center = nowPoint
        )
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = 9.5.sp,
            color = LightTextSecondary
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// 7. AI RECOMMENDATION (CrowdShield Recommends)
// ════════════════════════════════════════════════════════════════════════════════
@Composable
private fun AiRecommendationCard(
    onViewRouteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightBgCard),
        border = BorderStroke(1.2.dp, AiBlueBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AiBlueBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = AiBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "🧠 CROWDSHIELD RECOMMENDS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = AiBlue,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "AUTOMATED CROWD DIVERSION DIRECTIVE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightTextMuted
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(RiskSafeBg)
                        .border(1.dp, RiskSafeBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "AI OPTIMIZED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = RiskSafeText
                    )
                }
            }

            // Directive Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(AiBlueBg)
                    .border(1.dp, AiBlueBorder, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "\"Redirect movement away from Sector 4.\"",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightTextPrimary
                )
            }

            // Specs Row: Recommended Exit & Estimated Congestion
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Recommended Exit
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(LightBgSubtle)
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Recommended Exit",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = LightTextSecondary
                        )
                        Text(
                            text = "Exit B",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = RiskSafeText
                        )
                        Text(
                            text = "West Concourse Bypass",
                            fontSize = 9.5.sp,
                            color = LightTextMuted
                        )
                    }
                }

                // Estimated Congestion
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(LightBgSubtle)
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Estimated Congestion",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = LightTextSecondary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(RiskSafeGreen)
                            )
                            Text(
                                text = "LOW",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = RiskSafeText
                            )
                        }
                        Text(
                            text = "Flow capacity: 94%",
                            fontSize = 9.5.sp,
                            color = LightTextMuted
                        )
                    }
                }
            }

            // View Route Button
            Button(
                onClick = onViewRouteClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AiBlue,
                    contentColor = Color.White
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Route,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "VIEW ROUTE",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.4.sp
                    )
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// 8. ACTIVE INCIDENTS SECTION
// ════════════════════════════════════════════════════════════════════════════════
@Composable
private fun ActiveIncidentsSection(
    incidents: List<StaffIncidentItem>,
    onViewAllClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "ACTIVE INCIDENTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = LightTextSecondary,
                    letterSpacing = 1.1.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(RiskHighBg)
                        .border(0.8.dp, RiskHighBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${incidents.size}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = RiskHighText
                    )
                }
            }

            TextButton(
                onClick = onViewAllClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Text(
                    text = "VIEW ALL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AiBlue
                )
            }
        }

        incidents.forEach { incident ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = LightBgCard),
                border = BorderStroke(1.dp, incident.riskLevel.borderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(incident.riskLevel.color)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = incident.sector,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightTextPrimary
                            )
                            Text(
                                text = incident.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = incident.riskLevel.textColor
                            )
                            incident.officerAssigned?.let {
                                Text(
                                    text = it,
                                    fontSize = 10.sp,
                                    color = LightTextSecondary
                                )
                            }
                        }
                    }

                    Text(
                        text = incident.timeAgo,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = LightTextMuted
                    )
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// 9. QUICK ACTIONS SECTION (Analyze Image, CCTV, Command Map, Incidents)
// ════════════════════════════════════════════════════════════════════════════════
@Composable
private fun QuickActionsSection(
    onActionClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "QUICK ACTIONS",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = LightTextSecondary,
            letterSpacing = 1.1.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Psychology,
                label = "Analyze Image",
                accentColor = AiPurple,
                bgColor = AiPurpleBg,
                onClick = { onActionClick("ANALYSIS") }
            )
            QuickActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Videocam,
                label = "CCTV",
                accentColor = AiBlue,
                bgColor = AiBlueBg,
                onClick = { onActionClick("CCTV") }
            )
            QuickActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Map,
                label = "Command Map",
                accentColor = RiskSafeGreen,
                bgColor = RiskSafeBg,
                onClick = { onActionClick("MAP") }
            )
            QuickActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Emergency,
                label = "Incidents",
                accentColor = RiskHighRed,
                bgColor = RiskHighBg,
                onClick = { onActionClick("INCIDENTS") }
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    accentColor: Color,
    bgColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LightBgCard),
        border = BorderStroke(1.dp, LightBorderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = LightTextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// 10. STAFF COMMAND MAP UI (Purpose-Built Venue Safety Map)
// ════════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffCommandMapModal(
    sectors: List<StaffSectorData>,
    exits: List<StaffExitItem>,
    incidents: List<StaffIncidentItem>,
    onDismiss: () -> Unit,
    onSelectSector: (StaffSectorData) -> Unit
) {
    var selectedSectorId by remember { mutableStateOf(4) }
    var showRiskLayer by remember { mutableStateOf(true) }
    var showDensityLayer by remember { mutableStateOf(true) }
    var showExitLayer by remember { mutableStateOf(true) }
    var showIncidentLayer by remember { mutableStateOf(true) }

    val activeSelectedSector = sectors.firstOrNull { it.id == selectedSectorId } ?: sectors.first()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    tint = AiBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Staff Command Safety Map",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = LightTextPrimary
                                )
                            }
                            Text(
                                text = "LOCATION → DENSITY → RISK → EXITS → RESPONSE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightTextSecondary,
                                letterSpacing = 0.6.sp
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Map",
                                tint = LightTextSecondary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = LightBgCard)
                )
            },
            containerColor = LightBgPage
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Map Controls Filter Bar
                    MapControlsBar(
                        showRiskLayer = showRiskLayer,
                        onToggleRisk = { showRiskLayer = !showRiskLayer },
                        showDensityLayer = showDensityLayer,
                        onToggleDensity = { showDensityLayer = !showDensityLayer },
                        showExitLayer = showExitLayer,
                        onToggleExit = { showExitLayer = !showExitLayer },
                        showIncidentLayer = showIncidentLayer,
                        onToggleIncident = { showIncidentLayer = !showIncidentLayer },
                        onCenterMap = { selectedSectorId = 4 }
                    )

                    // Interactive Venue Canvas Map
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(LightBgSubtle)
                            .border(1.dp, LightBorderSubtle, RoundedCornerShape(16.dp))
                    ) {
                        VenueMapCanvas(
                            sectors = sectors,
                            selectedSectorId = selectedSectorId,
                            showRiskLayer = showRiskLayer,
                            showDensityLayer = showDensityLayer,
                            showExitLayer = showExitLayer,
                            showIncidentLayer = showIncidentLayer,
                            onSectorClicked = { sectorId ->
                                selectedSectorId = sectorId
                            }
                        )

                        // Map Legend (Floating Top-Left)
                        MapFloatingLegend(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(10.dp)
                        )

                        // Staff Monitoring Location Indicator (Floating Top-Right)
                        StaffMonitoringBadge(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                        )
                    }

                    // Sector Quick Selector Pills
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sectors) { sector ->
                            val isSelected = sector.id == selectedSectorId
                            val chipBorder = if (isSelected) sector.riskLevel.color else LightBorderSubtle
                            val chipBg = if (isSelected) sector.riskLevel.bgColor else LightBgCard

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(chipBg)
                                    .border(1.2.dp, chipBorder, RoundedCornerShape(20.dp))
                                    .clickable { selectedSectorId = sector.id }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(sector.riskLevel.color)
                                )
                                Text(
                                    text = "${sector.name} (${sector.riskLevel.label})",
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) sector.riskLevel.textColor else LightTextPrimary
                                )
                            }
                        }
                    }

                    // Sector Information Panel (Docked Bottom Info Panel)
                    SectorInformationPanel(
                        sector = activeSelectedSector,
                        exits = exits,
                        onViewAnalysis = {
                            onSelectSector(activeSelectedSector)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MapControlsBar(
    showRiskLayer: Boolean,
    onToggleRisk: () -> Unit,
    showDensityLayer: Boolean,
    onToggleDensity: () -> Unit,
    showExitLayer: Boolean,
    onToggleExit: () -> Unit,
    showIncidentLayer: Boolean,
    onToggleIncident: () -> Unit,
    onCenterMap: () -> Unit
) {
    Surface(
        color = LightBgCard,
        border = BorderStroke(1.dp, LightBorderSubtle)
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                OutlinedButton(
                    onClick = onCenterMap,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(32.dp),
                    border = BorderStroke(1.dp, LightBorderStrong)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            tint = AiBlue,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Center (S4)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AiBlue
                        )
                    }
                }
            }

            item {
                FilterChip(
                    selected = showRiskLayer,
                    onClick = onToggleRisk,
                    label = { Text("Risk", fontSize = 11.sp) },
                    modifier = Modifier.height(32.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RiskCautionBg,
                        selectedLabelColor = RiskCautionText
                    )
                )
            }

            item {
                FilterChip(
                    selected = showDensityLayer,
                    onClick = onToggleDensity,
                    label = { Text("Density", fontSize = 11.sp) },
                    modifier = Modifier.height(32.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AiBlueBg,
                        selectedLabelColor = AiBlue
                    )
                )
            }

            item {
                FilterChip(
                    selected = showExitLayer,
                    onClick = onToggleExit,
                    label = { Text("Exits", fontSize = 11.sp) },
                    modifier = Modifier.height(32.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RiskSafeBg,
                        selectedLabelColor = RiskSafeText
                    )
                )
            }

            item {
                FilterChip(
                    selected = showIncidentLayer,
                    onClick = onToggleIncident,
                    label = { Text("Incidents", fontSize = 11.sp) },
                    modifier = Modifier.height(32.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RiskHighBg,
                        selectedLabelColor = RiskHighText
                    )
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// CORE VENUE MAP CANVAS COMPONENT
// ════════════════════════════════════════════════════════════════════════════════
@Composable
private fun VenueMapCanvas(
    sectors: List<StaffSectorData>,
    selectedSectorId: Int,
    showRiskLayer: Boolean = true,
    showDensityLayer: Boolean = true,
    showExitLayer: Boolean = true,
    showIncidentLayer: Boolean = true,
    onSectorClicked: (Int) -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mapPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mapPulseAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                val nextId = if (selectedSectorId >= 4) 1 else selectedSectorId + 1
                onSectorClicked(nextId)
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Base Concourse Floor Grid & Corridor Pathways
            val corridorPaint = Color(0xFFE2E8F0)
            val walkwayPaint = Color(0xFFCBD5E1)

            // Outer Perimeter
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(w * 0.05f, h * 0.06f),
                size = Size(w * 0.90f, h * 0.88f),
                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
            )

            // Main Corridors (Cross layout connecting 4 sectors)
            drawRect(
                color = corridorPaint,
                topLeft = Offset(w * 0.45f, h * 0.08f),
                size = Size(w * 0.10f, h * 0.84f)
            )
            drawRect(
                color = corridorPaint,
                topLeft = Offset(w * 0.08f, h * 0.45f),
                size = Size(w * 0.84f, h * 0.10f)
            )

            // Central Concourse Rotunda
            drawCircle(
                color = Color(0xFFF1F5F9),
                radius = w * 0.14f,
                center = Offset(w * 0.50f, h * 0.50f)
            )
            drawCircle(
                color = walkwayPaint,
                radius = w * 0.14f,
                center = Offset(w * 0.50f, h * 0.50f),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Sector Rectangles
            val s1Rect = Size(w * 0.38f, h * 0.36f)
            val s1Pos = Offset(w * 0.08f, h * 0.08f)

            val s2Rect = Size(w * 0.38f, h * 0.36f)
            val s2Pos = Offset(w * 0.08f, h * 0.56f)

            val s3Rect = Size(w * 0.38f, h * 0.36f)
            val s3Pos = Offset(w * 0.54f, h * 0.08f)

            val s4Rect = Size(w * 0.38f, h * 0.36f)
            val s4Pos = Offset(w * 0.54f, h * 0.56f)

            fun drawSectorBlock(
                pos: Offset,
                rectSize: Size,
                sector: StaffSectorData,
                isSelected: Boolean
            ) {
                // Fill Base
                drawRoundRect(
                    color = if (showRiskLayer) sector.riskLevel.bgColor.copy(alpha = 0.7f) else Color(0xFFF8FAFC),
                    topLeft = pos,
                    size = rectSize,
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                )

                // High Risk Pulse on Sector 4
                if (sector.id == 4 && showRiskLayer) {
                    drawRoundRect(
                        color = RiskHighRed.copy(alpha = pulseAlpha * 0.25f),
                        topLeft = pos,
                        size = rectSize,
                        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                    )
                }

                // Sector Border
                val strokeColor = when {
                    isSelected -> sector.riskLevel.color
                    sector.id == 4 -> RiskHighRed.copy(alpha = 0.85f)
                    else -> sector.riskLevel.borderColor
                }
                drawRoundRect(
                    color = strokeColor,
                    topLeft = pos,
                    size = rectSize,
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                    style = Stroke(width = if (isSelected || sector.id == 4) 2.5.dp.toPx() else 1.2.dp.toPx())
                )

                // Density nodes visualization
                if (showDensityLayer) {
                    val numDots = (sector.densityPercent / 12).coerceIn(2, 10)
                    for (i in 0 until numDots) {
                        val dx = pos.x + (rectSize.width * 0.18f) + ((i % 3) * (rectSize.width * 0.28f))
                        val dy = pos.y + (rectSize.height * 0.30f) + ((i / 3) * (rectSize.height * 0.25f))
                        drawCircle(
                            color = sector.riskLevel.color.copy(alpha = 0.50f),
                            radius = 3.dp.toPx(),
                            center = Offset(dx, dy)
                        )
                    }
                }
            }

            drawSectorBlock(s1Pos, s1Rect, sectors[0], selectedSectorId == 1)
            drawSectorBlock(s2Pos, s2Rect, sectors[1], selectedSectorId == 2)
            drawSectorBlock(s3Pos, s3Rect, sectors[2], selectedSectorId == 3)
            drawSectorBlock(s4Pos, s4Rect, sectors[3], selectedSectorId == 4)

            // Entrances (North & West Gates)
            drawCircle(color = AiBlue, radius = 5.dp.toPx(), center = Offset(w * 0.25f, h * 0.06f))
            drawCircle(color = AiBlue, radius = 5.dp.toPx(), center = Offset(w * 0.05f, h * 0.25f))

            // Exits Markers
            if (showExitLayer) {
                // Exit A (North - Top)
                drawCircle(color = RiskSafeGreen, radius = 6.dp.toPx(), center = Offset(w * 0.50f, h * 0.06f))
                // Exit B (West - Left, Recommended)
                drawCircle(color = RiskSafeGreen, radius = 7.dp.toPx(), center = Offset(w * 0.05f, h * 0.50f))
                drawCircle(color = Color.White, radius = 4.dp.toPx(), center = Offset(w * 0.05f, h * 0.50f))
                drawCircle(color = RiskSafeGreen, radius = 2.5.dp.toPx(), center = Offset(w * 0.05f, h * 0.50f))

                // Exit C (East - Right, Busy)
                drawCircle(color = RiskCautionOrange, radius = 6.dp.toPx(), center = Offset(w * 0.95f, h * 0.50f))
            }

            // Incident Marker on Sector 4
            if (showIncidentLayer) {
                val incPos = Offset(w * 0.73f, h * 0.70f)
                drawCircle(
                    color = RiskHighRed.copy(alpha = pulseAlpha * 0.45f),
                    radius = 13.dp.toPx(),
                    center = incPos
                )
                drawCircle(
                    color = RiskHighRed,
                    radius = 6.5.dp.toPx(),
                    center = incPos
                )
            }
        }
    }
}

@Composable
private fun MapFloatingLegend(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, LightBorderSubtle),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = "MAP LEGEND",
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold,
                color = LightTextSecondary,
                letterSpacing = 0.5.sp
            )
            LegendRow(color = RiskSafeGreen, label = "Safe (S1, S2)")
            LegendRow(color = RiskCautionOrange, label = "Caution (S3)")
            LegendRow(color = RiskHighRed, label = "High Risk (S4)")
        }
    }
}

@Composable
private fun LegendRow(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = LightTextPrimary
        )
    }
}

@Composable
private fun StaffMonitoringBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, LightBorderSubtle),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Sensors,
                contentDescription = null,
                tint = AiBlue,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "HQ Command Active",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = AiBlue
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// SECTOR INFORMATION DOCKED PANEL
// ════════════════════════════════════════════════════════════════════════════════
@Composable
private fun SectorInformationPanel(
    sector: StaffSectorData,
    exits: List<StaffExitItem>,
    onViewAnalysis: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LightBgCard),
        border = BorderStroke(1.2.dp, sector.riskLevel.borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(sector.riskLevel.color)
                    )
                    Column {
                        Text(
                            text = "${sector.name} — ${sector.subLocation}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = LightTextPrimary
                        )
                        Text(
                            text = sector.statusDetail,
                            fontSize = 11.sp,
                            color = LightTextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(sector.riskLevel.bgColor)
                        .border(1.dp, sector.riskLevel.borderColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = sector.riskLevel.label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = sector.riskLevel.textColor
                    )
                }
            }

            // Metric Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SectorMetricPill(
                    modifier = Modifier.weight(1f),
                    label = "Density",
                    value = "${sector.densityPercent}%",
                    textColor = if (sector.densityPercent > 70) RiskHighText else LightTextPrimary
                )
                SectorMetricPill(
                    modifier = Modifier.weight(1f),
                    label = "Trend",
                    value = sector.trend,
                    textColor = if (sector.trendDirection > 0) RiskHighText else RiskSafeText
                )
                SectorMetricPill(
                    modifier = Modifier.weight(1f),
                    label = "Risk Index",
                    value = "${sector.riskScore}/10",
                    textColor = sector.riskLevel.textColor
                )
            }

            // Exit status markers
            Text(
                text = "🚪 Exit A: Open · 🚪 Exit B: Low Congestion · 🚪 Exit C: Busy",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = LightTextSecondary
            )

            // Action Button
            Button(
                onClick = onViewAnalysis,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (sector.id == 4) RiskCautionOrange else AiBlue,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "VIEW ANALYSIS & TELEMETRY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SectorMetricPill(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    textColor: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(LightBgSubtle)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = LightTextSecondary
            )
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = textColor
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// 11. BOTTOM NAVIGATION BAR (Exactly 4 Staff Tabs)
// ════════════════════════════════════════════════════════════════════════════════
@Composable
private fun StaffBottomNavigationBar(
    selectedTab: StaffTab,
    onTabSelected: (StaffTab) -> Unit
) {
    Surface(
        color = LightBgCard,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, LightBorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StaffTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                val color = if (isSelected) AiBlue else LightTextSecondary

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) AiBlueBg else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = color,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = tab.title,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                        color = color
                    )
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// 12. DIALOGS WITH SPECIALIZED MAP VISUALIZATIONS
// ════════════════════════════════════════════════════════════════════════════════

// Sector Detail Dialog (With Sector Footprint & Sensor Grid Map)
@Composable
private fun SectorDetailDialog(
    sector: StaffSectorData,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = LightBgCard),
            border = BorderStroke(1.5.dp, sector.riskLevel.borderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(sector.riskLevel.color)
                        )
                        Text(
                            text = "${sector.name} Telemetry & Footprint",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = LightTextPrimary
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = LightTextSecondary
                        )
                    }
                }

                Text(
                    text = sector.subLocation,
                    fontSize = 11.5.sp,
                    color = LightTextSecondary
                )

                // Embedded Sector Footprint & Sensor Map Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(LightBgSubtle)
                        .border(1.dp, LightBorderSubtle, RoundedCornerShape(10.dp))
                ) {
                    SectorFootprintMapCanvas(sector = sector)
                }

                // Metric Grid in Dialog
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    DetailRow(label = "Risk Index", value = "${sector.riskScore} / 10 (${sector.riskLevel.label})", valueColor = sector.riskLevel.textColor)
                    DetailRow(label = "Current Density", value = "${sector.densityPercent}% of capacity", valueColor = LightTextPrimary)
                    DetailRow(label = "5-Min Velocity", value = sector.trend, valueColor = if (sector.trendDirection > 0) RiskHighText else RiskSafeText)
                    DetailRow(label = "Crowd Flow Speed", value = sector.flowRate, valueColor = LightTextPrimary)
                    DetailRow(label = "Active Sensors", value = "${sector.sensorCount} Nodes Reporting", valueColor = RiskSafeText)
                }

                if (sector.id == 4) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(RiskHighBg)
                            .border(1.dp, RiskHighBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "⚠ AI Early Prediction: Influx surge projected in 5 min. Recommended: Activate Exit B tactical bypass.",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RiskHighText,
                            lineHeight = 14.sp
                        )
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LightBgSubtle)
                ) {
                    Text("CLOSE TELEMETRY VIEW", fontWeight = FontWeight.Bold, color = LightTextPrimary)
                }
            }
        }
    }
}

@Composable
private fun SectorFootprintMapCanvas(sector: StaffSectorData) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Background grid lines
        val gridStep = 20.dp.toPx()
        var x = 0f
        while (x < w) {
            drawLine(Color(0xFFE2E8F0), Offset(x, 0f), Offset(x, h), strokeWidth = 0.8f)
            x += gridStep
        }
        var y = 0f
        while (y < h) {
            drawLine(Color(0xFFE2E8F0), Offset(0f, y), Offset(w, y), strokeWidth = 0.8f)
            y += gridStep
        }

        // Sector Footprint Polygon
        val sectorRect = Size(w * 0.70f, h * 0.70f)
        val sectorPos = Offset(w * 0.15f, h * 0.15f)

        drawRoundRect(
            color = sector.riskLevel.bgColor,
            topLeft = sectorPos,
            size = sectorRect,
            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
        )
        drawRoundRect(
            color = sector.riskLevel.color,
            topLeft = sectorPos,
            size = sectorRect,
            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
            style = Stroke(width = 2.dp.toPx())
        )

        // 12 Sensor Nodes visualization
        for (i in 0 until 12) {
            val col = i % 4
            val row = i / 4
            val sx = sectorPos.x + (sectorRect.width * 0.15f) + (col * (sectorRect.width * 0.23f))
            val sy = sectorPos.y + (sectorRect.height * 0.20f) + (row * (sectorRect.height * 0.30f))

            drawCircle(color = sector.riskLevel.color.copy(alpha = 0.2f), radius = 6.dp.toPx(), center = Offset(sx, sy))
            drawCircle(color = sector.riskLevel.color, radius = 2.5.dp.toPx(), center = Offset(sx, sy))
        }

        // Inflow / Outflow Vector Arrows
        drawLine(
            color = AiBlue,
            start = Offset(w * 0.05f, h * 0.50f),
            end = Offset(w * 0.15f, h * 0.50f),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = RiskSafeGreen,
            start = Offset(w * 0.85f, h * 0.50f),
            end = Offset(w * 0.95f, h * 0.50f),
            strokeWidth = 2.dp.toPx()
        )
    }
}

// AI Route Recommendation Dialog (With Tactical Route Diversion Map Canvas)
@Composable
private fun AiRouteRecommendationDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = LightBgCard),
            border = BorderStroke(1.5.dp, AiBlueBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Route,
                            contentDescription = null,
                            tint = AiBlue,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Tactical Diversion Route Map",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = LightTextPrimary
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = LightTextSecondary
                        )
                    }
                }

                // Tactical Route Map Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(LightBgSubtle)
                        .border(1.dp, LightBorderSubtle, RoundedCornerShape(10.dp))
                ) {
                    TacticalRouteMapCanvas()
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AiBlueBg)
                        .border(1.dp, AiBlueBorder, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "PRIMARY DIVERSION VECTOR",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AiBlue
                        )
                        Text(
                            text = "Sector 4 (East Concourse) ➔ Corridor 3B ➔ Exit B",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightTextPrimary
                        )
                        Text(
                            text = "Estimated Relief: -40% Pressure in 8 minutes",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = RiskSafeText
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    DetailRow(label = "Target Exit", value = "Exit B (West)", valueColor = RiskSafeText)
                    DetailRow(label = "Exit Congestion", value = "LOW (18%)", valueColor = RiskSafeText)
                    DetailRow(label = "Field Staff", value = "4 Marshals Notified", valueColor = LightTextPrimary)
                    DetailRow(label = "LED Dynamic Signage", value = "Broadcast Ready", valueColor = AiBlue)
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AiBlue)
                ) {
                    Text(
                        text = "CONFIRM & BROADCAST ROUTE",
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun TacticalRouteMapCanvas() {
    val infiniteTransition = rememberInfiniteTransition(label = "routePulse")
    val dashPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "routeDash"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Venue Floor Layout
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.05f, h * 0.08f),
            size = Size(w * 0.90f, h * 0.84f),
            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
        )

        // Sectors
        // Sector 4 (Origin - Red)
        val s4Pos = Offset(w * 0.58f, h * 0.52f)
        val s4Size = Size(w * 0.32f, h * 0.35f)
        drawRoundRect(
            color = RiskHighBg,
            topLeft = s4Pos,
            size = s4Size,
            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
        )
        drawRoundRect(
            color = RiskHighRed,
            topLeft = s4Pos,
            size = s4Size,
            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
            style = Stroke(width = 1.5.dp.toPx())
        )

        // Exit B Target Area (West - Green)
        val exitBPos = Offset(w * 0.10f, h * 0.52f)
        val exitBSize = Size(w * 0.22f, h * 0.35f)
        drawRoundRect(
            color = RiskSafeBg,
            topLeft = exitBPos,
            size = exitBSize,
            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
        )
        drawRoundRect(
            color = RiskSafeGreen,
            topLeft = exitBPos,
            size = exitBSize,
            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
            style = Stroke(width = 1.5.dp.toPx())
        )

        // Corridor 3B Bypass Route
        val routePath = Path().apply {
            moveTo(s4Pos.x + s4Size.width * 0.1f, s4Pos.y + s4Size.height * 0.5f)
            lineTo(w * 0.50f, h * 0.30f) // Central concourse bypass
            lineTo(exitBPos.x + exitBSize.width * 0.8f, exitBPos.y + exitBSize.height * 0.5f)
        }

        // Animated Tactical Diversion Line
        drawPath(
            path = routePath,
            color = RiskSafeGreen,
            style = Stroke(
                width = 3.5.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), dashPhase)
            )
        )

        // Markers
        // Origin Pin
        drawCircle(color = RiskHighRed, radius = 5.dp.toPx(), center = Offset(s4Pos.x + s4Size.width * 0.1f, s4Pos.y + s4Size.height * 0.5f))
        // Target Exit B Pin
        drawCircle(color = RiskSafeGreen, radius = 6.dp.toPx(), center = Offset(exitBPos.x + exitBSize.width * 0.8f, exitBPos.y + exitBSize.height * 0.5f))
        drawCircle(color = Color.White, radius = 3.dp.toPx(), center = Offset(exitBPos.x + exitBSize.width * 0.8f, exitBPos.y + exitBSize.height * 0.5f))
    }
}

// All Incidents Dialog (With Spatial Incident Map)
@Composable
private fun AllIncidentsDialog(
    incidents: List<StaffIncidentItem>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = LightBgCard),
            border = BorderStroke(1.dp, LightBorderSubtle),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Incident Dispatch Map & Logs",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightTextPrimary
                    )

                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = LightTextSecondary
                        )
                    }
                }

                // Spatial Incident Map Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(LightBgSubtle)
                        .border(1.dp, LightBorderSubtle, RoundedCornerShape(10.dp))
                ) {
                    IncidentSpatialMapCanvas(incidents = incidents)
                }

                incidents.forEach { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(LightBgSubtle)
                            .border(1.dp, item.riskLevel.borderColor, RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${item.id} · ${item.sector}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LightTextPrimary
                                )
                                Text(
                                    text = item.timeAgo,
                                    fontSize = 10.5.sp,
                                    color = LightTextMuted
                                )
                            }
                            Text(
                                text = item.title,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = item.riskLevel.textColor
                            )
                            item.officerAssigned?.let {
                                Text(
                                    text = "Status: $it",
                                    fontSize = 10.sp,
                                    color = LightTextSecondary
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LightBgSubtle)
                ) {
                    Text("CLOSE INCIDENT LOGS", fontWeight = FontWeight.Bold, color = LightTextPrimary)
                }
            }
        }
    }
}

@Composable
private fun IncidentSpatialMapCanvas(incidents: List<StaffIncidentItem>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Venue Outline
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.05f, h * 0.08f),
            size = Size(w * 0.90f, h * 0.84f),
            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
        )

        // Sector grid lines
        drawLine(Color(0xFFCBD5E1), Offset(w * 0.50f, h * 0.08f), Offset(w * 0.50f, h * 0.92f), strokeWidth = 1f)
        drawLine(Color(0xFFCBD5E1), Offset(w * 0.05f, h * 0.50f), Offset(w * 0.95f, h * 0.50f), strokeWidth = 1f)

        // Incident Marker 1: Sector 4 (Bottom-Right)
        val inc1 = Offset(w * 0.75f, h * 0.72f)
        drawCircle(color = RiskHighRed.copy(alpha = 0.3f), radius = 12.dp.toPx(), center = inc1)
        drawCircle(color = RiskHighRed, radius = 6.dp.toPx(), center = inc1)

        // Unit Alpha-3 Dispatched vector
        drawLine(color = AiBlue, start = Offset(w * 0.50f, h * 0.50f), end = inc1, strokeWidth = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
        drawCircle(color = AiBlue, radius = 3.5.dp.toPx(), center = Offset(w * 0.50f, h * 0.50f))

        // Incident Marker 2: Exit B (Left)
        val inc2 = Offset(w * 0.08f, h * 0.50f)
        drawCircle(color = RiskCautionOrange.copy(alpha = 0.3f), radius = 10.dp.toPx(), center = inc2)
        drawCircle(color = RiskCautionOrange, radius = 5.dp.toPx(), center = inc2)
    }
}

// Quick Action Modal (CCTV Spatial Coverage / Incidents)
@Composable
private fun QuickActionModal(
    actionType: String,
    sectors: List<StaffSectorData>,
    incidents: List<StaffIncidentItem>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = LightBgCard),
            border = BorderStroke(1.2.dp, LightBorderStrong),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (actionType) {
                            "CCTV" -> "📹 CCTV Spatial Coverage"
                            else -> "🚨 Incident Dispatch Spatial Map"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightTextPrimary
                    )

                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = LightTextSecondary
                        )
                    }
                }

                when (actionType) {
                    "CCTV" -> {
                        // CCTV Spatial Coverage Map + Camera Grid
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(LightBgSubtle)
                                .border(1.dp, LightBorderSubtle, RoundedCornerShape(10.dp))
                        ) {
                            CctvCoverageMapCanvas()
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CctvPreviewBox(modifier = Modifier.weight(1f), label = "CAM-01 (North Gate)")
                                CctvPreviewBox(modifier = Modifier.weight(1f), label = "CAM-02 (West Plaza)")
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CctvPreviewBox(modifier = Modifier.weight(1f), label = "CAM-03 (Arena Stage)")
                                CctvPreviewBox(modifier = Modifier.weight(1f), label = "CAM-04 (East Concourse)", isAlert = true)
                            }
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(LightBgSubtle)
                                .border(1.dp, LightBorderSubtle, RoundedCornerShape(10.dp))
                        ) {
                            IncidentSpatialMapCanvas(incidents = incidents)
                        }
                        Text(
                            text = "2 active sector dispatches. Unit Alpha-3 currently en route to Sector 4 corridor.",
                            fontSize = 11.5.sp,
                            color = LightTextSecondary
                        )
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LightBgSubtle)
                ) {
                    Text("BACK TO DASHBOARD", fontWeight = FontWeight.Bold, color = LightTextPrimary)
                }
            }
        }
    }
}

@Composable
private fun CctvCoverageMapCanvas() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.05f, h * 0.08f),
            size = Size(w * 0.90f, h * 0.84f),
            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
        )

        // Camera nodes & vision coverage cones
        fun drawCameraCone(pos: Offset, angleColor: Color, isAlert: Boolean = false) {
            drawCircle(color = angleColor.copy(alpha = 0.25f), radius = 22.dp.toPx(), center = pos)
            drawCircle(color = angleColor, radius = 4.dp.toPx(), center = pos)
        }

        drawCameraCone(Offset(w * 0.25f, h * 0.25f), AiBlue) // CAM-01
        drawCameraCone(Offset(w * 0.25f, h * 0.75f), AiBlue) // CAM-02
        drawCameraCone(Offset(w * 0.75f, h * 0.25f), RiskCautionOrange) // CAM-03
        drawCameraCone(Offset(w * 0.75f, h * 0.75f), RiskHighRed, isAlert = true) // CAM-04
    }
}

@Composable
private fun CctvPreviewBox(modifier: Modifier = Modifier, label: String, isAlert: Boolean = false) {
    Box(
        modifier = modifier
            .height(55.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isAlert) RiskHighBg else LightBgSubtle)
            .border(
                1.dp,
                if (isAlert) RiskHighBorder else LightBorderSubtle,
                RoundedCornerShape(8.dp)
            )
            .padding(6.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (isAlert) RiskHighText else LightTextPrimary
            )
            PulsingDot(color = if (isAlert) RiskHighRed else RiskSafeGreen, size = 5)
        }
    }
}

// Logout Confirmation Dialog
@Composable
private fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = LightBgCard),
            border = BorderStroke(1.dp, LightBorderSubtle),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Confirm Staff Logout",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightTextPrimary
                )
                Text(
                    text = "Are you sure you want to exit the Staff Command Dashboard and return to the login screen?",
                    fontSize = 12.sp,
                    color = LightTextSecondary,
                    lineHeight = 16.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel", color = LightTextPrimary)
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RiskHighRed)
                    ) {
                        Text("Logout", color = Color.White)
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// HELPER UI COMPONENTS
// ════════════════════════════════════════════════════════════════════════════════
@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            color = LightTextSecondary
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
private fun PulsingDot(color: Color, size: Int = 8) {
    val infiniteTransition = rememberInfiniteTransition(label = "dotPulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
    )
}

@Composable
private fun OfficerAuthorityFooter() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = LightTextMuted,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "CrowdShield Command Center · Authorized Staff Operations Only",
            fontSize = 10.sp,
            color = LightTextMuted,
            textAlign = TextAlign.Center
        )
    }
}
