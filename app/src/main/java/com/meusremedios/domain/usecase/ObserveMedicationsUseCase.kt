package com.meusremedios.domain.usecase

import com.meusremedios.data.repository.MedicationRepository
import com.meusremedios.domain.model.Medication
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observa a lista de medicamentos cadastrados, ordenada por nome. */
class ObserveMedicationsUseCase
    @Inject
    constructor(
        private val medicationRepository: MedicationRepository,
    ) {
        operator fun invoke(): Flow<List<Medication>> = medicationRepository.observeAll()
    }
