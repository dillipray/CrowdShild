"""
Pydantic data models for CrowdShield Computer Vision and Density Telemetry.
"""

from __future__ import annotations
from enum import Enum
from typing import List, Optional, Tuple, Dict, Any
from pydantic import BaseModel, Field


class RiskLevel(str, Enum):
    SAFE = "SAFE"
    CAUTION = "CAUTION"
    HIGH_RISK = "HIGH_RISK"


class RiskScoreBreakdown(BaseModel):
    density_factor: float = Field(..., description="Calculated density component (0.0 to 6.0)")
    velocity_factor: float = Field(..., description="Bottleneck factor (0.0 to 2.0)")
    accel_variance_factor: float = Field(..., description="Panic/surge variance factor (0.0 to 2.0)")


class RiskScore(BaseModel):
    score: float = Field(..., ge=0.0, le=10.0, description="Clamped risk score from 0.0 to 10.0")
    level: RiskLevel = Field(..., description="Categorical risk level: SAFE, CAUTION, HIGH_RISK")
    breakdown: Optional[RiskScoreBreakdown] = Field(
        default=None, description="Detailed component factors contributing to score"
    )


class BoundingBox(BaseModel):
    x1: float
    y1: float
    x2: float
    y2: float
    confidence: float = Field(..., ge=0.0, le=1.0)
    track_id: Optional[int] = Field(default=None, description="Unique tracking identifier across frames")
    centroid_x: float
    centroid_y: float
    width: float
    height: float
    area: float


class GridCellDensity(BaseModel):
    row: int
    col: int
    x1: int
    y1: int
    x2: int
    y2: int
    human_count: int = Field(..., ge=0)
    density_ratio: float = Field(..., description="Relative density compared to frame average")
    is_hotspot: bool = Field(default=False, description="True if cell exceeds hotspot threshold")
    risk_level: RiskLevel = Field(default=RiskLevel.SAFE)


class CrowdCluster(BaseModel):
    id: str
    center_x: float
    center_y: float
    radius: float
    human_count: int
    density: float = Field(..., description="Density (humans per unit area)")
    severity: RiskLevel = Field(default=RiskLevel.SAFE)


class MovementMetrics(BaseModel):
    avg_velocity_px: float = Field(default=0.0, description="Average tracked velocity (pixels/frame or px/sec)")
    max_velocity_px: float = Field(default=0.0, description="Maximum individual speed detected")
    accel_variance: float = Field(default=0.0, description="Acceleration variance across crowd (panic indicator)")
    bottleneck_detected: bool = Field(default=False, description="True if crowd has high density with stalled movement")
    surge_detected: bool = Field(default=False, description="True if acceleration variance spikes suddenly")
    tracked_count: int = Field(default=0, description="Number of currently active tracked human instances")


class DensitySummary(BaseModel):
    total_humans: int = Field(..., ge=0)
    hotspot_count: int = Field(default=0)
    active_clusters_count: int = Field(default=0)
    max_grid_density: int = Field(default=0)
    mean_density_ratio: float = Field(default=0.0)


class FrameDensityResult(BaseModel):
    frame_index: int
    timestamp_ms: int
    frame_width: int
    frame_height: int
    fps: float = Field(default=0.0)
    summary: DensitySummary
    detections: List[BoundingBox]
    grid_density: List[GridCellDensity]
    clusters: List[CrowdCluster]
    movement: MovementMetrics
    risk_score: RiskScore
    metadata: Dict[str, Any] = Field(default_factory=dict)
