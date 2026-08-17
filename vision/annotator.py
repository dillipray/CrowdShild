"""
OpenCV Visual Annotator for CrowdShield Computer Vision.
Overlays bounding boxes, centroid trails, density grid zones, clusters, and HUD risk telemetry.
"""

from typing import List, Tuple, Optional
import cv2
import numpy as np

from vision.models import (
    FrameDensityResult,
    BoundingBox,
    GridCellDensity,
    CrowdCluster,
    RiskLevel,
)
from vision.config import VisionConfig


class VisionAnnotator:
    """
    Renders visual debug & monitoring overlays on top of video frames.
    """

    # Color palette (BGR format for OpenCV)
    COLOR_SAFE = (76, 175, 80)        # Green
    COLOR_CAUTION = (0, 191, 255)     # Amber/Orange
    COLOR_HIGH_RISK = (40, 40, 230)   # Red
    COLOR_CYAN = (255, 230, 0)        # Cyan/Teal
    COLOR_WHITE = (255, 255, 255)
    COLOR_BLACK = (0, 0, 0)
    COLOR_DARK_BG = (20, 20, 20)

    def __init__(self, config: Optional[VisionConfig] = None):
        self.config = config or VisionConfig()

    def annotate(
        self,
        frame: np.ndarray,
        result: FrameDensityResult,
        draw_boxes: bool = True,
        draw_grid: bool = True,
        draw_clusters: bool = True,
        draw_hud: bool = True,
    ) -> np.ndarray:
        """
        Overlays all vision telemetry onto the frame.

        :param frame: Raw BGR numpy image
        :param result: Processed FrameDensityResult
        :return: Annotated BGR numpy image
        """
        annotated = frame.copy()

        if draw_grid:
            annotated = self._draw_grid_overlay(annotated, result.grid_density)

        if draw_clusters:
            annotated = self._draw_clusters(annotated, result.clusters)

        if draw_boxes:
            annotated = self._draw_detections(annotated, result.detections)

        if draw_hud:
            annotated = self._draw_hud(annotated, result)

        return annotated

    def _draw_detections(self, frame: np.ndarray, detections: List[BoundingBox]) -> np.ndarray:
        """Draw bounding boxes and centroids for detected humans."""
        for det in detections:
            x1, y1, x2, y2 = int(det.x1), int(det.y1), int(det.x2), int(det.y2)
            cx, cy = int(det.centroid_x), int(det.centroid_y)

            # Box color
            color = self.COLOR_CYAN
            cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)

            # Centroid point
            cv2.circle(frame, (cx, cy), 4, (0, 0, 255), -1)

            # Label (ID and Confidence)
            label = f"#{det.track_id} {det.confidence:.2f}" if det.track_id is not None else f"{det.confidence:.2f}"
            t_size, _ = cv2.getTextSize(label, cv2.FONT_HERSHEY_SIMPLEX, 0.45, 1)
            cv2.rectangle(frame, (x1, y1 - 18), (x1 + t_size[0] + 6, y1), color, -1)
            cv2.putText(
                frame,
                label,
                (x1 + 3, y1 - 4),
                cv2.FONT_HERSHEY_SIMPLEX,
                0.45,
                self.COLOR_BLACK,
                1,
                cv2.LINE_AA,
            )

        return frame

    def _draw_grid_overlay(self, frame: np.ndarray, grid_cells: List[GridCellDensity]) -> np.ndarray:
        """Draw semi-transparent spatial density grid with cell counts."""
        overlay = frame.copy()

        for cell in grid_cells:
            # Color based on risk level
            if cell.risk_level == RiskLevel.HIGH_RISK:
                fill_color = self.COLOR_HIGH_RISK
                alpha = 0.28
            elif cell.risk_level == RiskLevel.CAUTION:
                fill_color = self.COLOR_CAUTION
                alpha = 0.18
            else:
                fill_color = (120, 120, 120)
                alpha = 0.04

            # Draw filled rectangle on overlay
            cv2.rectangle(overlay, (cell.x1, cell.y1), (cell.x2, cell.y2), fill_color, -1)
            # Grid borders
            cv2.rectangle(frame, (cell.x1, cell.y1), (cell.x2, cell.y2), (80, 80, 80), 1)

            # Cell count text in top-left of each cell
            if cell.human_count > 0:
                txt = f"{cell.human_count}"
                cv2.putText(
                    frame,
                    txt,
                    (cell.x1 + 8, cell.y1 + 22),
                    cv2.FONT_HERSHEY_SIMPLEX,
                    0.55,
                    self.COLOR_WHITE if not cell.is_hotspot else self.COLOR_HIGH_RISK,
                    2 if cell.is_hotspot else 1,
                    cv2.LINE_AA,
                )

        # Blend overlay
        cv2.addWeighted(overlay, 0.5, frame, 0.5, 0, frame)
        return frame

    def _draw_clusters(self, frame: np.ndarray, clusters: List[CrowdCluster]) -> np.ndarray:
        """Draw crowd cluster boundaries."""
        for c in clusters:
            cx, cy = int(c.center_x), int(c.center_y)
            r = int(c.radius)

            color = self.COLOR_HIGH_RISK if c.severity == RiskLevel.HIGH_RISK else self.COLOR_CAUTION
            cv2.circle(frame, (cx, cy), r, color, 2, cv2.LINE_AA)
            cv2.putText(
                frame,
                f"{c.id}: {c.human_count}p",
                (cx - 30, cy - r - 6),
                cv2.FONT_HERSHEY_SIMPLEX,
                0.45,
                color,
                1,
                cv2.LINE_AA,
            )
        return frame

    def _draw_hud(self, frame: np.ndarray, result: FrameDensityResult) -> np.ndarray:
        """Render upper HUD dashboard with risk telemetry and status badges."""
        h, w = frame.shape[:2]
        hud_h = 70
        hud_bg = np.zeros((hud_h, w, 3), dtype=np.uint8)

        # Risk badge color
        score = result.risk_score.score
        level = result.risk_score.level
        if level == RiskLevel.HIGH_RISK:
            badge_color = self.COLOR_HIGH_RISK
        elif level == RiskLevel.CAUTION:
            badge_color = self.COLOR_CAUTION
        else:
            badge_color = self.COLOR_SAFE

        # Background bar
        cv2.rectangle(frame, (0, 0), (w, hud_h), (20, 20, 25), -1)
        cv2.line(frame, (0, hud_h), (w, hud_h), badge_color, 2)

        # Left: Crowd & Density Metrics
        cv2.putText(
            frame,
            f"HUMANS: {result.summary.total_humans}  |  HOTSPOTS: {result.summary.hotspot_count}  |  CLUSTERS: {len(result.clusters)}",
            (16, 26),
            cv2.FONT_HERSHEY_SIMPLEX,
            0.55,
            self.COLOR_WHITE,
            1,
            cv2.LINE_AA,
        )

        # Subtitle: Movement Telemetry
        v_str = f"Avg Vel: {result.movement.avg_velocity_px:.1f}px  |  Accel Var: {result.movement.accel_variance:.2f}"
        if result.movement.bottleneck_detected:
            v_str += "  [BOTTLENECK!]"
        if result.movement.surge_detected:
            v_str += "  [SURGE!]"

        cv2.putText(
            frame,
            v_str,
            (16, 52),
            cv2.FONT_HERSHEY_SIMPLEX,
            0.45,
            (200, 200, 200) if not (result.movement.bottleneck_detected or result.movement.surge_detected) else self.COLOR_HIGH_RISK,
            1,
            cv2.LINE_AA,
        )

        # Right: Risk Score Gauge Box
        box_w = 200
        box_x = w - box_w - 16
        cv2.rectangle(frame, (box_x, 10), (box_x + box_w, hud_h - 10), badge_color, -1)
        score_text = f"RISK: {score:.1f}/10 ({level.value})"
        t_size, _ = cv2.getTextSize(score_text, cv2.FONT_HERSHEY_SIMPLEX, 0.5, 2)
        tx = box_x + (box_w - t_size[0]) // 2
        cv2.putText(
            frame,
            score_text,
            (tx, 40),
            cv2.FONT_HERSHEY_SIMPLEX,
            0.5,
            self.COLOR_WHITE,
            2,
            cv2.LINE_AA,
        )

        # If Critical Alert threshold met (Score >= 8.0), flash warning banner
        if score >= self.config.risk_critical_alert:
            alert_bar_y = hud_h + 30
            cv2.rectangle(frame, (w // 4, hud_h + 8), (3 * w // 4, alert_bar_y), self.COLOR_HIGH_RISK, -1)
            cv2.putText(
                frame,
                "CRITICAL ALERT: STAMPEDE RISK DETECTED",
                (w // 4 + 20, alert_bar_y - 8),
                cv2.FONT_HERSHEY_SIMPLEX,
                0.55,
                self.COLOR_WHITE,
                2,
                cv2.LINE_AA,
            )

        return frame
