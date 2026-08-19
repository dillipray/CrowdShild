/**
 * CrowdShield Web Command Dashboard - Real-time WebSocket Client, Video Switcher & Telemetry Visualizer
 */

class CrowdShieldDashboard {
  constructor() {
    this.ws = null;
    this.reconnectInterval = 2000;
    this.chart = null;
    this.historyData = {
      labels: [],
      riskScores: [],
      humanCounts: [],
      accelVariances: [],
    };
    this.maxHistoryPoints = 25;
    this.audioContext = null;
    this.activeSource = 'large_crowd.mp4';

    this.initChart();
    this.initGrid();
    this.connectWebSocket();
    this.bindEvents();
    this.startDbPolling();
  }

  initChart() {
    const ctx = document.getElementById('riskChart').getContext('2d');
    this.chart = new Chart(ctx, {
      type: 'line',
      data: {
        labels: [],
        datasets: [
          {
            label: 'Risk Score (0-10)',
            data: [],
            borderColor: '#ef4444',
            backgroundColor: 'rgba(239, 68, 68, 0.1)',
            fill: true,
            tension: 0.3,
            borderWidth: 2,
            yAxisID: 'yRisk',
          },
          {
            label: 'Humans Count',
            data: [],
            borderColor: '#06b6d4',
            backgroundColor: 'transparent',
            borderWidth: 2,
            tension: 0.3,
            yAxisID: 'yCount',
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        animation: { duration: 200 },
        plugins: {
          legend: {
            labels: { color: '#94a3b8', font: { size: 10 } }
          }
        },
        scales: {
          x: {
            display: false
          },
          yRisk: {
            type: 'linear',
            position: 'left',
            min: 0,
            max: 10,
            grid: { color: 'rgba(255, 255, 255, 0.05)' },
            ticks: { color: '#ef4444', font: { size: 10 } }
          },
          yCount: {
            type: 'linear',
            position: 'right',
            min: 0,
            grid: { display: false },
            ticks: { color: '#06b6d4', font: { size: 10 } }
          }
        }
      }
    });
  }

  initGrid() {
    const gridContainer = document.getElementById('spatialGrid');
    gridContainer.innerHTML = '';
    for (let r = 0; r < 4; r++) {
      for (let c = 0; c < 4; c++) {
        const cell = document.createElement('div');
        cell.className = 'grid-cell';
        cell.id = `cell-${r}-${c}`;
        cell.innerHTML = `
          <span class="cell-count" id="count-${r}-${c}">0</span>
          <span class="cell-label">R${r+1}C${c+1}</span>
        `;
        gridContainer.appendChild(cell);
      }
    }
  }

  connectWebSocket() {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.host}/ws/dashboard`;

    this.ws = new WebSocket(wsUrl);

    this.ws.onopen = () => {
      document.getElementById('wsStatus').textContent = 'Live Connected';
      document.getElementById('wsBadge').className = 'badge online';
    };

    this.ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        if (data.type === 'DASHBOARD_TELEMETRY') {
          this.handleTelemetry(data);
        }
      } catch (err) {
        console.error('Error parsing telemetry JSON:', err);
      }
    };

    this.ws.onclose = () => {
      document.getElementById('wsStatus').textContent = 'Reconnecting...';
      document.getElementById('wsBadge').className = 'badge';
      setTimeout(() => this.connectWebSocket(), this.reconnectInterval);
    };

    this.ws.onerror = () => {
      this.ws.close();
    };
  }

  handleTelemetry(data) {
    // 1. Update Top Stats
    document.getElementById('statHumans').textContent = data.summary.total_humans || 0;
    document.getElementById('statHotspots').textContent = data.summary.hotspot_count || 0;
    document.getElementById('statClusters').textContent = data.clusters ? data.clusters.length : 0;
    document.getElementById('statVelocity').textContent = `${(data.movement.avg_velocity_px || 0).toFixed(1)} px`;
    document.getElementById('statAccelVar').textContent = (data.movement.accel_variance || 0).toFixed(2);
    document.getElementById('statFps').textContent = (data.fps || 0).toFixed(1);
    document.getElementById('statMobileClients').textContent = data.clientStats ? data.clientStats.mobileClients || 0 : 0;

    // 2. Update Risk Gauge
    const score = data.riskScore.score || 0.0;
    const level = data.riskScore.level || 'SAFE';
    const breakdown = data.riskScore.breakdown || {};

    const gaugeValEl = document.getElementById('gaugeVal');
    const gaugeCircle = document.getElementById('gaugeCircle');
    const gaugeLabel = document.getElementById('gaugeLabel');

    gaugeValEl.textContent = score.toFixed(1);
    gaugeLabel.textContent = level;

    // Set color based on level
    let gaugeColor = '#10b981';
    if (level === 'HIGH_RISK') gaugeColor = '#ef4444';
    else if (level === 'CAUTION') gaugeColor = '#f59e0b';

    const pct = Math.min(100, Math.max(0, (score / 10.0) * 100));
    gaugeCircle.style.background = `conic-gradient(${gaugeColor} 0%, ${gaugeColor} ${pct}%, #1e293b ${pct}%)`;
    gaugeValEl.style.color = gaugeColor;

    // 3. Update Breakdown Bars
    const dFactor = breakdown.density_factor || 0.0;
    const vFactor = breakdown.velocity_factor || 0.0;
    const aFactor = breakdown.accel_variance_factor || 0.0;

    document.getElementById('barDensityVal').textContent = `${dFactor.toFixed(1)} / 6.0`;
    document.getElementById('barDensityFill').style.width = `${(dFactor / 6.0) * 100}%`;
    document.getElementById('barDensityFill').style.backgroundColor = gaugeColor;

    document.getElementById('barVelocityVal').textContent = `${vFactor.toFixed(1)} / 2.0`;
    document.getElementById('barVelocityFill').style.width = `${(vFactor / 2.0) * 100}%`;
    document.getElementById('barVelocityFill').style.backgroundColor = vFactor > 0 ? '#ef4444' : '#06b6d4';

    document.getElementById('barAccelVal').textContent = `${aFactor.toFixed(1)} / 2.0`;
    document.getElementById('barAccelFill').style.width = `${(aFactor / 2.0) * 100}%`;
    document.getElementById('barAccelFill').style.backgroundColor = aFactor > 1.0 ? '#ef4444' : '#3b82f6';

    // 4. Update Critical Alert Banner
    const criticalBanner = document.getElementById('criticalBanner');
    if (score >= 8.0) {
      criticalBanner.classList.add('active');
      criticalBanner.innerHTML = `⚠️ CRITICAL STAMPEDE RISK (Score: ${score.toFixed(1)}/10) - EMERGENCY PROTOCOL ACTIVE`;
      this.playAlertSound();
    } else {
      criticalBanner.classList.remove('active');
    }

    // 5. Update Spatial Grid Map
    if (data.gridDensity) {
      data.gridDensity.forEach(cell => {
        const cellEl = document.getElementById(`cell-${cell.row}-${cell.col}`);
        const countEl = document.getElementById(`count-${cell.row}-${cell.col}`);
        if (cellEl && countEl) {
          countEl.textContent = cell.human_count;
          cellEl.className = 'grid-cell';
          if (cell.is_hotspot) {
            cellEl.classList.add('hotspot');
          } else if (cell.risk_level === 'CAUTION') {
            cellEl.classList.add('caution');
          }
        }
      });
    }

    // 6. Update Real-Time Chart
    const timeLabel = new Date().toLocaleTimeString().split(' ')[0];
    this.historyData.labels.push(timeLabel);
    this.historyData.riskScores.push(score);
    this.historyData.humanCounts.push(data.summary.total_humans || 0);

    if (this.historyData.labels.length > this.maxHistoryPoints) {
      this.historyData.labels.shift();
      this.historyData.riskScores.shift();
      this.historyData.humanCounts.shift();
    }

    this.chart.data.labels = this.historyData.labels;
    this.chart.datasets[0].data = this.historyData.riskScores;
    this.chart.datasets[1].data = this.historyData.humanCounts;
    this.chart.update('none');
  }

  playAlertSound() {
    try {
      if (!this.audioContext) {
        this.audioContext = new (window.AudioContext || window.webkitAudioContext)();
      }
      if (this.audioContext.state === 'suspended') {
        this.audioContext.resume();
      }
      const osc = this.audioContext.createOscillator();
      const gain = this.audioContext.createGain();
      osc.type = 'sawtooth';
      osc.frequency.setValueAtTime(880, this.audioContext.currentTime);
      osc.frequency.exponentialRampToValueAtTime(440, this.audioContext.currentTime + 0.2);
      gain.gain.setValueAtTime(0.1, this.audioContext.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.01, this.audioContext.currentTime + 0.2);
      osc.connect(gain);
      gain.connect(this.audioContext.destination);
      osc.start();
      osc.stop(this.audioContext.currentTime + 0.2);
    } catch (e) {
      // Audio context might be restricted before user interaction
    }
  }

  setActivePill(activeId, label, path) {
    const pillIds = ['btnLargeCrowd', 'btnMediumCrowd', 'btnSmallCrowd', 'btnCamMode', 'btnSimMode'];
    pillIds.forEach(id => {
      const el = document.getElementById(id);
      if (el) {
        if (id === activeId) el.classList.add('active');
        else el.classList.remove('active');
      }
    });

    const activeSourceTag = document.getElementById('activeSourceTag');
    if (activeSourceTag) activeSourceTag.textContent = `Source: ${label}`;

    const activeSourceLabel = document.getElementById('activeSourceLabel');
    if (activeSourceLabel) activeSourceLabel.textContent = path;
  }

  async switchVideoSource(videoPath, buttonId, label) {
    try {
      const res = await fetch('/api/v1/source/video', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ video_path: videoPath })
      });
      if (res.ok) {
        this.setActivePill(buttonId, label, videoPath);
      }
    } catch (e) {
      console.error('Error switching video source:', e);
    }
  }

  async startDbPolling() {
    const fetchDb = async () => {
      try {
        // 1. Fetch Stats
        const statsRes = await fetch('/api/v1/telemetry/stats');
        if (statsRes.ok) {
          const stats = await statsRes.json();
          if (stats.enabled && stats.database) {
            document.getElementById('dbTotalFrames').textContent = stats.database.total_telemetry_frames || 0;
            document.getElementById('dbTotalClusters').textContent = stats.database.total_clusters_recorded || 0;
            document.getElementById('dbTotalHotspots').textContent = stats.database.total_hotspots_recorded || 0;
            document.getElementById('dbQueueDepth').textContent = stats.queue_depth || 0;
          }
        }

        // 2. Fetch Recent Records
        const recentRes = await fetch('/api/v1/telemetry/recent?limit=8');
        if (recentRes.ok) {
          const recData = await recentRes.json();
          if (recData.connected && recData.records && recData.records.length > 0) {
            const tbody = document.getElementById('dbRecordsBody');
            tbody.innerHTML = recData.records.map(r => {
              const dt = r.captured_at ? r.captured_at.replace('T', ' ').substring(11, 19) : '--';
              let badgeClass = 'badge-risk-safe';
              if (r.risk_level === 'HIGH_RISK') badgeClass = 'badge-risk-high';
              else if (r.risk_level === 'CAUTION') badgeClass = 'badge-risk-caution';

              return `
                <tr>
                  <td><code style="color: #38bdf8;">#${r.id}</code></td>
                  <td>${dt}</td>
                  <td>CAM-${r.camera_id}</td>
                  <td>${r.location_name}</td>
                  <td><strong style="color: #fff;">${r.total_humans}</strong></td>
                  <td><span style="color: ${r.hotspot_count > 0 ? '#ef4444' : '#64748b'};">${r.hotspot_count}</span></td>
                  <td><span class="${badgeClass}">${r.risk_level}</span></td>
                  <td><strong style="color: #f1f5f9;">${r.risk_score.toFixed(2)}</strong></td>
                  <td>${r.avg_velocity.toFixed(1)} px</td>
                </tr>
              `;
            }).join('');
          }
        }
      } catch (err) {
        console.error('Error fetching database telemetry:', err);
      }
    };

    // Initial fetch and 2.5-second periodic refresh
    await fetchDb();
    setInterval(fetchDb, 2500);
  }

  bindEvents() {
    document.getElementById('btnLargeCrowd')?.addEventListener('click', () => {
      this.switchVideoSource('videos/large_crowd.mp4', 'btnLargeCrowd', 'large_crowd.mp4');
    });

    document.getElementById('btnMediumCrowd')?.addEventListener('click', () => {
      this.switchVideoSource('videos/medium_crowd.mp4', 'btnMediumCrowd', 'medium_crowd.mp4');
    });

    document.getElementById('btnSmallCrowd')?.addEventListener('click', () => {
      this.switchVideoSource('videos/small_crowd.mp4', 'btnSmallCrowd', 'small_crowd.mp4');
    });

    document.getElementById('btnCamMode')?.addEventListener('click', async () => {
      await fetch('/api/v1/source/camera', { method: 'POST' });
      this.setActivePill('btnCamMode', 'Live Webcam', 'Device: /dev/video0');
    });

    document.getElementById('btnSimMode')?.addEventListener('click', async () => {
      await fetch('/api/v1/source/simulation', { method: 'POST' });
      this.setActivePill('btnSimMode', 'Synthetic Simulation', 'Synthetic Generator');
    });

    document.getElementById('btnTriggerSos')?.addEventListener('click', async () => {
      await fetch('/api/v1/trigger-mock-surge', { method: 'POST' });
    });
  }
}

window.addEventListener('DOMContentLoaded', () => {
  window.crowdDashboard = new CrowdShieldDashboard();
});
