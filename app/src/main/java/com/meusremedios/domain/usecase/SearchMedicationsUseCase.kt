package com.meusremedios.domain.usecase

import com.meusremedios.data.repository.MedicationRepository
import com.meusremedios.domain.model.Medication
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Busca medicamentos por uma consulta textual de nome. Quando a consulta está
 * em branco, retorna a lista completa ordenada por nome.
 */
class SearchMedicationsUseCase @Inject constructor(
    private val medicationRepository: MedicationRepository,
) {
    operator fun invoke(query: String): Flow<List<Medication>> {
        val trimmed = query.trim()
        return if (trimmed.isEmpty()) {
            medicationRepository.observeAll()
        } else {
            medicationRepository.search(trimmed)
        }
    }
}
