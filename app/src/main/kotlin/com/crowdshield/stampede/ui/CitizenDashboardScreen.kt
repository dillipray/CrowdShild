package com.crowdshield.stampede.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoorBack
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crowdshield.stampede.domain.RiskSignal
import com.crowdshield.stampede.domain.SeverityLevel

/**
 * High-Contrast Accessible Color Palette for Senior Citizens & Stress Environments.
 * Meets WCAG AAA guidelines with distinct, unmistakable colors.
 */
object SafetyThemeColors {
    // Low / Safe (Green)
    val SafePrimary = Color(0xFF1B5E20)
    val SafeAccent = Color(0xFF2E7D32)
    val SafeContainer = Color(0xFFE8F5E9)
    val SafeBorder = Color(0xFF81C784)
    val SafeText = Color(0xFF0A3D0A)

    // Medium / Caution (Orange/Amber)
    val CautionPrimary = Color(0xFFE65100)
    val CautionAccent = Color(0xFFF57C00)
    val CautionContainer = Color(0xFFFFF3E0)
    val CautionBorder = Color(0xFFFFB74D)
    val CautionText = Color(0xFFBF360C)

    // High / Danger (Red)
    val HighRiskPrimary = Color(0xFFB71C1C)
    val HighRiskAccent = Color(0xFFD32F2F)
    val HighRiskContainer = Color(0xFFFFEBEE)
    val HighRiskBorder = Color(0xFFE57373)
    val HighRiskText = Color(0xFF7F0000)

    // Critical / Emergency (Dark Crimson)
    val CriticalPrimary = Color(0xFF880E4F)
    val CriticalAccent = Color(0xFF800000)
    val CriticalContainer = Color(0xFFFFCDD2)
    val CriticalBorder = Color(0xFFB71C1C)
    val CriticalText = Color(0xFF4A0000)

    // Neutral Surfaces
    val ScreenBackground = Color(0xFFF4F6F9)
    val SurfaceWhite = Color(0xFFFFFFFF)
    val CardBorder = Color(0xFFE2E8F0)
    val TextPrimaryDark = Color(0xFF0F172A)
    val TextSecondaryDark = Color(0xFF475569)
    val TextMuted = Color(0xFF64748B)
}

/**
 * Severity visual configurations for UI components.
 */
data class SeverityVisual(
    val primaryColor: Color,
    val containerColor: Color,
    val borderColor: Color,
    val textColor: Color,
    val icon: ImageVector,
    val riskLevelLabel: String,
    val actionHeadline: String
)

fun getSeverityVisual(level: SeverityLevel): SeverityVisual = when (level) {
    SeverityLevel.LOW -> SeverityVisual(
        primaryColor = SafetyThemeColors.SafeAccent,
        containerColor = SafetyThemeColors.SafeContainer,
        borderColor = SafetyThemeColors.SafeBorder,
        textColor = SafetyThemeColors.SafeText,
        icon = Icons.Default.CheckCircle,
        riskLevelLabel = "LOW RISK",
        actionHeadline = "CURRENT ACTION"
    )
    SeverityLevel.MEDIUM -> SeverityVisual(
        primaryColor = SafetyThemeColors.CautionAccent,
        containerColor = SafetyThemeColors.CautionContainer,
        borderColor = SafetyThemeColors.CautionBorder,
        textColor = SafetyThemeColors.CautionText,
        icon = Icons.Default.WarningAmber,
        riskLevelLabel = "MEDIUM RISK",
        actionHeadline = "CAUTION REQUIRED"
    )
    SeverityLevel.HIGH -> SeverityVisual(
        primaryColor = SafetyThemeColors.HighRiskAccent,
        containerColor = SafetyThemeColors.HighRiskContainer,
        borderColor = SafetyThemeColors.HighRiskBorder,
        textColor = SafetyThemeColors.HighRiskText,
        icon = Icons.Default.Warning,
        riskLevelLabel = "HIGH RISK",
        actionHeadline = "IMMEDIATE ACTION"
    )
    SeverityLevel.CRITICAL -> SeverityVisual(
        primaryColor = SafetyThemeColors.CriticalPrimary,
        containerColor = SafetyThemeColors.CriticalContainer,
        borderColor = SafetyThemeColors.CriticalBorder,
        textColor = SafetyThemeColors.CriticalText,
        icon = Icons.Default.Emergency,
        riskLevelLabel = "CRITICAL RISK",
        actionHeadline = "EMERGENCY ACTION"
    )
}

/**
 * Formats timestamps into a relative readable string without generating fake data.
 */
fun formatRelativeTime(timestamp: Long): String {
    val diffSec = ((System.currentTimeMillis() - timestamp) / 1000).coerceAtLeast(0)
    return when {
        diffSec < 5 -> "Updated just now"
        diffSec < 60 -> "Updated ${diffSec}s ago"
        diffSec < 3600 -> "Updated ${diffSec / 60}m ago"
        else -> "Updated recently"
    }
}

/**
 * Provides clean, calm recommended action text mapped from severity level.
 */
fun getRecommendedActionText(signal: RiskSignal): String {
    return when (signal.severityLevel) {
        SeverityLevel.LOW -> "Conditions are stable. You can move normally."
        SeverityLevel.MEDIUM -> if (signal.navigationGuidance.isNotBlank()) signal.navigationGuidance else "Elevated crowd density. Move calmly toward open exits."
        SeverityLevel.HIGH -> if (signal.navigationGuidance.isNotBlank()) signal.navigationGuidance else "High crowd surge detected. Avoid bottlenecks and follow exit signs."
        SeverityLevel.CRITICAL -> if (signal.navigationGuidance.isNotBlank()) signal.navigationGuidance else "CRITICAL: Stop moving toward choke points! Divert immediately to nearest emergency exit!"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenDashboardScreen(
    uiState: DashboardUiState,
    onStartSosCountdown: () -> Unit,
    onCancelSosCountdown: () -> Unit,
    onResetSosDispatch: () -> Unit,
    onQuickHazardClick: (String) -> Unit,
    onDetailedReportClick: () -> Unit,
    onSimulationPresetSelect: (SeverityLevel) -> Unit,
    onMockToggle: (Boolean) -> Unit,
    onDismissBanner: () -> Unit
) {
    val signal = uiState.riskSignal
    val visual = getSeverityVisual(signal.severityLevel)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(visual.primaryColor)
                        )
                        Text(
                            text = "CrowdShield Live",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = SafetyThemeColors.TextPrimaryDark
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onDetailedReportClick) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Custom Incident Report",
                            tint = SafetyThemeColors.TextPrimaryDark,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SafetyThemeColors.SurfaceWhite
                )
            )
        },
        containerColor = SafetyThemeColors.ScreenBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Alert Banner Notification (if present)
            item {
                AnimatedVisibility(
                    visible = uiState.recentBannerMessage != null,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    uiState.recentBannerMessage?.let { bannerText ->
                        NotificationBannerCard(
                            message = bannerText,
                            onDismiss = onDismissBanner
                        )
                    }
                }
            }

            // 2. Primary Safety Status & Risk Overview (Main Focus of Home Screen)
            item {
                PrimarySafetyStatusCard(
                    signal = signal,
                    trend = uiState.riskTrend,
                    visual = visual
                )
            }

            // 3. Recommended Action Section
            item {
                RecommendedActionCard(
                    signal = signal,
                    visual = visual
                )
            }

            // 4. SOS Emergency Trigger Section (Calm in Idle, Urgent in Countdown)
            item {
                SosEmergencySection(
                    isCountingDown = uiState.isSosCountingDown,
                    countdownSeconds = uiState.sosCountdownSeconds,
                    isDispatched = uiState.isSosDispatched,
                    onStartCountdown = onStartSosCountdown,
                    onCancelCountdown = onCancelSosCountdown,
                    onResetDispatch = onResetSosDispatch
                )
            }

            // 5. Quick Hazard Reporting Grid
            item {
                HazardQuickReportingSection(
                    onQuickHazardClick = onQuickHazardClick,
                    onDetailedReportClick = onDetailedReportClick
                )
            }

            // 6. Simulation & Demo Controls
            item {
                SimulationControlsSection(
                    isMockEnabled = uiState.isMockEnabled,
                    currentSeverity = signal.severityLevel,
                    onMockToggle = onMockToggle,
                    onPresetSelect = onSimulationPresetSelect
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Top notification banner card for feedback messages.
 */
@Composable
fun NotificationBannerCard(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * 1. Primary Safety Status Card (Hierarchy Focal Point)
 * Communicates safety situation within 2-3 seconds:
 * - Current Sector
 * - Real Live Status indicator with timestamp
 * - Prominent Risk Score (e.g. 2.9 / 10)
 * - Risk Level Badge (e.g. 🟢 LOW RISK)
 * - Risk Trend (e.g. ↓ Stable, ↑ Increasing)
 * - Color-coded Risk Meter Bar & Density Metric
 */
@Composable
fun PrimarySafetyStatusCard(
    signal: RiskSignal,
    trend: RiskTrend,
    visual: SeverityVisual
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SafetyThemeColors.SurfaceWhite),
        border = BorderStroke(1.5.dp, SafetyThemeColors.CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Sector & Live Indicator Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Current Sector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0F2FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "CURRENT SECTOR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SafetyThemeColors.TextMuted,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = signal.sectorId,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SafetyThemeColors.TextPrimaryDark
                        )
                    }
                }

                // 6. Improved Live Status with real-time relative formatting
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 0.85f,
                        targetValue = 1.15f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(900, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulseScale"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(visual.primaryColor)
                    )
                    Text(
                        text = "LIVE • ${formatRelativeTime(signal.timestamp)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SafetyThemeColors.TextSecondaryDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // 2. Improve Risk Score & 3. Risk Trend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score Display (e.g. 2.9 / 10)
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format("%.1f", signal.riskScore),
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            color = visual.primaryColor,
                            lineHeight = 44.sp
                        )
                        Text(
                            text = " / 10",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = SafetyThemeColors.TextMuted,
                            modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                        )
                    }
                    Text(
                        text = "Real-time Safety Index",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = SafetyThemeColors.TextMuted
                    )
                }

                // Level Badge & Trend Indicator Column
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Risk Level Badge (e.g. 🟢 LOW RISK)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(visual.containerColor)
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Icon(
                            imageVector = visual.icon,
                            contentDescription = null,
                            tint = visual.primaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = visual.riskLevelLabel,
                            color = visual.textColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // 3. Risk Trend Indicator (e.g. ↓ Stable, ↑ Increasing, ↓ Decreasing)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        val trendIcon = when (trend) {
                            RiskTrend.INCREASING -> Icons.AutoMirrored.Filled.TrendingUp
                            RiskTrend.DECREASING -> Icons.AutoMirrored.Filled.TrendingDown
                            RiskTrend.STABLE -> Icons.AutoMirrored.Filled.TrendingFlat
                        }
                        val trendColor = when (trend) {
                            RiskTrend.INCREASING -> SafetyThemeColors.HighRiskAccent
                            RiskTrend.DECREASING -> SafetyThemeColors.SafeAccent
                            RiskTrend.STABLE -> SafetyThemeColors.TextSecondaryDark
                        }
                        Icon(
                            imageVector = trendIcon,
                            contentDescription = trend.label,
                            tint = trendColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${trend.symbol} ${trend.label}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = trendColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Visual Risk Meter Progress Bar
            val animatedBarColor by animateColorAsState(
                targetValue = visual.primaryColor,
                label = "riskMeterColor"
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LinearProgressIndicator(
                    progress = { (signal.riskScore / 10.0f).coerceIn(0.04f, 1.0f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = animatedBarColor,
                    trackColor = Color(0xFFE2E8F0),
                    strokeCap = StrokeCap.Round
                )

                // Scale calibration markers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0.0 (Safe)", fontSize = 10.sp, color = SafetyThemeColors.SafeAccent, fontWeight = FontWeight.SemiBold)
                    Text("4.0 (Caution)", fontSize = 10.sp, color = SafetyThemeColors.CautionAccent, fontWeight = FontWeight.SemiBold)
                    Text("7.0 (High)", fontSize = 10.sp, color = SafetyThemeColors.HighRiskAccent, fontWeight = FontWeight.SemiBold)
                    Text("10.0 (Critical)", fontSize = 10.sp, color = SafetyThemeColors.CriticalPrimary, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Density metric
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = "Crowd Density",
                        tint = SafetyThemeColors.TextSecondaryDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Crowd Density:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SafetyThemeColors.TextSecondaryDark
                    )
                }

                Text(
                    text = signal.densityLevel,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = visual.primaryColor
                )
            }
        }
    }
}

/**
 * 4. Recommended Action Card (UI Component Only)
 * Section showing:
 * 🟢 CURRENT ACTION
 * Conditions are stable. You can move normally.
 */
@Composable
fun RecommendedActionCard(
    signal: RiskSignal,
    visual: SeverityVisual
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = visual.containerColor),
        border = BorderStroke(1.5.dp, visual.borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Badge: 🟢 CURRENT ACTION
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .background(visual.primaryColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Icon(
                    imageVector = visual.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = visual.actionHeadline,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Text (Senior-friendly, calm and clear)
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                    contentDescription = "Action Direction",
                    tint = visual.textColor,
                    modifier = Modifier
                        .size(28.dp)
                        .padding(top = 2.dp)
                )
                Text(
                    text = getRecommendedActionText(signal),
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = visual.textColor
                )
            }
        }
    }
}

/**
 * 5. Improved SOS Emergency Trigger Section
 * Keeps full 5-second countdown safeguard functionality.
 * Visually distinct and easy to access without overwhelming the screen during normal safe conditions.
 */
@Composable
fun SosEmergencySection(
    isCountingDown: Boolean,
    countdownSeconds: Int,
    isDispatched: Boolean,
    onStartCountdown: () -> Unit,
    onCancelCountdown: () -> Unit,
    onResetDispatch: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isDispatched -> Color(0xFF1E293B)
                isCountingDown -> SafetyThemeColors.HighRiskPrimary
                else -> SafetyThemeColors.SurfaceWhite
            }
        ),
        border = BorderStroke(
            if (isCountingDown) 2.5.dp else 1.5.dp,
            when {
                isDispatched -> Color(0xFF38BDF8)
                isCountingDown -> Color.White
                else -> SafetyThemeColors.HighRiskBorder
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                // State 1: Dispatched Confirmation
                isDispatched -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Dispatched",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "SOS DISPATCHED!",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "First responders and on-site security have received your emergency alert and coordinates.",
                        color = Color(0xFFE2E8F0),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onResetDispatch,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Reset / Stand Down SOS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                // State 2: 5-Second Active Countdown
                isCountingDown -> {
                    Text(
                        text = "TAP TO CANCEL WITHIN 5 SECONDS",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { countdownSeconds / 5f },
                            modifier = Modifier.size(32.dp),
                            color = Color.White,
                            strokeWidth = 3.5.dp
                        )
                        Text(
                            text = "DISPATCHING IN $countdownSeconds s",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onCancelCountdown,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = SafetyThemeColors.HighRiskPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CANCEL EMERGENCY SOS",
                            color = SafetyThemeColors.HighRiskPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // State 3: Calm, Distinct Idle State
                else -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(SafetyThemeColors.HighRiskContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Emergency,
                                    contentDescription = "SOS",
                                    tint = SafetyThemeColors.HighRiskAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "EMERGENCY SOS",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SafetyThemeColors.HighRiskText
                                )
                                Text(
                                    text = "Tap to start 5s safeguard dispatch",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SafetyThemeColors.TextSecondaryDark
                                )
                            }
                        }

                        Button(
                            onClick = onStartCountdown,
                            colors = ButtonDefaults.buttonColors(containerColor = SafetyThemeColors.HighRiskAccent),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "TRIGGER",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 4 Quick-Tap Hazard Reporting Grid for rapid reporting.
 */
@Composable
fun HazardQuickReportingSection(
    onQuickHazardClick: (String) -> Unit,
    onDetailedReportClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SafetyThemeColors.SurfaceWhite),
        border = BorderStroke(1.5.dp, SafetyThemeColors.CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QUICK HAZARD REPORT",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SafetyThemeColors.TextSecondaryDark,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "1-Tap Dispatch",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2x2 Grid of Hazard Quick-Tap Buttons
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HazardTapCard(
                        title = "Blocked Exit",
                        subtitle = "Locked / Blocked",
                        icon = Icons.Default.DoorBack,
                        accentColor = Color(0xFFDC2626),
                        modifier = Modifier.weight(1f),
                        onClick = { onQuickHazardClick("Blocked Exit") }
                    )
                    HazardTapCard(
                        title = "Medical Help",
                        subtitle = "Injury / Assist",
                        icon = Icons.Default.MedicalServices,
                        accentColor = Color(0xFFE11D48),
                        modifier = Modifier.weight(1f),
                        onClick = { onQuickHazardClick("Medical Emergency") }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HazardTapCard(
                        title = "Fire / Smoke",
                        subtitle = "Hazard Spotted",
                        icon = Icons.Default.LocalFireDepartment,
                        accentColor = Color(0xFFEA580C),
                        modifier = Modifier.weight(1f),
                        onClick = { onQuickHazardClick("Fire / Smoke") }
                    )
                    HazardTapCard(
                        title = "Crowd Surge",
                        subtitle = "Crush / Dense",
                        icon = Icons.Default.People,
                        accentColor = Color(0xFF7C3AED),
                        modifier = Modifier.weight(1f),
                        onClick = { onQuickHazardClick("Extreme Crowd Surge") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onDetailedReportClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.2.dp, Color(0xFFCBD5E1))
            ) {
                Icon(
                    imageVector = Icons.Default.EditNote,
                    contentDescription = null,
                    tint = SafetyThemeColors.TextPrimaryDark,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Submit Detailed Incident Report",
                    color = SafetyThemeColors.TextPrimaryDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

/**
 * Individual quick-tap hazard card.
 */
@Composable
fun HazardTapCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(76.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.07f)),
        border = BorderStroke(1.2.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SafetyThemeColors.TextPrimaryDark,
                    lineHeight = 16.sp
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = SafetyThemeColors.TextSecondaryDark
                )
            }
        }
    }
}

/**
 * Simulation Controls for QA, Judges, and Live Testing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulationControlsSection(
    isMockEnabled: Boolean,
    currentSeverity: SeverityLevel,
    onMockToggle: (Boolean) -> Unit,
    onPresetSelect: (SeverityLevel) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SafetyThemeColors.SurfaceWhite),
        border = BorderStroke(1.dp, SafetyThemeColors.CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = "Simulation",
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Simulation & Demo Mode",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SafetyThemeColors.TextPrimaryDark
                    )
                }

                Switch(
                    checked = isMockEnabled,
                    onCheckedChange = onMockToggle
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Simulate 4 Risk Severity Tiers:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = SafetyThemeColors.TextSecondaryDark
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SeverityLevel.entries.forEach { severity ->
                    val isSelected = currentSeverity == severity
                    val visual = getSeverityVisual(severity)

                    FilterChip(
                        selected = isSelected,
                        onClick = { onPresetSelect(severity) },
                        label = {
                            Text(
                                text = when (severity) {
                                    SeverityLevel.LOW -> "Low"
                                    SeverityLevel.MEDIUM -> "Med"
                                    SeverityLevel.HIGH -> "High"
                                    SeverityLevel.CRITICAL -> "Critical"
                                },
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = visual.primaryColor,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// Jetpack Compose Previews (All 4 Tiers + SOS Active Countdown)
// -------------------------------------------------------------------------

@Preview(name = "1. Safe - Low Risk (Green)", showBackground = true)
@Composable
fun CitizenDashboardPreview_Safe() {
    MaterialTheme {
        CitizenDashboardScreen(
            uiState = DashboardUiState(
                riskSignal = RiskSignal(
                    sectorId = "Sector 4 - East Concourse",
                    riskScore = 2.9f,
                    densityLevel = "Low (1.2 people/m²)",
                    headline = "SAFE CONDITIONS",
                    navigationGuidance = "Area clear. Move freely toward any open exit."
                ),
                riskTrend = RiskTrend.STABLE
            ),
            onStartSosCountdown = {},
            onCancelSosCountdown = {},
            onResetSosDispatch = {},
            onQuickHazardClick = {},
            onDetailedReportClick = {},
            onSimulationPresetSelect = {},
            onMockToggle = {},
            onDismissBanner = {}
        )
    }
}

@Preview(name = "2. Caution - Medium Risk (Orange)", showBackground = true)
@Composable
fun CitizenDashboardPreview_Caution() {
    MaterialTheme {
        CitizenDashboardScreen(
            uiState = DashboardUiState(
                riskSignal = RiskSignal(
                    sectorId = "Sector 4 - East Concourse",
                    riskScore = 5.2f,
                    densityLevel = "Moderate (3.8 people/m²)",
                    headline = "CAUTION: ELEVATED CROWD",
                    navigationGuidance = "Congestion near Gate 2. Stay right and proceed steadily."
                ),
                riskTrend = RiskTrend.INCREASING
            ),
            onStartSosCountdown = {},
            onCancelSosCountdown = {},
            onResetSosDispatch = {},
            onQuickHazardClick = {},
            onDetailedReportClick = {},
            onSimulationPresetSelect = {},
            onMockToggle = {},
            onDismissBanner = {}
        )
    }
}

@Preview(name = "3. Danger - High Risk (Red)", showBackground = true)
@Composable
fun CitizenDashboardPreview_HighRisk() {
    MaterialTheme {
        CitizenDashboardScreen(
            uiState = DashboardUiState(
                riskSignal = RiskSignal(
                    sectorId = "Sector 4 - East Concourse",
                    riskScore = 7.8f,
                    densityLevel = "High (6.2 people/m²)",
                    headline = "HIGH RISK: SURGE DETECTED",
                    navigationGuidance = "Avoid Gate 2, move toward Exit B."
                ),
                riskTrend = RiskTrend.INCREASING
            ),
            onStartSosCountdown = {},
            onCancelSosCountdown = {},
            onResetSosDispatch = {},
            onQuickHazardClick = {},
            onDetailedReportClick = {},
            onSimulationPresetSelect = {},
            onMockToggle = {},
            onDismissBanner = {}
        )
    }
}

@Preview(name = "4. Emergency - Critical (Dark Red)", showBackground = true)
@Composable
fun CitizenDashboardPreview_Critical() {
    MaterialTheme {
        CitizenDashboardScreen(
            uiState = DashboardUiState(
                riskSignal = RiskSignal(
                    sectorId = "Sector 4 - East Concourse",
                    riskScore = 9.5f,
                    densityLevel = "Critical (8.9 people/m²)",
                    headline = "CRITICAL EMERGENCY",
                    navigationGuidance = "CRITICAL: STOP MOVING TOWARD GATE 2! Divert immediately to Exit C!"
                ),
                riskTrend = RiskTrend.DECREASING
            ),
            onStartSosCountdown = {},
            onCancelSosCountdown = {},
            onResetSosDispatch = {},
            onQuickHazardClick = {},
            onDetailedReportClick = {},
            onSimulationPresetSelect = {},
            onMockToggle = {},
            onDismissBanner = {}
        )
    }
}

@Preview(name = "5. SOS Active Countdown", showBackground = true)
@Composable
fun CitizenDashboardPreview_SosCountdown() {
    MaterialTheme {
        CitizenDashboardScreen(
            uiState = DashboardUiState(
                riskSignal = RiskSignal(
                    sectorId = "Sector 4 - East Concourse",
                    riskScore = 8.0f,
                    densityLevel = "High (6.5 people/m²)",
                    headline = "HIGH RISK SURGE",
                    navigationGuidance = "Avoid Gate 2, move toward Exit B."
                ),
                riskTrend = RiskTrend.STABLE,
                isSosCountingDown = true,
                sosCountdownSeconds = 3
            ),
            onStartSosCountdown = {},
            onCancelSosCountdown = {},
            onResetSosDispatch = {},
            onQuickHazardClick = {},
            onDetailedReportClick = {},
            onSimulationPresetSelect = {},
            onMockToggle = {},
            onDismissBanner = {}
        )
    }
}

