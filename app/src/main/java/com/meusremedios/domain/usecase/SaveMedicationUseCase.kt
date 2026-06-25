package com.meusremedios.domain.usecase

import com.meusremedios.data.repository.MedicationRepository
import com.meusremedios.data.repository.ScheduleRepository
import com.meusremedios.domain.model.Medication
import com.meusremedios.domain.model.ScheduleTime
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Cria ou atualiza um medicamento junto de seus horários, aplicando as regras de
 * negócio do PRD-1:
 * - RN-1.1: nome é obrigatório (não pode ser vazio ou só espaços).
 * - RN-1.3: se houver data de término e de início, o término deve ser ≥ início.
 *
 * Na edição, os horários são reconciliados: os ausentes na nova lista são
 * removidos, os novos são adicionados e os existentes são atualizados.
 */
class SaveMedicationUseCase
    @Inject
    constructor(
        private val medicationRepository: MedicationRepository,
        private val scheduleRepository: ScheduleRepository,
        private val rescheduleAllAlarmsUseCase: RescheduleAllAlarmsUseCase,
    ) {
        suspend operator fun invoke(
            medication: Medication,
            schedules: List<ScheduleTime>,
        ): SaveMedicationResult {
            val trimmedName = medication.name.trim()
            if (trimmedName.isEmpty()) {
                return SaveMedicationResult.Invalid(MedicationValidationError.BLANK_NAME)
            }
            val start = medication.startDate
            val end = medication.endDate
            if (start != null && end != null && end.isBefore(start)) {
                return SaveMedicationResult.Invalid(
                    MedicationValidationError.END_DATE_BEFORE_START_DATE,
                )
            }

            val normalized = medication.copy(name = trimmedName)
            val medicationId: Long
            if (normalized.id == 0L) {
                medicationId = medicationRepository.add(normalized)
            } else {
                medicationRepository.update(normalized)
                medicationId = normalized.id
            }

            reconcileSchedules(medicationId, schedules)
            runCatching { rescheduleAllAlarmsUseCase() }
            return SaveMedicationResult.Success(medicationId)
        }

        private suspend fun reconcileSchedules(
            medicationId: Long,
            schedules: List<ScheduleTime>,
        ) {
            val existing = scheduleRepository.observeByMedication(medicationId).first()
            val incomingIds = schedules.mapNotNull { it.id.takeIf { id -> id != 0L } }.toSet()

            existing
                .filter { it.id !in incomingIds }
                .forEach { scheduleRepository.delete(it) }

            schedules.forEach { schedule ->
                val owned = schedule.copy(medicationId = medicationId)
                if (owned.id == 0L) {
                    scheduleRepository.add(owned)
                } else {
                    scheduleRepository.update(owned)
                }
            }
        }
    }
