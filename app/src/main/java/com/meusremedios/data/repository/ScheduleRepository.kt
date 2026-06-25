package com.meusremedios.data.repository

import com.meusremedios.domain.model.ScheduleTime
import kotlinx.coroutines.flow.Flow

/** Mediação entre o domínio e a fonte de dados local de horários. */
interface ScheduleRepository {
    fun observeByMedication(medicationId: Long): Flow<List<ScheduleTime>>

    fun observeAll(): Flow<List<ScheduleTime>>

    suspend fun getAll(): List<ScheduleTime>

    suspend fun add(scheduleTime: ScheduleTime): Long

    suspend fun update(scheduleTime: ScheduleTime)

    suspend fun delete(scheduleTime: ScheduleTime)
}
