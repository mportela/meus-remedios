package com.meusremedios.domain.usecase

import com.meusremedios.data.repository.MedicationRepository
import com.meusremedios.domain.model.Medication
import javax.inject.Inject

/**
 * Exclui um medicamento. Os horários e fotos associados são removidos em cascade
 * pela camada de dados (FKs `onDelete = CASCADE`).
 */
class DeleteMedicationUseCase @Inject constructor(
    private val medicationRepository: MedicationRepository,
) {
    suspend operator fun invoke(medication: Medication) {
        medicationRepository.delete(medication)
    }
}
