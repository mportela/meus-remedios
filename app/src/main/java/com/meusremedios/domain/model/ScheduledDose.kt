package com.meusremedios.domain.model

import java.time.Instant
import java.time.LocalTime

/** Uma dose esperada num dia, derivada de um horário de um medicamento. */
data class ScheduledDose(
    val medicationId: Long,
    val medicationName: String,
    val scheduleTimeId: Long,
    val time: LocalTime,
    val scheduledAt: Instant,
    val status: DoseStatus,
    val period: DayPeriod = DayPeriod.fromTime(time),
)
