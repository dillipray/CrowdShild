package com.crowdshield.stampede.ui;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u000f\u001a\u00020\u0010H\u0002J\u0012\u0010\u0011\u001a\u00020\u00102\b\u0010\u0012\u001a\u0004\u0018\u00010\u0013H\u0014J\b\u0010\u0014\u001a\u00020\u0010H\u0014J\b\u0010\u0015\u001a\u00020\u0010H\u0002J\b\u0010\u0016\u001a\u00020\u0010H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001b\u0010\t\u001a\u00020\n8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\r\u0010\u000e\u001a\u0004\b\u000b\u0010\f\u00a8\u0006\u0017"}, d2 = {"Lcom/crowdshield/stampede/ui/MainActivity;", "Landroidx/activity/ComponentActivity;", "()V", "connection", "Landroid/content/ServiceConnection;", "crowdService", "Lcom/crowdshield/stampede/service/CrowdMonitorService;", "isBound", "", "viewModel", "Lcom/crowdshield/stampede/ui/MainViewModel;", "getViewModel", "()Lcom/crowdshield/stampede/ui/MainViewModel;", "viewModel$delegate", "Lkotlin/Lazy;", "observeService", "", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "requestPermissions", "startMonitoringService", "app_debug"})
public final class MainActivity extends androidx.activity.ComponentActivity {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy viewModel$delegate = null;
    @org.jetbrains.annotations.Nullable()
    private com.crowdshield.stampede.service.CrowdMonitorService crowdService;
    private boolean isBound = false;
    @org.jetbrains.annotations.NotNull()
    private final android.content.ServiceConnection connection = null;
    
    public MainActivity() {
        super(0);
    }
    
    private final com.crowdshield.stampede.ui.MainViewModel getViewModel() {
        return null;
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void observeService() {
    }
    
    private final void startMonitoringService() {
    }
    
    private final void requestPermissions() {
    }
    
    @java.lang.Override()
    protected void onDestroy() {
    }
}