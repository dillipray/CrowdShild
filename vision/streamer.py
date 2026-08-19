"""
Continuous Video & Telemetry Streamer for CrowdShield.
Ingests frames from video sources (webcam, video files, RTSP, or synthetic simulation),
processes them with CrowdVisionPipeline, and yields telemetry + encoded frames.
"""

import os
import asyncio
import base64
import time
import math
import cv2
import numpy as np
from typing import Optional, AsyncGenerator, Tuple, Dict, Any, Set
from fastapi import WebSocket

from vision.config import VisionConfig
from vision.models import FrameDensityResult
from vision.pipeline import CrowdVisionPipeline
from vision.telemetry_db import AsyncPostGISStorage
import logging

logger = logging.getLogger("CrowdShield.Streamer")


class VisionStreamManager:
    """
    Manages live camera capture, video file streaming, synthetic crowd simulation, and WebSocket broadcasts.
    """

    def __init__(self, config: Optional[VisionConfig] = None):
        self.config = config or VisionConfig()
        self.pipeline = CrowdVisionPipeline(self.config)
        self.active_connections: Set[WebSocket] = set()
        self.mobile_connections: Set[WebSocket] = set()
        self.dashboard_connections: Set[WebSocket] = set()
        self.is_running = False
        
        # Source mode: "video", "camera", or "simulation"
        self.source_mode = self.config.source_mode
        self.video_path = self.config.video_source
        self.camera_index = 0
        self.video_cap: Optional[cv2.VideoCapture] = None
        
        # Initialize video capture if configured
        if self.source_mode == "video" and self.video_path:
            self._open_video_capture(self.video_path)
        elif self.source_mode == "camera":
            self.video_cap = cv2.VideoCapture(self.camera_index)
            if not self.video_cap.isOpened():
                logger.warning(f"Could not open camera {self.camera_index}. Defaulting to simulation.")
                self.source_mode = "simulation"

        self.latest_result: Optional[FrameDensityResult] = None
        self.latest_frame_bgr: Optional[np.ndarray] = None
        self.latest_annotated_bgr: Optional[np.ndarray] = None
        self.sim_tick = 0
        self.lock = asyncio.Lock()

        # Async PostGIS telemetry storage — connected/disconnected in run_stream_loop
        self.telemetry_storage: Optional[AsyncPostGISStorage] = (
            AsyncPostGISStorage(self.config) if self.config.postgres_enabled else None
        )
        # Wire the non-blocking enqueue as the pipeline's telemetry sink
        if self.telemetry_storage is not None:
            self.pipeline.telemetry_sink = self.telemetry_storage.enqueue

    def _open_video_capture(self, video_path: str) -> bool:
        """Internal helper to open a video capture source."""
        if self.video_cap and self.video_cap.isOpened():
            self.video_cap.release()
        
        # Handle relative or absolute paths
        target_path = video_path
        if not os.path.isabs(target_path) and not os.path.exists(target_path):
            alt_path = os.path.join(os.path.dirname(os.path.dirname(__file__)), target_path)
            if os.path.exists(alt_path):
                target_path = alt_path

        self.video_cap = cv2.VideoCapture(target_path)
        if self.video_cap.isOpened():
            self.video_path = video_path
            self.source_mode = "video"
            logger.info(f"[VisionStreamManager] Successfully opened video file source: '{target_path}'")
            return True
        else:
            logger.warning(f"[VisionStreamManager] Could not open video file: '{video_path}'. Falling back to simulation.")
            self.source_mode = "simulation"
            self.video_cap = None
            return False

    async def connect_client(self, websocket: WebSocket, client_type: str = "web"):
        """Register a new WebSocket client."""
        await websocket.accept()
        self.active_connections.add(websocket)
        if client_type == "mobile":
            self.mobile_connections.add(websocket)
        else:
            self.dashboard_connections.add(websocket)

    def disconnect_client(self, websocket: WebSocket):
        """Unregister a disconnected WebSocket client."""
        self.active_connections.discard(websocket)
        self.mobile_connections.discard(websocket)
        self.dashboard_connections.discard(websocket)

    async def broadcast_telemetry(self, result: FrameDensityResult, frame_bgr: Optional[np.ndarray] = None):
        """
        Broadcast structured telemetry to mobile and dashboard clients.
        Formats payload tailored for com.crowdshield.stampede mobile and Web Command Dashboard.
        """
        self.latest_result = result
        
        # 1. Prepare Mobile App Payload (aligned with com.crowdshield.stampede domain models)
        mobile_payload = {
            "type": "CROWD_TELEMETRY",
            "package": "com.crowdshield.stampede",
            "timestamp": result.timestamp_ms,
            "sourceMode": self.source_mode,
            "videoSource": os.path.basename(self.video_path) if self.video_path else "Live Stream",
            "riskScore": {
                "score": result.risk_score.score,
                "level": result.risk_score.level.value,
                "breakdown": {
                    "densityFactor": result.risk_score.breakdown.density_factor if result.risk_score.breakdown else 0.0,
                    "velocityFactor": result.risk_score.breakdown.velocity_factor if result.risk_score.breakdown else 0.0,
                    "accelFactor": result.risk_score.breakdown.accel_variance_factor if result.risk_score.breakdown else 0.0,
                }
            },
            "crowdMetrics": {
                "totalHumans": result.summary.total_humans,
                "hotspotsCount": result.summary.hotspot_count,
                "avgVelocity": result.movement.avg_velocity_px,
                "accelVariance": result.movement.accel_variance,
                "isBottleneck": result.movement.bottleneck_detected,
                "isSurge": result.movement.surge_detected,
            },
            "clusters": [
                {
                    "id": c.id,
                    "centerLat": 28.6139 + (c.center_y - 240) * 0.00001,
                    "centerLng": 77.2090 + (c.center_x - 320) * 0.00001,
                    "radius": c.radius,
                    "density": c.density,
                    "severity": c.severity.value,
                }
                for c in result.clusters
            ],
            "alert": {
                "isCritical": result.risk_score.score >= self.config.risk_critical_alert,
                "title": "CRITICAL RISK ALERT" if result.risk_score.score >= self.config.risk_critical_alert else "NORMAL",
                "message": (
                    f"Risk Score: {result.risk_score.score:.1f} ({result.risk_score.level.value}). "
                    + ("Bottleneck detected! " if result.movement.bottleneck_detected else "")
                    + ("Panic surge variance spike! " if result.movement.surge_detected else "")
                ),
            }
        }

        # 2. Prepare Dashboard Payload (Full Vision & Grid Detail)
        dashboard_payload = {
            "type": "DASHBOARD_TELEMETRY",
            "frameIndex": result.frame_index,
            "timestamp": result.timestamp_ms,
            "fps": result.fps,
            "sourceMode": self.source_mode,
            "videoSource": os.path.basename(self.video_path) if self.video_path else "Simulation",
            "summary": result.summary.model_dump(),
            "detectionsCount": len(result.detections),
            "detections": [d.model_dump() for d in result.detections[:30]],  # Cap for payload efficiency
            "gridDensity": [g.model_dump() for g in result.grid_density],
            "clusters": [c.model_dump() for c in result.clusters],
            "movement": result.movement.model_dump(),
            "riskScore": result.risk_score.model_dump(),
            "clientStats": {
                "mobileClients": len(self.mobile_connections),
                "dashboardClients": len(self.dashboard_connections),
            }
        }

        # Send to mobile clients
        for ws in list(self.mobile_connections):
            try:
                await ws.send_json(mobile_payload)
            except Exception:
                self.disconnect_client(ws)

        # Send to web dashboard clients
        for ws in list(self.dashboard_connections):
            try:
                await ws.send_json(dashboard_payload)
            except Exception:
                self.disconnect_client(ws)

    def generate_synthetic_crowd_frame(self, w: int = 640, h: int = 480) -> np.ndarray:
        """
        Generates realistic synthetic video frames with moving human silhouettes
        simulating varying crowd densities, stampede bottlenecking, and panic surges.
        """
        self.sim_tick += 1
        frame = np.full((h, w, 3), (30, 30, 35), dtype=np.uint8)

        # Draw walking surface perspective grid
        for gy in range(80, h, 40):
            cv2.line(frame, (0, gy), (w, gy), (45, 45, 52), 1)
        for gx in range(0, w, 60):
            cv2.line(frame, (gx, 80), (gx, h), (45, 45, 52), 1)

        # Periodic cycle: 0-100 ticks Normal, 100-200 ticks Growing Density, 200-300 ticks Surge/Bottleneck
        cycle = self.sim_tick % 300
        if cycle < 100:
            num_people = 6
            panic = False
            base_speed = 3.0
        elif cycle < 200:
            num_people = 14
            panic = False
            base_speed = 1.2
        else:
            num_people = 22
            panic = True
            base_speed = 0.4

        # Generate clustered humans
        center_x = w // 2 + int(math.sin(self.sim_tick * 0.05) * 40)
        center_y = h // 2 + int(math.cos(self.sim_tick * 0.05) * 30)

        for i in range(num_people):
            angle = (i * (2 * math.pi / num_people)) + (self.sim_tick * 0.02)
            spread = 30 + (i * 8) if not panic else (20 + (i * 4))
            px = int(center_x + math.cos(angle) * spread + math.sin(i + self.sim_tick * 0.1) * 10)
            py = int(center_y + math.sin(angle) * spread * 0.7 + math.cos(i + self.sim_tick * 0.1) * 8)
            px = max(40, min(w - 40, px))
            py = max(90, min(h - 90, py))

            # Draw a realistic human silhouette representation (head + torso + legs)
            head_radius = 8
            torso_w, torso_h = 16, 26
            leg_h = 20

            # Head
            cv2.circle(frame, (px, py - torso_h // 2 - head_radius), head_radius, (220, 200, 180), -1)
            # Torso
            cv2.rectangle(frame, (px - torso_w // 2, py - torso_h // 2), (px + torso_w // 2, py + torso_h // 2), (180, 120, 70), -1)
            # Legs
            cv2.line(frame, (px - 4, py + torso_h // 2), (px - 6, py + torso_h // 2 + leg_h), (80, 60, 50), 3)
            cv2.line(frame, (px + 4, py + torso_h // 2), (px + 6, py + torso_h // 2 + leg_h), (80, 60, 50), 3)

        return frame

    async def run_stream_loop(self, fps: int = 15):
        """
        Continuous streaming worker loop.
        Processes video frames from the active source (video file, camera, or simulation),
        runs YOLO analysis, renders annotations, enqueues DB telemetry, and broadcasts over WebSockets.
        """
        self.is_running = True
        interval = 1.0 / max(1, fps)

        # Connect PostGIS storage pool (no-op when postgres_enabled=False)
        if self.telemetry_storage is not None:
            try:
                await self.telemetry_storage.connect()
            except Exception as exc:
                logger.error("[VisionStreamManager] Failed to connect PostGIS telemetry storage: %s", exc)

        while self.is_running:
            start_t = time.perf_counter()
            frame = None

            if self.source_mode in ("video", "camera"):
                if self.video_cap is not None and self.video_cap.isOpened():
                    ret, raw_frame = self.video_cap.read()
                    if not ret:
                        if self.source_mode == "video":
                            # Seamless loop back to the beginning of the video file
                            self.video_cap.set(cv2.CAP_PROP_POS_FRAMES, 0)
                            ret, raw_frame = self.video_cap.read()
                            if not ret:
                                # Reopen file if seeking fails
                                self._open_video_capture(self.video_path)
                                if self.video_cap and self.video_cap.isOpened():
                                    ret, raw_frame = self.video_cap.read()
                        if ret and raw_frame is not None:
                            frame = raw_frame
                        else:
                            frame = self.generate_synthetic_crowd_frame()
                    else:
                        frame = raw_frame
                else:
                    # Try to re-initialize video source if closed
                    if self.source_mode == "video" and self.video_path:
                        self._open_video_capture(self.video_path)
                    frame = self.generate_synthetic_crowd_frame()
            else:
                frame = self.generate_synthetic_crowd_frame()

            if frame is None or frame.size == 0:
                frame = self.generate_synthetic_crowd_frame()

            self.latest_frame_bgr = frame

            # Process through vision pipeline with annotations
            result, annotated = self.pipeline.process_frame(frame, annotate=True)
            self.latest_annotated_bgr = annotated

            # Broadcast live telemetry to active WebSockets
            if self.active_connections:
                await self.broadcast_telemetry(result, frame)

            elapsed = time.perf_counter() - start_t
            sleep_time = max(0.001, interval - elapsed)
            await asyncio.sleep(sleep_time)

        # Drain and close PostGIS pool on loop exit
        if self.telemetry_storage is not None:
            await self.telemetry_storage.disconnect()

    def set_source_video(self, video_path: str) -> bool:
        """Switch stream source to a local video file (e.g. 'videos/large_crowd.mp4')."""
        return self._open_video_capture(video_path)

    def set_source_camera(self, camera_index: int = 0):
        """Switch stream source to a physical camera index."""
        if self.video_cap and self.video_cap.isOpened():
            self.video_cap.release()
        self.camera_index = camera_index
        self.video_cap = cv2.VideoCapture(camera_index)
        self.source_mode = "camera" if self.video_cap.isOpened() else "simulation"

    def set_source_simulation(self):
        """Switch stream source to simulation generator."""
        if self.video_cap and self.video_cap.isOpened():
            self.video_cap.release()
            self.video_cap = None
        self.source_mode = "simulation"
