package com.kurakulas.app.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.kurakulas.app.data.local.SessionManager
import com.kurakulas.app.data.local.AppointmentPointsManager
import com.kurakulas.app.data.BankerRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideSessionManager(
        @ApplicationContext context: Context
    ): SessionManager {
        return SessionManager(context)
    }

    @Provides
    @Singleton
    fun provideAppointmentPointsManager(
        @ApplicationContext context: Context
    ): AppointmentPointsManager {
        return AppointmentPointsManager(context)
    }

    @Provides
    @Singleton
    fun provideBankerRepository(): BankerRepository {
        return BankerRepository()
    }
} 
