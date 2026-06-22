package com.meusremedios.domain.model

import java.time.LocalDate

/** Relatório das doses esperadas em uma data, com contagens derivadas. */
data class DailyReport(
    val date: LocalDate,
    val doses: List<ScheduledDose> = emptyList(),
) {
    val takenCount: Int get() = doses.count { it.status == DoseStatus.TAKEN }
    val pendingCount: Int get() = doses.count { it.status == DoseStatus.PENDING }
    val lateCount: Int get() = doses.count { it.status == DoseStatus.LATE }
    val skippedCount: Int get() = doses.count { it.status == DoseStatus.SKIPPED }

    val isEmpty: Boolean get() = doses.isEmpty()

    /** Doses agrupadas por período, na ordem manhã → tarde → noite. */
    fun byPeriod(): List<Pair<DayPeriod, List<ScheduledDose>>> =
        DayPeriod.entries
            .map { period -> period to doses.filter { it.period == period } }
            .filter { (_, list) -> list.isNotEmpty() }
}
