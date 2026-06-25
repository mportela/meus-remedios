package com.meusremedios.di

import com.meusremedios.data.repository.IntakeLogRepository
import com.meusremedios.data.repository.MedicationPhotoRepository
import com.meusremedios.data.repository.MedicationRepository
import com.meusremedios.data.repository.ScheduleRepository
import com.meusremedios.data.repository.SettingsRepository
import com.meusremedios.domain.usecase.FakeIntakeLogRepository
import com.meusremedios.domain.usecase.FakeMedicationPhotoRepository
import com.meusremedios.domain.usecase.FakeMedicationRepository
import com.meusremedios.domain.usecase.FakeScheduleRepository
import com.meusremedios.domain.usecase.FakeSettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * Substitui [RepositoryModule] em testes, provendo implementações in-memory
 * para todos os repositórios — sem banco de dados real.
 */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [RepositoryModule::class])
object FakeRepositoryModule {
    @Provides
    @Singleton
    fun provideMedicationRepository(): MedicationRepository = FakeMedicationRepository()

    @Provides
    @Singleton
    fun provideScheduleRepository(): ScheduleRepository = FakeScheduleRepository()

    @Provides
    @Singleton
    fun provideIntakeLogRepository(): IntakeLogRepository = FakeIntakeLogRepository()

    @Provides
    @Singleton
    fun provideMedicationPhotoRepository(): MedicationPhotoRepository = FakeMedicationPhotoRepository()

    @Provides
    @Singleton
    fun provideSettingsRepository(): SettingsRepository = FakeSettingsRepository()
}
