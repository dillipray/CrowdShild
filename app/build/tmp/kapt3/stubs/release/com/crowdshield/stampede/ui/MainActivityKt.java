package com.crowdshield.stampede.ui;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u00000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u0007\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u001aB\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0012\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00010\b2\u000e\b\u0002\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00010\bH\u0007\u001a\u0016\u0010\n\u001a\u00020\u00012\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00010\bH\u0007\u001a$\u0010\u000b\u001a\u00020\u00012\u0006\u0010\f\u001a\u00020\u00062\u0012\u0010\r\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00010\u0005H\u0007\u001a\u0010\u0010\u000e\u001a\u00020\u00012\u0006\u0010\u000f\u001a\u00020\u0010H\u0007\u001a\u0018\u0010\u0011\u001a\u00020\u00012\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u0010H\u0007\u00a8\u0006\u0015"}, d2 = {"DashboardScreen", "", "uiState", "Lcom/crowdshield/stampede/ui/DashboardUiState;", "onMockToggle", "Lkotlin/Function1;", "", "onEmergencyClick", "Lkotlin/Function0;", "onReportClick", "EmergencyButtonWithCountdown", "MockDataControl", "enabled", "onToggle", "MockMapView", "riskScore", "", "RiskIndicator", "level", "Lcom/crowdshield/stampede/domain/RiskLevel;", "score", "app_release"})
public final class MainActivityKt {
    
    @androidx.compose.runtime.Composable()
    public static final void DashboardScreen(@org.jetbrains.annotations.NotNull()
    com.crowdshield.stampede.ui.DashboardUiState uiState, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onMockToggle, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onEmergencyClick, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onReportClick) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void RiskIndicator(@org.jetbrains.annotations.NotNull()
    com.crowdshield.stampede.domain.RiskLevel level, float score) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void MockMapView(float riskScore) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void MockDataControl(boolean enabled, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onToggle) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void EmergencyButtonWithCountdown(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onEmergencyClick) {
    }
}