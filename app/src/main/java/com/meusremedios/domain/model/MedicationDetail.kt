package com.meusremedios.domain.model

/** Agregado de leitura para a tela de detalhe de um medicamento. */
data class MedicationDetail(
    val medication: Medication,
    val schedules: List<ScheduleTime> = emptyList(),
    val photos: List<MedicationPhoto> = emptyList(),
    val history: List<IntakeLog> = emptyList(),
)
