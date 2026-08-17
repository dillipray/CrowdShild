"""
Spatial Density and Crowd Cluster Calculator.
Computes grid cell densities, identifies local hotspots, and clusters crowd hotspots.
"""

from typing import List, Tuple, Dict, Any, Optional
import math
import numpy as np
from sklearn.cluster import DBSCAN

from vision.config import VisionConfig
from vision.models import (
    BoundingBox,
    GridCellDensity,
    CrowdCluster,
    DensitySummary,
    RiskLevel,
)


class DensityCalculator:
    """
    Computes spatial density distributions, grid overlays, and crowd clustering.
    """

    def __init__(self, config: Optional[VisionConfig] = None):
        self.config = config or VisionConfig()

    def compute_grid_density(
        self,
        detections: List[BoundingBox],
        frame_width: int,
        frame_height: int,
    ) -> List[GridCellDensity]:
        """
        Subdivides the frame into an (R x C) grid and computes localized crowd densities.

        :param detections: List of human BoundingBox detections
        :param frame_width: Frame pixel width
        :param frame_height: Frame pixel height
        :return: List of GridCellDensity objects
        """
        rows = self.config.grid_rows
        cols = self.config.grid_cols
        cell_w = frame_width / cols
        cell_h = frame_height / rows

        # Initialize count matrix
        counts = np.zeros((rows, cols), dtype=int)

        for det in detections:
            # Map human centroid to grid cell
            col_idx = int(np.clip(det.centroid_x // cell_w, 0, cols - 1))
            row_idx = int(np.clip(det.centroid_y // cell_h, 0, rows - 1))
            counts[row_idx, col_idx] += 1

        total_cells = rows * cols
        total_detections = len(detections)
        avg_density = total_detections / total_cells if total_cells > 0 else 0.0

        grid_cells: List[GridCellDensity] = []
        for r in range(rows):
            for c in range(cols):
                count = int(counts[r, c])
                x1 = int(c * cell_w)
                y1 = int(r * cell_h)
                x2 = int((c + 1) * cell_w) if c < cols - 1 else frame_width
                y2 = int((r + 1) * cell_h) if r < rows - 1 else frame_height

                ratio = (count / avg_density) if avg_density > 0 else (1.0 if count > 0 else 0.0)
                is_hotspot = (
                    count >= self.config.min_hotspot_count
                    and ratio >= self.config.hotspot_ratio_threshold
                )

                # Determine cell risk level based on local density
                if count >= (self.config.min_hotspot_count * 2.5) or (is_hotspot and count >= 8):
                    risk_level = RiskLevel.HIGH_RISK
                elif is_hotspot or count >= self.config.min_hotspot_count:
                    risk_level = RiskLevel.CAUTION
                else:
                    risk_level = RiskLevel.SAFE

                grid_cells.append(
                    GridCellDensity(
                        row=r,
                        col=c,
                        x1=x1,
                        y1=y1,
                        x2=x2,
                        y2=y2,
                        human_count=count,
                        density_ratio=round(ratio, 2),
                        is_hotspot=is_hotspot,
                        risk_level=risk_level,
                    )
                )

        return grid_cells

    def compute_clusters(self, detections: List[BoundingBox]) -> List[CrowdCluster]:
        """
        Performs DBSCAN spatial clustering on human centroids to isolate crowd groups.

        :param detections: List of human BoundingBox detections
        :return: List of CrowdCluster models
        """
        if len(detections) < self.config.cluster_min_samples:
            return []

        centroids = np.array([[det.centroid_x, det.centroid_y] for det in detections])

        # Cluster spatial points
        db = DBSCAN(
            eps=self.config.cluster_eps_px,
            min_samples=self.config.cluster_min_samples,
            metric="euclidean",
        ).fit(centroids)

        labels = db.labels_
        unique_labels = set(labels)
        clusters: List[CrowdCluster] = []

        for label in unique_labels:
            if label == -1:
                # Noise points (not in a concentrated cluster)
                continue

            cluster_points = centroids[labels == label]
            count = len(cluster_points)

            center_x = float(np.mean(cluster_points[:, 0]))
            center_y = float(np.mean(cluster_points[:, 1]))

            # Calculate cluster radius (max distance from centroid or default min)
            distances = np.linalg.norm(cluster_points - np.array([center_x, center_y]), axis=1)
            radius = float(max(np.max(distances) + 20.0, 40.0))

            # Density metric: count per area (normalized)
            area_sq_kpx = (math.pi * (radius ** 2)) / 1000.0
            density_val = round(count / area_sq_kpx if area_sq_kpx > 0 else 0.0, 3)

            if count >= 10 or density_val > 0.15:
                severity = RiskLevel.HIGH_RISK
            elif count >= 5 or density_val > 0.08:
                severity = RiskLevel.CAUTION
            else:
                severity = RiskLevel.SAFE

            clusters.append(
                CrowdCluster(
                    id=f"cluster_{label + 1}",
                    center_x=round(center_x, 1),
                    center_y=round(center_y, 1),
                    radius=round(radius, 1),
                    human_count=count,
                    density=density_val,
                    severity=severity,
                )
            )

        return clusters

    def summarize(
        self,
        detections: List[BoundingBox],
        grid_cells: List[GridCellDensity],
        clusters: List[CrowdCluster],
    ) -> DensitySummary:
        """Create summary statistics of crowd density across the frame."""
        total_humans = len(detections)
        hotspot_count = sum(1 for c in grid_cells if c.is_hotspot)
        max_grid_density = max((c.human_count for c in grid_cells), default=0)
        mean_ratio = float(np.mean([c.density_ratio for c in grid_cells])) if grid_cells else 0.0

        return DensitySummary(
            total_humans=total_humans,
            hotspot_count=hotspot_count,
            active_clusters_count=len(clusters),
            max_grid_density=max_grid_density,
            mean_density_ratio=round(mean_ratio, 2),
        )
