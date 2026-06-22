package com.meusremedios.domain.model

/** Status de uma dose no relatório do dia. Derivado de [IntakeLog] ou do horário. */
enum class DoseStatus {
    TAKEN,
    PENDING,
    LATE,
    SKIPPED,
}
