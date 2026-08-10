package com.crowdshield.stampede.di;

import com.crowdshield.stampede.data.AppDatabase;
import com.crowdshield.stampede.data.IncidentDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class AppModule_ProvideIncidentDaoFactory implements Factory<IncidentDao> {
  private final Provider<AppDatabase> databaseProvider;

  public AppModule_ProvideIncidentDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public IncidentDao get() {
    return provideIncidentDao(databaseProvider.get());
  }

  public static AppModule_ProvideIncidentDaoFactory create(Provider<AppDatabase> databaseProvider) {
    return new AppModule_ProvideIncidentDaoFactory(databaseProvider);
  }

  public static IncidentDao provideIncidentDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideIncidentDao(database));
  }
}
