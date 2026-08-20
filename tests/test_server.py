"""
Integration tests for FastAPI WebSockets, Vision Streaming, and Mobile App Telemetry.
"""

import pytest
import cv2
import json
import numpy as np
from fastapi.testclient import TestClient
from app import app, stream_manager


def test_health_endpoint():
    """Verify health status endpoint."""
    with TestClient(app) as client:
        response = client.get("/health")
        assert response.status_code == 200
        data = response.json()
        assert data["status"] == "healthy"


def test_serve_dashboard_html():
    """Verify web dashboard HTML is served at root."""
    with TestClient(app) as client:
        response = client.get("/")
        assert response.status_code == 200
        assert "CrowdShield" in response.text


def test_process_frame_rest_endpoint():
    """Verify multipart frame upload and detection response."""
    dummy_img = np.zeros((480, 640, 3), dtype=np.uint8)
    _, encoded = cv2.imencode(".jpg", dummy_img)

    with TestClient(app) as client:
        response = client.post(
            "/api/v1/process-frame",
            files={"file": ("frame.jpg", encoded.tobytes(), "image/jpeg")},
        )
        assert response.status_code == 200
        data = response.json()
        assert "risk_score" in data
        assert "summary" in data
        assert "grid_density" in data


def test_mobile_websocket_telemetry_stream():
    """Verify WebSocket connection and telemetry format for Android (com.crowdshield.stampede)."""
    with TestClient(app) as client:
        with client.websocket_connect("/ws/mobile") as websocket:
            # Client connects and awaits telemetry broadcast from background worker
            data = websocket.receive_json()
            assert data["type"] == "CROWD_TELEMETRY"
            assert data["package"] == "com.crowdshield.stampede"
            assert "riskScore" in data
            assert "crowdMetrics" in data
            assert "clusters" in data
            assert "alert" in data


def test_dashboard_websocket_telemetry_stream():
    """Verify WebSocket connection and telemetry format for Web Dashboard."""
    with TestClient(app) as client:
        with client.websocket_connect("/ws/dashboard") as websocket:
            data = websocket.receive_json()
            assert data["type"] == "DASHBOARD_TELEMETRY"
            assert "gridDensity" in data
            assert "summary" in data
            assert "riskScore" in data


def test_video_upload_endpoint(tmp_path):
    """Verify custom video file upload and switching."""
    test_video_path = tmp_path / "valid_test.mp4"
    fourcc = cv2.VideoWriter_fourcc(*"mp4v")
    out = cv2.VideoWriter(str(test_video_path), fourcc, 10.0, (160, 120))
    for _ in range(5):
        frame = np.zeros((120, 160, 3), dtype=np.uint8)
        out.write(frame)
    out.release()

    with open(test_video_path, "rb") as f:
        video_bytes = f.read()

    with TestClient(app) as client:
        response = client.post(
            "/api/v1/videos/upload",
            files={"file": ("test_custom_crowd.mp4", video_bytes, "video/mp4")},
        )
        assert response.status_code == 200
        data = response.json()
        assert data["status"] == "ok"
        assert data["filename"] == "test_custom_crowd.mp4"
        assert "videos/test_custom_crowd.mp4" in data["video_path"]

