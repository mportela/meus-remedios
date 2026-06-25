package com.meusremedios.domain.usecase

import com.meusremedios.data.repository.IntakeLogRepository
import com.meusremedios.data.repository.MedicationRepository
import com.meusremedios.data.repository.ScheduleRepository
import com.meusremedios.domain.model.DoseStatus
import com.meusremedios.domain.model.IntakeStatus
import com.meusremedios.domain.model.Medication
import com.meusremedios.domain.model.ScheduledDose
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject

/**
 * Retorna as doses pendentes ou atrasadas de hoje para um medicamento específico.
 * Usado pela tela de reconhecimento para determinar qual horário marcar ao tocar "Tomei".
 */
class GetPendingDosesTodayForMedicationUseCase
    @Inject
    constructor(
        private val medicationRepository: MedicationRepository,
        private val scheduleRepository: ScheduleRepository,
        private val intakeLogRepository: IntakeLogRepository,
        private val clock: Clock,
    ) {
        suspend operator fun invoke(medicationId: Long): List<ScheduledDose> {
            val today = LocalDate.now(clock)
            val now = LocalDateTime.now(clock)
            val medication = medicationRepository.getById(medicationId) ?: return emptyList()
            if (!medication.isActiveOn(today)) return emptyList()

            val weekdayBit = 1 shl (today.dayOfWeek.value - 1)
            val schedules =
                scheduleRepository.getAll()
                    .filter { it.medicationId == medicationId && it.daysOfWeekMask and weekdayBit != 0 }

            if (schedules.isEmpty()) return emptyList()

            val logs =
                intakeLogRepository.observeByDate(today).first()
                    .filter { it.medicationId == medicationId }
                    .associateBy { it.scheduleTimeId }

            return schedules.mapNotNull { schedule ->
                val log = logs[schedule.id]
                if (log?.status == IntakeStatus.TAKEN || log?.status == IntakeStatus.SKIPPED) return@mapNotNull null
                val status = if (!LocalDateTime.of(today, schedule.timeOfDay).isAfter(now)) DoseStatus.LATE else DoseStatus.PENDING
                ScheduledDose(
                    medicationId = medicationId,
                    medicationName = medication.name,
                    scheduleTimeId = schedule.id,
                    time = schedule.timeOfDay,
                    scheduledAt = LocalDateTime.of(today, schedule.timeOfDay).toInstant(ZoneOffset.UTC),
                    status = status,
                )
            }.sortedBy { it.time }
        }

        private fun Medication.isActiveOn(date: LocalDate): Boolean {
            val afterStart = startDate?.let { !date.isBefore(it) } ?: true
            val beforeEnd = endDate?.let { !date.isAfter(it) } ?: true
            return afterStart && beforeEnd
        }
    }
