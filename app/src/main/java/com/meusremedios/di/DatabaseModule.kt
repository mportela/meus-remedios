package com.meusremedios.di

import android.content.Context
import androidx.room.Room
import com.meusremedios.data.local.MeusRemediosDatabase
import com.meusremedios.data.local.dao.AppSettingsDao
import com.meusremedios.data.local.dao.IntakeLogDao
import com.meusremedios.data.local.dao.MedicationDao
import com.meusremedios.data.local.dao.MedicationPhotoDao
import com.meusremedios.data.local.dao.ScheduleTimeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Provê o banco de dados local e seus DAOs. */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): MeusRemediosDatabase = Room.databaseBuilder(
        context,
        MeusRemediosDatabase::class.java,
        MeusRemediosDatabase.DATABASE_NAME,
    ).build()

    @Provides
    fun provideMedicationDao(db: MeusRemediosDatabase): MedicationDao = db.medicationDao()

    @Provides
    fun provideMedicationPhotoDao(db: MeusRemediosDatabase): MedicationPhotoDao = db.medicationPhotoDao()

    @Provides
    fun provideScheduleTimeDao(db: MeusRemediosDatabase): ScheduleTimeDao = db.scheduleTimeDao()

    @Provides
    fun provideIntakeLogDao(db: MeusRemediosDatabase): IntakeLogDao = db.intakeLogDao()

    @Provides
    fun provideAppSettingsDao(db: MeusRemediosDatabase): AppSettingsDao = db.appSettingsDao()
}
