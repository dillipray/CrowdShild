"""
CrowdVisionPipeline: Complete End-to-End Vision Pipeline.
Ingests video frames, runs human detection strictly on Class 0, computes spatial
density, movement velocity/acceleration variance, and evaluates deterministic risk.
"""

from typing import List, Generator, Tuple, Optional, Callable, Union
import time
import cv2
import numpy as np

from vision.config import VisionConfig
from vision.models import FrameDensityResult
from vision.detector import HumanDetector
from vision.density import DensityCalculator
from vision.movement import MovementTracker
from vision.risk_engine import VisionRiskCalculator
from vision.annotator import VisionAnnotator


class CrowdVisionPipeline:
    """
    High-level Computer Vision Engine for CrowdShield.
    """

    def __init__(self, config: Optional[VisionConfig] = None):
        self.config = config or VisionConfig()
        self.detector = HumanDetector(self.config)
        self.density_calculator = DensityCalculator(self.config)
        self.movement_tracker = MovementTracker(self.config)
        self.risk_calculator = VisionRiskCalculator(self.config)
        self.annotator = VisionAnnotator(self.config)

        self._frame_count = 0
        self._last_frame_time = time.perf_counter()

        # Optional telemetry sink: any callable that accepts FrameDensityResult.
        # Register AsyncPostGISStorage.enqueue (or any other sink) here.
        # The sink is called synchronously but MUST be non-blocking on the caller
        # side (e.g. asyncio.Queue.put_nowait) so the vision loop is never stalled.
        self.telemetry_sink: Optional[Callable[["FrameDensityResult"], None]] = None

    def process_frame(
        self,
        frame: np.ndarray,
        timestamp_ms: Optional[int] = None,
        annotate: bool = False,
    ) -> Union[FrameDensityResult, Tuple[FrameDensityResult, np.ndarray]]:
        """
        Process a single video frame.

        :param frame: Raw BGR numpy image frame
        :param timestamp_ms: Timestamp in milliseconds (default: current time)
        :param annotate: If True, returns (FrameDensityResult, annotated_frame_bgr)
        :return: FrameDensityResult or tuple of (result, annotated_frame)
        """
        if frame is None or frame.size == 0:
            raise ValueError("Invalid empty video frame provided to pipeline.")

        self._frame_count += 1
        now = time.perf_counter()
        dt = now - self._last_frame_time
        fps = (1.0 / dt) if dt > 0 else 0.0
        self._last_frame_time = now

        if timestamp_ms is None:
            timestamp_ms = int(time.time() * 1000)

        h, w = frame.shape[:2]

        # 1. Detect and track humans strictly (class 0)
        detections = self.detector.track(frame)

        # 2. Compute spatial grid density
        grid_density = self.density_calculator.compute_grid_density(
            detections=detections,
            frame_width=w,
            frame_height=h,
        )

        # 3. Compute spatial crowd clusters
        clusters = self.density_calculator.compute_clusters(detections=detections)

        # 4. Summarize density statistics
        summary = self.density_calculator.summarize(
            detections=detections,
            grid_cells=grid_density,
            clusters=clusters,
        )

        # 5. Track crowd movement and acceleration variance
        movement = self.movement_tracker.update(
            detections=detections,
            timestamp_ms=timestamp_ms,
            total_humans=summary.total_humans,
        )

        # 6. Evaluate deterministic risk score (matching Developer 1 engine specifications)
        risk_score = self.risk_calculator.evaluate_from_metrics(
            summary=summary,
            movement=movement,
        )

        # 7. Assemble structured telemetry result
        result = FrameDensityResult(
            frame_index=self._frame_count,
            timestamp_ms=timestamp_ms,
            frame_width=w,
            frame_height=h,
            fps=round(fps, 1),
            summary=summary,
            detections=detections,
            grid_density=grid_density,
            clusters=clusters,
            movement=movement,
            risk_score=risk_score,
            metadata={"device": self.config.device, "model": self.config.model_name},
        )

        should_log = self._should_log_telemetry(result)

        if annotate:
            annotated_frame = self.annotator.annotate(frame, result)
            # Fire telemetry sink (non-blocking enqueue to PostGIS worker) if frame meets throttling policy
            if self.telemetry_sink is not None and should_log:
                try:
                    self.telemetry_sink(result)
                except Exception:
                    pass  # Sink errors must NEVER crash the vision loop
            return result, annotated_frame

        # Fire telemetry sink (non-blocking enqueue to PostGIS worker) if frame meets throttling policy
        if self.telemetry_sink is not None and should_log:
            try:
                self.telemetry_sink(result)
            except Exception:
                pass  # Sink errors must NEVER crash the vision loop

        return result

    def _should_log_telemetry(self, result: FrameDensityResult) -> bool:
        """
        Determines whether the current frame should be persisted to the database sink.
        Decouples 30 FPS CV / WebSocket rate from throttled database logging (e.g. 5 FPS / every Nth frame).
        Bypasses throttling immediately if emergency risk or panic surge is detected.
        """
        interval = max(1, self.config.postgres_log_interval_frames)
        is_stride_frame = (self._frame_count % interval == 0)
        is_critical_event = (
            self.config.postgres_force_log_on_critical
            and (
                result.risk_score.score >= self.config.risk_critical_alert
                or result.movement.surge_detected
            )
        )
        return is_stride_frame or is_critical_event

    def process_video_stream(
        self,
        source: Union[int, str],
        max_frames: Optional[int] = None,
        annotate: bool = False,
    ) -> Generator[Union[FrameDensityResult, Tuple[FrameDensityResult, np.ndarray]], None, None]:
        """
        Stream and process frames from a video file, webcam index, or RTSP URL.

        :param source: Video file path, RTSP stream URL, or webcam device integer (e.g. 0)
        :param max_frames: Optional limit on number of frames to process
        :param annotate: If True, yields (FrameDensityResult, annotated_frame)
        """
        cap = cv2.VideoCapture(source)
        if not cap.isOpened():
            raise IOError(f"Cannot open video source: {source}")

        frame_idx = 0
        try:
            while cap.isOpened():
                ret, frame = cap.read()
                if not ret:
                    break

                frame_idx += 1
                result = self.process_frame(frame, annotate=annotate)
                yield result

                if max_frames is not None and frame_idx >= max_frames:
                    break
        finally:
            cap.release()
