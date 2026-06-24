package com.meusremedios.data.repository

import com.meusremedios.data.local.dao.IntakeLogDao
import com.meusremedios.data.local.mapper.toDomain
import com.meusremedios.data.local.mapper.toEntity
import com.meusremedios.domain.model.IntakeLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class IntakeLogRepositoryImpl @Inject constructor(
    private val intakeLogDao: IntakeLogDao,
) : IntakeLogRepository {

    override fun observeByDate(date: LocalDate): Flow<List<IntakeLog>> =
        intakeLogDao.observeByDate(date.toString()).map { entities -> entities.map { it.toDomain() } }

    override fun observeByMedication(medicationId: Long): Flow<List<IntakeLog>> =
        intakeLogDao.observeByMedication(medicationId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun add(log: IntakeLog): Long =
        intakeLogDao.insert(log.toEntity())

    override suspend fun update(log: IntakeLog) =
        intakeLogDao.update(log.toEntity())

    override suspend fun delete(log: IntakeLog) =
        intakeLogDao.delete(log.toEntity())

    override suspend fun deleteOlderThan(thresholdDate: LocalDate): Int =
        intakeLogDao.deleteOlderThan(thresholdDate.toString())

    override suspend fun getByMedicationScheduleAndDate(
        medicationId: Long,
        scheduleTimeId: Long,
        date: LocalDate,
    ): IntakeLog? = intakeLogDao
        .getByMedicationScheduleDate(medicationId, scheduleTimeId, date.toString())
        ?.toDomain()
}
