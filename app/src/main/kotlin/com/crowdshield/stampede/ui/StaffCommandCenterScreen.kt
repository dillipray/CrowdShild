package com.crowdshield.stampede.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel

private val CmdDark   = Color(0xFF060F1E)
private val CmdMid    = Color(0xFF0A1F2F)
private val CmdGreen  = Color(0xFF00E5A0)
private val CmdBlue   = Color(0xFF1E8FFF)
private val CmdGold   = Color(0xFFFFBB44)
private val CmdRed    = Color(0xFFFF4757)
private val CmdSurface = Color(0x18FFFFFF)
private val CmdBorder  = Color(0x28FFFFFF)
private val CmdText    = Color(0xFFE8F4F8)
private val CmdMuted   = Color(0x99E8F4F8)

data class CommandFeature(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val accentColor: Color,
    val statusLabel: String,
    val isActionable: Boolean = false
)

private val commandFeatures = listOf(
    CommandFeature(
        icon = Icons.Default.MonitorHeart,
        title = "Live Monitoring",
        description = "Real-time crowd density and sensor telemetry across all zones",
        accentColor = CmdGreen,
        statusLabel = "ACTIVE"
    ),
    CommandFeature(
        icon = Icons.Default.CloudUpload,
        title = "Video Analysis",
        description = "Upload and analyze video feeds using OpenCV & YOLO engines",
        accentColor = Color(0xFFE67E22),
        statusLabel = "UPLOAD",
        isActionable = true
    ),
    CommandFeature(
        icon = Icons.Default.Map,
        title = "Command Map",
        description = "Tactical situational awareness map with sector risk overlays",
        accentColor = CmdBlue,
        statusLabel = "LIVE"
    ),
    CommandFeature(
        icon = Icons.Default.CameraAlt,
        title = "CCTV Analysis",
        description = "Automated video feed analysis across all camera nodes",
        accentColor = Color(0xFF9B59B6),
        statusLabel = "PROCESSING"
    ),
    CommandFeature(
        icon = Icons.Default.CameraAlt,
        title = "Crowd Image Analysis",
        description = "AI-assisted crowd density estimation from aerial imagery",
        accentColor = Color(0xFFFF6B35),
        statusLabel = "READY"
    ),
    CommandFeature(
        icon = Icons.Default.Warning,
        title = "Incidents",
        description = "Active incident reports, field dispatches, and SOS alerts",
        accentColor = CmdRed,
        statusLabel = "0 ACTIVE"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffCommandCenterScreen(
    viewModel: StaffCommandCenterViewModel = hiltViewModel(),
    onLogout: () -> Unit
) {
    val uploadState by viewModel.uploadState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.uploadVideo(it) }
    }

    LaunchedEffect(uploadState) {
        when (val state = uploadState) {
            is UploadState.Success -> {
                snackbarHostState.showSnackbar(
                    "Analysis Complete: ${state.result.totalHumansDetected} humans detected. Risk: ${state.result.riskScore}"
                )
                viewModel.resetUploadState()
            }
            is UploadState.Error -> {
                snackbarHostState.showSnackbar("Upload Failed: ${state.message}")
                viewModel.resetUploadState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = CmdGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "CrowdShield",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = CmdText
                            )
                            Text(
                                text = "Command Center",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = CmdGold,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = CmdMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CmdDark
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = CmdDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── System Status Banner ───────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF002B1A))
                    .border(1.dp, CmdGreen.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(CmdGreen)
                    )
                    Column {
                        Text(
                            text = "System Operational",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = CmdGreen
                        )
                        Text(
                            text = "All sensors reporting nominal — no active emergencies",
                            fontSize = 12.sp,
                            color = CmdGreen.copy(alpha = 0.65f)
                        )
                    }
                }
            }

            // ── Section Header ─────────────────────────────────────────────────
            Text(
                text = "QUICK ACCESS",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CmdMuted,
                letterSpacing = 1.6.sp,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )

            // ── Feature Cards ──────────────────────────────────────────────────
            commandFeatures.forEach { feature ->
                CommandFeatureCard(
                    feature = feature,
                    isLoading = feature.isActionable && uploadState is UploadState.Uploading,
                    onClick = {
                        if (feature.isActionable) {
                            videoPickerLauncher.launch("video/*")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Authority Badge Footer ─────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = CmdGold.copy(alpha = 0.4f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Authorized Staff Access Only  ·  CrowdShield v1.0",
                    fontSize = 11.sp,
                    color = CmdMuted.copy(alpha = 0.45f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CommandFeatureCard(
    feature: CommandFeature,
    isLoading: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLoading) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1C30)),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = feature.accentColor.copy(alpha = 0.22f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(feature.accentColor.copy(alpha = 0.14f))
                    .border(1.dp, feature.accentColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = feature.accentColor,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = feature.icon,
                        contentDescription = null,
                        tint = feature.accentColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = feature.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = CmdText
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = feature.description,
                    fontSize = 12.sp,
                    color = CmdMuted,
                    lineHeight = 17.sp
                )
            }

            // Status Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(feature.accentColor.copy(alpha = 0.16f))
                    .border(1.dp, feature.accentColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isLoading) "ANALYZING" else feature.statusLabel,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = feature.accentColor,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}
