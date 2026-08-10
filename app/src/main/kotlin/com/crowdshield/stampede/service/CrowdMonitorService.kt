package com.crowdshield.stampede.service

import android.app.*
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.crowdshield.stampede.domain.RiskCalculator
import com.crowdshield.stampede.domain.RiskScore
import com.crowdshield.stampede.notification.AlertManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlin.math.sqrt

@AndroidEntryPoint
class CrowdMonitorService : Service(), SensorEventListener {

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    
    @Inject lateinit var riskCalculator: RiskCalculator
    @Inject lateinit var alertManager: AlertManager

    private val _currentRisk = MutableStateFlow<RiskScore?>(null)
    val currentRisk = _currentRisk.asStateFlow()

    private var mockEnabled = false
    private var lastAccelValues = FloatArray(3)

    inner class LocalBinder : Binder() {
        fun getService(): CrowdMonitorService = this@CrowdMonitorService
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        startForeground(1, createNotification("Monitoring Crowd Safety", "Service is running"))
        
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun createNotification(title: String, content: String): Notification {
        return NotificationCompat.Builder(this, AlertManager.CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        serviceScope.cancel()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            
            val magnitude = sqrt(x*x + y*y + z*z)
            // Logic to calculate variance or sudden movements
            processSensorData(magnitude.toDouble())
        }
    }

    private fun processSensorData(magnitude: Double) {
        if (mockEnabled) return // Use mock loop instead
        
        serviceScope.launch {
            // Simplified: calculate risk based on sensor magnitude as a proxy for activity
            val mockDensity = 2.0 // Normally fetched from backend
            val risk = riskCalculator.calculateRisk(mockDensity, 1.0, magnitude / 10.0)
            _currentRisk.value = risk
            
            if (risk.score > 7.5) {
                alertManager.showAlert("HIGH RISK DETECTED", "Possible stampede conditions in your area!")
            }
        }
    }

    fun toggleMockData(enabled: Boolean) {
        mockEnabled = enabled
        if (enabled) {
            startMockLoop()
        }
    }

    private fun startMockLoop() {
        serviceScope.launch {
            while (mockEnabled) {
                val mockDensity = (2.0 + Math.random() * 6.0) // 2 to 8
                val mockVelocity = Math.random() * 2.0
                val mockAccelVar = Math.random() * 5.0
                
                val risk = riskCalculator.calculateRisk(mockDensity, mockVelocity, mockAccelVar)
                _currentRisk.value = risk
                
                if (risk.score > 7.5) {
                    alertManager.showAlert("MOCK ALERT", "High risk simulated: ${risk.score}")
                }
                delay(3000)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
