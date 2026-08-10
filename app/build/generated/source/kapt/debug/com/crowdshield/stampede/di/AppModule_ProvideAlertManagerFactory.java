package com.crowdshield.stampede.di;

import android.content.Context;
import com.crowdshield.stampede.notification.AlertManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class AppModule_ProvideAlertManagerFactory implements Factory<AlertManager> {
  private final Provider<Context> contextProvider;

  public AppModule_ProvideAlertManagerFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public AlertManager get() {
    return provideAlertManager(contextProvider.get());
  }

  public static AppModule_ProvideAlertManagerFactory create(Provider<Context> contextProvider) {
    return new AppModule_ProvideAlertManagerFactory(contextProvider);
  }

  public static AlertManager provideAlertManager(Context context) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideAlertManager(context));
  }
}
