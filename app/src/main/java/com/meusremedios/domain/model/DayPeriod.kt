package com.meusremedios.domain.model

import java.time.LocalTime

/** Período do dia para agrupar doses no relatório (RN-2.3). */
enum class DayPeriod {
    MORNING,
    AFTERNOON,
    NIGHT,
    ;

    companion object {
        private val MORNING_START: LocalTime = LocalTime.of(5, 0)
        private val AFTERNOON_START: LocalTime = LocalTime.of(12, 0)
        private val NIGHT_START: LocalTime = LocalTime.of(18, 0)

        /** Classifica um horário em manhã `[05:00,12:00)`, tarde `[12:00,18:00)` ou noite. */
        fun fromTime(time: LocalTime): DayPeriod =
            when {
                time >= MORNING_START && time < AFTERNOON_START -> MORNING
                time >= AFTERNOON_START && time < NIGHT_START -> AFTERNOON
                else -> NIGHT
            }
    }
}
