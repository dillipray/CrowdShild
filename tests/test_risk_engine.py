dir app"""
Unit tests for the CrowdShield Modular Risk Engine.
Tests all factor evaluators, configuration loaders, environment overrides,
multi-factor interactions, risk levels, and custom factor extensions.
"""
import json
import os
import tempfile
import time
import unittest

from app.services.models import (
    CrowdTelemetry,
    FactorScore,
    IncidentReportData,
    RiskAssessment,
    RiskLevel,
    SensorPayload,
)
from app.services.risk_config import (
    FactorParametersConfig,
    FactorWeightsConfig,
    RiskEngineConfig,
    RiskThresholdsConfig,
)
from app.services.risk_engine import RiskEngine
from app.services.risk_factors import (
    BaseRiskFactor,
    DensityFactorEvaluator,
    DensityTrendFactorEvaluator,
    DirectionVarianceFactorEvaluator,
    IncidentReportFactorEvaluator,
    SensorFactorEvaluator,
    SpeedFactorEvaluator,
)


class TestRiskModels(unittest.TestCase):
    """Tests data models and serialization."""

    def test_risk_level_mapping(self):
        self.assertEqual(RiskLevel.from_score(0.0), RiskLevel.SAFE)
        self.assertEqual(RiskLevel.from_score(2.99), RiskLevel.SAFE)
        self.assertEqual(RiskLevel.from_score(3.0), RiskLevel.CAUTION)
        self.assertEqual(RiskLevel.from_score(4.99), RiskLevel.CAUTION)
        self.assertEqual(RiskLevel.from_score(5.0), RiskLevel.HIGH)
        self.assertEqual(RiskLevel.from_score(6.99), RiskLevel.HIGH)
        self.assertEqual(RiskLevel.from_score(7.0), RiskLevel.CRITICAL)
        self.assertEqual(RiskLevel.from_score(10.0), RiskLevel.CRITICAL)

    def test_custom_risk_level_thresholds(self):
        # Custom thresholds: SAFE < 2.0, CAUTION < 4.0, HIGH < 6.0, CRITICAL >= 6.0
        self.assertEqual(
            RiskLevel.from_score(2.5, safe_max=2.0, caution_max=4.0, high_max=6.0),
            RiskLevel.CAUTION,
        )
        self.assertEqual(
            RiskLevel.from_score(6.5, safe_max=2.0, caution_max=4.0, high_max=6.0),
            RiskLevel.CRITICAL,
        )

    def test_telemetry_and_assessment_serialization(self):
        telemetry = CrowdTelemetry(
            density=2.5,
            average_speed=1.2,
            direction_variance=0.3,
            sensor_data=SensorPayload(accelerometer_variance=1.5),
            incident_reports=[
                IncidentReportData(
                    id="rep-1",
                    latitude=40.7128,
                    longitude=-74.0060,
                    severity=3.0,
                )
            ],
        )
        d = telemetry.to_dict()
        self.assertEqual(d["density"], 2.5)
        self.assertIsNotNone(d["sensor_data"])
        self.assertEqual(len(d["incident_reports"]), 1)


class TestRiskConfig(unittest.TestCase):
    """Tests configuration loading from dict, JSON file, and environment variables."""

    def test_default_config(self):
        config = RiskEngineConfig()
        self.assertEqual(config.weights.density, 0.30)
        self.assertEqual(config.thresholds.safe_max, 3.0)
        self.assertEqual(config.thresholds.caution_max, 5.0)
        self.assertEqual(config.thresholds.high_max, 7.0)
        self.assertEqual(config.thresholds.critical_max, 10.0)

    def test_json_file_loading(self):
        config_path = os.path.join("config", "risk_config.json")
        if os.path.exists(config_path):
            config = RiskEngineConfig.from_json_file(config_path)
            self.assertEqual(config.thresholds.safe_max, 3.0)
            self.assertEqual(config.weights.density, 0.30)

    def test_save_and_reload_json(self):
        with tempfile.NamedTemporaryFile(suffix=".json", delete=False) as f:
            temp_path = f.name

        try:
            cfg = RiskEngineConfig()
            cfg.weights.density = 0.50
            cfg.thresholds.caution_max = 4.5
            cfg.save_json(temp_path)

            loaded_cfg = RiskEngineConfig.from_json_file(temp_path)
            self.assertEqual(loaded_cfg.weights.density, 0.50)
            self.assertEqual(loaded_cfg.thresholds.caution_max, 4.5)
        finally:
            if os.path.exists(temp_path):
                os.remove(temp_path)

    def test_env_variable_overrides(self):
        os.environ["RISK_WEIGHT_DENSITY"] = "0.45"
        os.environ["RISK_THRESHOLD_SAFE_MAX"] = "2.5"
        os.environ["RISK_PARAM_DENSITY_CRITICAL_LIMIT"] = "6.0"

        try:
            config = RiskEngineConfig.from_env()
            self.assertEqual(config.weights.density, 0.45)
            self.assertEqual(config.thresholds.safe_max, 2.5)
            self.assertEqual(config.parameters.density_critical_limit, 6.0)
            # Default value preserved for unset variables
            self.assertEqual(config.weights.speed, 0.20)
        finally:
            del os.environ["RISK_WEIGHT_DENSITY"]
            del os.environ["RISK_THRESHOLD_SAFE_MAX"]
            del os.environ["RISK_PARAM_DENSITY_CRITICAL_LIMIT"]


class TestIndividualRiskFactors(unittest.TestCase):
    """Tests each modular factor evaluator in isolation."""

    def setUp(self):
        self.config = RiskEngineConfig()

    def test_density_evaluator(self):
        evaluator = DensityFactorEvaluator()

        # Safe density (0.5 p/m^2)
        score_safe = evaluator.evaluate(
            CrowdTelemetry(density=0.5, average_speed=1.3, direction_variance=0.1),
            self.config,
        )
        self.assertLess(score_safe.normalized_score, 2.5)

        # Caution density (2.5 p/m^2)
        score_caution = evaluator.evaluate(
            CrowdTelemetry(density=2.5, average_speed=1.0, direction_variance=0.3),
            self.config,
        )
        self.assertTrue(2.5 <= score_caution.normalized_score <= 5.0)

        # Critical density (6.0 p/m^2)
        score_critical = evaluator.evaluate(
            CrowdTelemetry(density=6.0, average_speed=0.2, direction_variance=0.8),
            self.config,
        )
        self.assertGreaterEqual(score_critical.normalized_score, 7.5)

    def test_speed_evaluator(self):
        evaluator = SpeedFactorEvaluator()

        # Normal walking speed (1.34 m/s) -> low risk
        score_norm = evaluator.evaluate(
            CrowdTelemetry(density=1.0, average_speed=1.34, direction_variance=0.1),
            self.config,
        )
        self.assertLessEqual(score_norm.normalized_score, 1.5)

        # Stagnation speed in dense crowd (0.1 m/s, density 4.5) -> high crush / bottleneck risk
        score_bottleneck = evaluator.evaluate(
            CrowdTelemetry(density=4.5, average_speed=0.1, direction_variance=0.2),
            self.config,
        )
        self.assertGreaterEqual(score_bottleneck.normalized_score, 6.5)

        # Panic sprint speed (3.5 m/s) -> high stampede flight risk
        score_stampede = evaluator.evaluate(
            CrowdTelemetry(density=2.0, average_speed=3.5, direction_variance=0.4),
            self.config,
        )
        self.assertGreaterEqual(score_stampede.normalized_score, 5.0)

    def test_direction_variance_evaluator(self):
        evaluator = DirectionVarianceFactorEvaluator()

        # Unidirectional flow (0.05)
        score_laminar = evaluator.evaluate(
            CrowdTelemetry(density=1.0, average_speed=1.2, direction_variance=0.05),
            self.config,
        )
        self.assertLess(score_laminar.normalized_score, 2.0)

        # Severe chaotic movement (0.95)
        score_chaotic = evaluator.evaluate(
            CrowdTelemetry(density=2.0, average_speed=1.0, direction_variance=0.95),
            self.config,
        )
        self.assertGreaterEqual(score_chaotic.normalized_score, 8.0)

    def test_density_trend_evaluator(self):
        evaluator = DensityTrendFactorEvaluator()

        # Dispersing / negative trend
        score_disp = evaluator.evaluate(
            CrowdTelemetry(density=2.0, average_speed=1.2, direction_variance=0.1, density_trend=-0.5),
            self.config,
        )
        self.assertEqual(score_disp.normalized_score, 0.0)

        # Rapid flash surge (+2.0 p/m^2/min)
        score_surge = evaluator.evaluate(
            CrowdTelemetry(density=2.0, average_speed=1.2, direction_variance=0.1, density_trend=2.0),
            self.config,
        )
        self.assertGreaterEqual(score_surge.normalized_score, 7.5)

    def test_sensor_evaluator(self):
        evaluator = SensorFactorEvaluator()

        # No sensor data
        score_none = evaluator.evaluate(
            CrowdTelemetry(density=1.0, average_speed=1.0, direction_variance=0.1),
            self.config,
        )
        self.assertEqual(score_none.normalized_score, 0.0)

        # Violent jostling + impact + fall
        sensor = SensorPayload(
            accelerometer_variance=12.0,
            jerk_magnitude=25.0,
            peak_impact_g=3.5,
            is_device_falling=True,
        )
        score_impact = evaluator.evaluate(
            CrowdTelemetry(density=2.0, average_speed=1.0, direction_variance=0.1, sensor_data=sensor),
            self.config,
        )
        self.assertGreaterEqual(score_impact.normalized_score, 8.0)

    def test_incident_report_evaluator(self):
        evaluator = IncidentReportFactorEvaluator()

        # No reports
        score_none = evaluator.evaluate(
            CrowdTelemetry(density=1.0, average_speed=1.0, direction_variance=0.1),
            self.config,
        )
        self.assertEqual(score_none.normalized_score, 0.0)

        # Active severe report in close vicinity
        reports = [
            IncidentReportData(
                id="inc-1",
                latitude=12.9716,
                longitude=77.5946,
                severity=5.0,
                timestamp=time.time() - 30,  # 30 seconds ago
                incident_type="SURGE",
            )
        ]
        score_active = evaluator.evaluate(
            CrowdTelemetry(
                density=2.0,
                average_speed=1.0,
                direction_variance=0.1,
                latitude=12.9716,
                longitude=77.5946,
                incident_reports=reports,
            ),
            self.config,
        )
        self.assertGreaterEqual(score_active.normalized_score, 6.0)


class TestRiskEngine(unittest.TestCase):
    """Tests the full RiskEngine service integration, composite scoring, and risk levels."""

    def setUp(self):
        self.engine = RiskEngine()

    def test_safe_scenario(self):
        telemetry = CrowdTelemetry(
            density=0.8,
            average_speed=1.30,
            direction_variance=0.10,
            density_trend=0.0,
        )
        assessment = self.engine.evaluate(telemetry)
        self.assertEqual(assessment.risk_level, RiskLevel.SAFE)
        self.assertLess(assessment.composite_score, 3.0)
        self.assertGreaterEqual(assessment.composite_score, 0.0)

    def test_caution_scenario(self):
        telemetry = CrowdTelemetry(
            density=2.6,
            average_speed=0.9,
            direction_variance=0.40,
            density_trend=0.3,
        )
        assessment = self.engine.evaluate(telemetry)
        self.assertEqual(assessment.risk_level, RiskLevel.CAUTION)
        self.assertTrue(3.0 <= assessment.composite_score < 5.0)

    def test_high_risk_scenario(self):
        telemetry = CrowdTelemetry(
            density=4.2,
            average_speed=0.6,
            direction_variance=0.65,
            density_trend=0.8,
        )
        assessment = self.engine.evaluate(telemetry)
        self.assertEqual(assessment.risk_level, RiskLevel.HIGH)
        self.assertTrue(5.0 <= assessment.composite_score < 7.0)

    def test_critical_risk_scenario(self):
        # High density + Stoppage/Bottleneck + Violent Chaos + Rapid Flash Surge
        telemetry = CrowdTelemetry(
            density=5.8,
            average_speed=0.15,
            direction_variance=0.88,
            density_trend=1.8,
            sensor_data=SensorPayload(accelerometer_variance=10.0, peak_impact_g=3.0),
            incident_reports=[
                IncidentReportData(
                    id="crit-1",
                    latitude=13.0,
                    longitude=80.0,
                    severity=5.0,
                    timestamp=time.time(),
                )
            ],
            latitude=13.0,
            longitude=80.0,
        )
        assessment = self.engine.evaluate(telemetry)
        self.assertEqual(assessment.risk_level, RiskLevel.CRITICAL)
        self.assertTrue(7.0 <= assessment.composite_score <= 10.0)
        self.assertGreater(assessment.interaction_penalty, 0.0)
        self.assertTrue(any("CRITICAL" in r or "emergency" in r.lower() for r in assessment.recommendations))

    def test_weight_normalization_without_optional_data(self):
        # No sensor data or incident reports
        telemetry = CrowdTelemetry(
            density=2.0,
            average_speed=1.2,
            direction_variance=0.2,
            density_trend=0.1,
        )
        assessment = self.engine.evaluate(telemetry)
        # Sum of active weights should equal 1.0
        active_weights_sum = sum(
            f.weight for f in assessment.factor_breakdown.values() if f.weight > 0
        )
        self.assertAlmostEqual(active_weights_sum, 1.0, places=3)

    def test_custom_factor_plugin(self):
        class WeatherFactor(BaseRiskFactor):
            @property
            def factor_name(self) -> str:
                return "extreme_weather"

            def evaluate(self, telemetry: CrowdTelemetry, config: RiskEngineConfig) -> FactorScore:
                return FactorScore(
                    factor_name=self.factor_name,
                    raw_value=40.0,  # e.g., 40 C heat wave
                    normalized_score=5.0,
                    weight=0.10,
                    weighted_score=0.5,
                )

        custom_engine = RiskEngine()
        custom_engine.register_factor(WeatherFactor())

        telemetry = CrowdTelemetry(density=1.0, average_speed=1.2, direction_variance=0.1)
        assessment = custom_engine.evaluate(telemetry)
        self.assertIn("extreme_weather", assessment.factor_breakdown)

    def test_density_trend_calculator(self):
        # Synthetic time series: density increasing from 1.0 to 4.0 over 2 minutes (120s)
        now = time.time()
        history = [
            (now, 1.0),
            (now + 30, 1.75),
            (now + 60, 2.5),
            (now + 90, 3.25),
            (now + 120, 4.0),
        ]
        trend = RiskEngine.calculate_density_trend(history)
        # Expected slope: 3.0 increase / 2 minutes = 1.5 p/m^2/min
        self.assertAlmostEqual(trend, 1.5, places=2)

    def test_batch_evaluation(self):
        telemetry_list = [
            CrowdTelemetry(density=0.5, average_speed=1.4, direction_variance=0.05),
            CrowdTelemetry(density=6.0, average_speed=0.2, direction_variance=0.9),
        ]
        results = self.engine.evaluate_batch(telemetry_list)
        self.assertEqual(len(results), 2)
        self.assertEqual(results[0].risk_level, RiskLevel.SAFE)
        self.assertEqual(results[1].risk_level, RiskLevel.CRITICAL)


if __name__ == "__main__":
    unittest.main()
