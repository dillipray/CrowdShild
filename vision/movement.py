"""
Crowd Movement, Velocity Tracking, and Acceleration Variance Engine.
Estimates movement vectors, velocity bottlenecks, and stampede panic surges.
"""

from typing import List, Dict, Tuple, Optional
from collections import deque
import numpy as np

from vision.config import VisionConfig
from vision.models import BoundingBox, MovementMetrics


class TrackHistoryItem:
    def __init__(self, x: float, y: float, timestamp_ms: int):
        self.x = x
        self.y = y
        self.timestamp_ms = timestamp_ms


class MovementTracker:
    """
    Maintains motion trajectories for tracked humans, computing average velocities,
    acceleration variances, bottleneck conditions, and surge anomalies.
    """

    def __init__(self, config: Optional[VisionConfig] = None):
        self.config = config or VisionConfig()
        # Mapping: track_id -> deque of (x, y, timestamp_ms)
        self.tracks: Dict[int, deque] = {}
        # Mapping: track_id -> previous velocity (px/frame)
        self.prev_velocities: Dict[int, float] = {}

    def update(
        self,
        detections: List[BoundingBox],
        timestamp_ms: int,
        total_humans: int,
    ) -> MovementMetrics:
        """
        Updates movement trajectories and calculates motion metrics.

        :param detections: Current frame human bounding boxes with track_ids
        :param timestamp_ms: Current timestamp in milliseconds
        :param total_humans: Total human count in current frame
        :return: MovementMetrics with velocity and acceleration variance
        """
        current_track_ids = set()
        velocities: List[float] = []
        accelerations: List[float] = []

        for det in detections:
            tid = det.track_id
            if tid is None:
                continue

            current_track_ids.add(tid)

            if tid not in self.tracks:
                self.tracks[tid] = deque(maxlen=self.config.track_history_length)

            history = self.tracks[tid]
            history.append(TrackHistoryItem(det.centroid_x, det.centroid_y, timestamp_ms))

            if len(history) >= 2:
                p_curr = history[-1]
                p_prev = history[-2]

                # Displacement in pixels
                dx = p_curr.x - p_prev.x
                dy = p_curr.y - p_prev.y
                dist = float(np.hypot(dx, dy))
                dt = max((p_curr.timestamp_ms - p_prev.timestamp_ms) / 1000.0, 0.001)

                v = dist / dt  # pixels per second (or px displacement)
                v_frame = dist  # px per frame
                velocities.append(v_frame)

                # Compute acceleration if previous velocity exists
                if tid in self.prev_velocities:
                    v_prev = self.prev_velocities[tid]
                    accel = abs(v_frame - v_prev)
                    accelerations.append(accel)

                self.prev_velocities[tid] = v_frame

        # Clean up tracks that disappeared
        active_ids = list(self.tracks.keys())
        for tid in active_ids:
            if tid not in current_track_ids:
                del self.tracks[tid]
                if tid in self.prev_velocities:
                    del self.prev_velocities[tid]

        # Compute aggregate movement statistics
        avg_v = float(np.mean(velocities)) if velocities else 0.0
        max_v = float(np.max(velocities)) if velocities else 0.0
        accel_var = float(np.var(accelerations)) if len(accelerations) >= 2 else 0.0

        # Bottleneck detection: high density but stagnant movement
        bottleneck = (
            total_humans >= self.config.bottleneck_density_min
            and avg_v < self.config.bottleneck_velocity_max
        )

        # Surge / panic detection: high acceleration variance indicating sudden agitation
        surge = accel_var >= self.config.surge_accel_var_threshold

        return MovementMetrics(
            avg_velocity_px=round(avg_v, 2),
            max_velocity_px=round(max_v, 2),
            accel_variance=round(accel_var, 3),
            bottleneck_detected=bottleneck,
            surge_detected=surge,
            tracked_count=len(current_track_ids),
        )

    def get_track_trail(self, track_id: int) -> List[Tuple[int, int]]:
        """Returns the recent (x, y) trail points for a given track ID for visualization."""
        if track_id not in self.tracks:
            return []
        return [(int(item.x), int(item.y)) for item in self.tracks[track_id]]
