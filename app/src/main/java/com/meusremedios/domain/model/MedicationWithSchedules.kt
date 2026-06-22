package com.meusremedios.domain.model

/**
 * Agregado de um medicamento com seus horários de agendamento, usado pela UI de
 * cadastro/edição e detalhe.
 */
data class MedicationWithSchedules(
    val medication: Medication,
    val schedules: List<ScheduleTime> = emptyList(),
)
