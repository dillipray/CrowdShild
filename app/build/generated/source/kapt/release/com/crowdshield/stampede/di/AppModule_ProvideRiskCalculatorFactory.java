package com.crowdshield.stampede.di;

import com.crowdshield.stampede.domain.RiskCalculator;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class AppModule_ProvideRiskCalculatorFactory implements Factory<RiskCalculator> {
  @Override
  public RiskCalculator get() {
    return provideRiskCalculator();
  }

  public static AppModule_ProvideRiskCalculatorFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static RiskCalculator provideRiskCalculator() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideRiskCalculator());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideRiskCalculatorFactory INSTANCE = new AppModule_ProvideRiskCalculatorFactory();
  }
}
