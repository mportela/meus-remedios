package com.meusremedios.data.repository

import com.meusremedios.data.local.dao.MedicationDao
import com.meusremedios.data.local.mapper.toDomain
import com.meusremedios.data.local.mapper.toEntity
import com.meusremedios.domain.model.Medication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MedicationRepositoryImpl @Inject constructor(
    private val medicationDao: MedicationDao,
) : MedicationRepository {

    override fun observeAll(): Flow<List<Medication>> =
        medicationDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeById(id: Long): Flow<Medication?> =
        medicationDao.observeById(id).map { it?.toDomain() }

    override fun search(query: String): Flow<List<Medication>> =
        medicationDao.search(query).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getById(id: Long): Medication? =
        medicationDao.getById(id)?.toDomain()

    override suspend fun add(medication: Medication): Long =
        medicationDao.insert(medication.toEntity())

    override suspend fun update(medication: Medication) =
        medicationDao.update(medication.toEntity())

    override suspend fun delete(medication: Medication) =
        medicationDao.delete(medication.toEntity())
}
