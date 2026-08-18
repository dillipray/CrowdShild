package com.crowdshield.stampede.ui;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000`\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u000e\u001a\u00020\u000fJ\u0006\u0010\u0010\u001a\u00020\u000fJ\u0006\u0010\u0011\u001a\u00020\u000fJ\u000e\u0010\u0012\u001a\u00020\u000f2\u0006\u0010\u0013\u001a\u00020\u0014J\u0016\u0010\u0015\u001a\u00020\u000f2\u000e\b\u0002\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0017J\u001e\u0010\u0018\u001a\u00020\u000f2\u0006\u0010\u0019\u001a\u00020\u001a2\u000e\b\u0002\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0017J\u001e\u0010\u001c\u001a\u00020\u000f2\u0006\u0010\u001d\u001a\u00020\u001a2\u000e\b\u0002\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0017J\u000e\u0010\u001e\u001a\u00020\u000f2\u0006\u0010\u001f\u001a\u00020 J\u0016\u0010!\u001a\u00020\u000f2\u000e\b\u0002\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0017J\u000e\u0010\"\u001a\u00020\u000f2\u0006\u0010#\u001a\u00020$J\u000e\u0010%\u001a\u00020\u000f2\u0006\u0010&\u001a\u00020\'R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\b\u001a\u0004\u0018\u00010\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00070\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\r\u00a8\u0006("}, d2 = {"Lcom/crowdshield/stampede/ui/MainViewModel;", "Landroidx/lifecycle/ViewModel;", "incidentDao", "Lcom/crowdshield/stampede/data/IncidentDao;", "(Lcom/crowdshield/stampede/data/IncidentDao;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/crowdshield/stampede/ui/DashboardUiState;", "countdownJob", "Lkotlinx/coroutines/Job;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "cancelSosCountdown", "", "dismissBannerMessage", "resetSosDispatch", "setSimulationPreset", "severity", "Lcom/crowdshield/stampede/domain/SeverityLevel;", "startSosCountdown", "onDispatched", "Lkotlin/Function0;", "submitIncidentReport", "description", "", "onComplete", "submitQuickHazard", "hazardType", "toggleMock", "enabled", "", "triggerEmergency", "updateRisk", "risk", "Lcom/crowdshield/stampede/domain/RiskScore;", "updateRiskSignal", "signal", "Lcom/crowdshield/stampede/domain/RiskSignal;", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class MainViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.crowdshield.stampede.data.IncidentDao incidentDao = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.crowdshield.stampede.ui.DashboardUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.crowdshield.stampede.ui.DashboardUiState> uiState = null;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job countdownJob;
    
    @javax.inject.Inject()
    public MainViewModel(@org.jetbrains.annotations.NotNull()
    com.crowdshield.stampede.data.IncidentDao incidentDao) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.crowdshield.stampede.ui.DashboardUiState> getUiState() {
        return null;
    }
    
    public final void updateRisk(@org.jetbrains.annotations.NotNull()
    com.crowdshield.stampede.domain.RiskScore risk) {
    }
    
    public final void updateRiskSignal(@org.jetbrains.annotations.NotNull()
    com.crowdshield.stampede.domain.RiskSignal signal) {
    }
    
    public final void toggleMock(boolean enabled) {
    }
    
    public final void setSimulationPreset(@org.jetbrains.annotations.NotNull()
    com.crowdshield.stampede.domain.SeverityLevel severity) {
    }
    
    public final void startSosCountdown(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDispatched) {
    }
    
    public final void cancelSosCountdown() {
    }
    
    public final void resetSosDispatch() {
    }
    
    public final void triggerEmergency(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDispatched) {
    }
    
    public final void submitQuickHazard(@org.jetbrains.annotations.NotNull()
    java.lang.String hazardType, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onComplete) {
    }
    
    public final void dismissBannerMessage() {
    }
    
    public final void submitIncidentReport(@org.jetbrains.annotations.NotNull()
    java.lang.String description, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onComplete) {
    }
}