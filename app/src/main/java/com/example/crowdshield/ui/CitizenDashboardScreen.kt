package com.example.crowdshield.ui

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsRun
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.crowdshield.domain.RiskSeverity
import com.example.crowdshield.domain.RiskSignal
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Senior Citizen Accessibility High-Contrast Color Palette.
 */
object CitizenThemeColors {
    // Low (Green)
    val GreenPrimary = Color(0xFF1B5E20)
    val GreenAccent = Color(0xFF2E7D32)
    val GreenContainer = Color(0xFFE8F5E9)
    val GreenBorder = Color(0xFF81C784)
    val GreenText = Color(0xFF0A3D0A)

    // Medium (Orange/Amber)
    val OrangePrimary = Color(0xFFE65100)
    val OrangeAccent = Color(0xFFF57C00)
    val OrangeContainer = Color(0xFFFFF3E0)
    val OrangeBorder = Color(0xFFFFB74D)
    val OrangeText = Color(0xFFBF360C)

    // High (Red)
    val RedPrimary = Color(0xFFB71C1C)
    val RedAccent = Color(0xFFD32F2F)
    val RedContainer = Color(0xFFFFEBEE)
    val RedBorder = Color(0xFFE57373)
    val RedText = Color(0xFF7F0000)

    // Critical (Dark Red / Crimson)
    val DarkRedPrimary = Color(0xFF880E4F)
    val DarkRedAccent = Color(0xFF800000)
    val DarkRedContainer = Color(0xFFFFCDD2)
    val DarkRedBorder = Color(0xFFB71C1C)
    val DarkRedText = Color(0xFF4A0000)

    // Neutrals
    val Background = Color(0xFFF4F6F9)
    val CardWhite = Color(0xFFFFFFFF)
    val TextMain = Color(0xFF111827)
    val TextMuted = Color(0xFF4B5563)
}

data class SeverityStyle(
    val primary: Color,
    val container: Color,
    val border: Color,
    val text: Color,
    val icon: ImageVector,
    val label: String
)

fun getSeverityStyle(severity: RiskSeverity): SeverityStyle = when (severity) {
    RiskSeverity.LOW -> SeverityStyle(
        primary = CitizenThemeColors.GreenAccent,
        container = CitizenThemeColors.GreenContainer,
        border = CitizenThemeColors.GreenBorder,
        text = CitizenThemeColors.GreenText,
        icon = Icons.Default.CheckCircle,
        label = "LOW RISK - SAFE"
    )
    RiskSeverity.MEDIUM -> SeverityStyle(
        primary = CitizenThemeColors.OrangeAccent,
        container = CitizenThemeColors.OrangeContainer,
        border = CitizenThemeColors.OrangeBorder,
        text = CitizenThemeColors.OrangeText,
        icon = Icons.Default.WarningAmber,
        label = "MEDIUM - CAUTION"
    )
    RiskSeverity.HIGH -> SeverityStyle(
        primary = CitizenThemeColors.RedAccent,
        container = CitizenThemeColors.RedContainer,
        border = CitizenThemeColors.RedBorder,
        text = CitizenThemeColors.RedText,
        icon = Icons.Default.Warning,
        label = "HIGH RISK - DANGER"
    )
    RiskSeverity.CRITICAL -> SeverityStyle(
        primary = CitizenThemeColors.DarkRedPrimary,
        container = CitizenThemeColors.DarkRedContainer,
        border = CitizenThemeColors.DarkRedBorder,
        text = CitizenThemeColors.DarkRedText,
        icon = Icons.Default.Emergency,
        label = "CRITICAL EMERGENCY"
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenDashboardScreen(
    riskSignal: RiskSignal,
    isSosCountingDown: Boolean = false,
    sosCountdownSeconds: Int = 0,
    isSosDispatched: Boolean = false,
    bannerMessage: String? = null,
    onStartSosCountdown: () -> Unit = {},
    onCancelSosCountdown: () -> Unit = {},
    onResetSosDispatch: () -> Unit = {},
    onQuickHazardClick: (String) -> Unit = {},
    onDetailedReportClick: () -> Unit = {},
    onSimulationPresetSelect: (RiskSeverity) -> Unit = {},
    onMockToggle: (Boolean) -> Unit = {},
    onDismissBanner: () -> Unit = {}
) {
    val severity = RiskSeverity.fromScore(riskSignal.riskScore)
    val style = getSeverityStyle(severity)

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
                                .background(style.primary)
                        )
                        Text(
                            text = "CrowdShield",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = CitizenThemeColors.TextMain
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onDetailedReportClick) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Custom Report",
                            tint = CitizenThemeColors.TextMain,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CitizenThemeColors.CardWhite
                )
            )
        },
        containerColor = CitizenThemeColors.Background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Status Feedback Banner (if any)
            item {
                AnimatedVisibility(
                    visible = bannerMessage != null,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    bannerMessage?.let { msg ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = msg,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = onDismissBanner,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Sector Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CitizenThemeColors.CardWhite),
                    border = BorderStroke(1.5.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
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
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0F2FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "LOCATION SECTOR",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CitizenThemeColors.TextMuted,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = riskSignal.sectorId,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CitizenThemeColors.TextMain
                                )
                            }
                        }

                        // Live pulse
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                            val pulseScale by infiniteTransition.animateFloat(
                                initialValue = 0.8f,
                                targetValue = 1.2f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(800, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulseScale"
                            )
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(Color(0xFF16A34A))
                            )
                            Text(
                                text = "LIVE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF16A34A)
                            )
                        }
                    }
                }
            }

            // 3. Actionable Navigation Guidance Banner (Typography >= 20sp for Senior Accessibility)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = style.container),
                    border = BorderStroke(2.5.dp, style.border)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .background(style.primary, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = style.icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = riskSignal.headline.uppercase(),
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsRun,
                                contentDescription = null,
                                tint = style.text,
                                modifier = Modifier
                                    .size(34.dp)
                                    .padding(top = 2.dp)
                            )
                            Text(
                                text = riskSignal.navigationGuidance,
                                fontSize = 23.sp,
                                lineHeight = 30.sp,
                                fontWeight = FontWeight.Black,
                                color = style.text
                            )
                        }
                    }
                }
            }

            // 4. Risk Score & Density Assessment Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CitizenThemeColors.CardWhite),
                    border = BorderStroke(1.5.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "CROWD RISK ASSESSMENT",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CitizenThemeColors.TextMuted,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = String.format("%.1f", riskSignal.riskScore),
                                        fontSize = 44.sp,
                                        fontWeight = FontWeight.Black,
                                        color = style.primary
                                    )
                                    Text(
                                        text = " / 10.0",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CitizenThemeColors.TextMuted,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }
                                Text(
                                    text = "0.0 (Safe) to 10.0 (Critical)",
                                    fontSize = 12.sp,
                                    color = CitizenThemeColors.TextMuted
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(style.primary)
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = style.label,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val progressColor by animateColorAsState(
                            targetValue = style.primary,
                            label = "riskProgress"
                        )
                        LinearProgressIndicator(
                            progress = { (riskSignal.riskScore / 10f).coerceIn(0.05f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                                .clip(RoundedCornerShape(7.dp)),
                            color = progressColor,
                            trackColor = Color(0xFFE2E8F0),
                            strokeCap = StrokeCap.Round
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.5.dp)
                        Spacer(modifier = Modifier.height(12.dp))

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
                                    imageVector = Icons.Default.People,
                                    contentDescription = "Density",
                                    tint = CitizenThemeColors.TextMuted,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = "Crowd Density Level:",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CitizenThemeColors.TextMuted
                                )
                            }
                            Text(
                                text = riskSignal.densityLevel,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = style.primary
                            )
                        }
                    }
                }
            }

            // 5. SOS Emergency Trigger (5-Second Safeguard Countdown)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(22.dp)),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isSosDispatched -> Color(0xFF1E293B)
                            isSosCountingDown -> CitizenThemeColors.RedPrimary
                            else -> CitizenThemeColors.CardWhite
                        }
                    ),
                    border = BorderStroke(
                        3.dp,
                        when {
                            isSosDispatched -> Color(0xFF0EA5E9)
                            isSosCountingDown -> Color.White
                            else -> CitizenThemeColors.RedAccent
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when {
                            isSosDispatched -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Text(
                                        text = "SOS DISPATCHED!",
                                        color = Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Emergency teams alerted with your GPS location.",
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 15.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = onResetSosDispatch,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                                ) {
                                    Text("Reset SOS", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                            isSosCountingDown -> {
                                Text(
                                    text = "TAP BUTTON BELOW TO CANCEL",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CircularProgressIndicator(
                                        progress = { sosCountdownSeconds / 5f },
                                        modifier = Modifier.size(36.dp),
                                        color = Color.White,
                                        strokeWidth = 4.dp
                                    )
                                    Text(
                                        text = "DISPATCHING IN $sosCountdownSeconds s",
                                        color = Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = onCancelSosCountdown,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cancel",
                                        tint = CitizenThemeColors.RedPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "CANCEL EMERGENCY SOS",
                                        color = CitizenThemeColors.RedPrimary,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                            else -> {
                                Button(
                                    onClick = onStartSosCountdown,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(78.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CitizenThemeColors.RedAccent),
                                    shape = RoundedCornerShape(18.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Emergency,
                                            contentDescription = "SOS",
                                            tint = Color.White,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(horizontalAlignment = Alignment.Start) {
                                            Text(
                                                text = "SOS EMERGENCY",
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Tap to start 5-second safety countdown",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color.White.copy(alpha = 0.9f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. Rapid Hazard Reporting Action Grid
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CitizenThemeColors.CardWhite),
                    border = BorderStroke(1.5.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
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
                                color = CitizenThemeColors.TextMuted,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "1-Tap Dispatch",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                HazardButton(
                                    title = "Blocked Exit",
                                    subtitle = "Locked / Closed",
                                    icon = Icons.Default.DoorBack,
                                    accent = Color(0xFFDC2626),
                                    modifier = Modifier.weight(1f),
                                    onClick = { onQuickHazardClick("Blocked Exit") }
                                )
                                HazardButton(
                                    title = "Medical Help",
                                    subtitle = "Injury / Collapsed",
                                    icon = Icons.Default.MedicalServices,
                                    accent = Color(0xFFE11D48),
                                    modifier = Modifier.weight(1f),
                                    onClick = { onQuickHazardClick("Medical Emergency") }
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                HazardButton(
                                    title = "Fire / Smoke",
                                    subtitle = "Spotted Hazard",
                                    icon = Icons.Default.LocalFireDepartment,
                                    accent = Color(0xFFEA580C),
                                    modifier = Modifier.weight(1f),
                                    onClick = { onQuickHazardClick("Fire / Smoke") }
                                )
                                HazardButton(
                                    title = "Extreme Surge",
                                    subtitle = "Crush Hazard",
                                    icon = Icons.Default.People,
                                    accent = Color(0xFF7C3AED),
                                    modifier = Modifier.weight(1f),
                                    onClick = { onQuickHazardClick("Extreme Crowd Surge") }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = onDetailedReportClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, Color(0xFF94A3B8))
                        ) {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = null,
                                tint = CitizenThemeColors.TextMain
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Submit Detailed Incident Report",
                                color = CitizenThemeColors.TextMain,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            // 7. Simulation / Testing Strip
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CitizenThemeColors.CardWhite),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Test 4 Color Severity Tiers:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CitizenThemeColors.TextMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            RiskSeverity.entries.forEach { s ->
                                val isSelected = severity == s
                                val chipStyle = getSeverityStyle(s)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSimulationPresetSelect(s) },
                                    label = {
                                        Text(
                                            text = when (s) {
                                                RiskSeverity.LOW -> "Low"
                                                RiskSeverity.MEDIUM -> "Medium"
                                                RiskSeverity.HIGH -> "High"
                                                RiskSeverity.CRITICAL -> "Critical"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = chipStyle.primary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun HazardButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(86.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.08f)),
        border = BorderStroke(1.5.dp, accent.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CitizenThemeColors.TextMain,
                    lineHeight = 18.sp
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = CitizenThemeColors.TextMuted
                )
            }
        }
    }
}

// -------------------------------------------------------------------------
// Previews
// -------------------------------------------------------------------------

@Preview(name = "Low Risk - Safe (Green)", showBackground = true)
@Composable
fun Preview_LowRisk() {
    MaterialTheme {
        CitizenDashboardScreen(
            riskSignal = RiskSignal(
                sectorId = "Sector 4 - East Concourse",
                riskScore = 1.5f,
                densityLevel = "Low (1.2 people/m²)",
                headline = "SAFE CONDITIONS",
                navigationGuidance = "Area clear. Move freely toward any exit."
            )
        )
    }
}

@Preview(name = "Medium Risk - Caution (Orange)", showBackground = true)
@Composable
fun Preview_MediumRisk() {
    MaterialTheme {
        CitizenDashboardScreen(
            riskSignal = RiskSignal(
                sectorId = "Sector 4 - East Concourse",
                riskScore = 5.2f,
                densityLevel = "Moderate (3.8 people/m²)",
                headline = "CAUTION: ELEVATED CROWD",
                navigationGuidance = "Congestion forming near Gate 2. Stay to the right."
            )
        )
    }
}

@Preview(name = "High Risk - Danger (Red)", showBackground = true)
@Composable
fun Preview_HighRisk() {
    MaterialTheme {
        CitizenDashboardScreen(
            riskSignal = RiskSignal(
                sectorId = "Sector 4 - East Concourse",
                riskScore = 7.8f,
                densityLevel = "High (6.2 people/m²)",
                headline = "HIGH RISK: SURGE DETECTED",
                navigationGuidance = "HIGH RISK: Avoid Gate 2, move toward Exit B."
            )
        )
    }
}

@Preview(name = "Critical Risk - Emergency (Dark Red)", showBackground = true)
@Composable
fun Preview_CriticalRisk() {
    MaterialTheme {
        CitizenDashboardScreen(
            riskSignal = RiskSignal(
                sectorId = "Sector 4 - East Concourse",
                riskScore = 9.5f,
                densityLevel = "Critical (8.9 people/m²)",
                headline = "CRITICAL EMERGENCY: CRUSH HAZARD",
                navigationGuidance = "CRITICAL: STOP MOVING TOWARD GATE 2! Divert immediately to Exit C!"
            )
        )
    }
}
