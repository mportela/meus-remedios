package com.meusremedios.domain.usecase

import com.meusremedios.data.repository.MedicationPhotoRepository
import com.meusremedios.domain.model.MedicationPhoto
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observa as fotos de um medicamento. */
class ObserveMedicationPhotosUseCase
    @Inject
    constructor(
        private val photoRepository: MedicationPhotoRepository,
    ) {
        operator fun invoke(medicationId: Long): Flow<List<MedicationPhoto>> = photoRepository.observeByMedication(medicationId)
    }
