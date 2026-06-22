package com.meusremedios.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Entidade Room de um horário de agendamento. */
@Entity(
    tableName = "schedule_times",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medication_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["medication_id"])],
)
data class ScheduleTimeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "medication_id")
    val medicationId: Long,
    @ColumnInfo(name = "time_of_day")
    val timeOfDay: String,
    @ColumnInfo(name = "days_of_week_mask")
    val daysOfWeekMask: Int,
)
