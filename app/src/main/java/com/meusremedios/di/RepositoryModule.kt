package com.meusremedios.di

import com.meusremedios.data.repository.IntakeLogRepository
import com.meusremedios.data.repository.IntakeLogRepositoryImpl
import com.meusremedios.data.repository.MedicationPhotoRepository
import com.meusremedios.data.repository.MedicationPhotoRepositoryImpl
import com.meusremedios.data.repository.MedicationRepository
import com.meusremedios.data.repository.MedicationRepositoryImpl
import com.meusremedios.data.repository.ScheduleRepository
import com.meusremedios.data.repository.ScheduleRepositoryImpl
import com.meusremedios.data.repository.SettingsRepository
import com.meusremedios.data.repository.SettingsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Vincula as interfaces de repositório às suas implementações. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindMedicationRepository(impl: MedicationRepositoryImpl): MedicationRepository

    @Binds
    @Singleton
    abstract fun bindMedicationPhotoRepository(impl: MedicationPhotoRepositoryImpl): MedicationPhotoRepository

    @Binds
    @Singleton
    abstract fun bindScheduleRepository(impl: ScheduleRepositoryImpl): ScheduleRepository

    @Binds
    @Singleton
    abstract fun bindIntakeLogRepository(impl: IntakeLogRepositoryImpl): IntakeLogRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
