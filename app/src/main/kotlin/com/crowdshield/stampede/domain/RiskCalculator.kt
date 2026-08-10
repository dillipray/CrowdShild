package com.crowdshield.stampede.domain

import kotlin.math.min

class RiskCalculator {
    /**
     * Calculates risk score based on density, velocity, and acceleration variance.
     * Score range: 0.0 to 10.0
     */
    fun calculateRisk(
        density: Double,
        avgVelocity: Double,
        accelVariance: Double
    ): RiskScore {
        // Simple mock algorithm for the prototype
        // density factor: 0-5 people/sqm -> 0-6 score
        val densityFactor = min(density * 1.2, 6.0)
        
        // velocity factor: if avgVelocity is low in high density, it indicates bottleneck
        val velocityFactor = if (density > 3.0 && avgVelocity < 0.5) 2.0 else 0.0
        
        // acceleration variance: high variance indicates panic/surge
        val accelFactor = min(accelVariance * 0.5, 2.0)
        
        val totalScore = (densityFactor + velocityFactor + accelFactor).toFloat().coerceIn(0f, 10f)
        
        val level = when {
            totalScore > 7.5 -> RiskLevel.HIGH_RISK
            totalScore > 4.0 -> RiskLevel.CAUTION
            else -> RiskLevel.SAFE
        }
        
        return RiskScore(totalScore, level)
    }
}
