import os
import torch
import numpy as np
from dataclasses import dataclass, field
from typing import Tuple, Optional, Sequence, List, Union

try:
    from dotenv import load_dotenv
    load_dotenv()
except ImportError:
    pass


def _parse_camera_id(val: Union[str, int, None]) -> int:
    """Safely parse camera index/ID string (e.g. '0', 'Gate-1' -> 1)."""
    if val is None:
        return 0
    if isinstance(val, int):
        return val
    try:
        return int(val)
    except (ValueError, TypeError):
        digits = "".join(filter(str.isdigit, str(val)))
        return int(digits) if digits else 0


@dataclass
class VisionConfig:
    # Model Configuration: Supports TensorRT (.engine), ONNX (.onnx), or PyTorch (.pt)
    model_name: str = "yolov8n.engine" if os.path.exists("yolov8n.engine") else "yolov8n.pt"
    human_class_id: int = 0         # STRICT: Only class 0 ('person' in COCO dataset)
    conf_threshold: float = 0.35    # Confidence threshold for human detection
    iou_threshold: float = 0.45     # NMS IOU threshold
    device: str = "cuda"            # Explicitly configured for NVIDIA GPU acceleration
    half_precision: bool = True     # FP16 precision for accelerated TensorRT/CUDA inference
    img_size: int = 640

    # Tracking Configuration
    enable_tracking: bool = True
    tracker_type: str = "bytetrack.yaml"  # Built-in ByteTrack for accurate multi-person tracking
    track_history_length: int = 30        # Number of frames to retain centroid history

    # Spatial Grid Density Configuration
    grid_rows: int = 4
    grid_cols: int = 4
    hotspot_ratio_threshold: float = 1.6  # If cell density > 1.6 * average cell density -> Hotspot
    min_hotspot_count: int = 3           # Minimum humans in a single cell to qualify as hotspot

    # Cluster Analysis (DBSCAN / Spatial grouping)
    cluster_eps_px: float = 110.0        # Max pixel distance between individuals in a cluster
    cluster_min_samples: int = 3         # Min individuals to form a distinct crowd cluster

    # Movement & Velocity Bottleneck Parameters
    bottleneck_density_min: int = 6      # Minimum crowd count in proximity for bottleneck evaluation
    bottleneck_velocity_max: float = 2.0 # Maximum velocity (px/frame) to trigger bottleneck flag
    surge_accel_var_threshold: float = 4.0 # Acceleration variance indicating panic surge

    # Mathematical Risk Engine Parameters (Aligned with Developer 1 rules)
    risk_density_weight: float = 1.2     # densityFactor = min(density * 1.2, 6.0)
    risk_max_density_score: float = 6.0
    risk_velocity_penalty: float = 2.0   # If bottleneck detected -> +2.0
    risk_accel_weight: float = 0.5       # accelFactor = min(accelVariance * 0.5, 2.0)
    risk_max_accel_score: float = 2.0
    risk_caution_threshold: float = 4.0  # > 4.0 -> CAUTION
    risk_high_threshold: float = 7.5     # > 7.5 -> HIGH_RISK
    risk_critical_alert: float = 8.0     # >= 8.0 -> Trigger High-Priority System Notification

    # -------------------------------------------------------------------------
    # Camera & Location Metadata
    # Identifies the physical camera source and geographic deployment site.
    # These fields are persisted in every DB telemetry row.
    # -------------------------------------------------------------------------
    camera_id: int = _parse_camera_id(os.environ.get("CROWDSHIELD_CAMERA_ID", "0"))
    # ^ Camera index / device number (e.g. 0=main gate, 1=platform A, 2=exit corridor)
    location_name: str = os.environ.get("CROWDSHIELD_LOCATION", "DEFAULT_LOCATION")
    # ^ Human-readable deployment site label (e.g. "Gate-1", "Platform-A", "Exit-North")

    # Geospatial anchor: WGS84 origin (EPSG:4326) for mapping pixel coords -> real-world lat/lng
    geo_origin_lat: float = float(os.environ.get("CROWDSHIELD_LAT", "28.6139"))
    geo_origin_lng: float = float(os.environ.get("CROWDSHIELD_LNG", "77.2090"))
    # Pixel-to-degree scale factors (nominal camera FOV parameters)
    geo_px_to_lat: float = 0.00001   # 1 pixel ~ 1.11 m latitude offset
    geo_px_to_lng: float = 0.00001   # 1 pixel ~ 1.11 m longitude offset
    geo_frame_cx: float = 320.0      # Nominal frame center X (pixels) for coord projection
    geo_frame_cy: float = 240.0      # Nominal frame center Y (pixels) for coord projection

    # 3x3 Projective Transformation Matrix (Planar Homography)
    # Maps homogeneous pixel coordinates [u, v, 1]^T -> [lng', lat', w']^T -> (EPSG:4326)
    homography_matrix: Optional[Tuple[Tuple[float, float, float], Tuple[float, float, float], Tuple[float, float, float]]] = None

    # -------------------------------------------------------------------------
    # PostgreSQL / PostGIS Async Telemetry Persistence
    # -------------------------------------------------------------------------
    postgres_enabled: bool = os.environ.get("POSTGRES_ENABLED", "false").lower() == "true"
    postgres_dsn: str = os.environ.get(
        "POSTGRES_DSN",
        "postgresql://crowdshield:crowdshield@localhost:5432/crowdshield_db",
    )
    postgres_min_pool: int = 2          # Minimum persistent DB connections in pool
    postgres_max_pool: int = 10         # Maximum concurrent DB connections
    postgres_batch_size: int = 50       # Flush queue to DB after N telemetry frames
    postgres_flush_interval_sec: float = 1.0   # Or flush every T seconds (whichever first)
    postgres_queue_maxsize: int = 1000  # Drop-on-overflow guard; prevents memory bloat

    # Telemetry Rate Throttling (Decouples 30 FPS CV loop from database ingestion rate, e.g. 5 FPS)
    postgres_log_interval_frames: int = int(os.environ.get("POSTGRES_LOG_INTERVAL_FRAMES", "6"))  # Every Nth frame (6 @ 30fps = 5fps)
    postgres_telemetry_target_fps: float = float(os.environ.get("POSTGRES_TELEMETRY_FPS", "5.0"))
    postgres_force_log_on_critical: bool = os.environ.get("POSTGRES_FORCE_LOG_ON_CRITICAL", "true").lower() == "true"

    # -------------------------------------------------------------------------
    # Projective Transformation / Homography Methods
    # -------------------------------------------------------------------------
    def get_homography_matrix(self) -> np.ndarray:
        """
        Return the active 3x3 Homography Matrix as a numpy float64 array.
        If no custom matrix is configured, constructs an analytical affine-projective
        transformation matrix mapping (geo_frame_cx, geo_frame_cy) to (geo_origin_lng, geo_origin_lat).
        """
        if self.homography_matrix is not None:
            return np.array(self.homography_matrix, dtype=np.float64)

        # Default projective/affine mapping in homogeneous coordinates:
        # [lng]   [  s_x   0.0   lng_0 - cx * s_x ] [  u  ]
        # [lat] = [  0.0  -s_y   lat_0 + cy * s_y ] [  v  ]
        # [ 1 ]   [  0.0   0.0          1.0       ] [ 1.0 ]
        sx = float(self.geo_px_to_lng)
        sy = float(self.geo_px_to_lat)
        return np.array([
            [sx, 0.0, float(self.geo_origin_lng) - float(self.geo_frame_cx) * sx],
            [0.0, -sy, float(self.geo_origin_lat) + float(self.geo_frame_cy) * sy],
            [0.0, 0.0, 1.0],
        ], dtype=np.float64)

    def set_homography(self, matrix: Union[Sequence[Sequence[float]], np.ndarray]) -> None:
        """Set a 3x3 Projective Transformation Matrix."""
        arr = np.array(matrix, dtype=np.float64)
        if arr.shape != (3, 3):
            raise ValueError(f"Homography matrix must be of shape (3, 3), got {arr.shape}")
        self.homography_matrix = (
            (float(arr[0, 0]), float(arr[0, 1]), float(arr[0, 2])),
            (float(arr[1, 0]), float(arr[1, 1]), float(arr[1, 2])),
            (float(arr[2, 0]), float(arr[2, 1]), float(arr[2, 2])),
        )

    def set_homography_from_points(
        self,
        src_pixel_pts: Sequence[Tuple[float, float]],
        dst_gps_pts: Sequence[Tuple[float, float]],
    ) -> np.ndarray:
        """
        Compute and configure the 3x3 Homography Matrix from >= 4 point correspondences
        using Direct Linear Transformation (DLT) with SVD.
        
        Args:
            src_pixel_pts: List of (u, v) image pixel coordinates (min 4 points).
            dst_gps_pts: List of corresponding (lng, lat) real-world GPS coordinates in EPSG:4326.
        """
        if len(src_pixel_pts) < 4 or len(dst_gps_pts) < 4:
            raise ValueError("Direct Linear Transformation requires at least 4 point pairs.")
        if len(src_pixel_pts) != len(dst_gps_pts):
            raise ValueError("src_pixel_pts and dst_gps_pts must have the same length.")

        # Construct Direct Linear Transform matrix A (2N x 9)
        A = []
        for (u, v), (x, y) in zip(src_pixel_pts, dst_gps_pts):
            A.append([-u, -v, -1.0, 0.0, 0.0, 0.0, u * x, v * x, x])
            A.append([0.0, 0.0, 0.0, -u, -v, -1.0, u * y, v * y, y])

        A = np.array(A, dtype=np.float64)
        # Solve Ah = 0 via SVD
        _, _, Vt = np.linalg.svd(A)
        h = Vt[-1]  # eigenvector corresponding to smallest singular value
        H = h.reshape((3, 3))
        if abs(H[2, 2]) > 1e-12:
            H = H / H[2, 2]

        self.set_homography(H)
        return H

    def pixel_to_gps(self, px: float, py: float) -> Tuple[float, float]:
        """
        Project camera view coordinates (px, py) to real-world WGS84 GPS (lng, lat)
        in EPSG:4326 using the projective homography matrix.

        Returns:
            Tuple[float, float]: (longitude, latitude) in degrees.
        """
        H = self.get_homography_matrix()
        pt = np.array([float(px), float(py), 1.0], dtype=np.float64)
        transformed = H @ pt
        w = transformed[2]
        if abs(w) < 1e-12:
            w = 1e-12
        lng = transformed[0] / w
        lat = transformed[1] / w
        return float(lng), float(lat)

    def gps_to_pixel(self, lng: float, lat: float) -> Tuple[float, float]:
        """
        Project real-world GPS coordinates (lng, lat) back to camera view coordinates (px, py)
        using the inverse projective homography matrix.

        Returns:
            Tuple[float, float]: (pixel_x, pixel_y).
        """
        H = self.get_homography_matrix()
        H_inv = np.linalg.inv(H)
        pt = np.array([float(lng), float(lat), 1.0], dtype=np.float64)
        transformed = H_inv @ pt
        w = transformed[2]
        if abs(w) < 1e-12:
            w = 1e-12
        px = transformed[0] / w
        py = transformed[1] / w
        return float(px), float(py)
