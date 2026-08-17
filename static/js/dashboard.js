/**
 * CrowdShield Web Command Dashboard - Real-time WebSocket Client & Telemetry Visualizer
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

    this.initChart();
    this.initGrid();
    this.connectWebSocket();
    this.bindEvents();
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
    document.getElementById('statClusters').textContent = data.clusters.length || 0;
    document.getElementById('statVelocity').textContent = `${(data.movement.avg_velocity_px || 0).toFixed(1)} px`;
    document.getElementById('statAccelVar').textContent = (data.movement.accel_variance || 0).toFixed(2);
    document.getElementById('statFps').textContent = (data.fps || 0).toFixed(1);
    document.getElementById('statMobileClients').textContent = data.clientStats.mobileClients || 0;

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
      osc.frequency.setValueAtTime(880, this.audioContext.currentTime); // A5 note
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

  bindEvents() {
    document.getElementById('btnSimMode')?.addEventListener('click', async () => {
      await fetch('/api/v1/source/simulation', { method: 'POST' });
      document.getElementById('btnSimMode').classList.add('active');
      document.getElementById('btnCamMode').classList.remove('active');
    });

    document.getElementById('btnCamMode')?.addEventListener('click', async () => {
      await fetch('/api/v1/source/camera', { method: 'POST' });
      document.getElementById('btnCamMode').classList.add('active');
      document.getElementById('btnSimMode').classList.remove('active');
    });

    document.getElementById('btnTriggerSos')?.addEventListener('click', async () => {
      await fetch('/api/v1/trigger-mock-surge', { method: 'POST' });
    });
  }
}

window.addEventListener('DOMContentLoaded', () => {
  window.crowdDashboard = new CrowdShieldDashboard();
});
