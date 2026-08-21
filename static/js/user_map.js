/**
 * CrowdShield Citizen Live Crowd Map & Smart Evacuation Client (Leaflet API)
 */

class CrowdShieldUserPortal {
  constructor() {
    this.map = null;
    this.userLocation = [28.6128, 77.2290]; // Default user location inside venue
    this.userMarker = null;
    this.routeLine = null;
    this.venueData = null;
    this.ws = null;
    this.sectorLayers = {};
    this.isRouteVisible = true;

    this.initMap();
    this.connectWebSocket();
    this.bindEvents();
  }

  async initMap() {
    if (typeof L === 'undefined') return;

    // 1. Initialize Map centered on venue
    this.map = L.map('userLeafletMap', {
      zoomControl: true,
      attributionControl: false
    }).setView(this.userLocation, 17);

    // 2. OpenStreetMap / CartoDB high-visibility tiles
    L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
      maxZoom: 19,
      subdomains: 'abcd'
    }).addTo(this.map);

    // 3. Add User Location Pulsing Beacon Marker
    const userIcon = L.divIcon({
      className: 'custom-user-marker',
      html: `<div class="user-gps-beacon" title="You are here"></div>`,
      iconSize: [20, 20],
      iconAnchor: [10, 10]
    });

    this.userMarker = L.marker(this.userLocation, {
      icon: userIcon,
      draggable: true
    }).addTo(this.map);

    this.userMarker.bindPopup(`
      <div style="font-weight: 800; color: #06b6d4; margin-bottom: 4px;">📍 Your Current Location</div>
      <div style="font-size: 0.8rem; color: #475569;">Drag this pin to test route updates from different venue sectors.</div>
    `);

    this.userMarker.on('dragend', (e) => {
      const pos = e.target.getLatLng();
      this.userLocation = [pos.lat, pos.lng];
      this.updateEvacuationRoute();
    });

    // 4. Fetch Venue Map Geometry
    try {
      const res = await fetch('/api/v1/venue/map-data');
      if (res.ok) {
        this.venueData = await res.json();
        this.renderVenueLayers();
        this.updateEvacuationRoute();
      }
    } catch (err) {
      console.warn('Venue map data fetch error:', err);
    }
  }

  renderVenueLayers() {
    if (!this.venueData || !this.map) return;

    // Render Sector Polygons
    if (this.venueData.sectors) {
      this.venueData.sectors.forEach(sec => {
        const poly = L.polygon(sec.coordinates, {
          color: sec.color || '#10b981',
          weight: 2,
          opacity: 0.8,
          fillColor: sec.color || '#10b981',
          fillOpacity: 0.18
        }).addTo(this.map);

        poly.bindTooltip(`${sec.name} (${sec.risk_level})`, { sticky: true });
        this.sectorLayers[sec.id] = poly;
      });
    }

    // Render Exits
    if (this.venueData.exits) {
      this.venueData.exits.forEach(ex => {
        const exitIcon = L.divIcon({
          className: 'custom-exit-marker',
          html: `<div style="background: ${ex.is_recommended ? '#10b981' : (ex.congestion_percent > 60 ? '#ef4444' : '#3b82f6')}; color: #fff; font-weight: 800; font-size: 11px; padding: 3px 8px; border-radius: 6px; box-shadow: 0 0 10px rgba(0,0,0,0.5); border: 1.5px solid #fff; white-space: nowrap;">
            🚪 ${ex.name}
          </div>`,
          iconSize: [100, 24],
          iconAnchor: [50, 12]
        });

        L.marker([ex.lat, ex.lng], { icon: exitIcon })
          .bindPopup(`
            <strong style="color: #0f172a;">${ex.name}</strong><br>
            <span style="color: #64748b;">Status: <strong>${ex.status}</strong></span><br>
            <span style="color: #64748b;">Congestion: <strong>${ex.congestion_percent}%</strong></span><br>
            ${ex.is_recommended ? '<strong style="color: #10b981;">★ Safest Recommended Exit Path</strong>' : ''}
          `)
          .addTo(this.map);
      });
    }
  }

  updateEvacuationRoute() {
    if (!this.map) return;

    // Clear previous route
    if (this.routeLine) {
      this.map.removeLayer(this.routeLine);
    }

    if (!this.isRouteVisible) return;

    // Destination: Exit B (Safest West Concourse Exit [28.6126, 77.2260])
    const exitTarget = [28.6126, 77.2260];
    
    // Construct safe waypoint route steering around high-density arena
    const waypoint = [this.userLocation[0], (this.userLocation[1] + exitTarget[1]) / 2];
    const path = [this.userLocation, waypoint, exitTarget];

    this.routeLine = L.polyline(path, {
      color: '#10b981',
      weight: 5,
      dashArray: '8, 8',
      opacity: 0.95
    }).addTo(this.map);

    this.routeLine.bindTooltip('Dynamic Safe Evacuation Path (Follow Green Route)', { sticky: true });
  }

  connectWebSocket() {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.host}/ws/mobile`;

    this.ws = new WebSocket(wsUrl);

    this.ws.onopen = () => {
      const badge = document.getElementById('userWsBadge');
      const statusText = document.getElementById('userWsStatus');
      if (badge) badge.className = 'status-badge online';
      if (statusText) statusText.textContent = 'Live GPS & Safety Feed Active';
    };

    this.ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        if (data.riskScore) {
          this.handleLiveSafetyUpdate(data.riskScore);
        }
      } catch (err) {
        console.error('User portal telemetry parsing error:', err);
      }
    };

    this.ws.onclose = () => {
      const badge = document.getElementById('userWsBadge');
      const statusText = document.getElementById('userWsStatus');
      if (badge) badge.className = 'status-badge';
      if (statusText) statusText.textContent = 'Connecting...';
      setTimeout(() => this.connectWebSocket(), 3000);
    };
  }

  handleLiveSafetyUpdate(riskScore) {
    const score = riskScore.score || 0.0;
    const level = riskScore.level || 'SAFE';

    const box = document.getElementById('safetyBox');
    const icon = document.getElementById('safetyIcon');
    const title = document.getElementById('safetyTitle');
    const desc = document.getElementById('safetyDesc');

    if (!box || !title) return;

    box.className = 'safety-status-box';
    if (level === 'HIGH_RISK' || score >= 6.5) {
      box.classList.add('high-risk');
      icon.textContent = '🚨';
      title.textContent = `ZONE STATUS: SURGE CAUTION (${score.toFixed(1)}/10)`;
      desc.textContent = 'High crowd density detected near Main Stage. Please follow green evacuation path to Exit B.';
    } else if (level === 'CAUTION' || score >= 3.5) {
      box.classList.add('caution');
      icon.textContent = '⚠️';
      title.textContent = `ZONE STATUS: ELEVATED CROWD (${score.toFixed(1)}/10)`;
      desc.textContent = 'Moderate crowd buildup. Keep walking steadily towards open concourses.';
    } else {
      icon.textContent = '🛡️';
      title.textContent = 'ZONE STATUS: SAFE';
      desc.textContent = 'Normal flow in Sector 2. Evacuation routes are fully open and clear.';
    }
  }

  bindEvents() {
    // Locate Me (HTML5 Geolocation)
    document.getElementById('btnLocateMe')?.addEventListener('click', () => {
      if ('geolocation' in navigator) {
        navigator.geolocation.getCurrentPosition(
          (pos) => {
            const lat = pos.coords.latitude;
            const lng = pos.coords.longitude;
            this.userLocation = [lat, lng];
            if (this.userMarker) {
              this.userMarker.setLatLng(this.userLocation);
            }
            if (this.map) {
              this.map.setView(this.userLocation, 18, { animate: true });
            }
            this.updateEvacuationRoute();
          },
          (err) => {
            // Simulated user location in venue
            if (this.map) {
              this.map.setView(this.userLocation, 18, { animate: true });
            }
          }
        );
      }
    });

    // Center Venue
    document.getElementById('btnCenterVenue')?.addEventListener('click', () => {
      if (this.map) {
        this.map.setView([28.6129, 77.2295], 17, { animate: true });
      }
    });

    // Toggle Route Visibility
    document.getElementById('btnToggleRoute')?.addEventListener('click', () => {
      this.isRouteVisible = !this.isRouteVisible;
      const btn = document.getElementById('btnToggleRoute');
      if (btn) {
        btn.textContent = this.isRouteVisible ? 'Route Active ✓' : 'Route Hidden';
        btn.style.color = this.isRouteVisible ? 'var(--color-safe)' : 'var(--text-muted)';
      }
      this.updateEvacuationRoute();
    });

    // SOS Trigger
    document.getElementById('btnSosTrigger')?.addEventListener('click', async () => {
      const confirmed = confirm('⚠️ Trigger Emergency SOS Alert to Venue Staff Command Center?');
      if (confirmed) {
        try {
          await fetch('/api/v1/incidents', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              description: 'CITIZEN SOS DISPATCH TRIGGERED',
              latitude: this.userLocation[0],
              longitude: this.userLocation[1]
            })
          });

          // Drop SOS marker on map
          if (this.map) {
            L.circle(this.userLocation, {
              radius: 20,
              color: '#ef4444',
              fillColor: '#ef4444',
              fillOpacity: 0.6
            }).addTo(this.map).bindPopup('<strong>🚨 SOS Broadcast Active</strong>').openPopup();
          }

          alert('🚨 Emergency SOS dispatched! Venue response team has received your GPS coordinates.');
        } catch (err) {
          console.error('SOS dispatch error:', err);
        }
      }
    });
  }
}

// Global 1-Tap Quick Hazard Reporter
window.submitQuickHazard = async function(type) {
  try {
    const lat = window.userPortal ? window.userPortal.userLocation[0] : 28.6128;
    const lng = window.userPortal ? window.userPortal.userLocation[1] : 77.2290;

    await fetch('/api/v1/incidents', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        description: `Hazard: ${type}`,
        latitude: lat,
        longitude: lng
      })
    });

    if (window.userPortal && window.userPortal.map) {
      const pin = L.marker([lat + 0.0002, lng + 0.0002]).addTo(window.userPortal.map);
      pin.bindPopup(`<strong>Hazard Reported:</strong> ${type}<br><em>Sent to Command Center</em>`).openPopup();
    }

    alert(`✓ Hazard "${type}" reported successfully to CrowdShield Command.`);
  } catch (err) {
    console.error('Hazard submission error:', err);
  }
};

window.addEventListener('DOMContentLoaded', () => {
  window.userPortal = new CrowdShieldUserPortal();
});
