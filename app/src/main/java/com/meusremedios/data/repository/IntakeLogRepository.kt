package com.meusremedios.data.repository

import com.meusremedios.domain.model.IntakeLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** Mediação entre o domínio e a fonte de dados local de registros de tomada. */
interface IntakeLogRepository {
    fun observeByDate(date: LocalDate): Flow<List<IntakeLog>>
    fun observeByMedication(medicationId: Long): Flow<List<IntakeLog>>
    suspend fun add(log: IntakeLog): Long
    suspend fun update(log: IntakeLog)
    suspend fun delete(log: IntakeLog)
    suspend fun deleteOlderThan(thresholdDate: LocalDate): Int
    suspend fun getByMedicationScheduleAndDate(medicationId: Long, scheduleTimeId: Long, date: LocalDate): IntakeLog?
}
