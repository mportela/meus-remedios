package com.meusremedios.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.meusremedios.domain.model.IntakeStatus

/** Entidade Room de um registro de tomada. */
@Entity(
    tableName = "intake_logs",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medication_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ScheduleTimeEntity::class,
            parentColumns = ["id"],
            childColumns = ["schedule_time_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["medication_id"]),
        Index(value = ["schedule_time_id"]),
        Index(value = ["date"]),
    ],
)
data class IntakeLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "medication_id")
    val medicationId: Long,
    @ColumnInfo(name = "schedule_time_id")
    val scheduleTimeId: Long?,
    val date: String,
    @ColumnInfo(name = "scheduled_at")
    val scheduledAt: String,
    @ColumnInfo(name = "taken_at")
    val takenAt: String?,
    val status: IntakeStatus,
)
