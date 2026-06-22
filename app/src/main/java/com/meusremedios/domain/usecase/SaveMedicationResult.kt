package com.meusremedios.domain.usecase

/**
 * Resultado de uma tentativa de salvar um medicamento.
 */
sealed interface SaveMedicationResult {
    /** Salvo com sucesso; [medicationId] é o id persistido. */
    data class Success(val medicationId: Long) : SaveMedicationResult

    /** Falha de validação de regra de negócio. */
    data class Invalid(val error: MedicationValidationError) : SaveMedicationResult
}

/** Erros de validação ao salvar um medicamento. */
enum class MedicationValidationError {
    /** Nome em branco (RN-1.1). */
    BLANK_NAME,

    /** Data de término anterior à data de início (RN-1.3). */
    END_DATE_BEFORE_START_DATE,
}
