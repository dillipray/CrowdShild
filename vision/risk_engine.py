"""
Deterministic Mathematical Risk Evaluation Engine.
Directly mirrors the CrowdShield Android Core Engine mathematical specifications
(DEVELOPER_1_ENGINE_RULES.md and RiskCalculator.kt).
"""

from typing import Optional
from vision.config import VisionConfig
from vision.models import RiskLevel, RiskScore, RiskScoreBreakdown, MovementMetrics, DensitySummary


class VisionRiskCalculator:
    """
    Evaluates crowd density, velocity bottlenecks, and acceleration variance
    into a clamped risk score of 0.0 to 10.0 and assigns categorical risk levels.
    """

    def __init__(self, config: Optional[VisionConfig] = None):
        self.config = config or VisionConfig()

    def evaluate_risk(
        self,
        density_val: float,
        avg_velocity: float,
        accel_variance: float,
    ) -> RiskScore:
        """
        Pure deterministic risk evaluation matching CrowdShield core rules:
        - density_factor: min(density * 1.2, 6.0)
        - velocity_factor: if (density > 3.0 and avgVelocity < 0.5) -> 2.0 else 0.0 (or configured threshold)
        - accel_factor: min(accelVariance * 0.5, 2.0)
        - total_score: clamped to [0.0, 10.0]
        - Risk Level: > 7.5 -> HIGH_RISK, > 4.0 -> CAUTION, else SAFE

        :param density_val: Density metric (e.g. estimated persons per sq meter or normalized count)
        :param avg_velocity: Average velocity metric
        :param accel_variance: Acceleration variance metric
        :return: RiskScore with breakdown
        """
        # Density factor: maps 0-5 density scale to 0-6 score contribution
        density_factor = min(density_val * self.config.risk_density_weight, self.config.risk_max_density_score)

        # Velocity factor: detects stampede bottleneck when crowd density is high but flow is blocked
        velocity_factor = (
            self.config.risk_velocity_penalty
            if (density_val > 3.0 and avg_velocity < 0.5) or (density_val > 5.0 and avg_velocity < 1.5)
            else 0.0
        )

        # Acceleration variance factor: detects panic, turbulence, or sudden crowd surge
        accel_factor = min(accel_variance * self.config.risk_accel_weight, self.config.risk_max_accel_score)

        # Clamped Total Score (0.0 to 10.0)
        total_score = max(0.0, min(10.0, density_factor + velocity_factor + accel_factor))
        total_score = round(total_score, 2)

        # Categorical Risk Classification
        if total_score > self.config.risk_high_threshold:
            level = RiskLevel.HIGH_RISK
        elif total_score > self.config.risk_caution_threshold:
            level = RiskLevel.CAUTION
        else:
            level = RiskLevel.SAFE

        breakdown = RiskScoreBreakdown(
            density_factor=round(density_factor, 2),
            velocity_factor=round(velocity_factor, 2),
            accel_variance_factor=round(accel_factor, 2),
        )

        return RiskScore(
            score=total_score,
            level=level,
            breakdown=breakdown,
        )

    def evaluate_from_metrics(
        self,
        summary: DensitySummary,
        movement: MovementMetrics,
    ) -> RiskScore:
        """
        Convenience evaluator mapping computer vision summary metrics to risk score.
        """
        # Estimate equivalent density scale: max grid cell count or normalized human density
        # For a standard camera view, map total humans / hotspots to density equivalent
        density_metric = max(summary.max_grid_density * 0.8, summary.total_humans * 0.25)
        
        # Invert/normalize velocity for bottleneck calculation
        avg_v = movement.avg_velocity_px
        # If bottleneck is flagged by movement tracker, ensure velocity factor triggers
        if movement.bottleneck_detected:
            avg_v = 0.2

        return self.evaluate_risk(
            density_val=density_metric,
            avg_velocity=avg_v,
            accel_variance=movement.accel_variance,
        )
