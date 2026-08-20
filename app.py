"""
CrowdShield Real-Time Computer Vision & Telemetry Server (FastAPI).
Connects the Vision Engine to WebSockets streaming live telemetry to both the
Android Mobile App (com.crowdshield.stampede) and the Web Command Dashboard.
"""

import os
import io
import time
import asyncio
import cv2
import numpy as np
from contextlib import asynccontextmanager
from typing import Optional, Dict, Any

try:
    from dotenv import load_dotenv
    load_dotenv()
except ImportError:
    pass

from fastapi import FastAPI, WebSocket, WebSocketDisconnect, UploadFile, File, HTTPException
from fastapi.staticfiles import StaticFiles
from fastapi.responses import HTMLResponse, StreamingResponse, JSONResponse
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

from vision.config import VisionConfig
from vision.models import FrameDensityResult, RiskScore
from vision.streamer import VisionStreamManager

# Global Stream Manager instance
config = VisionConfig()
stream_manager = VisionStreamManager(config)
stream_task: Optional[asyncio.Task] = None


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Start background vision loop on server startup and cleanly terminate on shutdown."""
    global stream_task
    # Start continuous background vision streaming worker
    stream_task = asyncio.create_task(stream_manager.run_stream_loop(fps=15))
    yield
    # Shutdown
    stream_manager.is_running = False
    if stream_task:
        stream_task.cancel()


app = FastAPI(
    title="CrowdShield Real-Time Vision & Telemetry Hub",
    description="Live human detection, density calculation, and stampede risk streaming for Android & Web.",
    version="1.0.0",
    lifespan=lifespan,
)

# CORS middleware for mobile and web cross-origin requests
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Mount static web dashboard assets
static_dir = os.path.join(os.path.dirname(__file__), "static")
if os.path.exists(static_dir):
    app.mount("/static", StaticFiles(directory=static_dir), name="static")


@app.get("/", response_class=HTMLResponse)
async def serve_dashboard():
    """Serves the Web Command Dashboard."""
    index_file = os.path.join(static_dir, "index.html")
    if os.path.exists(index_file):
        with open(index_file, "r", encoding="utf-8") as f:
            return HTMLResponse(content=f.read())
    return HTMLResponse("<h2>CrowdShield Vision Engine Running</h2>")


@app.get("/health")
def health_check():
    storage = stream_manager.telemetry_storage
    db_status = (
        {"enabled": True, "connected": storage.is_connected, "queue_depth": storage.queue_depth, "stats": storage.stats}
        if storage is not None
        else {"enabled": False}
    )
    return {
        "status": "healthy",
        "timestamp": int(time.time() * 1000),
        "source_mode": stream_manager.source_mode,
        "video_source": stream_manager.video_path,
        "camera_id": config.camera_id,
        "location_name": config.location_name,
        "active_mobile_clients": len(stream_manager.mobile_connections),
        "active_dashboard_clients": len(stream_manager.dashboard_connections),
        "telemetry_db": db_status,
    }


# ==============================================================================
# WebSocket Streaming Endpoints
# ==============================================================================

@app.websocket("/ws/mobile")
async def websocket_mobile_endpoint(websocket: WebSocket):
    """
    Dedicated WebSocket endpoint for Android App (com.crowdshield.stampede).
    Streams reactive RiskScore, CrowdClusters, and critical alerts.
    """
    await stream_manager.connect_client(websocket, client_type="mobile")
    try:
        while True:
            # Handle incoming messages from Android app (e.g. SOS triggers or GPS location telemetry)
            data = await websocket.receive_json()
            msg_type = data.get("type", "")
            
            if msg_type == "EMERGENCY_SOS":
                # Broadcast immediate alert to all dashboard stations
                pass
            elif msg_type == "LOCATION_UPDATE":
                # Handle mobile user location
                pass
    except WebSocketDisconnect:
        stream_manager.disconnect_client(websocket)
    except Exception:
        stream_manager.disconnect_client(websocket)


@app.websocket("/ws/dashboard")
async def websocket_dashboard_endpoint(websocket: WebSocket):
    """
    Dedicated WebSocket endpoint for Web Command Center.
    Streams full spatial grid cells, heatmap data, trend telemetry, and bounding boxes.
    """
    await stream_manager.connect_client(websocket, client_type="dashboard")
    try:
        while True:
            # Keep-alive ping/pong or client action
            _ = await websocket.receive_text()
    except WebSocketDisconnect:
        stream_manager.disconnect_client(websocket)
    except Exception:
        stream_manager.disconnect_client(websocket)


@app.websocket("/ws/telemetry")
async def websocket_generic_endpoint(websocket: WebSocket):
    """Generic telemetry endpoint supporting query params."""
    await stream_manager.connect_client(websocket, client_type="dashboard")
    try:
        while True:
            _ = await websocket.receive_text()
    except WebSocketDisconnect:
        stream_manager.disconnect_client(websocket)


# ==============================================================================
# Video MJPEG Streaming Feed
# ==============================================================================

def generate_mjpeg_stream():
    """Generator for live MJPEG video feed to web browsers."""
    while True:
        frame = stream_manager.latest_annotated_bgr
        if frame is None:
            # Fallback black frame if no frame processed yet
            frame = np.zeros((480, 640, 3), dtype=np.uint8)

        ret, buffer = cv2.imencode(".jpg", frame, [cv2.IMWRITE_JPEG_QUALITY, 75])
        if not ret:
            time.sleep(0.04)
            continue

        yield (
            b"--frame\r\n"
            b"Content-Type: image/jpeg\r\n\r\n" + buffer.tobytes() + b"\r\n"
        )
        time.sleep(0.04)  # ~25 fps preview stream


@app.get("/video_feed")
def video_feed():
    """MJPEG stream endpoint for real-time video player in browser."""
    return StreamingResponse(
        generate_mjpeg_stream(),
        media_type="multipart/x-mixed-replace; boundary=frame",
    )


# ==============================================================================
# REST API Endpoints
# ==============================================================================

class VideoSourcePayload(BaseModel):
    video_path: str = "videos/large_crowd.mp4"


@app.post("/api/v1/source/video")
def switch_to_video(payload: Optional[VideoSourcePayload] = None, video_path: Optional[str] = None):
    """Switch vision feed to a local video file (e.g. 'videos/large_crowd.mp4')."""
    target = video_path or (payload.video_path if payload else "videos/large_crowd.mp4")
    success = stream_manager.set_source_video(target)
    if not success:
        raise HTTPException(status_code=400, detail=f"Could not open video file: {target}")
    return {
        "status": "ok",
        "mode": "video",
        "video_path": target,
        "filename": os.path.basename(target),
    }


@app.post("/api/v1/videos/upload")
async def upload_video(file: UploadFile = File(...)):
    """Upload a custom video file, save to videos/ directory, and switch vision stream to it."""
    if not file.filename:
        raise HTTPException(status_code=400, detail="No video file provided.")
    
    valid_extensions = (".mp4", ".avi", ".mov", ".mkv", ".webm", ".flv")
    filename = file.filename
    if not any(filename.lower().endswith(ext) for ext in valid_extensions):
        raise HTTPException(
            status_code=400,
            detail=f"Unsupported video format. Allowed formats: {', '.join(valid_extensions)}"
        )

    videos_dir = os.path.join(os.path.dirname(__file__), "videos")
    os.makedirs(videos_dir, exist_ok=True)

    # Sanitize filename
    safe_filename = "".join(c for c in filename if c.isalnum() or c in "._- ")
    if not safe_filename:
        safe_filename = f"upload_{int(time.time())}.mp4"

    target_path = os.path.join(videos_dir, safe_filename)

    try:
        with open(target_path, "wb") as buffer:
            while chunk := await file.read(1024 * 1024):  # 1MB buffer
                buffer.write(chunk)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to save video: {str(e)}")

    rel_path = os.path.join("videos", safe_filename).replace("\\", "/")
    success = stream_manager.set_source_video(rel_path)
    if not success:
        raise HTTPException(status_code=400, detail=f"Saved video, but OpenCV could not decode '{safe_filename}'.")

    return {
        "status": "ok",
        "mode": "video",
        "video_path": rel_path,
        "filename": safe_filename,
        "size_mb": round(os.path.getsize(target_path) / (1024 * 1024), 2),
    }


@app.get("/api/v1/videos")
def list_available_videos():
    """List all available video test files in the videos directory."""
    videos_dir = os.path.join(os.path.dirname(__file__), "videos")
    results = []
    if os.path.exists(videos_dir):
        for f in os.listdir(videos_dir):
            if f.lower().endswith((".mp4", ".avi", ".mov", ".mkv", ".webm")):
                fp = os.path.join(videos_dir, f)
                rel_p = os.path.join("videos", f).replace("\\", "/")
                is_active = (
                    stream_manager.source_mode == "video"
                    and os.path.basename(stream_manager.video_path or "") == f
                )
                results.append({
                    "filename": f,
                    "relative_path": rel_p,
                    "size_mb": round(os.path.getsize(fp) / (1024 * 1024), 2),
                    "is_active": is_active,
                })
    return {"videos": results, "active_source": stream_manager.source_mode, "current_video": stream_manager.video_path}


@app.post("/api/v1/source/simulation")
def switch_to_simulation():
    """Switch vision feed to synthetic crowd simulation."""
    stream_manager.set_source_simulation()
    return {"status": "ok", "mode": "simulation"}


@app.post("/api/v1/source/camera")
def switch_to_camera(camera_index: int = 0):
    """Switch vision feed to physical camera device."""
    stream_manager.set_source_camera(camera_index)
    return {"status": "ok", "mode": "camera", "device": camera_index}


@app.post("/api/v1/trigger-mock-surge")
def trigger_mock_surge():
    """Simulate a sudden stampede panic surge for testing mobile & dashboard alerts."""
    stream_manager.sim_tick += 120
    return {"status": "surge_simulated"}


@app.get("/api/v1/telemetry/recent")
async def get_recent_telemetry(limit: int = 15):
    """Fetch the latest persisted telemetry rows directly from the PostgreSQL database."""
    storage = stream_manager.telemetry_storage
    if storage is None or not storage.is_connected:
        return {"connected": False, "records": []}
    records = await storage.fetch_recent_telemetry(limit=limit)
    return {"connected": True, "count": len(records), "records": records}


@app.get("/api/v1/telemetry/stats")
async def get_telemetry_stats():
    """Returns database connection status, overall stored record counts, and pipeline stats."""
    storage = stream_manager.telemetry_storage
    if storage is None:
        return {"enabled": False, "message": "PostgreSQL telemetry disabled"}
    summary = await storage.fetch_db_summary()
    return {
        "enabled": True,
        "database": summary,
        "camera_id": config.camera_id,
        "location_name": config.location_name,
        "queue_depth": storage.queue_depth,
        "postgres_dsn": config.postgres_dsn.split("@")[-1],
    }


@app.post("/api/v1/process-frame", response_model=FrameDensityResult)
async def process_single_frame(file: UploadFile = File(...)):
    """Ingest a single frame via multipart/form-data upload and return density result."""
    try:
        contents = await file.read()
        nparr = np.frombuffer(contents, np.uint8)
        frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

        if frame is None:
            raise HTTPException(status_code=400, detail="Invalid image bytes.")

        result = stream_manager.pipeline.process_frame(frame)
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/api/v1/telemetry/status")
def telemetry_db_status():
    """Returns current PostGIS telemetry pipeline status, queue depth, and flush stats."""
    storage = stream_manager.telemetry_storage
    if storage is None:
        return {
            "enabled": False,
            "message": "PostgreSQL telemetry disabled. Set POSTGRES_ENABLED=true to activate.",
            "camera_id": config.camera_id,
            "location_name": config.location_name,
        }
    return {
        "enabled": True,
        "connected": storage.is_connected,
        "camera_id": config.camera_id,
        "location_name": config.location_name,
        "queue_depth": storage.queue_depth,
        "stats": storage.stats,
        "postgres_dsn": config.postgres_dsn.split("@")[-1],  # host/db only — no credentials
        "batch_size": config.postgres_batch_size,
        "flush_interval_sec": config.postgres_flush_interval_sec,
        "throttling": {
            "log_interval_frames": config.postgres_log_interval_frames,
            "target_telemetry_fps": config.postgres_telemetry_target_fps,
            "force_log_on_critical": config.postgres_force_log_on_critical,
        },
    }


class IncidentModel(BaseModel):
    description: str
    latitude: float = 0.0
    longitude: float = 0.0


@app.post("/api/v1/incidents")
async def report_incident(incident: IncidentModel):
    """Endpoint for mobile app com.crowdshield.stampede offline sync & hazard reporting."""
    return {
        "status": "success",
        "incident_id": f"inc_{int(time.time())}",
        "received_at": int(time.time() * 1000),
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app:app", host="0.0.0.0", port=8000, reload=False)
