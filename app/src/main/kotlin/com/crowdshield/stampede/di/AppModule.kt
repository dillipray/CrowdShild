package com.crowdshield.stampede.di

import android.content.Context
import com.crowdshield.stampede.data.AppDatabase
import com.crowdshield.stampede.data.IncidentDao
import com.crowdshield.stampede.domain.RiskCalculator
import com.crowdshield.stampede.notification.AlertManager
import com.crowdshield.stampede.repository.IncidentRepository
import com.crowdshield.stampede.repository.IncidentRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindIncidentRepository(
        implementation: IncidentRepositoryImpl
    ): IncidentRepository

    companion object {
        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            return OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()
        }

        @Provides
        @Singleton
        fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
            return AppDatabase.getDatabase(context)
        }

        @Provides
        @Singleton
        fun provideIncidentDao(database: AppDatabase): IncidentDao {
            return database.incidentDao()
        }

        @Provides
        @Singleton
        fun provideRiskCalculator(): RiskCalculator {
            return RiskCalculator()
        }

        @Provides
        @Singleton
        fun provideAlertManager(@ApplicationContext context: Context): AlertManager {
            return AlertManager(context)
        }
    }
}
