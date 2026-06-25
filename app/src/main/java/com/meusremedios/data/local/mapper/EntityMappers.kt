package com.meusremedios.data.local.mapper

import com.meusremedios.data.local.entity.AppSettingsEntity
import com.meusremedios.data.local.entity.IntakeLogEntity
import com.meusremedios.data.local.entity.MedicationEntity
import com.meusremedios.data.local.entity.MedicationPhotoEntity
import com.meusremedios.data.local.entity.ScheduleTimeEntity
import com.meusremedios.domain.model.AppSettings
import com.meusremedios.domain.model.IntakeLog
import com.meusremedios.domain.model.Medication
import com.meusremedios.domain.model.MedicationPhoto
import com.meusremedios.domain.model.ScheduleTime
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

fun MedicationEntity.toDomain(): Medication =
    Medication(
        id = id,
        name = name,
        dosage = dosage,
        notes = notes,
        periodType = periodType,
        startDate = startDate?.let(LocalDate::parse),
        endDate = endDate?.let(LocalDate::parse),
        remindersEnabled = remindersEnabled,
        createdAt = Instant.parse(createdAt),
    )

fun Medication.toEntity(): MedicationEntity =
    MedicationEntity(
        id = id,
        name = name,
        dosage = dosage,
        notes = notes,
        periodType = periodType,
        startDate = startDate?.toString(),
        endDate = endDate?.toString(),
        remindersEnabled = remindersEnabled,
        createdAt = createdAt.toString(),
    )

fun MedicationPhotoEntity.toDomain(): MedicationPhoto =
    MedicationPhoto(
        id = id,
        medicationId = medicationId,
        filePath = filePath,
        side = side,
        embedding = embedding,
        dominantColorLab = dominantColorLab,
        aspectRatio = aspectRatio,
        imprintText = imprintText,
        createdAt = Instant.parse(createdAt),
    )

fun MedicationPhoto.toEntity(): MedicationPhotoEntity =
    MedicationPhotoEntity(
        id = id,
        medicationId = medicationId,
        filePath = filePath,
        side = side,
        embedding = embedding,
        dominantColorLab = dominantColorLab,
        aspectRatio = aspectRatio,
        imprintText = imprintText,
        createdAt = createdAt.toString(),
    )

fun ScheduleTimeEntity.toDomain(): ScheduleTime =
    ScheduleTime(
        id = id,
        medicationId = medicationId,
        timeOfDay = LocalTime.parse(timeOfDay),
        daysOfWeekMask = daysOfWeekMask,
    )

fun ScheduleTime.toEntity(): ScheduleTimeEntity =
    ScheduleTimeEntity(
        id = id,
        medicationId = medicationId,
        timeOfDay = timeOfDay.toString(),
        daysOfWeekMask = daysOfWeekMask,
    )

fun IntakeLogEntity.toDomain(): IntakeLog =
    IntakeLog(
        id = id,
        medicationId = medicationId,
        scheduleTimeId = scheduleTimeId,
        date = LocalDate.parse(date),
        scheduledAt = Instant.parse(scheduledAt),
        takenAt = takenAt?.let(Instant::parse),
        status = status,
    )

fun IntakeLog.toEntity(): IntakeLogEntity =
    IntakeLogEntity(
        id = id,
        medicationId = medicationId,
        scheduleTimeId = scheduleTimeId,
        date = date.toString(),
        scheduledAt = scheduledAt.toString(),
        takenAt = takenAt?.toString(),
        status = status,
    )

fun AppSettingsEntity.toDomain(): AppSettings =
    AppSettings(
        historyRetentionDays = historyRetentionDays,
        autoCapture = autoCapture,
        remindersGlobal = remindersGlobal,
        reminderLeadMinutes = reminderLeadMinutes,
        fontScale = fontScale,
        highContrast = highContrast,
    )

fun AppSettings.toEntity(): AppSettingsEntity =
    AppSettingsEntity(
        id = AppSettingsEntity.SINGLETON_ID,
        historyRetentionDays = historyRetentionDays,
        autoCapture = autoCapture,
        remindersGlobal = remindersGlobal,
        reminderLeadMinutes = reminderLeadMinutes,
        fontScale = fontScale,
        highContrast = highContrast,
    )
