package com.crowdshield.stampede.ui

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.crowdshield.stampede.domain.RiskLevel
import com.crowdshield.stampede.service.CrowdMonitorService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private var crowdService: CrowdMonitorService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as CrowdMonitorService.LocalBinder
            crowdService = binder.getService()
            isBound = true
            observeService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        requestPermissions()
        startMonitoringService()

        setContent {
            val navController = rememberNavController()
            val uiState by viewModel.uiState.collectAsState()

            NavHost(navController = navController, startDestination = "dashboard") {
                composable("dashboard") {
                    DashboardScreen(
                        uiState = uiState,
                        onMockToggle = { enabled ->
                            viewModel.toggleMock(enabled)
                            crowdService?.toggleMockData(enabled)
                        },
                        onEmergencyClick = {
                            viewModel.triggerEmergency()
                        },
                        onReportClick = {
                            navController.navigate("report")
                        }
                    )
                }
                composable("report") {
                    IncidentReportScreen(
                        onSubmit = { description ->
                            viewModel.submitIncidentReport(description) {
                                navController.popBackStack()
                            }
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }

    private fun observeService() {
        lifecycleScope.launch {
            crowdService?.currentRisk?.collectLatest { risk ->
                risk?.let { viewModel.updateRisk(it) }
            }
        }
    }

    private fun startMonitoringService() {
        val intent = Intent(this, CrowdMonitorService::class.java)
        startForegroundService(intent)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 100)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }
}

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onMockToggle: (Boolean) -> Unit,
    onEmergencyClick: () -> Unit,
    onReportClick: () -> Unit = {}
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onReportClick) {
                Icon(Icons.Default.Add, contentDescription = "Report Incident")
            }
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "CROWD STAMPEDE",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                RiskIndicator(uiState.currentRisk.level, uiState.currentRisk.score)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                MockMapView(uiState.currentRisk.score)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                MockDataControl(uiState.isMockEnabled, onMockToggle)
                
                Spacer(modifier = Modifier.weight(1f))
                
                EmergencyButtonWithCountdown(onEmergencyClick)
            }
        }
    }
}

@Composable
fun RiskIndicator(level: RiskLevel, score: Float) {
    val color = when (level) {
        RiskLevel.SAFE -> Color(0xFF388E3C)
        RiskLevel.CAUTION -> Color(0xFFFBC02D)
        RiskLevel.HIGH_RISK -> Color(0xFFD32F2F)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = level.name,
                    style = MaterialTheme.typography.headlineLarge,
                    color = color,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Risk Score: ${"%.1f".format(score)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun MockMapView(riskScore: Float) {
    Text("Crowd Density Map (Mock)", style = MaterialTheme.typography.labelLarge)
    Canvas(modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1.5f)
        .background(Color.DarkGray.copy(alpha = 0.1f))
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        
        // Draw some static "clusters"
        drawCircle(
            color = Color.Blue.copy(alpha = 0.3f),
            radius = 20.dp.toPx(),
            center = Offset(size.width * 0.3f, size.height * 0.4f)
        )
        
        // Main cluster grows and reddens with risk score
        val intensity = (riskScore / 10f)
        drawCircle(
            color = Color.Red.copy(alpha = 0.2f + intensity * 0.6f),
            radius = (30.dp + (60.dp * intensity)).toPx(),
            center = center
        )
        
        // Draw user position
        drawCircle(
            color = Color.White,
            radius = 6.dp.toPx(),
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
fun MockDataControl(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color.LightGray.copy(alpha = 0.2f), shape = CircleShape)
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text("Simulation Mode", modifier = Modifier.weight(1f))
        Switch(checked = enabled, onCheckedChange = onToggle)
    }
}

@Composable
fun EmergencyButtonWithCountdown(onEmergencyClick: () -> Unit) {
    var countdown by remember { mutableStateOf(0) }
    val isCounting = countdown > 0

    LaunchedEffect(isCounting) {
        if (isCounting) {
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
            if (isCounting) onEmergencyClick()
        }
    }

    Button(
        onClick = { if (!isCounting) countdown = 5 else countdown = 0 },
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isCounting) Color.Gray else Color(0xFFD32F2F)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = if (isCounting) "CANCEL SOS ($countdown)" else "EMERGENCY SOS",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
    }
}
