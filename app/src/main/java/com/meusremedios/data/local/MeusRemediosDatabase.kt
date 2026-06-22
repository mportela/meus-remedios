package com.meusremedios.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.meusremedios.data.local.dao.AppSettingsDao
import com.meusremedios.data.local.dao.IntakeLogDao
import com.meusremedios.data.local.dao.MedicationDao
import com.meusremedios.data.local.dao.MedicationPhotoDao
import com.meusremedios.data.local.dao.ScheduleTimeDao
import com.meusremedios.data.local.entity.AppSettingsEntity
import com.meusremedios.data.local.entity.IntakeLogEntity
import com.meusremedios.data.local.entity.MedicationEntity
import com.meusremedios.data.local.entity.MedicationPhotoEntity
import com.meusremedios.data.local.entity.ScheduleTimeEntity

/** Banco de dados local Room do app (100% offline, armazenamento privado). */
@Database(
    entities = [
        MedicationEntity::class,
        MedicationPhotoEntity::class,
        ScheduleTimeEntity::class,
        IntakeLogEntity::class,
        AppSettingsEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class MeusRemediosDatabase : RoomDatabase() {

    abstract fun medicationDao(): MedicationDao

    abstract fun medicationPhotoDao(): MedicationPhotoDao

    abstract fun scheduleTimeDao(): ScheduleTimeDao

    abstract fun intakeLogDao(): IntakeLogDao

    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        const val DATABASE_NAME: String = "meus_remedios.db"
    }
}
