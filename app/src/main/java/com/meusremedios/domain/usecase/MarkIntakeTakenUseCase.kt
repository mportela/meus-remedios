package com.meusremedios.domain.usecase

import com.meusremedios.data.repository.IntakeLogRepository
import com.meusremedios.domain.model.IntakeLog
import com.meusremedios.domain.model.IntakeStatus
import com.meusremedios.domain.model.ScheduledDose
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

class MarkIntakeTakenUseCase
    @Inject
    constructor(
        private val repository: IntakeLogRepository,
        private val clock: Clock,
    ) {
        /** Marca/desmarca (toggle) uma dose agendada como TAKEN. */
        suspend operator fun invoke(
            dose: ScheduledDose,
            date: LocalDate,
        ) {
            val existing = repository.getByMedicationScheduleAndDate(dose.medicationId, dose.scheduleTimeId, date)
            when (existing?.status) {
                IntakeStatus.TAKEN -> repository.delete(existing)
                IntakeStatus.SKIPPED -> repository.update(existing.copy(status = IntakeStatus.TAKEN, takenAt = Instant.now(clock)))
                else ->
                    repository.add(
                        IntakeLog(
                            medicationId = dose.medicationId,
                            scheduleTimeId = dose.scheduleTimeId,
                            date = date,
                            scheduledAt = dose.scheduledAt,
                            takenAt = Instant.now(clock),
                            status = IntakeStatus.TAKEN,
                        ),
                    )
            }
        }

        /** Registra tomada ad-hoc (sem horário agendado — ex.: dose extra ou via reconhecimento). */
        suspend operator fun invoke(
            medicationId: Long,
            date: LocalDate,
        ) {
            val now = Instant.now(clock)
            repository.add(
                IntakeLog(
                    medicationId = medicationId,
                    scheduleTimeId = null,
                    date = date,
                    scheduledAt = now,
                    takenAt = now,
                    status = IntakeStatus.TAKEN,
                ),
            )
        }
    }
