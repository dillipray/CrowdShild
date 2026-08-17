"""
Human Detection and Tracking Engine using YOLO.
Strictly isolates and processes class 0 (person/human).
Explicitly targets CUDA GPU acceleration & TensorRT (yolov8n.engine).
"""

import os
import logging
from typing import List, Tuple, Optional
import numpy as np
import cv2
import torch
from ultralytics import YOLO

from vision.config import VisionConfig
from vision.models import BoundingBox

logger = logging.getLogger("CrowdShield.VisionDetector")


class HumanDetector:
    """
    Dedicated Human Detector strictly configured for Class 0 (person).
    Explicitly targets CUDA / TensorRT GPU execution with ByteTrack multi-person tracking.
    """

    def __init__(self, config: Optional[VisionConfig] = None):
        self.config = config or VisionConfig()
        
        # Explicit CUDA configuration with runtime validation
        if self.config.device == "cuda":
            if torch.cuda.is_available():
                self.device = "cuda"
                self.use_half = self.config.half_precision
                logger.info(f"[CUDA] GPU acceleration active on {torch.cuda.get_device_name(0)}")
            else:
                self.device = "cpu"
                self.use_half = False
                logger.warning("[CUDA] 'cuda' requested, but PyTorch runtime is currently CPU build. Defaulting to CPU.")
        else:
            self.device = self.config.device
            self.use_half = self.config.half_precision if self.device != "cpu" else False
        
        # Check if TensorRT engine exists or load configured model
        if os.path.exists("yolov8n.engine") and not self.config.model_name.endswith(".engine"):
            logger.info("Found TensorRT engine 'yolov8n.engine'. Loading for maximum GPU throughput.")
            self.model_name = "yolov8n.engine"
        else:
            self.model_name = self.config.model_name

        try:
            self.model = YOLO(self.model_name)
        except Exception as e:
            logger.warning(f"Could not load '{self.model_name}' on {self.device}: {e}. Falling back to standard yolov8n.pt")
            self.model_name = "yolov8n.pt"
            self.model = YOLO(self.model_name)

        # Verify human class exists in model class list (COCO index 0 is 'person')
        if hasattr(self.model, "names") and 0 in self.model.names:
            self.class_name = self.model.names[0]
        else:
            self.class_name = "person"

    def detect(self, frame: np.ndarray) -> List[BoundingBox]:
        """
        Run inference on a single frame, strictly filtering for human class (0).

        :param frame: BGR image frame (numpy ndarray)
        :return: List of structured BoundingBox instances
        """
        if frame is None or frame.size == 0:
            return []

        # Inference strictly filtered for human class_id on target device.
        # `half` is intentionally omitted — Ultralytics manages FP16 precision
        # automatically based on the device. Passing half=False explicitly on
        # CPU can trigger a UserWarning on newer Ultralytics versions.
        results = self.model.predict(
            source=frame,
            classes=[self.config.human_class_id],
            conf=self.config.conf_threshold,
            iou=self.config.iou_threshold,
            device=self.device,
            imgsz=self.config.img_size,
            verbose=False,
        )

        return self._parse_results(results[0])

    def track(self, frame: np.ndarray) -> List[BoundingBox]:
        """
        Run tracking on sequential video frames with persistent track IDs.
        Strictly filters for human class (0) on target device.

        :param frame: BGR image frame (numpy ndarray)
        :return: List of structured BoundingBox instances with track_id populated
        """
        if frame is None or frame.size == 0:
            return []

        if not self.config.enable_tracking:
            return self.detect(frame)

        # `half` is intentionally omitted — Ultralytics manages FP16 precision
        # automatically based on the device. Passing half=False explicitly on
        # CPU can trigger a UserWarning on newer Ultralytics versions.
        results = self.model.track(
            source=frame,
            classes=[self.config.human_class_id],
            conf=self.config.conf_threshold,
            iou=self.config.iou_threshold,
            tracker=self.config.tracker_type,
            persist=True,
            device=self.device,
            imgsz=self.config.img_size,
            verbose=False,
        )

        return self._parse_results(results[0])

    def export_tensorrt(self, output_path: str = "yolov8n.engine", half: bool = True) -> str:
        """
        Exports the underlying YOLO model to TensorRT (.engine) format for NVIDIA GPU acceleration.
        """
        logger.info(f"Converting YOLO model to TensorRT engine (half={half}, device='cuda')...")
        exported = self.model.export(
            format="engine",
            half=half,
            device="cuda" if torch.cuda.is_available() else "0",
            imgsz=self.config.img_size,
        )
        return exported

    def _parse_results(self, result) -> List[BoundingBox]:
        """Convert Ultralytics YOLO output tensors into structured BoundingBox models."""
        boxes = result.boxes
        if boxes is None or len(boxes) == 0:
            return []

        xyxy = boxes.xyxy.cpu().numpy()
        confs = boxes.conf.cpu().numpy()
        track_ids = boxes.id.int().cpu().numpy() if boxes.id is not None else [None] * len(boxes)

        bounding_boxes: List[BoundingBox] = []
        for i in range(len(xyxy)):
            x1, y1, x2, y2 = float(xyxy[i][0]), float(xyxy[i][1]), float(xyxy[i][2]), float(xyxy[i][3])
            w = max(0.0, x2 - x1)
            h = max(0.0, y2 - y1)
            cx = x1 + (w / 2.0)
            cy = y1 + (h / 2.0)
            conf = float(confs[i])
            tid = int(track_ids[i]) if track_ids[i] is not None else None

            bbox = BoundingBox(
                x1=round(x1, 2),
                y1=round(y1, 2),
                x2=round(x2, 2),
                y2=round(y2, 2),
                confidence=round(conf, 3),
                track_id=tid,
                centroid_x=round(cx, 2),
                centroid_y=round(cy, 2),
                width=round(w, 2),
                height=round(h, 2),
                area=round(w * h, 2),
            )
            bounding_boxes.append(bbox)

        return bounding_boxes
