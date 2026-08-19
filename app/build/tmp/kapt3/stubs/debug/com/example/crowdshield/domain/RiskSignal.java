package com.example.crowdshield.domain;

/**
 * Data Model for Citizen Crowd Risk Signal.
 * Used for real-time crowd status, navigation guidance, and risk level.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0004\n\u0002\u0010\t\n\u0002\b\u0012\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0087\b\u0018\u00002\u00020\u0001BA\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0003\u0012\b\b\u0002\u0010\b\u001a\u00020\u0003\u0012\b\b\u0002\u0010\t\u001a\u00020\n\u00a2\u0006\u0002\u0010\u000bJ\t\u0010\u0015\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0016\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0017\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0018\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0019\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001a\u001a\u00020\nH\u00c6\u0003JE\u0010\u001b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\u00032\b\b\u0002\u0010\b\u001a\u00020\u00032\b\b\u0002\u0010\t\u001a\u00020\nH\u00c6\u0001J\u0013\u0010\u001c\u001a\u00020\u001d2\b\u0010\u001e\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001f\u001a\u00020 H\u00d6\u0001J\t\u0010!\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\u0007\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\rR\u0011\u0010\b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\rR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\rR\u0011\u0010\t\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014\u00a8\u0006\""}, d2 = {"Lcom/example/crowdshield/domain/RiskSignal;", "", "sectorId", "", "riskScore", "", "densityLevel", "headline", "navigationGuidance", "timestamp", "", "(Ljava/lang/String;FLjava/lang/String;Ljava/lang/String;Ljava/lang/String;J)V", "getDensityLevel", "()Ljava/lang/String;", "getHeadline", "getNavigationGuidance", "getRiskScore", "()F", "getSectorId", "getTimestamp", "()J", "component1", "component2", "component3", "component4", "component5", "component6", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
public final class RiskSignal {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String sectorId = null;
    private final float riskScore = 0.0F;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String densityLevel = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String headline = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String navigationGuidance = null;
    private final long timestamp = 0L;
    
    public RiskSignal(@org.jetbrains.annotations.NotNull()
    java.lang.String sectorId, float riskScore, @org.jetbrains.annotations.NotNull()
    java.lang.String densityLevel, @org.jetbrains.annotations.NotNull()
    java.lang.String headline, @org.jetbrains.annotations.NotNull()
    java.lang.String navigationGuidance, long timestamp) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getSectorId() {
        return null;
    }
    
    public final float getRiskScore() {
        return 0.0F;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getDensityLevel() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getHeadline() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getNavigationGuidance() {
        return null;
    }
    
    public final long getTimestamp() {
        return 0L;
    }
    
    public RiskSignal() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    public final float component2() {
        return 0.0F;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component5() {
        return null;
    }
    
    public final long component6() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.crowdshield.domain.RiskSignal copy(@org.jetbrains.annotations.NotNull()
    java.lang.String sectorId, float riskScore, @org.jetbrains.annotations.NotNull()
    java.lang.String densityLevel, @org.jetbrains.annotations.NotNull()
    java.lang.String headline, @org.jetbrains.annotations.NotNull()
    java.lang.String navigationGuidance, long timestamp) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}