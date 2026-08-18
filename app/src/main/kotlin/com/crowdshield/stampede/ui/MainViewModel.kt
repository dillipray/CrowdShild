package com.crowdshield.stampede.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crowdshield.stampede.data.IncidentDao
import com.crowdshield.stampede.data.IncidentEntity
import com.crowdshield.stampede.domain.RiskLevel
import com.crowdshield.stampede.domain.RiskScore
import com.crowdshield.stampede.domain.RiskSignal
import com.crowdshield.stampede.domain.SeverityLevel
import com.crowdshield.stampede.domain.toRiskSignal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val incidentDao: IncidentDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    fun updateRisk(risk: RiskScore) {
        val previousScore = _uiState.value.currentRisk.score
        val newScore = risk.score
        val trend = when {
            newScore - previousScore > 0.15f -> RiskTrend.INCREASING
            previousScore - newScore > 0.15f -> RiskTrend.DECREASING
            else -> RiskTrend.STABLE
        }
        val signal = risk.toRiskSignal(_uiState.value.riskSignal.sectorId)
        _uiState.value = _uiState.value.copy(
            currentRisk = risk,
            riskSignal = signal,
            riskTrend = trend
        )
    }

    fun updateRiskSignal(signal: RiskSignal) {
        val previousScore = _uiState.value.currentRisk.score
        val newScore = signal.riskScore
        val trend = when {
            newScore - previousScore > 0.15f -> RiskTrend.INCREASING
            previousScore - newScore > 0.15f -> RiskTrend.DECREASING
            else -> RiskTrend.STABLE
        }
        val level = when (signal.severityLevel) {
            SeverityLevel.LOW -> RiskLevel.SAFE
            SeverityLevel.MEDIUM -> RiskLevel.CAUTION
            SeverityLevel.HIGH, SeverityLevel.CRITICAL -> RiskLevel.HIGH_RISK
        }
        _uiState.value = _uiState.value.copy(
            currentRisk = RiskScore(signal.riskScore, level),
            riskSignal = signal,
            riskTrend = trend
        )
    }

    fun toggleMock(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isMockEnabled = enabled)
    }

    fun setSimulationPreset(severity: SeverityLevel) {
        val simulatedSignal = when (severity) {
            SeverityLevel.LOW -> RiskSignal(
                sectorId = "Sector 4 - East Concourse",
                riskScore = 1.8f,
                densityLevel = "Low (1.2 people/m²)",
                headline = "SAFE CONDITIONS",
                navigationGuidance = "Area clear. Move freely toward any open exit.",
                timestamp = System.currentTimeMillis()
            )
            SeverityLevel.MEDIUM -> RiskSignal(
                sectorId = "Sector 4 - East Concourse",
                riskScore = 5.2f,
                densityLevel = "Moderate (3.9 people/m²)",
                headline = "CAUTION: ELEVATED CROWD",
                navigationGuidance = "Congestion near Gate 2. Stay right and walk steadily.",
                timestamp = System.currentTimeMillis()
            )
            SeverityLevel.HIGH -> RiskSignal(
                sectorId = "Sector 4 - East Concourse",
                riskScore = 7.8f,
                densityLevel = "High (6.4 people/m²)",
                headline = "HIGH SURGE DETECTED",
                navigationGuidance = "HIGH RISK: Avoid Gate 2, move toward Exit B.",
                timestamp = System.currentTimeMillis()
            )
            SeverityLevel.CRITICAL -> RiskSignal(
                sectorId = "Sector 4 - East Concourse",
                riskScore = 9.4f,
                densityLevel = "Critical (8.9 people/m²)",
                headline = "CRITICAL STAMPEDE HAZARD",
                navigationGuidance = "CRITICAL: DO NOT ENTER CONCOURSE! Divert immediately to Exit C!",
                timestamp = System.currentTimeMillis()
            )
        }
        updateRiskSignal(simulatedSignal)
    }

    fun startSosCountdown(onDispatched: () -> Unit = {}) {
        if (_uiState.value.isSosCountingDown || _uiState.value.isSosDispatched) return

        _uiState.value = _uiState.value.copy(
            isSosCountingDown = true,
            sosCountdownSeconds = 5
        )

        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (sec in 5 downTo 1) {
                _uiState.value = _uiState.value.copy(sosCountdownSeconds = sec)
                delay(1000)
            }
            triggerEmergency(onDispatched)
        }
    }

    fun cancelSosCountdown() {
        countdownJob?.cancel()
        countdownJob = null
        _uiState.value = _uiState.value.copy(
            isSosCountingDown = false,
            sosCountdownSeconds = 0
        )
    }

    fun resetSosDispatch() {
        countdownJob?.cancel()
        countdownJob = null
        _uiState.value = _uiState.value.copy(
            isSosCountingDown = false,
            sosCountdownSeconds = 0,
            isSosDispatched = false
        )
    }

    fun triggerEmergency(onDispatched: () -> Unit = {}) {
        _uiState.value = _uiState.value.copy(
            isSosCountingDown = false,
            sosCountdownSeconds = 0,
            isSosDispatched = true,
            recentBannerMessage = "🚨 SOS Emergency Dispatched! First Responders & Field Security Alerted."
        )

        viewModelScope.launch {
            val incident = IncidentEntity(
                id = UUID.randomUUID().toString(),
                type = "EMERGENCY_SOS",
                description = "SOS Panic Triggered by User at ${_uiState.value.riskSignal.sectorId}",
                latitude = 0.0,
                longitude = 0.0,
                timestamp = System.currentTimeMillis()
            )
            incidentDao.insertIncident(incident)
            onDispatched()
        }
    }

    fun submitQuickHazard(hazardType: String, onComplete: () -> Unit = {}) {
        val userFriendlyMessage = "Reported: $hazardType. Field teams notified."
        _uiState.value = _uiState.value.copy(
            recentBannerMessage = "✅ $userFriendlyMessage"
        )

        viewModelScope.launch {
            val incident = IncidentEntity(
                id = UUID.randomUUID().toString(),
                type = hazardType.uppercase().replace(" ", "_"),
                description = "Quick-Tap Hazard Report: $hazardType at ${_uiState.value.riskSignal.sectorId}",
                latitude = 0.0,
                longitude = 0.0,
                timestamp = System.currentTimeMillis()
            )
            incidentDao.insertIncident(incident)
            onComplete()
        }
    }

    fun dismissBannerMessage() {
        _uiState.value = _uiState.value.copy(recentBannerMessage = null)
    }

    fun submitIncidentReport(description: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val incident = IncidentEntity(
                id = UUID.randomUUID().toString(),
                type = "USER_REPORT",
                description = description,
                latitude = 0.0,
                longitude = 0.0,
                timestamp = System.currentTimeMillis()
            )
            incidentDao.insertIncident(incident)
            _uiState.value = _uiState.value.copy(
                recentBannerMessage = "✅ Incident report submitted successfully."
            )
            onComplete()
        }
    }
}

enum class RiskTrend(val label: String, val symbol: String) {
    INCREASING("Increasing", "↑"),
    STABLE("Stable", "→"),
    DECREASING("Decreasing", "↓")
}

data class DashboardUiState(
    val currentRisk: RiskScore = RiskScore(1.5f, RiskLevel.SAFE),
    val riskSignal: RiskSignal = RiskSignal(),
    val riskTrend: RiskTrend = RiskTrend.STABLE,
    val isMockEnabled: Boolean = false,
    val isSosCountingDown: Boolean = false,
    val sosCountdownSeconds: Int = 0,
    val isSosDispatched: Boolean = false,
    val recentBannerMessage: String? = null
)

