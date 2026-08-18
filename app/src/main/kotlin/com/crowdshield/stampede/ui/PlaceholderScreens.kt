package com.crowdshield.stampede.ui

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Data model ──────────────────────────────────────────────────────────────

enum class AlertSeverity { CRITICAL, CAUTION, INFO }
enum class AlertCategory { ALL, SAFETY, ROUTE }

data class SafetyAlert(
    val id: String,
    val severity: AlertSeverity,
    val category: AlertCategory,
    val label: String,
    val headline: String,
    val detail: String?,
    val timestamp: String,
    val hasMapAction: Boolean = false
)

private val sampleAlerts = listOf(
    SafetyAlert(
        id = "1",
        severity = AlertSeverity.CRITICAL,
        category = AlertCategory.SAFETY,
        label = "IMPORTANT",
        headline = "Crowd density increasing near Sector 4",
        detail = "Avoid the area for now.",
        timestamp = "2 min ago",
        hasMapAction = true
    ),
    SafetyAlert(
        id = "2",
        severity = AlertSeverity.CAUTION,
        category = AlertCategory.ROUTE,
        label = "CAUTION",
        headline = "Exit B is becoming crowded.",
        detail = null,
        timestamp = "8 min ago",
        hasMapAction = true
    ),
    SafetyAlert(
        id = "3",
        severity = AlertSeverity.INFO,
        category = AlertCategory.SAFETY,
        label = "SAFETY UPDATE",
        headline = "Sector 3 conditions are currently stable.",
        detail = null,
        timestamp = "15 min ago",
        hasMapAction = false
    )
)

// ─── Severity colours ─────────────────────────────────────────────────────────

private fun severityColors(severity: AlertSeverity): Triple<Color, Color, Color> =
    when (severity) {
        AlertSeverity.CRITICAL -> Triple(
            SafetyThemeColors.HighRiskAccent,
            SafetyThemeColors.HighRiskContainer,
            SafetyThemeColors.HighRiskBorder
        )
        AlertSeverity.CAUTION -> Triple(
            SafetyThemeColors.CautionAccent,
            SafetyThemeColors.CautionContainer,
            SafetyThemeColors.CautionBorder
        )
        AlertSeverity.INFO -> Triple(
            SafetyThemeColors.SafeAccent,
            SafetyThemeColors.SafeContainer,
            SafetyThemeColors.SafeBorder
        )
    }

private fun severityIcon(severity: AlertSeverity): ImageVector =
    when (severity) {
        AlertSeverity.CRITICAL -> Icons.Default.Warning
        AlertSeverity.CAUTION  -> Icons.Default.WarningAmber
        AlertSeverity.INFO     -> Icons.Default.CheckCircle
    }

// ─── Main Screen ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsPlaceholderScreen() {
    var activeFilter by remember { mutableStateOf(AlertCategory.ALL) }

    val filteredAlerts = remember(activeFilter) {
        if (activeFilter == AlertCategory.ALL) sampleAlerts
        else sampleAlerts.filter { it.category == activeFilter }
    }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 2.dp, color = SafetyThemeColors.SurfaceWhite) {
                Column {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = SafetyThemeColors.TextPrimaryDark,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = "Alerts",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SafetyThemeColors.TextPrimaryDark
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = SafetyThemeColors.SurfaceWhite
                        )
                    )

                    // Location & status row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = SafetyThemeColors.TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Sector 4 • East Concourse",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = SafetyThemeColors.TextMuted
                            )
                        }

                        // Live monitoring badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(SafetyThemeColors.SafeContainer)
                                .border(1.dp, SafetyThemeColors.SafeBorder, RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(SafetyThemeColors.SafeAccent)
                            )
                            Text(
                                text = "Monitoring normally",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SafetyThemeColors.SafePrimary
                            )
                        }
                    }

                    // Filter chips
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val filters = listOf(
                            AlertCategory.ALL    to "All",
                            AlertCategory.SAFETY to "Safety",
                            AlertCategory.ROUTE  to "Route"
                        )
                        items(filters) { (cat, label) ->
                            FilterChip(
                                selected = activeFilter == cat,
                                onClick = { activeFilter = cat },
                                label = {
                                    Text(
                                        text = label,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SafetyThemeColors.TextPrimaryDark,
                                    selectedLabelColor = Color.White,
                                    containerColor = SafetyThemeColors.SurfaceWhite,
                                    labelColor = SafetyThemeColors.TextSecondaryDark
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = activeFilter == cat,
                                    selectedBorderColor = SafetyThemeColors.TextPrimaryDark,
                                    borderColor = SafetyThemeColors.CardBorder
                                )
                            )
                        }
                    }
                }
            }
        },
        containerColor = SafetyThemeColors.ScreenBackground
    ) { paddingValues ->
        if (filteredAlerts.isEmpty()) {
            AlertsEmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "${filteredAlerts.size} active alert${if (filteredAlerts.size != 1) "s" else ""}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SafetyThemeColors.TextMuted,
                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                    )
                }

                items(filteredAlerts) { alert ->
                    AlertCard(
                        alert = alert,
                        onViewMap = { /* Navigation placeholder */ }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    // Monitoring footer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = SafetyThemeColors.TextMuted.copy(alpha = 0.5f),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "CrowdShield monitoring active • Updated just now",
                            fontSize = 11.sp,
                            color = SafetyThemeColors.TextMuted.copy(alpha = 0.55f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

// ─── Alert Card ───────────────────────────────────────────────────────────────

@Composable
private fun AlertCard(
    alert: SafetyAlert,
    onViewMap: () -> Unit
) {
    val (accent, container, border) = severityColors(alert.severity)
    val icon = severityIcon(alert.severity)
    val isCritical = alert.severity == AlertSeverity.CRITICAL

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCritical) container else SafetyThemeColors.SurfaceWhite
        ),
        border = BorderStroke(
            width = if (isCritical) 1.5.dp else 1.dp,
            color = border
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCritical) 3.dp else 1.dp
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row: severity badge + timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Severity badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accent.copy(alpha = if (isCritical) 0.15f else 0.10f))
                        .padding(horizontal = 9.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(if (isCritical) 15.dp else 13.dp)
                    )
                    Text(
                        text = alert.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accent,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = alert.timestamp,
                    fontSize = 11.sp,
                    color = SafetyThemeColors.TextMuted,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Headline
            Text(
                text = alert.headline,
                fontSize = if (isCritical) 16.sp else 14.sp,
                fontWeight = if (isCritical) FontWeight.Bold else FontWeight.SemiBold,
                color = SafetyThemeColors.TextPrimaryDark,
                lineHeight = 21.sp
            )

            // Optional detail
            if (!alert.detail.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = alert.detail,
                    fontSize = 13.sp,
                    color = SafetyThemeColors.TextSecondaryDark,
                    lineHeight = 18.sp
                )
            }

            // Map action
            if (alert.hasMapAction) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = SafetyThemeColors.CardBorder, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(10.dp))

                if (isCritical) {
                    // Full-width prominent button for critical
                    Button(
                        onClick = onViewMap,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accent,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "VIEW MAP",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                } else {
                    // Subtle inline text-link for secondary alerts
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onViewMap)
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "View Map",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = accent
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─── Empty State ──────────────────────────────────────────────────────────────

@Composable
private fun AlertsEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(SafetyThemeColors.SafeContainer)
                .border(2.dp, SafetyThemeColors.SafeBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = SafetyThemeColors.SafePrimary,
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "You're all clear",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = SafetyThemeColors.TextPrimaryDark,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "No active safety alerts around your current area.",
            fontSize = 14.sp,
            color = SafetyThemeColors.TextSecondaryDark,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Last checked • just now",
            fontSize = 12.sp,
            color = SafetyThemeColors.TextMuted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(SafetyThemeColors.SafeContainer)
                .border(1.dp, SafetyThemeColors.SafeBorder, RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(SafetyThemeColors.SafeAccent)
            )
            Text(
                text = "Monitoring",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = SafetyThemeColors.SafePrimary
            )
        }
    }
}

// ─── Profile Tab Screen ────────────────────────────────────────────────────────

private val ProfileBgDark       = Color(0xFF060F1E)
private val ProfileBgMid        = Color(0xFF0A1F2F)
private val ProfileCardBg       = Color(0xFF0E1C30)
private val ProfileCardBorder   = Color(0x28FFFFFF)
private val ProfileTextPrimary  = Color(0xFFE8F4F8)
private val ProfileTextSecondary= Color(0x99E8F4F8)
private val ProfileAccentGreen  = Color(0xFF00E5A0)
private val ProfileAccentBlue   = Color(0xFF1E8FFF)
private val ProfileAccentRed    = Color(0xFFFF4757)
private val ProfileAccentGold   = Color(0xFFFFBB44)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePlaceholderScreen(
    onSignOut: () -> Unit = {}
) {
    var locationSharingEnabled by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ProfileBgDark, ProfileBgMid, ProfileBgDark)))
    ) {
        // Dynamic subtle background canvas (grid + crowd nodes)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val gridStep = 48.dp.toPx()
            val gridColor = Color(0xFF1E8FFF).copy(alpha = 0.04f)

            var x = 0f
            while (x < w) {
                drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 0.6f)
                x += gridStep
            }
            var y = 0f
            while (y < h) {
                drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 0.6f)
                y += gridStep
            }

            // Subtle node points
            val nodes = listOf(
                Offset(w * 0.15f, h * 0.12f),
                Offset(w * 0.85f, h * 0.22f),
                Offset(w * 0.2f, h * 0.55f),
                Offset(w * 0.8f, h * 0.65f),
                Offset(w * 0.5f, h * 0.88f)
            )
            for (node in nodes) {
                drawCircle(
                    color = ProfileAccentGreen.copy(alpha = 0.12f),
                    radius = 16.dp.toPx(),
                    center = node
                )
                drawCircle(
                    color = ProfileAccentGreen.copy(alpha = 0.35f),
                    radius = 3.dp.toPx(),
                    center = node
                )
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = ProfileTextPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Profile",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = ProfileTextPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Subtitle
                Text(
                    text = "Manage your safety, preferences and account settings",
                    fontSize = 13.sp,
                    color = ProfileTextSecondary,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                // ── Profile Card ───────────────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ProfileCardBg),
                    border = BorderStroke(1.dp, ProfileCardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(ProfileAccentBlue.copy(alpha = 0.15f))
                                    .border(1.5.dp, ProfileAccentBlue.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Avatar",
                                    tint = ProfileAccentBlue,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            // User Info
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "User Name",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ProfileTextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "+91 XXXXX XXXXX",
                                    fontSize = 13.sp,
                                    color = ProfileTextSecondary
                                )
                            }
                        }

                        // Status Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF002B1A))
                                .border(1.dp, ProfileAccentGreen.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(ProfileAccentGreen)
                            )
                            Text(
                                text = "Safety monitoring active",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ProfileAccentGreen
                            )
                        }
                    }
                }

                // ── Safety & Location Section ───────────────────────────────────
                ProfileSectionHeader(title = "SAFETY & LOCATION")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ProfileCardBg),
                    border = BorderStroke(1.dp, ProfileCardBorder)
                ) {
                    Column {
                        // Current Location
                        ProfileItemRow(
                            icon = Icons.Default.LocationOn,
                            iconColor = ProfileAccentGreen,
                            title = "Current Location",
                            subtitle = "Sector 4 • East Concourse",
                            trailingContent = {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(ProfileAccentGreen.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "LIVE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ProfileAccentGreen
                                    )
                                }
                            }
                        )

                        HorizontalDivider(color = ProfileCardBorder, thickness = 0.5.dp)

                        // Alert Preferences
                        ProfileItemRow(
                            icon = Icons.Default.Notifications,
                            iconColor = ProfileAccentBlue,
                            title = "Alert Preferences",
                            subtitle = "Choose what alerts you want to receive",
                            onClick = {}
                        )

                        HorizontalDivider(color = ProfileCardBorder, thickness = 0.5.dp)

                        // Emergency Notifications
                        ProfileItemRow(
                            icon = Icons.Default.Warning,
                            iconColor = ProfileAccentGold,
                            title = "Emergency Notifications",
                            subtitle = "Critical alerts & emergency messages",
                            onClick = {}
                        )

                        HorizontalDivider(color = ProfileCardBorder, thickness = 0.5.dp)

                        // Preferred Exits & Routes
                        ProfileItemRow(
                            icon = Icons.Default.Map,
                            iconColor = ProfileAccentBlue,
                            title = "Preferred Exits & Routes",
                            subtitle = "Set preferred exits for quick navigation",
                            onClick = {}
                        )

                        HorizontalDivider(color = ProfileCardBorder, thickness = 0.5.dp)

                        // Location Sharing (Toggle)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ProfileAccentGreen.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = ProfileAccentGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Location Sharing",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ProfileTextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Helps us send location-relevant alerts",
                                    fontSize = 12.sp,
                                    color = ProfileTextSecondary
                                )
                            }
                            androidx.compose.material3.Switch(
                                checked = locationSharingEnabled,
                                onCheckedChange = { locationSharingEnabled = it },
                                colors = androidx.compose.material3.SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF060F1E),
                                    checkedTrackColor = ProfileAccentGreen,
                                    uncheckedThumbColor = ProfileTextSecondary,
                                    uncheckedTrackColor = ProfileCardBg
                                )
                            )
                        }
                    }
                }

                // ── Emergency Section ───────────────────────────────────────────
                ProfileSectionHeader(title = "EMERGENCY")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ProfileCardBg),
                    border = BorderStroke(1.dp, ProfileCardBorder)
                ) {
                    Column {
                        // Emergency Contact
                        ProfileItemRow(
                            icon = Icons.Default.Phone,
                            iconColor = ProfileAccentRed,
                            title = "Emergency Contact",
                            subtitle = "Add or update your emergency contact",
                            onClick = {}
                        )

                        HorizontalDivider(color = ProfileCardBorder, thickness = 0.5.dp)

                        // Emergency Assistance
                        ProfileItemRow(
                            icon = Icons.Default.Shield,
                            iconColor = ProfileAccentRed,
                            title = "Emergency Assistance",
                            subtitle = "Quick access to help when needed",
                            onClick = {}
                        )
                    }
                }

                // ── App Settings Section ────────────────────────────────────────
                ProfileSectionHeader(title = "APP SETTINGS")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ProfileCardBg),
                    border = BorderStroke(1.dp, ProfileCardBorder)
                ) {
                    Column {
                        // Language
                        ProfileItemRow(
                            icon = Icons.Default.Info,
                            iconColor = ProfileAccentBlue,
                            title = "Language",
                            subtitle = null,
                            trailingContent = {
                                Text(
                                    text = "English",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ProfileAccentBlue
                                )
                            },
                            onClick = {}
                        )

                        HorizontalDivider(color = ProfileCardBorder, thickness = 0.5.dp)

                        // Notification Settings
                        ProfileItemRow(
                            icon = Icons.Default.NotificationsActive,
                            iconColor = ProfileAccentGold,
                            title = "Notification Settings",
                            subtitle = "Manage app notification preferences",
                            onClick = {}
                        )

                        HorizontalDivider(color = ProfileCardBorder, thickness = 0.5.dp)

                        // About CrowdShield
                        ProfileItemRow(
                            icon = Icons.Default.Info,
                            iconColor = ProfileTextSecondary,
                            title = "About CrowdShield",
                            subtitle = "Version 1.0.0",
                            trailingContent = {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0x1FFFFFFF))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "v1.0.0",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = ProfileTextSecondary
                                    )
                                }
                            },
                            onClick = {}
                        )
                    }
                }

                // ── Bottom / Account Actions ────────────────────────────────────
                Spacer(modifier = Modifier.height(4.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ProfileCardBg),
                    border = BorderStroke(1.dp, ProfileCardBorder)
                ) {
                    Column {
                        // Privacy & Safety
                        ProfileItemRow(
                            icon = Icons.Default.Security,
                            iconColor = ProfileAccentGreen,
                            title = "Privacy & Safety",
                            subtitle = "Data encryption & anonymous mesh telemetry",
                            onClick = {}
                        )

                        HorizontalDivider(color = ProfileCardBorder, thickness = 0.5.dp)

                        // Sign Out
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onSignOut)
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ProfileAccentRed.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Sign Out",
                                    tint = ProfileAccentRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "Sign Out",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ProfileAccentRed,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = ProfileAccentRed.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Footer
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = ProfileTextSecondary.copy(alpha = 0.4f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CrowdShield · Stampede Prevention Mesh",
                        fontSize = 11.sp,
                        color = ProfileTextSecondary.copy(alpha = 0.45f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ProfileSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        color = ProfileTextSecondary,
        letterSpacing = 1.4.sp,
        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
    )
}

@Composable
private fun ProfileItemRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String?,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColor.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = ProfileTextPrimary
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = ProfileTextSecondary,
                    lineHeight = 16.sp
                )
            }
        }

        if (trailingContent != null) {
            trailingContent()
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = ProfileTextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

