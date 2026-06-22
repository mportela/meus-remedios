package com.meusremedios.domain.model

/**
 * Estado de um registro de tomada.
 * - [PENDING]: agendada e ainda não confirmada.
 * - [TAKEN]: confirmada como tomada.
 * - [SKIPPED]: explicitamente pulada pelo usuário.
 * - [LATE]: tomada após o horário previsto.
 */
enum class IntakeStatus {
    PENDING,
    TAKEN,
    SKIPPED,
    LATE,
}
