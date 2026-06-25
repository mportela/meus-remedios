package com.meusremedios.domain.usecase

import com.meusremedios.data.repository.IntakeLogRepository
import com.meusremedios.domain.model.IntakeLog
import com.meusremedios.domain.model.IntakeStatus
import com.meusremedios.domain.model.ScheduledDose
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

class MarkIntakeSkippedUseCase
    @Inject
    constructor(
        private val repository: IntakeLogRepository,
        private val clock: Clock,
    ) {
        /** Marca/desmarca (toggle) uma dose agendada como SKIPPED. */
        suspend operator fun invoke(
            dose: ScheduledDose,
            date: LocalDate,
        ) {
            val existing = repository.getByMedicationScheduleAndDate(dose.medicationId, dose.scheduleTimeId, date)
            when (existing?.status) {
                IntakeStatus.SKIPPED -> repository.delete(existing)
                IntakeStatus.TAKEN -> repository.update(existing.copy(status = IntakeStatus.SKIPPED, takenAt = null))
                else ->
                    repository.add(
                        IntakeLog(
                            medicationId = dose.medicationId,
                            scheduleTimeId = dose.scheduleTimeId,
                            date = date,
                            scheduledAt = dose.scheduledAt,
                            takenAt = null,
                            status = IntakeStatus.SKIPPED,
                        ),
                    )
            }
        }
    }
