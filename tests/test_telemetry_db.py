"""
Unit tests for AsyncPostGISStorage (vision/telemetry_db.py).

All tests use unittest.mock to avoid requiring a live PostgreSQL/PostGIS instance.
Validates:
  - Queue enqueue / overflow / drop behaviour
  - Batch SQL parameter building
  - Coordinate projection helpers
  - Schema DDL correctness
  - Pipeline telemetry_sink wiring
  - VisionConfig camera_id / location_name fields
"""
from __future__ import annotations

import asyncio
import time
from typing import Any
from unittest.mock import AsyncMock, MagicMock, patch, call

import numpy as np
import pytest

from vision.config import VisionConfig
from vision.models import (
    BoundingBox,
    CrowdCluster,
    DensitySummary,
    FrameDensityResult,
    GridCellDensity,
    MovementMetrics,
    RiskLevel,
    RiskScore,
    RiskScoreBreakdown,
)
from vision.telemetry_db import AsyncPostGISStorage, _px_to_lat, _px_to_lng, _envelope_wgs84


# ---------------------------------------------------------------------------
# Fixtures
# ---------------------------------------------------------------------------

@pytest.fixture()
def cfg() -> VisionConfig:
    """VisionConfig with PostGIS enabled, a test DSN, camera_id=3 and location_name."""
    return VisionConfig(
        postgres_enabled=True,
        postgres_dsn="postgresql://test:test@localhost:5432/test_db",
        postgres_min_pool=1,
        postgres_max_pool=2,
        postgres_batch_size=5,
        postgres_flush_interval_sec=0.2,
        postgres_queue_maxsize=10,
        camera_id=3,
        location_name="Gate-3-North",
        geo_origin_lat=28.6139,
        geo_origin_lng=77.2090,
        geo_px_to_lat=0.00001,
        geo_px_to_lng=0.00001,
        geo_frame_cx=320.0,
        geo_frame_cy=240.0,
    )


def _make_result(frame_index: int = 1) -> FrameDensityResult:
    """Construct a minimal but complete FrameDensityResult for testing."""
    summary = DensitySummary(
        total_humans=12,
        hotspot_count=2,
        active_clusters_count=1,
        max_grid_density=8,
        mean_density_ratio=1.4,
    )
    movement = MovementMetrics(
        avg_velocity_px=1.8,
        max_velocity_px=3.2,
        accel_variance=0.9,
        bottleneck_detected=True,
        surge_detected=False,
        tracked_count=12,
    )
    breakdown = RiskScoreBreakdown(
        density_factor=4.8,
        velocity_factor=2.0,
        accel_variance_factor=0.45,
    )
    risk = RiskScore(score=7.25, level=RiskLevel.CAUTION, breakdown=breakdown)
    cluster = CrowdCluster(
        id="cluster-0",
        center_x=310.0,
        center_y=250.0,
        radius=45.0,
        human_count=10,
        density=0.0015,
        severity=RiskLevel.CAUTION,
    )
    hotspot_cell = GridCellDensity(
        row=1, col=2,
        x1=160, y1=120, x2=320, y2=240,
        human_count=8,
        density_ratio=1.8,
        is_hotspot=True,
        risk_level=RiskLevel.CAUTION,
    )
    safe_cell = GridCellDensity(
        row=3, col=3,
        x1=480, y1=360, x2=640, y2=480,
        human_count=0,
        density_ratio=0.0,
        is_hotspot=False,
        risk_level=RiskLevel.SAFE,
    )
    return FrameDensityResult(
        frame_index=frame_index,
        timestamp_ms=int(time.time() * 1000),
        frame_width=640,
        frame_height=480,
        fps=15.0,
        summary=summary,
        detections=[],
        grid_density=[hotspot_cell, safe_cell],
        clusters=[cluster],
        movement=movement,
        risk_score=risk,
        metadata={"device": "cuda", "model": "yolov8n.pt"},
    )


# ---------------------------------------------------------------------------
# 1. VisionConfig camera/location fields
# ---------------------------------------------------------------------------

def test_config_camera_and_location_defaults():
    """Default VisionConfig should carry camera_id=0 and a location_name string."""
    config = VisionConfig()
    assert isinstance(config.camera_id, int)
    assert isinstance(config.location_name, str)


def test_config_camera_and_location_custom(cfg):
    """VisionConfig passed custom camera_id=3 and location_name='Gate-3-North'."""
    assert cfg.camera_id == 3
    assert cfg.location_name == "Gate-3-North"


def test_config_postgres_fields(cfg):
    """VisionConfig should expose all PostGIS connection and throttling fields."""
    assert cfg.postgres_enabled is True
    assert "5432" in cfg.postgres_dsn
    assert cfg.postgres_batch_size == 5
    assert cfg.postgres_min_pool == 1
    assert cfg.postgres_max_pool == 2
    assert cfg.postgres_log_interval_frames == 6
    assert cfg.postgres_telemetry_target_fps == 5.0
    assert cfg.postgres_force_log_on_critical is True


# ---------------------------------------------------------------------------
# 2. Geo coordinate projection helpers & Homography matrix
# ---------------------------------------------------------------------------

def test_px_to_lng_at_frame_center(cfg):
    """Pixel at frame center X should map to the geo_origin_lng exactly."""
    lng = _px_to_lng(cfg.geo_frame_cx, cfg)
    assert abs(lng - cfg.geo_origin_lng) < 1e-9


def test_px_to_lat_at_frame_center(cfg):
    """Pixel at frame center Y should map to the geo_origin_lat exactly."""
    lat = _px_to_lat(cfg.geo_frame_cy, cfg)
    assert abs(lat - cfg.geo_origin_lat) < 1e-9


def test_px_to_lng_offset(cfg):
    """Pixel offset to the right should produce a larger longitude."""
    lng_center = _px_to_lng(cfg.geo_frame_cx, cfg)
    lng_right = _px_to_lng(cfg.geo_frame_cx + 100, cfg)
    assert lng_right > lng_center


def test_px_to_lat_offset(cfg):
    """Pixel offset downward (larger py) should produce a smaller latitude (Y-inverted)."""
    lat_center = _px_to_lat(cfg.geo_frame_cy, cfg)
    lat_below = _px_to_lat(cfg.geo_frame_cy + 100, cfg)
    assert lat_below < lat_center


def test_envelope_wgs84_ordering(cfg):
    cell = GridCellDensity(
        row=0, col=0, x1=0, y1=0, x2=100, y2=80,
        human_count=5, density_ratio=1.2,
        is_hotspot=True, risk_level=RiskLevel.CAUTION,
    )
    min_lng, min_lat, max_lng, max_lat = _envelope_wgs84(cell, 640, 480, cfg)
    assert min_lng < max_lng
    assert min_lat < max_lat


def test_homography_matrix_default(cfg):
    """get_homography_matrix should return a 3x3 array mapping frame center to origin."""
    H = cfg.get_homography_matrix()
    assert H.shape == (3, 3)
    lng, lat = cfg.pixel_to_gps(cfg.geo_frame_cx, cfg.geo_frame_cy)
    assert abs(lng - cfg.geo_origin_lng) < 1e-9
    assert abs(lat - cfg.geo_origin_lat) < 1e-9


def test_pixel_to_gps_and_gps_to_pixel_roundtrip(cfg):
    """Projecting pixel -> GPS -> pixel must be invertible with high precision."""
    px, py = 123.45, 234.56
    lng, lat = cfg.pixel_to_gps(px, py)
    px_back, py_back = cfg.gps_to_pixel(lng, lat)
    assert abs(px - px_back) < 1e-5
    assert abs(py - py_back) < 1e-5


def test_set_homography_from_points_dlt(cfg):
    """Direct Linear Transformation (DLT) should fit 4 point correspondences perfectly."""
    src_pts = [(0.0, 0.0), (640.0, 0.0), (640.0, 480.0), (0.0, 480.0)]
    dst_pts = [
        (77.2080, 28.6145),
        (77.2100, 28.6145),
        (77.2102, 28.6130),
        (77.2078, 28.6130),
    ]
    H = cfg.set_homography_from_points(src_pts, dst_pts)
    assert H.shape == (3, 3)

    for (u, v), (expected_lng, expected_lat) in zip(src_pts, dst_pts):
        lng, lat = cfg.pixel_to_gps(u, v)
        assert abs(lng - expected_lng) < 1e-6
        assert abs(lat - expected_lat) < 1e-6


# ---------------------------------------------------------------------------
# 3. Enqueue / overflow / drop
# ---------------------------------------------------------------------------

def test_enqueue_stores_item(cfg):
    """Items enqueued within capacity should be stored in the queue."""
    storage = AsyncPostGISStorage(cfg)
    result = _make_result()
    storage.enqueue(result)
    assert storage.queue_depth == 1
    assert storage.stats["enqueued"] == 1
    assert storage.stats["dropped"] == 0


def test_enqueue_overflow_drops_gracefully(cfg):
    """When the queue is full, enqueue() must silently drop and count the drop."""
    storage = AsyncPostGISStorage(cfg)
    # Fill the queue to maxsize
    for i in range(cfg.postgres_queue_maxsize):
        storage.enqueue(_make_result(i))
    # One more must be dropped
    storage.enqueue(_make_result(9999))
    assert storage.stats["dropped"] == 1
    assert storage.queue_depth == cfg.postgres_queue_maxsize


# ---------------------------------------------------------------------------
# 4. Schema DDL content assertions
# ---------------------------------------------------------------------------

def test_schema_ddl_contains_camera_id():
    """telemetry_frames DDL must include camera_id and location_name columns."""
    from vision.telemetry_db import _DDL_TELEMETRY_FRAMES
    assert "camera_id" in _DDL_TELEMETRY_FRAMES
    assert "location_name" in _DDL_TELEMETRY_FRAMES


def test_schema_ddl_clusters_contains_camera_id():
    from vision.telemetry_db import _DDL_CLUSTERS
    assert "camera_id" in _DDL_CLUSTERS
    assert "location_name" in _DDL_CLUSTERS


def test_schema_ddl_hotspots_contains_camera_id():
    from vision.telemetry_db import _DDL_HOTSPOTS
    assert "camera_id" in _DDL_HOTSPOTS
    assert "location_name" in _DDL_HOTSPOTS


def test_schema_ddl_postgis_geometry():
    """Cluster DDL must use PostGIS geometry(Point, 4326) type."""
    from vision.telemetry_db import _DDL_CLUSTERS, _DDL_HOTSPOTS
    assert "geometry(Point, 4326)" in _DDL_CLUSTERS
    assert "geometry(Polygon, 4326)" in _DDL_HOTSPOTS


def test_schema_ddl_risk_breakdown_columns():
    """telemetry_frames DDL must store the full Developer 1 risk breakdown."""
    from vision.telemetry_db import _DDL_TELEMETRY_FRAMES
    for col in ("density_factor", "velocity_factor", "accel_factor", "risk_score", "risk_level"):
        assert col in _DDL_TELEMETRY_FRAMES, f"Missing column: {col}"


def test_schema_ddl_partitioning_by_timestamp():
    """telemetry_frames DDL must be range-partitioned by captured_at with a composite PK."""
    from vision.telemetry_db import _DDL_TELEMETRY_FRAMES
    assert "PARTITION BY RANGE (captured_at)" in _DDL_TELEMETRY_FRAMES
    assert "PRIMARY KEY (id, captured_at)" in _DDL_TELEMETRY_FRAMES


def test_schema_ddl_default_partition_and_partition_function():
    """Schema DDL must provide a default fallback partition and dynamic daily partition creator."""
    from vision.telemetry_db import _DDL_TELEMETRY_FRAMES_PARTITIONS
    assert "PARTITION OF telemetry_frames DEFAULT" in _DDL_TELEMETRY_FRAMES_PARTITIONS
    assert "create_telemetry_daily_partition" in _DDL_TELEMETRY_FRAMES_PARTITIONS


def test_schema_ddl_timescaledb_extension_support():
    """Extension DDL must include postgis and safe TimescaleDB extension setup."""
    from vision.telemetry_db import _DDL_EXTENSION
    assert "postgis" in _DDL_EXTENSION
    assert "timescaledb" in _DDL_EXTENSION


def test_schema_ddl_foreign_keys_include_partition_key():
    """Child tables must have composite foreign keys referencing (id, captured_at)."""
    from vision.telemetry_db import _DDL_CLUSTERS, _DDL_HOTSPOTS
    assert "FOREIGN KEY (frame_id, captured_at)" in _DDL_CLUSTERS
    assert "REFERENCES telemetry_frames(id, captured_at)" in _DDL_CLUSTERS
    assert "FOREIGN KEY (frame_id, captured_at)" in _DDL_HOTSPOTS
    assert "REFERENCES telemetry_frames(id, captured_at)" in _DDL_HOTSPOTS


# ---------------------------------------------------------------------------
# 5. Pipeline telemetry_sink wiring & Rate Throttling
# ---------------------------------------------------------------------------

def test_pipeline_sink_receives_results(cfg):
    """
    When postgres_log_interval_frames=1, telemetry_sink is called on every frame.
    """
    from vision.pipeline import CrowdVisionPipeline
    cfg.postgres_log_interval_frames = 1
    pipeline = CrowdVisionPipeline(cfg)

    received = []
    pipeline.telemetry_sink = received.append

    dummy = np.zeros((480, 640, 3), dtype=np.uint8)
    result = pipeline.process_frame(dummy, annotate=False)

    assert len(received) == 1
    assert received[0] is result


def test_pipeline_sink_called_with_annotate(cfg):
    """Sink must also fire when annotate=True (returns tuple)."""
    from vision.pipeline import CrowdVisionPipeline
    cfg.postgres_log_interval_frames = 1
    pipeline = CrowdVisionPipeline(cfg)

    received = []
    pipeline.telemetry_sink = received.append

    dummy = np.zeros((480, 640, 3), dtype=np.uint8)
    result, _ = pipeline.process_frame(dummy, annotate=True)

    assert len(received) == 1
    assert received[0] is result


def test_pipeline_sink_exception_does_not_crash(cfg):
    """A broken sink must NOT crash the vision loop."""
    from vision.pipeline import CrowdVisionPipeline
    cfg.postgres_log_interval_frames = 1
    pipeline = CrowdVisionPipeline(cfg)
    pipeline.telemetry_sink = lambda r: (_ for _ in ()).throw(RuntimeError("DB offline"))

    dummy = np.zeros((480, 640, 3), dtype=np.uint8)
    # Must not raise
    result = pipeline.process_frame(dummy, annotate=False)
    assert result is not None


def test_telemetry_rate_throttling_30fps_to_5fps(cfg):
    """
    30 FPS CV processing throttled to 5 FPS DB persistence (every 6th frame).
    All 30 frames are returned to caller, but only 5 are enqueued to the DB sink.
    """
    from vision.pipeline import CrowdVisionPipeline
    cfg.postgres_log_interval_frames = 6
    cfg.postgres_force_log_on_critical = False
    pipeline = CrowdVisionPipeline(cfg)

    received = []
    pipeline.telemetry_sink = received.append

    dummy = np.zeros((480, 640, 3), dtype=np.uint8)
    for i in range(30):
        res = pipeline.process_frame(dummy, annotate=False)
        assert res.frame_index == i + 1

    # Exactly 30 / 6 = 5 frames passed to DB sink
    assert len(received) == 5
    assert [r.frame_index for r in received] == [6, 12, 18, 24, 30]


def test_telemetry_critical_risk_bypasses_throttling(cfg):
    """
    When risk >= 8.0, frame must immediately bypass throttling stride and log to DB.
    """
    from vision.pipeline import CrowdVisionPipeline
    cfg.postgres_log_interval_frames = 6
    cfg.postgres_force_log_on_critical = True
    pipeline = CrowdVisionPipeline(cfg)

    received = []
    pipeline.telemetry_sink = received.append

    dummy = np.zeros((480, 640, 3), dtype=np.uint8)
    
    # Frame 1: nominal (risk 0.0) -> not logged (1 % 6 != 0)
    pipeline.process_frame(dummy, annotate=False)
    assert len(received) == 0

    # Frame 2: mock critical risk -> immediately logged via bypass
    with patch.object(
        pipeline.risk_calculator,
        "evaluate_from_metrics",
        return_value=RiskScore(score=8.5, level=RiskLevel.HIGH_RISK),
    ):
        res = pipeline.process_frame(dummy, annotate=False)
        assert len(received) == 1
        assert received[-1].frame_index == 2
        assert received[-1].risk_score.score == 8.5


def test_telemetry_panic_surge_bypasses_throttling(cfg):
    """
    When surge_detected=True, frame must immediately bypass throttling stride and log to DB.
    """
    from vision.pipeline import CrowdVisionPipeline
    cfg.postgres_log_interval_frames = 6
    cfg.postgres_force_log_on_critical = True
    pipeline = CrowdVisionPipeline(cfg)

    received = []
    pipeline.telemetry_sink = received.append

    dummy = np.zeros((480, 640, 3), dtype=np.uint8)
    
    # Frame 1: nominal
    pipeline.process_frame(dummy, annotate=False)
    assert len(received) == 0

    # Frame 2: surge detected
    with patch.object(
        pipeline.movement_tracker,
        "update",
        return_value=MovementMetrics(
            avg_velocity_px=1.0, max_velocity_px=2.0, accel_variance=5.0,
            bottleneck_detected=False, surge_detected=True, tracked_count=5
        ),
    ):
        pipeline.process_frame(dummy, annotate=False)
        assert len(received) == 1
        assert received[-1].frame_index == 2
        assert received[-1].movement.surge_detected is True


# ---------------------------------------------------------------------------
# 6. Async connect/disconnect lifecycle (mocked asyncpg via sys.modules)
# ---------------------------------------------------------------------------

def _inject_mock_asyncpg():
    """
    Inject a minimal asyncpg mock into sys.modules so that the deferred
    `import asyncpg` inside connect() resolves without the real package.
    """
    import sys
    import types

    mock_asyncpg = types.ModuleType("asyncpg")
    mock_asyncpg.create_pool = None   # will be replaced per-test
    sys.modules.setdefault("asyncpg", mock_asyncpg)
    return mock_asyncpg


@pytest.mark.anyio
async def test_connect_initialises_pool_and_schema(cfg):
    """connect() should create a pool, call _init_schema, and start the worker."""
    mock_asyncpg = _inject_mock_asyncpg()

    # asyncpg pool.acquire() acts as an async context manager, not a plain coroutine.
    # Build a context-manager mock that satisfies `async with pool.acquire() as conn:`
    mock_conn = AsyncMock()
    acquire_ctx = MagicMock()
    acquire_ctx.__aenter__ = AsyncMock(return_value=mock_conn)
    acquire_ctx.__aexit__ = AsyncMock(return_value=False)

    mock_pool = AsyncMock()
    mock_pool.acquire = MagicMock(return_value=acquire_ctx)
    mock_pool.close = AsyncMock()
    mock_asyncpg.create_pool = AsyncMock(return_value=mock_pool)

    storage = AsyncPostGISStorage(cfg)

    import sys
    orig = sys.modules.get("asyncpg")
    sys.modules["asyncpg"] = mock_asyncpg
    try:
        # Patch _init_schema so we don't need to fully simulate all DDL execute() chains
        with patch.object(storage, "_init_schema", new=AsyncMock()):
            await storage.connect()
    finally:
        if orig is None:
            sys.modules.pop("asyncpg", None)
        else:
            sys.modules["asyncpg"] = orig

    assert storage.is_connected is True
    assert storage._worker_task is not None
    mock_asyncpg.create_pool.assert_awaited_once()
    _, kwargs = mock_asyncpg.create_pool.call_args
    assert kwargs.get("statement_cache_size") == 0
    assert kwargs.get("dsn") == cfg.postgres_dsn
    assert kwargs.get("min_size") == cfg.postgres_min_pool
    assert kwargs.get("max_size") == cfg.postgres_max_pool
    assert kwargs.get("init") == AsyncPostGISStorage._init_connection

    storage._running = False
    storage._worker_task.cancel()
    try:
        await storage._worker_task
    except (asyncio.CancelledError, Exception):
        pass


@pytest.mark.anyio
async def test_init_connection():
    """_init_connection should set UTC timezone on the connection."""
    mock_conn = AsyncMock()
    await AsyncPostGISStorage._init_connection(mock_conn)
    mock_conn.execute.assert_awaited_once_with("SET timezone = 'UTC';")


@pytest.mark.anyio
async def test_disconnect_drains_queue(cfg):
    """disconnect() should flush remaining queued items before closing pool."""
    mock_asyncpg = _inject_mock_asyncpg()

    acquire_ctx = MagicMock()
    acquire_ctx.__aenter__ = AsyncMock(return_value=AsyncMock())
    acquire_ctx.__aexit__ = AsyncMock(return_value=False)

    mock_pool = AsyncMock()
    mock_pool.acquire = MagicMock(return_value=acquire_ctx)
    mock_pool.close = AsyncMock()
    mock_asyncpg.create_pool = AsyncMock(return_value=mock_pool)

    storage = AsyncPostGISStorage(cfg)

    import sys
    orig = sys.modules.get("asyncpg")
    sys.modules["asyncpg"] = mock_asyncpg
    try:
        with patch.object(storage, "_init_schema", new=AsyncMock()):
            await storage.connect()
    finally:
        if orig is None:
            sys.modules.pop("asyncpg", None)
        else:
            sys.modules["asyncpg"] = orig

    # Enqueue items and then disconnect with a mocked flush
    for i in range(3):
        storage.enqueue(_make_result(i))

    with patch.object(storage, "_flush_batch", new=AsyncMock()):
        storage._running = False
        await storage.disconnect()

    assert not storage.is_connected
