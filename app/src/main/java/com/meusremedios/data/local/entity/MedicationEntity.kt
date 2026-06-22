package com.meusremedios.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.meusremedios.domain.model.PeriodType

/** Entidade Room de um medicamento. */
@Entity(
    tableName = "medications",
    indices = [Index(value = ["name"])],
)
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val dosage: String?,
    val notes: String?,
    val periodType: PeriodType,
    val startDate: String?,
    val endDate: String?,
    @ColumnInfo(name = "reminders_enabled")
    val remindersEnabled: Boolean,
    @ColumnInfo(name = "created_at")
    val createdAt: String,
)
