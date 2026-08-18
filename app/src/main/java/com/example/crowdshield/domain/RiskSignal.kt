package com.example.crowdshield.domain

/**
 * Data Model for Citizen Crowd Risk Signal.
 * Used for real-time crowd status, navigation guidance, and risk level.
 */
data class RiskSignal(
    val sectorId: String = "Sector 4 - East Concourse",
    val riskScore: Float = 1.5f, // 0.0 to 10.0 scale
    val densityLevel: String = "Low (1.2 people/m²)",
    val headline: String = "SAFE CONDITIONS",
    val navigationGuidance: String = "Area clear. Move freely toward any open exit.",
    val timestamp: Long = System.currentTimeMillis()
)

enum class RiskSeverity(val label: String) {
    LOW("Low Risk"),
    MEDIUM("Moderate Caution"),
    HIGH("High Risk"),
    CRITICAL("Critical Emergency");

    companion object {
        fun fromScore(score: Float): RiskSeverity = when {
            score >= 8.5f -> CRITICAL
            score >= 7.0f -> HIGH
            score >= 4.0f -> MEDIUM
            else -> LOW
        }
    }
}
