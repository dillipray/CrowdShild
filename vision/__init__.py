"""
CrowdShield Computer Vision Engine
=================================
Real-time human detection, spatial density estimation, crowd movement tracking,
and stampede risk modeling for CrowdShield.
"""

from vision.models import (
    BoundingBox,
    GridCellDensity,
    CrowdCluster,
    MovementMetrics,
    RiskLevel,
    RiskScore,
    FrameDensityResult,
    DensitySummary,
)
from vision.config import VisionConfig
from vision.detector import HumanDetector
from vision.density import DensityCalculator
from vision.movement import MovementTracker
from vision.risk_engine import VisionRiskCalculator
from vision.annotator import VisionAnnotator
from vision.pipeline import CrowdVisionPipeline

__all__ = [
    "BoundingBox",
    "GridCellDensity",
    "CrowdCluster",
    "MovementMetrics",
    "RiskLevel",
    "RiskScore",
    "FrameDensityResult",
    "DensitySummary",
    "VisionConfig",
    "HumanDetector",
    "DensityCalculator",
    "MovementTracker",
    "VisionRiskCalculator",
    "VisionAnnotator",
    "CrowdVisionPipeline",
]
