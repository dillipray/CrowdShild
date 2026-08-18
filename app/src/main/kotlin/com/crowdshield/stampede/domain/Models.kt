package com.crowdshield.stampede.domain

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis()
)

data class CrowdCluster(
    val id: String,
    val centerLat: Double,
    val centerLng: Double,
    val radius: Double,
    val density: Double // People per sq. meter
)

enum class RiskLevel {
    SAFE, CAUTION, HIGH_RISK
}

enum class SeverityLevel(val label: String) {
    LOW("Low Risk"),
    MEDIUM("Moderate Caution"),
    HIGH("High Risk"),
    CRITICAL("Critical Emergency");

    companion object {
        fun fromScore(score: Float): SeverityLevel = when {
            score >= 8.5f -> CRITICAL
            score >= 7.0f -> HIGH
            score >= 4.0f -> MEDIUM
            else -> LOW
        }
    }
}

data class RiskScore(
    val score: Float, // 0.0 to 10.0
    val level: RiskLevel
)

data class RiskSignal(
    val sectorId: String = "Sector 4 - East Concourse",
    val riskScore: Float = 1.5f, // 0.0 to 10.0 scale
    val densityLevel: String = "Low (1.2 people/m²)",
    val headline: String = "SAFE CONDITIONS",
    val navigationGuidance: String = "Area clear. Move freely toward any exit.",
    val timestamp: Long = System.currentTimeMillis()
) {
    val severityLevel: SeverityLevel
        get() = SeverityLevel.fromScore(riskScore)
}

fun RiskScore.toRiskSignal(sectorId: String = "Sector 4 - East Concourse"): RiskSignal {
    val severity = SeverityLevel.fromScore(score)
    val (density, headline, guidance) = when (severity) {
        SeverityLevel.LOW -> Triple(
            "Low Density (1.2 people/m²)",
            "SAFE CONDITIONS",
            "Area clear. Move freely toward any exit."
        )
        SeverityLevel.MEDIUM -> Triple(
            "Moderate Density (3.8 people/m²)",
            "CAUTION: ELEVATED CROWD",
            "Congestion forming near Gate 2. Stay right and proceed steadily."
        )
        SeverityLevel.HIGH -> Triple(
            "High Density (6.2 people/m²)",
            "HIGH RISK: SURGE DETECTED",
            "HIGH RISK: Avoid Gate 2, move toward Exit B."
        )
        SeverityLevel.CRITICAL -> Triple(
            "Critical Density (8.5+ people/m²)",
            "CRITICAL EMERGENCY: CRUSH HAZARD",
            "CRITICAL: STOP MOVING TOWARD GATE 2! Divert immediately to Exit C!"
        )
    }
    return RiskSignal(
        sectorId = sectorId,
        riskScore = score,
        densityLevel = density,
        headline = headline,
        navigationGuidance = guidance,
        timestamp = System.currentTimeMillis()
    )
}

data class IncidentReport(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis()
)

