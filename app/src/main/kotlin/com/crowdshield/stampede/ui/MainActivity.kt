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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.crowdshield.stampede.service.CrowdMonitorService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
            MaterialTheme {
                val navController = rememberNavController()
                val uiState by viewModel.uiState.collectAsState()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val isBottomBarVisible = currentRoute in listOf("dashboard", "map", "alerts", "profile")


                Scaffold(
                    bottomBar = {
                        if (isBottomBarVisible) {
                            CrowdShieldBottomNav(
                                currentRoute = currentRoute ?: "dashboard",
                                onNavigate = { targetRoute ->
                                    navController.navigate(targetRoute) {
                                        popUpTo("dashboard") {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login") {
                            LoginScreen(
                                onPublicLogin = {
                                    navController.navigate("dashboard") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onStaffLogin = {
                                    navController.navigate("command_center") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("command_center") {
                            StaffCommandCenterScreen(
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("dashboard") {
                            CitizenDashboardScreen(
                                uiState = uiState,
                                onStartSosCountdown = {
                                    viewModel.startSosCountdown()
                                },
                                onCancelSosCountdown = {
                                    viewModel.cancelSosCountdown()
                                },
                                onResetSosDispatch = {
                                    viewModel.resetSosDispatch()
                                },
                                onQuickHazardClick = { hazardType ->
                                    viewModel.submitQuickHazard(hazardType)
                                },
                                onDetailedReportClick = {
                                    navController.navigate("report")
                                },
                                onSimulationPresetSelect = { severity ->
                                    viewModel.setSimulationPreset(severity)
                                },
                                onMockToggle = { enabled ->
                                    viewModel.toggleMock(enabled)
                                    crowdService?.toggleMockData(enabled)
                                },
                                onDismissBanner = {
                                    viewModel.dismissBannerMessage()
                                }
                            )
                        }
                        composable("map") {
                            LiveCrowdMapScreen(
                                onBack = {
                                    navController.navigate("dashboard") {
                                        popUpTo("dashboard") { inclusive = false }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                        composable("alerts") {
                            AlertsPlaceholderScreen()
                        }
                        composable("profile") {
                            ProfilePlaceholderScreen(
                                onSignOut = {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
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
