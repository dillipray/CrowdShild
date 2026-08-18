"""
Unit and integration tests for the CrowdShield Vision & Density Engine.
"""

import pytest
import numpy as np
from vision.config import VisionConfig
from vision.models import BoundingBox, RiskLevel
from vision.density import DensityCalculator
from vision.movement import MovementTracker
from vision.risk_engine import VisionRiskCalculator
from vision.pipeline import CrowdVisionPipeline


def test_risk_calculator_deterministic_rules():
    """Verify risk calculator matches DEVELOPER_1_ENGINE_RULES mathematical specifications."""
    calculator = VisionRiskCalculator()

    # Case 1: Low density, normal velocity, zero accel variance -> SAFE
    safe_risk = calculator.evaluate_risk(density_val=1.0, avg_velocity=2.0, accel_variance=0.0)
    assert safe_risk.score == 1.2
    assert safe_risk.level == RiskLevel.SAFE
    assert safe_risk.breakdown.density_factor == 1.2
    assert safe_risk.breakdown.velocity_factor == 0.0
    assert safe_risk.breakdown.accel_variance_factor == 0.0

    # Case 2: High density with bottleneck (low velocity) -> CAUTION/HIGH
    bottleneck_risk = calculator.evaluate_risk(density_val=4.0, avg_velocity=0.3, accel_variance=0.0)
    # density_factor = min(4.0 * 1.2, 6.0) = 4.8
    # velocity_factor = 2.0 (bottleneck triggered)
    # accel_factor = 0.0
    # total = 6.8 -> CAUTION
    assert bottleneck_risk.score == 6.8
    assert bottleneck_risk.level == RiskLevel.CAUTION
    assert bottleneck_risk.breakdown.velocity_factor == 2.0

    # Case 3: High density, bottleneck, and panic surge (high accel variance) -> HIGH_RISK / CRITICAL
    critical_risk = calculator.evaluate_risk(density_val=5.0, avg_velocity=0.2, accel_variance=4.0)
    # density_factor = 6.0
    # velocity_factor = 2.0
    # accel_factor = min(4.0 * 0.5, 2.0) = 2.0
    # total = 10.0 -> HIGH_RISK (clamped at 10.0)
    assert critical_risk.score == 10.0
    assert critical_risk.level == RiskLevel.HIGH_RISK


def test_density_grid_calculation():
    """Verify spatial grid density calculation and hotspot identification."""
    config = VisionConfig(grid_rows=4, grid_cols=4, min_hotspot_count=3, hotspot_ratio_threshold=1.5)
    calculator = DensityCalculator(config)

    # Mock 10 human detections concentrated in top-left cell (0, 0)
    detections = [
        BoundingBox(
            x1=10, y1=10, x2=50, y2=90,
            confidence=0.9, track_id=i,
            centroid_x=30, centroid_y=50,
            width=40, height=80, area=3200
        )
        for i in range(10)
    ]

    grid = calculator.compute_grid_density(detections, frame_width=640, frame_height=480)
    assert len(grid) == 16

    # Cell (0, 0) should have count 10 and be marked as hotspot
    top_left_cell = next(c for c in grid if c.row == 0 and c.col == 0)
    assert top_left_cell.human_count == 10
    assert top_left_cell.is_hotspot is True
    assert top_left_cell.risk_level in (RiskLevel.CAUTION, RiskLevel.HIGH_RISK)

    # Empty cells
    other_cell = next(c for c in grid if c.row == 3 and c.col == 3)
    assert other_cell.human_count == 0
    assert other_cell.is_hotspot is False


def test_movement_and_bottleneck_tracking():
    """Verify movement velocity and acceleration variance calculation."""
    tracker = MovementTracker()

    # Frame 1: 5 humans at (100, 100)
    frame1_dets = [
        BoundingBox(
            x1=100, y1=100, x2=140, y2=180,
            confidence=0.9, track_id=i,
            centroid_x=120.0, centroid_y=140.0,
            width=40, height=80, area=3200
        )
        for i in range(1, 6)
    ]
    m1 = tracker.update(frame1_dets, timestamp_ms=1000, total_humans=5)
    assert m1.tracked_count == 5

    # Frame 2: Move humans slightly (stationary / bottleneck test)
    frame2_dets = [
        BoundingBox(
            x1=101, y1=101, x2=141, y2=181,
            confidence=0.9, track_id=i,
            centroid_x=121.0, centroid_y=141.0,
            width=40, height=80, area=3200
        )
        for i in range(1, 6)
    ]
    m2 = tracker.update(frame2_dets, timestamp_ms=1033, total_humans=5)
    assert m2.avg_velocity_px > 0.0


def test_pipeline_synthetic_frame():
    """Verify end-to-end pipeline execution on a synthetic video frame."""
    pipeline = CrowdVisionPipeline()
    dummy_frame = np.zeros((480, 640, 3), dtype=np.uint8)

    result, annotated = pipeline.process_frame(dummy_frame, annotate=True)

    assert result.frame_width == 640
    assert result.frame_height == 480
    assert result.summary.total_humans == 0
    assert result.risk_score.score == 0.0
    assert result.risk_score.level == RiskLevel.SAFE
    assert annotated.shape == (480, 640, 3)
    assert len(result.grid_density) == 16
