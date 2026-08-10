package com.crowdshield.stampede.ui;

import com.crowdshield.stampede.data.IncidentDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class MainViewModel_Factory implements Factory<MainViewModel> {
  private final Provider<IncidentDao> incidentDaoProvider;

  public MainViewModel_Factory(Provider<IncidentDao> incidentDaoProvider) {
    this.incidentDaoProvider = incidentDaoProvider;
  }

  @Override
  public MainViewModel get() {
    return newInstance(incidentDaoProvider.get());
  }

  public static MainViewModel_Factory create(Provider<IncidentDao> incidentDaoProvider) {
    return new MainViewModel_Factory(incidentDaoProvider);
  }

  public static MainViewModel newInstance(IncidentDao incidentDao) {
    return new MainViewModel(incidentDao);
  }
}
