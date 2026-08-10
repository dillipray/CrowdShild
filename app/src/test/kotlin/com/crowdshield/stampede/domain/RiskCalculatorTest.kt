package com.crowdshield.stampede.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RiskCalculatorTest {
    private val calculator = RiskCalculator()

    @Test
    fun `test safe conditions`() {
        val result = calculator.calculateRisk(density = 0.5, avgVelocity = 1.5, accelVariance = 0.1)
        assertEquals(RiskLevel.SAFE, result.level)
        assertTrue(result.score < 2.0f)
    }

    @Test
    fun `test high risk conditions`() {
        // High density + Low velocity (bottleneck) + high acceleration variance (panic)
        val result = calculator.calculateRisk(density = 5.0, avgVelocity = 0.2, accelVariance = 4.0)
        assertEquals(RiskLevel.HIGH_RISK, result.level)
        assertTrue(result.score > 7.5f)
    }

    @Test
    fun `test caution conditions`() {
        val result = calculator.calculateRisk(density = 3.5, avgVelocity = 1.0, accelVariance = 1.0)
        assertEquals(RiskLevel.CAUTION, result.level)
        assertTrue(result.score in 4.0f..7.5f)
    }
}
