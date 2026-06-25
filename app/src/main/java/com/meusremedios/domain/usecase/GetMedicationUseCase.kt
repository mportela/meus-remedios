package com.meusremedios.domain.usecase

import com.meusremedios.data.repository.MedicationRepository
import com.meusremedios.data.repository.ScheduleRepository
import com.meusremedios.domain.model.MedicationWithSchedules
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** Carrega um medicamento com seus horários por id, ou `null` se não existir. */
class GetMedicationUseCase
    @Inject
    constructor(
        private val medicationRepository: MedicationRepository,
        private val scheduleRepository: ScheduleRepository,
    ) {
        suspend operator fun invoke(id: Long): MedicationWithSchedules? {
            val medication = medicationRepository.getById(id) ?: return null
            val schedules = scheduleRepository.observeByMedication(id).first()
            return MedicationWithSchedules(medication, schedules)
        }
    }
