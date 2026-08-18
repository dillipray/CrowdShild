package com.example.crowdshield

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.crowdshield.domain.RiskSeverity
import com.example.crowdshield.domain.RiskSignal
import com.example.crowdshield.ui.CitizenDashboardScreen
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Preview Activity for Citizen Dashboard UI (com.example.crowdshield).
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                CitizenDashboardInteractiveHost()
            }
        }
    }
}

@Composable
fun CitizenDashboardInteractiveHost() {
    var riskSignal by remember {
        mutableStateOf(
            RiskSignal(
                sectorId = "Sector 4 - East Concourse",
                riskScore = 7.8f,
                densityLevel = "High (6.2 people/m²)",
                headline = "HIGH SURGE DETECTED",
                navigationGuidance = "HIGH RISK: Avoid Gate 2, move toward Exit B."
            )
        )
    }

    var isSosCountingDown by remember { mutableStateOf(false) }
    var sosCountdownSeconds by remember { mutableIntStateOf(0) }
    var isSosDispatched by remember { mutableStateOf(false) }
    var bannerMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    var countdownJob by remember { mutableStateOf<Job?>(null) }

    CitizenDashboardScreen(
        riskSignal = riskSignal,
        isSosCountingDown = isSosCountingDown,
        sosCountdownSeconds = sosCountdownSeconds,
        isSosDispatched = isSosDispatched,
        bannerMessage = bannerMessage,
        onStartSosCountdown = {
            if (!isSosCountingDown && !isSosDispatched) {
                isSosCountingDown = true
                sosCountdownSeconds = 5
                countdownJob?.cancel()
                countdownJob = coroutineScope.launch {
                    for (sec in 5 downTo 1) {
                        sosCountdownSeconds = sec
                        delay(1000)
                    }
                    isSosCountingDown = false
                    sosCountdownSeconds = 0
                    isSosDispatched = true
                    bannerMessage = "🚨 SOS Dispatched! Emergency response alerted."
                }
            }
        },
        onCancelSosCountdown = {
            countdownJob?.cancel()
            countdownJob = null
            isSosCountingDown = false
            sosCountdownSeconds = 0
        },
        onResetSosDispatch = {
            countdownJob?.cancel()
            countdownJob = null
            isSosCountingDown = false
            sosCountdownSeconds = 0
            isSosDispatched = false
            bannerMessage = null
        },
        onQuickHazardClick = { hazardType ->
            bannerMessage = "✅ Reported: $hazardType. Field team notified."
        },
        onDetailedReportClick = {},
        onSimulationPresetSelect = { severity ->
            riskSignal = when (severity) {
                RiskSeverity.LOW -> RiskSignal(
                    sectorId = "Sector 4 - East Concourse",
                    riskScore = 1.5f,
                    densityLevel = "Low (1.2 people/m²)",
                    headline = "SAFE CONDITIONS",
                    navigationGuidance = "Area clear. Move freely toward any exit."
                )
                RiskSeverity.MEDIUM -> RiskSignal(
                    sectorId = "Sector 4 - East Concourse",
                    riskScore = 5.2f,
                    densityLevel = "Moderate (3.8 people/m²)",
                    headline = "CAUTION: ELEVATED CROWD",
                    navigationGuidance = "Congestion forming near Gate 2. Stay right and proceed steadily."
                )
                RiskSeverity.HIGH -> RiskSignal(
                    sectorId = "Sector 4 - East Concourse",
                    riskScore = 7.8f,
                    densityLevel = "High (6.2 people/m²)",
                    headline = "HIGH RISK: SURGE DETECTED",
                    navigationGuidance = "HIGH RISK: Avoid Gate 2, move toward Exit B."
                )
                RiskSeverity.CRITICAL -> RiskSignal(
                    sectorId = "Sector 4 - East Concourse",
                    riskScore = 9.5f,
                    densityLevel = "Critical (8.9 people/m²)",
                    headline = "CRITICAL EMERGENCY: CRUSH HAZARD",
                    navigationGuidance = "CRITICAL: STOP MOVING TOWARD GATE 2! Divert immediately to Exit C!"
                )
            }
        },
        onDismissBanner = {
            bannerMessage = null
        }
    )
}

@Preview(showBackground = true)
@Composable
fun InteractiveHostPreview() {
    MaterialTheme {
        CitizenDashboardInteractiveHost()
    }
}
