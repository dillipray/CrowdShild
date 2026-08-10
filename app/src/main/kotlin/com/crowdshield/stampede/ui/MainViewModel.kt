package com.crowdshield.stampede.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crowdshield.stampede.data.IncidentDao
import com.crowdshield.stampede.data.IncidentEntity
import com.crowdshield.stampede.domain.RiskLevel
import com.crowdshield.stampede.domain.RiskScore
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun updateRisk(risk: RiskScore) {
        _uiState.value = _uiState.value.copy(
            currentRisk = risk
        )
    }

    fun toggleMock(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isMockEnabled = enabled)
    }

    fun triggerEmergency() {
        // Logic for emergency SOS
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
            onComplete()
        }
    }
}

data class DashboardUiState(
    val currentRisk: RiskScore = RiskScore(0f, RiskLevel.SAFE),
    val isMockEnabled: Boolean = false
)

