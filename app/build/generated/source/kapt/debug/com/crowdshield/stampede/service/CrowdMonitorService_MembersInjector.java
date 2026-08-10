package com.crowdshield.stampede.service;

import com.crowdshield.stampede.domain.RiskCalculator;
import com.crowdshield.stampede.notification.AlertManager;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class CrowdMonitorService_MembersInjector implements MembersInjector<CrowdMonitorService> {
  private final Provider<RiskCalculator> riskCalculatorProvider;

  private final Provider<AlertManager> alertManagerProvider;

  public CrowdMonitorService_MembersInjector(Provider<RiskCalculator> riskCalculatorProvider,
      Provider<AlertManager> alertManagerProvider) {
    this.riskCalculatorProvider = riskCalculatorProvider;
    this.alertManagerProvider = alertManagerProvider;
  }

  public static MembersInjector<CrowdMonitorService> create(
      Provider<RiskCalculator> riskCalculatorProvider,
      Provider<AlertManager> alertManagerProvider) {
    return new CrowdMonitorService_MembersInjector(riskCalculatorProvider, alertManagerProvider);
  }

  @Override
  public void injectMembers(CrowdMonitorService instance) {
    injectRiskCalculator(instance, riskCalculatorProvider.get());
    injectAlertManager(instance, alertManagerProvider.get());
  }

  @InjectedFieldSignature("com.crowdshield.stampede.service.CrowdMonitorService.riskCalculator")
  public static void injectRiskCalculator(CrowdMonitorService instance,
      RiskCalculator riskCalculator) {
    instance.riskCalculator = riskCalculator;
  }

  @InjectedFieldSignature("com.crowdshield.stampede.service.CrowdMonitorService.alertManager")
  public static void injectAlertManager(CrowdMonitorService instance, AlertManager alertManager) {
    instance.alertManager = alertManager;
  }
}
