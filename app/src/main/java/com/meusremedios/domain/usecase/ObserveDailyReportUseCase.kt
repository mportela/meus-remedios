package com.meusremedios.domain.usecase

import com.meusremedios.data.repository.IntakeLogRepository
import com.meusremedios.data.repository.MedicationRepository
import com.meusremedios.data.repository.ScheduleRepository
import com.meusremedios.domain.model.DailyReport
import com.meusremedios.domain.model.DoseStatus
import com.meusremedios.domain.model.IntakeLog
import com.meusremedios.domain.model.IntakeStatus
import com.meusremedios.domain.model.Medication
import com.meusremedios.domain.model.ScheduleTime
import com.meusremedios.domain.model.ScheduledDose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject

/**
 * Deriva o relatório de doses esperadas para uma data, cruzando os horários cadastrados
 * com os registros de tomada existentes. Determinístico dado um [Clock].
 */
class ObserveDailyReportUseCase
    @Inject
    constructor(
        private val medicationRepository: MedicationRepository,
        private val scheduleRepository: ScheduleRepository,
        private val intakeLogRepository: IntakeLogRepository,
        private val clock: Clock,
    ) {
        operator fun invoke(date: LocalDate): Flow<DailyReport> =
            combine(
                medicationRepository.observeAll(),
                scheduleRepository.observeAll(),
                intakeLogRepository.observeByDate(date),
            ) { medications, schedules, logs ->
                build(date, medications, schedules, logs)
            }

        private fun build(
            date: LocalDate,
            medications: List<Medication>,
            schedules: List<ScheduleTime>,
            logs: List<IntakeLog>,
        ): DailyReport {
            val now = LocalDateTime.now(clock)
            val medicationsById = medications.associateBy { it.id }
            val weekdayBit = 1 shl (date.dayOfWeek.value - 1)

            val doses =
                schedules.mapNotNull { schedule ->
                    val medication = medicationsById[schedule.medicationId] ?: return@mapNotNull null
                    if (!medication.isActiveOn(date)) return@mapNotNull null
                    if (schedule.daysOfWeekMask and weekdayBit == 0) return@mapNotNull null

                    val log =
                        logs.firstOrNull {
                            it.medicationId == schedule.medicationId && it.scheduleTimeId == schedule.id
                        }
                    val status = resolveStatus(date, schedule.timeOfDay, log, now)

                    ScheduledDose(
                        medicationId = medication.id,
                        medicationName = medication.name,
                        scheduleTimeId = schedule.id,
                        time = schedule.timeOfDay,
                        scheduledAt = LocalDateTime.of(date, schedule.timeOfDay).toInstant(ZoneOffset.UTC),
                        status = status,
                    )
                }.sortedBy { it.time }

            return DailyReport(date = date, doses = doses)
        }

        private fun resolveStatus(
            date: LocalDate,
            time: java.time.LocalTime,
            log: IntakeLog?,
            now: LocalDateTime,
        ): DoseStatus =
            when (log?.status) {
                IntakeStatus.TAKEN -> DoseStatus.TAKEN
                IntakeStatus.SKIPPED -> DoseStatus.SKIPPED
                else -> if (!LocalDateTime.of(date, time).isAfter(now)) DoseStatus.LATE else DoseStatus.PENDING
            }

        private fun Medication.isActiveOn(date: LocalDate): Boolean {
            val afterStart = startDate?.let { !date.isBefore(it) } ?: true
            val beforeEnd = endDate?.let { !date.isAfter(it) } ?: true
            return afterStart && beforeEnd
        }
    }
