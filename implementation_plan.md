# Leaflet API Integration for Staff Command & Citizen User Pages

Connect interactive Leaflet.js maps with OpenStreetMap tiles, live telemetry WebSockets, sector risk overlays, exit routes, and real-time incident pins across both the **Staff Command Center** and the **User/Citizen Safety Portal**.

## Proposed Changes

### 1. Web Backend & Endpoints (`app.py`)
- Serve the new Citizen Safety Portal at `/user` loading `static/user.html`.
- Ensure `/ws/mobile`, `/ws/dashboard`, and `/ws/telemetry` supply live GPS coordinates, hotspot coordinates, and sector risk levels for Leaflet layers.
- Add `/api/v1/venue/map-data` endpoint returning GeoJSON/JSON coordinates for sectors, CCTV cameras, emergency exits, and safe paths.

### 2. Staff Command Dashboard (`static/index.html`, `static/js/dashboard.js`, `static/css/dashboard.css`)
- Embed Leaflet (`leaflet.js` & `leaflet.css`) into the Staff Command Center.
- Replace/enhance static spatial boxes with a live interactive **Leaflet Venue Map**:
  - Dark/Light tactical OpenStreetMap tiles (CartoDB / OSM).
  - Real-time Sector Risk polygons (Sectors 1-4) reflecting live YOLO & risk engine output.
  - CCTV camera pins with stream preview popups.
  - Emergency exit status pins (Exit A, B, C) with occupancy rates.
  - Incident markers and live crowd cluster heatmap / radius circles.
  - Interactive layer toggle controls (Risk zones, CCTV cameras, Exits, Incidents).

### 3. Citizen / User Page (`static/user.html`, `static/js/user_map.js`, `static/css/user_map.css`)
- Create a dedicated, mobile-first **Citizen Safety Live Map**:
  - Leaflet map centered on the user's location with live pulsing "You are Here" GPS marker.
  - Dynamic evacuation route polyline guiding the citizen away from high-density hotspots to the nearest safe exit.
  - Live Stampede Risk status indicator connected via WebSocket.
  - Emergency SOS broadcast button and 1-tap incident reporting.
  - Turn-by-turn safe egress instructions.

### 4. Android App Integration (`app/src/main/...`)
- In `app/src/main/kotlin/com/crowdshield/stampede/ui/components/CurrentLocationMap.kt` & `LiveCrowdMapScreen.kt` & `StaffCommandCenterScreen.kt`:
  - Provide interactive Leaflet WebView component (`LeafletMapView`) capable of loading Leaflet OSM maps with real-time GPS tracking and bridge callbacks.
  - Integrate toggle between Leaflet Satellite/OSM view and tactical vector rendering.

## Verification Plan

### Automated Verification
- Run Python test suite: `pytest tests/`
- Validate FastAPI endpoint routing (`/`, `/user`, `/health`, `/api/v1/venue/map-data`).

### Manual & Interactive Verification
- Launch FastAPI backend (`uvicorn app:app --port 8000`).
- Test Staff Command Center map interactions (`http://localhost:8000/`): check pan, zoom, layer toggles, sector click popups, and WebSocket real-time updates.
- Test Citizen User page (`http://localhost:8000/user`): check live geolocation, route rendering to safe exit, and risk level updates.
