package com.example.crowdshield.ui;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000V\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a\u00cc\u0001\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\u00052\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\n2\u000e\b\u0002\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00010\f2\u000e\b\u0002\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00010\f2\u000e\b\u0002\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00010\f2\u0014\b\u0002\u0010\u000f\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u00010\u00102\u000e\b\u0002\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00010\f2\u0014\b\u0002\u0010\u0012\u001a\u000e\u0012\u0004\u0012\u00020\u0013\u0012\u0004\u0012\u00020\u00010\u00102\u0014\b\u0002\u0010\u0014\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00010\u00102\u000e\b\u0002\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00010\fH\u0007\u001aJ\u0010\u0016\u001a\u00020\u00012\u0006\u0010\u0017\u001a\u00020\n2\u0006\u0010\u0018\u001a\u00020\n2\u0006\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u001c2\b\b\u0002\u0010\u001d\u001a\u00020\u001e2\f\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00010\fH\u0007\u00f8\u0001\u0000\u00a2\u0006\u0004\b \u0010!\u001a\b\u0010\"\u001a\u00020\u0001H\u0007\u001a\b\u0010#\u001a\u00020\u0001H\u0007\u001a\b\u0010$\u001a\u00020\u0001H\u0007\u001a\b\u0010%\u001a\u00020\u0001H\u0007\u001a\u000e\u0010&\u001a\u00020\'2\u0006\u0010(\u001a\u00020\u0013\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006)"}, d2 = {"CitizenDashboardScreen", "", "riskSignal", "Lcom/example/crowdshield/domain/RiskSignal;", "isSosCountingDown", "", "sosCountdownSeconds", "", "isSosDispatched", "bannerMessage", "", "onStartSosCountdown", "Lkotlin/Function0;", "onCancelSosCountdown", "onResetSosDispatch", "onQuickHazardClick", "Lkotlin/Function1;", "onDetailedReportClick", "onSimulationPresetSelect", "Lcom/example/crowdshield/domain/RiskSeverity;", "onMockToggle", "onDismissBanner", "HazardButton", "title", "subtitle", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "accent", "Landroidx/compose/ui/graphics/Color;", "modifier", "Landroidx/compose/ui/Modifier;", "onClick", "HazardButton-Bx497Mc", "(Ljava/lang/String;Ljava/lang/String;Landroidx/compose/ui/graphics/vector/ImageVector;JLandroidx/compose/ui/Modifier;Lkotlin/jvm/functions/Function0;)V", "Preview_CriticalRisk", "Preview_HighRisk", "Preview_LowRisk", "Preview_MediumRisk", "getSeverityStyle", "Lcom/example/crowdshield/ui/SeverityStyle;", "severity", "app_debug"})
public final class CitizenDashboardScreenKt {
    
    @org.jetbrains.annotations.NotNull()
    public static final com.example.crowdshield.ui.SeverityStyle getSeverityStyle(@org.jetbrains.annotations.NotNull()
    com.example.crowdshield.domain.RiskSeverity severity) {
        return null;
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void CitizenDashboardScreen(@org.jetbrains.annotations.NotNull()
    com.example.crowdshield.domain.RiskSignal riskSignal, boolean isSosCountingDown, int sosCountdownSeconds, boolean isSosDispatched, @org.jetbrains.annotations.Nullable()
    java.lang.String bannerMessage, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onStartSosCountdown, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onCancelSosCountdown, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onResetSosDispatch, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onQuickHazardClick, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDetailedReportClick, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.example.crowdshield.domain.RiskSeverity, kotlin.Unit> onSimulationPresetSelect, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onMockToggle, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismissBanner) {
    }
    
    @androidx.compose.ui.tooling.preview.Preview(name = "Low Risk - Safe (Green)", showBackground = true)
    @androidx.compose.runtime.Composable()
    public static final void Preview_LowRisk() {
    }
    
    @androidx.compose.ui.tooling.preview.Preview(name = "Medium Risk - Caution (Orange)", showBackground = true)
    @androidx.compose.runtime.Composable()
    public static final void Preview_MediumRisk() {
    }
    
    @androidx.compose.ui.tooling.preview.Preview(name = "High Risk - Danger (Red)", showBackground = true)
    @androidx.compose.runtime.Composable()
    public static final void Preview_HighRisk() {
    }
    
    @androidx.compose.ui.tooling.preview.Preview(name = "Critical Risk - Emergency (Dark Red)", showBackground = true)
    @androidx.compose.runtime.Composable()
    public static final void Preview_CriticalRisk() {
    }
}