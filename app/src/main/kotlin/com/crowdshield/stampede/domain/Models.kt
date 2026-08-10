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

data class RiskScore(
    val score: Float, // 0.0 to 10.0
    val level: RiskLevel
)

data class IncidentReport(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis()
)
