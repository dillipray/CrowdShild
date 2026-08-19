"""
Asynchronous PostgreSQL / PostGIS Telemetry Storage Engine for CrowdShield.

Architecture:
  - FrameDensityResult objects are enqueued in a bounded asyncio.Queue
    from the hot vision processing loop (zero latency on caller side).
  - A background asyncio worker drains the queue in configurable batches
    or after a configurable flush interval — whichever fires first.
  - Three tables are maintained:
      1. telemetry_frames       — time-series risk & crowd metrics per frame
      2. crowd_clusters_spatial — PostGIS Point geometries per cluster per frame
      3. hotspots_spatial       — PostGIS Polygon (Envelope) per grid hotspot cell
  - Every row is stamped with camera_id and location_name from VisionConfig
    so multi-camera deployments can be fully disambiguated in the database.

Conforms to:
  - DEVELOPER_1_ENGINE_RULES: Deterministic risk breakdown columns preserved.
  - No UI, Compose, or ViewModelscope dependencies.
"""

from __future__ import annotations

import asyncio
import json
import logging
import math
import time
from datetime import datetime, timezone
from typing import List, Optional, TYPE_CHECKING

from vision.models import FrameDensityResult, GridCellDensity, CrowdCluster

if TYPE_CHECKING:
    import asyncpg
    from vision.config import VisionConfig

logger = logging.getLogger("CrowdShield.TelemetryDB")

# ---------------------------------------------------------------------------
# Schema SQL
# ---------------------------------------------------------------------------

_DDL_EXTENSION = """
CREATE EXTENSION IF NOT EXISTS postgis;
-- Attempt to enable TimescaleDB extension if available in the PostgreSQL environment
DO $$
BEGIN
    CREATE EXTENSION IF NOT EXISTS timescaledb;
EXCEPTION WHEN OTHERS THEN
    -- TimescaleDB not installed or not in shared_preload_libraries; fallback to native range partitioning
    NULL;
END $$;
"""

_DDL_TELEMETRY_FRAMES = """
CREATE TABLE IF NOT EXISTS telemetry_frames (
    id                  BIGSERIAL,
    camera_id           INTEGER         NOT NULL,
    location_name       TEXT            NOT NULL,
    frame_index         BIGINT          NOT NULL,
    captured_at         TIMESTAMPTZ     NOT NULL,
    fps                 REAL            NOT NULL DEFAULT 0,
    frame_width         INTEGER         NOT NULL DEFAULT 0,
    frame_height        INTEGER         NOT NULL DEFAULT 0,
    total_humans        INTEGER         NOT NULL DEFAULT 0,
    hotspot_count       INTEGER         NOT NULL DEFAULT 0,
    active_clusters     INTEGER         NOT NULL DEFAULT 0,
    max_grid_density    INTEGER         NOT NULL DEFAULT 0,
    mean_density_ratio  REAL            NOT NULL DEFAULT 0,
    avg_velocity        REAL            NOT NULL DEFAULT 0,
    max_velocity        REAL            NOT NULL DEFAULT 0,
    accel_variance      REAL            NOT NULL DEFAULT 0,
    is_bottleneck       BOOLEAN         NOT NULL DEFAULT FALSE,
    is_surge            BOOLEAN         NOT NULL DEFAULT FALSE,
    tracked_count       INTEGER         NOT NULL DEFAULT 0,
    risk_score          REAL            NOT NULL DEFAULT 0,
    risk_level          VARCHAR(20)     NOT NULL DEFAULT 'SAFE',
    density_factor      REAL            NOT NULL DEFAULT 0,
    velocity_factor     REAL            NOT NULL DEFAULT 0,
    accel_factor        REAL            NOT NULL DEFAULT 0,
    metadata            JSONB           NOT NULL DEFAULT '{}',
    PRIMARY KEY (id, captured_at)
) PARTITION BY RANGE (captured_at);
"""

_DDL_TELEMETRY_FRAMES_PARTITIONS = """
CREATE TABLE IF NOT EXISTS telemetry_frames_default
    PARTITION OF telemetry_frames DEFAULT;

CREATE OR REPLACE FUNCTION create_telemetry_daily_partition(target_date DATE)
RETURNS TEXT AS $$
DECLARE
    partition_name TEXT;
    start_ts TIMESTAMPTZ;
    end_ts TIMESTAMPTZ;
BEGIN
    partition_name := 'telemetry_frames_y' || to_char(target_date, 'YYYY_mm_dd');
    start_ts := target_date::TIMESTAMPTZ;
    end_ts := (target_date + INTERVAL '1 day')::TIMESTAMPTZ;
    
    EXECUTE format(
        'CREATE TABLE IF NOT EXISTS %I PARTITION OF telemetry_frames FOR VALUES FROM (%L) TO (%L)',
        partition_name, start_ts, end_ts
    );
    RETURN partition_name;
END;
$$ LANGUAGE plpgsql;
"""

_DDL_TELEMETRY_FRAMES_IDX = """
CREATE INDEX IF NOT EXISTS idx_telemetry_frames_camera_time
    ON telemetry_frames (camera_id, captured_at DESC);
CREATE INDEX IF NOT EXISTS idx_telemetry_frames_risk_level
    ON telemetry_frames (risk_level, captured_at DESC);
CREATE INDEX IF NOT EXISTS idx_telemetry_frames_location
    ON telemetry_frames (location_name, captured_at DESC);
"""

_DDL_CLUSTERS = """
CREATE TABLE IF NOT EXISTS crowd_clusters_spatial (
    id              BIGSERIAL       PRIMARY KEY,
    frame_id        BIGINT          NOT NULL,
    camera_id       INTEGER         NOT NULL,
    location_name   TEXT            NOT NULL,
    captured_at     TIMESTAMPTZ     NOT NULL,
    cluster_uid     VARCHAR(60)     NOT NULL,
    human_count     INTEGER         NOT NULL DEFAULT 0,
    density         REAL            NOT NULL DEFAULT 0,
    radius_px       REAL            NOT NULL DEFAULT 0,
    severity        VARCHAR(20)     NOT NULL DEFAULT 'SAFE',
    geom            geometry(Point, 4326),
    CONSTRAINT fk_clusters_telemetry_frame
        FOREIGN KEY (frame_id, captured_at)
        REFERENCES telemetry_frames(id, captured_at)
        ON DELETE CASCADE
);
"""

_DDL_CLUSTERS_IDX = """
CREATE INDEX IF NOT EXISTS idx_clusters_geom ON crowd_clusters_spatial USING GIST (geom);
CREATE INDEX IF NOT EXISTS idx_clusters_camera_time ON crowd_clusters_spatial (camera_id, captured_at DESC);
"""

_DDL_HOTSPOTS = """
CREATE TABLE IF NOT EXISTS hotspots_spatial (
    id              BIGSERIAL       PRIMARY KEY,
    frame_id        BIGINT          NOT NULL,
    camera_id       INTEGER         NOT NULL,
    location_name   TEXT            NOT NULL,
    captured_at     TIMESTAMPTZ     NOT NULL,
    grid_row        INTEGER         NOT NULL,
    grid_col        INTEGER         NOT NULL,
    human_count     INTEGER         NOT NULL DEFAULT 0,
    density_ratio   REAL            NOT NULL DEFAULT 0,
    risk_level      VARCHAR(20)     NOT NULL DEFAULT 'SAFE',
    geom            geometry(Polygon, 4326),
    CONSTRAINT fk_hotspots_telemetry_frame
        FOREIGN KEY (frame_id, captured_at)
        REFERENCES telemetry_frames(id, captured_at)
        ON DELETE CASCADE
);
"""

_DDL_HOTSPOTS_IDX = """
CREATE INDEX IF NOT EXISTS idx_hotspots_geom ON hotspots_spatial USING GIST (geom);
CREATE INDEX IF NOT EXISTS idx_hotspots_camera_time ON hotspots_spatial (camera_id, captured_at DESC);
"""


# ---------------------------------------------------------------------------
# Geo coordinate helpers
# ---------------------------------------------------------------------------

def _px_to_lng(px: float, config: "VisionConfig", py: Optional[float] = None) -> float:
    """Map frame pixel coordinate to WGS84 longitude using projective transformation matrix (Homography)."""
    y = py if py is not None else config.geo_frame_cy
    lng, _ = config.pixel_to_gps(px, y)
    return lng


def _px_to_lat(py: float, config: "VisionConfig", px: Optional[float] = None) -> float:
    """Map frame pixel coordinate to WGS84 latitude using projective transformation matrix (Homography)."""
    x = px if px is not None else config.geo_frame_cx
    _, lat = config.pixel_to_gps(x, py)
    return lat


def _envelope_wgs84(cell: GridCellDensity, frame_w: int, frame_h: int, config: "VisionConfig"):
    """
    Compute WGS84 bounding box (min_lng, min_lat, max_lng, max_lat) for a grid cell
    using projective transformation matrix (Homography).
    """
    p1_lng, p1_lat = config.pixel_to_gps(cell.x1, cell.y1)
    p2_lng, p2_lat = config.pixel_to_gps(cell.x2, cell.y2)
    p3_lng, p3_lat = config.pixel_to_gps(cell.x1, cell.y2)
    p4_lng, p4_lat = config.pixel_to_gps(cell.x2, cell.y1)
    min_lng = min(p1_lng, p2_lng, p3_lng, p4_lng)
    max_lng = max(p1_lng, p2_lng, p3_lng, p4_lng)
    min_lat = min(p1_lat, p2_lat, p3_lat, p4_lat)
    max_lat = max(p1_lat, p2_lat, p3_lat, p4_lat)
    return min_lng, min_lat, max_lng, max_lat


# ---------------------------------------------------------------------------
# Async PostgreSQL/PostGIS Storage Engine
# ---------------------------------------------------------------------------

class AsyncPostGISStorage:
    """
    Non-blocking, queue-backed, batched asynchronous storage for CrowdShield telemetry.

    Usage:
        storage = AsyncPostGISStorage(config)
        await storage.connect()
        # In vision loop (never blocks):
        storage.enqueue(frame_density_result)
        # On shutdown:
        await storage.disconnect()
    """

    def __init__(self, config: "VisionConfig"):
        self.config = config
        self._pool: Optional["asyncpg.Pool"] = None
        self._queue: asyncio.Queue = asyncio.Queue(maxsize=config.postgres_queue_maxsize)
        self._worker_task: Optional[asyncio.Task] = None
        self._running = False
        self._stats = {
            "enqueued": 0,
            "flushed": 0,
            "dropped": 0,
            "errors": 0,
        }

    # ------------------------------------------------------------------
    # Lifecycle
    # ------------------------------------------------------------------

    @staticmethod
    async def _init_connection(conn: "asyncpg.Connection") -> None:
        """
        Initialize newly created asyncpg connections from the pool.
        Configures session-level parameters such as UTC timezone.
        """
        await conn.execute("SET timezone = 'UTC';")

    async def connect(self) -> None:
        """
        Create asyncpg connection pool and run schema migrations.
        Safe to call multiple times — idempotent.
        """
        if self._pool is not None:
            return

        try:
            import asyncpg  # deferred import — only required when postgres_enabled=True
        except ImportError:
            logger.error(
                "asyncpg is not installed. Run: pip install asyncpg>=0.29.0"
            )
            raise

        logger.info(
            "[PostGIS] Connecting to PostgreSQL (camera_id=%d location='%s') ...",
            self.config.camera_id,
            self.config.location_name,
        )
        self._pool = await asyncpg.create_pool(
            dsn=self.config.postgres_dsn,
            min_size=self.config.postgres_min_pool,
            max_size=self.config.postgres_max_pool,
            command_timeout=30.0,
            max_inactive_connection_lifetime=300.0,
            statement_cache_size=0,  # Required for PgBouncer / RDS Proxy transaction pooling compatibility
            init=self._init_connection,
        )
        await self._init_schema()

        self._running = True
        self._worker_task = asyncio.create_task(
            self._worker_loop(), name="telemetry_db_worker"
        )
        logger.info("[PostGIS] Connection pool ready. Worker loop started.")

    async def disconnect(self) -> None:
        """
        Drain the in-flight queue, flush remaining rows, and close the pool cleanly.
        """
        self._running = False

        # Give the worker a moment to drain naturally
        if self._worker_task and not self._worker_task.done():
            try:
                await asyncio.wait_for(self._worker_task, timeout=10.0)
            except (asyncio.TimeoutError, asyncio.CancelledError):
                self._worker_task.cancel()

        # Final flush of whatever is still in queue
        remaining: List[FrameDensityResult] = []
        while not self._queue.empty():
            try:
                remaining.append(self._queue.get_nowait())
            except asyncio.QueueEmpty:
                break

        if remaining and self._pool:
            try:
                await self._flush_batch(remaining)
            except Exception as exc:
                logger.warning("[PostGIS] Final flush error: %s", exc)

        if self._pool:
            await self._pool.close()
            self._pool = None

        logger.info(
            "[PostGIS] Disconnected. Stats — enqueued=%d flushed=%d dropped=%d errors=%d",
            self._stats["enqueued"],
            self._stats["flushed"],
            self._stats["dropped"],
            self._stats["errors"],
        )

    # ------------------------------------------------------------------
    # Public: enqueue (hot path — NEVER blocks)
    # ------------------------------------------------------------------

    def enqueue(self, result: FrameDensityResult) -> None:
        """
        Enqueue a FrameDensityResult for async DB persistence.
        Called from the video processing hot path — must NEVER block or raise.
        Drops the frame gracefully if the queue is full (overflow protection).
        """
        try:
            self._queue.put_nowait(result)
            self._stats["enqueued"] += 1
        except asyncio.QueueFull:
            self._stats["dropped"] += 1
            logger.warning(
                "[PostGIS] Queue overflow — dropped frame %d (total dropped: %d).",
                result.frame_index,
                self._stats["dropped"],
            )

    # ------------------------------------------------------------------
    # Schema initialization
    # ------------------------------------------------------------------

    async def _init_schema(self) -> None:
        """Run DDL to ensure PostGIS/TimescaleDB extension, partitioned tables, and indexes exist."""
        async with self._pool.acquire() as conn:
            await conn.execute(_DDL_EXTENSION)
            await conn.execute(_DDL_TELEMETRY_FRAMES)
            await conn.execute(_DDL_TELEMETRY_FRAMES_PARTITIONS)
            await conn.execute(_DDL_TELEMETRY_FRAMES_IDX)
            await conn.execute(_DDL_CLUSTERS)
            await conn.execute(_DDL_CLUSTERS_IDX)
            await conn.execute(_DDL_HOTSPOTS)
            await conn.execute(_DDL_HOTSPOTS_IDX)

            # Pre-provision daily partitions for today and next 7 days
            try:
                await conn.execute(
                    """
                    DO $$
                    DECLARE
                        i INT;
                    BEGIN
                        FOR i IN 0..7 LOOP
                            PERFORM create_telemetry_daily_partition((CURRENT_DATE + i * INTERVAL '1 day')::DATE);
                        END LOOP;
                    END $$;
                    """
                )
            except Exception as exc:
                logger.warning("[PostGIS] Daily partition pre-provisioning notice: %s", exc)

        logger.info("[PostGIS] Schema initialised (range-partitioned tables + spatial indexes ready).")

    # ------------------------------------------------------------------
    # ------------------------------------------------------------------
    # Background worker loop
    # ------------------------------------------------------------------

    async def _worker_loop(self) -> None:
        """
        Continuously drains the queue in configurable batches.
        Flushes when batch_size is reached OR flush_interval_sec elapses.
        """
        batch: List[FrameDensityResult] = []
        deadline = time.monotonic() + self.config.postgres_flush_interval_sec

        while self._running or not self._queue.empty():
            now = time.monotonic()
            timeout = max(0.01, deadline - now)

            try:
                result = await asyncio.wait_for(self._queue.get(), timeout=timeout)
                batch.append(result)
                self._queue.task_done()
            except asyncio.TimeoutError:
                pass  # flush deadline reached — fall through to flush

            batch_full = len(batch) >= self.config.postgres_batch_size
            time_up = time.monotonic() >= deadline

            if (batch_full or time_up) and batch:
                try:
                    await self._flush_batch(batch)
                    self._stats["flushed"] += len(batch)
                except Exception as exc:
                    self._stats["errors"] += 1
                    logger.error("[PostGIS] Flush error: %s", exc)
                finally:
                    batch.clear()
                    deadline = time.monotonic() + self.config.postgres_flush_interval_sec

        # Final flush after loop exits
        if batch:
            try:
                await self._flush_batch(batch)
                self._stats["flushed"] += len(batch)
            except Exception as exc:
                self._stats["errors"] += 1
                logger.error("[PostGIS] Final worker flush error: %s", exc)

    # ------------------------------------------------------------------
    # Batch DB write
    # ------------------------------------------------------------------

    async def _flush_batch(self, batch: List[FrameDensityResult]) -> None:
        """
        Write a batch of FrameDensityResult objects to the three PostGIS tables
        within a single transaction per batch.
        """
        cfg = self.config

        async with self._pool.acquire() as conn:
            async with conn.transaction():
                for result in batch:
                    captured_at = datetime.fromtimestamp(
                        result.timestamp_ms / 1000.0, tz=timezone.utc
                    )
                    rs = result.risk_score
                    mv = result.movement
                    sm = result.summary

                    breakdown = rs.breakdown
                    density_factor = breakdown.density_factor if breakdown else 0.0
                    velocity_factor = breakdown.velocity_factor if breakdown else 0.0
                    accel_factor = breakdown.accel_variance_factor if breakdown else 0.0

                    # 1. Insert frame telemetry row and get its generated PK
                    frame_id: int = await conn.fetchval(
                        """
                        INSERT INTO telemetry_frames (
                            camera_id, location_name, frame_index, captured_at,
                            fps, frame_width, frame_height,
                            total_humans, hotspot_count, active_clusters,
                            max_grid_density, mean_density_ratio,
                            avg_velocity, max_velocity, accel_variance,
                            is_bottleneck, is_surge, tracked_count,
                            risk_score, risk_level,
                            density_factor, velocity_factor, accel_factor,
                            metadata
                        ) VALUES (
                            $1,  $2,  $3,  $4,
                            $5,  $6,  $7,
                            $8,  $9,  $10,
                            $11, $12,
                            $13, $14, $15,
                            $16, $17, $18,
                            $19, $20,
                            $21, $22, $23,
                            $24
                        ) RETURNING id
                        """,
                        cfg.camera_id,       # $1
                        cfg.location_name,   # $2
                        result.frame_index,  # $3
                        captured_at,         # $4
                        result.fps,          # $5
                        result.frame_width,  # $6
                        result.frame_height, # $7
                        sm.total_humans,     # $8
                        sm.hotspot_count,    # $9
                        sm.active_clusters_count,   # $10
                        sm.max_grid_density,        # $11
                        sm.mean_density_ratio,      # $12
                        mv.avg_velocity_px,         # $13
                        mv.max_velocity_px,         # $14
                        mv.accel_variance,          # $15
                        mv.bottleneck_detected,     # $16
                        mv.surge_detected,          # $17
                        mv.tracked_count,           # $18
                        rs.score,                   # $19
                        rs.level.value,             # $20
                        density_factor,             # $21
                        velocity_factor,            # $22
                        accel_factor,               # $23
                        json.dumps(result.metadata), # $24
                    )

                    # 2. Insert crowd clusters as PostGIS Points
                    for cluster in result.clusters:
                        lng, lat = cfg.pixel_to_gps(cluster.center_x, cluster.center_y)
                        await conn.execute(
                            """
                            INSERT INTO crowd_clusters_spatial (
                                frame_id, camera_id, location_name, captured_at,
                                cluster_uid, human_count, density, radius_px, severity,
                                geom
                            ) VALUES (
                                $1, $2, $3, $4,
                                $5, $6, $7, $8, $9,
                                ST_SetSRID(ST_MakePoint($10, $11), 4326)
                            )
                            """,
                            frame_id,
                            cfg.camera_id,
                            cfg.location_name,
                            captured_at,
                            cluster.id,
                            cluster.human_count,
                            cluster.density,
                            cluster.radius,
                            cluster.severity.value,
                            lng,   # $10 — longitude (X)
                            lat,   # $11 — latitude  (Y)
                        )

                    # 3. Insert active hotspot grid cells as PostGIS Polygons
                    hotspots = [c for c in result.grid_density if c.is_hotspot]
                    for cell in hotspots:
                        min_lng, min_lat, max_lng, max_lat = _envelope_wgs84(
                            cell, result.frame_width, result.frame_height, cfg
                        )
                        await conn.execute(
                            """
                            INSERT INTO hotspots_spatial (
                                frame_id, camera_id, location_name, captured_at,
                                grid_row, grid_col,
                                human_count, density_ratio, risk_level,
                                geom
                            ) VALUES (
                                $1, $2, $3, $4,
                                $5, $6,
                                $7, $8, $9,
                                ST_MakeEnvelope($10, $11, $12, $13, 4326)
                            )
                            """,
                            frame_id,
                            cfg.camera_id,
                            cfg.location_name,
                            captured_at,
                            cell.row,
                            cell.col,
                            cell.human_count,
                            cell.density_ratio,
                            cell.risk_level.value,
                            min_lng,    # $10
                            min_lat,    # $11
                            max_lng,    # $12
                            max_lat,    # $13
                        )

    # ------------------------------------------------------------------
    # Query Helpers for Telemetry Insights & Verification
    # ------------------------------------------------------------------

    async def fetch_recent_telemetry(self, limit: int = 15) -> List[dict]:
        """Fetch the most recent persisted telemetry records from the database."""
        if not self._pool:
            return []
        try:
            async with self._pool.acquire() as conn:
                rows = await conn.fetch(
                    """
                    SELECT id, camera_id, location_name, frame_index, captured_at,
                           fps, total_humans, hotspot_count, active_clusters,
                           risk_score, risk_level, avg_velocity, accel_variance,
                           is_bottleneck, is_surge
                    FROM telemetry_frames
                    ORDER BY captured_at DESC
                    LIMIT $1
                    """,
                    limit,
                )
                return [
                    {
                        "id": r["id"],
                        "camera_id": r["camera_id"],
                        "location_name": r["location_name"],
                        "frame_index": r["frame_index"],
                        "captured_at": r["captured_at"].isoformat() if r["captured_at"] else None,
                        "fps": round(float(r["fps"]), 1),
                        "total_humans": r["total_humans"],
                        "hotspot_count": r["hotspot_count"],
                        "active_clusters": r["active_clusters"],
                        "risk_score": round(float(r["risk_score"]), 2),
                        "risk_level": r["risk_level"],
                        "avg_velocity": round(float(r["avg_velocity"]), 2),
                        "accel_variance": round(float(r["accel_variance"]), 2),
                        "is_bottleneck": bool(r["is_bottleneck"]),
                        "is_surge": bool(r["is_surge"]),
                    }
                    for r in rows
                ]
        except Exception as exc:
            logger.error("[PostGIS] fetch_recent_telemetry error: %s", exc)
            return []

    async def fetch_db_summary(self) -> dict:
        """Fetch aggregated telemetry totals from the PostgreSQL database."""
        if not self._pool:
            return {"connected": False}
        try:
            async with self._pool.acquire() as conn:
                total_frames = await conn.fetchval("SELECT count(*) FROM telemetry_frames")
                total_clusters = await conn.fetchval("SELECT count(*) FROM crowd_clusters_spatial")
                total_hotspots = await conn.fetchval("SELECT count(*) FROM hotspots_spatial")
                return {
                    "connected": True,
                    "total_telemetry_frames": total_frames or 0,
                    "total_clusters_recorded": total_clusters or 0,
                    "total_hotspots_recorded": total_hotspots or 0,
                    "session_stats": self.stats,
                }
        except Exception as exc:
            logger.error("[PostGIS] fetch_db_summary error: %s", exc)
            return {"connected": True, "error": str(exc), "session_stats": self.stats}

    # ------------------------------------------------------------------
    # Diagnostics
    # ------------------------------------------------------------------

    @property
    def is_connected(self) -> bool:
        return self._pool is not None

    @property
    def queue_depth(self) -> int:
        return self._queue.qsize()

    @property
    def stats(self) -> dict:
        return dict(self._stats)
