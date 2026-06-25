package com.meusremedios.data.repository

import com.meusremedios.data.local.dao.ScheduleTimeDao
import com.meusremedios.data.local.mapper.toDomain
import com.meusremedios.data.local.mapper.toEntity
import com.meusremedios.domain.model.ScheduleTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ScheduleRepositoryImpl
    @Inject
    constructor(
        private val scheduleTimeDao: ScheduleTimeDao,
    ) : ScheduleRepository {
        override fun observeByMedication(medicationId: Long): Flow<List<ScheduleTime>> =
            scheduleTimeDao.observeByMedication(medicationId).map { entities -> entities.map { it.toDomain() } }

        override fun observeAll(): Flow<List<ScheduleTime>> =
            scheduleTimeDao.observeAll().map { entities -> entities.map { it.toDomain() } }

        override suspend fun getAll(): List<ScheduleTime> = scheduleTimeDao.getAll().map { it.toDomain() }

        override suspend fun add(scheduleTime: ScheduleTime): Long = scheduleTimeDao.insert(scheduleTime.toEntity())

        override suspend fun update(scheduleTime: ScheduleTime) = scheduleTimeDao.update(scheduleTime.toEntity())

        override suspend fun delete(scheduleTime: ScheduleTime) = scheduleTimeDao.delete(scheduleTime.toEntity())
    }
