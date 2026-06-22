package com.meusremedios.domain.model

import java.time.Instant
import java.time.LocalDate

/**
 * Registro de uma tomada (prevista ou realizada).
 *
 * @param id identificador (0 = ainda não persistido).
 * @param medicationId medicamento associado.
 * @param scheduleTimeId horário de origem, se houver.
 * @param date dia ao qual a tomada se refere.
 * @param scheduledAt instante previsto.
 * @param takenAt instante em que foi efetivamente tomada, se houver.
 * @param status estado atual da tomada.
 */
data class IntakeLog(
    val id: Long = 0,
    val medicationId: Long,
    val scheduleTimeId: Long? = null,
    val date: LocalDate,
    val scheduledAt: Instant,
    val takenAt: Instant? = null,
    val status: IntakeStatus = IntakeStatus.PENDING,
)
