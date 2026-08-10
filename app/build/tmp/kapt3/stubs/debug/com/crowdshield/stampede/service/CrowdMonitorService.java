package com.crowdshield.stampede.service;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0090\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0014\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0006\n\u0002\b\u0005\b\u0007\u0018\u00002\u00020\u00012\u00020\u0002:\u0001?B\u0005\u00a2\u0006\u0002\u0010\u0003J\u0018\u0010#\u001a\u00020$2\u0006\u0010%\u001a\u00020&2\u0006\u0010\'\u001a\u00020&H\u0002J\u001a\u0010(\u001a\u00020)2\b\u0010*\u001a\u0004\u0018\u00010\b2\u0006\u0010+\u001a\u00020,H\u0016J\u0012\u0010-\u001a\u00020.2\b\u0010/\u001a\u0004\u0018\u000100H\u0016J\b\u00101\u001a\u00020)H\u0016J\b\u00102\u001a\u00020)H\u0016J\u0012\u00103\u001a\u00020)2\b\u00104\u001a\u0004\u0018\u000105H\u0016J\"\u00106\u001a\u00020,2\b\u0010/\u001a\u0004\u0018\u0001002\u0006\u00107\u001a\u00020,2\u0006\u00108\u001a\u00020,H\u0016J\u0010\u00109\u001a\u00020)2\u0006\u0010:\u001a\u00020;H\u0002J\b\u0010<\u001a\u00020)H\u0002J\u000e\u0010=\u001a\u00020)2\u0006\u0010>\u001a\u00020\u0018R\u0016\u0010\u0004\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00060\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001e\u0010\t\u001a\u00020\n8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000eR\u0012\u0010\u000f\u001a\u00060\u0010R\u00020\u0000X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u0011\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00060\u0012\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u000e\u0010\u0015\u001a\u00020\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0017\u001a\u00020\u0018X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001e\u0010\u0019\u001a\u00020\u001a8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001b\u0010\u001c\"\u0004\b\u001d\u0010\u001eR\u000e\u0010\u001f\u001a\u00020 X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010!\u001a\u00020\"X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006@"}, d2 = {"Lcom/crowdshield/stampede/service/CrowdMonitorService;", "Landroid/app/Service;", "Landroid/hardware/SensorEventListener;", "()V", "_currentRisk", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/crowdshield/stampede/domain/RiskScore;", "accelerometer", "Landroid/hardware/Sensor;", "alertManager", "Lcom/crowdshield/stampede/notification/AlertManager;", "getAlertManager", "()Lcom/crowdshield/stampede/notification/AlertManager;", "setAlertManager", "(Lcom/crowdshield/stampede/notification/AlertManager;)V", "binder", "Lcom/crowdshield/stampede/service/CrowdMonitorService$LocalBinder;", "currentRisk", "Lkotlinx/coroutines/flow/StateFlow;", "getCurrentRisk", "()Lkotlinx/coroutines/flow/StateFlow;", "lastAccelValues", "", "mockEnabled", "", "riskCalculator", "Lcom/crowdshield/stampede/domain/RiskCalculator;", "getRiskCalculator", "()Lcom/crowdshield/stampede/domain/RiskCalculator;", "setRiskCalculator", "(Lcom/crowdshield/stampede/domain/RiskCalculator;)V", "sensorManager", "Landroid/hardware/SensorManager;", "serviceScope", "Lkotlinx/coroutines/CoroutineScope;", "createNotification", "Landroid/app/Notification;", "title", "", "content", "onAccuracyChanged", "", "sensor", "accuracy", "", "onBind", "Landroid/os/IBinder;", "intent", "Landroid/content/Intent;", "onCreate", "onDestroy", "onSensorChanged", "event", "Landroid/hardware/SensorEvent;", "onStartCommand", "flags", "startId", "processSensorData", "magnitude", "", "startMockLoop", "toggleMockData", "enabled", "LocalBinder", "app_debug"})
public final class CrowdMonitorService extends android.app.Service implements android.hardware.SensorEventListener {
    @org.jetbrains.annotations.NotNull()
    private final com.crowdshield.stampede.service.CrowdMonitorService.LocalBinder binder = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope serviceScope = null;
    private android.hardware.SensorManager sensorManager;
    @org.jetbrains.annotations.Nullable()
    private android.hardware.Sensor accelerometer;
    @javax.inject.Inject()
    public com.crowdshield.stampede.domain.RiskCalculator riskCalculator;
    @javax.inject.Inject()
    public com.crowdshield.stampede.notification.AlertManager alertManager;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.crowdshield.stampede.domain.RiskScore> _currentRisk = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.crowdshield.stampede.domain.RiskScore> currentRisk = null;
    private boolean mockEnabled = false;
    @org.jetbrains.annotations.NotNull()
    private float[] lastAccelValues;
    
    public CrowdMonitorService() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.crowdshield.stampede.domain.RiskCalculator getRiskCalculator() {
        return null;
    }
    
    public final void setRiskCalculator(@org.jetbrains.annotations.NotNull()
    com.crowdshield.stampede.domain.RiskCalculator p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.crowdshield.stampede.notification.AlertManager getAlertManager() {
        return null;
    }
    
    public final void setAlertManager(@org.jetbrains.annotations.NotNull()
    com.crowdshield.stampede.notification.AlertManager p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.crowdshield.stampede.domain.RiskScore> getCurrentRisk() {
        return null;
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
    
    private final android.app.Notification createNotification(java.lang.String title, java.lang.String content) {
        return null;
    }
    
    @java.lang.Override()
    public int onStartCommand(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent, int flags, int startId) {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public android.os.IBinder onBind(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent) {
        return null;
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
    
    @java.lang.Override()
    public void onSensorChanged(@org.jetbrains.annotations.Nullable()
    android.hardware.SensorEvent event) {
    }
    
    private final void processSensorData(double magnitude) {
    }
    
    public final void toggleMockData(boolean enabled) {
    }
    
    private final void startMockLoop() {
    }
    
    @java.lang.Override()
    public void onAccuracyChanged(@org.jetbrains.annotations.Nullable()
    android.hardware.Sensor sensor, int accuracy) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0004\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0003\u001a\u00020\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/crowdshield/stampede/service/CrowdMonitorService$LocalBinder;", "Landroid/os/Binder;", "(Lcom/crowdshield/stampede/service/CrowdMonitorService;)V", "getService", "Lcom/crowdshield/stampede/service/CrowdMonitorService;", "app_debug"})
    public final class LocalBinder extends android.os.Binder {
        
        public LocalBinder() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.crowdshield.stampede.service.CrowdMonitorService getService() {
            return null;
        }
    }
}