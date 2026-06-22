package com.meusremedios.domain.model

import java.time.LocalTime

/**
 * Horário de agendamento de um medicamento.
 *
 * @param id identificador (0 = ainda não persistido).
 * @param medicationId medicamento ao qual o horário pertence.
 * @param timeOfDay hora do dia do lembrete.
 * @param daysOfWeekMask bitmask de 7 bits (bit 0 = segunda ... bit 6 = domingo).
 */
data class ScheduleTime(
    val id: Long = 0,
    val medicationId: Long,
    val timeOfDay: LocalTime,
    val daysOfWeekMask: Int = ALL_DAYS_MASK,
) {
    companion object {
        /** Máscara com todos os 7 dias da semana ativos. */
        const val ALL_DAYS_MASK: Int = 0b111_1111
    }
}
